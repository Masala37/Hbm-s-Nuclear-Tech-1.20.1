package com.hbm.items.machine;

import com.hbm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 {@code ItemPotatos}: charged potato battery that talks while held.
 */
public class ItemPotatos extends ItemBattery {
    public ItemPotatos(long maxCharge, long chargeRate, long dischargeRate) {
        super(maxCharge, chargeRate, dischargeRate, true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || getCharge(stack) <= 0L) {
            return;
        }
        int timer = getTimer(stack);
        if (timer > 0) {
            setTimer(stack, timer - 1);
            return;
        }
        if (entity instanceof Player && isSelected) {
            float pitch = (float) getCharge(stack) / (float) getMaxCharge() * 0.5F + 0.5F;
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.require("potatos.random"), SoundSource.PLAYERS, 1.0F, pitch);
            setTimer(stack, 200 + level.random.nextInt(100));
        }
    }

    private static int getTimer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt("timer");
    }

    private static void setTimer(ItemStack stack, int timer) {
        stack.getOrCreateTag().putInt("timer", Math.max(0, timer));
    }
}
