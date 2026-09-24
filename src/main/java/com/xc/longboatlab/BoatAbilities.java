package com.xc.longboatlab;

import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.math.Box;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import static com.xc.longboatlab.BoatControlPayload.*;

/** Transient abilities live on the server. Persistent upgrades live in BoatRig. */
public final class BoatAbilities {
    public static final int IDLE = 0, FLYING = 1, LOCKED = 2, RETURNING = 3;
    private final BoatBody body = new BoatBody();
    public BoatBody body() { return body; }
    private final GiantOarMotor giantMotor = new GiantOarMotor();
    public static final int MORPH_DURATION = 2;
    private static final int SPRING_RELEASE_TICKS = 5;
    private static final int SPRING_RETURN_TICKS = 19;
    public static final int JET_CAPACITY = 100;
    private int jetCharge = JET_CAPACITY;
    private final double[] faceCharge={1,1,1,1,1};
    private final boolean[] faceExhausted=new boolean[5];
    private final double[] faceCapacity=new double[5];
    private int jetFaces;
    public int jetFaces(){return jetFaces;}
    private int morphTicks;
    public boolean transforming() { return morphTicks > 0; }
    public NbtCompound saveJoints() { return giantMotor.save(); }
    public void loadJoints(NbtCompound tag) { giantMotor.load(tag); }
    public double giantPhase(boolean left) { return giantMotor.phase(left); }
    public double giantStepHeight(BoatEntity boat) { return giantMotor.stepHeight(boat); }
    public void giantInstalled(BoatEntity boat, boolean wasCompressed) {
        stopSpring();
        retract(); morphTicks = wasCompressed ? MORPH_DURATION : 0;
        ((BoatAccess) boat).longboat$syncAbilities();
    }
    private int held, pendingActions;
    private long lastInput = -100;
    private UUID driver;
    private int hookState;
    private Vec3d hook = Vec3d.ZERO, launchDirection = Vec3d.ZERO;
    private BlockPos anchorBlock;
    private BlockState anchorState;
    private Entity anchorEntity;
    private Vec3d anchorOffset = Vec3d.ZERO, anchorMotion = Vec3d.ZERO;
    private double cableLength, maxCableLength;
    private int springTick = -1, jumpCooldown, modeCooldown, boostTicks;
    private float spring;
    private boolean fallProtected;
    private int protectionTicks;
    private float turnVelocity;
    private double springEnergy, pendingSpringEnergy;
    private boolean springLaunched;
    private boolean springFlight;
    private int landingTicks;
    private Vec3d orbitRadial = Vec3d.ZERO, orbitEast = new Vec3d(1, 0, 0);
    private Vec3d spherePreviousPosition, sphereDestination;
    private Vec3d sphereOriginOffset = Vec3d.ZERO;
    private double sphereProposedLength;

    public void accept(BoatEntity boat, PlayerEntity player, BoatControlPayload packet) {
        long now = boat.getWorld().getTime();
        if (!player.getUuid().equals(driver)) {
            held = pendingActions = 0;
            driver = player.getUuid();
        }
        held = packet.held() & 16383;
        lastInput = now;
        // Coalesce clicks until the next simulation tick, including packets arriving in the same tick.
        pendingActions |= packet.actions() & 7;
    }

    public boolean protectsFall() { return fallProtected || hookState == LOCKED; }
    public boolean locked() { return hookState == LOCKED; }
    public boolean springFlight() { return springFlight; }
    public void restoreSpringFlight(boolean active) { springFlight = active; landingTicks = 0; }

    public void resetForRescue(BoatEntity boat) {
        stopSpring();
        springFlight = false;
        landingTicks = 0;
        held = pendingActions = jetFaces = boostTicks = morphTicks = 0;
        turnVelocity = 0;
        hookState = IDLE;
        anchorBlock = null; anchorState = null; anchorEntity = null;
        hook = launchDirection = anchorOffset = anchorMotion = Vec3d.ZERO;
        spherePreviousPosition = sphereDestination = null;
        cableLength = maxCableLength = 0;
        body.resetForRescue();
        giantMotor.load(giantMotor.save());
        fallProtected = true; protectionTicks = 0;
        boat.setVelocity(Vec3d.ZERO);
        boat.fallDistance = 0;
        BoatCollisionScene.invalidate(boat);
        ((BoatAccess) boat).longboat$syncAbilities();
    }

