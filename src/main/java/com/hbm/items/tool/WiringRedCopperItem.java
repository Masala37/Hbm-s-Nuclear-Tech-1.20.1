package com.hbm.items.tool;

import com.hbm.blockentity.network.PylonBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.energy.PylonLinks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code ItemWiring}: two-click pylon links.
 */
public class WiringRedCopperItem extends Item {
    public WiringRedCopperItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = corePos(level, context.getClickedPos());
        BlockEntity te = level.getBlockEntity(pos);
        if (!(te instanceof PylonBlockEntity)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("x")) {
            CompoundTag start = new CompoundTag();
            start.putInt("x", pos.getX());
            start.putInt("y", pos.getY());
            start.putInt("z", pos.getZ());
            stack.setTag(start);
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(Component.literal("Wire start"), false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos firstPos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        BlockEntity firstTe = level.getBlockEntity(firstPos);
        if (firstTe instanceof PylonBlockEntity first && te instanceof PylonBlockEntity second) {
            switch (first.canConnectTo(second)) {
                case PylonLinks.OK -> {
                    first.addConnection(pos);
                    second.addConnection(firstPos);
                    if (player != null) {
                        player.displayClientMessage(Component.literal("Wire end"), false);
                    }
                }
                case PylonLinks.TYPE -> {
                    if (player != null) {
                        player.displayClientMessage(Component.literal("Wire error - Pylons are not the same type"), false);
                    }
                }
                case PylonLinks.SAME -> {
                    if (player != null) {
                        player.displayClientMessage(Component.literal("Wire error - Cannot connect to the same pylon"), false);
                    }
                }
                case PylonLinks.FAR -> {
                    if (player != null) {
                        player.displayClientMessage(Component.literal("Wire error - Pylon is too far away"), false);
                    }
                }
                default -> {
                }
            }
        } else if (player != null) {
            player.displayClientMessage(Component.literal("Wire error"), false);
        }
        stack.setTag(null);
        return InteractionResult.CONSUME;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("x")) {
            return;
        }
        double dx = entity.getX() - tag.getInt("x");
        double dy = entity.getY() - tag.getInt("y");
        double dz = entity.getZ() - tag.getInt("z");
        int meters = (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
        player.displayClientMessage(Component.literal(stack.getHoverName().getString() + ": " + meters + "m"), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("x")) {
            tooltip.add(Component.literal("Wire start x: " + tag.getInt("x")).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Wire start y: " + tag.getInt("y")).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Wire start z: " + tag.getInt("z")).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Right-click poles to connect").withStyle(ChatFormatting.GRAY));
        }
    }

    private static BlockPos corePos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockDummyable dummyable) {
            BlockPos core = dummyable.findCore(level, pos);
            if (core != null) {
                return core;
            }
        }
        return pos;
    }
}
