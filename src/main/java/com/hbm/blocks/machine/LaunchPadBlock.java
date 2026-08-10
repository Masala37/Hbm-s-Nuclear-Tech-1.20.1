package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.items.tool.DesignatorItem;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Missile launch pad — designator sets target, missile inserts, empty hand / redstone launches.
 */
public class LaunchPadBlock extends BaseEntityBlock {
    public LaunchPadBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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

        if (held.getItem() instanceof DesignatorItem) {
            if (pad.trySetTargetFromDesignator(held)) {
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.5F, 1.0F);
                player.displayClientMessage(Component.literal("Launch pad target set: "
                        + pad.getTarget().getX() + ", " + pad.getTarget().getY() + ", "
                        + pad.getTarget().getZ()), true);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.literal("Designator has no target"), true);
            return InteractionResult.CONSUME;
        }

        if (held.is(ModItems.MISSILE_GENERIC.get())) {
            if (pad.tryInsertMissile(held)) {
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 1.0F);
                player.displayClientMessage(Component.literal("Missile loaded"), true);
                return InteractionResult.CONSUME;
            }
            player.displayClientMessage(Component.literal("Pad already has a missile"), true);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            if (player.isShiftKeyDown()) {
                pad.dropContents();
                player.displayClientMessage(Component.literal("Missile ejected"), true);
                return InteractionResult.CONSUME;
            }
            if (pad.launch()) {
                level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 1.0F, 0.8F);
                player.displayClientMessage(Component.literal("Missile launched"), true);
            } else {
                player.displayClientMessage(pad.statusMessage(), true);
            }
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(pad.statusMessage(), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LaunchPadBlockEntity pad) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        pad.getItems().getStackInSlot(0));
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
