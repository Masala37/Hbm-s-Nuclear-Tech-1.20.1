package com.hbm.items.tool;

import com.hbm.entity.logic.EntityBomber;
import com.hbm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Airstrike caller (legacy {@code ItemBombCaller}) — long-range aim like designator (500 blocks).
 */
public class BombCallerItem extends Item {
    public static final String TAG_TYPE = "StrikeType";
    /** Legacy {@code Library.rayTrace(player, 500, 1)}. */
    private static final double CALL_RANGE = 500.0D;

    public enum StrikeType {
        CARPET(0, "Carpet bombing"),
        NAPALM(1, "Napalm"),
        POISON(2, "Poison gas"),
        ORANGE(3, "Agent orange"),
        ATOMIC(4, "Atomic bomb");

        public final int id;
        public final String label;

        StrikeType(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public static StrikeType byId(int id) {
            for (StrikeType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return CARPET;
        }
    }

    public BombCallerItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static ItemStack stack(StrikeType type) {
        ItemStack stack = new ItemStack(com.hbm.registry.ModItems.BOMB_CALLER.get());
        setType(stack, type);
        return stack;
    }

    public static StrikeType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return StrikeType.CARPET;
        }
        return StrikeType.byId(tag.getInt(TAG_TYPE));
    }

    public static void setType(ItemStack stack, StrikeType type) {
        stack.getOrCreateTag().putInt(TAG_TYPE, type.id);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = longRangeClip(level, player);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = hit.getBlockPos();
        if (!level.isClientSide) {
            StrikeType type = getType(stack);
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 1.0D;
            double z = pos.getZ() + 0.5D;
            callStrike(level, type, x, y, z);
            player.displayClientMessage(Component.literal("Called in airstrike!"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Legacy long designator-style ray (500 blocks), not vanilla reach. */
    private static BlockHitResult longRangeClip(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * CALL_RANGE, look.y * CALL_RANGE, look.z * CALL_RANGE);
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private static void callStrike(Level level, StrikeType type, double x, double y, double z) {
        EntityBomber bomber = switch (type) {
            case NAPALM -> EntityBomber.statFacNapalm(level, x, y, z);
            case POISON -> EntityBomber.statFacChlorine(level, x, y, z);
            case ORANGE -> EntityBomber.statFacOrange(level, x, y, z);
            case ATOMIC -> EntityBomber.statFacABomb(level, x, y, z);
            default -> EntityBomber.statFacCarpet(level, x, y, z);
        };
        level.addFreshEntity(bomber);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Aim at a distant block & click to call an airstrike!")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 500 blocks").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Type: " + getType(stack).label)
                .withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getType(stack).id >= StrikeType.ATOMIC.id;
    }
}
