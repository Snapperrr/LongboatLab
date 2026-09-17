package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAbilities;
import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.LongboatLab;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/** Compact instruments with one row of directional jet reservoirs. */
public final class BoatHud {
    private static final Identifier ATLAS = Identifier.of(LongboatLab.ID, "textures/gui/boat_hud.png");
    private static final Path SETTINGS = FabricLoader.getInstance().getConfigDir().resolve("longboatlab-hud.properties");
    private static final float[] SCALES = {0.75f, 1, 1.25f, 1.5f};
    private static BoatEntity inspected;
    private static Text notice = Text.empty();
    private static int noticeTicks, scaleIndex = 1;
    private static boolean enabled = true;
    private BoatHud() {}
    public static void toggle() { enabled = !enabled; saveSettings(); }
    public static void cycleScale() { scaleIndex = (scaleIndex+1)%SCALES.length; saveSettings(); }
    public static void notice(Text message) { notice = message; noticeTicks = 70; }
    private static void saveSettings() {
        Properties values = new Properties();
        values.setProperty("enabled", Boolean.toString(enabled));
        values.setProperty("scaleIndex", Integer.toString(scaleIndex));
        try {
            Files.createDirectories(SETTINGS.getParent());
            try (var writer = Files.newBufferedWriter(SETTINGS)) { values.store(writer, "Longboat HUD"); }
        } catch (IOException e) { org.slf4j.LoggerFactory.getLogger("LongboatLab/HUD").warn("Cannot save HUD settings", e); }
    }
    public static void register() {
        if (Files.isRegularFile(SETTINGS)) {
            try (var reader = Files.newBufferedReader(SETTINGS)) {
                Properties values = new Properties(); values.load(reader);
                enabled = Boolean.parseBoolean(values.getProperty("enabled", "true"));
                scaleIndex = MathHelper.clamp(Integer.parseInt(values.getProperty("scaleIndex", "1")),0,SCALES.length-1);
            } catch (IOException | IllegalArgumentException e) { org.slf4j.LoggerFactory.getLogger("LongboatLab/HUD").warn("Cannot load HUD settings", e); }
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) { inspected = null; noticeTicks = 0; return; }
            if (noticeTicks > 0) noticeTicks--;
            if (enabled && !(client.player.getVehicle() instanceof BoatEntity) && client.player.age%4 == 0) {
                var target = BoatGeometry.raycast(client.player,1);
                inspected = target == null ? null : target.boat();
            }
        });
        HudRenderCallback.EVENT.register((context,counter) -> render(context));
    }
    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!enabled || client.player == null || client.world == null || client.options.hudHidden) return;
        BoatEntity boat = client.player.getVehicle() instanceof BoatEntity riding ? riding : inspected;
        if (boat != null && (boat.isRemoved() || boat.getWorld() != client.world)) boat = null;
        if (boat == null && noticeTicks == 0) return;
        boolean fish = boat != null && ((BoatAccess)boat).longboat$puffers() > 0;
        int bottom = boat == null ? 24 : fish ? 97 : 72;
        int height = bottom+7+(noticeTicks > 0 ? 17 : 0);
        float fit = Math.max(0.1f,Math.min((context.getScaledWindowWidth()-16)/208f,(context.getScaledWindowHeight()-16)/(float)height));
        float scale = Math.min(SCALES[scaleIndex],fit);
        context.getMatrices().push();
        context.getMatrices().translate(8,8,0);
        context.getMatrices().scale(scale,scale,1);
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        panel(context,height);
        if (boat == null) {
            icon(context,0,8,6);
            text(context,Text.translatable("category.longboatlab").getString(),29,10,170,0xFFE1A0);
        } else {
            BoatAccess access = (BoatAccess)boat;
            var state = access.longboat$visualState();
            icon(context,access.longboat$compressed() ? 1 : 0,8,6);
            text(context,Text.translatable(access.longboat$compressed() ? "hud.longboatlab.compressed" : "hud.longboatlab.extended").getString(),29,10,69,0xFFE1A0);
            text(context,Text.translatable("hud.longboatlab.dimensions",number(access.longboat$segments()),number(access.longboat$width())).getString(),108,10,91,0xC7DDD7);
            for (int i=0;i<3;i++) {
                int x=8+i*66;
                icon(context,2+i,x,27);
                long count=i==2 ? access.longboat$puffers() : access.longboat$oars(i==0);
                text(context,number(count),x+20,31,42,i==2 ? 0xFFD273 : 0x89D6EE);
            }
            context.fill(8,48,200,49,0xFF40514A);
            int hook=MathHelper.clamp(state.getInt("HookState"),0,3);
            String[] names={"idle","flying","locked","returning"};
            String hookLabel=hook==BoatAbilities.LOCKED ? number(Math.round(state.getDouble("CableLength")))+"m" : Text.translatable("hud.longboatlab."+names[hook]).getString();
            icon(context,5,8,53);
            text(context,hookLabel,28,55,70,hook==BoatAbilities.LOCKED ? 0x89D6EE : 0xC7DDD7);
            icon(context,7,108,53);
            int cooldown=state.getInt("JumpCooldown");
            String jump=!access.longboat$compressed() ? "-" : cooldown>0 ? String.format(Locale.ROOT,"%.1fs",cooldown/20.0) : Text.translatable("hud.longboatlab.ready").getString();
            text(context,jump,129,55,68,0x94CF87);
            bar(context,29,67,67,state.getDouble("CableLength")/Math.max(1,state.getDouble("CableMax")),0xFF74C8CD);
            bar(context,129,67,67,access.longboat$compressed() ? 1-cooldown/35.0 : 0,0xFF94CF87);
            if (fish) {
                int[] counts=com.xc.longboatlab.PufferGrid.counts(boat);
                String[] faces={"bottom","stern","bow","left","right"};
                for (int f=0;f<5;f++) {
                    int x=8+f*39;
                    boolean firing=(state.getInt("JetFaces")&(1<<f))!=0;
                    String label=Text.translatable("hud.longboatlab.face_"+faces[f]).getString()+" "+number(counts[f]);
                    text(context,label,x,79,35,counts[f]==0 ? 0x61756C : firing ? 0xFFE99A : 0xC7DDD7);
                    bar(context,x,91,35,counts[f]==0 ? 0 : state.getDouble("JetFill"+f),firing ? 0xFFFFE99A : 0xFF55B8BE);
                }
            }
        }
        if (noticeTicks>0) {
            context.fill(8,bottom+2,200,bottom+3,0xFF40514A);
            String value=notice.getString();
            if (client.textRenderer.getWidth(value)>190) value=client.textRenderer.trimToWidth(value,178)+"...";
            text(context,value,8,bottom+7,192,0xFFE4AD);
        }
        RenderSystem.disableBlend(); context.getMatrices().pop();
    }
    private static void panel(DrawContext context,int height) {
        context.drawTexture(ATLAS,0,0,0f,0f,208,26,256,256);
        for (int y=26;y<height-8;y+=8) context.drawTexture(ATLAS,0,y,0f,28f,208,Math.min(8,height-8-y),256,256);
        context.drawTexture(ATLAS,0,height-8,0f,110f,208,8,256,256);
    }
    private static void icon(DrawContext context,int icon,int x,int y) {
        context.drawTexture(ATLAS,x,y,icon*16f,128f,16,16,256,256);
    }
    private static void bar(DrawContext context,int x,int y,int width,double amount,int color) {
        context.fill(x,y,x+width,y+3,0xFF101E27);
        context.fill(x,y+1,x+(int)Math.round(width*MathHelper.clamp(amount,0,1)),y+2,color);
    }
    private static void text(DrawContext context,String text,int x,int y,int width,int color) {
        var renderer=MinecraftClient.getInstance().textRenderer;
        float fit=Math.min(1,width/(float)Math.max(1,renderer.getWidth(text)));
        context.getMatrices().push(); context.getMatrices().translate(x,y,0); context.getMatrices().scale(fit,fit,1);
        context.drawTextWithShadow(renderer,text,0,0,color); context.getMatrices().pop();
    }
    private static String number(long value) {
        if (value<10_000) return Long.toString(value);
        if (value<1_000_000) return String.format(Locale.ROOT,"%.1fk",value/1_000.0);
        if (value<1_000_000_000) return String.format(Locale.ROOT,"%.1fM",value/1_000_000.0);
        return String.format(Locale.ROOT,"%.1fB",value/1_000_000_000.0);
    }
}
