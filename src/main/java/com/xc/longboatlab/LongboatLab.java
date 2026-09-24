package com.xc.longboatlab;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;

public final class LongboatLab implements ModInitializer {
    public static final String ID = "longboatlab";
    public static final net.minecraft.sound.SoundEvent PUFFER_JET = Registry.register(Registries.SOUND_EVENT,
            Identifier.of(ID, "puffer_jet"), net.minecraft.sound.SoundEvent.of(Identifier.of(ID, "puffer_jet")));
    public static final net.minecraft.sound.SoundEvent WATER_IMPACT = Registry.register(Registries.SOUND_EVENT,
            Identifier.of(ID, "water_impact"), net.minecraft.sound.SoundEvent.of(Identifier.of(ID, "water_impact")));
    public static final net.minecraft.sound.SoundEvent WATER_WAKE = Registry.register(Registries.SOUND_EVENT,
            Identifier.of(ID, "water_wake"), net.minecraft.sound.SoundEvent.of(Identifier.of(ID, "water_wake")));
    public static final RecipeSerializer<ExtendBoatRecipe> EXTEND_BOAT = Registry.register(
            Registries.RECIPE_SERIALIZER, Identifier.of(ID, "extend_boat"), new SpecialRecipeSerializer<>(ExtendBoatRecipe::new));

    public static final RecipeSerializer<GiantOarRecipe> GIANT_OAR = Registry.register(
            Registries.RECIPE_SERIALIZER, Identifier.of(ID, "giant_oar"), new SpecialRecipeSerializer<>(GiantOarRecipe::new));

