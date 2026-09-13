package com.hbm.items.tool;

import com.hbm.blocks.network.ConveyorBlock;
import com.hbm.conveyor.ConveyorPathBuilder;
import com.hbm.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code ItemConveyorWand} (REGULAR). Express/double/triple and lift/chute are later.
 */
public class ConveyorWandItem extends Item {
    public ConveyorWandItem() {
        super(new Item.Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.conveyor_wand.desc").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.hbm.conveyor_wand.desc.place").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.hbm.conveyor_wand.desc.sneak").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.hbm.conveyor_wand.desc.screw").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!selected && stack.hasTag()) {
            if (entity instanceof Player player) {
                ItemStack held = player.getMainHandItem();
                if (held.getItem() != this) {
                    stack.setTag(null);
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && !stack.hasTag()) {
            BlockPos place = clicked.relative(side);
            if (ConveyorPathBuilder.replaceable(level, place)) {
                if (!level.isClientSide) {
                    BlockState placed = ModBlocks.CONVEYOR.get().defaultBlockState()
                            .setValue(ConveyorBlock.FACING, player.getDirection().getOpposite());
                    level.setBlock(place, placed, 3);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.FAIL;
        }

        BlockState on = level.getBlockState(clicked);
        if (on.getBlock() instanceof ConveyorBlock) {
            Direction move = stack.hasTag() ? ConveyorBlock.input(on) : ConveyorBlock.output(on);
            BlockPos snap = clicked.relative(move);
            if (ConveyorPathBuilder.replaceable(level, snap)) {
                side = move;
            }
        }

        if (!stack.hasTag()) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("x", clicked.getX());
            nbt.putInt("y", clicked.getY());
            nbt.putInt("z", clicked.getZ());
            nbt.putInt("side", side.get3DDataValue());
            nbt.putInt("count", player.getAbilities().instabuild ? 256 : countWands(player, stack));
            stack.setTag(nbt);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        CompoundTag nbt = stack.getTag();
        BlockPos start = new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
        Direction startFace = Direction.from3DDataValue(nbt.getInt("side"));
        int count = nbt.getInt("count");
        stack.setTag(null);

        if (!level.isClientSide) {
            int preview = ConveyorPathBuilder.construct(level, false, player, start, startFace, clicked, side, count);
            if (preview > 0) {
                int used = ConveyorPathBuilder.construct(level, true, player, start, startFace, clicked, side, count);
                if (!player.getAbilities().instabuild) {
                    consumeWands(player, used);
                }
                player.displayClientMessage(Component.literal("Conveyor built!"), true);
            } else if (preview == 0) {
                player.displayClientMessage(Component.literal("Not enough conveyors, build cancelled"), true);
            } else {
                player.displayClientMessage(Component.literal("Conveyor obstructed, build cancelled"), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static int countWands(Player player, ItemStack used) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack inv = player.getInventory().getItem(i);
            if (inv.getItem() == used.getItem()) {
                count += inv.getCount();
            }
        }
        return count;
    }

    private static void consumeWands(Player player, int toRemove) {
        int remaining = toRemove;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack inv = player.getInventory().getItem(i);
            if (inv.getItem() instanceof ConveyorWandItem) {
                int take = Math.min(remaining, inv.getCount());
                inv.shrink(take);
                remaining -= take;
            }
        }
    }
}
