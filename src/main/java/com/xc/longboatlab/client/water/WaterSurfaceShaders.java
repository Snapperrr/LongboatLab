package com.xc.longboatlab.client.water;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** Vanilla-pipeline adapter. Shader-masking only water top faces avoids per-tick chunk rebuilds.
 * The replacement mesh is drawn before the late spray pass, and writes its real displaced depth.
 */
public final class WaterSurfaceShaders extends RenderLayer {
    public static final HeightfieldSurface MODEL = new HeightfieldSurface();
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("LongboatLab/Water");
    private static net.minecraft.client.gl.ShaderProgram maskProgram, surfaceProgram;
    private static NativeImageBackedTexture heights, mask, lighting;
    private static VertexBuffer grid;
    private static RenderLayer waterLayer;
    private static int uploaded = -1;
    private static boolean incompatible, prepared, maskUsed;
    private static String pendingNotice;
    private static boolean reportedDraw;
    // Direct VBO draws do not inherit RenderSystem's world model-view matrix.
    // Capture the terrain matrices once: masking and displacement must share a frame.
    private static final Matrix4f frameView = new Matrix4f(), frameProjection = new Matrix4f();

    private WaterSurfaceShaders() {
        super("longboat_surface", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS, 65536, false, false, () -> {}, () -> {});
    }
    public static boolean available() { return !incompatible && maskProgram != null && surfaceProgram != null; }
    public static net.minecraft.client.gl.ShaderProgram terrainProgram(net.minecraft.client.gl.ShaderProgram original) {
        if (!prepared || !available()) return original;
        maskUsed = true;
        return maskProgram;
    }
    public static void register() {
        incompatible = Boolean.getBoolean("longboatlab.disableWaterSurface")
                || FabricLoader.getInstance().isModLoaded("sodium") || FabricLoader.getInstance().isModLoaded("iris");
        if (incompatible) LOG.warn("Local displaced water disabled by JVM option or Sodium/Iris; retaining vanilla water and spray.");
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            prepared = false; maskProgram = surfaceProgram = null; uploaded = -1;
            reportedDraw = false;
            pendingNotice = null;
            if (incompatible) {
                pendingNotice = "message.longboatlab.water_disabled";
                return;
            }
            try {
                context.register(Identifier.of("longboatlab", "water_mask"), VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
                        shader -> maskProgram = shader);
                context.register(Identifier.of("longboatlab", "water_surface"), VertexFormats.POSITION,
                        shader -> {
                            surfaceProgram = shader;
                            if (available()) LOG.info("Vanilla-material displaced water shaders loaded; awaiting nearby hull contact or impact.");
                        });
            } catch (IOException e) {
                maskProgram = surfaceProgram = null;
                pendingNotice = "message.longboatlab.water_shader_failed";
                LOG.error("Water shader loading failed; retaining vanilla water", e);
            }
        });
        // Resource loading often precedes joining a world. Deliver once when chat is available,
        // and allow a later resource reload to clear a stale failure notice.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingNotice != null && client.player != null) {
                client.player.sendMessage(Text.translatable(pendingNotice).formatted(Formatting.YELLOW), false);
                pendingNotice = null;
            }
        });
        WorldRenderEvents.START.register(WaterSurfaceShaders::prepare);
        // Registered before BoatEffectRenderPass: water depth exists when droplets/foam are rendered.
        WorldRenderEvents.LAST.register(WaterSurfaceShaders::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> release());
    }
    private static void release() {
        if (heights != null) heights.close(); if (mask != null) mask.close();
        if (lighting != null) lighting.close(); if (grid != null) grid.close();
        heights = mask = lighting = null; grid = null; uploaded = -1; prepared = false;
    }
    private static void resources() {
        if (heights != null) return;
        heights = new NativeImageBackedTexture(HeightfieldSurface.N, HeightfieldSurface.N, false);
        mask = new NativeImageBackedTexture(HeightfieldSurface.SIZE, HeightfieldSurface.SIZE, false);
        lighting = new NativeImageBackedTexture(HeightfieldSurface.SIZE, HeightfieldSurface.SIZE, false);
        heights.setFilter(false, false); mask.setFilter(false, false); lighting.setFilter(false, false);
        grid = new VertexBuffer(VertexBuffer.Usage.STATIC);
        try (BufferAllocator allocator = new BufferAllocator(HeightfieldSurface.SIZE * HeightfieldSurface.SIZE * 4 * 4 * 12)) {
            BufferBuilder vertices = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            for (int z = 0; z < HeightfieldSurface.N - 1; z++) for (int x = 0; x < HeightfieldSurface.N - 1; x++) {
                float a = x * HeightfieldSurface.STEP, b = z * HeightfieldSurface.STEP, s = HeightfieldSurface.STEP;
                vertices.vertex(a, 0, b); vertices.vertex(a, 0, b + s);
                vertices.vertex(a + s, 0, b + s); vertices.vertex(a + s, 0, b);
            }
            grid.bind(); grid.upload(vertices.end()); VertexBuffer.unbind();
        }
        waterLayer = RenderLayer.of("longboat_displaced_water", VertexFormats.POSITION, VertexFormat.DrawMode.QUADS,
                65536, false, false, MultiPhaseParameters.builder()
                        .program(new RenderPhase.ShaderProgram(() -> surfaceProgram))
                        .depthTest(LEQUAL_DEPTH_TEST).writeMaskState(ALL_MASK).cull(DISABLE_CULLING)
                        .transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).target(MAIN_TARGET).build(false));
    }
    private static int encode(float value) { return Math.round(Math.max(0, Math.min(1, (value + 2) * 0.25f)) * 65535); }
    private static void upload() {
        if (uploaded == MODEL.revision) return;
        var image = heights.getImage();
        for (int z = 0; z < HeightfieldSurface.N; z++) for (int x = 0; x < HeightfieldSurface.N; x++) {
            int i = z * HeightfieldSurface.N + x, h = encode(MODEL.height[i]), p = encode(MODEL.previous[i]);
            // NativeImage is ABGR. RG = current height, BA = previous; each encodes [-2, 2].
            image.setColor(x, z, (p << 16) | h);
        }
        var cells = mask.getImage();
        for (int z = 0; z < HeightfieldSurface.SIZE; z++) for (int x = 0; x < HeightfieldSurface.SIZE; x++) {
            int i = z * HeightfieldSurface.SIZE + x, rgb = MODEL.colors[i];
            cells.setColor(x, z, (MODEL.wet[i] ? 0xff000000 : 0) | ((rgb & 255) << 16) | (rgb & 0xff00) | ((rgb >> 16) & 255));
        }
        var light = lighting.getImage();
        for (int z = 0; z < HeightfieldSurface.SIZE; z++) for (int x = 0; x < HeightfieldSurface.SIZE; x++) {
            int packedLight = MODEL.lights[z * HeightfieldSurface.SIZE + x];
            light.setColor(x, z, 0xff000000 | (packedLight & 255) | (((packedLight >> 16) & 255) << 8));
        }
        heights.upload(); mask.upload(); lighting.upload(); uploaded = MODEL.revision;
    }
    private static void prepare(WorldRenderContext context) {
        prepared = maskUsed = false;
        if (!MODEL.active()) return;
        resources(); upload();
        frameView.set(context.positionMatrix());
        frameProjection.set(context.projectionMatrix());
        var client = MinecraftClient.getInstance();
        Vec3d camera = context.camera().getPos();
        Sprite still = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(Identifier.ofVanilla("block/water_still"));
        Sprite flow = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(Identifier.ofVanilla("block/water_flow"));
        for (net.minecraft.client.gl.ShaderProgram shader : new net.minecraft.client.gl.ShaderProgram[]{maskProgram, surfaceProgram}) {
            shader.addSampler("PatchMask", mask.getGlId());
            shader.getUniformOrDefault("PatchOrigin").set((float) (MODEL.originX - camera.x), (float) (MODEL.baseY - camera.y), (float) (MODEL.originZ - camera.z));
            shader.getUniformOrDefault("WaterStill").set(still.getMinU(), still.getMinV(), still.getMaxU(), still.getMaxV());
            shader.getUniformOrDefault("WaterFlow").set(flow.getMinU(), flow.getMinV(), flow.getMaxU(), flow.getMaxV());
        }
        surfaceProgram.addSampler("HeightField", heights.getGlId());
        surfaceProgram.addSampler("WaterAtlas", client.getTextureManager().getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getGlId());
        surfaceProgram.getUniformOrDefault("TickDelta").set(context.tickCounter().getTickDelta(false));
        surfaceProgram.addSampler("PatchLight", lighting.getGlId());
        surfaceProgram.getUniformOrDefault("WaterUvInset").set(still.getAnimationFrameDelta() * 0.5f);
        surfaceProgram.getUniformOrDefault("UpShade").set(context.world().getBrightness(net.minecraft.util.math.Direction.UP, true));
        prepared = true;
    }
    private static void render(WorldRenderContext context) {
        if (!prepared || !maskUsed || !available()) return;
        float[] color = RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        waterLayer.startDrawing();
        try {
            // context.matrixStack() contains local entity transforms (identity at LAST), not the
            // camera rotation. Using it here left the water in camera axes while terrain stayed in
            // world axes. PatchOrigin already subtracts camera position: do not translate again.
            grid.bind(); grid.draw(frameView, frameProjection, surfaceProgram);
            if (!reportedDraw) {
                LOG.info("Displaced water mesh submitted; translucent terrain shader replacement is active.");
                reportedDraw = true;
            }
        } finally {
            VertexBuffer.unbind(); waterLayer.endDrawing();
            RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
        }
    }
}
