package com.hbm.items.tool;

import com.hbm.blocks.machine.RadarCores;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.registry.ModSounds;
import com.hbm.tileentity.IRadarCommandReceiver;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadarLinkerItem extends Item {
    public RadarLinkerItem() {
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
        BlockEntity core = RadarCores.core(level, pos);
        if (!(core instanceof IRadarCommandReceiver) && !(core instanceof RadarScreenBlockEntity)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        setTarget(stack, core.getBlockPos());
        if (!level.isClientSide) {
            Player player = context.getPlayer();
            level.playSound(null, pos, ModSounds.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            if (player != null) {
                BlockPos t = core.getBlockPos();
                player.displayClientMessage(Component.literal("Linked: " + t.getX() + ", " + t.getY() + ", " + t.getZ())
                        .withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (hasTarget(stack)) {
            BlockPos pos = getTarget(stack);
            tooltip.add(Component.literal("Position: " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ())
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("Right-click a launch pad or radar screen")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
