package com.hbm.items.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Legacy {@code ItemInfiniteFluid} / {@code fluid_barrel_infinite} —
 * never empties; fills any tank that already has a type, or {@code fallback} if empty.
 * <p>
 * Forge {@link FluidUtil} always {@code drain(int)} from the item first, so a typeless
 * infinite source cannot use that path. Call {@link #fillInto} / {@link #interact} instead.
 */
public class InfiniteFluidBarrelItem extends Item {
    public static final int FILL_AMOUNT = 1_000_000_000;

    public InfiniteFluidBarrelItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    /**
     * Top up {@code dest}. Occupied tanks use their current fluid (1.7 typed tanks).
     * Empty tanks take the first {@code emptyFallbacks} entry they accept.
     */
    public static boolean fillInto(IFluidHandler dest, int maxAmount, Fluid... emptyFallbacks) {
        boolean any = false;
        int tanks = dest.getTanks();
        for (int i = 0; i < tanks; i++) {
            FluidStack have = dest.getFluidInTank(i);
            int cap = dest.getTankCapacity(i);
            if (!have.isEmpty()) {
                int space = cap - have.getAmount();
                if (space > 0) {
                    int moved = dest.fill(new FluidStack(have.getFluid(), Math.min(maxAmount, space)),
                            FluidAction.EXECUTE);
                    any |= moved > 0;
                }
                continue;
            }
            for (Fluid fallback : emptyFallbacks) {
                if (fallback == null) {
                    continue;
                }
                FluidStack offer = new FluidStack(fallback, Math.min(maxAmount, cap));
                if (offer.isEmpty() || !dest.isFluidValid(i, offer)) {
                    continue;
                }
                int moved = dest.fill(offer, FluidAction.EXECUTE);
                if (moved > 0) {
                    any = true;
                    break;
                }
            }
        }
        return any;
    }

    public static boolean fillTank(FluidTank tank, @Nullable Fluid preferred) {
        Fluid fluid = !tank.getFluid().isEmpty() ? tank.getFluid().getFluid() : preferred;
        if (fluid == null || tank.getSpace() <= 0) {
            return false;
        }
        FluidStack offer = new FluidStack(fluid, Math.min(FILL_AMOUNT, tank.getSpace()));
        if (!tank.isFluidValid(offer)) {
            return false;
        }
        return tank.fill(offer, FluidAction.EXECUTE) > 0;
    }

    public static boolean interact(Player player, InteractionHand hand, IFluidHandler dest,
                                   Fluid... emptyFallbacks) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof InfiniteFluidBarrelItem) {
            return fillInto(dest, FILL_AMOUNT, emptyFallbacks);
        }
        return FluidUtil.interactWithFluidHandler(player, hand, dest);
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
     * Drain-only handler for hoppers / pipes that ask for a specific fluid.
     * {@link FluidUtil} uses {@code drain(int)} and will not see this item; use {@link #fillInto}.
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