    public void tick(BoatEntity boat) {
        BoatCollisionScene.invalidate(boat);
        BoatAccess access = (BoatAccess) boat;
        if (!(boat.getControllingPassenger() instanceof PlayerEntity player)
                || !player.isAlive() || player.isSpectator() || !player.getUuid().equals(driver)
                || boat.getWorld().getTime() - lastInput > 10) {
            held = pendingActions = 0;
            if (hookState == FLYING) retract();
        }
        if (morphTicks > 0) morphTicks--;
        if (modeCooldown > 0) modeCooldown--;
        if (jumpCooldown > 0) jumpCooldown--;
        if (springFlight) {
            boolean landed = springTick < 0 && boat.getVelocity().y <= 0.08
                    && (boat.isOnGround() || BoatBody.jumpSupport(boat) > 0.12);
            landingTicks = landed ? landingTicks + 1 : 0;
            if (landingTicks >= 4) springFlight = false;
        }

        int actions = pendingActions;
        pendingActions = 0;
        PlayerEntity firePlayer = null;
        if (boat.getControllingPassenger() instanceof PlayerEntity player && player.getUuid().equals(driver)) {
            if ((actions & TOGGLE) != 0 && modeCooldown == 0) toggle(boat, player);
            if ((actions & JUMP) != 0) {
                if (hookState != IDLE) retract();
                else jump(boat, player);
            }
            // A jump and a fresh shot can share a tick; an existing hook must finish returning first.
            if ((actions & HOOK) != 0) {
                if (hookState == IDLE) firePlayer = player;
                else retract();
            }
        }
        int[] counts=PufferGrid.counts(boat); jetFaces=0; double chargeSum=0,capacitySum=0;
        for(int face=0;face<5;face++) {
            // Store a fill fraction: changing equipment cannot create free air in an empty reservoir.
            double capacity=JET_CAPACITY+40.0*Math.max(0,counts[face]-1); faceCapacity[face]=capacity;
            boolean wants=(down(JET_HELD)||down(JET_BOTTOM<<face)) && counts[face]>0;
            if(!wants){faceExhausted[face]=false;faceCharge[face]=Math.min(1,faceCharge[face]+1/capacity);}
            else if(!faceExhausted[face] && faceCharge[face]>0) {
                jetFaces|=1<<face; faceCharge[face]=Math.max(0,faceCharge[face]-2/capacity);
                if(faceCharge[face]==0)faceExhausted[face]=true;
            }
            if(counts[face]>0){chargeSum+=faceCharge[face];capacitySum++;}
        }
        boostTicks=jetFaces==0?0:1;
        jetCharge=(int)Math.round(JET_CAPACITY*chargeSum/Math.max(1,capacitySum));
        float previousSpring = spring;
        pendingSpringEnergy = 0;
        if (springTick >= 0) {
            springTick++;
            spring = springTick <= SPRING_RELEASE_TICKS ? springTick / (float) SPRING_RELEASE_TICKS
                    : Math.max(0, 1 - (springTick - SPRING_RELEASE_TICKS) / (float) SPRING_RETURN_TICKS);
            pendingSpringEnergy = springEnergy * Math.max(0, spring - previousSpring);
            if (springTick >= SPRING_RELEASE_TICKS + SPRING_RETURN_TICKS) stopSpring();
        }
        if (fallProtected) {
            boat.fallDistance = 0;
            if (++protectionTicks > 400 || (protectionTicks > 8 && (boat.isOnGround() || boat.isTouchingWater()))) {
                fallProtected = false;
            }
        }
        access.longboat$syncAbilities();
        if (spring > previousSpring && !boat.getWorld().isSpaceEmpty(boat,
                BoatGeometry.bounds(boat).stretch(0, boat.getControllingPassenger() == null ? 0 : boat.getControllingPassenger().getHeight(), 0))) {
            // A ceiling encountered during flight must also stop expansion, not only the initial launch.
            spring = previousSpring;
            springTick = SPRING_RELEASE_TICKS + (int) Math.ceil((1 - spring) * SPRING_RETURN_TICKS);
            cancelSpringThrust();
            boat.setVelocity(boat.getVelocity().x, Math.min(0, boat.getVelocity().y), boat.getVelocity().z);
            access.longboat$syncAbilities();
        }
        // Use the current spring/morph state for the launch point and the lock constraint.
        if (firePlayer != null) fire(boat, firePlayer);
        tickHook(boat);
        access.longboat$syncAbilities();
        boat.setBoundingBox(BoatGeometry.bounds(boat));
    }

