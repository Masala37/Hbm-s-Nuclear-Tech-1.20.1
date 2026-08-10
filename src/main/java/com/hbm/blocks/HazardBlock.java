package com.hbm.blocks;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Solid storage block with optional beacon-base flag and chunk-radiation emission
 * when the block item is registered with a RAD hazard (legacy {@code BlockHazard}).
 */
public class HazardBlock extends Block {
    private final boolean beaconBase;
    /** Optional fixed emit rate; {@code <= 0} looks up {@link HazardSystem} * 0.1F. */
    private final float baseRad;

    public HazardBlock(Properties properties, boolean beaconBase) {
        this(properties, beaconBase, 0.0F);
    }

    public HazardBlock(Properties properties, boolean beaconBase, float baseRad) {
        super(properties);
        this.beaconBase = beaconBase;
        this.baseRad = baseRad;
    }

    public boolean isBeaconBase() {
        return beaconBase;
    }

    public float getBaseRad() {
        return baseRad;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && resolveEmit() > 0.0F) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float emit = resolveEmit();
        if (emit > 0.0F) {
            ChunkRadiationManager.INSTANCE.incrementRad(level, pos.getX(), pos.getY(), pos.getZ(), emit);
            level.scheduleTick(pos, this, 20);
        }
    }

    private float resolveEmit() {
        if (baseRad > 0.0F) {
            return baseRad;
        }
        float level = HazardSystem.getHazardLevelFromStack(new ItemStack(this), HazardRegistry.RADIATION);
        return level * 0.1F;
    }
}
