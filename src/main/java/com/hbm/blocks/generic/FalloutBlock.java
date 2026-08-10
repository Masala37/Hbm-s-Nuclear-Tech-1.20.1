package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Thin radioactive ash layer (legacy {@code BlockFallout}).
 * Texture reuses {@code ash.png}; drops the fallout item.
 */
public class FalloutBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public FalloutBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.1F)
                .sound(SoundType.SAND)
                .noOcclusion()
                .replaceable()
                .pushReaction(PushReaction.DESTROY)
                .isViewBlocking((s, g, p) -> false)
                .isSuffocating((s, g, p) -> false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.ICE) || below.is(Blocks.PACKED_ICE) || below.is(Blocks.BLUE_ICE)) {
            return false;
        }
        if (below.is(BlockTags.LEAVES)) {
            return true;
        }
        return below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        applyRad(level, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        applyRad(level, entity);
        super.stepOn(level, pos, state, entity);
    }

    private static void applyRad(Level level, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (living instanceof Player player && player.getAbilities().instabuild) {
            return;
        }
        living.addEffect(new MobEffectInstance(MobEffects.WITHER, 10 * 60 * 20, 0, true, false));
        living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 10 * 60 * 20, 0, true, false));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }
}
