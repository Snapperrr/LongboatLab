package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.GiantOars;
import com.xc.longboatlab.PufferAttachments;
import com.xc.longboatlab.LongboatLab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public final class LongboatRenderer {
    private static final ItemStack CHEST = new ItemStack(Items.CHEST);
    private static PufferfishEntity puffer;
    private LongboatRenderer() {}

    public static void render(BoatEntity boat, float yaw, float tickDelta, MatrixStack matrices,
                              VertexConsumerProvider consumers, int light) {
        render(boat, yaw, tickDelta, matrices, consumers, light, false);
    }

    public static void render(BoatEntity boat, float yaw, float tickDelta, MatrixStack matrices,
                              VertexConsumerProvider consumers, int light, boolean inventory) {
        BoatAccess access = (BoatAccess) boat;
        // Use the same wrapped heading as the seated camera, attachments and picking.
        // Inventory previews supply their own fixed display angle.
        if (!inventory) yaw = com.xc.longboatlab.BoatBody.visualYaw(boat, tickDelta);
        if (!inventory) CableRenderer.render(boat, tickDelta, matrices, consumers, light);
        float extension = inventory ? 1 : BoatMorph.extension(boat, tickDelta);
        double visibleHalf = 1 + (access.longboat$segments() - 1) * (double) extension;
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        if (!inventory) {
            double center = com.xc.longboatlab.BoatBody.visualCenterY(boat,tickDelta);
            matrices.translate(0, center, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) BoatBodyView.roll(boat, tickDelta)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) -BoatBodyView.pitch(boat, tickDelta)));
            matrices.translate(0, -center, 0);
        }
        Identifier texture = Identifier.of(LongboatLab.ID, "textures/entity/" + boat.getVariant().asString() + ".png");
        var vertices = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        int segments = extension == 0 ? 1 : access.longboat$segments();
        double zScale = (visibleHalf - 0.125) / (segments - 0.125);
        double stackHeight = inventory?0:com.xc.longboatlab.BoatBody.visualStackHeight(boat,tickDelta);
        // Hidden coincident layers add draw calls without adding pixels. Sample by visible stack height.
        int layers = extension < 1 ? Math.min(access.longboat$segments(),
                Math.max(2, Math.min(128, (int) Math.ceil(stackHeight / 0.035) + 1))) : 1;
        var hull = LongboatModels.INSTANCE.prepareHull(segments, visibleHalf, extension, access.longboat$width());
        for (int i = 0; i < layers; i++) {
            matrices.push();
            matrices.translate(0, layers == 1 ? 0 : stackHeight * i / (layers - 1), 0);
            matrices.scale(1, i == 0 ? 1 : 1 - extension, 1);
            hull.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
        if (!GiantOars.mounted(boat).isEmpty()) {
            for (var mount : GiantOars.mounted(boat)) {
                var pose = GiantOars.pose(boat, mount, GiantOarAnimation.phase(boat, mount.left(), tickDelta));
                matrices.push();
                matrices.translate(pose.pivot().x, pose.pivot().y + stackHeight, pose.pivot().z * extension);
                matrices.scale(pose.sign(), 1, 1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-pose.yaw()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(pose.dip()));
                float size = (float) mount.scale(); matrices.scale(size, size, size);
                LongboatModels.INSTANCE.oar().render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
                matrices.pop();
            }
        } else for (boolean left : new boolean[] {true, false}) {
            int count = extension == 0 ? Math.min(1, access.longboat$oars(left)) : Math.min(256, access.longboat$oars(left));
            for (int i = 0; i < count; i++) {
                BoatGeometry.OarPose pose = BoatGeometry.oarPose(boat, left, i, tickDelta);
                matrices.push();
                double z = count <= 1 ? 0 : (-1 + 2.0 * i / (count - 1)) * (access.longboat$segments() - 0.35) * extension;
                matrices.translate(pose.sign()*(BoatGeometry.halfWidth(boat)-0.06), BoatGeometry.HEIGHT + 0.06 + stackHeight,
                        z);
                matrices.scale(pose.sign(), 1, 1);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-pose.yaw()));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(pose.dip()));
                LongboatModels.INSTANCE.oar().render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);
                matrices.pop();
            }
        }
        if (!inventory && dryHull(boat, tickDelta)) {
            matrices.push(); matrices.scale((float)((BoatGeometry.halfWidth(boat)-0.125)/(9.0/16)), 1, (float) zScale);
            LongboatModels.INSTANCE.waterMask(segments).render(matrices,
                    consumers.getBuffer(RenderLayer.getWaterMask()), light, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }
        if (boat instanceof ChestBoatEntity) {
            matrices.push();
            matrices.translate(0, 0.45 + stackHeight, -0.35);
            matrices.scale(0.85f, 0.85f, 0.85f);
            MinecraftClient.getInstance().getItemRenderer().renderItem(CHEST, ModelTransformationMode.FIXED,
                    light, OverlayTexture.DEFAULT_UV, matrices, consumers, boat.getWorld(), boat.getId());
            matrices.pop();
        }
        renderPuffers(boat, tickDelta, matrices, consumers, light, inventory, extension);
        matrices.pop();
    }

    public static boolean dryHull(BoatEntity boat, float delta) {
        // A slight pitch/roll is normal while floating. Only a submerged rim should admit water.
        if (boat.isSubmergedInWater()) return false;
        double pitch = BoatBodyView.pitch(boat, delta), roll = BoatBodyView.roll(boat, delta);
        if (Math.cos(pitch) * Math.cos(roll) <= 0) return false;
        double half = 1 + (((BoatAccess) boat).longboat$segments()-1) * BoatMorph.extension(boat, delta);
        double center = com.xc.longboatlab.BoatBody.visualCenterY(boat,delta);
        double lowestRim = center + (BoatGeometry.HEIGHT-center) * Math.cos(pitch) * Math.cos(roll)
                - half * Math.abs(Math.sin(pitch)) - BoatGeometry.halfWidth(boat) * Math.abs(Math.sin(roll));
        var pos = boat.getBlockPos();
        var fluid = boat.getWorld().getFluidState(pos);
        double water = fluid.isIn(net.minecraft.registry.tag.FluidTags.WATER)
                ? pos.getY()+fluid.getHeight(boat.getWorld(),pos) : boat.getY();
        return water < boat.getY()+lowestRim-0.025;
    }

    private static void renderPuffers(BoatEntity boat, float delta, MatrixStack matrices, VertexConsumerProvider consumers, int light, boolean inventory, float extension) {
        BoatAccess access = (BoatAccess) boat;
        if (access.longboat$puffers() == 0) return;
        if (puffer == null || puffer.getWorld() != boat.getWorld()) puffer = new PufferfishEntity(EntityType.PUFFERFISH, boat.getWorld());

        puffer.age = boat.age;
        var camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        var local = com.xc.longboatlab.BoatBody.local(boat, camera);
        var mounts = inventory ? PufferAttachments.preview(boat) : PufferAttachments.near(boat, local, 64, extension);
        for (var mount : mounts) {
            var center = PufferAttachments.position(boat, mount, extension);
            if (!inventory && center.squaredDistanceTo(local) > 66 * 66) continue;
            drawPuffer(boat,mount,center,delta,matrices,consumers,light,false);
        }
    }
    public static void drawPuffer(BoatEntity boat, com.xc.longboatlab.PufferAttachments.Mount mount,
                                  net.minecraft.util.math.Vec3d center,float delta,MatrixStack matrices,
                                  VertexConsumerProvider consumers,int light,boolean preview) {
        if(puffer==null||puffer.getWorld()!=boat.getWorld())puffer=new PufferfishEntity(EntityType.PUFFERFISH,boat.getWorld());
        puffer.setPuffState(!preview && com.xc.longboatlab.PufferGrid.firing(boat,mount.face())?2:1);puffer.age=boat.age;
        matrices.push();matrices.translate(center.x,center.y,center.z);
        // The vanilla fish model at zero body yaw faces +Z after the living renderer's 180-degree turn.
        // Use the very same outward normal as the nozzle and physical thrust, including when the hull is inverted.
        var outward=PufferAttachments.direction(mount);
        matrices.multiply(new org.joml.Quaternionf().rotationYXZ((float)Math.atan2(outward.x,outward.z),(float)Math.asin(-outward.y),0));
        puffer.setYaw(0);puffer.prevYaw=0;puffer.bodyYaw=0;puffer.prevBodyYaw=0;puffer.headYaw=0;puffer.prevHeadYaw=0;
        matrices.translate(0,-0.13,0); matrices.scale(0.48f,0.48f,0.48f);
        matrices.translate(0,-MathHelper.cos((puffer.age+delta)*0.05f)*0.08f,0);
        MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(puffer).render(puffer,0,delta,matrices,consumers,light);
        matrices.pop();
    }

}
