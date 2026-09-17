package com.xc.longboatlab.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;

/** Draw spray after vanilla water AND Fabulous translucency compositing.
 * Read scene depth for terrain/hull occlusion, but never write particle-quad depth.
 */
public final class BoatEffectRenderPass extends RenderLayer {
    private record LayerKey(Identifier texture, boolean emissive) {}
    private static final Map<LayerKey, RenderLayer> LAYERS = new HashMap<>();
    private static BufferAllocator allocator;
    private static VertexConsumerProvider.Immediate consumers;

    private BoatEffectRenderPass() {
        super("longboat_effect_pass", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS, 65536, false, true, () -> {}, () -> {});
    }

    public static RenderLayer layer(Identifier texture, boolean emissive) {
        return LAYERS.computeIfAbsent(new LayerKey(texture, emissive), key -> RenderLayer.of(
                "longboat_effect_" + (key.emissive ? "emissive" : "lit"),
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS, 65536, false, true,
                MultiPhaseParameters.builder()
                        .program(key.emissive ? ENTITY_TRANSLUCENT_EMISSIVE_PROGRAM : ENTITY_TRANSLUCENT_PROGRAM)
                        .texture(new Texture(key.texture, false, false))
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).overlay(ENABLE_OVERLAY_COLOR)
                        .depthTest(LEQUAL_DEPTH_TEST).writeMaskState(COLOR_MASK)
                        .target(MAIN_TARGET).build(false)));
    }

    public static void register(BiConsumer<WorldRenderContext, VertexConsumerProvider> water) {
        WorldRenderEvents.LAST.register(context -> {
            if (context.world() == null || context.matrixStack() == null) return;
            if (allocator == null) {
                allocator = new BufferAllocator(65536);
                consumers = VertexConsumerProvider.immediate(allocator);
            }
            // LAST has no context.consumers(): own and flush every submitted layer in this callback.
            float[] color = RenderSystem.getShaderColor().clone();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            try {
                water.accept(context, consumers);
                BoatExhaust.renderWorld(context, consumers);
                consumers.draw();
            } finally {
                // MultiPhase end actions restore depth mask/blending; also restore caller's color.
                RenderSystem.setShaderColor(color[0], color[1], color[2], color[3]);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (allocator != null) allocator.close();
            allocator = null; consumers = null; LAYERS.clear();
        });
    }
}
