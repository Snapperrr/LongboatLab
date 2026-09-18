package com.xc.longboatlab.client.water;

import java.util.Arrays;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Bounded world-space wave equation, inspired by Evan Wallace's MIT WebGL Water.
 * Height and vertical velocity are separate fields; the replacement mesh uses the very
 * same interpolated heights as spray contact queries. This is not a volumetric fluid solver.
 */
public final class HeightfieldSurface implements WaterSurfaceModel {
    static final int SIZE = 48, N = SIZE * 2 + 1, CELLS = N * N;
    static final float STEP = 0.5f, MAX_HEIGHT = 1.1f;
    // Broader pressure footprints and slower decay increase reach within the same bounded grid.
    private static final float WAVE_SPEED = 0.34f, WAVE_DAMPING = 0.13f, RESTORING = 0.05f;
    private static final double HULL_SIDE_REACH = 5.6;
    private final VanillaWaterSurface vanilla = new VanillaWaterSurface();
    final float[] height = new float[CELLS], previous = new float[CELLS], velocity = new float[CELLS];
    private final float[] nextHeight = new float[CELLS], nextVelocity = new float[CELLS], weight = new float[CELLS];
    private final float[] pressure = new float[CELLS], depthLimit = new float[CELLS];
    private final float[] retainedPressure = new float[CELLS], impactPressure = new float[CELLS];
    private final int[] pressureHold = new int[CELLS];
    private final float[] hullPressure = new float[CELLS], hullWeight = new float[CELLS];
    private final int[] edgeDistance = new int[CELLS];
    final boolean[] wet = new boolean[SIZE * SIZE];
    final int[] colors = new int[SIZE * SIZE], lights = new int[SIZE * SIZE];
    private final float[] cellDepth = new float[SIZE * SIZE];
    private final boolean[] source = new boolean[(SIZE + 4) * (SIZE + 4)];
    private final boolean[] scanned = new boolean[source.length];
    private final int[] scanQueue = new int[source.length];
    private int scanHead, scanEnd;
    private final float[] shiftScratch = new float[CELLS];
    private final int[] shiftInts = new int[CELLS];
    private final boolean[] shiftFlags = new boolean[source.length];
    private static final int SCANS_PER_TICK = 128;
    private int scanBudget=SCANS_PER_TICK;
    private ClientWorld world;
    int originX, originZ, revision;
    double baseY;
    private int age, lastImpact, scanRow, hullsThisTick;
    private boolean active;

