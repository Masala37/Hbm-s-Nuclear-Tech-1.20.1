package com.hbm.blockentity.bomb;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Crashed-bomb TE with passive radiation aura.
 */
public class CrashedBombBlockEntity extends BlockEntity {
    public CrashedBombBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRASHED_BOMB.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrashedBombBlockEntity be) {
        if (!(level instanceof ServerLevel) || level.getGameTime() % 20L != 0L) {
            return;
        }
        ChunkRadiationManager.INSTANCE.incrementRad(level, pos.getX(), pos.getY(), pos.getZ(), 1.5F);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(4.0D);
    }
}
