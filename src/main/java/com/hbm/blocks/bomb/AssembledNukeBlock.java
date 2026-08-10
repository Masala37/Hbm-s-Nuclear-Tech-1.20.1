package com.hbm.blocks.bomb;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.bomb.AssembledNuke;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.inventory.menu.HbmMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Shared placeable nuclear bomb: facing, arming insert, redstone / detonator blast.
 */
public abstract class AssembledNukeBlock extends BaseEntityBlock implements IBomb {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected AssembledNukeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 200.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** Config crater radius passed to MK5.statFac. */
    protected abstract int blastRadius();

    /** Lang key prefix, e.g. {@code block.hbm.nuke_boy}. */
    protected abstract String langKey();

    /**
     * Outline / click volume. Must stay near the origin cell — vanilla rejects UseItemOn
     * packets whose hit location is too far from the block position (breaks oversized OBJs).
     * Visual casing is still the OBJ; use {@link com.hbm.client.ClientMenuClickHandler} for look-opens.
     */
    protected VoxelShape interactionShape() {
        return Shapes.block();
    }

    @Nullable
    protected AssembledNuke asAssembly(@Nullable BlockEntity be) {
        return be instanceof AssembledNuke nuke ? nuke : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
        // Legacy TESR-only (getRenderType == -1). Mesh drawn by AssembledNukeRenderer.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof MenuProvider provider ? provider : null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            tryDetonate(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
            if (nuke != null && nuke.isReady()) {
                explode(level, pos);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
        if (nuke == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            nuke.dropContents();
            player.displayClientMessage(Component.translatable(langKey() + ".ejected"), true);
            return InteractionResult.CONSUME;
        }

        // Always open schematic GUI (legacy behavior). Components are placed via the menu.
        if (player instanceof ServerPlayer sp) {
            MenuProvider provider = state.getMenuProvider(level, pos);
            if (provider != null) {
                try {
                    HbmMenuHelper.open(sp, provider, pos);
                } catch (Throwable t) {
                    // Fallback if custom open path fails to resolve (classloading / packet issues).
                    net.minecraftforge.network.NetworkHooks.openScreen(sp, provider, pos);
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
            if (nuke != null) {
                Containers.dropContents(level, pos, new net.minecraft.world.SimpleContainer(nuke.copyStacks()));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private void tryDetonate(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
            if (nuke != null && nuke.isReady()) {
                explode(level, pos);
            }
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
        if (nuke == null || !nuke.isReady()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }

        int radius = nuke.resolveBlastRadius(blastRadius());
        nuke.clearSlots();
        level.removeBlock(pos, false);
        detonateBlast(level, pos, radius);
        return BombReturnCode.DETONATED;
    }

    /** Default: MK5 dig + Torex. Override for FLEIJA / Solinium MK3 paths. */
    protected void detonateBlast(Level level, BlockPos pos, int radius) {
        ignite(level, pos, radius);
    }

    public static void ignite(Level level, BlockPos pos, int radius) {
        if (level.isClientSide) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        level.addFreshEntity(EntityNukeExplosionMK5.statFac(level, radius, x, y, z));
        EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, radius);
    }
}