    private void toggle(BoatEntity boat, PlayerEntity player) {
        BoatAccess access = (BoatAccess) boat;
        if (transforming()) return;
        boolean interruptingSpring = springTick >= 0;
        if (access.longboat$rig().hasGiants()) {
            BoatNoticePayload.send(player, Text.translatable("message.longboatlab.giant_fold")); return;
        }
        BoatRig previous = access.longboat$rig();
        var seats=access.longboat$seatData().copy();
        if(!previous.compressed() && !BoatSeats.prepareFold(boat)) {
            BoatNoticePayload.send(player,Text.translatable("message.longboatlab.fold_seats"));return;
        }
        access.longboat$setRig(previous.withMode(!previous.compressed()));
        // Avoid expanding a boat through blocks, passengers or adjacent vehicles.
        if (!BoatGeometry.spaceEmpty(boat, boat.getYaw())) {
            access.longboat$setRig(previous);
            access.longboat$setSeatData(seats);
            BoatNoticePayload.send(player, Text.translatable("message.longboatlab.no_space"));
            modeCooldown = 10;
            return;
        }
        stopSpring();
        retract();
        turnVelocity = 0;
        modeCooldown = MORPH_DURATION;
        morphTicks = interruptingSpring ? 0 : MORPH_DURATION;
        access.longboat$syncAbilities();
        boat.setBoundingBox(BoatGeometry.bounds(boat));
        BoatNoticePayload.send(player, Text.translatable(access.longboat$compressed()
                ? "message.longboatlab.compressed" : "message.longboatlab.extended"));
    }

    private void jump(BoatEntity boat, PlayerEntity player) {
        BoatAccess access = (BoatAccess) boat;
        if (transforming() || !access.longboat$compressed() || jumpCooldown > 0 || springFlight
                || (!boat.isOnGround() && (boat.getVelocity().y > 0.08 || BoatBody.jumpSupport(boat) <= 0.12))) return;
        double extra = (access.longboat$segments() - 1) * 0.48;
        if (!boat.getWorld().isSpaceEmpty(boat, boat.getBoundingBox().stretch(0, extra + player.getHeight(), 0))) {
            BoatNoticePayload.send(player, Text.translatable("message.longboatlab.no_space"));
            return;
        }
        springTick = 0;
        spring = 0;
        springLaunched = false;
        springFlight = true;
        landingTicks = 0;
        jumpCooldown = 35;
        fallProtected = true;
        protectionTicks = 0;
        // Release a fixed energy budget as the stack unfolds, rather than granting full height at takeoff.
        springEnergy = BoatBody.GRAVITY * (1.5 + access.longboat$segments() * 1.4);
        pendingSpringEnergy = 0;
        boat.setOnGround(false);
        boat.getWorld().playSound(null, boat.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_FALL,
                SoundCategory.PLAYERS, 1, 0.6f);
    }

    private void cancelSpringThrust() {
        springEnergy = pendingSpringEnergy = 0;
    }

    private void stopSpring() {
        springTick = -1;
        spring = 0;
        springLaunched = false;
        cancelSpringThrust();
    }

    public static double hookRange(BoatAccess access) {
        long oars = (long) access.longboat$oars(true) + access.longboat$oars(false);
        return oars == 0 ? 0 : Math.min(192, 6 + oars * 2.5);
    }

