package com.hbm.blocks.machine;

import com.hbm.api.bomb.IBomb;
import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.entity.missile.MissileLaunchRegistry;
import com.hbm.inventory.menu.HbmMenuHelper;
import com.hbm.items.tool.DesignatorItem;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Missile launch pad — designator, missile, battery/fuel GUI, redstone / detonator launch.
 * Legacy {@code LaunchPad} implements {@link IBomb}; detonators call {@link #explode}.
 */
public class LaunchPadBlock extends BaseEntityBlock implements IBomb {
    public LaunchPadBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LAUNCH_PAD.get(),
                level.isClientSide ? LaunchPadBlockEntity::clientTick : LaunchPadBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            checkPower(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        checkPower(level, pos);
    }

    private static void checkPower(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LaunchPadBlockEntity pad) {
            pad.checkRedstone(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof LaunchPadBlockEntity pad)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty() && held.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            boolean filled = pad.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                    .map(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler))
                    .orElse(false);
            if (filled) {
                return InteractionResult.CONSUME;
            }
        }

        if (held.getItem() instanceof DesignatorItem) {
            if (pad.trySetTargetFromDesignator(held)) {
                level.playSound(null, pos, ModSounds.TECH_BLEEP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
                player.displayClientMessage(Component.literal("Launch pad target set: "
                        + pad.getTarget().getX() + ", " + pad.getTarget().getY() + ", "
                        + pad.getTarget().getZ()), true);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.literal("Designator has no target"), true);
            return InteractionResult.CONSUME;
        }

        if (MissileLaunchRegistry.isLaunchable(held)) {
            if (pad.tryInsertMissile(held)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 1.0F);
                player.displayClientMessage(Component.literal("Missile loaded"), true);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.literal("Pad already has a missile"), true);
            return InteractionResult.CONSUME;
        }

        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        HbmMenuHelper.open(sp, pad, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LaunchPadBlockEntity pad) {
                pad.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    /** Legacy launch-pad detonator path → {@link LaunchPadBlockEntity#launchFromDesignator()}. */
    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LaunchPadBlockEntity pad) {
            return pad.launchFromDesignator();
        }
        return BombReturnCode.UNDEFINED;
    }
}
