package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.bomb.ChargeBlockEntity;
import com.hbm.explosion.ExplosionNT;
import com.hbm.items.tool.DefuserItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Sticky demolition charge (legacy {@code BlockChargeBase}).
 * Right-click cycles timer; sneak-click arms; defuser disarms or removes.
 */
public class ChargeBlock extends BaseEntityBlock implements IBomb {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static boolean safe;

    public enum ChargeType {
        DYNAMITE,
        C4,
        SEMTEX,
        MINER
    }

    private final ChargeType type;

    public ChargeBlock(ChargeType type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_ORANGE)
                .strength(0.5F, 1.0F)
                .sound(SoundType.GRASS)
                .noOcclusion()
                .noCollission());
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public ChargeType getChargeType() {
        return type;
    }

    public static ChargeBlock dynamite() {
        return new ChargeBlock(ChargeType.DYNAMITE);
    }

    public static ChargeBlock c4() {
        return new ChargeBlock(ChargeType.C4);
    }

    public static ChargeBlock semtex() {
        return new ChargeBlock(ChargeType.SEMTEX);
    }

    public static ChargeBlock miner() {
        return new ChargeBlock(ChargeType.MINER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.CHARGE.get(), ChargeBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        BlockPos place = context.getClickedPos();
        Level level = context.getLevel();
        if (!canSurviveOn(level, place, face)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, face);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSurviveOn(level, pos, state.getValue(FACING));
    }

    private static boolean canSurviveOn(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!canSurvive(state, level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static VoxelShape shapeFor(Direction facing) {
        return switch (facing) {
            case DOWN -> Block.box(0, 10, 0, 16, 16, 16);
            case UP -> Block.box(0, 0, 0, 16, 6, 16);
            case NORTH -> Block.box(0, 0, 10, 16, 16, 16);
            case SOUTH -> Block.box(0, 0, 0, 16, 16, 6);
            case WEST -> Block.box(10, 0, 0, 16, 16, 16);
            case EAST -> Block.box(0, 0, 0, 6, 16, 16);
        };
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.hbm.charge.hint_timer"));
        tooltip.add(Component.translatable("block.hbm.charge.hint_arm"));
        tooltip.add(Component.translatable("block.hbm.charge.hint_defuser"));
        switch (type) {
            case C4 -> tooltip.add(Component.translatable("block.hbm.charge.c4.desc"));
            case SEMTEX -> {
                tooltip.add(Component.translatable("block.hbm.charge.semtex.desc1"));
                tooltip.add(Component.translatable("block.hbm.charge.semtex.desc2"));
            }
            case MINER -> {
                tooltip.add(Component.translatable("block.hbm.charge.miner.desc1"));
                tooltip.add(Component.translatable("block.hbm.charge.miner.desc2"));
            }
            default -> {
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ChargeBlockEntity charge)) {
            return InteractionResult.PASS;
        }

        if (held.getItem() instanceof DefuserItem) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (charge.isStarted()) {
                charge.setStarted(false);
                level.playSound(null, pos, ModSounds.FSTBMB_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("block.hbm.charge.disarmed"), true);
            } else {
                safe = true;
                level.removeBlock(pos, false);
                safe = false;
                ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        new ItemStack(this));
                drop.setDefaultPickUpDelay();
                level.addFreshEntity(drop);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (charge.isStarted()) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            if (charge.getTimer() > 0) {
                charge.setStarted(true);
                level.playSound(null, pos, ModSounds.FSTBMB_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("block.hbm.charge.armed",
                        charge.getMinutes(), charge.getSeconds()), true);
            } else {
                player.displayClientMessage(Component.translatable("block.hbm.charge.need_timer"), true);
            }
        } else {
            charge.cycleTimer();
            level.playSound(null, pos, ModSounds.TECH_BOOP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (charge.getTimer() == 0) {
                player.displayClientMessage(Component.translatable("block.hbm.charge.timer_off"), true);
            } else {
                player.displayClientMessage(Component.translatable("block.hbm.charge.timer_set",
                        charge.getMinutes(), charge.getSeconds()), true);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !safe && !level.isClientSide) {
            explode(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        safe = true;
        level.removeBlock(pos, false);
        safe = false;

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        BiConsumer<Level, BlockPos> blast = switch (type) {
            case DYNAMITE -> (l, p) -> new ExplosionNT(l, null, x, y, z, 4.0F).explode();
            case C4 -> (l, p) -> new ExplosionNT(l, null, x, y, z, 15.0F)
                    .overrideResolution(32)
                    .addAttrib(ExplosionNT.ExAttrib.NODROP)
                    .explode();
            case SEMTEX -> (l, p) -> new ExplosionNT(l, null, x, y, z, 10.0F)
                    .overrideResolution(32)
                    .addAttrib(ExplosionNT.ExAttrib.ALLDROP)
                    .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                    .explode();
            case MINER -> (l, p) -> new ExplosionNT(l, null, x, y, z, 4.0F)
                    .addAttrib(ExplosionNT.ExAttrib.ALLDROP)
                    .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                    .explode();
        };
        blast.accept(level, pos);
        return BombReturnCode.DETONATED;
    }
}