    public static Vec3d cableOrigin(BoatEntity boat) {
        double half = 1 + (((BoatAccess) boat).longboat$segments() - 1) * (double) BoatGeometry.extension(boat);
        return BoatBody.world(boat, new Vec3d(0,
                BoatGeometry.HEIGHT + BoatGeometry.stackHeight(boat) + 0.08, half - 0.12));
    }

    private void fire(BoatEntity boat, PlayerEntity player) {
        BoatAccess access = (BoatAccess) boat;
        if (!access.longboat$compressed() || hookState != IDLE) return;
        maxCableLength = hookRange(access);
        if (maxCableLength == 0) {
            BoatNoticePayload.send(player, Text.translatable("message.longboatlab.hook_needs_oars"));
            return;
        }
        hook = cableOrigin(boat);
        // Aim at the point on the player's sight line, with the projectile starting on the boat.
        Vec3d eye = player.getEyePos();
        Vec3d sightDirection = player.getRotationVec(1);
        Vec3d sightEnd = eye;
        for (double distance = 0.5; distance <= maxCableLength; distance += 0.5) {
            Vec3d point = eye.add(sightDirection.multiply(distance));
            if (!com.xc.longboatlab.BoatEnvironment.isLoaded(boat.getWorld(), BlockPos.ofFloored(point))) break;
            sightEnd = point;
        }
        if (sightEnd.squaredDistanceTo(eye) < 0.1) return;
        BlockHitResult sight = boat.getWorld().raycast(new RaycastContext(eye, sightEnd,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, boat));
        Vec3d aim = sight.getPos();
        var entityAim = ProjectileUtil.getEntityCollision(boat.getWorld(), boat, eye, aim, new Box(eye, aim).expand(1),
                e -> hookTarget(boat, e));
        if (entityAim != null) aim = entityAim.getPos();
        launchDirection = aim.subtract(hook).normalize();
        hookState = FLYING;
        cableLength = 0;
        boat.getWorld().playSound(null, boat.getBlockPos(), SoundEvents.ENTITY_FISHING_BOBBER_THROW,
                SoundCategory.PLAYERS, 1, 0.7f);
    }

    private void retract() {
        if (hookState != IDLE) hookState = RETURNING;
        anchorBlock = null;
        anchorState = null;
        anchorEntity = null;
        anchorMotion = Vec3d.ZERO;
        spherePreviousPosition = sphereDestination = null;
    }

