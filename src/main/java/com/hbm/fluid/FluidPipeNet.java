package com.hbm.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Flood-fill typed ducts and move matching Forge fluids with {@link FluidNet}.
 */
public final class FluidPipeNet {
    private FluidPipeNet() {
    }

    public static void tick(Level level, BlockPos start) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity origin = level.getBlockEntity(start);
        if (!(origin instanceof IFluidPipe pipe) || !pipe.isFluidPipe()) {
            return;
        }
        long gameTime = level.getGameTime();
        if (pipe.lastFluidNetTick() == gameTime) {
            return;
        }

        List<IFluidPipe> pipes = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        queue.add(start);
        ResourceLocation netFluid = pipe.pipeFluid();

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!seen.add(pos.asLong())) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IFluidPipe node) || !node.isFluidPipe()) {
                continue;
            }
            if (!sameFluid(netFluid, node.pipeFluid())) {
                continue;
            }
            if (netFluid == null && node.pipeFluid() != null) {
                netFluid = node.pipeFluid();
            }
            node.markFluidNetTick(gameTime);
            pipes.add(node);
            for (Direction direction : Direction.values()) {
                queue.addLast(pos.relative(direction));
            }
        }

        if (netFluid == null) {
            netFluid = adoptFromNeighbors(level, pipes);
        }
        if (netFluid != null) {
            for (IFluidPipe node : pipes) {
                if (node.pipeFluid() == null) {
                    node.setPipeFluid(netFluid);
                }
            }
        }
        if (netFluid == null) {
            return;
        }

        Fluid fluid = ForgeRegistries.FLUIDS.getValue(netFluid);
        if (fluid == null) {
            return;
        }

        IdentityHashMap<IFluidHandler, HandlerTank> providers = new IdentityHashMap<>();
        IdentityHashMap<IFluidHandler, HandlerTank> receivers = new IdentityHashMap<>();

        for (IFluidPipe node : pipes) {
            if (!(node instanceof BlockEntity pipeBe) || pipeBe.getLevel() == null) {
                continue;
            }
            BlockPos pos = pipeBe.getBlockPos();
            for (Direction direction : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
                if (neighbor instanceof IFluidPipe neighborPipe && neighborPipe.isFluidPipe()) {
                    continue;
                }
                if (neighbor == null) {
                    continue;
                }
                ResourceLocation type = netFluid;
                neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).ifPresent(handler -> {
                    HandlerTank tank = new HandlerTank(handler, type);
                    if (tank.available() > 0) {
                        providers.putIfAbsent(handler, tank);
                    }
                    if (tank.room() > 0) {
                        receivers.putIfAbsent(handler, tank);
                    }
                });
            }
        }

        if (providers.isEmpty() || receivers.isEmpty()) {
            return;
        }
        int moved = FluidNet.transfer(new ArrayList<>(providers.values()), new ArrayList<>(receivers.values()));
        if (moved > 0) {
            for (IFluidPipe node : pipes) {
                node.onNetFluidMoved(moved);
            }
        }
    }

    private static boolean sameFluid(ResourceLocation a, ResourceLocation b) {
        if (a == null || b == null) {
            return true;
        }
        return a.equals(b);
    }

    private static ResourceLocation adoptFromNeighbors(Level level, List<IFluidPipe> pipes) {
        for (IFluidPipe node : pipes) {
            if (!(node instanceof BlockEntity pipeBe)) {
                continue;
            }
            BlockPos pos = pipeBe.getBlockPos();
            for (Direction direction : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
                if (neighbor == null || (neighbor instanceof IFluidPipe neighborPipe && neighborPipe.isFluidPipe())) {
                    continue;
                }
                ResourceLocation found = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite())
                        .map(handler -> {
                            for (int i = 0; i < handler.getTanks(); i++) {
                                FluidStack stack = handler.getFluidInTank(i);
                                if (!stack.isEmpty()) {
                                    return ForgeRegistries.FLUIDS.getKey(stack.getFluid());
                                }
                            }
                            return null;
                        }).orElse(null);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static final class HandlerTank implements FluidNet.Tank {
        private final IFluidHandler handler;
        private final FluidStack filter;

        private HandlerTank(IFluidHandler handler, ResourceLocation fluidId) {
            this.handler = handler;
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            this.filter = fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, Integer.MAX_VALUE);
        }

        @Override
        public int available() {
            if (filter.isEmpty()) {
                return 0;
            }
            FluidStack drained = handler.drain(new FluidStack(filter.getFluid(), Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);
            return drained.getAmount();
        }

        @Override
        public int room() {
            if (filter.isEmpty()) {
                return 0;
            }
            return handler.fill(new FluidStack(filter.getFluid(), Integer.MAX_VALUE), IFluidHandler.FluidAction.SIMULATE);
        }

        @Override
        public int fill(int amount) {
            if (filter.isEmpty() || amount <= 0) {
                return 0;
            }
            return handler.fill(new FluidStack(filter.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        public int drain(int amount) {
            if (filter.isEmpty() || amount <= 0) {
                return 0;
            }
            FluidStack drained = handler.drain(new FluidStack(filter.getFluid(), amount), IFluidHandler.FluidAction.EXECUTE);
            return drained.getAmount();
        }
    }
}
