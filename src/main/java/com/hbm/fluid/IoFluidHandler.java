package com.hbm.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Fill the input tank, drain the output tank. Used by boiler, turbine, and condenser.
 */
public final class IoFluidHandler implements IFluidHandler {
    private final FluidTank input;
    private final FluidTank output;
    private final Predicate<FluidStack> acceptInput;

    public IoFluidHandler(FluidTank input, FluidTank output, Predicate<FluidStack> acceptInput) {
        this.input = input;
        this.output = output;
        this.acceptInput = acceptInput;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (tank == 0) {
            return input.getFluid();
        }
        return tank == 1 ? output.getFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank == 0) {
            return input.getCapacity();
        }
        return tank == 1 ? output.getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 && acceptInput.test(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty() || !acceptInput.test(resource)) {
            return 0;
        }
        return input.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource == null || resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return output.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return output.drain(maxDrain, action);
    }
}
