package com.xc.longboatlab;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.*;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.entity.vehicle.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/** Detailed one-command rigs; 1-based slots run from stern to bow, independently on each side. */
public final class RigBoatCommand {
    private RigBoatCommand() {}
    public static LiteralArgumentBuilder<ServerCommandSource> branch() {
        var root = CommandManager.literal("rig");
        for (String mode : new String[] {"spawn", "give"}) root.then(CommandManager.literal(mode)
                .then(CommandManager.argument("config", NbtCompoundArgumentType.nbtCompound())
                        .suggests((context, builder) -> {
                            builder.suggest("{Length:4,Width:2,LeftCount:4,RightCount:4,LeftSize:4.0d,RightSize:4.0d,Height:2.0d,Distance:8.0d}");
                            return builder.buildFuture();
                        })
                        .executes(context -> create(context.getSource(), NbtCompoundArgumentType.getNbtCompound(context, "config"), mode))));
        return root;
    }
    private static CommandSyntaxException bad(String detail) {
        return new SimpleCommandExceptionType(Text.translatable("command.longboatlab.invalid_rig", detail)).create();
    }
    private static int integer(NbtCompound tag, String key, int fallback, int min, int max) throws CommandSyntaxException {
        if (!tag.contains(key)) return fallback;
        if (!tag.contains(key, NbtElement.NUMBER_TYPE)) throw bad(key+": integer required");
        double value = tag.getDouble(key);
        if (!Double.isFinite(value) || value != Math.rint(value) || value < min || value > max) throw bad(key+": "+min+".."+max);
        return (int) value;
    }
    private static double number(NbtCompound tag, String key, double fallback, double min, double max) throws CommandSyntaxException {
        if (!tag.contains(key)) return fallback;
        if (!tag.contains(key, NbtElement.NUMBER_TYPE)) throw bad(key+": number required");
        double value = tag.getDouble(key);
        if (!Double.isFinite(value) || value < min || value > max) throw bad(key+": "+min+".."+max);
        return value;
    }
    private static ItemStack oar(int slot, double size) {
        ItemStack stack = new ItemStack(Items.WOODEN_SHOVEL);
        GiantOars.setData(stack, (int) Math.min(Integer.MAX_VALUE, Math.max(2, Math.round(size*size*size))), slot);
        GiantOars.setScale(stack, size);
        stack.set(net.minecraft.component.DataComponentTypes.ITEM_NAME, Text.translatable("item.longboatlab.giant_oar"));
        return stack;
    }
    private static List<ItemStack> side(NbtCompound config, String side, int length) throws CommandSyntaxException {
        if(config.getBoolean("NormalOars")) {
            if(config.contains(side)||config.contains(side+"Size"))throw bad("NormalOars cannot combine with explicit giant oars or sizes");
            return OarRack.plain(integer(config,side+"Count",1,0,Integer.MAX_VALUE));
        }
        List<ItemStack> oars = new ArrayList<>();
        if (config.contains(side)) {
            if (!(config.get(side) instanceof NbtList rows) || (!rows.isEmpty() && rows.getHeldType() != NbtElement.COMPOUND_TYPE)) throw bad(side+": compound list required");
            if (rows.size() > 512) throw bad(side+": at most 512 explicit giant oars");
            Set<Integer> used = new HashSet<>();
            for (int i = 0; i < rows.size(); i++) {
                var entry = rows.getCompound(i);
                for (String key : entry.getKeys()) if (!Set.of("Slot", "Size").contains(key)) throw bad(side+": unknown field "+key);
                int slot = integer(entry, "Slot", 1, 1, length)-1;
                if (!used.add(slot)) throw bad(side+": duplicate Slot "+(slot+1));
                oars.add(oar(slot, number(entry, "Size", 4, 1.001, Math.cbrt(Integer.MAX_VALUE))));
            }
        } else {
            int count = integer(config, side+"Count", 1, 0, Math.min(512, length));
            double size = number(config, side+"Size", 4, 1.001, Math.cbrt(Integer.MAX_VALUE));
            for (int i = 0; i < count; i++) {
                int slot = count <= 1 ? (length-1)/2 : (int) ((long) i*(length-1)/(count-1));
                oars.add(oar(slot, size));
            }
        }
        return oars;
    }
    private static int create(ServerCommandSource source, NbtCompound config, String mode) throws CommandSyntaxException {
        Set<String> allowed = Set.of("Length","Width","NormalOars","Compressed","Wood","Chest","Left","Right","LeftCount","RightCount","LeftSize","RightSize",
                "SternPuffers","Bottom","Grid","Yaw","Pitch","Roll","Distance","Height");
        for (String key : config.getKeys()) if (!allowed.contains(key)) throw bad("Unknown field: "+key);
        if (config.contains("Wood") && !config.contains("Wood", NbtElement.STRING_TYPE)) throw bad("Wood: string required");
        if (config.contains("Chest")) integer(config, "Chest", 0, 0, 1);
        integer(config,"NormalOars",0,0,1); integer(config,"Compressed",0,0,1);
        int length = integer(config, "Length", 1, 1, Integer.MAX_VALUE);
        int width = integer(config, "Width", 1, 1, Integer.MAX_VALUE);
        String wood = config.contains("Wood") ? config.getString("Wood") : "oak";
        BoatEntity.Type type = null;
        for (var candidate : BoatEntity.Type.values()) if (candidate.asString().equals(wood)) type = candidate;
        if (type == null) throw bad("Wood: "+wood);
        var left = side(config, "Left", length);
        var right = side(config, "Right", length);
        long total = integer(config, "SternPuffers", 0, 0, Integer.MAX_VALUE);
        Map<Integer, Integer> bottom = new TreeMap<>();
        if (config.contains("Bottom")) {
            if (!(config.get("Bottom") instanceof NbtList rows) || (!rows.isEmpty() && rows.getHeldType() != NbtElement.COMPOUND_TYPE)) throw bad("Bottom: compound list required");
            for (int i = 0; i < rows.size(); i++) {
                var row = rows.getCompound(i);
                for (String key : row.getKeys()) if (!Set.of("Slot", "Count").contains(key)) throw bad("Bottom: unknown field "+key);
                int slot = integer(row, "Slot", 1, 1, length)-1;
                int count = integer(row, "Count", 1, 0, Integer.MAX_VALUE);
                if (bottom.containsKey(slot)) throw bad("Bottom: duplicate Slot "+(slot+1));
                bottom.put(slot, count); total += count;
                if (total > Integer.MAX_VALUE) throw bad("Puffer total exceeds integer storage");
            }
        }
        Map<PufferGrid.Cell,Integer> grid=new LinkedHashMap<>();
        if(config.contains("Grid")) {
            if(!(config.get("Grid") instanceof NbtList rows) || (!rows.isEmpty()&&rows.getHeldType()!=NbtElement.COMPOUND_TYPE))throw bad("Grid: compound list required");
            for(int i=0;i<rows.size();i++) {
                var row=rows.getCompound(i);
                for(String key:row.getKeys())if(!Set.of("Face","Slot","U","V","Count","Lane","Depth","Outward").contains(key))throw bad("Grid: unknown field "+key);
                String faceName=row.getString("Face");
                int face=switch(faceName){case "bottom"->0;case "stern"->1;case "bow"->2;case "left"->3;case "right"->4;default->-1;};
                if(face<0)throw bad("Grid Face: bottom/stern/bow/left/right");
                int slot=integer(row,"Slot",1,1,length)-1;
                if((face==1||face==2)&&slot!=0)throw bad("End-face grid uses Slot:1");
                var cell=new PufferGrid.Cell(face,slot,integer(row,"U",0,face>=3?-3:-2,face>=3?3:2),integer(row,"V",0,face==0?-3:0,face==0?3:Integer.MAX_VALUE),
                        integer(row,"Lane",1,1,width)-1,integer(row,"Depth",0,0,Integer.MAX_VALUE),
                        integer(row,"Outward",1,0,1)!=0);
                if(face>=3 && cell.lane()!=0)throw bad("Side-face grid uses Lane:1");
                int count=integer(row,"Count",1,1,Integer.MAX_VALUE);
                if(grid.putIfAbsent(cell,count)!=null)throw bad("Grid: duplicate cell");
                total+=count;if(total>Integer.MAX_VALUE)throw bad("Puffer total exceeds integer storage");
            }
        }
        BoatRig rig = new BoatRig(length, left, right, config.getBoolean("Compressed"), (int) total, bottom, grid, width);
        float yaw = (float) number(config, "Yaw", source.getRotation().y, -360, 360);
        double pitch = number(config, "Pitch", 0, -360, 360), roll = number(config, "Roll", 0, -360, 360);
        double distance = number(config, "Distance", 4, 0, 256), height = number(config, "Height", 0, -64, 256);
        Vec3d pos = source.getPosition().add(BoatGeometry.rotateY(new Vec3d(0, height, distance), Math.toRadians(source.getRotation().y)));
        BoatEntity boat = config.getBoolean("Chest") ? new ChestBoatEntity(source.getWorld(),pos.x,pos.y,pos.z)
                : new BoatEntity(source.getWorld(),pos.x,pos.y,pos.z);
        boat.setVariant(type); boat.setYaw(yaw);
        BoatAccess access = (BoatAccess) boat;
        access.longboat$setRig(rig);
        if (mode.equals("give")) {
            if (pitch != 0 || roll != 0) throw bad("Pitch/Roll are only supported by rig spawn");
            ItemStack stack = new ItemStack(boat.asItem()); rig.applyToStack(stack, source.getRegistryManager());
            var player = source.getPlayerOrThrow();
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
        } else {
            access.longboat$abilities().body().setAngles(pitch, roll); access.longboat$syncAbilities();
            BoatCollisionScene.invalidate(boat);
            if (!BoatGeometry.spaceEmpty(boat, yaw)) {
                if (BoatCollisionScene.of(boat).unknown()) throw bad("Collision region is unloaded or exceeds the per-tick budget; reduce rig dimensions or move to loaded terrain");
                throw new SimpleCommandExceptionType(Text.translatable("message.longboatlab.no_space")).create();
            }
            if (!source.getWorld().spawnEntity(boat)) return 0;
        }
        source.sendFeedback(() -> Text.translatable("command.longboatlab.created", mode, length, left.size(), right.size(), rig.puffers()), false);
        return 1;
    }
}
