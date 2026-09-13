package api.hbm.conveyor;

import net.minecraft.world.item.ItemStack;

/**
 * 1.7.10 {@code api.hbm.conveyor.IConveyorPackage}. Packages are not spawned in this pass.
 */
public interface IConveyorPackage {
    ItemStack[] getItemStacks();
}
