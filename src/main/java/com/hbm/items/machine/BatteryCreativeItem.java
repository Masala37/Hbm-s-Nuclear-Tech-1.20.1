package com.hbm.items.machine;

import com.hbm.energy.InfiniteEnergyStorage;
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
 * Legacy {@code battery_creative} — infinite Forge Energy source for machines that accept item FE.
 */
public class BatteryCreativeItem extends Item {
    public BatteryCreativeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final InfiniteEnergyStorage storage = new InfiniteEnergyStorage();
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Infinite FE — creative power source"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
