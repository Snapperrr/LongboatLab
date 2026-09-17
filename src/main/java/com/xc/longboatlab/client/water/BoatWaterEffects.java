package com.xc.longboatlab.client.water;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatBody;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.LoadedBoats;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Bounded, client-only hull contact sampling. Effects outlive the boat that emitted them. */
public final class BoatWaterEffects {
    // Ordinary rowing makes a visible wake; stronger forcing stays continuous through the speed threshold.
    public static final double WAKE_START_SPEED = 0.12;
    public static final double WAKE_STOP_SPEED = 0.09;
    private static final double WAKE_BASE_STRENGTH = 0.10, WAKE_SPEED_GAIN = 1.12, MAX_WAKE_STRENGTH = 1.7;
    private static final int MAX_BOATS = 24, MAX_EVENTS = 20, SAMPLE_BUDGET = 2304;
    private static final double RANGE = 80;
    private static final Map<BoatEntity, Track> TRACKS = new WeakHashMap<>();
    private static final WaterSplashRenderer EFFECTS = new WaterSplashRenderer();
    private static WaterSurfaceModel surface = new VanillaWaterSurface();
    private static final VanillaWaterSurface CONTACT_SURFACE = new VanillaWaterSurface();
    private static ClientWorld currentWorld;
    private static int samplesLeft, clock;

