package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

public class RadarScreenBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty OX = IntegerProperty.create("ox", 0, 1);
    public static final IntegerProperty OY = IntegerProperty.create("oy", 0, 1);

    public RadarScreenBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OX, 0)
                .setValue(OY, 0));
    }

    public static boolean isCore(BlockState state) {
        return state.getBlock() instanceof RadarScreenBlock
                && state.getValue(OX) == 0
                && state.getValue(OY) == 0;
    }

    public static BlockPos corePos(BlockPos pos, BlockState state) {
        Direction clockwise = state.getValue(FACING).getClockWise();
        return pos.relative(clockwise.getOpposite(), state.getValue(OX)).below(state.getValue(OY));
    }

    @Nullable
    public static RadarScreenBlockEntity coreEntity(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof RadarScreenBlock)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(corePos(pos, state));
        return be instanceof RadarScreenBlockEntity screen ? screen : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OX, OY);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!hasRoom(context)) {
            return null;
        }
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(OX, 0)
                .setValue(OY, 0);
    }

    public static boolean hasRoom(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos core = context.getClickedPos();
        Direction clockwise = context.getHorizontalDirection().getClockWise();
        for (int ox = 0; ox <= 1; ox++) {
            for (int oy = 0; oy <= 1; oy++) {
                BlockPos cell = core.relative(clockwise, ox).above(oy);
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
        if (level.isClientSide || !(state.getBlock() instanceof RadarScreenBlock)) {
            return;
        }
        fillStructure(level, corePos(pos, state), state.getValue(FACING), state.getBlock());
    }

    public static void fillStructure(Level level, BlockPos core, Direction facing, Block screenBlock) {
        if (level.isClientSide || !(screenBlock instanceof RadarScreenBlock)) {
            return;
        }
        Direction clockwise = facing.getClockWise();
        DummyablePlacement.begin();
        try {
            for (int ox = 0; ox <= 1; ox++) {
                for (int oy = 0; oy <= 1; oy++) {
                    BlockPos cell = core.relative(clockwise, ox).above(oy);
                    BlockState want = screenBlock.defaultBlockState()
                            .setValue(FACING, facing)
                            .setValue(OX, ox)
                            .setValue(OY, oy);
                    BlockState at = level.getBlockState(cell);
                    if (!at.equals(want) && (at.is(screenBlock) || at.canBeReplaced())) {
                        level.setBlock(cell, want, 3);
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
        return isCore(state) ? new RadarScreenBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.RADAR_SCREEN.get(), RadarScreenBlockEntity::serverTick);
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            RadarScreenBlockEntity screen = coreEntity(level, pos, state);
            if (screen != null && screen.linked) {
                BlockPos radarPos = new BlockPos(screen.refX, screen.refY, screen.refZ);
                if (level.getBlockEntity(radarPos) instanceof RadarNTBlockEntity) {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                            com.hbm.client.ClientRadarScreens.openRadar(radarPos));
                }
            }
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
        Direction clockwise = state.getValue(FACING).getClockWise();
        DummyablePlacement.beginDismantle();
        try {
            for (int ox = 0; ox <= 1; ox++) {
                for (int oy = 0; oy <= 1; oy++) {
                    BlockPos cell = core.relative(clockwise, ox).above(oy);
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
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
