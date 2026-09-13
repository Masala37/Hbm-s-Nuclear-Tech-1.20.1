package com.hbm.items.machine;

import com.hbm.energy.ItemChargeStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code ItemBattery}: long HE stored as FE 1:1.
 */
public class ItemBattery extends Item {
    private final long maxCharge;
    private final long chargeRate;
    private final long dischargeRate;
    private final boolean defaultFull;

    public ItemBattery(long maxCharge, long chargeRate, long dischargeRate) {
        this(maxCharge, chargeRate, dischargeRate, true);
    }

    public ItemBattery(long maxCharge, long chargeRate, long dischargeRate, boolean defaultFull) {
        super(new Properties().stacksTo(1));
        this.maxCharge = maxCharge;
        this.chargeRate = chargeRate;
        this.dischargeRate = dischargeRate;
        this.defaultFull = defaultFull;
    }

    public long getMaxCharge() {
        return maxCharge;
    }

    public long getChargeRate() {
        return chargeRate;
    }

    public long getDischargeRate() {
        return dischargeRate;
    }

    public long defaultCharge() {
        return defaultFull ? maxCharge : 0L;
    }

    public long getCharge(ItemStack stack) {
        return ItemChargeStorage.read(stack, defaultCharge());
    }

    public static ItemStack withCharge(ItemStack stack, long charge) {
        if (stack.getItem() instanceof ItemBattery battery) {
            ItemChargeStorage.write(stack, charge, battery.maxCharge);
        }
        return stack;
    }

    public static ItemStack full(Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof ItemBattery battery) {
            ItemChargeStorage.write(stack, battery.maxCharge, battery.maxCharge);
        }
        return stack;
    }

    public static ItemStack empty(Item item) {
        ItemStack stack = new ItemStack(item);
        if (item instanceof ItemBattery battery) {
            ItemChargeStorage.write(stack, 0L, battery.maxCharge);
        }
        return stack;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemChargeStorage storage = new ItemChargeStorage(
                    stack, maxCharge, chargeRate, dischargeRate, defaultCharge());
            private final LazyOptional<IEnergyStorage> optional = LazyOptional.of(() -> storage);

            @Override
            public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                                     @Nullable net.minecraft.core.Direction side) {
                if (cap == ForgeCapabilities.ENERGY) {
                    return optional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (maxCharge <= 0) {
            return 0;
        }
        return Math.round(13.0F * (float) ((double) getCharge(stack) / (double) maxCharge));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float fill = maxCharge <= 0 ? 0.0F : (float) ((double) getCharge(stack) / (double) maxCharge);
        return net.minecraft.util.Mth.hsvToRgb(Math.max(0.0F, fill) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long charge = getCharge(stack);
        tooltip.add(Component.literal("Energy stored: " + ItemChargeStorage.shortNumber(charge)
                + "/" + ItemChargeStorage.shortNumber(maxCharge) + "FE"));
        tooltip.add(Component.literal("Charge rate: " + ItemChargeStorage.shortNumber(chargeRate) + "FE/t"));
        tooltip.add(Component.literal("Discharge rate: " + ItemChargeStorage.shortNumber(dischargeRate) + "FE/t"));
    }
}
