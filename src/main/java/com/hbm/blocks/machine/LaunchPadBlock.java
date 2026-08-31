package com.hbm.blocks.machine;

import api.hbm.item.IDesignatorItem;
import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blockentity.machine.LaunchPadProxyBlockEntity;
import com.hbm.entity.missile.MissileLaunchRegistry;
import com.hbm.inventory.menu.HbmMenuHelper;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Missile launch pad — designator, missile, battery/fuel GUI, redstone / detonator launch.
 * 3×3 dummyable: four full-height corners, half-height deck everywhere else (silo pit).
 */
public class LaunchPadBlock extends BaseEntityBlock implements IBomb {
    public static final IntegerProperty OX = IntegerProperty.create("ox", LaunchPadOffsets.PACKED_MIN, LaunchPadOffsets.PACKED_MAX);
    public static final IntegerProperty OZ = IntegerProperty.create("oz", LaunchPadOffsets.PACKED_MIN, LaunchPadOffsets.PACKED_MAX);

    private static final VoxelShape CORNER = Shapes.block();
    private static final VoxelShape DECK = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public static void beginStructureEdit() {
        DummyablePlacement.begin();
    }

    public static void endStructureEdit() {
        DummyablePlacement.end();
    }

    public LaunchPadBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any()
                .setValue(OX, LaunchPadOffsets.CORE)
                .setValue(OZ, LaunchPadOffsets.CORE));
    }

    public static boolean isCore(BlockState state) {
        return state.getBlock() instanceof LaunchPadBlock
                && LaunchPadOffsets.isCore(state.getValue(OX), state.getValue(OZ));
    }

    public static BlockPos corePos(BlockPos pos, BlockState state) {
        return pos.offset(
                LaunchPadOffsets.coreDeltaX(state.getValue(OX)),
                0,
                LaunchPadOffsets.coreDeltaZ(state.getValue(OZ)));
    }

    @Nullable
    public static LaunchPadBlockEntity coreEntity(Level level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof LaunchPadBlock)) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(corePos(pos, state));
        return be instanceof LaunchPadBlockEntity pad ? pad : null;
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
        Direction look = context.getHorizontalDirection();
        return defaultBlockState()
                .setValue(OX, LaunchPadOffsets.pack(-look.getStepX()))
                .setValue(OZ, LaunchPadOffsets.pack(-look.getStepZ()));
    }

    public static boolean hasRoom(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos core = clicked.relative(context.getHorizontalDirection());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos cell = core.offset(dx, 0, dz);
                if (cell.equals(clicked)) {
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
        if (level.isClientSide || !(state.getBlock() instanceof LaunchPadBlock)) {
            return;
        }
        fillStructure(level, corePos(pos, state), state.getBlock());
        LaunchPadBlockEntity pad = coreEntity(level, pos, state);
        if (pad != null && placer != null) {
            pad.setFacing(placer.getDirection());
        }
        checkPower(level, corePos(pos, state));
    }

    public static void fillStructure(Level level, BlockPos core, Block padBlock) {
        if (level.isClientSide || !(padBlock instanceof LaunchPadBlock)) {
            return;
        }
        beginStructureEdit();
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos cell = core.offset(dx, 0, dz);
                    BlockState want = padBlock.defaultBlockState()
                            .setValue(OX, LaunchPadOffsets.pack(dx))
                            .setValue(OZ, LaunchPadOffsets.pack(dz));
                    BlockState at = level.getBlockState(cell);
                    if (!at.equals(want) && (at.is(padBlock) || at.canBeReplaced())) {
                        level.setBlock(cell, want, 3);
                    }
                }
            }
        } finally {
            endStructureEdit();
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
            return new LaunchPadBlockEntity(pos, state);
        }
        if (LaunchPadOffsets.isCorner(state.getValue(OX), state.getValue(OZ))) {
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
        return createTickerHelper(type, ModBlockEntities.LAUNCH_PAD.get(),
                level.isClientSide ? LaunchPadBlockEntity::clientTick : LaunchPadBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return isCore(state) ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return LaunchPadOffsets.isCorner(state.getValue(OX), state.getValue(OZ)) ? CORNER : DECK;
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
        if (!(state.getBlock() instanceof LaunchPadBlock)) {
            return;
        }
        BlockPos core = corePos(pos, state);
        LaunchPadBlockEntity pad = coreEntity(level, pos, state);
        if (pad == null) {
            return;
        }
        boolean powered = false;
        for (int dx = -1; dx <= 1 && !powered; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (level.hasNeighborSignal(core.offset(dx, 0, dz))) {
                    powered = true;
                    break;
                }
            }
        }
        pad.checkRedstone(powered);
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
        LaunchPadBlockEntity pad = coreEntity(level, pos, state);
        if (pad == null) {
            return InteractionResult.PASS;
        }
        BlockPos core = corePos(pos, state);
        if (!held.isEmpty() && held.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            boolean filled = pad.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                    .map(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler))
                    .orElse(false);
            if (filled) {
                return InteractionResult.CONSUME;
            }
        }

        if (!player.isShiftKeyDown()) {
            if (MissileLaunchRegistry.isLaunchable(held) && pad.tryInsertMissile(held)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 1.0F);
                player.displayClientMessage(Component.literal("Missile loaded"), true);
                return InteractionResult.CONSUME;
            }
            if (held.getItem() instanceof IDesignatorItem && pad.tryInsertDesignator(held)) {
                level.playSound(null, pos, ModSounds.TECH_BLEEP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
                player.displayClientMessage(Component.literal("Designator loaded"), true);
                return InteractionResult.CONSUME;
            }
        }

        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        HbmMenuHelper.open(sp, pad, core);
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
        if (be instanceof LaunchPadBlockEntity pad) {
            pad.dropContents();
        }
        DummyablePlacement.beginDismantle();
        try {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
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
        LaunchPadBlockEntity pad = coreEntity(level, pos, level.getBlockState(pos));
        if (pad != null) {
            return pad.launchFromDesignator();
        }
        return BombReturnCode.UNDEFINED;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        int ox = state.getValue(OX);
        int oz = state.getValue(OZ);
        return switch (rotation) {
            case CLOCKWISE_90 -> state
                    .setValue(OX, LaunchPadOffsets.rotate90Ox(ox, oz))
                    .setValue(OZ, LaunchPadOffsets.rotate90Oz(ox, oz));
            case CLOCKWISE_180 -> state
                    .setValue(OX, LaunchPadOffsets.rotate180Ox(ox))
                    .setValue(OZ, LaunchPadOffsets.rotate180Oz(oz));
            case COUNTERCLOCKWISE_90 -> state
                    .setValue(OX, LaunchPadOffsets.rotate270Ox(ox, oz))
                    .setValue(OZ, LaunchPadOffsets.rotate270Oz(ox, oz));
            case NONE -> state;
        };
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        int ox = state.getValue(OX);
        int oz = state.getValue(OZ);
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(OZ, LaunchPadOffsets.mirrorZ(oz));
            case FRONT_BACK -> state.setValue(OX, LaunchPadOffsets.mirrorX(ox));
            case NONE -> state;
        };
    }
}