    public boolean active() { return active && WaterSurfaceShaders.available(); }
    public int contributingHulls() { return hullsThisTick; }
    @Override public void clear() {
        active = false; world = null; age = lastImpact = scanRow = 0;
        Arrays.fill(height, 0); Arrays.fill(previous, 0); Arrays.fill(velocity, 0);
        Arrays.fill(pressure, 0); hullsThisTick = 0;
        Arrays.fill(retainedPressure, 0); Arrays.fill(impactPressure, 0); Arrays.fill(pressureHold, 0);
        Arrays.fill(wet, false); Arrays.fill(source, false); Arrays.fill(scanned,false);
        scanHead=scanEnd=0;vanilla.clear(); revision++;
    }
    private int index(int x, int z) { return z * N + x; }
    boolean covered(double x, double z) {
        int ix = MathHelper.floor(x - originX), iz = MathHelper.floor(z - originZ);
        return active && ix >= 0 && iz >= 0 && ix < SIZE && iz < SIZE && wet[iz * SIZE + ix];
    }
    private boolean cellWet(int x, int z) {
        return x >= 0 && z >= 0 && x < SIZE && z < SIZE && wet[z * SIZE + x];
    }
    private void scan(int row) {
        int width = SIZE + 4;
        for (int x = 0; x < width; x++) scanCell(row,x);
    }
    private void scanCell(int row,int x) {
            int width=SIZE+4;
            scanned[row*width+x]=true;
            BlockPos pos = BlockPos.ofFloored(originX + x - 2, baseY - 0.01, originZ + row - 2);
            boolean valid = com.xc.longboatlab.BoatEnvironment.isLoaded(world, pos);
            if (valid) {
                var fluid = world.getFluidState(pos);
                valid = fluid.isIn(FluidTags.WATER) && fluid.isStill()
                        && !world.getFluidState(pos.up()).isIn(FluidTags.WATER)
                        && world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()
                        && world.getBlockState(pos.up()).getCollisionShape(world, pos.up()).isEmpty()
                        && Math.abs(pos.getY() + fluid.getHeight(world, pos) - baseY) < 0.02;
            }
            source[row * width + x] = valid;
            int bx = x - 2, bz = row - 2;
            if (bx >= 0 && bz >= 0 && bx < SIZE && bz < SIZE && valid) {
                int cell = bz * SIZE + bx;
                colors[cell] = BiomeColors.getWaterColor(world, pos);
                // Same max-of-water/above light calculation as vanilla FluidRenderer.
                int lower = WorldRenderer.getLightmapCoordinates(world, pos);
                int upper = WorldRenderer.getLightmapCoordinates(world, pos.up());
                lights[cell] = Math.max(lower & 255, upper & 255)
                        | (Math.max((lower >> 16) & 255, (upper >> 16) & 255) << 16);
                // Two water cells below are enough for the bounded visual depression.
                float clearance = (float) (baseY - pos.getY() - 0.06);
                for (int down = 1; down <= 2; down++) {
                    BlockPos below = pos.down(down);
                    if (!world.getFluidState(below).isIn(FluidTags.WATER)
                            || !world.getBlockState(below).getCollisionShape(world, below).isEmpty()) break;
                    clearance += 1;
                }
                cellDepth[cell] = Math.min(1.6f, clearance);
            }
            // Immediately stop masking any cell whose required neighborhood just became invalid.
            if (!valid) for (int dz = -1; dz <= 1; dz++) for (int dx = -1; dx <= 1; dx++) {
                int invalidX = x - 2 + dx, invalidZ = row - 2 + dz;
                if (invalidX >= 0 && invalidZ >= 0 && invalidX < SIZE && invalidZ < SIZE)
                    wet[invalidZ * SIZE + invalidX] = false;
            }
    }
    private void queueUnscanned(Vec3d camera) {
        scanHead=scanEnd=0;int size=SIZE+4;
        int cx=MathHelper.clamp(MathHelper.floor(camera.x-originX)+2,0,size-1);
        int cz=MathHelper.clamp(MathHelper.floor(camera.z-originZ)+2,0,size-1);
        // Visit nearest water first. Unknown cells keep vanilla water until their neighborhood is ready.
        queueCell(cx,cz);
        for(int radius=1;radius<size;radius++) {
            for(int x=cx-radius;x<=cx+radius;x++) { queueCell(x,cz-radius);queueCell(x,cz+radius); }
            for(int z=cz-radius+1;z<cz+radius;z++) { queueCell(cx-radius,z);queueCell(cx+radius,z); }
        }
    }
    private void queueCell(int x,int z) {
        int size=SIZE+4;
        if(x>=0 && z>=0 && x<size && z<size && !scanned[z*size+x])scanQueue[scanEnd++]=z*size+x;
    }
    private void scanPending() {
        while(scanHead<scanEnd && scanBudget>0) {
            int cell=scanQueue[scanHead++];
            if(scanned[cell])continue;
            scanCell(cell/(SIZE+4),cell%(SIZE+4));scanBudget--;
        }
        for(int row=0;scanHead==scanEnd && row<2 && scanBudget>=SIZE+4;row++) {
            scan(scanRow);scanRow=(scanRow+1)%(SIZE+4);scanBudget-=SIZE+4;
        }
    }
    private void rebuildMask() {
        int width = SIZE + 4;
        for (int z = 0; z < SIZE; z++) for (int x = 0; x < SIZE; x++) {
            boolean valid = true;
            // Leave flowing shore margins to vanilla: only a flat 3x3 source-water neighborhood is replaced.
            for (int dz = -1; dz <= 1 && valid; dz++) for (int dx = -1; dx <= 1; dx++)
                if (!source[(z + 2 + dz) * width + x + 2 + dx]) { valid = false; break; }
            wet[z * SIZE + x] = valid;
        }
        for (int z = 0; z < N; z++) for (int x = 0; x < N; x++) {
            int bx = MathHelper.floor(x * STEP), bz = MathHelper.floor(z * STEP), i = index(x, z);
            boolean interior = cellWet(bx, bz) && cellWet(bx - 1, bz) && cellWet(bx, bz - 1) && cellWet(bx - 1, bz - 1);
            edgeDistance[i] = interior ? 8 : 0;
            depthLimit[i] = interior ? Math.min(Math.min(cellDepth[bz * SIZE + bx], cellDepth[bz * SIZE + bx - 1]),
                    Math.min(cellDepth[(bz - 1) * SIZE + bx], cellDepth[(bz - 1) * SIZE + bx - 1])) : 0;
        }
        // Distance to ANY dry edge, not only the square boundary. Smoothly flatten both
        // height and slope near shores/patch limits instead of revealing a rectangular material seam.
        for (int z = 1; z < N; z++) for (int x = 1; x < N; x++) {
            int i = index(x, z);
            edgeDistance[i] = Math.min(edgeDistance[i], Math.min(edgeDistance[i - 1], edgeDistance[i - N]) + 1);
        }
        for (int z = N - 2; z >= 0; z--) for (int x = N - 2; x >= 0; x--) {
            int i = index(x, z);
            edgeDistance[i] = Math.min(edgeDistance[i], Math.min(edgeDistance[i + 1], edgeDistance[i + N]) + 1);
        }
        for (int i = 0; i < CELLS; i++) {
            float t = edgeDistance[i] / 8f;
            weight[i] = t * t * (3 - 2 * t);
            height[i] = MathHelper.clamp(height[i], -depthLimit[i] * weight[i], MAX_HEIGHT * weight[i]);
            previous[i] = MathHelper.clamp(previous[i], -depthLimit[i] * weight[i], MAX_HEIGHT * weight[i]);
            if (weight[i] == 0) height[i] = previous[i] = velocity[i] = pressure[i] = 0;
            if (weight[i] == 0) { retainedPressure[i] = impactPressure[i] = 0; pressureHold[i] = 0; }
        }
    }
    private void open(ClientWorld world, double level, Vec3d camera) {
        this.world = world; baseY = level;
        originX = MathHelper.floor(camera.x / 16) * 16 - 16;
        originZ = MathHelper.floor(camera.z / 16) * 16 - 16;
        queueUnscanned(camera);scanPending();
        active = true; lastImpact = age; rebuildMask(); revision++;
    }
    private void shift(Vec3d camera) {
        int nx = MathHelper.floor(camera.x / 16) * 16 - 16, nz = MathHelper.floor(camera.z / 16) * 16 - 16;
        if (nx == originX && nz == originZ) return;
        int bx=nx-originX,bz=nz-originZ,dx=bx*2,dz=bz*2;
        shiftArray(height,N,dx,dz,shiftScratch);shiftArray(previous,N,dx,dz,shiftScratch);
        shiftArray(velocity,N,dx,dz,shiftScratch);shiftArray(pressure,N,dx,dz,shiftScratch);
        shiftArray(retainedPressure,N,dx,dz,shiftScratch);shiftArray(impactPressure,N,dx,dz,shiftScratch);
        shiftArray(pressureHold,N,dx,dz,shiftInts);
        shiftArray(source,SIZE+4,bx,bz,shiftFlags);shiftArray(scanned,SIZE+4,bx,bz,shiftFlags);
        shiftArray(cellDepth,SIZE,bx,bz,shiftScratch);
        shiftArray(colors,SIZE,bx,bz,shiftInts);shiftArray(lights,SIZE,bx,bz,shiftInts);
        // Border water was sampled only for validity, without color/light/depth. Rescan it before
        // it enters the drawable interior, so an old valid flag can never expose a black/missing tile.
        for(int z=0;z<SIZE;z++)for(int x=0;x<SIZE;x++)
            if(x+bx<0 || x+bx>=SIZE || z+bz<0 || z+bz>=SIZE) {
                int cell=(z+2)*(SIZE+4)+x+2;source[cell]=scanned[cell]=false;
            }
        originX=nx;originZ=nz;queueUnscanned(camera);rebuildMask();revision++;
    }
    /** One reusable buffer; overlap survives a chunk crossing without synchronous world queries. */
    private static void shiftArray(Object values,int size,int dx,int dz,Object scratch) {
        int length=size*size;
        System.arraycopy(values,0,scratch,0,length);
        if(values instanceof float[] a)Arrays.fill(a,0);
        else if(values instanceof int[] a)Arrays.fill(a,0);
        else Arrays.fill((boolean[])values,false);
        int firstX=Math.max(0,-dx),lastX=Math.min(size,size-dx);
        if(firstX>=lastX)return;
        for(int z=Math.max(0,-dz);z<Math.min(size,size-dz);z++)
            System.arraycopy(scratch,(z+dz)*size+firstX+dx,values,z*size+firstX,lastX-firstX);
    }
    @Override public void tick(ClientWorld nextWorld) {
        vanilla.tick(nextWorld);
        if (world != null && world != nextWorld) clear();
        age++;
        scanBudget=SCANS_PER_TICK;
        hullsThisTick = 0;
        if (!active()) return;
        if (age - lastImpact > 260) { active = false; revision++; return; }
        Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        shift(camera);
        scanPending();
        if (age % 3 == 0) rebuildMask();
        System.arraycopy(height, 0, previous, 0, CELLS);
        // Retain forcing at the WORLD cells the hull crossed. It does not rotate into a new
        // straight wake when the boat turns. Hold briefly, then refill over several seconds.
        for (int i = 0; i < CELLS; i++) {
            float incoming = pressure[i] + impactPressure[i];
            if (Math.abs(incoming) > 0.006f) {
                float attack = Math.abs(incoming) > Math.abs(retainedPressure[i]) ? 0.42f : 0.10f;
                retainedPressure[i] += (incoming - retainedPressure[i]) * attack;
                pressureHold[i] = 10;
            } else if (pressureHold[i] > 0) pressureHold[i]--;
            else retainedPressure[i] *= 0.95f;
            pressure[i] = MathHelper.clamp(retainedPressure[i], -depthLimit[i] * weight[i], MAX_HEIGHT * weight[i]);
            impactPressure[i] *= 0.92f;
        }
        // CFL c*dt/dx ~ 0.227 with three substeps, safely below the 2D stability limit.
        // Keep extra absorption at shore/patch edges so the broader waves flatten continuously.
        float dt = 1f / 3, wave = WAVE_SPEED * WAVE_SPEED / (STEP * STEP);
        for (int sub = 0; sub < 3; sub++) {
            for (int z = 1; z < N - 1; z++) for (int x = 1; x < N - 1; x++) {
                int i = index(x, z);
                if (weight[i] == 0) { nextHeight[i] = nextVelocity[i] = 0; continue; }
                float h = height[i];
                float laplace = height[i - 1] + height[i + 1] + height[i - N] + height[i + N] - 4 * h;
                // Moving pressure has a depressed keel/trough and raised shoulders. Its Laplacian
                // sustains displacement while free water continues to propagate and refill behind it.
                float target = pressure[i];
                float pressureLaplace = pressure[i - 1] + pressure[i + 1] + pressure[i - N] + pressure[i + N] - 4 * target;
                float v = (velocity[i] + dt * (wave * (laplace - pressureLaplace) - (h - target) * RESTORING))
                        * (1 - dt * (WAVE_DAMPING + (1 - weight[i]) * 0.6f));
                v = MathHelper.clamp(v, -0.26f, 0.26f);
                float proposed = h + dt * v;
                float clipped = MathHelper.clamp(proposed, -depthLimit[i] * weight[i], MAX_HEIGHT * weight[i]);
                nextHeight[i] = clipped; nextVelocity[i] = clipped == proposed ? v : v * 0.2f;
            }
            System.arraycopy(nextHeight, 0, height, 0, CELLS);
            System.arraycopy(nextVelocity, 0, velocity, 0, CELLS);
        }
        Arrays.fill(pressure, 0);
        revision++;
    }
    @Override public void acceptHull(ClientWorld world, Vec3d stern, Vec3d bow, Vec3d travel) {
        acceptHull(world,stern,bow,travel,com.xc.longboatlab.BoatGeometry.HALF_WIDTH);
    }
    @Override public void acceptHull(ClientWorld world, Vec3d stern, Vec3d bow, Vec3d travel, double halfWidth) {
        if (!WaterSurfaceShaders.available() || hullsThisTick >= 8) return;
        Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        Vec3d axis = bow.subtract(stern);
        double length = Math.hypot(axis.x, axis.z);
        if (length < 0.25) return; // Nearly vertical hulls use swept impact events instead.
        double fx = axis.x / length, fz = axis.z / length;
        double nearest = MathHelper.clamp((camera.x - stern.x) * fx + (camera.z - stern.z) * fz, 0, length);
        Vec3d probe = stern.add(axis.multiply(nearest / length));
        if (probe.squaredDistanceTo(camera) > (28+halfWidth)*(28+halfWidth)) return;
        Surface water = vanilla.sample(world, probe);
        if (water == null || probe.y > water.position().y + 0.10 || probe.y < water.position().y - 1.2) return;
        if (!active || Math.abs(baseY - water.position().y) > 0.15) {
            if (active && age - lastImpact < 30) return;
            clear(); open(world, water.position().y, camera);
        }
        if (!covered(probe.x, probe.z)) return;
        hullsThisTick++;
        lastImpact = age;
        double speed = Math.hypot(travel.x, travel.z);
        double drive = MathHelper.clamp((speed - 0.08) / 0.85, 0, 1);
        double sign = travel.x * fx + travel.z * fz >= 0 ? 1 : -1;
        double half = length * 0.5, cx = (stern.x + bow.x) * 0.5, cz = (stern.z + bow.z) * 0.5;
        double tailLength = 1.5 + drive * 3.2;
        double depth = 0.28 + 1.38 * drive;
        double sum = 0, totalWeight = 0;
        Arrays.fill(hullPressure, 0); Arrays.fill(hullWeight, 0);
        for (int z = 1; z < N - 1; z++) for (int x = 1; x < N - 1; x++) {
            int i = index(x, z); if (weight[i] == 0) continue;
            double dx = originX + x * STEP - cx, dz = originZ + z * STEP - cz;
            // Sweep the keel across the previous tick's travel instead of stamping isolated
            // footprints at high speed. Rotation history is retained by the world-space field.
            double sweep = speed > 0.01 ? MathHelper.clamp((dx * travel.x + dz * travel.z) / (speed * speed), -1, 0) : 0;
            dx -= travel.x * sweep; dz -= travel.z * sweep;
            double along = dx * fx + dz * fz, across = dx * fz - dz * fx;
            double aft = -sign * along - half;
            double outside = Math.max(0, Math.abs(along) - half);
            if (Math.abs(across) > halfWidth+HULL_SIDE_REACH || (outside > 1.8 && (aft <= 0 || aft > tailLength * 2))) continue;
            double keelY = stern.y + axis.y * MathHelper.clamp((along + half) / length, 0, 1);
            double contact = MathHelper.clamp((baseY + 0.10 - keelY) / 0.28, 0, 1);
            if (keelY < baseY - 1.2 || contact == 0) continue;
            double body = Math.exp(-outside * outside * 2.4);
            // Low trough behind the trailing end widens downstream; its shoulders form the V wake.
            double tailFade = MathHelper.clamp((2 * tailLength - aft) / (0.6 * tailLength), 0, 1);
            tailFade = tailFade * tailFade * (3 - 2 * tailFade);
            double tail = aft > 0 ? drive * Math.exp(-aft / tailLength) * tailFade : 0;
            double width = 0.90 + Math.max(0, aft) * 0.16;
            double sideFade = MathHelper.clamp((halfWidth+HULL_SIDE_REACH - Math.abs(across)) / 1.4, 0, 1);
            sideFade = sideFade * sideFade * (3 - 2 * sideFade);
            double envelope = Math.max(body, tail) * sideFade;
            double q = Math.max(0,Math.abs(across)-Math.max(0,halfWidth-0.6875)) / width;
            double profile = -Math.exp(-q * q * 1.6)
                    + 0.36 * Math.exp(-Math.pow((Math.abs(q) - 1.8) * 1.6, 2));
            double support = envelope * Math.exp(-q * q * 0.18) * contact * weight[i];
            double value = profile * envelope * depth * contact * weight[i];
            hullPressure[i] = (float) value; hullWeight[i] = (float) support;
            sum += value; totalWeight += support;
        }
        if (totalWeight == 0) return;
        double mean = sum / totalWeight;
        for (int i = 0; i < CELLS; i++) {
            // Redistribute removed volume into the shoulders instead of draining the whole patch.
            pressure[i] = MathHelper.clamp(pressure[i] + hullPressure[i] - (float) (mean * hullWeight[i]),
                    -depthLimit[i] * weight[i], MAX_HEIGHT * weight[i]);
        }
    }

