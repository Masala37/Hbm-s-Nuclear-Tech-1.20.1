package api.hbm.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 {@code api.hbm.conveyor.IEnterableBlock}.
 */
public interface IEnterableBlock {
    boolean canItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity);

    void onItemEnter(Level level, BlockPos pos, Direction dir, IConveyorItem entity);

    boolean canPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity);

    void onPackageEnter(Level level, BlockPos pos, Direction dir, IConveyorPackage entity);
}
