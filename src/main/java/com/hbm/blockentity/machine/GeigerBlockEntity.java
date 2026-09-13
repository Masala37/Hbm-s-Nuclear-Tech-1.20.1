package com.hbm.blockentity.machine;

import com.hbm.handler.GeigerClicks;
import com.hbm.handler.GeigerSound;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Chunk-radiation sampler and clicker (legacy {@code TileEntityGeiger}).
 */
public class GeigerBlockEntity extends BlockEntity {
    private int timer;
    private float ticker;

    public GeigerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEIGER.get(), pos, state);
    }

    public float check() {
        return ChunkRadiationManager.INSTANCE.getRadiation(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }

    public int analogSignal() {
        return GeigerClicks.analogSignal(check());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GeigerBlockEntity be) {
        if (level.isClientSide) {
            return;
        }
        be.timer++;
        if (be.timer == 10) {
            be.timer = 0;
            be.ticker = be.check();
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        if (be.timer % 5 == 0) {
            int track = GeigerClicks.pickTrack(be.ticker, 0.0F, level.random::nextInt);
            GeigerSound.play(level, pos.getX(), pos.getY(), pos.getZ(), track);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition);
    }
}