    @Override public void acceptImpact(ClientWorld world, WaterImpact impact) {
        if (!WaterSurfaceShaders.available()) return;
        Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        if (impact.position().squaredDistanceTo(camera) > 28 * 28) return;
        Surface water = vanilla.sample(world, impact.position());
        if (water == null) return;
        if (!active || Math.abs(baseY - water.position().y) > 0.15) {
            if (active && age - lastImpact < 30) return;
            clear(); open(world, water.position().y, camera);
        }
        if (!covered(impact.position().x, impact.position().z)) return;
        lastImpact = age;
        double radius = impact.kind() == WaterImpact.Kind.ENTRY ? Math.min(5.0, 1.55 + Math.sqrt(impact.area()) * 0.9) : 1.2;
        int cx = MathHelper.floor((impact.position().x - originX) / STEP), cz = MathHelper.floor((impact.position().z - originZ) / STEP);
        int r = (int) Math.ceil((radius * 1.8 + Math.min(5, impact.tangentSpeed())) / STEP);
        double mean = 0, count = 0;
        // A depressed footprint pushes a raised annulus outward. Remove net volume from the impulse.
        for (int z = Math.max(1, cz - r); z <= Math.min(N - 2, cz + r); z++)
            for (int x = Math.max(1, cx - r); x <= Math.min(N - 2, cx + r); x++) {
                int i = index(x, z); if (weight[i] == 0) continue;
                mean += kernel(x, z, impact, radius) * weight[i]; count += weight[i];
            }
        if (count == 0) return;
        mean /= count;
        double power = impact.kind() == WaterImpact.Kind.ENTRY ? Math.min(1.7, 0.30 + impact.strength() * 0.70) : 0.05 * impact.strength();
        for (int z = Math.max(1, cz - r); z <= Math.min(N - 2, cz + r); z++)
            for (int x = Math.max(1, cx - r); x <= Math.min(N - 2, cx + r); x++) {
                int i = index(x, z); if (weight[i] == 0) continue;
                float impulse = (float) ((kernel(x, z, impact, radius) - mean) * power * weight[i]);
                // Do not jump height/velocity after the previous-frame snapshot. A decaying
                // target lets the water descend, remain depressed and refill over many frames.
                impactPressure[i] = MathHelper.clamp(impactPressure[i] + impulse,
                        -depthLimit[i] * weight[i], MAX_HEIGHT * weight[i]);
            }
        revision++;
    }
    private double kernel(int x, int z, WaterImpact impact, double radius) {
        double dx = originX + x * STEP - impact.position().x, dz = originZ + z * STEP - impact.position().z;
        Vec3d velocity = impact.velocity();
        double speed = Math.hypot(velocity.x, velocity.z);
        if (speed > 0.05) {
            double along = (dx * velocity.x + dz * velocity.z) / speed;
            double across = (dx * velocity.z - dz * velocity.x) / speed;
            along -= MathHelper.clamp(along, -Math.min(5, speed), 0);
            dx = along / (1 + Math.min(0.65, speed * 0.25)); dz = across;
        }
        double q = Math.hypot(dx, dz) / radius;
        return -Math.exp(-q * q * 3.5) + 0.30 * Math.exp(-Math.pow((q - 1.1) * 3, 2));
    }
    float value(double x, double z, float delta) {
        double gx = (x - originX) / STEP, gz = (z - originZ) / STEP;
        int ix = MathHelper.clamp(MathHelper.floor(gx), 0, N - 2), iz = MathHelper.clamp(MathHelper.floor(gz), 0, N - 2);
        float tx = (float) MathHelper.clamp(gx - ix, 0, 1), tz = (float) MathHelper.clamp(gz - iz, 0, 1);
        int a = index(ix, iz);
        float h0 = MathHelper.lerp(delta, previous[a], height[a]), h1 = MathHelper.lerp(delta, previous[a + 1], height[a + 1]);
        float h2 = MathHelper.lerp(delta, previous[a + N], height[a + N]), h3 = MathHelper.lerp(delta, previous[a + N + 1], height[a + N + 1]);
        return MathHelper.lerp(tz, MathHelper.lerp(tx, h0, h1), MathHelper.lerp(tx, h2, h3));
    }
    @Override public Surface sample(ClientWorld world, Vec3d probe) {
        Surface base = vanilla.sample(world, probe);
        if (base == null || !active() || Math.abs(base.position().y - baseY) > 0.1 || !covered(probe.x, probe.z)) return base;
        float dx = value(probe.x + STEP, probe.z, 1) - value(probe.x - STEP, probe.z, 1);
        float dz = value(probe.x, probe.z + STEP, 1) - value(probe.x, probe.z - STEP, 1);
        return new Surface(new Vec3d(probe.x, baseY + value(probe.x, probe.z, 1), probe.z),
                new Vec3d(-dx, 2 * STEP, -dz).normalize(), base.flow());
    }
}
