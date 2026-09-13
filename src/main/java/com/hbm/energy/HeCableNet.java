package com.hbm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Flood-fill HE cable graph and allocate FE 1:1 with {@link PowerNet}.
 * Cables are not endpoints; leftover cable buffers are flushed into the pool.
 */
public final class HeCableNet {
    private HeCableNet() {
    }

    public static void tick(Level level, BlockPos start) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity origin = level.getBlockEntity(start);
        if (!(origin instanceof IEnergyConductor conductor) || !conductor.isEnergyConductor()) {
            return;
        }
        long gameTime = level.getGameTime();
        if (conductor.lastEnergyNetTick() == gameTime) {
            return;
        }

        List<IEnergyConductor> cables = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!seen.add(pos.asLong())) {
                continue;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IEnergyConductor node) || !node.isEnergyConductor()) {
                continue;
            }
            node.markEnergyNetTick(gameTime);
            cables.add(node);
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (EnergyConnect.canWalkToNeighbor(node, direction, level.getBlockEntity(next))) {
                    queue.addLast(next);
                }
            }
            for (BlockPos extra : node.extraEnergyLinks()) {
                queue.addLast(extra.immutable());
            }
        }

        IdentityHashMap<IEnergyStorage, StorageNode> providers = new IdentityHashMap<>();
        IdentityHashMap<IEnergyStorage, StorageNode> receivers = new IdentityHashMap<>();
        long extraSupply = 0L;

        for (IEnergyConductor cable : cables) {
            extraSupply += Math.max(0, cable.drainConductorBuffer());
            if (!(cable instanceof BlockEntity cableBe) || cableBe.getLevel() == null) {
                continue;
            }
            BlockPos pos = cableBe.getBlockPos();
            for (Direction direction : Direction.values()) {
                if (!cable.connectsEnergy(direction)) {
                    continue;
                }
                BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
                if (EnergyConnect.isActiveConductor(neighbor)) {
                    continue;
                }
                if (neighbor == null) {
                    continue;
                }
                neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                    StorageNode node = new StorageNode(storage);
                    if (storage.canExtract() && storage.getEnergyStored() > 0) {
                        providers.putIfAbsent(storage, node);
                    }
                    if (storage.canReceive() && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
                        receivers.putIfAbsent(storage, node);
                    }
                });
            }
        }

        List<PowerNet.Node> providerList = new ArrayList<>(providers.values());
        if (extraSupply > 0) {
            providerList.add(new ExtraSupply(extraSupply));
        }
        if (providerList.isEmpty() || receivers.isEmpty()) {
            return;
        }
        PowerNet.transfer(providerList, new ArrayList<>(receivers.values()));
    }

    private static final class ExtraSupply implements PowerNet.Node {
        private long stored;

        private ExtraSupply(long stored) {
            this.stored = stored;
        }

        @Override
        public long offer() {
            return stored;
        }

        @Override
        public long demand() {
            return 0L;
        }

        @Override
        public ConnectionPriority priority() {
            return ConnectionPriority.NORMAL;
        }

        @Override
        public long receive(long amount) {
            return 0L;
        }

        @Override
        public void extract(long amount) {
            stored = Math.max(0L, stored - amount);
        }
    }

    private static final class StorageNode implements PowerNet.Node {
        private final IEnergyStorage storage;

        private StorageNode(IEnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public long offer() {
            if (!storage.canExtract()) {
                return 0L;
            }
            return storage.extractEnergy(Integer.MAX_VALUE, true);
        }

        @Override
        public long demand() {
            if (!storage.canReceive()) {
                return 0L;
            }
            return storage.receiveEnergy(Integer.MAX_VALUE, true);
        }

        @Override
        public ConnectionPriority priority() {
            if (storage instanceof IEnergyPriority prioritized) {
                return prioritized.energyPriority();
            }
            return ConnectionPriority.NORMAL;
        }

        @Override
        public long receive(long amount) {
            if (amount <= 0L) {
                return 0L;
            }
            return storage.receiveEnergy((int) Math.min(amount, Integer.MAX_VALUE), false);
        }

        @Override
        public void extract(long amount) {
            if (amount <= 0L) {
                return;
            }
            storage.extractEnergy((int) Math.min(amount, Integer.MAX_VALUE), false);
        }
    }
}
