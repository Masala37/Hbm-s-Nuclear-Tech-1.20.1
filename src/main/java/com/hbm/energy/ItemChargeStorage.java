package com.hbm.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.LongSupplier;

/**
 * Long HE/FE stored on an item as NBT {@code charge}, exposed as Forge Energy 1:1.
 * Per-call transfer is capped to the 1.7 charge/discharge rates.
 * Capacity and rates are read live so {@code battery_pack} type NBT set after
 * {@code ItemStack} construction still applies.
 */
public final class ItemChargeStorage implements IEnergyStorage {
    public static final String CHARGE = "charge";

    private final ItemStack stack;
    private final LongSupplier maxCharge;
    private final LongSupplier chargeRate;
    private final LongSupplier dischargeRate;
    private final long defaultCharge;

    public ItemChargeStorage(ItemStack stack, long maxCharge, long chargeRate, long dischargeRate, long defaultCharge) {
        this(stack, () -> maxCharge, () -> chargeRate, () -> dischargeRate, defaultCharge);
    }

    public ItemChargeStorage(ItemStack stack, LongSupplier maxCharge, LongSupplier chargeRate,
                             LongSupplier dischargeRate, long defaultCharge) {
        this.stack = stack;
        this.maxCharge = maxCharge;
        this.chargeRate = chargeRate;
        this.dischargeRate = dischargeRate;
        this.defaultCharge = Math.max(0, defaultCharge);
    }

    public static long read(ItemStack stack, long defaultCharge) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CHARGE)) {
            return defaultCharge;
        }
        return tag.getLong(CHARGE);
    }

    public static void write(ItemStack stack, long charge, long maxCharge) {
        stack.getOrCreateTag().putLong(CHARGE, Math.max(0L, Math.min(maxCharge, charge)));
    }

    private long max() {
        return Math.max(0L, maxCharge.getAsLong());
    }

    private long chargeRate() {
        return Math.max(0L, chargeRate.getAsLong());
    }

    private long dischargeRate() {
        return Math.max(0L, dischargeRate.getAsLong());
    }

    public long getCharge() {
        long cap = max();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CHARGE)) {
            long value = Math.min(cap, defaultCharge);
            write(stack, value, cap);
            return value;
        }
        return Math.max(0L, Math.min(cap, tag.getLong(CHARGE)));
    }

    public void setCharge(long charge) {
        write(stack, charge, max());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long rate = chargeRate();
        if (rate <= 0 || maxReceive <= 0) {
            return 0;
        }
        long room = max() - getCharge();
        long moved = Math.min(Math.min(maxReceive, rate), room);
        if (moved <= 0) {
            return 0;
        }
        if (!simulate) {
            setCharge(getCharge() + moved);
        }
        return (int) moved;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        long rate = dischargeRate();
        if (rate <= 0 || maxExtract <= 0) {
            return 0;
        }
        long have = getCharge();
        long moved = Math.min(Math.min(maxExtract, rate), have);
        if (moved <= 0) {
            return 0;
        }
        if (!simulate) {
            setCharge(have - moved);
        }
        return (int) moved;
    }

    @Override
    public int getEnergyStored() {
        return toInt(getCharge());
    }

    @Override
    public int getMaxEnergyStored() {
        return toInt(max());
    }

    @Override
    public boolean canExtract() {
        return dischargeRate() > 0 && getCharge() > 0;
    }

    @Override
    public boolean canReceive() {
        return chargeRate() > 0 && getCharge() < max();
    }

    public static int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    public static String shortNumber(long value) {
        long v = Math.abs(value);
        String sign = value < 0 ? "-" : "";
        if (v < 1_000L) {
            return sign + v;
        }
        if (v < 1_000_000L) {
            return sign + (v / 1_000L) + "k";
        }
        if (v < 1_000_000_000L) {
            return sign + (v / 1_000_000L) + "M";
        }
        if (v < 1_000_000_000_000L) {
            return sign + (v / 1_000_000_000L) + "G";
        }
        return sign + (v / 1_000_000_000_000L) + "T";
    }
}
