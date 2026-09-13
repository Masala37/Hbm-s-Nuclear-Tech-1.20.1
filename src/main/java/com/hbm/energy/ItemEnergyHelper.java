package com.hbm.energy;

import com.hbm.items.machine.BatteryCreativeItem;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.machine.ItemBatteryPack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Move FE between inventory battery items and machine buffers.
 */
public final class ItemEnergyHelper {
    private ItemEnergyHelper() {
    }

    /**
     * @return FE transferred this call
     */
    public static int chargeFromItem(ItemStack stack, IEnergyStorage sink, int maxTransfer) {
        if (stack.isEmpty() || maxTransfer <= 0 || sink.getEnergyStored() >= sink.getMaxEnergyStored()) {
            return 0;
        }
        if (stack.getItem() instanceof BatteryCreativeItem) {
            int room = sink.getMaxEnergyStored() - sink.getEnergyStored();
            if (room <= 0) {
                return 0;
            }
            if (sink instanceof ModEnergyStorage mod) {
                mod.setEnergy(sink.getMaxEnergyStored());
            } else {
                sink.receiveEnergy(room, false);
            }
            return room;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).map(source -> {
            if (!source.canExtract()) {
                return 0;
            }
            int want = Math.min(maxTransfer, sink.getMaxEnergyStored() - sink.getEnergyStored());
            int available = source.extractEnergy(want, true);
            if (available <= 0) {
                return 0;
            }
            int accepted = sink.receiveEnergy(available, false);
            if (accepted > 0) {
                source.extractEnergy(accepted, false);
            }
            return accepted;
        }).orElse(0);
    }

    /**
     * 1.7 {@code Library.chargeTEFromItems}: dump an item into a buffer, ignoring cable extract caps.
     */
    public static int dischargeItemIntoBuffer(ItemStack stack, ModEnergyStorage sink) {
        if (stack.isEmpty() || sink.getEnergyStored() >= sink.getMaxEnergyStored()) {
            return 0;
        }
        if (stack.getItem() instanceof BatteryCreativeItem) {
            int room = sink.getMaxEnergyStored() - sink.getEnergyStored();
            sink.setEnergy(sink.getMaxEnergyStored());
            return room;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).map(source -> {
            if (!source.canExtract()) {
                return 0;
            }
            int room = sink.getMaxEnergyStored() - sink.getEnergyStored();
            int extracted = source.extractEnergy(room, false);
            if (extracted > 0) {
                sink.setEnergy(sink.getEnergyStored() + extracted);
            }
            return extracted;
        }).orElse(0);
    }

    /**
     * 1.7 {@code Library.chargeItemsFromTE}: fill an item from a buffer, ignoring cable extract caps.
     */
    public static int chargeItemFromBuffer(ItemStack stack, ModEnergyStorage source) {
        if (stack.isEmpty() || source.getEnergyStored() <= 0 || stack.getItem() instanceof BatteryCreativeItem) {
            return 0;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).map(sink -> {
            if (!sink.canReceive()) {
                return 0;
            }
            int want = Math.min(source.getEnergyStored(), Integer.MAX_VALUE);
            int accepted = sink.receiveEnergy(want, false);
            if (accepted > 0) {
                source.setEnergy(source.getEnergyStored() - accepted);
            }
            return accepted;
        }).orElse(0);
    }

    public static boolean isEnergyItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof BatteryCreativeItem
                || stack.getItem() instanceof ItemBattery
                || stack.getItem() instanceof ItemBatteryPack) {
            return true;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
    }

    public static boolean isDrained(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof BatteryCreativeItem) {
            return false;
        }
        return storedCharge(stack) <= 0L;
    }

    public static boolean isFilled(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof BatteryCreativeItem) {
            return false;
        }
        long max = maxCharge(stack);
        return max > 0L && storedCharge(stack) >= max;
    }

    public static long storedCharge(ItemStack stack) {
        if (stack.getItem() instanceof ItemBattery battery) {
            return battery.getCharge(stack);
        }
        if (stack.getItem() instanceof ItemBatteryPack) {
            return ItemBatteryPack.getCharge(stack);
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).map(s -> (long) s.getEnergyStored()).orElse(0L);
    }

    public static long maxCharge(ItemStack stack) {
        if (stack.getItem() instanceof ItemBattery battery) {
            return battery.getMaxCharge();
        }
        if (stack.getItem() instanceof ItemBatteryPack) {
            return ItemBatteryPack.getType(stack).capacity();
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).map(s -> (long) s.getMaxEnergyStored()).orElse(0L);
    }
}
