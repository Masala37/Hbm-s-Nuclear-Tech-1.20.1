package com.hbm.items.tool;

import com.hbm.api.bomb.IBomb;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

import java.util.List;

/**
 * Shift-use to link a bomb position, use in air to detonate (legacy ItemDetonator).
 */
public class DetonatorItem extends Item {
    private static final String TAG_X = "LinkX";
    private static final String TAG_Y = "LinkY";
    private static final String TAG_Z = "LinkZ";
    private static final String TAG_DIM = "LinkDim";

    public DetonatorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.detonator.desc"));
        BlockPos linked = getLinkedPos(stack);
        if (linked == null) {
            tooltip.add(Component.translatable("item.hbm.detonator.unset"));
        } else {
            tooltip.add(Component.translatable("item.hbm.detonator.linked",
                    linked.getX(), linked.getY(), linked.getZ()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        setLinkedPos(stack, pos, level.dimension());

        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("item.hbm.detonator.position_set"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos linked = getLinkedPos(stack);

        if (linked == null) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("item.hbm.detonator.unset"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!isLinkedDimension(stack, level.dimension())) {
            player.displayClientMessage(Component.translatable("item.hbm.detonator.wrong_dimension"), true);
            return InteractionResultHolder.fail(stack);
        }

        BlockState state = level.getBlockState(linked);
        if (state.getBlock() instanceof IBomb bomb) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            IBomb.BombReturnCode result = bomb.explode(level, linked);
            player.displayClientMessage(Component.translatable(result.getMessageKey()), true);
        } else {
            player.displayClientMessage(Component.translatable(IBomb.BombReturnCode.ERROR_NO_BOMB.getMessageKey()), true);
        }

        return InteractionResultHolder.consume(stack);
    }

    @Nullable
    private static BlockPos getLinkedPos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_X) || !tag.contains(TAG_Y) || !tag.contains(TAG_Z)) {
            return null;
        }
        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    private static boolean isLinkedDimension(ItemStack stack, ResourceKey<Level> dimension) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_DIM)) {
            // Legacy links without dim: allow same-world use only if tag missing after upgrade.
            return true;
        }
        ResourceLocation linked = ResourceLocation.tryParse(tag.getString(TAG_DIM));
        return linked != null && linked.equals(dimension.location());
    }

    private static void setLinkedPos(ItemStack stack, BlockPos pos, ResourceKey<Level> dimension) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_X, pos.getX());
        tag.putInt(TAG_Y, pos.getY());
        tag.putInt(TAG_Z, pos.getZ());
        tag.putString(TAG_DIM, dimension.location().toString());
    }
}
