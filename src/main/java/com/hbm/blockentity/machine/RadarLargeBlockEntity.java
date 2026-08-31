package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.RadarLargeBlock;
import com.hbm.energy.EnergyNetworkHelper;
import com.hbm.handler.RadarRules;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class RadarLargeBlockEntity extends RadarNTBlockEntity {
    public RadarLargeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADAR_LARGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarLargeBlockEntity be) {
        RadarNTBlockEntity.serverTick(level, pos, state, be);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RadarLargeBlockEntity be) {
        RadarNTBlockEntity.clientTick(level, pos, state, be);
    }

    @Override
    public int getRange() {
        return RadarRules.RANGE_LARGE;
    }

    @Override
    protected void pullEnergy(Level level) {
        BlockPos pos = worldPosition;
        EnergyNetworkHelper.pullFrom(level, pos.offset(2, 0, 0), Direction.WEST, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(-2, 0, 0), Direction.EAST, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(0, 0, 2), Direction.NORTH, getEnergy(), ENERGY_TRANSFER);
        EnergyNetworkHelper.pullFrom(level, pos.offset(0, 0, -2), Direction.SOUTH, getEnergy(), ENERGY_TRANSFER);
    }

    @Override
    protected void notifyRedstone(Level level) {
        super.notifyRedstone(level);
        for (int[] extra : RadarLargeBlock.ENERGY_EXTRAS) {
            level.updateNeighborsAt(worldPosition.offset(extra[0], 0, extra[2]), getBlockState().getBlock());
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = worldPosition;
        return new AABB(
                pos.getX() - 5.0D, pos.getY(), pos.getZ() - 5.0D,
                pos.getX() + 6.0D, pos.getY() + 10.0D, pos.getZ() + 6.0D);
    }
}
