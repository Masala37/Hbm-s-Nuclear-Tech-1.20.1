package com.hbm.items.machine;

import com.hbm.energy.ItemChargeStorage;
import com.hbm.registry.ModItems;
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
import java.util.Locale;

/**
 * 1.7.10 {@code ItemBatteryPack}: one item, enum variants via NBT {@code type}.
 */
public class ItemBatteryPack extends Item {
    public static final String TAG_TYPE = "type";

    public enum Pack {
        BATTERY_REDSTONE("battery_redstone", 100L, false),
        BATTERY_LEAD("battery_lead", 1_000L, false),
        BATTERY_LITHIUM("battery_lithium", 10_000L, false),
        BATTERY_SODIUM("battery_sodium", 50_000L, false),
        BATTERY_SCHRABIDIUM("battery_schrabidium", 250_000L, false),
        BATTERY_QUANTUM("battery_quantum", 1_000_000L, 20 * 60 * 60),

        CAPACITOR_COPPER("capacitor_copper", 1_000L, true),
        CAPACITOR_GOLD("capacitor_gold", 10_000L, true),
        CAPACITOR_NIOBIUM("capacitor_niobium", 100_000L, true),
        CAPACITOR_TANTALUM("capacitor_tantalum", 500_000L, true),
        CAPACITOR_BISMUTH("capacitor_bismuth", 2_500_000L, true),
        CAPACITOR_SPARK("capacitor_spark", 10_000_000L, true);

        private final String langKey;
        private final long capacity;
        private final long chargeRate;
        private final long dischargeRate;

        Pack(String langKey, long dischargeRate, boolean capacitor) {
            this(langKey,
                    capacitor ? dischargeRate * 20L * 30L : dischargeRate * 20L * 60L * 15L,
                    capacitor ? dischargeRate : dischargeRate * 10L,
                    dischargeRate);
        }

        Pack(String langKey, long dischargeRate, long durationTicks) {
            this(langKey, dischargeRate * durationTicks, dischargeRate * 10L, dischargeRate);
        }

        Pack(String langKey, long capacity, long chargeRate, long dischargeRate) {
            this.langKey = langKey;
            this.capacity = capacity;
            this.chargeRate = chargeRate;
            this.dischargeRate = dischargeRate;
        }

        public String langKey() {
            return langKey;
        }

        public long capacity() {
            return capacity;
        }

        public long chargeRate() {
            return chargeRate;
        }

        public long dischargeRate() {
            return dischargeRate;
        }

        public boolean capacitor() {
            return ordinal() > BATTERY_QUANTUM.ordinal();
        }

        public static Pack byName(String name) {
            if (name == null || name.isEmpty()) {
                return BATTERY_REDSTONE;
            }
            try {
                return Pack.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                for (Pack pack : values()) {
                    if (pack.langKey.equalsIgnoreCase(name)) {
                        return pack;
                    }
                }
                return BATTERY_REDSTONE;
            }
        }

        public static Pack byOrdinal(int ordinal) {
            Pack[] values = values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
            return BATTERY_REDSTONE;
        }
    }

    public ItemBatteryPack() {
        super(new Properties().stacksTo(1));
    }

    public static ItemStack stack(Pack pack, boolean full) {
        ItemStack stack = new ItemStack(ModItems.BATTERY_PACK.get());
        setType(stack, pack);
        ItemChargeStorage.write(stack, full ? pack.capacity() : 0L, pack.capacity());
        return stack;
    }

    public static void setType(ItemStack stack, Pack pack) {
        stack.getOrCreateTag().putString(TAG_TYPE, pack.name());
    }

    public static Pack getType(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemBatteryPack)) {
            return Pack.BATTERY_REDSTONE;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return Pack.BATTERY_REDSTONE;
        }
        if (tag.contains(TAG_TYPE)) {
            return Pack.byName(tag.getString(TAG_TYPE));
        }
        if (tag.contains("Damage")) {
            return Pack.byOrdinal(tag.getInt("Damage"));
        }
        return Pack.BATTERY_REDSTONE;
    }

    public static long getCharge(ItemStack stack) {
        Pack pack = getType(stack);
        return ItemChargeStorage.read(stack, 0L);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.hbm.battery_pack." + getType(stack).langKey());
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final ItemChargeStorage storage = new ItemChargeStorage(
                    stack,
                    () -> getType(stack).capacity(),
                    () -> getType(stack).chargeRate(),
                    () -> getType(stack).dischargeRate(),
                    0L);
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
        Pack pack = getType(stack);
        return pack.capacity() > 0 && getCharge(stack) != pack.capacity();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        Pack pack = getType(stack);
        if (pack.capacity() <= 0) {
            return 0;
        }
        return Math.round(13.0F * (float) ((double) getCharge(stack) / (double) pack.capacity()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Pack pack = getType(stack);
        float fill = pack.capacity() <= 0 ? 0.0F : (float) ((double) getCharge(stack) / (double) pack.capacity());
        return net.minecraft.util.Mth.hsvToRgb(Math.max(0.0F, fill) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Pack pack = getType(stack);
        long charge = getCharge(stack);
        double percent = pack.capacity() <= 0 ? 0.0 : (charge * 1000L / pack.capacity()) / 10.0;
        tooltip.add(Component.literal("Energy stored: " + ItemChargeStorage.shortNumber(charge)
                + "/" + ItemChargeStorage.shortNumber(pack.capacity()) + "FE (" + percent + "%)"));
        tooltip.add(Component.literal("Charge rate: " + ItemChargeStorage.shortNumber(pack.chargeRate()) + "FE/t"));
        tooltip.add(Component.literal("Discharge rate: " + ItemChargeStorage.shortNumber(pack.dischargeRate()) + "FE/t"));
        if (pack.chargeRate() > 0) {
            tooltip.add(Component.literal("Time for full charge: "
                    + (pack.capacity() / pack.chargeRate() / 20 / 60.0) + "min"));
        }
        if (pack.dischargeRate() > 0) {
            tooltip.add(Component.literal("Charge lasts for: "
                    + (pack.capacity() / pack.dischargeRate() / 20 / 60.0) + "min"));
        }
    }
}
