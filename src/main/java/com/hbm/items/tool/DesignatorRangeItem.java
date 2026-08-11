package com.hbm.items.tool;

import com.hbm.blocks.machine.LaunchPadBlock;
import com.hbm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Legacy {@code ItemDesingatorRange} — air-use raycast 300 blocks to set target.
 */
public class DesignatorRangeItem extends DesignatorItem {
    private static final double RANGE = 300.0D;

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = longRangeClip(level, player);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = hit.getBlockPos();
        if (level.getBlockState(pos).getBlock() instanceof LaunchPadBlock) {
            // Let pad claim the click when already programmed / for programming via useOn.
            return InteractionResultHolder.pass(stack);
        }

        setTarget(stack, pos);
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            player.displayClientMessage(Component.literal("Position set to X:" + pos.getX()
                    + ", Z:" + pos.getZ()).withStyle(ChatFormatting.YELLOW), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Prefer air-use ray for long range; only pass through to pad when targeting a pad.
        if (context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof LaunchPadBlock
                && hasTarget(context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        // Short click on nearby block still works as fallback (same NBT as designator).
        return super.useOn(context);
    }

    private static BlockHitResult longRangeClip(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * RANGE, look.y * RANGE, look.z * RANGE);
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Long-range targeting (300 blocks)")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
