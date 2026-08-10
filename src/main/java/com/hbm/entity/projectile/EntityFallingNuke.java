package com.hbm.entity.projectile;

import com.hbm.blocks.bomb.NukeCustomYield;
import com.hbm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Falling custom-nuke payload (legacy {@code EntityFallingNuke}).
 */
public class EntityFallingNuke extends Entity {
    public float tnt;
    public float nuke;
    public float hydro;
    public float amat;
    public float dirty;
    public float schrab;
    public float euph;

    public EntityFallingNuke(EntityType<? extends EntityFallingNuke> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public EntityFallingNuke(Level level) {
        this(ModEntities.FALLING_NUKE.get(), level);
    }

    public static EntityFallingNuke create(Level level, double x, double y, double z,
                                           float tnt, float nuke, float hydro, float amat,
                                           float dirty, float schrab, float euph) {
        EntityFallingNuke entity = new EntityFallingNuke(level);
        entity.setPos(x, y, z);
        entity.tnt = tnt;
        entity.nuke = nuke;
        entity.hydro = hydro;
        entity.amat = amat;
        entity.dirty = dirty;
        entity.schrab = schrab;
        entity.euph = euph;
        return entity;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }
        setDeltaMovement(getDeltaMovement().multiply(0.98D, 0.98D, 0.98D));
        move(MoverType.SELF, getDeltaMovement());

        if (!level().isClientSide && onGround()) {
            NukeCustomYield.explodeCustom(level(), getX(), getY(), getZ(),
                    tnt, nuke, hydro, amat, dirty, schrab, euph);
            discard();
        }

        if (!level().isClientSide && tickCount > 20 * 60 * 5) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tnt = tag.getFloat("tnt");
        nuke = tag.getFloat("nuke");
        hydro = tag.getFloat("hydro");
        amat = tag.getFloat("amat");
        dirty = tag.getFloat("dirty");
        schrab = tag.getFloat("schrab");
        euph = tag.getFloat("euph");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("tnt", tnt);
        tag.putFloat("nuke", nuke);
        tag.putFloat("hydro", hydro);
        tag.putFloat("amat", amat);
        tag.putFloat("dirty", dirty);
        tag.putFloat("schrab", schrab);
        tag.putFloat("euph", euph);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
