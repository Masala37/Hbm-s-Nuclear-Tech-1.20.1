package com.hbm.entity.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Primed bomb entity (legacy EntityTNTPrimedBase).
 */
public class PrimedBombEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(PrimedBombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BLOCK_ID =
            SynchedEntityData.defineId(PrimedBombEntity.class, EntityDataSerializers.INT);

    @Nullable
    private LivingEntity owner;
    private boolean detonateOnCollision;

    public PrimedBombEntity(EntityType<? extends PrimedBombEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public PrimedBombEntity(Level level, double x, double y, double z, @Nullable LivingEntity owner, Block bomb) {
        this(ModEntities.PRIMED_BOMB.get(), level);
        setPos(x, y, z);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        setDeltaMovement(-Math.sin(angle) * 0.02D, 0.2D, -Math.cos(angle) * 0.02D);
        setFuse(80);
        setBombBlock(bomb);
        this.owner = owner;
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public void setDetonateOnCollision(boolean detonateOnCollision) {
        this.detonateOnCollision = detonateOnCollision;
    }

    public boolean detonateOnCollision() {
        return detonateOnCollision;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_FUSE, 80);
        entityData.define(DATA_BLOCK_ID, Block.getId(Blocks.TNT.defaultBlockState()));
    }

    public void setFuse(int fuse) {
        entityData.set(DATA_FUSE, fuse);
    }

    public int getFuse() {
        return entityData.get(DATA_FUSE);
    }

    public void setBombBlock(Block block) {
        entityData.set(DATA_BLOCK_ID, Block.getId(block.defaultBlockState()));
    }

    public BlockState getBombState() {
        return Block.stateById(entityData.get(DATA_BLOCK_ID));
    }

    @Nullable
    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    public void tick() {
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98D));
        if (onGround()) {
            setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int fuse = getFuse() - 1;
        setFuse(fuse);
        boolean collideDetonate = detonateOnCollision && (horizontalCollision || verticalCollision);
        if (fuse <= 0 || collideDetonate) {
            discard();
            if (!level().isClientSide) {
                explode();
            }
        } else {
            updateInWaterStateAndDoFluidPushing();
            if (level().isClientSide) {
                level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void explode() {
        BlockState state = getBombState();
        if (state.getBlock() instanceof IBomb bomb) {
            bomb.explodeEntity(level(), getX(), getY(), getZ(), this);
        } else {
            level().explode(this, getX(), getY(), getZ(), 4.0F, Level.ExplosionInteraction.TNT);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putShort("Fuse", (short) getFuse());
        tag.putInt("BombBlock", entityData.get(DATA_BLOCK_ID));
        tag.putBoolean("DetonateOnCollision", detonateOnCollision);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setFuse(tag.getShort("Fuse"));
        entityData.set(DATA_BLOCK_ID, tag.getInt("BombBlock"));
        detonateOnCollision = tag.getBoolean("DetonateOnCollision");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }
}
