package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.config.BombConfig;
import com.hbm.explosion.ExplosionNT;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Instant-detonation charge (det cord / HE charge / AP mine). No fuse entity.
 */
public class InstantExplosiveBlock extends Block implements IBomb {
    private static final VoxelShape MINE_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 1.0D, 11.0D);

    private final float power;
    private final boolean nuclear;
    private final boolean tripMine;
    private final VoxelShape shape;

    public InstantExplosiveBlock(float power) {
        this(power, false, false, null);
    }

    public static InstantExplosiveBlock nuclearCharge() {
        return new InstantExplosiveBlock(0.0F, true, false, null);
    }

    public static InstantExplosiveBlock tripMine(float power) {
        return new InstantExplosiveBlock(power, false, true, MINE_SHAPE);
    }

    private InstantExplosiveBlock(float power, boolean nuclear, boolean tripMine, VoxelShape shape) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.FIRE)
                .strength(tripMine ? 1.0F : 0.1F)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.power = power;
        this.nuclear = nuclear;
        this.tripMine = tripMine;
        this.shape = shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape != null ? shape : super.getShape(state, level, pos, context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        tryTrip(level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        tryTrip(level, pos, entity);
        super.stepOn(level, pos, state, entity);
    }

    private void tryTrip(Level level, BlockPos pos, Entity entity) {
        if (tripMine && !level.isClientSide && entity instanceof LivingEntity living
                && !(living instanceof Player player && player.isCreative())) {
            explode(level, pos);
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        level.removeBlock(pos, false);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        if (nuclear) {
            AssembledNukeBlock.ignite(level, pos, BombConfig.missileRadius.get());
        } else {
            new ExplosionNT(level, null, x, y, z, power).explode();
        }
        return BombReturnCode.DETONATED;
    }
}
