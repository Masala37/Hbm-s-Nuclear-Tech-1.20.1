package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.bomb.LandmineBlockEntity;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.tool.DefuserItem;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Proximity landmine (legacy {@code Landmine}) — AP / HE / Shrap / Fat / Naval.
 */
public class LandmineBlock extends BaseEntityBlock implements IBomb {
    /** Prevents recursive detonation while the mine removes itself. */
    public static boolean safeMode;

    private final double range;
    private final double height;
    private final MineType type;
    private final VoxelShape shape;

    public enum MineType {
        AP,
        HE,
        SHRAP,
        FAT,
        NAVAL
    }

    public LandmineBlock(double range, double height, MineType type, VoxelShape shape) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0F)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.range = range;
        this.height = height;
        this.type = type;
        this.shape = shape;
    }

    public static LandmineBlock ap() {
        return new LandmineBlock(1.5D, 1.0D, MineType.AP, Block.box(5, 0, 5, 11, 1, 11));
    }

    public static LandmineBlock he() {
        return new LandmineBlock(2.0D, 5.0D, MineType.HE, Block.box(4, 0, 4, 12, 2, 12));
    }

    public static LandmineBlock shrap() {
        return new LandmineBlock(1.5D, 1.0D, MineType.SHRAP, Block.box(5, 0, 5, 11, 1, 11));
    }

    public static LandmineBlock fat() {
        return new LandmineBlock(2.5D, 1.0D, MineType.FAT, Block.box(5, 0, 4, 11, 6, 12));
    }

    public static LandmineBlock naval() {
        return new LandmineBlock(2.5D, 1.0D, MineType.NAVAL, Block.box(1, 0, 1, 15, 14, 15));
    }

    public double getRange() {
        return range;
    }

    public double getHeight() {
        return height;
    }

    public MineType getMineType() {
        return type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LandmineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.LANDMINE.get(), LandmineBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // World mesh drawn by RenderLandmine (legacy TESR scales / biome skins).
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return Block.canSupportCenter(level, pos.below(), Direction.UP) || below.getBlock() instanceof FenceBlock;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return canSurvive(defaultBlockState(), context.getLevel(), context.getClickedPos())
                ? defaultBlockState()
                : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN && !canSurvive(state, level, currentPos)) {
            if (level instanceof Level real && !real.isClientSide) {
                if (!safeMode) {
                    explode(real, currentPos);
                } else {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos);
            return;
        }
        if (!canSurvive(state, level, pos)) {
            if (!safeMode) {
                explode(level, pos);
            } else {
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative() && !safeMode) {
            explode(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DefuserItem)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            safeMode = true;
            level.removeBlock(pos, false);
            safeMode = false;

            ItemEntity drop = new ItemEntity(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    new ItemStack(this));
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide || safeMode) {
            return BombReturnCode.UNDEFINED;
        }

        boolean water = isWaterAbove(level, pos);

        safeMode = true;
        level.removeBlock(pos, false);
        safeMode = false;

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        switch (type) {
            case AP -> {
                // Piercing anti-personnel: entity damage, no terrain dig.
                new ExplosionNT(level, null, x, y, z, 3.0F)
                        .addAttrib(ExplosionNT.ExAttrib.NOBLOCK)
                        .overrideResolution(12)
                        .explode();
                pierceLiving(level, x, y, z, 4.0D, 8.0F);
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.SMOKE, x, y + 0.2D, z, 12, 0.2D, 0.1D, 0.2D, 0.02D);
                    server.sendParticles(ParticleTypes.CRIT, x, y + 0.3D, z, 18, 0.4D, 0.2D, 0.4D, 0.15D);
                }
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.0F, 1.4F);
            }
            case HE -> {
                new ExplosionNT(level, null, x, y, z, 4.0F)
                        .overrideResolution(24)
                        .explode();
                pierceLiving(level, x, y, z, 5.0D, 14.0F);
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.5D, z, 20, 0.6D, 0.4D, 0.6D, 0.04D);
                }
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, 0.9F);
            }
            case SHRAP -> {
                new ExplosionNT(level, null, x, y, z, 3.0F)
                        .addAttrib(ExplosionNT.ExAttrib.NOBLOCK)
                        .overrideResolution(12)
                        .explode();
                spawnShrapnel(level, x, y, z);
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 2.5F, 1.2F);
            }
            case FAT -> {
                new ExplosionNT(level, null, x, y, z, 10.0F)
                        .overrideResolution(48)
                        .explode();
                EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, 10.0F);
                ChunkRadiationManager.INSTANCE.incrementRad(level, (int) x, (int) y, (int) z, 25.0F);
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 1.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 12.0F, 0.85F);
            }
            case NAVAL -> {
                // Legacy offsets dig center +5/+5/+5 and uses BlockAllocatorWater.
                new ExplosionNT(level, null, x + 5.0D, y + 5.0D, z + 5.0D, 25.0F)
                        .overrideResolution(32)
                        .addAttrib(ExplosionNT.ExAttrib.WATER_DIG)
                        .addAttrib(ExplosionNT.ExAttrib.NODROP)
                        .explode();
                pierceLiving(level, x, y, z, 12.0D, 25.0F);
                if (level instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 2.0D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    server.sendParticles(ParticleTypes.CLOUD, x, y + 1.5D, z, 30, 1.5D, 0.8D, 1.5D, 0.05D);
                    if (water) {
                        server.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, x, y + 0.5D, z, 60, 2.0D, 1.0D, 2.0D, 0.2D);
                        server.sendParticles(ParticleTypes.SPLASH, x, y + 1.0D, z, 40, 2.0D, 0.5D, 2.0D, 0.15D);
                    }
                }
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 8.0F, 0.7F);
            }
        }

        return BombReturnCode.DETONATED;
    }

    private static void pierceLiving(Level level, double x, double y, double z, double range, float damage) {
        AABB box = new AABB(x, y, z, x, y, z).inflate(range);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            double dist = Math.sqrt(living.distanceToSqr(x, y, z));
            if (dist > range) {
                continue;
            }
            float scaled = (float) (damage * (1.0D - dist / range));
            living.hurt(level.damageSources().explosion(null), Math.max(2.0F, scaled));
        }
    }

    private static void spawnShrapnel(Level level, double x, double y, double z) {
        ExplosionLarge.spawnShrapnels(level, x, y + 0.4D, z, 12);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, x, y + 0.3D, z, 16, 0.3D, 0.2D, 0.3D, 0.03D);
        }
    }

    public static boolean isWaterAbove(Level level, BlockPos pos) {
        for (int xo = -1; xo <= 1; xo++) {
            for (int zo = -1; zo <= 1; zo++) {
                BlockPos check = pos.offset(xo, 1, zo);
                if (level.getFluidState(check).is(Fluids.WATER) || level.getFluidState(check).is(Fluids.FLOWING_WATER)
                        || level.getBlockState(check).is(Blocks.WATER) || level.getBlockState(check).is(Blocks.BUBBLE_COLUMN)) {
                    return true;
                }
            }
        }
        return false;
    }
}
