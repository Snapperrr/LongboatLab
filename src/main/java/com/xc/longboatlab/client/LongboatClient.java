package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.LoadedBoats;
import com.xc.longboatlab.RaceTools;
import com.xc.longboatlab.BoatNoticePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import com.xc.longboatlab.OarInteractionPayload;
import com.xc.longboatlab.BoatBoardPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Hand;

public final class LongboatClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> LoadedBoats.add(entity));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> LoadedBoats.remove(entity));
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(LongboatModels.INSTANCE);
        BoatKeys.register();
        ClientJetSettings.register();
        BoatHud.register();
        PufferPlacementPreview.register();
        com.xc.longboatlab.client.water.WaterSurfaceShaders.register();
        com.xc.longboatlab.client.water.BoatWaterEffects.setSurfaceModel(com.xc.longboatlab.client.water.WaterSurfaceShaders.MODEL);
        com.xc.longboatlab.client.water.BoatWaterEffects.register();
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!stack.isOf(Items.WOODEN_SHOVEL) || !com.xc.longboatlab.GiantOars.giant(stack)) return;
            int units = com.xc.longboatlab.GiantOars.units(stack);
            lines.add(net.minecraft.text.Text.translatable("tooltip.longboatlab.giant_oar", units,
                    String.format(java.util.Locale.ROOT, "%.2f", com.xc.longboatlab.GiantOars.scale(stack)))
                    .formatted(net.minecraft.util.Formatting.GOLD));
            lines.add(net.minecraft.text.Text.translatable("tooltip.longboatlab.giant_rules").formatted(net.minecraft.util.Formatting.GRAY));
        });
        ClientPlayNetworking.registerGlobalReceiver(BoatNoticePayload.ID,
                (payload, context) -> BoatHud.notice(payload.message()));
    }

    public static boolean interact(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null
                || client.player.isSpectator()
                || client.player.isUsingItem() || client.interactionManager.isBreakingBlock()
                || !ClientPlayNetworking.canSend(OarInteractionPayload.ID)) return false;
        var main = client.player.getMainHandStack();
        boolean modifyingMain = client.player.isSneaking()
                && (main.isOf(Items.WOODEN_SHOVEL) || main.isOf(Items.MACE) || main.isOf(Items.PUFFERFISH_BUCKET));
        // Dock blocks have priority over the occupied hull; a hull click otherwise changes seats.
        if (!modifyingMain && !RaceTools.isTool(main) && useMountedBlock(client)) return true;
        if (RaceTools.isTool(main)) return false;
        for (Hand hand : Hand.values()) {
            var held = client.player.getStackInHand(hand);
            if (RaceTools.isTool(held)) return false;
            boolean modifying = client.player.isSneaking()
                    && (held.isOf(Items.WOODEN_SHOVEL) || held.isOf(Items.MACE) || held.isOf(Items.PUFFERFISH_BUCKET));
            if (client.player.isSneaking() && !modifying) continue;
            if (!modifying) {
                float delta=client.getRenderTickCounter().getTickDelta(false);
                BoatGeometry.Target hull=BoatGeometry.raycastHull(client.player,delta);
                if (hull==null) { clearPhantomHullTarget(client,delta);continue; }
                if (ClientPlayNetworking.canSend(BoatBoardPayload.ID)) {
                    var point=hull.localHit();
                    ClientPlayNetworking.send(new BoatBoardPayload(hull.boat().getId(),hand==Hand.OFF_HAND,point.x,point.y,point.z));
                } else ClientPlayNetworking.send(new OarInteractionPayload(hull.boat().getId(),hand==Hand.OFF_HAND));
                client.player.swingHand(hand);
                return true;
            }
            BoatGeometry.Target hit = BoatGeometry.raycast(client.player, 1, held.isOf(Items.MACE), held.isOf(Items.PUFFERFISH_BUCKET));
            if (hit == null) continue;
            if (!client.player.getItemCooldownManager().isCoolingDown(held.getItem())) {
                ClientPlayNetworking.send(new OarInteractionPayload(hit.boat().getId(), hand == Hand.OFF_HAND));
                client.player.swingHand(hand);
                client.player.getItemCooldownManager().set(held.getItem(), 4);
            }
            return true;
        }
        return false;
    }

    /** Keep vanilla damage, attack cooldowns, tool effects and the rig-aware drop path. */
    public static boolean attack(MinecraftClient client) {
        if (client.player==null || client.world==null || client.interactionManager==null
                || client.player.isSpectator() || client.player.isUsingItem() || client.attackCooldown>0
                || !client.player.getMainHandStack().isItemEnabled(client.world.getEnabledFeatures())) return false;
        float delta=client.getRenderTickCounter().getTickDelta(false);
        BoatGeometry.Target hit=BoatGeometry.raycastHull(client.player,delta);
        if (hit==null) { clearPhantomHullTarget(client,delta);return false; }
        client.interactionManager.cancelBlockBreaking();
        client.crosshairTarget=new net.minecraft.util.hit.EntityHitResult(hit.boat(),BoatGeometry.hitPosition(hit,delta));
        client.targetedEntity=hit.boat();
        client.interactionManager.attackEntity(client.player,hit.boat());
        client.player.swingHand(Hand.MAIN_HAND);
        return true;
    }

    private static void clearPhantomHullTarget(MinecraftClient client,float delta) {
        if (!(client.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult old)
                || !(old.getEntity() instanceof net.minecraft.entity.vehicle.BoatEntity)) return;
        // The enclosing AABB has empty corners when a long boat turns or rolls. A missed
        // precise hull must not fall back to attacking/boarding those corners through vanilla.
        var player=client.player;
        var block=player.raycast(player.getBlockInteractionRange(),delta,false);
        var start=player.getCameraPosVec(delta);
        var end=start.add(player.getRotationVec(delta).multiply(player.getEntityInteractionRange()));
        var entity=net.minecraft.entity.projectile.ProjectileUtil.raycast(player,start,end,
                new net.minecraft.util.math.Box(start,end).expand(1),
                candidate->!(candidate instanceof net.minecraft.entity.vehicle.BoatEntity)
                        && !candidate.isSpectator() && candidate.canHit() && candidate.getRootVehicle()!=player.getRootVehicle(),
                Math.min(start.squaredDistanceTo(end),start.squaredDistanceTo(block.getPos())));
        client.crosshairTarget=entity==null?block:entity;
        client.targetedEntity=entity==null?null:entity.getEntity();
    }

    private static boolean useMountedBlock(MinecraftClient client) {
        var player=client.player;
        if (!(player.getVehicle() instanceof net.minecraft.entity.vehicle.BoatEntity) || player.isSneaking()) return false;
        // Use vanilla block reach (not the shorter entity reach), the player's eyes in either
        // perspective, and the first real block. Walls and other entities still occlude the target.
        var hit=player.raycast(player.getBlockInteractionRange(),1,false);
        if (!(hit instanceof net.minecraft.util.hit.BlockHitResult block)
                || hit.getType()!=net.minecraft.util.hit.HitResult.Type.BLOCK) return false;
        var state=client.world.getBlockState(block.getBlockPos());
        if (!(state.getBlock() instanceof net.minecraft.block.ChestBlock)
                && !state.isOf(net.minecraft.block.Blocks.CRAFTING_TABLE)) return false;
        var start=player.getCameraPosVec(1);
        var end=block.getPos();
        var obstructing=net.minecraft.entity.projectile.ProjectileUtil.raycast(player,start,end,
                new net.minecraft.util.math.Box(start,end).expand(1),
                entity->!entity.isSpectator() && entity.canHit() && entity.getRootVehicle()!=player.getRootVehicle(),
                start.squaredDistanceTo(end));
        if(obstructing!=null)return false;
        var result=client.interactionManager.interactBlock(player,Hand.MAIN_HAND,block);
        if(result.shouldSwingHand())player.swingHand(Hand.MAIN_HAND);
        return true;
    }
}
