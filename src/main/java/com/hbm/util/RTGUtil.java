package com.hbm.util;

import com.hbm.config.MachineConfig;
import com.hbm.interfaces.HalfLifeType;
import com.hbm.items.machine.ItemRTGPellet;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * 1.7.10 {@code RTGUtil}: pellet heat sum, decay, and half-life → tick lifespan.
 */
public final class RTGUtil {
    private RTGUtil() {
    }

    public static short getPower(ItemRTGPellet fuel, ItemStack stack) {
        return MachineConfig.scaleRTGPower() ? ItemRTGPellet.getScaledPower(fuel, stack) : fuel.getHeat();
    }

    public static boolean hasHeat(IItemHandlerModifiable inventory, int[] rtgSlots) {
        for (int slot : rtgSlots) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemRTGPellet) {
                return true;
            }
        }
        return false;
    }

    public static int updateRTGs(IItemHandlerModifiable inventory, int[] rtgSlots) {
        int heat = 0;
        for (int slot : rtgSlots) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemRTGPellet pellet)) {
                continue;
            }
            heat += getPower(pellet, stack);
            inventory.setStackInSlot(slot, ItemRTGPellet.handleDecay(stack, pellet));
        }
        return heat;
    }

    /**
     * Half-life converted to Minecraft ticks.
     *
     * @param halfLife  half-life magnitude
     * @param type      days / years / hundreds of years
     * @param realYears 365 days per year instead of NTM's 100
     */
    public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
        float life = 0.0F;
        switch (type) {
            case LONG -> life = (48000 * (realYears ? 365 : 100) * 100) * halfLife;
            case MEDIUM -> life = (48000 * (realYears ? 365 : 100)) * halfLife;
            case SHORT -> life = 48000 * halfLife;
        }
        return (long) life;
    }

    /** 1.7 {@code BobMathUtil.ticksToDate}: year / day / 0–10 hour scale. */
    public static String[] ticksToDate(long ticks) {
        int tickDay = 48000;
        int tickYear = tickDay * 100;
        long year = Math.floorDiv(ticks, tickYear);
        byte day = (byte) Math.floorDiv(ticks - tickYear * year, tickDay);
        float time = ticks - (tickYear * year + tickDay * day);
        time = (float) convertScale(time, 0, tickDay, 0, 10.0F);
        return new String[] {String.valueOf(year), String.valueOf(day), String.valueOf(time)};
    }

    public static double convertScale(double toScale, double oldMin, double oldMax, double newMin, double newMax) {
        double prevRange = oldMax - oldMin;
        if (prevRange == 0.0D) {
            return newMin;
        }
        double newRange = newMax - newMin;
        return (((toScale - oldMin) * newRange) / prevRange) + newMin;
    }
}
