package com.hbm.blocks;

import com.hbm.blocks.machine.DummyablePlacement;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.menu.HbmMenuHelper;
import api.hbm.block.IToolable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code BlockDummyable}: one core TE plus dummy cells filled from {@link #getDimensions()}.
 * Launch pads keep their packed OX/OZ grids; new machines should extend this.
 */
public abstract class BlockDummyable extends BaseEntityBlock {
    public static final int OFFSET = DummyableMeta.OFFSET;
    public static final int EXTRA = DummyableMeta.EXTRA;
    public static final IntegerProperty META = IntegerProperty.create("meta", 0, 15);

    protected BlockDummyable(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(META, DummyableMeta.coreMeta(DummyableMeta.SOUTH)));
    }

    /** {@code [UP, DOWN, NORTH, SOUTH, WEST, EAST]} when the core faces south. */
    public abstract int[] getDimensions();

    /** Blocks from the clicked cell to the core along the player's look. */
    public abstract int getOffset();

    public int getHeightOffset() {
        return 0;
    }

    protected abstract BlockEntity newCoreEntity(BlockPos pos, BlockState state);

    public static boolean isCore(BlockState state) {
        return state.getBlock() instanceof BlockDummyable && DummyableMeta.isCore(state.getValue(META));
    }

    @Nullable
    public BlockPos findCore(BlockGetter level, BlockPos pos) {
        java.util.HashSet<Long> seen = new java.util.HashSet<>();
        BlockPos at = pos;
        for (int step = 0; step < MultiblockHandlerXR.CELL_LIMIT; step++) {
            if (!seen.add(at.asLong())) {
                return null;
            }
            BlockState state = level.getBlockState(at);
            if (!state.is(this)) {
                return null;
            }
            int meta = state.getValue(META);
            if (DummyableMeta.isCore(meta)) {
                return at;
            }
            at = at.offset(
                    DummyableMeta.towardCoreX(meta),
                    DummyableMeta.towardCoreY(meta),
                    DummyableMeta.towardCoreZ(meta));
        }
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(META);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int facing = DummyableMeta.facingFromYaw(context.getRotation());
        if (!hasRoom(context, facing)) {
            return null;
        }
        return defaultBlockState().setValue(META, DummyableMeta.coreMeta(facing));
    }

    public boolean hasRoom(BlockPlaceContext context, int facing) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        int o = -getOffset();
        int height = getHeightOffset();
        BlockPos core = clicked.offset(
                DummyableMeta.offsetX(facing) * o,
                height,
                DummyableMeta.offsetZ(facing) * o);
        for (MultiblockHandlerXR.Cell cell : allDummyCells(core, facing)) {
            BlockPos at = new BlockPos(cell.x(), cell.y(), cell.z());
            if (at.equals(clicked)) {
                continue;
            }
            if (!level.getBlockState(at).canBeReplaced(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(state.getBlock() instanceof BlockDummyable)) {
            return;
        }
        int facing = DummyableMeta.isCore(state.getValue(META))
                ? DummyableMeta.coreFacing(state.getValue(META))
                : DummyableMeta.SOUTH;
        if (placer != null) {
            facing = DummyableMeta.facingFromYaw(placer.getYRot());
        }
        int o = -getOffset();
        BlockPos core = pos.offset(
                DummyableMeta.offsetX(facing) * o,
                getHeightOffset(),
                DummyableMeta.offsetZ(facing) * o);
        DummyablePlacement.begin();
        DummyablePlacement.beginDismantle();
        try {
            // 1.7 airs the click cell on both sides so client prediction is not a second core TESR.
            if (!pos.equals(core)) {
                level.removeBlock(pos, false);
            }
            if (level.isClientSide) {
                return;
            }
            BlockState coreState = defaultBlockState().setValue(META, DummyableMeta.coreMeta(facing));
            level.setBlock(core, coreState, Block.UPDATE_ALL);
            for (MultiblockHandlerXR.Cell cell : allDummyCells(core, facing)) {
                BlockPos at = new BlockPos(cell.x(), cell.y(), cell.z());
                if (at.equals(core)) {
                    continue;
                }
                level.setBlock(at, defaultBlockState().setValue(META, cell.meta()), Block.UPDATE_ALL);
            }
            fillExtras(level, core, facing);
        } finally {
            DummyablePlacement.endDismantle();
            DummyablePlacement.end();
        }
    }

    /** 1.7.10 {@code fillSpace} extras (ports). Called after dummy cells exist. */
    protected void fillExtras(Level level, BlockPos core, int facing) {
    }

    /**
     * Extra Dummyable boxes outside {@link #getDimensions()}, such as the pumpjack walkway.
     * Meta still faces the fill origin used to generate each cell.
     */
    protected java.util.List<MultiblockHandlerXR.Cell> extraDummyCells(BlockPos core, int facing) {
        return java.util.List.of();
    }

    protected java.util.List<MultiblockHandlerXR.Cell> allDummyCells(BlockPos core, int facing) {
        java.util.List<MultiblockHandlerXR.Cell> cells = new java.util.ArrayList<>(
                MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), getDimensions(), facing));
        java.util.HashSet<Long> seen = new java.util.HashSet<>();
        seen.add(core.asLong());
        for (MultiblockHandlerXR.Cell cell : cells) {
            seen.add(BlockPos.asLong(cell.x(), cell.y(), cell.z()));
        }
        for (MultiblockHandlerXR.Cell cell : extraDummyCells(core, facing)) {
            if (seen.add(BlockPos.asLong(cell.x(), cell.y(), cell.z()))) {
                cells.add(cell);
            }
        }
        return cells;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (this instanceof IToolable toolable) {
            InteractionResult tooled = IToolable.tryUse(level, player, hand, hit, toolable);
            if (tooled.consumesAction() || tooled == InteractionResult.SUCCESS) {
                return tooled;
            }
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof MenuProvider) {
            HbmMenuHelper.open(sp, be);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /**
     * 1.7 press/e-press {@code onScrew}: air a dummy cell without collapsing the core.
     */
    protected boolean drillOutDummy(Level level, BlockPos pos, BlockState state, IToolable.ToolType tool) {
        if (tool != IToolable.ToolType.HAND_DRILL || DummyableMeta.isCore(state.getValue(META))) {
            return false;
        }
        DummyablePlacement.beginSafeRem();
        try {
            level.removeBlock(pos, false);
        } finally {
            DummyablePlacement.endSafeRem();
        }
        return true;
    }

    public void makeExtra(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this) || DummyableMeta.isCore(state.getValue(META))) {
            return;
        }
        int extra = DummyableMeta.makeExtra(state.getValue(META));
        if (extra != state.getValue(META)) {
            DummyablePlacement.begin();
            try {
                level.setBlock(pos, state.setValue(META, extra), Block.UPDATE_CLIENTS);
            } finally {
                DummyablePlacement.end();
            }
        }
    }

    public void removeExtra(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(this) || DummyableMeta.isCore(state.getValue(META))) {
            return;
        }
        int stripped = DummyableMeta.removeExtra(state.getValue(META));
        if (stripped != state.getValue(META)) {
            DummyablePlacement.begin();
            try {
                level.setBlock(pos, state.setValue(META, stripped), Block.UPDATE_CLIENTS);
            } finally {
                DummyablePlacement.end();
            }
        }
    }

    /**
     * Core and extra cells keep a BE. Forge 47.4.10 has no {@code IForgeBlock.hasBlockEntity},
     * so this is not an override of Block; dummy conversion still uses {@link #newBlockEntity}.
     */
    public boolean hasBlockEntity(BlockState state) {
        int meta = state.getValue(META);
        return DummyableMeta.isCore(meta) || DummyableMeta.isExtra(meta);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return DummyableMeta.isCore(state.getValue(META)) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (DummyableMeta.isCore(state.getValue(META)) || DummyablePlacement.placing()) {
            return true;
        }
        BlockPos core = findCore(level, pos);
        return core != null && DummyableMeta.isCore(level.getBlockState(core).getValue(META));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (DummyablePlacement.safeRem()) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            return;
        }
        if (DummyablePlacement.dismantling() || DummyablePlacement.editing()) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        int facing = DummyableMeta.isCore(state.getValue(META))
                ? DummyableMeta.coreFacing(state.getValue(META))
                : DummyableMeta.SOUTH;
        BlockPos core = DummyableMeta.isCore(state.getValue(META)) ? pos : findCore(level, pos);
        if (core == null) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        BlockState coreState = level.getBlockState(core);
        if (coreState.is(this) && DummyableMeta.isCore(coreState.getValue(META))) {
            facing = DummyableMeta.coreFacing(coreState.getValue(META));
        }
        DummyablePlacement.beginDismantle();
        try {
            for (MultiblockHandlerXR.Cell cell : allDummyCells(core, facing)) {
                BlockPos at = new BlockPos(cell.x(), cell.y(), cell.z());
                if (at.equals(pos)) {
                    continue;
                }
                if (level.getBlockState(at).is(this)) {
                    level.removeBlock(at, false);
                }
            }
            if (!core.equals(pos) && level.getBlockState(core).is(this)) {
                level.removeBlock(core, true);
            }
        } finally {
            DummyablePlacement.endDismantle();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!DummyableMeta.isCore(state.getValue(META)) && !player.isCreative()) {
            BlockPos core = findCore(level, pos);
            if (core != null && DummyableMeta.isCore(level.getBlockState(core).getValue(META))) {
                super.playerWillDestroy(level, core, level.getBlockState(core), player);
                return;
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
