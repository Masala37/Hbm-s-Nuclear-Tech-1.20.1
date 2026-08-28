package com.hbm.entity.logic;

import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

/**
 * Sustained EMP field (legacy {@code EntityEMP}).
 * First tick snapshots energy machines in radius 100 by walking loaded chunk
 * block-entity maps (not a 201³ block scan), then drains them every tick for
 * ten minutes.
 */
public class EntityEMP extends Entity {
    public static final int RADIUS = 100;
    public static final int LIFE = 10 * 60 * 20;

    private List<BlockPos> machines;
    private int remaining = LIFE;

    public EntityEMP(EntityType<? extends EntityEMP> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EntityEMP(Level level) {
        this(ModEntities.EMP_LOGIC.get(), level);
    }

    public static void spawn(Level level, double x, double y, double z) {
        if (level.isClientSide) {
            return;
        }
        EntityEMP emp = new EntityEMP(level);
        emp.setPos(x, y, z);
        level.addFreshEntity(emp);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        setDeltaMovement(Vec3.ZERO);

        if (machines == null) {
            allocate();
        } else {
            shock();
        }

        if (--remaining <= 0) {
            discard();
        }
    }

    private void allocate() {
        machines = new ArrayList<>();
        Level level = level();
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        int originX = Mth.floor(getX());
        int originY = Mth.floor(getY());
        int originZ = Mth.floor(getZ());
        long radiusSq = (long) RADIUS * (long) RADIUS;
        int minCx = (originX - RADIUS) >> 4;
        int maxCx = (originX + RADIUS) >> 4;
        int minCz = (originZ - RADIUS) >> 4;
        int maxCz = (originZ + RADIUS) >> 4;

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                LevelChunk chunk = server.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getBlockPos();
                    long dx = (long) pos.getX() - originX;
                    long dy = (long) pos.getY() - originY;
                    long dz = (long) pos.getZ() - originZ;
                    if (dx * dx + dy * dy + dz * dz > radiusSq) {
                        continue;
                    }
                    if (ExplosionNukeGeneric.hasEnergyCapability(be)) {
                        machines.add(pos.immutable());
                    }
                }
            }
        }
    }

    private void shock() {
        Level level = level();
        for (BlockPos pos : machines) {
            boolean drained = ExplosionNukeGeneric.drainEnergy(level, pos);
            if (drained && random.nextInt(20) == 0 && level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        8, 0.35D, 0.35D, 0.35D, 0.02D);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        remaining = tag.contains("empLife") ? tag.getInt("empLife") : LIFE;
        machines = null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("empLife", remaining);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
