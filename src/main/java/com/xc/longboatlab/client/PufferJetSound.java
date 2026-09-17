package com.xc.longboatlab.client;

import com.xc.longboatlab.BoatAccess;
import com.xc.longboatlab.BoatGeometry;
import com.xc.longboatlab.PufferAttachments;
import com.xc.longboatlab.LongboatLab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/** One moving spatial source at the nearest puffer mouth; a seamless loop lasts exactly as long as boost. */
public final class PufferJetSound extends MovingSoundInstance {
    private final BoatEntity boat;
    private float envelope;
    private PufferAttachments.Mount lastMouth;
    public PufferJetSound(BoatEntity boat) {
        super(LongboatLab.PUFFER_JET, SoundCategory.PLAYERS, Random.create());
        this.boat = boat; repeat = true; repeatDelay = 0; volume = 0.025f; pitch = 0.86f;
        tick();
    }
    @Override public void tick() {
        var client = MinecraftClient.getInstance();
        var access = (BoatAccess) boat;
        if (boat.isRemoved() || client.world != boat.getWorld()) { setDone(); return; }
        boolean firing=access.longboat$puffers()>0 && access.longboat$visualState().getInt("Boost")>0;
        envelope+=( (firing?1:0)-envelope)*(firing?0.28f:0.22f);
        if(!firing && envelope<0.008f){setDone();return;}
        pitch+=(0.82f+envelope*0.12f-pitch)*0.15f;
        Vec3d listener = client.gameRenderer.getCamera().getPos();
        Vec3d localListener = com.xc.longboatlab.BoatBody.local(boat, listener);
        float extension = BoatMorph.extension(boat, 1);
        var mounts = firing ? PufferAttachments.near(boat, localListener, 64, extension) : java.util.List.<PufferAttachments.Mount>of();
        PufferAttachments.Mount nearest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (var mount : mounts) {
            if(!com.xc.longboatlab.PufferGrid.firing(boat,mount.face()))continue;
            double squared = PufferAttachments.mouth(boat, mount, extension).squaredDistanceTo(localListener);
            if (squared < distance) { distance = squared; nearest = mount; }
        }
        if(nearest!=null)lastMouth=nearest;
        else nearest=lastMouth;
        if(nearest==null){volume*=0.75f;return;}
        Vec3d local = PufferAttachments.mouth(boat, nearest, extension);
        Vec3d at = com.xc.longboatlab.BoatBody.world(boat, local);
        x = at.x; y = at.y; z = at.z;
        int active=0; int[] counts=com.xc.longboatlab.PufferGrid.counts(boat);
        for(int f=0;f<counts.length;f++)if(com.xc.longboatlab.PufferGrid.firing(boat,f))active+=counts[f];
        float target=(float)Math.min(0.85,0.36+Math.log1p(Math.max(1,active))*0.075);
        volume+=(target*envelope-volume)*0.32f;
    }
}
