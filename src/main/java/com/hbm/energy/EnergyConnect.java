package com.hbm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/**
 * 1.7.10 {@code Library.canConnect} for HE/FE neighbors (TESR cable stubs).
 */
public final class EnergyConnect {
    private EnergyConnect() {
    }

    public static boolean isActiveConductor(BlockEntity be) {
        return be instanceof IEnergyConductor conductor && conductor.isEnergyConductor();
    }

    /**
     * BFS step onto an adjacent cell. 1.7 {@code PowerNode} only joins faces both
     * nodes list as {@code DirPos}. Inactive {@link IEnergyConductor} extras
     * (assembly/chemplant proxies) are not graph nodes.
     */
    public static boolean canTraverseFace(boolean fromConnects, boolean neighborActiveConductor,
                                          boolean neighborConnectsBack) {
        if (!fromConnects) {
            return false;
        }
        if (!neighborActiveConductor) {
            return true;
        }
        return neighborConnectsBack;
    }

    public static boolean canWalkToNeighbor(IEnergyConductor from, Direction dir, BlockEntity neighbor) {
        if (!from.connectsEnergy(dir)) {
            return false;
        }
        if (!isActiveConductor(neighbor)) {
            return true;
        }
        return ((IEnergyConductor) neighbor).connectsEnergy(dir.getOpposite());
    }

    /**
     * @param cableSide direction from the machine into the neighbor (1.7 {@code ForgeDirection} argument)
     */
    public static boolean canConnect(BlockGetter level, BlockPos neighborPos, Direction cableSide) {
        BlockEntity be = level.getBlockEntity(neighborPos);
        if (isActiveConductor(be)) {
            return ((IEnergyConductor) be).connectsEnergy(cableSide.getOpposite());
        }
        if (be == null) {
            return false;
        }
        return be.getCapability(ForgeCapabilities.ENERGY, cableSide.getOpposite())
                .map(storage -> storage.canReceive() || storage.canExtract())
                .orElse(false);
    }
}
