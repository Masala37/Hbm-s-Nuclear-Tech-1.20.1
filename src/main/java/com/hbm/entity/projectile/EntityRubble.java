package com.hbm.entity.projectile;

import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Flying rubble chunk (legacy {@code EntityRubble}).
 * Carries a block appearance and deals impact damage.
 */
public class EntityRubble extends ThrowableProjectile {
    private static final EntityDataAccessor<BlockState> DATA_BLOCK =
            SynchedEntityData.defineId(EntityRubble.class, EntityDataSerializers.BLOCK_STATE);

    public EntityRubble(EntityType<? extends EntityRubble> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public EntityRubble(Level level) {
        this(ModEntities.RUBBLE.get(), level);
    }

    public EntityRubble(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_BLOCK, Blocks.STONE.defaultBlockState());
    }

    public BlockState getBlockState() {
        return entityData.get(DATA_BLOCK);
    }

    public void setBlockState(BlockState state) {
        entityData.set(DATA_BLOCK, state == null ? Blocks.STONE.defaultBlockState() : state);
    }

    /** Legacy {@code setMetaBasedOnBlock} — meta ignored in 1.20. */
    public void setBasedOnBlock(Block block) {
        setBlockState(block == null ? Blocks.STONE.defaultBlockState() : block.defaultBlockState());
    }

    public void setBasedOnBlock(Block block, int ignoredMeta) {
        setBasedOnBlock(block);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 200) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (tickCount <= 2) {
            return;
        }
        if (level().isClientSide) {
            return;
        }

        BlockState state = getBlockState();
        BlockPos pos = BlockPos.containing(getX(), getY(), getZ());

        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.5F, 1.0F);

        if (level() instanceof ServerLevel server) {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    getX(), getY(), getZ(),
                    12, 0.25D, 0.25D, 0.25D, 0.08D);
            // Vanilla destroy FX (id 2001) for nearby clients
            server.levelEvent(2001, pos, Block.getId(state));
        }

        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (hit instanceof LivingEntity living) {
            living.hurt(damageSources().thrown(this, getOwner()), 15.0F);
        }
    }

    @Override
    protected float getGravity() {
        return 0.04F;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("blockState", NbtUtils.writeBlockState(getBlockState()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("blockState")) {
            setBlockState(NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK),
                    tag.getCompound("blockState")));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
