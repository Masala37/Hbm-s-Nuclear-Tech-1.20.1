package com.hbm.items.tool;

import com.hbm.blocks.machine.LaunchPadBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Missile designator — RMB on a block stores target coordinates (NBT keys x/y/z).
 * RMB on a launch pad with a stored target lets the pad claim the click to program itself.
 */
public class DesignatorItem extends Item {
    public DesignatorItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static boolean hasTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("x") && tag.contains("y") && tag.contains("z");
    }

    public static BlockPos getTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return BlockPos.ZERO;
        }
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public static void setTarget(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);

        // Already programmed: let LaunchPadBlock.use transfer coords to the pad
        if (state.getBlock() instanceof LaunchPadBlock && hasTarget(stack)) {
            return InteractionResult.PASS;
        }

        setTarget(stack, pos);
        if (!level.isClientSide) {
            Player player = context.getPlayer();
            level.playSound(null, pos, com.hbm.registry.ModSounds.TECH_BLEEP.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
            if (player != null) {
                player.displayClientMessage(Component.literal("Target set: " + pos.getX() + ", "
                        + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (hasTarget(stack)) {
            BlockPos pos = getTarget(stack);
            tooltip.add(Component.literal("Target: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("Right-click a block to set target")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
