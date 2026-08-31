package com.hbm.blockentity.machine;

import com.hbm.blocks.machine.RadarLargeBlock;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadarProxyBlockEntity extends BlockEntity {
    public RadarProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADAR_PROXY.get(), pos, state);
    }

    @Nullable
    private BlockEntity core() {
        if (level == null) {
            return null;
        }
        BlockState state = getBlockState();
        if (state.getBlock() instanceof RadarLargeBlock) {
            return RadarLargeBlock.coreEntity(level, worldPosition, state);
        }
        return null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && RadarLargeBlock.isEnergyPort(getBlockState())) {
            BlockEntity core = core();
            if (core != null) {
                return core.getCapability(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }
}