    private void tickHook(BoatEntity boat) {
        if (hookState == IDLE) return;
        BoatAccess access = (BoatAccess) boat;
        Vec3d origin = cableOrigin(boat);
        if (!access.longboat$compressed() || hookRange(access) < maxCableLength) retract();
        if (hookState == FLYING) {
            double remaining = maxCableLength - hook.distanceTo(origin);
            if (remaining <= 0) { retract(); return; }
            Vec3d next = hook.add(launchDirection.multiply(Math.min(4.5, remaining)));
            if (!com.xc.longboatlab.BoatEnvironment.isLoaded(boat.getWorld(), BlockPos.ofFloored(next))) { retract(); return; }
            BlockHitResult hit = boat.getWorld().raycast(new RaycastContext(hook, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, boat));
            var entityHit = ProjectileUtil.getEntityCollision(boat.getWorld(), boat, hook, hit.getPos(),
                    new Box(hook, next).expand(1), e -> hookTarget(boat, e));
            if (entityHit != null) {
                hook = entityHit.getPos();
                anchorEntity = entityHit.getEntity();
                anchorOffset = BoatGeometry.rotateY(hook.subtract(anchorEntity.getPos()), -Math.toRadians(anchorEntity.getYaw()));
                anchorBlock = null;
                beginLock(boat, origin);
                return;
            }
            hook = next;
            if (hit.getType() == HitResult.Type.BLOCK) {
                Vec3d normal = Vec3d.of(hit.getSide().getVector());
                hook = hit.getPos().add(normal.multiply(0.035));
                anchorBlock = hit.getBlockPos().toImmutable();
                anchorState = boat.getWorld().getBlockState(anchorBlock);
                beginLock(boat, origin);
            } else if (hook.distanceTo(origin) >= maxCableLength - 0.01) retract();
        } else if (hookState == LOCKED) {
            anchorMotion = Vec3d.ZERO;
            if (anchorEntity != null) {
                if (anchorEntity.isRemoved() || !anchorEntity.isAlive() || anchorEntity.getWorld() != boat.getWorld()
                        || anchorEntity.getRootVehicle() == boat.getRootVehicle()) { retract(); return; }
                Vec3d next = anchorEntity.getPos().add(BoatGeometry.rotateY(anchorOffset, Math.toRadians(anchorEntity.getYaw())));
                anchorMotion = next.subtract(hook);
                hook = next;
            } else if (anchorBlock == null || !com.xc.longboatlab.BoatEnvironment.isLoaded(boat.getWorld(), anchorBlock)
                    || !boat.getWorld().getBlockState(anchorBlock).equals(anchorState)) { retract(); return; }
            Vec3d previousOrigin = boat.getPos().add(sphereOriginOffset);
            BlockHitResult obstruction = boat.getWorld().raycast(new RaycastContext(previousOrigin.add(anchorMotion), hook,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, boat));
            if (obstruction.getType() == HitResult.Type.BLOCK && !obstruction.getBlockPos().equals(anchorBlock)) retract();
        } else {
            Vec3d delta = origin.subtract(hook);
            double returnSpeed = 5.5 + boat.getVelocity().length();
            if (delta.length() <= returnSpeed) { hookState = IDLE; cableLength = 0; }
            else hook = hook.add(delta.normalize().multiply(returnSpeed));
        }
    }

    private boolean hookTarget(BoatEntity boat, Entity entity) {
        return !entity.isRemoved() && !entity.isSpectator() && entity != boat
                && entity.getRootVehicle() != boat.getRootVehicle()
                && (driver == null || !driver.equals(entity.getUuid()));
    }

    private void beginLock(BoatEntity boat, Vec3d origin) {
        cableLength = origin.distanceTo(hook);
        if (cableLength < 2 || cableLength > maxCableLength) { retract(); return; }
        hookState = LOCKED;
        cancelSpringThrust();
        anchorMotion = Vec3d.ZERO;
        sphereOriginOffset = origin.subtract(boat.getPos());
        orbitRadial = origin.subtract(hook).normalize();
        float yaw = boat.getControllingPassenger() == null ? boat.getYaw() : boat.getControllingPassenger().getYaw();
        orbitEast = tangentAxis(BoatGeometry.rotateY(new Vec3d(-1, 0, 0), Math.toRadians(yaw)), orbitRadial);
        boat.setVelocity(Vec3d.ZERO);
        boat.getWorld().playSound(null, BlockPos.ofFloored(hook), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.PLAYERS, 1, 0.8f);
    }

