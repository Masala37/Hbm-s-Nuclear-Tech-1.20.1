package com.hbm.entity.effect;

import java.awt.Color;
import java.util.ArrayList;

import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/**
 * Toroidal Convection Simulation Explosion Effect (Torex).
 * Client-side cloudlet mushroom cloud matching legacy 1.7.10 visuals.
 */
public class EntityNukeTorex extends Entity {
    private static final EntityDataAccessor<Float> DATA_SCALE =
            SynchedEntityData.defineId(EntityNukeTorex.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(EntityNukeTorex.class, EntityDataSerializers.INT);

    public double coreHeight = 3;
    public double convectionHeight = 3;
    public double torusWidth = 3;
    public double rollerSize = 1;
    public double heat = 1;
    public double lastSpawnY = -1;
    public final ArrayList<Cloudlet> cloudlets = new ArrayList<>();

    public boolean didPlaySound = false;
    public boolean didShake = false;

    private boolean killOnLoad;

    public EntityNukeTorex(EntityType<? extends EntityNukeTorex> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EntityNukeTorex(Level level) {
        this(ModEntities.NUKE_TOREX.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_SCALE, 1.5F);
        entityData.define(DATA_TYPE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (killOnLoad) {
            discard();
            return;
        }

        double s = 1.5D;
        double cs = 1.5D;
        int maxAge = this.getMaxAge();

        if (level().isClientSide) {
            if (tickCount == 1) {
                this.setScale((float) s);
            }

            if (lastSpawnY == -1) {
                lastSpawnY = getY() - 3;
            }

            int spawnTarget = Math.max(
                    level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(getX()), Mth.floor(getZ())) - 3,
                    1);
            double moveSpeed = 0.5D;

            if (Math.abs(spawnTarget - lastSpawnY) < moveSpeed) {
                lastSpawnY = spawnTarget;
            } else {
                lastSpawnY += moveSpeed * Math.signum(spawnTarget - lastSpawnY);
            }

            // spawn mush clouds
            double range = (torusWidth - rollerSize) * 0.25;
            double simSpeed = getSimulationSpeed();
            int toSpawn = (int) Math.ceil(10 * simSpeed * simSpeed);
            int lifetime = Math.min((tickCount * tickCount) + 200, maxAge - tickCount + 200);

            for (int i = 0; i < toSpawn; i++) {
                double x = getX() + random.nextGaussian() * range;
                double z = getZ() + random.nextGaussian() * range;
                Cloudlet cloud = new Cloudlet(x, lastSpawnY, z, (float) (random.nextDouble() * 2D * Math.PI), 0, lifetime);
                cloud.setScale(1F + this.tickCount * 0.005F * (float) cs, 5F * (float) cs);
                cloudlets.add(cloud);
            }

            // spawn shock clouds
            if (tickCount < 150) {
                int cloudCount = tickCount * 5;
                int shockLife = Math.max(300 - tickCount * 20, 50);

                for (int i = 0; i < cloudCount; i++) {
                    Vec3 vec = new Vec3((tickCount * 1.5 + random.nextDouble()) * 1.5, 0, 0);
                    float rot = (float) (Math.PI * 2 * random.nextDouble());
                    vec = vec.yRot(rot);
                    int hx = Mth.floor(vec.x + getX()) + 1;
                    int hz = Mth.floor(vec.z + getZ());
                    double hy = level().getHeight(Heightmap.Types.MOTION_BLOCKING, hx, hz);
                    this.cloudlets.add(new Cloudlet(vec.x + getX(), hy, vec.z + getZ(), rot, 0, shockLife, TorexType.SHOCK)
                            .setScale(7F, 2F)
                            .setMotion(tickCount > 15 ? 0.75 : 0));
                }

                if (!didPlaySound) {
                    double hearRange = (tickCount * 1.5 + 1) * 1.5;
                    Player player = level().getNearestPlayer(this, hearRange);
                    if (player != null) {
                        level().playLocalSound(getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE,
                                SoundSource.BLOCKS, 10000F, 1F, false);
                        didPlaySound = true;
                    }
                }
            }

            // spawn ring clouds
            if (tickCount < 130 * s) {
                lifetime = (int) (lifetime * s);
                for (int i = 0; i < 2; i++) {
                    Cloudlet cloud = new Cloudlet(getX(), getY() + coreHeight, getZ(),
                            (float) (random.nextDouble() * 2D * Math.PI), 0, lifetime, TorexType.RING);
                    cloud.setScale(1F + this.tickCount * 0.0025F * (float) (cs * cs), 3F * (float) (cs * cs));
                    cloudlets.add(cloud);
                }
            }

            // spawn condensation clouds
            if (tickCount > 130 * s && tickCount < 600 * s) {
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 4; j++) {
                        float angle = (float) (Math.PI * 2 * random.nextDouble());
                        Vec3 vec = new Vec3(torusWidth + rollerSize * (5 + random.nextDouble()), 0, 0);
                        vec = vec.zRot((float) (Math.PI / 45 * j));
                        vec = vec.yRot(angle);
                        Cloudlet cloud = new Cloudlet(getX() + vec.x, getY() + coreHeight - 5 + j * s, getZ() + vec.z,
                                angle, 0, (int) ((20 + tickCount / 10) * (1 + random.nextDouble() * 0.1)),
                                TorexType.CONDENSATION);
                        cloud.setScale(0.125F * (float) cs, 3F * (float) cs);
                        cloudlets.add(cloud);
                    }
                }
            }
            if (tickCount > 200 * s && tickCount < 600 * s) {
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 4; j++) {
                        float angle = (float) (Math.PI * 2 * random.nextDouble());
                        Vec3 vec = new Vec3(torusWidth + rollerSize * (3 + random.nextDouble() * 0.5), 0, 0);
                        vec = vec.zRot((float) (Math.PI / 45 * j));
                        vec = vec.yRot(angle);
                        Cloudlet cloud = new Cloudlet(getX() + vec.x, getY() + coreHeight + 25 + j * cs, getZ() + vec.z,
                                angle, 0, (int) ((20 + tickCount / 10) * (1 + random.nextDouble() * 0.1)),
                                TorexType.CONDENSATION);
                        cloud.setScale(0.125F * (float) cs, 3F * (float) cs);
                        cloudlets.add(cloud);
                    }
                }
            }

