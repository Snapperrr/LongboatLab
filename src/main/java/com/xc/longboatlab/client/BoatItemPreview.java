package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatRig;
import com.xc.longboatlab.OarRack;
import com.xc.longboatlab.GiantOars;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

/** GUI retains exaggerated length; first-person view holds the hull outboard and away from the camera. */
public final class BoatItemPreview {
    private record Key(BoatEntity.Type wood, boolean chest, int length, int left, int right, int puffers, net.minecraft.nbt.NbtCompound rig) {}
    private static World world;
    private static final Map<Key, BoatEntity> CACHE = new LinkedHashMap<>(32, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Key, BoatEntity> entry) { return size() > 32; }
    };
    private BoatItemPreview() {}
    public static void worldChanged(World current) {
        if (world != current) { CACHE.clear(); world = current; }
    }

    public static boolean render(ItemStack stack, MatrixStack matrices, VertexConsumerProvider consumers, int light, ModelTransformationMode mode) {
        var client = MinecraftClient.getInstance();
        if (stack.isOf(net.minecraft.item.Items.WOODEN_SHOVEL) && GiantOars.giant(stack) && LongboatModels.INSTANCE.oar() != null) {
            matrices.push();
            float size = (float) GiantOars.scale(stack);
            matrices.scale(size, size, size);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-35));
            var vertices = consumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityCutoutNoCull(
                    net.minecraft.util.Identifier.of("longboatlab", "textures/entity/oak.png")));
            LongboatModels.INSTANCE.oar().render(matrices, vertices, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV);
            matrices.pop(); return true;
        }
        if (!(stack.getItem() instanceof BoatItem) || client.world == null || LongboatModels.INSTANCE.oar() == null) return false;
        var tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getCompound(BoatRig.KEY);
        int length = tag.getInt("Segments");
        length = Math.max(1, length);
        worldChanged(client.world);
        String item = Registries.ITEM.getId(stack.getItem()).getPath();
        BoatEntity.Type wood = BoatEntity.Type.OAK;
        for (BoatEntity.Type candidate : BoatEntity.Type.values()) {
            if (item.startsWith(candidate.asString() + "_")) { wood = candidate; break; }
        }
        Key key = new Key(wood, stack.isIn(ItemTags.CHEST_BOATS), length,
                OarRack.storedCount(tag, "Left"), OarRack.storedCount(tag, "Right"), Math.max(0, tag.getInt("Puffers")), tag);
        BoatEntity preview = CACHE.computeIfAbsent(key, k -> {
            BoatEntity boat = k.chest ? new ChestBoatEntity(world, 0, 0, 0) : new BoatEntity(world, 0, 0, 0);
            boat.setVariant(k.wood);
            ((BoatAccess) boat).longboat$setRig((k.rig.isEmpty() ? BoatRig.vanilla()
                    : BoatRig.fromNbt(k.rig, world.getRegistryManager())).withMode(false));
            return boat;
        });
        // Deliberately retain world-model scale: a long boat spills outside its inventory slot.
        matrices.push();
        if (mode == ModelTransformationMode.GUI) {
            matrices.scale(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(65));
        } else if (mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND
                || mode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND) {
            float hand = mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND ? -1 : 1;
            // This renderer replaces ItemRenderer at HEAD, before vanilla display transforms.
            // Keep the near end below/outside the crosshair and extend long hulls away from the eye.
            matrices.translate(hand * 0.48, -0.48, -0.65);
            matrices.scale(0.32f, 0.32f, 0.32f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(hand * -12));
            matrices.translate(hand*(com.xc.longboatlab.BoatGeometry.halfWidth(preview)-com.xc.longboatlab.BoatGeometry.HALF_WIDTH), 0, -length);
        } else {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            matrices.translate(0, 0.1, 0);
        }
        LongboatRenderer.render(preview, 0, 0, matrices, consumers, light, true);
        matrices.pop();
        return true;
    }
}
