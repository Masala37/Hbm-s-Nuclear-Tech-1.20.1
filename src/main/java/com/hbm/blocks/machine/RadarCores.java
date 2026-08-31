package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.CompactLauncherBlockEntity;
import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.blockentity.machine.LaunchPadLargeBlockEntity;
import com.hbm.blockentity.machine.LaunchTableBlockEntity;
import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.blockentity.machine.RadarScreenBlockEntity;
import com.hbm.tileentity.IRadarCommandReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class RadarCores {
    private RadarCores() {
    }

    @Nullable
    public static BlockEntity core(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RadarNTBlock) {
            return level.getBlockEntity(pos);
        }
        if (state.getBlock() instanceof RadarLargeBlock) {
            return RadarLargeBlock.coreEntity(level, pos, state);
        }
        if (state.getBlock() instanceof RadarScreenBlock) {
            return RadarScreenBlock.coreEntity(level, pos, state);
        }
        LaunchPadLargeBlockEntity large = LaunchPadLargeBlock.coreEntity(level, pos, state);
        if (large != null) {
            return large;
        }
        LaunchPadBlockEntity silo = LaunchPadBlock.coreEntity(level, pos, state);
        if (silo != null) {
            return silo;
        }
        CompactLauncherBlockEntity compact = CompactLauncherBlock.coreEntity(level, pos, state);
        if (compact != null) {
            return compact;
        }
        LaunchTableBlockEntity table = LaunchTableBlock.coreEntity(level, pos, state);
        if (table != null) {
            return table;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IRadarCommandReceiver || be instanceof RadarScreenBlockEntity || be instanceof RadarNTBlockEntity) {
            return be;
        }
        return null;
    }
}