            for (Cloudlet cloud : cloudlets) {
                cloud.update();
            }
            coreHeight += 0.15 / s;
            torusWidth += 0.05 / s;
            rollerSize = torusWidth * 0.35;
            convectionHeight = coreHeight + rollerSize;

            int maxHeat = (int) (50 * cs);
            heat = maxHeat - Math.pow((maxHeat * (double) this.tickCount) / maxAge, 1);

            cloudlets.removeIf(x -> x.isDead);
        }

        if (!level().isClientSide && this.tickCount > maxAge) {
            discard();
        }
    }

    public EntityNukeTorex setScale(float scale) {
        if (!level().isClientSide) {
            entityData.set(DATA_SCALE, scale);
        }
        this.coreHeight = this.coreHeight / 1.5D * scale;
        this.convectionHeight = this.convectionHeight / 1.5D * scale;
        this.torusWidth = this.torusWidth / 1.5D * scale;
        this.rollerSize = this.rollerSize / 1.5D * scale;
        return this;
    }

    public EntityNukeTorex setType(int type) {
        entityData.set(DATA_TYPE, type);
        return this;
    }

    public int getTorexType() {
        return entityData.get(DATA_TYPE);
    }

    public double getSimulationSpeed() {
        int lifetime = getMaxAge();
        int simSlow = lifetime / 4;
        int simStop = lifetime / 2;
        int life = this.tickCount;

        if (life > simStop) {
            return 0D;
        }

        if (life > simSlow) {
            return 1D - ((double) (life - simSlow) / (double) (simStop - simSlow));
        }

        return 1.0D;
    }

    public double getScale() {
        return entityData.get(DATA_SCALE);
    }

    public float getCloudScale() {
        return entityData.get(DATA_SCALE);
    }

    public double getSaturation() {
        double d = (double) this.tickCount / (double) this.getMaxAge();
        return 1D - (d * d * d * d);
    }

    public double getGreying() {
        int lifetime = getMaxAge();
        int greying = lifetime * 3 / 4;

        if (tickCount > greying) {
            return 1 + ((double) (tickCount - greying) / (double) (lifetime - greying));
        }

        return 1D;
    }

    public float getAlpha() {
        int lifetime = getMaxAge();
        int fadeOut = lifetime * 3 / 4;
        int life = this.tickCount;

        if (life > fadeOut) {
            float fac = (float) (life - fadeOut) / (float) (lifetime - fadeOut);
            return 1F - fac;
        }

        return 1.0F;
    }

    public int getMaxAge() {
        double s = this.getScale();
        return (int) (45 * 20 * s);
    }

    public class Cloudlet {
        public double posX;
        public double posY;
        public double posZ;
        public double prevPosX;
        public double prevPosY;
        public double prevPosZ;
        public double motionX;
        public double motionY;
        public double motionZ;
        public int age;
        public int cloudletLife;
        public float angle;
        public boolean isDead = false;
        float rangeMod = 1.0F;
        public float colorMod = 1.0F;
        public Vec3 color = Vec3.ZERO;
        public Vec3 prevColor = Vec3.ZERO;
        public TorexType type;

        public Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge) {
            this(posX, posY, posZ, angle, age, maxAge, TorexType.STANDARD);
        }

        public Cloudlet(double posX, double posY, double posZ, float angle, int age, int maxAge, TorexType type) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.age = age;
            this.cloudletLife = maxAge;
            this.angle = angle;
            this.rangeMod = 0.3F + random.nextFloat() * 0.7F;
            this.colorMod = 0.8F + random.nextFloat() * 0.2F;
            this.type = type;
            this.updateColor();
            this.prevColor = this.color;
        }

        private void update() {
            age++;

            if (age > cloudletLife) {
                this.isDead = true;
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            Vec3 simPos = new Vec3(EntityNukeTorex.this.getX() - this.posX, 0, EntityNukeTorex.this.getZ() - this.posZ);
            double simPosX = EntityNukeTorex.this.getX() + simPos.length();
            double simPosZ = EntityNukeTorex.this.getZ();

            if (this.type == TorexType.STANDARD) {
                Vec3 convection = getConvectionMotion(simPosX, simPosZ);
                Vec3 lift = getLiftMotion(simPosX, simPosZ);

                double factor = Mth.clamp((this.posY - EntityNukeTorex.this.getY()) / EntityNukeTorex.this.coreHeight, 0, 1);
                this.motionX = convection.x * factor + lift.x * (1D - factor);
                this.motionY = convection.y * factor + lift.y * (1D - factor);
                this.motionZ = convection.z * factor + lift.z * (1D - factor);
            } else if (this.type == TorexType.SHOCK) {
                double factor = Mth.clamp((this.posY - EntityNukeTorex.this.getY()) / EntityNukeTorex.this.coreHeight, 0, 1);
                Vec3 motion = new Vec3(1, 0, 0).yRot(this.angle);
                this.motionX = motion.x * factor;
                this.motionY = motion.y * factor;
                this.motionZ = motion.z * factor;
            } else if (this.type == TorexType.RING) {
                Vec3 motion = getRingMotion(simPosX, simPosZ);
                this.motionX = motion.x;
                this.motionY = motion.y;
                this.motionZ = motion.z;
            } else if (this.type == TorexType.CONDENSATION) {
                Vec3 motion = getCondensationMotion();
                this.motionX = motion.x;
                this.motionY = motion.y;
                this.motionZ = motion.z;
            }

            double mult = this.motionMult * getSimulationSpeed();

            this.posX += this.motionX * mult;
            this.posY += this.motionY * mult;
            this.posZ += this.motionZ * mult;

            this.updateColor();
        }

        private Vec3 getCondensationMotion() {
            Vec3 delta = new Vec3(posX - EntityNukeTorex.this.getX(), 0, posZ - EntityNukeTorex.this.getZ());
            double speed = 0.00002 * EntityNukeTorex.this.tickCount;
            return new Vec3(delta.x * speed, 0, delta.z * speed);
        }

        private Vec3 getRingMotion(double simPosX, double simPosZ) {
            if (simPosX > EntityNukeTorex.this.getX() + torusWidth * 2) {
                return Vec3.ZERO;
            }

            Vec3 torusPos = new Vec3(
                    EntityNukeTorex.this.getX() + torusWidth,
                    EntityNukeTorex.this.getY() + coreHeight * 0.5,
                    EntityNukeTorex.this.getZ());

            Vec3 delta = new Vec3(torusPos.x - simPosX, torusPos.y - this.posY, torusPos.z - simPosZ);

            double roller = EntityNukeTorex.this.rollerSize * this.rangeMod * 0.25;
            double dist = delta.length() / roller - 1D;

            double func = 1D - Math.pow(Math.E, -dist);
            float angle = (float) (func * Math.PI * 0.5D);

            Vec3 rot = new Vec3(-delta.x / dist, -delta.y / dist, -delta.z / dist).zRot(angle);

            Vec3 motion = new Vec3(
                    torusPos.x + rot.x - simPosX,
                    torusPos.y + rot.y - this.posY,
                    torusPos.z + rot.z - simPosZ);

            double speed = 0.001D;
            motion = new Vec3(motion.x * speed, motion.y * speed, motion.z * speed);
            motion = motion.normalize().yRot(this.angle);

            return motion;
        }

        private Vec3 getConvectionMotion(double simPosX, double simPosZ) {
            Vec3 torusPos = new Vec3(
                    EntityNukeTorex.this.getX() + torusWidth,
                    EntityNukeTorex.this.getY() + coreHeight,
                    EntityNukeTorex.this.getZ());

            Vec3 delta = new Vec3(torusPos.x - simPosX, torusPos.y - this.posY, torusPos.z - simPosZ);

            double roller = EntityNukeTorex.this.rollerSize * this.rangeMod;
            double dist = delta.length() / roller - 1D;

            double func = 1D - Math.pow(Math.E, -dist);
            float angle = (float) (func * Math.PI * 0.5D);

            Vec3 rot = new Vec3(-delta.x / dist, -delta.y / dist, -delta.z / dist).zRot(angle);

            Vec3 motion = new Vec3(
                    torusPos.x + rot.x - simPosX,
                    torusPos.y + rot.y - this.posY,
                    torusPos.z + rot.z - simPosZ);

            motion = motion.normalize().yRot(this.angle);

            return motion;
        }

        private Vec3 getLiftMotion(double simPosX, double simPosZ) {
            double scale = Mth.clamp(1D - (simPosX - (EntityNukeTorex.this.getX() + torusWidth)), 0, 1);

            Vec3 motion = new Vec3(
                    EntityNukeTorex.this.getX() - this.posX,
                    (EntityNukeTorex.this.getY() + convectionHeight) - this.posY,
                    EntityNukeTorex.this.getZ() - this.posZ);

            motion = motion.normalize();
            return new Vec3(motion.x * scale, motion.y * scale, motion.z * scale);
        }

        private void updateColor() {
            this.prevColor = this.color;

            double exX = EntityNukeTorex.this.getX();
            double exY = EntityNukeTorex.this.getY() + EntityNukeTorex.this.coreHeight;
            double exZ = EntityNukeTorex.this.getZ();

            double distX = exX - posX;
            double distY = exY - posY;
            double distZ = exZ - posZ;

            double distSq = distX * distX + distY * distY + distZ * distZ;
            distSq /= EntityNukeTorex.this.heat;
            double dist = Math.sqrt(distSq);

            dist = Math.max(dist, 1);
            double col = 2D / dist;

            int typeId = EntityNukeTorex.this.getTorexType();

            if (typeId == 1) {
                this.color = new Vec3(
                        Math.max(col * 1, 0.25),
                        Math.max(col * 2, 0.25),
                        Math.max(col * 0.5, 0.25));
            } else if (typeId == 2) {
                Color awt = Color.getHSBColor(this.angle / 2F / (float) Math.PI, 1F, 1F);
                if (this.type == TorexType.RING) {
                    this.color = new Vec3(
                            Math.max(col * 1, 0.25),
                            Math.max(col * 1, 0.25),
                            Math.max(col * 1, 0.25));
                } else {
                    this.color = new Vec3(awt.getRed() / 255D, awt.getGreen() / 255D, awt.getBlue() / 255D);
                }
            } else {
                this.color = new Vec3(
                        Math.max(col * 2, 0.25),
                        Math.max(col * 1.5, 0.25),
                        Math.max(col * 0.5, 0.25));
            }
        }

        public Vec3 getInterpPos(float interp) {
            float scale = (float) EntityNukeTorex.this.getScale();
            double x = prevPosX + (posX - prevPosX) * interp;
            double y = prevPosY + (posY - prevPosY) * interp;
            double z = prevPosZ + (posZ - prevPosZ) * interp;

            if (this.type != TorexType.SHOCK) {
                x = (x - EntityNukeTorex.this.getX()) * scale + EntityNukeTorex.this.getX();
                y = (y - EntityNukeTorex.this.getY()) * scale + EntityNukeTorex.this.getY();
                z = (z - EntityNukeTorex.this.getZ()) * scale + EntityNukeTorex.this.getZ();
            }

            return new Vec3(x, y, z);
        }

        public Vec3 getInterpColor(float interp) {
            if (this.type == TorexType.CONDENSATION) {
                return new Vec3(1F, 1F, 1F);
            }

            double greying = EntityNukeTorex.this.getGreying();

            if (this.type == TorexType.RING) {
                greying += 1;
            }

            return new Vec3(
                    (prevColor.x + (color.x - prevColor.x) * interp) * greying,
                    (prevColor.y + (color.y - prevColor.y) * interp) * greying,
                    (prevColor.z + (color.z - prevColor.z) * interp) * greying);
        }

        public float getAlpha() {
            float alpha = (1F - ((float) age / (float) cloudletLife)) * EntityNukeTorex.this.getAlpha();
            if (this.type == TorexType.CONDENSATION) {
                alpha *= 0.25F;
            }
            return alpha;
        }

        private float startingScale = 1;
        private float growingScale = 5F;

        public float getScale() {
            float base = startingScale + ((float) age / (float) cloudletLife) * growingScale;
            if (this.type != TorexType.SHOCK) {
                base *= (float) EntityNukeTorex.this.getScale();
            }
            return base;
        }

        public Cloudlet setScale(float start, float grow) {
            this.startingScale = start;
            this.growingScale = grow;
            return this;
        }

        private double motionMult = 1F;

        public Cloudlet setMotion(double mult) {
            this.motionMult = mult;
            return this;
        }
    }

    public enum TorexType {
        STANDARD,
        SHOCK,
        RING,
        CONDENSATION
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Ephemeral visual — die next tick instead of discarding mid-load.
        killOnLoad = true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /** Squirt helper from legacy BobMathUtil. */
    public static double squirt(double x) {
        return Math.sqrt(x + 1.0D / Math.pow(x + 2.0D, 2)) - 1.0D / (x + 2.0D);
    }

    public static float scaleFromRadius(float radius) {
        return Mth.clamp((float) (squirt(radius * 0.01D) * 1.5D), 0.5F, 5.0F);
    }

    public static void statFacStandard(Level level, double x, double y, double z, float radius) {
        if (level.isClientSide) {
            return;
        }
        EntityNukeTorex torex = new EntityNukeTorex(level).setScale(scaleFromRadius(radius));
        torex.setPos(x, y, z);
        level.addFreshEntity(torex);
    }
}
