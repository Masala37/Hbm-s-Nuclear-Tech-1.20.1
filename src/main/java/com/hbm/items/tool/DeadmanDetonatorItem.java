package com.hbm.items.tool;

import com.hbm.api.bomb.IBomb;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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
 * Dead man's switch — link a bomb, drop the item to detonate (legacy {@code ItemDrop} deadman).
 */
public class DeadmanDetonatorItem extends Item {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";

    public DeadmanDetonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.detonator_deadman.desc1"));
        tooltip.add(Component.translatable("item.hbm.detonator_deadman.desc2"));
        BlockPos linked = getLinkedPos(stack);
        if (linked == null) {
            tooltip.add(Component.translatable("item.hbm.detonator.unset").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("item.hbm.detonator.linked",
                    linked.getX(), linked.getY(), linked.getZ()).withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable("trait.hbm.drop").withStyle(ChatFormatting.RED));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());

        Level level = context.getLevel();
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("item.hbm.detonator.position_set"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();
        if (level.isClientSide) {
            return false;
        }

        BlockPos linked = getLinkedPos(stack);
        if (linked != null) {
            BlockState state = level.getBlockState(linked);
            if (state.getBlock() instanceof IBomb bomb) {
                bomb.explode(level, linked);
            }
        }
        level.explode(entity, entity.getX(), entity.getY(), entity.getZ(), 0.0F, false, Level.ExplosionInteraction.NONE);
        entity.discard();
        return true;
    }

    @Nullable
    private static BlockPos getLinkedPos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_X) || !tag.contains(TAG_Y) || !tag.contains(TAG_Z)) {
            return null;
        }
        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }
}
