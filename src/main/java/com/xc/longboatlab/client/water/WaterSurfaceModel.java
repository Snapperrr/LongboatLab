package com.xc.longboatlab.client.water;

import net.minecraft.client.world.ClientWorld;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

/** Extension point for a future local height field. All calls run on the client thread.
 * Implementations must not load chunks. Return null for dry, occluded or unknown water.
 * The normal must be unit length and point out of the water; flow uses blocks/tick.
 */
public interface WaterSurfaceModel {
    record Surface(Vec3d position, Vec3d normal, Vec3d flow) {}
    /** Find an exposed surface near the probe (at most about two blocks vertically). */
    Surface sample(ClientWorld world, Vec3d probe);
    /** Deposit a localized disturbance into a future surface simulation. */
    default void acceptImpact(ClientWorld world, WaterImpact impact) {}
    /** Visual-only moving hull pressure. Endpoints are world-space keel positions;
     * travel is measured client displacement per tick, never a force sent to the server.
     */
    default void acceptHull(ClientWorld world, Vec3d stern, Vec3d bow, Vec3d travel) {}
    default void acceptHull(ClientWorld world, Vec3d stern, Vec3d bow, Vec3d travel, double halfWidth) {
        acceptHull(world,stern,bow,travel);
    }
    default void tick(ClientWorld world) {}
    /** Optional replacement-surface mesh, at AFTER_ENTITIES. Submit camera-relative vertices;
     * do not retain a VertexConsumer across callbacks. This does not automatically hide vanilla water.
     */
    default void render(WorldRenderContext context) {}
    /** Called on world change or model replacement; release world-specific state here. */
    default void clear() {}
}
