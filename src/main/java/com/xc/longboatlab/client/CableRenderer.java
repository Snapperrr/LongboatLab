package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAbilities;
import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.LongboatLab;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** A straight, telescoping structure made entirely from joined wooden oars. */
public final class CableRenderer {
    private CableRenderer() {}
    private static final java.util.Map<BoatEntity, Motion> MOTION = new java.util.WeakHashMap<>();
    private static final class Motion {
        Vec3d previous, current;
        Motion(Vec3d point) { previous = current = point; }
    }
    public static void tick(net.minecraft.client.MinecraftClient client) {
        if (client.world == null) { MOTION.clear(); return; }
        for (BoatEntity boat : com.xc.longboatlab.LoadedBoats.in(client.world)) {
            var state = ((BoatAccess) boat).longboat$visualState();
            if (state.getInt("HookState") == BoatAbilities.IDLE) { MOTION.remove(boat); continue; }
            Motion motion = MOTION.computeIfAbsent(boat, b -> new Motion(BoatAbilities.cableOrigin(b)));
            motion.previous = motion.current;
            motion.current = hookPosition(state);
        }
    }
    private static Vec3d renderedHook(BoatEntity boat, NbtCompound state, float delta) {
        int id = state.getInt("AnchorEntity");
        if (state.getInt("HookState") == BoatAbilities.LOCKED && id >= 0) {
            var entity = boat.getWorld().getEntityById(id);
            if (entity != null) {
                Vec3d position = new Vec3d(MathHelper.lerp(delta, entity.prevX, entity.getX()),
                        MathHelper.lerp(delta, entity.prevY, entity.getY()), MathHelper.lerp(delta, entity.prevZ, entity.getZ()));
                Vec3d offset = new Vec3d(state.getDouble("AnchorOffsetX"), state.getDouble("AnchorOffsetY"), state.getDouble("AnchorOffsetZ"));
                return position.add(com.xc.longboatlab.BoatGeometry.rotateY(offset,
                        Math.toRadians(MathHelper.lerpAngleDegrees(delta, entity.prevYaw, entity.getYaw()))));
            }
        }
        Motion motion = MOTION.get(boat);
        return motion == null ? hookPosition(state) : motion.previous.lerp(motion.current, delta);
    }

    public static Vec3d hookPosition(NbtCompound state) {
        return new Vec3d(state.getDouble("HookX"), state.getDouble("HookY"), state.getDouble("HookZ"));
    }

    public static void render(BoatEntity boat, float tickDelta, MatrixStack matrices, VertexConsumerProvider consumers, int light) {
        NbtCompound state = ((BoatAccess) boat).longboat$visualState();
        if (state.getInt("HookState") == BoatAbilities.IDLE) return;
        Vec3d renderedBoat = com.xc.longboatlab.BoatBody.visualPosition(boat,tickDelta);
        Vec3d start = BoatBodyView.offset(boat, new Vec3d(0,
                com.xc.longboatlab.BoatGeometry.HEIGHT + com.xc.longboatlab.BoatBody.visualStackHeight(boat,tickDelta) + 0.08,
                1+(((BoatAccess)boat).longboat$segments()-1)*(double)BoatMorph.extension(boat,tickDelta)-0.12),
                com.xc.longboatlab.BoatBody.visualYaw(boat,tickDelta), tickDelta);
        Vec3d end = renderedHook(boat, state, tickDelta).subtract(renderedBoat);
        Vec3d span = end.subtract(start);
        double length = span.length();
        if (length < 0.001) return;
        Vec3d direction = span.multiply(1 / length);
        Quaternionf rotation = new Quaternionf().rotationTo(new Vector3f(1, 0, 0),
                new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
        var vertices = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(
                Identifier.of(LongboatLab.ID, "textures/entity/" + boat.getVariant().asString() + ".png")));
        matrices.push();
        matrices.translate(start.x, start.y, start.z);
        matrices.multiply(rotation);
        // Oar extents are -0.25..1.25 blocks along X. Segments overlap slightly at their joints.
        // Only the leading partial segment changes length; the complete oars retain their proportions.
        double spacing = 1.01;
        int pieces = (int) Math.ceil(length / spacing);
        for (int i = 0; i < pieces; i++) {
            double begin = i * spacing;
            double sectionLength = Math.min(spacing, length - begin);
            double scaleX = (sectionLength + (i + 1 < pieces ? 0.04 : 0)) / 1.5;
            matrices.push();
            matrices.translate(begin + 0.25 * scaleX, 0, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i % 2 == 0 ? 0 : 90));
            matrices.scale((float) scaleX, 0.7f, 0.7f);
            LongboatModels.INSTANCE.oar().render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
        matrices.translate(length, 0, 0);
        matrices.scale(0.45f, 0.45f, 0.45f);
        for (int i = 0; i < 3; i++) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * 120));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(135));
            LongboatModels.INSTANCE.oar().render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
        matrices.pop();
    }
}
