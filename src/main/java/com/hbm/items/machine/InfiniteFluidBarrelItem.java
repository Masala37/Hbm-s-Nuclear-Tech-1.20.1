package com.hbm.items.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Legacy {@code ItemInfiniteFluid} / {@code fluid_barrel_infinite} —
 * never empties; can fill any accepting tank.
 */
public class InfiniteFluidBarrelItem extends Item {
    public static final int FILL_AMOUNT = 1_000_000_000;

    public InfiniteFluidBarrelItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final InfiniteHandler handler = new InfiniteHandler(stack);
            private final LazyOptional<IFluidHandlerItem> optional = LazyOptional.of(() -> handler);

            @Override
            public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                                     @Nullable net.minecraft.core.Direction side) {
                if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
                    return optional.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Infinite fluid source").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("Fills machine tanks without being consumed")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    /**
     * Drain-only infinite handler. {@code drain} returns the requested fluid type
     * so {@link net.minecraftforge.fluids.FluidUtil#tryEmptyContainer} can fill tanks
     * when the tank already has a fluid (or via pad special-case for empty tanks).
     */
    private static final class InfiniteHandler implements IFluidHandlerItem {
        private final ItemStack container;

        private InfiniteHandler(ItemStack container) {
            this.container = container;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return container;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return FILL_AMOUNT;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            return new FluidStack(resource.getFluid(), Math.min(resource.getAmount(), FILL_AMOUNT));
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
