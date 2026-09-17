package com.xc.longboatlab;

import java.util.Set;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/** Map-issued tools request a fixed datapack action; item NBT never executes arbitrary commands. */
public final class RaceTools {
    private static final Set<String> ACTIONS = Set.of("rescue", "boat", "practice", "leave", "reset", "help");
    private RaceTools() {}

    private static String action(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getString("LongboatRaceTool");
    }
    public static boolean isTool(ItemStack stack) { return ACTIONS.contains(action(stack)); }

    public static void register() {
        RaceRescue.register();
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher,registries,environment)->
                dispatcher.register(net.minecraft.server.command.CommandManager.literal("longboatlab")
                    .requires(source->source.hasPermissionLevel(2))
                    .then(net.minecraft.server.command.CommandManager.literal("race_supply")
                        .then(net.minecraft.server.command.CommandManager.argument("kind",com.mojang.brigadier.arguments.StringArgumentType.word())
                            .executes(context->giveSupply(context.getSource().getPlayerOrThrow(),
                                    com.mojang.brigadier.arguments.StringArgumentType.getString(context,"kind")))))));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            ActionResult result = use(player, hand);
            if (result == ActionResult.FAIL) return TypedActionResult.fail(stack);
            return result == ActionResult.PASS ? TypedActionResult.pass(stack) : TypedActionResult.success(stack);
        });
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            ActionResult tool = use(player, hand);
            if (tool != ActionResult.PASS) return tool;
            if (player.isSpectator() || !player.getCommandTags().contains("lr_tools")) return ActionResult.PASS;
            var block = world.getBlockEntity(hit.getBlockPos());
            if (!(block instanceof net.minecraft.block.entity.ChestBlockEntity)) return ActionResult.PASS;
            var data = block.getComponents().getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            if (!data.contains("LongboatRaceStation", net.minecraft.nbt.NbtElement.INT_TYPE)) return ActionResult.PASS;
            int station = data.getInt("LongboatRaceStation");
            if (station < 0 || station >= 4) return ActionResult.PASS;
            String kind=data.getString("LongboatRaceSupply");
            if(!kind.isEmpty() && !Set.of("fish","oars","boats").contains(kind))return ActionResult.PASS;
            if (!world.isClient) player.addCommandTag("lr_supply_" + station+(kind.isEmpty()?"":"_"+kind));
            return ActionResult.SUCCESS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> use(player, hand));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            RaceRescue.tick(server);
            for (var player : server.getPlayerManager().getPlayerList()) {
                if (player.removeCommandTag("lr_select_empty")) {
                    player.getInventory().selectedSlot = 0;
                    player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket(0));
                }
            }
        });
    }

    /** Datapack owns per-player/per-round eligibility; capacity is checked atomically against real stacks. */
    private static int giveSupply(net.minecraft.server.network.ServerPlayerEntity player,String kind) {
        ItemStack reward=switch(kind) {
            case "fish"->new ItemStack(net.minecraft.item.Items.PUFFERFISH_BUCKET,4);
            case "oars"->new ItemStack(net.minecraft.item.Items.WOODEN_SHOVEL,6);
            case "boats"->new ItemStack(net.minecraft.item.Items.OAK_BOAT,2);
            default->ItemStack.EMPTY;
        };
        if(reward.isEmpty())return 0;
        int remaining=reward.getCount();
        ItemStack offHand=player.getOffHandStack();
        if(!offHand.isEmpty() && ItemStack.areItemsAndComponentsEqual(offHand,reward))
            remaining-=Math.max(0,offHand.getMaxCount()-offHand.getCount());
        for(ItemStack slot:player.getInventory().main) {
            if(slot.isEmpty())remaining-=reward.getMaxCount();
            else if(ItemStack.areItemsAndComponentsEqual(slot,reward))remaining-=Math.max(0,slot.getMaxCount()-slot.getCount());
            if(remaining<=0)break;
        }
        if(remaining>0) {
            player.sendMessage(net.minecraft.text.Text.translatable("message.longboatlab.supply_room"),true);
            return 0;
        }
        player.getInventory().insertStack(reward);
        return 1;
    }

    private static ActionResult use(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        String action = action(stack);
        if (!ACTIONS.contains(action)) return ActionResult.PASS;
        if (player.isSpectator()) return ActionResult.FAIL;
        if (!player.getWorld().isClient && player.getCommandTags().contains("lr_tools")
                && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            // The race datapack checks phase, player membership and penalties before fulfilling a request.
            player.addCommandTag("lr_use_" + action);
            player.getItemCooldownManager().set(stack.getItem(), 20);
        }
        return ActionResult.SUCCESS;
    }
}
