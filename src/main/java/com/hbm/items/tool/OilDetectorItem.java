package com.hbm.items.tool;

import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handheld oil-reservoir scanner (legacy {@code ItemOilDetector}).
 */
public class OilDetectorItem extends Item {
    public OilDetectorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.oil_detector.desc1"));
        tooltip.add(Component.translatable("item.hbm.oil_detector.desc2"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            OilDetectorScan.Hit hit = scan(level, player.getBlockX(), player.getBlockY(), player.getBlockZ());
            Component message = switch (hit) {
                case DIRECT -> Component.translatable("item.hbm.oil_detector.bullseye")
                        .withStyle(ChatFormatting.DARK_GREEN);
                case NEARBY -> Component.translatable("item.hbm.oil_detector.detected")
                        .withStyle(ChatFormatting.GOLD);
                case NONE -> Component.translatable("item.hbm.oil_detector.noOil")
                        .withStyle(ChatFormatting.RED);
            };
            player.displayClientMessage(message, true);
            level.playSound(null, player.blockPosition(), ModSounds.TECH_BLEEP.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static OilDetectorScan.Hit scan(Level level, int x, int y, int z) {
        return OilDetectorScan.scan((px, py, pz) -> {
            if (!level.isInWorldBounds(new BlockPos(px, py, pz))) {
                return false;
            }
            BlockState state = level.getBlockState(new BlockPos(px, py, pz));
            return state.is(ModBlocks.ORE_OIL.get());
        }, x, y, z);
    }
}