    private BoatWaterEffects() {}
    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> surface.render(context));
        com.xc.longboatlab.client.BoatEffectRenderPass.register(EFFECTS::render);
    }

    /** Client-thread API: swap in a height-field solver without rewriting contact detection or spray. */
    public static void setSurfaceModel(WaterSurfaceModel model) {
        Objects.requireNonNull(model);
        surface.clear(); surface = model;
        TRACKS.clear(); EFFECTS.clear();
    }
    public static void useVanillaSurface() { setSurfaceModel(new VanillaWaterSurface()); }
    static boolean hasWaterSamples(int count) { return samplesLeft >= count; }
    static WaterSurfaceModel.Surface sample(ClientWorld world, Vec3d probe) {
        if (samplesLeft <= 0) return null;
        samplesLeft--;
        return surface.sample(world, probe);
    }

    private record Pose(Vec3d position, double yaw, double pitch, double roll, double half, double width, double stack) {
        static Pose of(BoatEntity boat) {
            double half = 1 + (((BoatAccess) boat).longboat$segments() - 1)
                    * (double) BoatBody.visualExtension(boat, 1);
            return new Pose(boat.getPos(), Math.toRadians(boat.getYaw()), BoatBody.visualPitch(boat, 1),
                    BoatBody.visualRoll(boat, 1), half, BoatGeometry.halfWidth(boat), BoatBody.visualStackHeight(boat, 1));
        }
        Vec3d direction(Vec3d v) { return BoatBody.direction(v, yaw, pitch, roll); }
        Vec3d point(Vec3d local) {
            double center = (BoatGeometry.HEIGHT + stack) / 2;
            return position.add(direction(local.add(0, -center, 0))).add(0, center, 0);
        }
    }
    private static final class Track {
        Pose pose;
        int seen;
        final int[] nextEntry = new int[8];
        int nextWake;
        boolean waking;
        int lead = 1;
        Track(Pose pose, int seen) { this.pose = pose; this.seen = seen; }
    }
    private record Contact(Vec3d point, Vec3d velocity, Vec3d normal, Vec3d hullNormal, Vec3d waterFlow,
                           double normalSpeed, double tangentSpeed, double strength, WaterImpact.Kind kind) {}

    public static void tick(MinecraftClient client) {
        if (currentWorld != client.world) {
            surface.clear(); CONTACT_SURFACE.clear(); TRACKS.clear(); EFFECTS.clear();
            currentWorld = client.world; clock = 0;
        }
        if (!client.isPaused()) BoatWaterSounds.tick(client);
        if (client.world == null || client.player == null || client.isPaused()) return;
        clock++; samplesLeft = SAMPLE_BUDGET;
        surface.tick(client.world);
        CONTACT_SURFACE.tick(client.world);
        EFFECTS.tick(client);
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        // Spatial discovery includes ends of long hulls whose entity origin lies off-screen.
        List<BoatEntity> boats = new ArrayList<>(LoadedBoats.near(client.world,
                new Box(camera.add(-RANGE, -RANGE, -RANGE), camera.add(RANGE, RANGE, RANGE))));
        boats.sort(Comparator.comparingDouble(boat -> distanceToHull(boat, camera)));
        int inspected = 0, emitted = 0;
        for (BoatEntity boat : boats) {
            if (inspected++ >= MAX_BOATS || distanceToHull(boat, camera) > RANGE * RANGE) break;
            Pose pose = Pose.of(boat);
            Track track = TRACKS.get(boat);
            if (track == null) { TRACKS.put(boat, new Track(pose, clock)); continue; }
            Pose old = track.pose;
            boolean continuous = track.seen == clock - 1;
            track.pose = pose; track.seen = clock;
            // Spawn, teleport and morph changes are not impacts; do not turn a resize into a tsunami.
            if (!continuous || pose.position.squaredDistanceTo(old.position) > 144
                    || Math.abs(pose.width-old.width)>0.02 || Math.abs(pose.half - old.half) > 0.02 || Math.abs(pose.stack - old.stack) > 0.05) {
                track.waking = false; EFFECTS.resetHullWake(boat.getUuid()); continue;
            }
            Vec3d travel = pose.position.subtract(old.position);
            // Persistent displacement is independent of the spray threshold/event budget.
            // Spawn, morph and teleport discontinuities have already been rejected above.
            surface.acceptHull(client.world, pose.point(new Vec3d(0, 0.04, -pose.half + 0.18)),
                    pose.point(new Vec3d(0, 0.04, pose.half - 0.18)), travel, pose.width);
            double horizontalSpeed = Math.sqrt(travel.x * travel.x + travel.z * travel.z);
            double turnSpeed = Math.abs(MathHelper.wrapDegrees(Math.toDegrees(pose.yaw - old.yaw)))
                    * Math.PI / 180 * Math.min(32, Math.hypot(pose.half,pose.width));
            track.waking = horizontalSpeed + turnSpeed > (track.waking ? WAKE_STOP_SPEED : WAKE_START_SPEED);
            BoatWaterSounds.wake(client, boat, horizontalSpeed + turnSpeed, boat.isTouchingWater());
            double longitudinalSpeed = travel.dotProduct(pose.direction(new Vec3d(0, 0, 1)));
            if (Math.abs(longitudinalSpeed) > 0.04) track.lead = longitudinalSpeed > 0 ? 1 : -1;
            // Stable hull-local station coordinates: looking/turning must not renumber the same nodes.
            if (inspected <= 8 && samplesLeft >= 192 && track.waking) {
                double localCameraZ = BoatBody.local(boat, camera).z;
                double hullEnd = pose.half - 0.30;
                List<Double> stations = new ArrayList<>();
                if (hullEnd <= 32) {
                    int count = MathHelper.clamp((int) Math.ceil(hullEnd * 2 / 4) + 1, 2, 17);
                    for (int i = 0; i < count; i++) stations.add(MathHelper.lerp(i / (double) (count - 1), -hullEnd, hullEnd));
                } else {
                    double first = Math.max(-hullEnd, Math.floor((localCameraZ - 28) / 4) * 4);
                    double last = Math.min(hullEnd, Math.ceil((localCameraZ + 28) / 4) * 4);
                    stations.add(first);
                    for (double z = Math.floor(first / 4) * 4 + 4; z < last && stations.size() < 16; z += 4) stations.add(z);
                    stations.add(last);
                }
                // Sample the leading transition explicitly even on a one-section hull. Without
                // these points, a four-block span cannot resolve the rise from zero at the bow.
                double leadZ = track.lead * hullEnd;
                double windowFirst = stations.getFirst(), windowLast = stations.getLast();
                if (leadZ >= stations.getFirst() && leadZ <= stations.getLast()) {
                    for (double distance : new double[]{0.30, 0.75, 1.35}) {
                        double z = leadZ - track.lead * Math.min(distance, hullEnd * 1.4);
                        if (z > windowFirst && z < windowLast) stations.add(z);
                    }
                    stations = new ArrayList<>(new TreeSet<>(stations));
                }
                for (int side : new int[]{-1, 1}) {
                    List<WaterImpact> contacts = new ArrayList<>(stations.size());
                    for (double z : stations) contacts.add(sideWake(client.world, boat, pose, old, z, side, camera, true));
                    // Project chord motion onto the midpoint heading. Using only the new
                    // heading accumulates artificial texture stretching on every full rotation.
                    Vec3d flowAxis = pose.direction(new Vec3d(0, 0, 1)).add(old.direction(new Vec3d(0, 0, 1))).normalize();
                    EFFECTS.hullWake(client, boat, side, stations, contacts, -track.lead * hullEnd, flowAxis);
                }
            }
            if (emitted >= MAX_EVENTS || samplesLeft < 96) continue;
            int rows = MathHelper.clamp((int) Math.min(8, Math.ceil(pose.half * 2)), 2, 8);
            double area = Math.min(3, 2 * pose.half / rows) * (pose.width*2-0.275);
            double cameraZ = BoatBody.local(boat, camera).z;
            double firstZ = Math.max(-pose.half + 0.18, cameraZ - RANGE);
            double lastZ = Math.min(pose.half - 0.18, cameraZ + RANGE);
            List<WaterImpact> impacts = new ArrayList<>();
            // Pure horizontal travel cannot cross a flat surface downwards at impact speed.
            // Use a conservative rotation bound so a whipping long bow still gets full swept probes.
            double verticalBound = -travel.y + (pose.half + pose.width + pose.stack + 1)
                    * (Math.abs(pose.pitch - old.pitch) + Math.abs(pose.roll - old.roll))
                    + Math.abs(pose.stack - old.stack);
            boolean possibleImpact = (!(surface instanceof VanillaWaterSurface) && !(surface instanceof HeightfieldSurface))
                    || verticalBound > 0.075;
            for (int row = 0; possibleImpact && row < rows && samplesLeft >= 56; row++) {
                double z = MathHelper.lerp(row / (double) (rows - 1), firstZ, lastZ);
                Contact strongest = null;
                List<Contact> contacts = new ArrayList<>(7);
                for (int column = 0; column < 7; column++) {
                    double x = switch (column) { case 0, 3, 5 -> -pose.width+0.0875; case 2, 4, 6 -> pose.width-0.0875; default -> 0; };
                    double y = column < 3 ? 0.04 : column < 5 ? 0.28 : BoatGeometry.HEIGHT + pose.stack;
                    Vec3d local = new Vec3d(x * (column < 3 ? 0.72 : 1), y, z);
                    Vec3d hullNormal = column < 3 ? new Vec3d(0, -1, 0) : new Vec3d(Math.signum(x), 0, 0);
                    // End sections also face the oncoming water, including backwards travel.
                    if (Math.abs(z) >= pose.half - 0.20)
                        hullNormal = hullNormal.add(0, 0, Math.signum(z) * 0.8).normalize();
                    Vec3d previous = old.point(local), now = pose.point(local);
                    if (now.squaredDistanceTo(camera) > RANGE * RANGE) continue;
                    Contact contact = contact(client.world, previous, now, pose.direction(hullNormal),
                            clock >= track.nextEntry[row]);
                    if (contact != null) {
                        contacts.add(contact);
                        if (strongest == null || contact.strength > strongest.strength) strongest = contact;
                    }
                }
                if (strongest == null) continue;
                // A flat bottom slap is centered across its wet footprint, not always at the first (left) probe.
                Contact c = merge(contacts, strongest);
                double power = MathHelper.clamp(c.strength * Math.sqrt(area) * 1.2, 0.14, 4.2);
                impacts.add(new WaterImpact(boat.getUuid(), c.point, c.velocity, c.normal, c.hullNormal,
                        area, c.normalSpeed, c.tangentSpeed, power, c.kind, 0, c.waterFlow));
                if (c.kind == WaterImpact.Kind.ENTRY) track.nextEntry[row] = clock + 7;
            }
            boolean slapped = !impacts.isEmpty();
            for (WaterImpact impact : distributed(impacts)) {
                if (emitted >= MAX_EVENTS) break;
                BoatWaterSounds.impact(client, impact);
                surface.acceptImpact(client.world, impact);
                EFFECTS.emit(client, impact);
                emitted++;
            }
            // Free history starts at the trailing end; live water alongside the hull is rendered above.
            if (!slapped && track.waking && clock >= track.nextWake && emitted <= MAX_EVENTS - 2) {
                for (int side : new int[]{-1, 1}) {
                    WaterImpact wake = sideWake(client.world, boat, pose, old, -track.lead * (pose.half - 0.30), side, camera, false);
                    if (wake == null) continue;
                    surface.acceptImpact(client.world, wake); EFFECTS.emit(client, wake); emitted++;
                }
                track.nextWake = clock + 2;
            }
        }
        // Emit after both gunwales have been updated, so history/one side cannot take all spray slots.
        EFFECTS.finishTick(client);
        TRACKS.entrySet().removeIf(e -> e.getKey().isRemoved() || e.getKey().getWorld() != currentWorld
                || clock - e.getValue().seen > 40);
    }

    private static List<WaterImpact> distributed(List<WaterImpact> impacts) {
        List<WaterImpact> selected = new ArrayList<>(3);
        // Equal-strength flat slaps should cover the hull instead of spending all three events at the stern.
        while (!impacts.isEmpty() && selected.size() < 3) {
            WaterImpact best = null;
            double bestScore = -1;
            for (WaterImpact candidate : impacts) {
                double separation = selected.isEmpty() ? 0 : Double.POSITIVE_INFINITY;
                for (WaterImpact other : selected)
                    separation = Math.min(separation, candidate.position().squaredDistanceTo(other.position()));
                double score = candidate.strength() * (1 + Math.min(4, Math.sqrt(separation)));
                if (score > bestScore) { bestScore = score; best = candidate; }
            }
            selected.add(best); impacts.remove(best);
        }
        return selected;
    }

    private static Contact merge(List<Contact> contacts, Contact strongest) {
        Vec3d point = Vec3d.ZERO, velocity = Vec3d.ZERO, normal = Vec3d.ZERO, hullNormal = Vec3d.ZERO, flow = Vec3d.ZERO;
        double weight = 0, normalSpeed = 0, tangentSpeed = 0;
        for (Contact c : contacts) {
            if (c.kind != strongest.kind) continue;
            double w = Math.max(0.01, c.strength);
            weight += w; point = point.add(c.point.multiply(w)); velocity = velocity.add(c.velocity.multiply(w));
            normal = normal.add(c.normal.multiply(w)); hullNormal = hullNormal.add(c.hullNormal.multiply(w));
            flow = flow.add(c.waterFlow.multiply(w));
            normalSpeed += c.normalSpeed * w; tangentSpeed += c.tangentSpeed * w;
        }
        return new Contact(point.multiply(1 / weight), velocity.multiply(1 / weight), normal.normalize(),
                hullNormal.lengthSquared() < 1e-6 ? strongest.hullNormal : hullNormal.normalize(),
                flow.multiply(1 / weight), normalSpeed / weight, tangentSpeed / weight, strongest.strength, strongest.kind);
    }

    private static WaterImpact sideWake(ClientWorld world, BoatEntity boat, Pose pose, Pose old, double z, int side, Vec3d camera, boolean hullContact) {
        double x = side * (pose.width + 0.06);
        Vec3d bottom = pose.point(new Vec3d(x, 0, z));
        Vec3d top = pose.point(new Vec3d(x, BoatGeometry.HEIGHT + pose.stack, z));
        Vec3d middle = bottom.lerp(top, 0.5);
        if (middle.squaredDistanceTo(camera) > RANGE * RANGE) return null;
        if (samplesLeft-- <= 0) return null;
        var water = CONTACT_SURFACE.sample(world, middle);
        if (water == null) return null;
        Vec3d normal = water.normal();
        double low = bottom.subtract(water.position()).dotProduct(normal);
        double high = top.subtract(water.position()).dotProduct(normal);
        if (Math.min(low, high) > 0.04 || Math.max(low, high) < -0.04 || Math.abs(high - low) < 0.08) return null;
        Vec3d at = bottom.lerp(top, MathHelper.clamp(-low / (high - low), 0, 1));
        at = at.subtract(normal.multiply(at.subtract(water.position()).dotProduct(normal))).add(normal.multiply(0.025));
        // A visually depressed waterline must not switch the wake on/off as it crosses the keel.
        var visibleWater = sample(world, at);
        if (visibleWater != null) at = visibleWater.position().add(0, 0.025, 0);
        Vec3d localContact = new Vec3d(x, 0.15, z);
        Vec3d relative = pose.point(localContact).subtract(old.point(localContact)).subtract(water.flow());
        Vec3d tangent = relative.subtract(normal.multiply(relative.dotProduct(normal)));
        double speed = tangent.length();
        if (!hullContact && speed < WAKE_STOP_SPEED) return null;
        Vec3d outward = pose.direction(new Vec3d(side, 0, 0));
        outward = outward.subtract(normal.multiply(outward.dotProduct(normal))).normalize();
        // The outer turning side pushes into water; the retreating side sheds a weaker wave.
        double normalMotion = relative.dotProduct(outward);
        double forcingSpeed = Math.hypot(relative.dotProduct(pose.direction(new Vec3d(0, 0, 1))), Math.max(0, normalMotion));
        double power = MathHelper.clamp(WAKE_BASE_STRENGTH + Math.max(0, forcingSpeed - WAKE_STOP_SPEED) * WAKE_SPEED_GAIN,
                WAKE_BASE_STRENGTH, MAX_WAKE_STRENGTH)
                * MathHelper.clamp(speed / WAKE_START_SPEED, 0, 1);
        return new WaterImpact(boat.getUuid(), at, relative, normal, outward, 0.8,
                Math.max(0, -relative.dotProduct(normal)), speed, power, WaterImpact.Kind.SKIM, side, water.flow());
    }

    private static Contact contact(ClientWorld world, Vec3d previous, Vec3d now, Vec3d hullNormal,
                                   boolean entryReady) {
        if (!entryReady) return null;
        Vec3d velocity = now.subtract(previous);
        if (velocity.lengthSquared() < 0.004) return null;
        int steps = MathHelper.clamp((int) Math.ceil(velocity.length() / 0.45), 1, 8);
        for (int step = 1; step <= steps; step++) {
            Vec3d a = previous.lerp(now, (step - 1.0) / steps), b = previous.lerp(now, (double) step / steps);
            // Visual wave slopes must not turn horizontal rowing into repeated new slaps.
            // Detect entry against the underlying fluid; droplets still follow displaced water.
            if (samplesLeft-- <= 0) return null;
            var water = CONTACT_SURFACE.sample(world, b);
            if (water == null) continue;
            Vec3d n = water.normal(), relative = velocity.subtract(water.flow());
            double before = a.subtract(water.position()).dotProduct(n), after = b.subtract(water.position()).dotProduct(n);
            double normalSpeed = Math.max(0, -relative.dotProduct(n));
            double tangentSpeed = relative.subtract(n.multiply(relative.dotProduct(n))).length();
            if (before > 0 && after <= 0 && normalSpeed > 0.09) {
                double t = before / (before - after);
                Vec3d point = a.lerp(b, t);
                return new Contact(point, relative, n, hullNormal, water.flow(), normalSpeed, tangentSpeed,
                        normalSpeed * 1.6 + Math.min(1, tangentSpeed) * 0.18, WaterImpact.Kind.ENTRY);
            }
        }
        return null;
    }
    private static double distanceToHull(BoatEntity boat, Vec3d point) {
        Vec3d local = BoatBody.local(boat, point);
        double z = Math.max(0, Math.abs(local.z) - BoatGeometry.halfLength(boat));
        double x=Math.max(0,Math.abs(local.x)-BoatGeometry.halfWidth(boat));
        return x*x + local.y * local.y + z * z;
    }
}
