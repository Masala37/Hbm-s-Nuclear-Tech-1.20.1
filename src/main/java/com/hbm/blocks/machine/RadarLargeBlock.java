package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.RadarLargeBlockEntity;
import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.blockentity.machine.RadarProxyBlockEntity;
import com.hbm.handler.RadarRules;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class RadarLargeBlock extends BaseEntityBlock {
    public static final DummyGridOffsets GRID = DummyGridOffsets.SILO;
    public static final IntegerProperty OX = IntegerProperty.create("ox", GRID.packedMin, GRID.packedMax);
    public static final IntegerProperty OZ = IntegerProperty.create("oz", GRID.packedMin, GRID.packedMax);
    public static final IntegerProperty OY = IntegerProperty.create("oy", 0, 4);
    public static final int HEIGHT = 4;

    public static final int[][] ENERGY_EXTRAS = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

    public RadarLargeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any()
                .setValue(OX, GRID.core)
                .setValue(OZ, GRID.core)
                .setValue(OY, 0));
    }

    public static boolean isCore(BlockState state) {
        return state.getBlock() instanceof RadarLargeBlock
                && GRID.isCore(state.getValue(OX), state.getValue(OZ))
                && state.getValue(OY) == 0;
    }

    public static boolean isEnergyPort(BlockState state) {
        if (!(state.getBlock() instanceof RadarLargeBlock) || state.getValue(OY) != 0) {
            return false;
        }
        int dx = Math.abs(GRID.unpack(state.getValue(OX)));
        int dz = Math.abs(GRID.unpack(state.getValue(OZ)));
        return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
    }

    public static BlockPos corePos(BlockPos pos, BlockState state) {
        return pos.offset(
                GRID.coreDeltaX(state.getValue(OX)),
                -state.getValue(OY),
                GRID.coreDeltaZ(state.getValue(OZ)));
    }

    @Nullable
    public static RadarLargeBlockEntity coreEntity(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof RadarLargeBlock)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(corePos(pos, state));
        return be instanceof RadarLargeBlockEntity radar ? radar : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OX, OZ, OY);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!hasRoom(context)) {
            return null;
        }
        Direction look = context.getHorizontalDirection();
        return defaultBlockState()
                .setValue(OX, GRID.pack(-look.getStepX()))
                .setValue(OZ, GRID.pack(-look.getStepZ()))
                .setValue(OY, 0);
    }

    public static boolean hasRoom(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos core = clicked.relative(context.getHorizontalDirection());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= HEIGHT; dy++) {
                    BlockPos cell = core.offset(dx, dy, dz);
                    if (cell.equals(clicked)) {
                        continue;
                    }
                    if (!level.getBlockState(cell).canBeReplaced(context)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide || !(state.getBlock() instanceof RadarLargeBlock)) {
            return;
        }
        fillStructure(level, corePos(pos, state), state.getBlock());
    }

    public static void fillStructure(Level level, BlockPos core, Block radarBlock) {
        if (level.isClientSide || !(radarBlock instanceof RadarLargeBlock)) {
            return;
        }
        DummyablePlacement.begin();
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= HEIGHT; dy++) {
                        BlockPos cell = core.offset(dx, dy, dz);
                        BlockState want = radarBlock.defaultBlockState()
                                .setValue(OX, GRID.pack(dx))
                                .setValue(OZ, GRID.pack(dz))
                                .setValue(OY, dy);
                        BlockState at = level.getBlockState(cell);
                        if (!at.equals(want) && (at.is(radarBlock) || at.canBeReplaced())) {
                            level.setBlock(cell, want, 3);
                        }
                    }
                }
            }
        } finally {
            DummyablePlacement.end();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isCore(state)) {
            return new RadarLargeBlockEntity(pos, state);
        }
        if (isEnergyPort(state)) {
            return new RadarProxyBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RADAR_LARGE.get(),
                level.isClientSide ? RadarLargeBlockEntity::clientTick : RadarLargeBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isCore(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (DummyablePlacement.placing() || DummyablePlacement.dismantling()) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return isEnergyPort(state);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!isEnergyPort(state) || !(level instanceof Level world)) {
            return 0;
        }
        RadarLargeBlockEntity radar = coreEntity(world, pos, state);
        return radar != null ? radar.getRedPower() : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return isEnergyPort(state);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        RadarLargeBlockEntity radar = coreEntity(level, pos, state);
        return radar != null ? radar.getRedPower() : 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        BlockPos core = corePos(pos, state);
        if (core.getY() < RadarRules.ALTITUDE) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal("[Radar] Error: Radar altitude not sufficient.")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hbm.client.ClientRadarScreens.openRadar(core));
        }
        return InteractionResult.SUCCESS;
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
        if (be instanceof RadarNTBlockEntity radar) {
            radar.dropContents();
        }
        DummyablePlacement.beginDismantle();
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= HEIGHT; dy++) {
                        BlockPos cell = core.offset(dx, dy, dz);
                        if (cell.equals(pos)) {
                            continue;
                        }
                        if (level.getBlockState(cell).is(this)) {
                            level.removeBlock(cell, false);
                        }
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
