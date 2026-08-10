package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Floor spikes — no solid collision, hurts falling living entities. */
public class SpikesBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 3, 10, 3),
            Block.box(6, 0, 1, 8, 12, 3),
            Block.box(13, 0, 1, 15, 9, 3),
            Block.box(1, 0, 6, 3, 11, 8),
            Block.box(6, 0, 6, 10, 14, 10),
            Block.box(13, 0, 6, 15, 10, 8),
            Block.box(1, 0, 13, 3, 9, 15),
            Block.box(6, 0, 13, 8, 11, 15),
            Block.box(13, 0, 13, 15, 12, 15));

    public SpikesBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion()
                .noCollission());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity && entity.getDeltaMovement().y < -0.1D) {
            entity.hurt(level.damageSources().cactus(), 8.0F);
        }
    }
}