    /** Called after vanilla buoyancy, before the server moves the hull through block collisions. */
    public void drive(BoatEntity boat, float decayedYawVelocity) {
        BoatAccess access = (BoatAccess) boat;
        if (pendingSpringEnergy > 0) {
            Vec3d velocity = boat.getVelocity();
            if (springLaunched && velocity.y <= 0) {
                // A blocked/reversed ascent consumes the remaining stroke instead of repeatedly relaunching.
                cancelSpringThrust();
            } else {
                double upward = springLaunched ? velocity.y : 0;
                boat.setVelocity(velocity.x, Math.sqrt(upward * upward + 2 * pendingSpringEnergy), velocity.z);
                springLaunched = true;
                pendingSpringEnergy = 0;
            }
        }
        body.step(boat, jetFaces, hookState == LOCKED);
        if (hookState == LOCKED) {
            turnVelocity = 0;
            driveOnSphere(boat);
            boat.setPaddleMovings(false, false);
            boat.fallDistance = 0;
            return;
        }
        if (!(boat.getControllingPassenger() instanceof PlayerEntity)) {
            turnVelocity = decayedYawVelocity + body.takeYawImpulse();
            float yaw=boat.getYaw();
            if(Math.abs(turnVelocity)>0.0001f) {
                double radius=Math.max(Math.hypot(BoatGeometry.halfLength(boat),BoatGeometry.halfWidth(boat)),GiantOars.mounted(boat).stream().mapToDouble(m->m.scale()*1.8+BoatGeometry.halfWidth(boat)).max().orElse(1));
                turnVelocity=MathHelper.clamp(turnVelocity,(float)-Math.toDegrees(0.2/radius),(float)Math.toDegrees(0.2/radius));
                if(BoatGeometry.spaceEmpty(boat,yaw+turnVelocity))boat.setYaw(yaw+turnVelocity);else turnVelocity=0;
            }
            boat.setPaddleMovings(false, false);
            return;
        }
        double throttle = (down(FORWARD) ? 1 : 0) - (down(BACK) ? 0.125 : 0);
        float physicalYaw=body.takeYawImpulse();
        int steering = (down(RIGHT) ? 1 : 0) - (down(LEFT) ? 1 : 0);
        int left = access.longboat$oars(true), right = access.longboat$oars(false);
        long total = access.longboat$compressed() ? Math.min(1, left) + Math.min(1, right) : (long) left + right;
        boat.setPaddleMovings(left > 0 && (throttle != 0 || steering > 0),
                right > 0 && (throttle != 0 || steering < 0));
        // Equal counts use vanilla angular acceleration and environment-dependent damping.
        // Only the ratio imbalance adds yaw; installing more balanced pairs never multiplies steering.
        double imbalance = total == 0 ? 0 : (left - right) / ((double) left + right);
        double bias = imbalance * throttle * 1.5;
        turnVelocity = (total == 0 ? decayedYawVelocity : decayedYawVelocity + steering + (float) bias) + physicalYaw;
        if (left != right) turnVelocity = MathHelper.clamp(turnVelocity, -16, 16);
        float originalYaw = boat.getYaw();
        double rotationRadius = Math.hypot(BoatGeometry.halfLength(boat),BoatGeometry.halfWidth(boat));
        if (!GiantOars.mounted(boat).isEmpty()) {
            rotationRadius = Math.max(rotationRadius, GiantOars.mounted(boat).stream()
                    .mapToDouble(m -> m.scale() * 1.75+BoatGeometry.halfWidth(boat)).max().orElse(1));
            float maxTurn = (float) Math.toDegrees(0.25 / rotationRadius);
            turnVelocity = MathHelper.clamp(turnVelocity, -maxTurn, maxTurn);
        }
        int rotationSteps = Math.max(1, (int) Math.min(64, Math.ceil(Math.abs(turnVelocity) * rotationRadius / 8)));
        boolean rotationClear = true;
        for (int step = 1; Math.abs(turnVelocity) > 0.0001f && step <= rotationSteps; step++) {
            if (!BoatGeometry.spaceEmpty(boat, originalYaw + turnVelocity * step / rotationSteps)) {
                rotationClear = false;
                break;
            }
        }
        if (rotationClear && Math.abs(turnVelocity) > 0.0001f) boat.setYaw(originalYaw + turnVelocity);
        else turnVelocity = 0;
        boolean spin = total > 0 && throttle != 0 && (left == 0 || right == 0
                || Math.max(left, right) >= Math.max(1, Math.min(left, right)) * 4L);
        Vec3d v = boat.getVelocity();
        if (spin && boostTicks == 0) boat.setVelocity(v.x * 0.65, v.y, v.z * 0.65);
        else {
            // Start at vanilla's 0.04 forward / 0.005 reverse, with diminishing gains per added pair.
            double effectiveOars = total;
            if (!GiantOars.mounted(boat).isEmpty()) effectiveOars = GiantOars.mounted(boat).stream()
                    .mapToDouble(m -> m.scale() * m.scale() * 2).sum();
            double power = Math.sqrt(effectiveOars / 2.0);
            double thrust = throttle * 0.04 * power;
            if (steering != 0 && !down(FORWARD) && !down(BACK)) thrust += 0.005 * power;
            int bottomJets = (jetFaces & 1) != 0 ? PufferGrid.counts(boat)[0] : 0;
            // Leaving a wave, ramp or spring jump must never turn ordinary rowing into upward thrust.
            Vec3d forward = body.rowingDirection(boat);
            if (!GiantOars.mounted(boat).isEmpty() && !boat.isTouchingWater()) thrust = 0;
            v = v.add(forward.multiply(thrust));
            if (boostTicks > 0 && bottomJets > 0) {
                fallProtected = true;
                protectionTicks = 0;
            }
            boat.setVelocity(v);
        }
        if (!transforming()) giantMotor.drive(boat, down(FORWARD), down(BACK), steering, body.horizontalHeading(boat));
    }