    @Override
    public void onInitialize() {
        StackableSupplies.register();
        JetSettings.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> LoadedBoats.add(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> LoadedBoats.remove(entity));
        BoatDebugCommand.register();
        RaceTools.register();
        PayloadTypeRegistry.playS2C().register(BoatNoticePayload.ID, BoatNoticePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BoatControlPayload.ID, BoatControlPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BoatControlPayload.ID, (payload, context) -> {
            var player = context.player();
            if (player.isAlive() && !player.isSpectator()
                    && player.getVehicle() instanceof net.minecraft.entity.vehicle.BoatEntity boat
                    && boat.getControllingPassenger() == player) {
                ((BoatAccess) boat).longboat$abilities().accept(boat, player, payload);
            }
        });
        PayloadTypeRegistry.playC2S().register(BoatBoardPayload.ID, BoatBoardPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BoatBoardPayload.ID, (payload, context) -> {
            var player=context.player();
            if (!player.isAlive() || player.isSpectator() || player.isSneaking()) return;
            if (!(player.getWorld().getEntityById(payload.entityId()) instanceof net.minecraft.entity.vehicle.BoatEntity boat)) return;
            Hand hand=payload.offHand()?Hand.OFF_HAND:Hand.MAIN_HAND;
            if (RaceTools.isTool(player.getStackInHand(hand)) || !BoatGeometry.canUseHull(player,boat,payload.localHit())) return;
            BoatSeats.board(boat,player,hand,payload.localHit());
        });
        PayloadTypeRegistry.playC2S().register(OarInteractionPayload.ID, OarInteractionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OarInteractionPayload.ID, (payload, context) -> {
            var player = context.player();
            if (!player.isAlive() || player.isSpectator()) return;
            Hand hand = payload.offHand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
            ItemStack held = player.getStackInHand(hand);
            BoatGeometry.Target target = player.isSneaking()
                    ? BoatGeometry.raycast(player, 1, held.isOf(Items.MACE), held.isOf(Items.PUFFERFISH_BUCKET))
                    : BoatGeometry.raycastHull(player,1);
            if (target == null || target.boat().getId() != payload.entityId()) return;
            if (!player.isSneaking()) {
                BoatSeats.board(target.boat(), player, hand, target.localHit());
                return;
            }
            if (!player.canModifyBlocks()) return;
            if ((!held.isOf(Items.WOODEN_SHOVEL) && !held.isOf(Items.MACE) && !held.isOf(Items.PUFFERFISH_BUCKET))
                    || player.getItemCooldownManager().isCoolingDown(held.getItem())) return;
            BoatAccess access = (BoatAccess) target.boat();
            BoatRig rig = access.longboat$rig();
            if (held.isOf(Items.PUFFERFISH_BUCKET)) {
                var cell = PufferGrid.placement(target);
                if (cell == null || target.oar() >= 0 || access.longboat$abilities().transforming()) {
                    BoatNoticePayload.send(player, Text.translatable("message.longboatlab.stern_only"));
                    return;
                }
                if (rig.puffers() == Integer.MAX_VALUE) {
                    BoatNoticePayload.send(player, Text.translatable("message.longboatlab.puffer_storage")); return;
                }
                access.longboat$setRig(rig.changeGrid(cell, 1));
                held.decrementUnlessCreative(1, player);
                player.getItemCooldownManager().set(Items.PUFFERFISH_BUCKET, 5);
                var local = PufferGrid.position(target.boat(), cell, rig.gridPuffers().getOrDefault(cell, 0),
                        BoatGeometry.extension(target.boat()));
                var soundPosition = BoatBody.world(target.boat(), local);
                player.getWorld().playSound(null, soundPosition.x, soundPosition.y, soundPosition.z,
                        SoundEvents.ITEM_BUCKET_EMPTY_FISH, SoundCategory.PLAYERS, 0.9f, 1.05f);
                player.getWorld().playSound(null, soundPosition.x, soundPosition.y, soundPosition.z,
                        SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 0.55f, 1.25f);
                target.boat().emitGameEvent(GameEvent.ENTITY_INTERACT, player);
                BoatNoticePayload.send(player, Text.translatable("message.longboatlab.puffers", access.longboat$puffers()));
                return;
            } else if (held.isOf(Items.WOODEN_SHOVEL)) {
                if (target.oar() < -1) return;
                if (rig.side(target.left()).size() >= rig.oarLimit()) {
                    BoatNoticePayload.send(player, Text.translatable("message.longboatlab.limit", rig.oarLimit()));
                    return;
                }
                if (GiantOars.giant(held)) {
                    if (!GiantOars.install(target.boat(), player, target.left(), target.localHit(), held)) return;
                    held.decrementUnlessCreative(1, player);
                } else {
                    if (rig.hasGiants()) {
                        BoatNoticePayload.send(player, Text.translatable("message.longboatlab.giant_mix")); return;
                    }
                    int amount=Math.min(held.getCount(),rig.oarLimit()-rig.side(target.left()).size());
                    access.longboat$setRig(rig.add(target.left(), held,amount));
                    held.decrementUnlessCreative(amount, player);
                }
                player.getItemCooldownManager().set(Items.WOODEN_SHOVEL, 4);
            } else {
                if (target.puffer() != null) {
                    var mount = target.puffer();
                    int available = mount.grid()!=null ? rig.gridPuffers().getOrDefault(mount.grid(),0) : mount.bottom() ? rig.bottomPuffers().getOrDefault(mount.segment(), 0) : rig.sternPuffers();
                    if (mount.index() >= available) return;
                    if (!player.isCreative()) {
                        ItemStack recovered = new ItemStack(Items.PUFFERFISH_BUCKET);
                        if (!player.getInventory().insertStack(recovered)) player.dropItem(recovered, false);
                    }
                    access.longboat$setRig(mount.grid()!=null ? rig.changeGrid(mount.grid(),-1) : rig.changePuffer(mount.segment(), -1));
                    held.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    player.getItemCooldownManager().set(Items.MACE, 4);
                    target.boat().emitGameEvent(GameEvent.ENTITY_INTERACT, player);
                    player.getWorld().playSound(null, target.boat().getBlockPos(), SoundEvents.ITEM_BUCKET_FILL_FISH,
                            SoundCategory.PLAYERS, 0.8f, 1);
                    BoatNoticePayload.send(player, Text.translatable("message.longboatlab.puffer_removed", access.longboat$puffers()));
                    return;
                }
                if (target.oar() < 0 || target.oar() >= rig.side(target.left()).size()) return;
                int index = access.longboat$compressed() ? rig.side(target.left()).size() - 1 : target.oar();
                ItemStack recovered = rig.side(target.left()).get(index).copy();
                access.longboat$setRig(rig.remove(target.left(), index));
                if (!player.isCreative() && !player.getInventory().insertStack(recovered)) player.dropItem(recovered, false);
                held.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                player.getItemCooldownManager().set(Items.MACE, 4);
            }
            player.getWorld().playSound(null, target.boat().getBlockPos(), SoundEvents.BLOCK_WOOD_PLACE,
                    SoundCategory.PLAYERS, 0.8f, held.isOf(Items.MACE) ? 0.7f : 1.2f);
            target.boat().emitGameEvent(GameEvent.ENTITY_INTERACT, player);
            BoatNoticePayload.send(player, Text.translatable("message.longboatlab.rig",
                    access.longboat$segments(), access.longboat$oars(true), access.longboat$oars(false)));
        });
    }
}
