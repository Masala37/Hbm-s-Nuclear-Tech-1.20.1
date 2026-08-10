package com.hbm.items.tool;

import com.hbm.api.bomb.IBomb;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Multi-position detonator (legacy {@code ItemMultiDetonator}).
 */
public class MultiDetonatorItem extends Item {
    private static final String TAG_X = "xValues";
    private static final String TAG_Y = "yValues";
    private static final String TAG_Z = "zValues";

    public MultiDetonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.detonator_multi.desc1"));
        tooltip.add(Component.translatable("item.hbm.detonator_multi.desc2"));
        tooltip.add(Component.translatable("item.hbm.detonator_multi.desc3"));
        int[][] locs = getLocations(stack);
        if (locs == null) {
            tooltip.add(Component.translatable("item.hbm.detonator.unset").withStyle(ChatFormatting.RED));
        } else {
            for (int i = 0; i < locs[0].length; i++) {
                tooltip.add(Component.literal(locs[0][i] + " / " + locs[1][i] + " / " + locs[2][i])
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        addLocation(stack, pos);

        Level level = context.getLevel();
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("item.hbm.detonator_multi.added"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int[][] locs = getLocations(stack);

        if (locs == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.hbm.detonator.unset"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            clearLocations(stack);
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.hbm.detonator_multi.cleared"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        int succ = 0;
        for (int i = 0; i < locs[0].length; i++) {
            BlockPos pos = new BlockPos(locs[0][i], locs[1][i], locs[2][i]);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof IBomb bomb) {
                IBomb.BombReturnCode result = bomb.explode(level, pos);
                if (result.wasSuccessful()) {
                    succ++;
                }
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("item.hbm.detonator_multi.triggered", succ, locs[0].length), true);
        return InteractionResultHolder.consume(stack);
    }

    private static void addLocation(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrCreateTag();
        int[] xs = tag.getIntArray(TAG_X);
        int[] ys = tag.getIntArray(TAG_Y);
        int[] zs = tag.getIntArray(TAG_Z);
        tag.putIntArray(TAG_X, append(xs, pos.getX()));
        tag.putIntArray(TAG_Y, append(ys, pos.getY()));
        tag.putIntArray(TAG_Z, append(zs, pos.getZ()));
    }

    private static void clearLocations(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putIntArray(TAG_X, new int[0]);
        tag.putIntArray(TAG_Y, new int[0]);
        tag.putIntArray(TAG_Z, new int[0]);
    }

    @Nullable
    private static int[][] getLocations(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        int[] xs = tag.getIntArray(TAG_X);
        int[] ys = tag.getIntArray(TAG_Y);
        int[] zs = tag.getIntArray(TAG_Z);
        if (xs.length == 0 || ys.length == 0 || zs.length == 0
                || xs.length != ys.length || xs.length != zs.length) {
            return null;
        }
        return new int[][]{xs, ys, zs};
    }

    private static int[] append(int[] src, int value) {
        int[] out = Arrays.copyOf(src, src.length + 1);
        out[src.length] = value;
        return out;
    }
}
