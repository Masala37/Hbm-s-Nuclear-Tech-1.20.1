package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DiFurnaceRtgBlockEntity;
import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.inventory.menu.HbmMenuHelper;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code MachineDiFurnaceRTG}. Registry id is {@code machine_difurnace_rtg_off};
 * LIT replaces the off/on block pair.
 */
public class MachineDiFurnaceRtgBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public MachineDiFurnaceRtgBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .lightLevel(state -> state.getValue(LIT) ? 15 : 0));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiFurnaceRtgBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.DI_FURNACE_RTG.get(), DiFurnaceRtgBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DiFurnaceRtgBlockEntity) {
            HbmMenuHelper.open(sp, be);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction facing = state.getValue(FACING);
        double x0 = pos.getX() + 0.5D;
        double y0 = pos.getY() + 0.25D + random.nextDouble() * 6.0D / 16.0D;
        double z0 = pos.getZ() + 0.5D;
        double sideOff = 0.52D;
        double sideRand = random.nextDouble() * 0.5D - 0.25D;
        switch (facing) {
            case WEST -> level.addParticle(ParticleTypes.FLAME, x0 - sideOff, y0, z0 + sideRand, 0.0D, 0.0D, 0.0D);
            case EAST -> level.addParticle(ParticleTypes.FLAME, x0 + sideOff, y0, z0 + sideRand, 0.0D, 0.0D, 0.0D);
            case NORTH -> level.addParticle(ParticleTypes.FLAME, x0 + sideRand, y0, z0 - sideOff, 0.0D, 0.0D, 0.0D);
            default -> level.addParticle(ParticleTypes.FLAME, x0 + sideRand, y0, z0 + sideOff, 0.0D, 0.0D, 0.0D);
        }
        float f5 = random.nextFloat() * 0.75F + 0.125F;
        float f6 = random.nextFloat() * 0.75F + 0.125F;
        level.addParticle(ParticleTypes.SMOKE, pos.getX() + f5, pos.getY() + 1.0D, pos.getZ() + f6, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DiFurnaceRtgBlockEntity furnace) {
                MachineDrops.drop(level, pos, furnace.getItems());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
