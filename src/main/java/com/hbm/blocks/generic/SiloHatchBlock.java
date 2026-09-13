package com.hbm.blocks.generic;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.machine.SiloHatchBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7 {@code BlockDoorGeneric} silo hatches: dummyable footprint, click / redstone / detonator toggle.
 */
public class SiloHatchBlock extends BlockDummyable implements IBomb {
    private static final int[] SMALL = {0, 0, 2, 2, 2, 2};
    private static final int[] LARGE = {0, 0, 3, 3, 3, 3};

    private final boolean large;

    public SiloHatchBlock(boolean large) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(10.0F, 100.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        this.large = large;
    }

    public boolean large() {
        return large;
    }

    @Override
    public int[] getDimensions() {
        return large ? LARGE : SMALL;
    }

    @Override
    public int getOffset() {
        return large ? 3 : 2;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new SiloHatchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.SILO_HATCH.get(), SiloHatchBlockEntity::tick);
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
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof SiloHatchBlockEntity hatch && hatch.tryToggle(player, false)) {
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return BombReturnCode.ERROR_INCOMPATIBLE;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof SiloHatchBlockEntity hatch && hatch.tryToggle(null, true)) {
            return BombReturnCode.TRIGGERED;
        }
        return BombReturnCode.ERROR_INCOMPATIBLE;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || !level.getBlockState(pos).is(this)) {
            return;
        }
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof SiloHatchBlockEntity hatch) {
            hatch.updateRedstonePower(pos);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hatchOpen(level, pos, state) ? Shapes.empty() : Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return DummyableMeta.isExtra(state.getValue(META));
    }

    private boolean hatchOpen(BlockGetter level, BlockPos pos, BlockState state) {
        if (DummyableMeta.isExtra(state.getValue(META))) {
            return true;
        }
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(core);
        return be instanceof SiloHatchBlockEntity hatch && SiloHatchLogic.passable(hatch.doorState());
    }
}
