package com.hbm.entity.logic;

import com.hbm.explosion.ExplosionBalefire;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Comparator;

/**
 * Multi-tick balefire dig (legacy {@code EntityBalefire}).
 */
public class EntityBalefire extends Entity {
    private static final TicketType<ChunkPos> BALEFIRE_TICKET =
            TicketType.create("hbm_balefire", Comparator.comparingLong(ChunkPos::toLong), 40);

    public int age;
    public int destructionRange;
    public ExplosionBalefire exp;
    public int speed = 1;
    private boolean did;

    public EntityBalefire(EntityType<? extends EntityBalefire> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    public EntityBalefire(Level level) {
        this(ModEntities.BALEFIRE_BLAST.get(), level);
    }

    public static EntityBalefire statFac(Level level, double x, double y, double z, int range) {
        EntityBalefire bf = new EntityBalefire(level);
        bf.setPos(x, y, z);
        bf.destructionRange = Math.max(1, range);
        return bf;
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

        if (level() instanceof ServerLevel server) {
            ChunkPos cp = new ChunkPos(blockPosition());
            server.getChunkSource().addRegionTicket(BALEFIRE_TICKET, cp, 2, cp);
        }

        if (destructionRange <= 0) {
            discard();
            return;
        }

        if (!did) {
            int ox = Mth.floor(getX());
            int oy = Mth.floor(getY());
            int oz = Mth.floor(getZ());
            exp = new ExplosionBalefire(ox, oy, oz, level(), destructionRange);
            ChunkRadiationManager.INSTANCE.incrementRad(level(), ox, oy, oz, Math.max(25.0F, destructionRange * 0.2F));
            did = true;
        }

        speed += 1;
        boolean done = false;
        for (int i = 0; i < speed; i++) {
            if (exp != null && exp.update()) {
                done = true;
                break;
            }
        }

        if (!done) {
            // Legacy: dealDamage once per tick while dig continues.
            ExplosionNukeGeneric.dealDamage(level(), getX(), getY(), getZ(), destructionRange * 2.0F);
            age++;
        } else {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("age");
        destructionRange = tag.getInt("destructionRange");
        speed = Math.max(1, tag.getInt("speed"));
        did = tag.getBoolean("did");
        if (did) {
            exp = new ExplosionBalefire(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()), level(), destructionRange);
            exp.readFromNbt(tag, "exp_");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("age", age);
        tag.putInt("destructionRange", destructionRange);
        tag.putInt("speed", speed);
        tag.putBoolean("did", did);
        if (exp != null) {
            exp.saveToNbt(tag, "exp_");
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}
