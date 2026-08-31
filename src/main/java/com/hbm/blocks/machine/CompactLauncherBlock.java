package com.hbm.blocks.machine;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.machine.CompactLauncherBlockEntity;
import com.hbm.blockentity.machine.LaunchPadProxyBlockEntity;
import com.hbm.inventory.menu.HbmMenuHelper;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 3×3 compact custom-missile launcher (1.7.10 {@code CompactLauncher}).
 * Corners are energy/fluid ports; other cells are thin plates.
 */
public class CompactLauncherBlock extends BaseEntityBlock implements IBomb {
    public static final DummyGridOffsets GRID = DummyGridOffsets.SILO;
    public static final IntegerProperty OX = IntegerProperty.create("ox", GRID.packedMin, GRID.packedMax);
    public static final IntegerProperty OZ = IntegerProperty.create("oz", GRID.packedMin, GRID.packedMax);

    private static final VoxelShape PORT = Shapes.block();
    private static final VoxelShape PLATE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public CompactLauncherBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any()
                .setValue(OX, GRID.core)
                .setValue(OZ, GRID.core));
    }

    public static boolean isCore(BlockState state) {
        return state.getBlock() instanceof CompactLauncherBlock
                && GRID.isCore(state.getValue(OX), state.getValue(OZ));
    }

    public static BlockPos corePos(BlockPos pos, BlockState state) {
        return pos.offset(GRID.coreDeltaX(state.getValue(OX)), 0, GRID.coreDeltaZ(state.getValue(OZ)));
    }

    @Nullable
    public static CompactLauncherBlockEntity coreEntity(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof CompactLauncherBlock)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(corePos(pos, state));
        return be instanceof CompactLauncherBlockEntity launcher ? launcher : null;
    }

    public static boolean isPort(BlockState state) {
        return state.getBlock() instanceof CompactLauncherBlock
                && GRID.isCorner(state.getValue(OX), state.getValue(OZ));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OX, OZ);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!hasRoom(context)) {
            return null;
        }
        return defaultBlockState();
    }

    public static boolean hasRoom(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos core = context.getClickedPos();
        for (int dx = -GRID.radius; dx <= GRID.radius; dx++) {
            for (int dz = -GRID.radius; dz <= GRID.radius; dz++) {
                BlockPos cell = core.offset(dx, 0, dz);
                if (cell.equals(core)) {
                    continue;
                }
                if (!level.getBlockState(cell).canBeReplaced(context)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide || !(state.getBlock() instanceof CompactLauncherBlock)) {
            return;
        }
        fillStructure(level, corePos(pos, state), state.getBlock());
        checkPower(level, corePos(pos, state));
    }

    public static void fillStructure(Level level, BlockPos core, Block launcher) {
        if (level.isClientSide || !(launcher instanceof CompactLauncherBlock)) {
            return;
        }
        DummyablePlacement.begin();
        try {
            for (int dx = -GRID.radius; dx <= GRID.radius; dx++) {
                for (int dz = -GRID.radius; dz <= GRID.radius; dz++) {
                    BlockPos cell = core.offset(dx, 0, dz);
                    BlockState want = launcher.defaultBlockState()
                            .setValue(OX, GRID.pack(dx))
                            .setValue(OZ, GRID.pack(dz));
                    BlockState at = level.getBlockState(cell);
                    if (!at.equals(want) && (at.is(launcher) || at.canBeReplaced())) {
                        level.setBlock(cell, want, 3);
                    }
                }
            }
        } finally {
            DummyablePlacement.end();
        }
    }

    public static void tryCompleteStructure(Level level, BlockPos core) {
        if (level.isClientSide) {
            return;
        }
        BlockState coreState = level.getBlockState(core);
        if (!isCore(coreState)) {
            return;
        }
        fillStructure(level, core, coreState.getBlock());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isCore(state)) {
            return new CompactLauncherBlockEntity(pos, state);
        }
        if (isPort(state)) {
            return new LaunchPadProxyBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.COMPACT_LAUNCHER.get(),
                level.isClientSide ? CompactLauncherBlockEntity::clientTick : CompactLauncherBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isCore(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return isPort(state) ? PORT : PLATE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (isCore(state) || DummyablePlacement.placing()) {
            return true;
        }
        BlockState coreState = level.getBlockState(corePos(pos, state));
        return isCore(coreState);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (DummyablePlacement.placing() || DummyablePlacement.dismantling()) {
            return;
        }
        if (!oldState.is(state.getBlock())) {
            checkPower(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (DummyablePlacement.placing() || DummyablePlacement.dismantling()) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false);
            return;
        }
        checkPower(level, pos);
    }

    private static void checkPower(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CompactLauncherBlock)) {
            return;
        }
        BlockPos core = corePos(pos, state);
        CompactLauncherBlockEntity launcher = coreEntity(level, pos, state);
        if (launcher == null) {
            return;
        }
        boolean powered = false;
        for (int dx = -GRID.radius; dx <= GRID.radius && !powered; dx++) {
            for (int dz = -GRID.radius; dz <= GRID.radius; dz++) {
                if (level.hasNeighborSignal(core.offset(dx, 0, dz))) {
                    powered = true;
                    break;
                }
            }
        }
        launcher.checkRedstone(powered);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof com.hbm.items.tool.RadarLinkerItem) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        CompactLauncherBlockEntity launcher = coreEntity(level, pos, state);
        if (launcher == null) {
            return InteractionResult.PASS;
        }
        BlockPos core = corePos(pos, state);
        if (held.getItem() instanceof com.hbm.items.machine.InfiniteFluidBarrelItem) {
            if (launcher.fillFromInfiniteBarrel()) {
                return InteractionResult.CONSUME;
            }
        } else if (!held.isEmpty() && held.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            boolean filled = launcher.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                    .map(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler))
                    .orElse(false);
            if (filled) {
                return InteractionResult.CONSUME;
            }
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        HbmMenuHelper.open(sp, launcher, core);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            return;
        }
        if (DummyablePlacement.dismantling()) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        BlockPos core = corePos(pos, state);
        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof CompactLauncherBlockEntity launcher) {
            launcher.dropContents();
        }
        DummyablePlacement.beginDismantle();
        try {
            for (int dx = -GRID.radius; dx <= GRID.radius; dx++) {
                for (int dz = -GRID.radius; dz <= GRID.radius; dz++) {
                    BlockPos cell = core.offset(dx, 0, dz);
                    if (cell.equals(pos)) {
                        continue;
                    }
                    if (level.getBlockState(cell).is(this)) {
                        level.removeBlock(cell, false);
                    }
                }
            }
        } finally {
            DummyablePlacement.endDismantle();
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (!isCore(state)) {
            return Collections.emptyList();
        }
        return super.getDrops(state, params);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        BlockState state = level.getBlockState(pos);
        if (!isCore(state)) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }
        CompactLauncherBlockEntity launcher = coreEntity(level, pos, state);
        if (launcher != null && launcher.canLaunch()) {
            launcher.launchFromDesignator();
            return BombReturnCode.LAUNCHED;
        }
        return BombReturnCode.ERROR_MISSING_COMPONENT;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        int ox = state.getValue(OX);
        int oz = state.getValue(OZ);
        return switch (rotation) {
            case CLOCKWISE_90 -> state
                    .setValue(OX, GRID.rotate90Ox(ox, oz))
                    .setValue(OZ, GRID.rotate90Oz(ox, oz));
            case CLOCKWISE_180 -> state
                    .setValue(OX, GRID.rotate180Ox(ox))
                    .setValue(OZ, GRID.rotate180Oz(oz));
            case COUNTERCLOCKWISE_90 -> state
                    .setValue(OX, GRID.rotate270Ox(ox, oz))
                    .setValue(OZ, GRID.rotate270Oz(ox, oz));
            case NONE -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        int ox = state.getValue(OX);
        int oz = state.getValue(OZ);
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(OZ, GRID.mirrorZ(oz));
            case FRONT_BACK -> state.setValue(OX, GRID.mirrorX(ox));
            case NONE -> state;
        };
    }
}
