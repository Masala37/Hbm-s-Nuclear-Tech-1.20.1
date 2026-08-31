package com.hbm.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Legacy {@code ParticleCoolingTower} — rising vapor for HUGE/ATLAS pads.
 */
@OnlyIn(Dist.CLIENT)
public class ParticleCoolingTower extends Particle {
    private float baseScale = 1.0F;
    private float maxScale = 1.0F;
    private float lift = 0.3F;
    private float strafe = 0.075F;
    private boolean windDir = true;
    private float alphaMod = 0.25F;

    private float particleScale = 1.0F;

    public ParticleCoolingTower(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.rCol = this.gCol = this.bCol = 0.9F + level.random.nextFloat() * 0.05F;
        this.hasPhysics = false;
        this.lifetime = 80;
    }

    public void setBaseScale(float f) {
        this.baseScale = f;
    }

    public void setMaxScale(float f) {
        this.maxScale = f;
    }

    public void setLift(float f) {
        this.lift = f;
    }

    public void setLife(int i) {
        this.lifetime = i;
    }

    public void setStrafe(float f) {
        this.strafe = f;
    }

    public void noWind() {
        this.windDir = false;
    }

    public void alphaMod(float mod) {
        this.alphaMod = mod;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        float ageScale = (float) this.age / (float) this.lifetime;
        this.alpha = alphaMod - ageScale * alphaMod;
        this.particleScale = baseScale + (float) Math.pow(maxScale * ageScale - baseScale, 2);
        this.age++;
        if (lift > 0 && this.yd < this.lift) {
            this.yd += 0.01F;
        }
        if (lift < 0 && this.yd > this.lift) {
            this.yd -= 0.01F;
        }
        this.xd += random.nextGaussian() * strafe * ageScale;
        this.zd += random.nextGaussian() * strafe * ageScale;
        if (windDir) {
            this.xd += 0.02 * ageScale;
            this.zd -= 0.01 * ageScale;
        }
        if (this.age >= this.lifetime) {
            remove();
            return;
        }
        move(this.xd, this.yd, this.zd);
        this.xd *= 0.925D;
        this.yd *= 0.925D;
        this.zd *= 0.925D;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cam = camera.getPosition();
        float cx = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x);
        float cy = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y);
        float cz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z);
        Quaternionf rotation = new Quaternionf(camera.rotation());
        float scale = Math.max(this.particleScale, 0.05F);
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f corner : corners) {
            corner.mul(scale);
            corner.rotate(rotation);
            corner.add(cx, cy, cz);
        }
        int light = LightTexture.FULL_BRIGHT;
        float a = Mth.clamp(this.alpha, 0.0F, 1.0F);
        buffer.vertex(corners[0].x(), corners[0].y(), corners[0].z()).uv(1.0F, 1.0F)
                .color(rCol, gCol, bCol, a).uv2(light).endVertex();
        buffer.vertex(corners[1].x(), corners[1].y(), corners[1].z()).uv(1.0F, 0.0F)
                .color(rCol, gCol, bCol, a).uv2(light).endVertex();
        buffer.vertex(corners[2].x(), corners[2].y(), corners[2].z()).uv(0.0F, 0.0F)
                .color(rCol, gCol, bCol, a).uv2(light).endVertex();
        buffer.vertex(corners[3].x(), corners[3].y(), corners[3].z()).uv(0.0F, 1.0F)
                .color(rCol, gCol, bCol, a).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleSmokePlume.RENDER_TYPE;
    }
}
