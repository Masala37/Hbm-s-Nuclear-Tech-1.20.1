package com.hbm.energy;

import com.hbm.items.machine.BatteryCreativeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Drain FE from an inventory battery item into a machine buffer.
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

    public static boolean isEnergyItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof BatteryCreativeItem) {
            return true;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canExtract).orElse(false);
    }
}
