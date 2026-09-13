package com.hbm.energy;

/**
 * Optional extra on an {@link net.minecraftforge.energy.IEnergyStorage} exposed by a block entity.
 * {@link HeCableNet} reads this when allocating FE so batteries can starve or fill first.
 */
public interface IEnergyPriority {
    ConnectionPriority energyPriority();
}