    private void driveOnSphere(BoatEntity boat) {
        Vec3d origin = cableOrigin(boat);
        if (orbitRadial.lengthSquared() < 0.5) { retract(); return; }
        Vec3d expectedOrigin = hook.subtract(anchorMotion).add(orbitRadial.multiply(cableLength));
        // Validate hull translation in the previous attachment frame. Spring/morph motion
        // changes the bow offset legitimately; the constraint compensates that offset below.
        if (boat.getPos().add(sphereOriginOffset).squaredDistanceTo(expectedOrigin) > 1.0e-6) { retract(); return; }
        boolean attachmentMoved = origin.subtract(boat.getPos()).squaredDistanceTo(sphereOriginOffset) > 1.0e-8;
        double reel = (down(BACK) ? 1 : 0) - (down(FORWARD) ? 1 : 0);
        double proposedLength = MathHelper.clamp(cableLength + reel * (boostTicks > 0 ? 1.5 : 0.8), 2, maxCableLength);
        Vec3d east = tangentAxis(orbitEast, orbitRadial);
        Vec3d north = orbitRadial.crossProduct(east).normalize();
        int horizontal = (down(ORBIT_RIGHT) ? 1 : 0) - (down(ORBIT_LEFT) ? 1 : 0);
        int vertical = (down(UP) ? 1 : 0) - (down(DOWN) ? 1 : 0);
        Vec3d tangent = east.multiply(horizontal).add(north.multiply(vertical));
        double speed = boostTicks > 0 ? 1.8 : 0.9;
        double angle = tangent.lengthSquared() < 1e-9 ? 0 : speed / proposedLength;
        Vec3d radial = orbitRadial.multiply(Math.cos(angle)).add(tangent.normalize().multiply(Math.sin(angle))).normalize();
        Vec3d destination = hook.add(radial.multiply(proposedLength));
        Vec3d motion = destination.subtract(origin);
        boolean clear = com.xc.longboatlab.BoatEnvironment.isLoaded(boat.getWorld(), BlockPos.ofFloored(destination));
        if (clear) {
            BlockHitResult obstruction = boat.getWorld().raycast(new RaycastContext(destination, hook,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, boat));
            clear = BoatGeometry.clipMovement(boat, motion).squaredDistanceTo(motion) <= 1.0e-10
                    && (obstruction.getType() != HitResult.Type.BLOCK || obstruction.getBlockPos().equals(anchorBlock));
        }
        if (!clear) {
            // A blocked step preserves the last valid point AND radius; do not slide into the sphere.
            if (anchorMotion.lengthSquared() > 1.0e-8 || attachmentMoved) {
                // Moving anchors and attachment animation cannot push through walls or stretch the link.
                retract();
                boat.setVelocity(Vec3d.ZERO);
                return;
            }
            motion = Vec3d.ZERO;
            proposedLength = cableLength;
        }
        spherePreviousPosition = boat.getPos();
        sphereDestination = spherePreviousPosition.add(motion);
        sphereProposedLength = proposedLength;
        boat.setVelocity(motion);
    }

    private static Vec3d tangentAxis(Vec3d preferred, Vec3d normal) {
        Vec3d tangent = preferred.subtract(normal.multiply(preferred.dotProduct(normal)));
        if (tangent.lengthSquared() < 1.0e-8) {
            Vec3d fallback = Math.abs(normal.y) < 0.9 ? new Vec3d(0, 1, 0) : new Vec3d(0, 0, 1);
            tangent = fallback.crossProduct(normal);
        }
        return tangent.normalize();
    }

