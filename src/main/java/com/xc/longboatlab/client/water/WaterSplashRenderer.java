package com.xc.longboatlab.client.water;

import com.xc.longboatlab.client.BoatEffectRenderPass;
import java.util.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

/** Transient world-space sheets, ballistic droplets, mist and shoreline-clipped foam.
 * Simulation and collision run at 20 Hz; rendering only interpolates cached geometry.
 */
final class WaterSplashRenderer {
    // Spray is scattering water, not a solid two-sided hull. Mirrored quad winding must not
    // turn one side's lighting normal downward; retain lightmap lighting with a shared up normal.
    private static final Vec3d LIGHT_NORMAL = new Vec3d(0, 1, 0);
    private static final Identifier SHEET = texture("impact_sheet"), FOAM = texture("foam"),
            DROP = texture("drop"), MIST = texture("vapor"), RIBBON = texture("ribbon"), FROTH = texture("froth"), LACE = texture("wake_lace");
    private static final int MAX_SHEETS = 16, MAX_DROPS = 384, MAX_MIST = 192, MAX_RIPPLES = 48;
    private static final int WAKE_LIFE = 92;
    private static final double WAKE_WIDTH = 0.60, WAKE_WIDTH_GAIN = 0.40;
    private static final double WAKE_TEXTURE_SCALE = 0.35;
    private static final double MAX_WAKE_SPRAY_SPEED = 0.22;
    private static final double DROP_GRAVITY = 0.045, MIST_SETTLING = 0.0024;
    private final List<Sheet> sheets = new ArrayList<>();
    private final List<Fleck> drops = new ArrayList<>(), mist = new ArrayList<>();
    private final List<Ripple> ripples = new ArrayList<>();
    private final List<WakeStrip> wakes = new ArrayList<>();
    private record HullRibbon(int seen, Map<Double, WakeNode> nodes, List<WakeStrip> strips, double tail) {}
    private final Map<WakeKey, HullRibbon> hullRibbons = new HashMap<>();
    private record WakeKey(UUID boat, int side) {}
    private final Map<WakeKey, WakeNode> wakeHeads = new HashMap<>();
    private final Random random = new Random();
    private int raysLeft, waterQueriesLeft, secondaryLeft, clock, liveStripsLeft, crestParticlesLeft;
    private static Identifier texture(String name) {
        return Identifier.of("longboatlab", "textures/water/" + name + ".png");
    }
    void clear() { sheets.clear(); drops.clear(); mist.clear(); ripples.clear(); wakes.clear(); wakeHeads.clear(); hullRibbons.clear(); clock = 0; }
    record Counts(int hullStrips, int wakeStrips, int splashes, int drops, int mist, int ripples) {}
    Counts counts() {
        return new Counts(hullRibbons.values().stream().mapToInt(h -> h.strips.size()).sum(),
                wakes.size(), sheets.size(), drops.size(), mist.size(), ripples.size());
    }
    private static int quality(MinecraftClient client) {
        return switch (client.options.getParticles().getValue()) { case MINIMAL -> 1; case DECREASED -> 2; default -> 3; };
    }
    private record Appearance(int color, int light) {
        static Appearance at(ClientWorld world, Vec3d point) {
            BlockPos pos = BlockPos.ofFloored(point);
            return new Appearance(BiomeColors.getWaterColor(world, pos), WorldRenderer.getLightmapCoordinates(world, pos.up()));
        }
    }
    private WaterSurfaceModel.Surface waterAt(ClientWorld world, Vec3d point) {
        if (waterQueriesLeft-- <= 0) return null;
        return BoatWaterEffects.sample(world, point);
    }
    /** Unknown chunks or exhausted budgets stop a visual instead of letting it penetrate terrain. */
    private boolean clearPath(MinecraftClient client, Vec3d from, Vec3d to) {
        if (raysLeft-- <= 0 || !com.xc.longboatlab.BoatEnvironment.isLoaded(client.world, BlockPos.ofFloored(from))
                || !com.xc.longboatlab.BoatEnvironment.isLoaded(client.world, BlockPos.ofFloored(to))) return false;
        return client.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, client.player)).getType() == HitResult.Type.MISS;
    }
    void tick(MinecraftClient client) {
        clock++;
        raysLeft = 384; waterQueriesLeft = 384; secondaryLeft = 8;
        liveStripsLeft = 80; crestParticlesLeft = 10 * quality(client);
        Vec3d camera = client.gameRenderer.getCamera().getPos();
        for (Iterator<Sheet> it = sheets.iterator(); it.hasNext();) {
            Sheet sheet = it.next();
            if (sheet.origin.squaredDistanceTo(camera) > 104 * 104 || !sheet.tick(client)) it.remove();
        }
        // A node shared by two spans is advanced ONCE, so both sides of every seam are identical.
        Set<WakeNode> nodes = Collections.newSetFromMap(new IdentityHashMap<>());
        for (WakeStrip span : wakes) { nodes.add(span.a); nodes.add(span.b); }
        for (HullRibbon hull : hullRibbons.values()) nodes.addAll(hull.nodes.values());
        nodes.addAll(wakeHeads.values());
        for (WakeNode node : nodes) node.tick(client);
        for (Iterator<HullRibbon> it = hullRibbons.values().iterator(); it.hasNext();) {
            HullRibbon hull = it.next();
            if (clock - hull.seen > 2) {
                for (WakeNode node : hull.nodes.values()) node.release();
                for (WakeStrip span : hull.strips) addSpan(span, quality(client));
                it.remove();
            }
        }
        wakes.removeIf(span -> span.b.origin.squaredDistanceTo(camera) > 104 * 104
                || (span.a.expired() && span.b.expired()));
        wakeHeads.values().removeIf(node -> clock - node.seen > 6);
        for (Iterator<Ripple> it = ripples.iterator(); it.hasNext();) {
            Ripple ripple = it.next();
            if (ripple.origin.squaredDistanceTo(camera) > 104 * 104 || !ripple.tick(client)) it.remove();
        }
        // Ribbons keep priority over the denser fine spray. Extra droplets cannot starve wake continuity.
        advance(client, drops, camera);
        advance(client, mist, camera);
        waterQueriesLeft = Math.max(0, waterQueriesLeft) + 256;
        raysLeft = Math.max(0, raysLeft) + 128;
    }
    private void advance(MinecraftClient client, List<Fleck> particles, Vec3d camera) {
        for (Iterator<Fleck> it = particles.iterator(); it.hasNext();) {
            Fleck p = it.next();
            if (p.position.squaredDistanceTo(camera) > 104 * 104 || !p.tick(client)) it.remove();
        }
    }
    private record SpraySource(WakeStrip span, double endWeight) {}

    /** Stratified samples along complete crests, sharing the existing global particle budget. */
    void finishTick(MinecraftClient client) {
        if (crestParticlesLeft <= 0) return;
        List<SpraySource> sources = new ArrayList<>();
        double total = 0;
        for (HullRibbon hull : hullRibbons.values()) {
            if (clock - hull.seen > 1) continue;
            for (WakeStrip span : hull.strips) total = spraySource(sources, span, total, true);
        }
        for (WakeStrip span : wakes) total = spraySource(sources, span, total, false);
        if (total <= 0) return;
        int quality = quality(client);
        int count = Math.min(crestParticlesLeft, (int)Math.ceil(total * quality * 3.4));
        int sourceIndex = 0;
        double startWeight = 0;
        for (int i = 0; i < count; i++) {
            double sample = total * (i + random.nextDouble()) / count;
            while (sourceIndex < sources.size() - 1 && sample >= sources.get(sourceIndex).endWeight) {
                startWeight = sources.get(sourceIndex++).endWeight;
            }
            SpraySource source = sources.get(sourceIndex);
            WakeNode a = source.span.a, b = source.span.b;
            double t = (sample - startWeight) / (source.endWeight - startWeight);
            double power = MathHelper.lerp(t, a.strength, b.strength);
            Vec3d out = a.outward.lerp(b.outward, t).normalize();
            if (out.lengthSquared() < 0.5) continue;
            boolean fog = (i + clock) % 3 == 0;
            // Never substitute a long-lived vapor puff for a ballistic drop when a pool fills.
            // Those substitutions formerly left bead-like "droplets" hovering over the wake.
            List<Fleck> pool = fog ? mist : drops;
            if (pool.size() >= (fog ? MAX_MIST : MAX_DROPS) * quality / 3) continue;
            double across = MathHelper.lerp(t, WakeProfile.crest(a.phase), WakeProfile.crest(b.phase))
                    + (random.nextDouble() - 0.5) * 0.12;
            Vec3d at = a.point(across, 1, false).lerp(b.point(across, 1, false), t).add(0, 0.025, 0);
            Vec3d velocity = wakeVelocity(a.relativeMotion.lerp(b.relativeMotion, t),
                    a.waterFlow.lerp(b.waterFlow, t), out, power, fog, random.nextDouble() * 2 - 1);
            Fleck fleck = new Fleck(at, velocity, fog ? 0.16 + random.nextDouble() * 0.12
                    : 0.004 + random.nextDouble() * 0.006, fog, a.appearance);
            // The birth frame follows the interpolated crest, not its next-tick position.
            // Subsequent ticks are independent world-space flight.
            fleck.previous = a.point(across, 0, false).lerp(b.point(across, 0, false), t).add(0, 0.025, 0);
            pool.add(fleck);
            crestParticlesLeft--;
        }
    }
    private double spraySource(List<SpraySource> sources, WakeStrip span, double total, boolean live) {
        WakeNode a = span.a, b = span.b;
        double power = (a.strength + b.strength) * 0.5;
        double fade = Math.min(a.visibility, b.visibility) * Math.min(1, (a.alpha + b.alpha) / 140);
        double age = Math.max(a.released < 0 ? 0 : clock - a.released, b.released < 0 ? 0 : clock - b.released);
        if (!live) fade *= 0.18 * Math.max(0, 1 - age / 18);
        double weight = Math.min(4, a.inner.distanceTo(b.inner)) * Math.max(0, power - 0.12)
                * (a.entry + b.entry) * 0.5 * fade * (0.18 + 0.82 * (a.breaking + b.breaking) * 0.5);
        if (weight <= 0.001) return total;
        sources.add(new SpraySource(span, total + weight));
        return total + weight;
    }
    /** Water gains a fraction of the local hull momentum, then trails the moving hull.
     * Never negate the hull velocity: that launches the spray backwards in world space.
     * The along-hull component also handles reverse travel and opposite ends during a turn.
     */
    private static Vec3d wakeVelocity(Vec3d relative, Vec3d flow, Vec3d outward,
                                      double power, boolean fog, double scatter) {
        Vec3d out = new Vec3d(outward.x, 0, outward.z).normalize();
        Vec3d flat = new Vec3d(relative.x, 0, relative.z);
        double intoWater = flat.dotProduct(out);
        Vec3d along = flat.subtract(out.multiply(intoWater));
        Vec3d drift = along.multiply(fog ? 0.12 : 0.25);
        double speed = drift.length();
        if (speed > 0.12) drift = drift.multiply(0.12 / speed);
        double eject = Math.min(0.17, 0.035 + power * 0.060 + Math.max(0, intoWater) * 0.08)
                * (fog ? 0.60 : 1) * (0.9 + Math.abs(scatter) * 0.2);
        Vec3d jitter = new Vec3d(-out.z, 0, out.x).multiply(Math.min(0.008, along.length() * 0.03) * scatter);
        double lift = Math.min(0.17, 0.06 + power * 0.09) * (fog ? 0.16 : 1) * (1 + scatter * 0.22);
        Vec3d spray = drift.add(out.multiply(eject)).add(jitter).add(0, lift, 0);
        double magnitude = spray.length();
        // Fine crest spray stays on the inexpensive collision path, even on debug-speed boats.
        if (magnitude > MAX_WAKE_SPRAY_SPEED) spray = spray.multiply(MAX_WAKE_SPRAY_SPEED / magnitude);
        return flow.add(spray);
    }
    void emit(MinecraftClient client, WaterImpact impact) {
        int quality = quality(client);
        boolean wake = impact.kind() == WaterImpact.Kind.SKIM;
        Appearance appearance = Appearance.at(client.world, impact.position());
        int sheetLimit = MAX_SHEETS * quality / 3;
        if (!wake && sheets.size() >= sheetLimit) {
            Sheet weakest = sheets.stream().min(Comparator.comparingDouble(s -> s.strength * (1 - s.age / (double) s.life))).orElse(null);
            if (weakest != null && impact.strength() > weakest.strength * (1 - weakest.age / (double) weakest.life))
                sheets.remove(weakest);
        }
        if (!wake && sheets.size() < sheetLimit) sheets.add(new Sheet(impact, appearance,
                impact.kind() == WaterImpact.Kind.SKIM ? 6 + quality : 8 + quality * 3));
        // Reserve a little spray for a new heavy slap even while a fast wake fills the particle pools.
        if (impact.kind() == WaterImpact.Kind.ENTRY && impact.strength() > 0.5) {
            int free = MAX_DROPS * quality / 3 - drops.size();
            if (free < quality * 6) drops.subList(0, Math.min(drops.size(), quality * 6 - free)).clear();
        }
        if (wake && impact.wakeSide() != 0) addWake(impact, appearance, quality);
        else addRipple(impact.position(), impact.strength(), false, appearance, quality);
        int count = (int) ((wake ? Math.max(0, impact.strength() - 0.45) * 2 : 7 + impact.strength() * 7) * quality);
        Vec3d normal = impact.surfaceNormal();
        Vec3d tangent = impact.velocity().subtract(normal.multiply(impact.velocity().dotProduct(normal)));
        Vec3d outward = impact.hullNormal().subtract(normal.multiply(impact.hullNormal().dotProduct(normal))).normalize();
        for (int i = 0; i < count && drops.size() < MAX_DROPS * quality / 3; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            Vec3d radial = wake ? outward : radial(normal, angle);
            double speed = ((wake ? 0.025 : 0.08) + impact.strength() * 0.17) * (0.5 + random.nextDouble());
            Vec3d velocity = wake ? wakeVelocity(impact.velocity(), impact.waterFlow(), outward,
                    impact.strength(), false, random.nextDouble() * 2 - 1)
                    : impact.waterFlow().add(radial.multiply(speed)).add(tangent.multiply(0.22))
                    .add(normal.multiply((0.12 + impact.strength() * 0.23) * (0.5 + random.nextDouble() * 0.6)));
            double footprint = wake ? 0 : MathHelper.clamp(Math.sqrt(impact.area()) * 0.42, 0.28, 1.4);
            boolean lowEjecta = !wake && i % 3 != 0;
            if (lowEjecta) velocity = impact.waterFlow().add(radial.multiply(speed * 1.35)).add(tangent.multiply(0.20))
                    .add(normal.multiply(0.055 + random.nextDouble() * 0.09));
            drops.add(new Fleck(impact.position().add(radial.multiply(footprint)).add(normal.multiply(0.035)), velocity,
                    wake ? 0.007 + random.nextDouble()*0.008 : lowEjecta ? 0.016 + random.nextDouble() * 0.018 : 0.03 + random.nextDouble() * 0.03, false, appearance));
        }
    }
    private void addRipple(Vec3d at, double strength, boolean wake, Appearance appearance, int quality) {
        if (ripples.size() < MAX_RIPPLES * quality / 3)
            ripples.add(new Ripple(at, strength, wake, appearance, 8 + quality * 4));
    }
    void resetHullWake(UUID boat) {
        for (var entry : hullRibbons.entrySet()) if (entry.getKey().boat.equals(boat)) {
            for (WakeNode node : entry.getValue().nodes.values()) node.release();
        }
        hullRibbons.keySet().removeIf(key -> key.boat.equals(boat));
        wakeHeads.keySet().removeIf(key -> key.boat.equals(boat));
    }
    private void addSpan(WakeStrip span, int quality) {
        if (wakes.size() >= 64 * quality) wakes.remove(0);
        wakes.add(span);
    }
    /** Persistent hull-local nodes. The trailing node is also the head of the free wake. */
    void hullWake(MinecraftClient client, net.minecraft.entity.vehicle.BoatEntity boat, int side,
                  List<Double> stations, List<WaterImpact> contacts, double tail, Vec3d flowAxis) {
        WakeKey key = new WakeKey(boat.getUuid(), side);
        HullRibbon old = hullRibbons.get(key);
        int count = contacts.size();
        double leading = -tail, riseLength = Math.min(2.5, Math.max(0.55, Math.abs(tail) * 0.85));
        // Budget exhaustion is unknown, not dry water: retain the complete old ribbon this tick.
        if (liveStripsLeft < count - 1 || waterQueriesLeft < count * 3
                || raysLeft < count * 2 || !BoatWaterEffects.hasWaterSamples(count * 3)) {
            if (old != null) hullRibbons.put(key, new HullRibbon(clock, old.nodes, old.strips, old.tail));
            return;
        }
        Map<Double, WakeNode> nodes = new LinkedHashMap<>();
        List<WakeStrip> strips = new ArrayList<>();
        WakeNode preceding = null;
        double phaseOffset = 0;
        if (old != null && !old.nodes.isEmpty()) {
            var reference = old.nodes.entrySet().iterator().next();
            phaseOffset = reference.getValue().phase - reference.getKey() * WAKE_TEXTURE_SCALE;
        }
        for (int i = 0; i < count; i++) {
            double station = stations.get(i);
            WaterImpact contact = contacts.get(i);
            WakeNode prior = old == null ? null : old.nodes.get(station);
            if (contact == null) {
                if (prior != null) prior.release();
                preceding = null; continue;
            }
            Vec3d out = contact.hullNormal().normalize();
            double strength = prior == null ? contact.strength() : MathHelper.lerp(0.25, prior.strength, contact.strength());
            double entry = smooth(Math.abs(station - leading) / riseLength);
            double width = 0.02 + (WAKE_WIDTH + strength * WAKE_WIDTH_GAIN) * entry;
            Vec3d inner = contact.position().add(out.multiply(0.02));
            Vec3d outer = contact.position().add(out.multiply(width));
            var water = waterAt(client.world, outer);
            var middle = waterAt(client.world, inner.lerp(outer, 0.5));
            var along = preceding == null ? middle : waterAt(client.world, preceding.inner.lerp(inner, 0.5));
            if (water == null || middle == null || along == null || Math.abs(water.position().y - inner.y) > 1.8
                    || !clearPath(client, inner, outer)
                    || (preceding != null && !clearPath(client, preceding.inner, inner))) {
                if (prior != null) prior.release();
                preceding = null; continue;
            }
            outer = new Vec3d(outer.x, water.position().y + 0.04, outer.z);
            boolean trailing = Math.abs(station - tail) < 0.001;
            // Freeze the old trailing node in world space. The new node is shared by the last
            // hull span and the bridge into history, including every interpolated frame value.
            WakeNode node = prior;
            boolean wasTrailing = old != null && Math.abs(station - old.tail) < 0.001;
            if (node == null || trailing || wasTrailing) {
                node = new WakeNode(contact, Appearance.at(client.world, contact.position()));
                if (prior != null) { node.inherit(prior); prior.release(); }
                else node.phase = node.previousPhase = station * WAKE_TEXTURE_SCALE + phaseOffset;
            }
            node.speed = contact.tangentSpeed();
            node.relativeMotion = contact.velocity(); node.waterFlow = contact.waterFlow();
            double pressure = WakeProfile.pressure(Math.abs(station - leading), contact.velocity().dotProduct(out));
            node.attach(inner, outer, out, strength, entry, pressure, contact.velocity().dotProduct(flowAxis));
            nodes.put(station, node);
            if (preceding != null) strips.add(new WakeStrip(preceding, node));
            preceding = node;
            if (trailing) {
                WakeNode head = wakeHeads.put(key, node);
                if (head != null && old != null && old.tail == tail && head == prior
                        && head.inner.squaredDistanceTo(node.inner) < 16) {
                    head.release();
                    addSpan(new WakeStrip(head, node), quality(client));
                } else if (head != null && head != node) head.release();
            }
        }
        if (old != null) for (var entry : old.nodes.entrySet()) {
            if (!nodes.containsKey(entry.getKey())) entry.getValue().release();
        }
        liveStripsLeft -= strips.size();
        hullRibbons.put(key, new HullRibbon(clock, nodes, strips, tail));
    }
    private void addWake(WaterImpact impact, Appearance appearance, int quality) {
        WakeKey key = new WakeKey(impact.boat(), impact.wakeSide());
        // Hull sampling already emits the exact shared trailing node every tick. A second emitter
        // with a different birth time/width would recreate the seam.
        HullRibbon hull = hullRibbons.get(key);
        if (hull != null && clock - hull.seen <= 2) return;
        WakeNode head = new WakeNode(impact, appearance);
        double width = head.inner.distanceTo(head.outer);
        head.amplitude = head.previousAmplitude = WakeProfile.amplitude(impact.strength(), 1, 1, width);
        head.breaking = head.previousBreaking = WakeProfile.breaking(head.amplitude, width, head.strength);
        head.alpha = head.previousAlpha = 175 * Math.min(1, 0.25 + impact.strength() * 0.8);
        head.release();
        WakeNode prior = wakeHeads.put(key, head);
        if (prior != null && clock - prior.seen <= 4 && prior.origin.squaredDistanceTo(head.origin) < 144
                && prior.outward.dotProduct(head.outward) >= 0) {
            prior.release(); addSpan(new WakeStrip(prior, head), quality);
        }
    }
    private final class WakeNode {
        Vec3d origin, outward, inner, outer, previousInner, previousOuter, relativeMotion, waterFlow;
        double strength, amplitude, previousAmplitude, alpha, previousAlpha, visibility = 1;
        double breaking, previousBreaking, releasedBreaking;
        double releasedAmplitude, releasedAlpha, surfaceY, speed, phase, previousPhase, entry = 1;
        final Vec3d[] frameCurve=new Vec3d[WakeProfile.SAMPLES+1];
        int curveClock=-1;float curveDelta=-1;
        int seen = clock, released = -1, advanced = clock;
        final Appearance appearance;
        WakeNode(WaterImpact impact, Appearance appearance) {
            this.appearance = appearance; origin = impact.position(); outward = impact.hullNormal().normalize();
            relativeMotion = impact.velocity(); waterFlow = impact.waterFlow();
            strength = impact.strength(); speed = impact.tangentSpeed();
            phase = previousPhase = origin.x * 0.37 + origin.z * 0.41;
            inner = previousInner = origin.add(outward.multiply(0.02));
            outer = previousOuter = origin.add(outward.multiply(0.02 + WAKE_WIDTH + strength * WAKE_WIDTH_GAIN));
            surfaceY = outer.y;
        }
        void inherit(WakeNode prior) {
            inner = prior.inner; outer = prior.outer;
            previousInner = prior.previousInner; previousOuter = prior.previousOuter;
            amplitude = prior.amplitude; previousAmplitude = prior.previousAmplitude;
            alpha = prior.alpha; previousAlpha = prior.previousAlpha;
            breaking = prior.breaking; previousBreaking = prior.previousBreaking;
            visibility = prior.visibility; surfaceY = prior.surfaceY; entry = prior.entry;
            phase = prior.phase; previousPhase = prior.previousPhase;
        }
        void attach(Vec3d nextInner, Vec3d nextOuter, Vec3d out, double power, double entryRamp,
                    double pressure, double alongMotion) {
            released = -1; seen = clock; outward = out; strength = power; entry = entryRamp;
            // Texture phase is continuous through release and shared by both sides of each seam.
            // A stable local-Z texture axis and signed water-relative travel make both gunwales
            // flow towards the trailing end, including reverse travel and rotation across 360 degrees.
            phase += MathHelper.clamp(alongMotion, -1.5, 1.5) * WAKE_TEXTURE_SCALE;
            origin = nextInner;
            // XZ follows the real hull; smooth the sampled water height and wave strength separately.
            inner = new Vec3d(nextInner.x, MathHelper.lerp(0.35, inner.y, nextInner.y), nextInner.z);
            outer = new Vec3d(nextOuter.x, MathHelper.lerp(0.35, outer.y, nextOuter.y), nextOuter.z);
            surfaceY = nextOuter.y;
            double width = nextInner.distanceTo(nextOuter);
            double targetHeight = WakeProfile.amplitude(power, entryRamp, pressure, width);
            amplitude += (targetHeight - amplitude) * 0.25;
            breaking += (WakeProfile.breaking(targetHeight, width, power) * entryRamp - breaking) * 0.25;
            alpha += (175 * Math.min(1, 0.25 + power * 0.8) * entryRamp - alpha) * 0.25;
        }
        void release() {
            if (released >= 0) return;
            released = clock;
            releasedAmplitude = amplitude; releasedAlpha = alpha;
            releasedBreaking = breaking;
        }
        boolean expired() { return released >= 0 && clock - released > WAKE_LIFE; }
        void tick(MinecraftClient client) {
            if (advanced == clock) return;
            advanced = clock;
            previousInner = inner; previousOuter = outer;
            previousAmplitude = amplitude; previousAlpha = alpha; previousPhase = phase;
            previousBreaking = breaking;
            if (released < 0) return;
            double age = clock - released;
            double fade = (1 - smooth(age / WAKE_LIFE)) / Math.sqrt(1 + age * 0.04);
            // Divergent gravity waves fan outward with boat speed, retaining a bounded lateral
            // speed and gradual dispersion instead of widening indefinitely at extreme speeds.
            double spread = MathHelper.clamp(speed * 0.44, 0.05, 0.29) / Math.sqrt(1 + age * 0.03);
            Vec3d nextInner = inner.add(outward.multiply(spread));
            Vec3d nextOuter = outer.add(outward.multiply(spread + 0.019));
            // Each shared node owns one height sample/cache. Adjacent spans can never sample the
            // same endpoint on different tick phases. No deletion on budget exhaustion.
            if (clock % 3 == released % 3 && waterQueriesLeft > 0 && raysLeft > 0
                    && BoatWaterEffects.hasWaterSamples(1)) {
                var water = waterAt(client.world, nextOuter);
                if (water == null || Math.abs(water.position().y - outer.y) > 1.8
                        || !clearPath(client, outer, nextOuter)) visibility = 0;
                else surfaceY = water.position().y + 0.04;
            }
            inner = new Vec3d(nextInner.x, MathHelper.lerp(0.25, inner.y, surfaceY), nextInner.z);
            outer = new Vec3d(nextOuter.x, MathHelper.lerp(0.25, outer.y, surfaceY), nextOuter.z);
            amplitude = releasedAmplitude * Math.exp(-age * 0.052) * (1-smooth(age/WAKE_LIFE));
            breaking = releasedBreaking * Math.exp(-age * 0.065);
            double targetAlpha = releasedAlpha * fade * visibility;
            alpha = visibility == 0 ? alpha * 0.65 : targetAlpha;
        }

        Vec3d point(double across, float delta, boolean flat) {
            across = MathHelper.clamp(across, 0, 1);
            if (!flat) {
                prepareCurve(delta);
                double sample = across * WakeProfile.SAMPLES;
                int lower = Math.min(WakeProfile.SAMPLES - 1, (int) sample);
                // Foam and spray use the exact tessellated water surface, not a separate
                // analytic arc that can cut into it between the mesh vertices.
                double fraction = sample - lower;
                if (fraction < 1.0e-8) return frameCurve[lower];
                if (fraction > 1 - 1.0e-8) return frameCurve[lower + 1];
                return frameCurve[lower].lerp(frameCurve[lower + 1], fraction);
            }
            Vec3d a = previousInner.lerp(inner, delta), b = previousOuter.lerp(outer, delta);
            double time = clock - 1 + delta;
            double h = 0, profile = 0;
            if (released >= 0) {
                double age = Math.max(0, time - released);
                double wavePhase = MathHelper.lerp(delta, previousPhase, phase) * Math.PI * 2;
                // Several wavelengths disperse at different rates; history stays at the actual turn.
                profile = (Math.sin(across*Math.PI*3-Math.sqrt(age+1)*0.65)
                        +0.32*Math.sin(across*Math.PI*7-Math.sqrt(age+1)*1.1+wavePhase*0.12))*Math.sin(across*Math.PI);
                h = releasedAmplitude * 0.26 * smooth(age / 12) * Math.exp(-age*0.028)*(1-smooth(age/WAKE_LIFE));
            }
            return a.lerp(b, across).add(0, profile * h, 0);
        }
        private void prepareCurve(float delta) {
            if (curveClock == clock && curveDelta == delta) return;
            curveClock = clock; curveDelta = delta;
            Vec3d a = previousInner.lerp(inner, delta), b = previousOuter.lerp(outer, delta);
            double p = MathHelper.lerp(delta, previousPhase, phase);
            double crest = WakeProfile.crest(p);
            double height = Math.min(a.distanceTo(b) * 0.34,
                    MathHelper.lerp(delta, previousAmplitude, amplitude) * WakeProfile.modulation(p));
            for (int i = 0; i <= WakeProfile.SAMPLES; i++) {
                double across = i / (double) WakeProfile.SAMPLES;
                frameCurve[i] = a.lerp(b, across).add(0, WakeProfile.height(across, crest) * height, 0);
            }
        }
        double foamEdge(int band,boolean high,boolean flat,float delta) {
            double p=MathHelper.lerp(delta,previousPhase,phase);
            double amount=MathHelper.lerp(delta,previousBreaking,breaking);
            double drift=flat?0.025*Math.sin(p*8.1-(clock-1+delta)*0.08+band*2.1):0;
            double center=flat?0.17+band*0.29:band==0?WakeProfile.crest(p):0.865;
            double width=flat?0.13:band==0?0.070+amount*0.045:0.115;
            return MathHelper.clamp(center+drift+(high?width:-width),0.01,0.99);
        }
        int opacity(float delta, boolean flat) {
            double age = released < 0 ? 0 : Math.max(0, clock - 1 + delta - released);
            double transition = smooth((age - 10) / 14);
            return (int) (MathHelper.lerp(delta, previousAlpha, alpha) * (flat ? transition : 1 - transition));
        }
    }
    /** Both hull and free wake spans use the same node geometry, interpolation, width and alpha. */
    private final class WakeStrip {
        final WakeNode a, b;
        WakeStrip(WakeNode a, WakeNode b) { this.a = a; this.b = b; }
        void renderWave(float delta, MatrixStack.Entry matrix, VertexConsumer vertices, Vec3d camera, boolean foam) {
            draw(delta, matrix, vertices, camera, foam, false);
        }
        void render(float delta, MatrixStack.Entry matrix, VertexConsumer vertices, Vec3d camera) {
            draw(delta, matrix, vertices, camera, true, true);
        }
        private void draw(float delta, MatrixStack.Entry matrix, VertexConsumer vertices, Vec3d camera, boolean foam, boolean flat) {
            int alphaA = a.opacity(delta, flat), alphaB = b.opacity(delta, flat);
            if ((alphaA | alphaB) <= 0) return;
            if (foam && !flat) {
                // Node-owned breaking intensity is shared with neighbouring spans and the
                // released tail. Transparent texture islands, not per-span cutoffs, make gaps.
                alphaA = (int) (alphaA * MathHelper.lerp(delta,a.previousBreaking,a.breaking));
                alphaB = (int) (alphaB * MathHelper.lerp(delta,b.previousBreaking,b.breaking));
                if ((alphaA | alphaB) <= 0) return;
            }
            int strips = foam ? (flat ? 3 : 2) : WakeProfile.SAMPLES;
            int subdivisions=foam&&!flat?4:1;
            for (int i = 0; i < strips; i++) for(int part=0;part<subdivisions;part++) {
                double v0=part/(double)subdivisions,v1=(part+1.0)/subdivisions;
                double alo=foam?a.foamEdge(i,false,flat,delta):i/(double)WakeProfile.SAMPLES;
                double ahi=foam?a.foamEdge(i,true,flat,delta):(i+1)/(double)WakeProfile.SAMPLES;
                double blo=foam?b.foamEdge(i,false,flat,delta):alo, bhi=foam?b.foamEdge(i,true,flat,delta):ahi;
                double lo=MathHelper.lerp(v0,alo,ahi),hi=MathHelper.lerp(v1,alo,ahi);
                // Shared nodes keep both the live hull ribbon and released wake watertight.
                Vec3d p0 = a.point(lo, delta, flat), p1 = b.point(MathHelper.lerp(v0,blo,bhi), delta, flat);
                Vec3d p2 = b.point(MathHelper.lerp(v1,blo,bhi), delta, flat), p3 = a.point(hi, delta, flat);
                if (foam) { p0 = p0.add(0, 0.004, 0); p1 = p1.add(0, 0.004, 0); p2 = p2.add(0, 0.004, 0); p3 = p3.add(0, 0.004, 0); }
                float whiten = foam ? 0.94f : 0.045f;
                float offset = foam ? i * 0.41f : 0;
                float au = (float) MathHelper.lerp(delta, a.previousPhase, a.phase)+offset;
                float bu = (float) MathHelper.lerp(delta, b.previousPhase, b.phase)+offset;
                double bodyFade=foam?(flat?1:i==0?1.80:1.10):0.85;
                nodeVertex(vertices, matrix, p0.subtract(camera), au, foam ? (float)(1-v0) : (float) lo, a, whiten, (int)(alphaA*bodyFade));
                nodeVertex(vertices, matrix, p1.subtract(camera), bu, foam ? (float)(1-v0) : (float) lo, b, whiten, (int)(alphaB*bodyFade));
                nodeVertex(vertices, matrix, p2.subtract(camera), bu, foam ? (float)(1-v1) : (float) hi, b, whiten, (int)(alphaB*bodyFade));
                nodeVertex(vertices, matrix, p3.subtract(camera), au, foam ? (float)(1-v1) : (float) hi, a, whiten, (int)(alphaA*bodyFade));
            }
        }
        private void nodeVertex(VertexConsumer vertices, MatrixStack.Entry matrix, Vec3d p, float u, float v,
                                WakeNode node, float whiten, int alpha) {
            int color = node.appearance.color;
            vertex(vertices, matrix, p, u, v, (int) MathHelper.lerp(whiten, (color >> 16) & 255, 255),
                    (int) MathHelper.lerp(whiten, (color >> 8) & 255, 255),
                    (int) MathHelper.lerp(whiten, color & 255, 255), alpha, node.appearance.light, LIGHT_NORMAL);
        }
    }
    private static double smooth(double t) {
        t = MathHelper.clamp(t, 0, 1); return t * t * (3 - 2 * t);
    }
    private static Vec3d radial(Vec3d normal, double angle) {
        Vec3d u = normal.crossProduct(Math.abs(normal.y) > 0.9 ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0)).normalize();
        Vec3d v = normal.crossProduct(u).normalize();
        return u.multiply(Math.cos(angle)).add(v.multiply(Math.sin(angle)));
    }

    /** Short-lived corrugated crown. Its rim atomizes; no persistent radial tubes. */
    private final class Sheet {
        private static final double[] HEIGHTS = {0,0.2,0.3,0.4,0.6,0.8,0.9,1};
        private static final int[] ROWS = {0,1,3,4,5,7};
        final Vec3d origin, normal;
        final Appearance appearance;
        final Vec3d[] previous, crest, velocity, base, previousBase, radial, footprint;
        final Vec3d[] frameBase, frameCrest;
        final Vec3d[][] frameEdges, frameMids;
        final double[] lobe, noise;
        final boolean[] alive, wet;
        final double strength;
        final int life=22;
        int age;
        Sheet(WaterImpact hit, Appearance appearance, int segments) {
            origin=hit.position();normal=hit.surfaceNormal();this.appearance=appearance;strength=hit.strength();
            previous=new Vec3d[segments];crest=new Vec3d[segments];velocity=new Vec3d[segments];
            base=new Vec3d[segments];previousBase=new Vec3d[segments];radial=new Vec3d[segments];
            footprint=new Vec3d[segments];frameBase=new Vec3d[segments];frameCrest=new Vec3d[segments];
            frameEdges=new Vec3d[segments][HEIGHTS.length];frameMids=new Vec3d[segments][HEIGHTS.length];
            lobe=new double[segments];noise=new double[segments];alive=new boolean[segments];wet=new boolean[segments];
            Vec3d tangent=hit.velocity().subtract(normal.multiply(hit.velocity().dotProduct(normal)));
            if(tangent.length()>1.2)tangent=tangent.normalize().multiply(1.2);
            Vec3d facing=hit.hullNormal().subtract(normal.multiply(hit.hullNormal().dotProduct(normal)));
            Vec3d bias=tangent.multiply(0.5).add(facing.multiply(0.25)).normalize();
            double grazing=MathHelper.clamp(hit.tangentSpeed()/(hit.normalSpeed()+0.18)*0.25,0,0.85);
            double radius=MathHelper.clamp(Math.sqrt(hit.area())*0.46,0.30,1.65);
            double eject=Math.sqrt(Math.min(1.5,Math.max(0.01,hit.normalSpeed())))*0.18;
            double phase=random.nextDouble()*Math.PI*2;
            for(int i=0;i<segments;i++) {
                double angle=phase+Math.PI*2*(i+(random.nextDouble()-0.5)*0.65)/segments;
                Vec3d direction=radial(normal,angle);radial[i]=direction;
                noise[i]=random.nextDouble();lobe[i]=0.48+random.nextDouble()*0.52;
                double directional=MathHelper.clamp(1+grazing*direction.dotProduct(bias),0.22,1.7);
                footprint[i]=origin.add(direction.multiply(radius*(0.88+noise[i]*0.24)));
                velocity[i]=direction.multiply((0.14+eject)*(0.8+noise[i]*0.5)*directional)
                        .add(hit.waterFlow()).add(tangent.multiply(0.18))
                        .add(normal.multiply((0.16+eject)*lobe[i]*Math.sqrt(directional)));
                previous[i]=crest[i]=footprint[i].add(normal.multiply(0.028));
                previousBase[i]=base[i]=footprint[i].add(normal.multiply(0.018));
                alive[i]=wet[i]=true;
            }
        }
        boolean tick(MinecraftClient client) {
            if(++age>=life)return false;
            boolean any=false;
            for(int i=0;i<crest.length;i++) {
                previous[i]=crest[i];previousBase[i]=base[i];
                if(!alive[i])continue;
                Vec3d target=crest[i].add(velocity[i]);
                if(!clearPath(client,crest[i],target)){alive[i]=false;continue;}
                crest[i]=target;velocity[i]=velocity[i].multiply(0.975).add(0,-0.042,0);
                if(waterQueriesLeft>0) {
                    var water=waterAt(client.world,footprint[i].add(radial[i].multiply(age*(0.06+strength*0.016))));
                    wet[i]=water!=null&&Math.abs(water.position().y-origin.y)<1.8;
                    if(wet[i])base[i]=water.position().add(water.normal().multiply(0.025));
                }
                if(crest[i].subtract(base[i]).dotProduct(normal)<0.01){alive[i]=false;continue;}
                // Break up in staggered patches during ascent. The upper rim does not remain
                // tethered to the water as long arms; droplets inherit its velocity and fall.
                if(age==2+i%3 || age==5+i%2) {
                    int pieces=2+(int)(noise[i]*3);
                    for(int part=0;part<pieces && crestParticlesLeft>0 && drops.size()<MAX_DROPS*quality(client)/3;part++) {
                        Vec3d side=normal.crossProduct(radial[i]).multiply((random.nextDouble()-0.5)*0.09);
                        Vec3d spread=side.add(radial[i].multiply(random.nextDouble()*0.055));
                        Fleck drop=new Fleck(crest[i].add(spread),velocity[i].add(spread),
                                0.010+random.nextDouble()*0.012,false,appearance);
                        drop.previous=previous[i].add(spread);drops.add(drop);crestParticlesLeft--;
                    }
                    if(i%3==0 && crestParticlesLeft>0 && mist.size()<MAX_MIST*quality(client)/3) {
                        mist.add(new Fleck(crest[i],velocity[i].multiply(0.10),0.19+noise[i]*0.12,true,appearance));
                        crestParticlesLeft--;
                    }
                }
                any=true;
            }
            return any;
        }
        void prepareFrame(float delta) {
            double time=Math.max(0,age-1+delta);
            for(int i=0;i<crest.length;i++) {
                frameBase[i]=previousBase[i].lerp(base[i],delta);
                frameCrest[i]=previous[i].lerp(crest[i],delta).add(0,0.021*delta*(1-delta),0);
                for(int h=0;h<HEIGHTS.length;h++)frameEdges[i][h]=edge(i,HEIGHTS[h],time);
            }
            // Water and froth reuse the same cached folded surface. Every adjacent sector
            // shares its boundary vertices, instead of recomputing the curves per quad/pass.
            for(int i=0;i<crest.length;i++) {
                int next=(i+1)%crest.length;
                Vec3d out=radial[i].add(radial[next]).normalize();
                for(int h=0;h<HEIGHTS.length;h++) {
                    double fold=Math.sin(HEIGHTS[h]*Math.PI)*(0.055+0.11*noise[i]);
                    frameMids[i][h]=curveMid(frameEdges[(i+crest.length-1)%crest.length][h],
                            frameEdges[i][h],frameEdges[next][h],frameEdges[(next+1)%crest.length][h])
                            .add(out.multiply(fold)).add(0,-fold*0.6,0);
                }
            }
        }
        private Vec3d curveMid(Vec3d a,Vec3d b,Vec3d c,Vec3d d) {
            // Catmull-Rom midpoint: a curved lip instead of straight chords between spikes.
            return new Vec3d((-a.x+9*b.x+9*c.x-d.x)/16,(-a.y+9*b.y+9*c.y-d.y)/16,
                    (-a.z+9*b.z+9*c.z-d.z)/16);
        }
        Vec3d edge(int node,double height,double time) {
            // Irregular folds project into depth, with a falling lip. No cylinder cross-sections.
            double bulge=Math.sin(height*Math.PI)*(0.06+strength*0.08)*(0.7+lobe[node]*0.3);
            double curl=Math.sin(height*Math.PI)*smooth((time-1)/4)*0.10;
            return frameBase[node].lerp(frameCrest[node],height).add(radial[node].multiply(bulge-curl*height));
        }
        Vec3d patch(int i,int next,int col,int height) {
            return col==0?frameEdges[i][height]:col==1?frameMids[i][height]:frameEdges[next][height];
        }
        double opacity(int i,double height,double time) {
            // Thin upper films dissolve first, lower scallops spread into surface foam.
            double breakup=1-smooth((time-(2.0+noise[i]*2.5+(1-height)*2.2))/2.4);
            return smooth(time/0.8)*(1-smooth((time-5)/5))*breakup;
        }
        void render(float delta,MatrixStack.Entry matrix,VertexConsumer vertices,boolean foam,Vec3d camera) {
            double time=Math.max(0,age-1+delta);
            for(int i=0;i<crest.length;i++) {
                int next=(i+1)%crest.length;
                if(!alive[i]||!alive[next]||!wet[i]||!wet[next])continue;
                double landing=smooth(Math.min(frameCrest[i].subtract(frameBase[i]).dotProduct(normal),
                        frameCrest[next].subtract(frameBase[next]).dotProduct(normal))/0.13);
                // Tessellated, bowed fragments give a crown volume from every angle, then clear
                // away in under half a second. The later silhouette consists of falling spray.
                for(int row=0;row<5;row++) {
                    double low=row/5.0,high=(row+1)/5.0;
                    double fade=opacity(i,(low+high)*0.5,time)*landing;
                    if(fade<0.008)continue;
                    if(foam && row!=4 && row!=1)continue;
                    if(foam)low=high-0.10;
                    int lowIndex=foam?(row==1?2:6):ROWS[row],highIndex=ROWS[row+1];
                    for(int col=0;col<2;col++) {
                        double a=col/2.0,b=(col+1)/2.0;
                        quad(vertices,matrix,camera,patch(i,next,col,lowIndex),patch(i,next,col+1,lowIndex),
                                patch(i,next,col+1,highIndex),patch(i,next,col,highIndex),
                                (float)(i*0.37+a),(float)(1-low),(float)(i*0.37+b),(float)(1-high),
                                appearance,foam?0.97f:0.58f,(int)((foam?185:150)*fade));
                    }
                }
            }
        }
    }

    private final class Fleck {
        Vec3d previous, position, velocity;
        final double size;
        final boolean fog;
        final Appearance appearance;
        final int life;
        final int born=clock;
        int age;
        Fleck(Vec3d at, Vec3d velocity, double size, boolean fog, Appearance appearance) {
            previous = position = at; this.velocity = velocity; this.size = size; this.fog = fog;
            this.appearance = appearance; life = fog ? 26 + random.nextInt(13) : 24 + random.nextInt(9);
        }
        boolean tick(MinecraftClient client) {
            // Rim breakup happens before the particle update in this tick. Keep that birth
            // frame on the interpolated crown instead of advancing new drops a second time.
            if(clock==born)return true;
            if (++age >= life) return false;
            previous = position;
            Vec3d target = position.add(velocity);
            // Fine slow spray uses the destination cell's cached voxel shape. It must not
            // spend the ray budget reserved for the continuous water ribbons.
            BlockPos block=BlockPos.ofFloored(target);
            if(!com.xc.longboatlab.BoatEnvironment.isLoaded(client.world, block))return false;
            var shape=client.world.getBlockState(block).getCollisionShape(client.world,block);
            if(!shape.isEmpty() && shape.getBoundingBox().offset(block).contains(target))return false;
            if(velocity.lengthSquared()>0.0625 && !clearPath(client,position,target))return false;
            if (!fog && velocity.y < 0) {
                var water = waterAt(client.world, target);
                if (water != null && previous.subtract(water.position()).dotProduct(water.normal()) > 0
                        && target.subtract(water.position()).dotProduct(water.normal()) <= 0) {
                    if (secondaryLeft-- > 0) addRipple(water.position(), 0.06, true, appearance, quality(client));
                    return false;
                }
            }
            position = target;
            velocity = velocity.multiply(fog ? 0.92 : 0.985).add(0, fog ? -MIST_SETTLING : -DROP_GRAVITY, 0);
            return true;
        }
        void render(float delta, MatrixStack matrices, VertexConsumer vertices, Vec3d camera,
                    org.joml.Quaternionf cameraRotation) {
            Vec3d at = previous.lerp(position, delta).subtract(camera);
            double fraction = Math.max(0, age - 1 + delta) / life;
            float radius = (float) (size * (fog ? 1 + fraction * 2.3 : 1));
            float height = fog ? radius*0.52f : radius * (float) (1.3 + Math.min(2, velocity.length() * 2));
            int alpha = (int) ((fog ? 70 : 210) * Math.pow(1 - fraction, fog ? 1.1 : 0.5));
            if (fog) alpha = (int) (alpha * Math.min(1, (age + delta) / 4));
            matrices.push(); matrices.translate(at.x, at.y, at.z); matrices.multiply(cameraRotation);
            quad(vertices, matrices.peek(), Vec3d.ZERO, new Vec3d(-radius, -height, 0), new Vec3d(radius, -height, 0),
                    new Vec3d(radius, height, 0), new Vec3d(-radius, height, 0), 0, 1, 1, 0,
                    appearance, fog ? 0.92f : 0.80f, alpha);
            matrices.pop();
        }
    }

    private final class Ripple {
        final Vec3d origin;
        final Appearance appearance;
        final double strength, speed;
        final int life, seed;
        final Vec3d[] inner, outer, previousInner, previousOuter;
        final boolean[] wet, edgeWet, blocked;
        int age, sampledAt;
        Ripple(Vec3d at, double strength, boolean wake, Appearance appearance, int segments) {
            origin = at; this.strength = strength; this.appearance = appearance;
            speed = 0.05 + strength * 0.075; life = wake ? 36 : 60;
            seed = random.nextInt(10000);
            inner = new Vec3d[segments]; outer = new Vec3d[segments];
            previousInner = new Vec3d[segments]; previousOuter = new Vec3d[segments]; wet = new boolean[segments];
            edgeWet = new boolean[segments]; blocked = new boolean[segments];
            Arrays.fill(inner, at); Arrays.fill(outer, at); Arrays.fill(previousInner, at); Arrays.fill(previousOuter, at);
        }
        boolean tick(MinecraftClient client) {
            if (++age >= life) return false;
            // Sample each ring every three ticks; interpolate between successive cached rings.
            if (age != 1 && (age + seed) % 3 != 0) return true;
            System.arraycopy(inner, 0, previousInner, 0, inner.length);
            System.arraycopy(outer, 0, previousOuter, 0, outer.length);
            sampledAt = age;
            double radius = 0.18 + (age + 3) * speed, width = 0.08 + strength * 0.19;
            for (int i = 0; i < inner.length; i++) {
                if (blocked[i]) continue;
                double angle = Math.PI * 2 * i / inner.length;
                Vec3d radial = new Vec3d(Math.cos(angle), 0, Math.sin(angle));
                double irregular = 1 + 0.06 * Math.sin(angle * 5 + seed);
                Vec3d in = origin.add(radial.multiply(radius * irregular));
                Vec3d out = origin.add(radial.multiply(radius * irregular + width));
                var a = waterAt(client.world, in); var b = waterAt(client.world, out);
                wet[i] = a != null && b != null && Math.abs(a.position().y - origin.y) < 1.8
                        && Math.abs(b.position().y - origin.y) < 1.8;
                if (wet[i]) {
                    inner[i] = a.position().add(a.normal().multiply(0.025));
                    outer[i] = b.position().add(b.normal().multiply(0.03));
                    if (age == 1) { previousInner[i] = inner[i]; previousOuter[i] = outer[i]; }
                } else blocked[i] = true;
            }
            for (int i = 0; i < inner.length; i++) {
                int next = (i + 1) % inner.length;
                edgeWet[i] = false;
                if (!wet[i] || !wet[next]) continue;
                var middle = waterAt(client.world, outer[i].lerp(outer[next], 0.5));
                edgeWet[i] = middle != null && Math.abs(middle.position().y - origin.y) < 1.8;
            }
            return true;
        }
        void render(float delta, MatrixStack.Entry entry, VertexConsumer vertices, Vec3d camera) {
            double fraction = Math.max(0, age - 1 + delta) / life;
            float blend = MathHelper.clamp((age - sampledAt + delta) / 3, 0, 1);
            for (int i = 0; i < inner.length; i++) {
                int next = (i + 1) % inner.length;
                if (!edgeWet[i]) continue;
                int alpha = (int) (190 * Math.pow(1 - fraction, 1.5) * (0.55 + noise(seed + i) * 0.45));
                quad(vertices, entry, camera, previousInner[i].lerp(inner[i], blend), previousInner[next].lerp(inner[next], blend),
                        previousOuter[next].lerp(outer[next], blend), previousOuter[i].lerp(outer[i], blend), 0, 1, 1, 0,
                        appearance, 0.93f, alpha);
            }
        }
    }
    private static double noise(int key) {
        int n = key * 1664525 + 1013904223;
        n ^= n >>> 16;
        return (n & 65535) / 65535.0;
    }
    void render(WorldRenderContext context, VertexConsumerProvider consumers) {
        var matrices = context.matrixStack();
        if (matrices == null || consumers == null || (sheets.isEmpty() && drops.isEmpty() && mist.isEmpty() && ripples.isEmpty() && wakes.isEmpty() && hullRibbons.isEmpty())) return;
        float delta = context.tickCounter().getTickDelta(false);
        Vec3d camera = context.camera().getPos();
        for (Sheet sheet : sheets) sheet.prepareFrame(delta);
        var water = consumers.getBuffer(BoatEffectRenderPass.layer(SHEET, false));
        for (Sheet sheet : sheets) sheet.render(delta, matrices.peek(), water, false, camera);
        var ribbon = consumers.getBuffer(BoatEffectRenderPass.softLayer(RIBBON));
        for (WakeStrip wake : wakes) wake.renderWave(delta, matrices.peek(), ribbon, camera, false);
        for (HullRibbon hull : hullRibbons.values()) for (WakeStrip strip : hull.strips)
            strip.renderWave(delta, matrices.peek(), ribbon, camera, false);
        // Finish each layer before requesting the next: Immediate may reuse its fallback buffer.
        var foam = consumers.getBuffer(BoatEffectRenderPass.layer(FOAM, false));
        for (Sheet sheet : sheets) sheet.render(delta, matrices.peek(), foam, true, camera);

        for (Ripple ripple : ripples) ripple.render(delta, matrices.peek(), foam, camera);
        var froth = consumers.getBuffer(BoatEffectRenderPass.softLayer(FROTH));
        for (HullRibbon hull : hullRibbons.values()) for (WakeStrip strip : hull.strips)
            strip.renderWave(delta, matrices.peek(), froth, camera, true);
        for (WakeStrip wake : wakes) wake.renderWave(delta, matrices.peek(), froth, camera, true);
        var lace=consumers.getBuffer(BoatEffectRenderPass.layer(LACE,false));
        for(WakeStrip wake:wakes)wake.render(delta,matrices.peek(),lace,camera);
        var droplet = consumers.getBuffer(BoatEffectRenderPass.layer(DROP, false));
        for (Fleck drop : drops) drop.render(delta, matrices, droplet, camera, context.camera().getRotation());
        var fog = consumers.getBuffer(BoatEffectRenderPass.softLayer(MIST));
        for (Fleck p : mist) p.render(delta, matrices, fog, camera, context.camera().getRotation());
    }
    private static void quad(VertexConsumer out, MatrixStack.Entry matrix, Vec3d camera,
                             Vec3d a, Vec3d b, Vec3d c, Vec3d d, float u0, float v0, float u1, float v1,
                             Appearance appearance, float whiten, int alpha) {
        quad(out, matrix, camera, a, b, c, d, u0, v0, u1, v1, appearance, whiten, alpha, alpha, alpha, alpha);
    }
    private static void quad(VertexConsumer out, MatrixStack.Entry matrix, Vec3d camera,
                             Vec3d a, Vec3d b, Vec3d c, Vec3d d, float u0, float v0, float u1, float v1,
                             Appearance appearance, float whiten, int alphaA, int alphaB, int alphaC, int alphaD) {
        if ((alphaA | alphaB | alphaC | alphaD) <= 0) return;
        Vec3d normal = LIGHT_NORMAL;
        int red = (int) MathHelper.lerp(whiten, (appearance.color >> 16) & 255, 255);
        int green = (int) MathHelper.lerp(whiten, (appearance.color >> 8) & 255, 255);
        int blue = (int) MathHelper.lerp(whiten, appearance.color & 255, 255);
        vertex(out, matrix, a.subtract(camera), u0, v0, red, green, blue, alphaA, appearance.light, normal);
        vertex(out, matrix, b.subtract(camera), u1, v0, red, green, blue, alphaB, appearance.light, normal);
        vertex(out, matrix, c.subtract(camera), u1, v1, red, green, blue, alphaC, appearance.light, normal);
        vertex(out, matrix, d.subtract(camera), u0, v1, red, green, blue, alphaD, appearance.light, normal);
    }
    private static void vertex(VertexConsumer out, MatrixStack.Entry matrix, Vec3d p, float u, float v,
                               int r, int g, int b, int alpha, int light, Vec3d normal) {
        out.vertex(matrix.getPositionMatrix(), (float) p.x, (float) p.y, (float) p.z)
                .color(r, g, b, MathHelper.clamp(alpha,0,255)).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light)
                .normal(matrix, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
