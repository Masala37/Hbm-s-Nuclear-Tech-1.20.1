package com.hbm.blocks.rbmk;

import com.hbm.blockentity.rbmk.RBMKBaseBlockEntity;
import com.hbm.config.RBMKConfig;
import com.hbm.rbmk.RBMKBlockStateProperties;
import com.hbm.rbmk.RBMKColumnPart;
import com.hbm.rbmk.RBMKLidType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class RBMKBaseBlock extends Block implements EntityBlock {
    private static final ThreadLocal<Boolean> DESTROYING_COLUMN = ThreadLocal.withInitial(() -> false);

    private static final VoxelShape CORE_SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape LID_SHAPE = Block.box(0, 0, 0, 16, 20, 16);

    protected RBMKBaseBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 30.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(RBMKBlockStateProperties.COLUMN_PART, RBMKColumnPart.CORE)
                .setValue(RBMKBlockStateProperties.SEGMENT, 0)
                .setValue(RBMKBlockStateProperties.LID, RBMKLidType.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RBMKBlockStateProperties.COLUMN_PART, RBMKBlockStateProperties.SEGMENT, RBMKBlockStateProperties.LID);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public RBMKLidType getLidType(BlockState state) {
        return state.getValue(RBMKBlockStateProperties.LID);
    }

    public boolean isCore(BlockState state) {
        return state.getValue(RBMKBlockStateProperties.COLUMN_PART) == RBMKColumnPart.CORE;
    }

    @Nullable
    public static BlockPos findCore(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RBMKBaseBlock rbmk)) {
            return null;
        }

        if (rbmk.isCore(state)) {
            return pos;
        }

        int segment = state.getValue(RBMKBlockStateProperties.SEGMENT);
        BlockPos corePos = pos.below(segment);
        BlockState coreState = level.getBlockState(corePos);
        if (coreState.getBlock() instanceof RBMKBaseBlock coreBlock
                && coreBlock.isCore(coreState)
                && coreState.getBlock() == state.getBlock()) {
            return corePos;
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (isCore(state) && getLidType(state).hasLid()) {
            return LID_SHAPE;
        }
        return CORE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }

        int height = RBMKConfig.columnHeight;
        for (int i = 1; i <= height; i++) {
            BlockPos target = pos.above(i);
            if (!level.getBlockState(target).canBeReplaced()) {
                level.removeBlock(pos, false);
                if (placer instanceof Player player && !player.getAbilities().instabuild) {
                    if (!player.addItem(stack.copy())) {
                        Block.popResource(level, pos, stack.copy());
                    }
                }
                return;
            }
        }

        for (int segment = 1; segment <= height; segment++) {
            BlockPos dummyPos = pos.above(segment);
            BlockState dummyState = defaultBlockState()
                    .setValue(RBMKBlockStateProperties.COLUMN_PART, RBMKColumnPart.DUMMY)
                    .setValue(RBMKBlockStateProperties.SEGMENT, segment)
                    .setValue(RBMKBlockStateProperties.LID, RBMKLidType.NONE);
            level.setBlock(dummyPos, dummyState, Block.UPDATE_ALL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !DESTROYING_COLUMN.get()) {
            if (!level.isClientSide) {
                BlockPos core = findCore(level, pos);
                if (core != null) {
                    destroyColumn(level, core);
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    protected void destroyColumn(Level level, BlockPos core) {
        DESTROYING_COLUMN.set(true);
        try {
            int height = RBMKConfig.columnHeight;
            for (int segment = 0; segment <= height; segment++) {
                BlockPos part = core.above(segment);
                BlockState partState = level.getBlockState(part);
                if (partState.getBlock() == this) {
                    level.removeBlock(part, false);
                }
            }
        } finally {
            DESTROYING_COLUMN.set(false);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide || state.getValue(RBMKBlockStateProperties.COLUMN_PART) != RBMKColumnPart.DUMMY) {
            return;
        }

        BlockPos core = findCore(level, pos);
        if (core == null) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (state.getValue(RBMKBlockStateProperties.COLUMN_PART) == RBMKColumnPart.CORE) {
            return Collections.singletonList(new ItemStack(this));
        }
        return Collections.emptyList();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos core = findCore(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }

        BlockEntity be = level.getBlockEntity(core);
        if (be instanceof RBMKBaseBlockEntity rbmk) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            String.format("RBMK %s | heat: %.1fC | lid: %s",
                                    rbmk.getColumnType().name().toLowerCase(),
                                    rbmk.getHeat(),
                                    rbmk.getLidType().name().toLowerCase())),
                    true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (!isCore(state)) {
            return null;
        }
        return createCoreBlockEntity(pos, state);
    }

    @Nullable
    protected abstract BlockEntity createCoreBlockEntity(BlockPos pos, BlockState state);

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || !isCore(state)) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof RBMKBaseBlockEntity rbmk) {
                RBMKBaseBlockEntity.serverTick(lvl, pos, st, rbmk);
            }
        };
    }
}