    public float turnVelocity() { return turnVelocity; }

    public void afterMove(BoatEntity boat) {
        giantMotor.afterMove(boat);
        if (springLaunched && boat.verticalCollision && boat.getVelocity().y <= 0) cancelSpringThrust();
        ((BoatAccess) boat).longboat$syncAbilities();
        if (!GiantOars.mounted(boat).isEmpty()) {
            LoadedBoats.update(boat);
        }
        if (hookState != LOCKED || sphereDestination == null) return;
        if (boat.getPos().squaredDistanceTo(sphereDestination) <= 1.0e-10) {
            cableLength = sphereProposedLength;
            Vec3d nextRadial = cableOrigin(boat).subtract(hook).normalize();
            // Parallel-transport the controls with the orbit, including passages across either pole.
            Vec3d axis = orbitRadial.crossProduct(nextRadial);
            double cosine = MathHelper.clamp(orbitRadial.dotProduct(nextRadial), -1, 1);
            Vec3d transported = orbitEast.add(axis.crossProduct(orbitEast))
                    .add(axis.crossProduct(axis.crossProduct(orbitEast)).multiply(1 / (1 + cosine)));
            orbitEast = tangentAxis(transported, nextRadial);
            orbitRadial = nextRadial;
            sphereOriginOffset = cableOrigin(boat).subtract(boat.getPos());
        } else {
            // Slime, bubble columns or another collision modifier can change move() after preflight.
            // Roll back to the last valid pose rather than converting that displacement to a new radius.
            Vec3d rollback = spherePreviousPosition.subtract(boat.getPos());
            boolean clear = rollback.lengthSquared() <= 2;
            for (var part : BoatGeometry.collisionParts(boat, boat.getYaw())) {
                if (!boat.getWorld().isSpaceEmpty(boat, part.offset(rollback))) { clear = false; break; }
            }
            if (clear && cableOrigin(boat).subtract(boat.getPos()).squaredDistanceTo(sphereOriginOffset) <= 1.0e-8)
                boat.setPosition(spherePreviousPosition);
            else retract();
        }
        boat.setVelocity(Vec3d.ZERO);
        spherePreviousPosition = sphereDestination = null;
        ((BoatAccess) boat).longboat$syncAbilities();
    }

    private boolean down(int bit) { return (held & bit) != 0; }

    public NbtCompound visualState() {
        NbtCompound tag = new NbtCompound();
        tag.putDouble("TiltPitch", body.pitch());
        tag.putDouble("TiltRoll", body.roll());
        tag.putInt("MorphTicks", morphTicks);
        tag.putDouble("GiantLeftPhase", giantMotor.phase(true));
        tag.putDouble("GiantRightPhase", giantMotor.phase(false));
        tag.putInt("HookState", hookState);
        tag.putInt("AnchorEntity", anchorEntity == null ? -1 : anchorEntity.getId());
        tag.putDouble("AnchorOffsetX", anchorOffset.x); tag.putDouble("AnchorOffsetY", anchorOffset.y); tag.putDouble("AnchorOffsetZ", anchorOffset.z);
        tag.putDouble("HookX", hook.x); tag.putDouble("HookY", hook.y); tag.putDouble("HookZ", hook.z);
        tag.putFloat("Spring", spring);
        tag.putInt("Boost", boostTicks);
        tag.putInt("JetFaces",jetFaces);
        for(int f=0;f<5;f++){tag.putDouble("JetFill"+f,faceCharge[f]);tag.putDouble("JetSeconds"+f,faceCapacity[f]/40);}

        tag.putInt("JetCharge", jetCharge);
        tag.putInt("JetCapacity", JET_CAPACITY);
        tag.putInt("JumpCooldown", jumpCooldown);
        tag.putDouble("CableLength", cableLength);
        tag.putDouble("CableMax", maxCableLength);
        return tag;
    }
}
