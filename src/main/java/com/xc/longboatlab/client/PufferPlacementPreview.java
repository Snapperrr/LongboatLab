package com.xc.longboatlab.client;

import com.xc.longboatlab.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;

/** The exact same grid quantizer runs on the server; no client-selected coordinates are trusted. */
public final class PufferPlacementPreview {
    private PufferPlacementPreview() {}
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context->{
            var client=MinecraftClient.getInstance();var player=client.player;
            if(player==null || client.currentScreen!=null || client.options.hudHidden
                    || (!player.getMainHandStack().isOf(Items.PUFFERFISH_BUCKET) && !player.getOffHandStack().isOf(Items.PUFFERFISH_BUCKET)))return;
            float delta=context.tickCounter().getTickDelta(false);
            var hit=BoatGeometry.raycast(player,delta,false);if(hit==null || hit.oar()>=0)return;
            var boat=hit.boat();if(((BoatAccess)boat).longboat$visualState().getInt("MorphTicks")>0)return;
            var mount=PufferGrid.preview(hit);if(mount==null)return;
            var matrices=context.matrixStack();var consumers=context.consumers();if(matrices==null||consumers==null)return;
            Vec3d camera=context.camera().getPos();
            matrices.push();matrices.translate(MathHelper.lerp(delta,boat.lastRenderX,boat.getX())-camera.x,
                    MathHelper.lerp(delta,boat.lastRenderY,boat.getY())-camera.y,MathHelper.lerp(delta,boat.lastRenderZ,boat.getZ())-camera.z);
            float yaw=BoatBody.visualYaw(boat,delta);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));double centerY=BoatBody.visualCenterY(boat,delta);
            matrices.translate(0,centerY,0);matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float)BoatBodyView.roll(boat,delta)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation((float)-BoatBodyView.pitch(boat,delta)));matrices.translate(0,-centerY,0);
            Vec3d center=PufferAttachments.position(boat,mount,BoatMorph.extension(boat,delta));
            LongboatRenderer.drawPuffer(boat,mount,center,delta,matrices,consumers,0xF000F0,true);
            WorldRenderer.drawBox(matrices,consumers.getBuffer(RenderLayer.getLines()),new Box(center.add(-0.17,-0.17,-0.17),center.add(0.17,0.17,0.17)),0.25f,1f,0.85f,0.85f);
            // Show adjacent cells on the selected hull face without enumerating the entire ship grid.
            var cell=mount.grid();
            for(int du=-1;du<=1;du++)for(int dv=-1;dv<=1;dv++) {
                var neighbor=PufferGrid.canonical(boat,new PufferGrid.Cell(cell.face(),cell.slot(),cell.u()+du,cell.v()+dv,cell.lane(),cell.depth(),cell.outward()));
                if(PufferGrid.sanitize(java.util.Map.of(neighbor,1),((BoatAccess)boat).longboat$segments(),((BoatAccess)boat).longboat$width(),1).isEmpty())continue;
                Vec3d at=PufferGrid.position(boat,neighbor,0,BoatMorph.extension(boat,delta));
                Vec3d n=PufferGrid.normal(cell.face());
                Vec3d extent=new Vec3d(Math.abs(n.x)>0?0.005:0.155,Math.abs(n.y)>0?0.005:0.10,Math.abs(n.z)>0?0.005:0.155);
                at=at.subtract(n.multiply(0.13));
                WorldRenderer.drawBox(matrices,consumers.getBuffer(RenderLayer.getLines()),new Box(at.subtract(extent),at.add(extent)),0.25f,0.75f,0.85f,0.5f);
            }
            matrices.pop();
        });
    }
}
