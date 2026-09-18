package com.xc.longboatlab;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;

/** One persistent setting per save, shared by all dimensions. No global cross-world cache. */
public final class JetSettings extends PersistentState {
    public static final float MAX_DENSITY = 8, MAX_FORCE = 16;
    private static final Type<JetSettings> TYPE = new Type<>(JetSettings::new, JetSettings::read, null);
    private float density = 1, force = 1;

    public static float bounded(float value, float maximum) {
        return Float.isFinite(value) ? Math.max(0, Math.min(maximum, value)) : 1;
    }
    public static JetSettings get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "longboatlab_jets");
    }
    private static JetSettings read(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        JetSettings settings = new JetSettings();
        if (nbt.contains("Density", NbtElement.NUMBER_TYPE)) settings.density = bounded(nbt.getFloat("Density"), MAX_DENSITY);
        if (nbt.contains("Force", NbtElement.NUMBER_TYPE)) settings.force = bounded(nbt.getFloat("Force"), MAX_FORCE);
        return settings;
    }
    @Override public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putFloat("Density", density); nbt.putFloat("Force", force); return nbt;
    }
    public float force() { return force; }
    private void send(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, JetSettingsPayload.ID))
            ServerPlayNetworking.send(player, new JetSettingsPayload(density, force));
    }
    public static void register() {
        PayloadTypeRegistry.playS2C().register(JetSettingsPayload.ID, JetSettingsPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> get(server).send(handler.player));
    }
    public static LiteralArgumentBuilder<ServerCommandSource> branch() {
        return CommandManager.literal("jets").requires(source -> source.hasPermissionLevel(2))
                .executes(context -> show(context.getSource()))
                .then(CommandManager.literal("density").then(CommandManager.argument("multiplier", FloatArgumentType.floatArg(0, MAX_DENSITY))
                        .executes(context -> change(context.getSource(), true, FloatArgumentType.getFloat(context, "multiplier")))))
                .then(CommandManager.literal("force").then(CommandManager.argument("multiplier", FloatArgumentType.floatArg(0, MAX_FORCE))
                        .executes(context -> change(context.getSource(), false, FloatArgumentType.getFloat(context, "multiplier")))))
                .then(CommandManager.literal("reset").executes(context -> {
                    JetSettings settings = get(context.getSource().getServer());
                    settings.density = settings.force = 1;
                    return settings.changed(context.getSource());
                }));
    }
    private static int change(ServerCommandSource source, boolean particleDensity, float value) {
        JetSettings settings = get(source.getServer());
        if (!Float.isFinite(value)) return 0;
        if (particleDensity) settings.density = bounded(value, MAX_DENSITY);
        else settings.force = bounded(value, MAX_FORCE);
        return settings.changed(source);
    }
    private int changed(ServerCommandSource source) {
        markDirty();
        source.getServer().getPlayerManager().getPlayerList().forEach(this::send);
        return show(source);
    }
    private static int show(ServerCommandSource source) {
        JetSettings settings = get(source.getServer());
        source.sendFeedback(() -> Text.translatable("command.longboatlab.jets", settings.density, settings.force), false);
        return 1;
    }
}
