package com.xc.longboatlab.mixin;

import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.LoadedBoats;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.EntityView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldBoatCollisionMixin implements EntityView {
    @Inject(method = "getOtherEntities", at = @At("RETURN"), cancellable = true)
    private void longboat$wholeHull(Entity except, Box box, Predicate<? super Entity> predicate,
                                    CallbackInfoReturnable<List<Entity>> cir) {
        List<Entity> result = null;
        for (BoatEntity boat : LoadedBoats.near((World) (Object) this, box)) {
            if (boat == except || !com.xc.longboatlab.GiantOars.discovery(boat).intersects(box) || !predicate.test(boat)
                    || cir.getReturnValue().contains(boat)) continue;
            if (result == null) result = new ArrayList<>(cir.getReturnValue());
            result.add(boat);
        }
        if (result != null) cir.setReturnValue(result);
    }

    /** Override EntityView's default implementation, retaining its entity collision predicate. */
    @Override public List<VoxelShape> getEntityCollisions(Entity entity, Box box) {
        if (box.getAverageSideLength() < 1.0e-7) return List.of();
        Predicate<Entity> predicate = entity == null ? EntityPredicates.CAN_COLLIDE
                : EntityPredicates.EXCEPT_SPECTATOR.and(entity::collidesWith);
        Box search = box.expand(1.0e-7);
        List<VoxelShape> shapes = new ArrayList<>();
        for (Entity other : getOtherEntities(entity, search, predicate)) {
            if (other instanceof BoatEntity boat) {
                for (Box part : BoatGeometry.collisionParts(boat, boat.getYaw())) {
                    if (part.intersects(search)) shapes.add(VoxelShapes.cuboid(part));
                }
            } else if(other.getVehicle() instanceof BoatEntity ridden) {
                // The vehicle owns the rider's rotated collision shape; do not also collide with its upright proxy.
                for(Box part:com.xc.longboatlab.BoatSeats.collisionParts(ridden,ridden.getYaw()))
                    if(part.intersects(search))shapes.add(VoxelShapes.cuboid(part));
            } else shapes.add(VoxelShapes.cuboid(other.getBoundingBox()));
        }
        return shapes;
    }
}
