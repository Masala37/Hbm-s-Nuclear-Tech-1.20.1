package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.AssembledNuke;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * N² Mine ({@code nuke_n2}): twelve N2 charges, MK5 dig without fallout.
 */
public class NukeN2Block extends AssembledNukeBlock {
    private static final VoxelShape INTERACT = Block.box(-8, 0, -8, 24, 56, 24);

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.hbm.blockentity.bomb.NukeN2BlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.n2Radius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_n2";
    }

    @Override
    protected VoxelShape interactionShape() {
        return INTERACT;
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        AssembledNuke nuke = asAssembly(level.getBlockEntity(pos));
        if (nuke == null || !nuke.isReady()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }

        int radius = nuke.resolveBlastRadius(blastRadius());
        nuke.clearSlots();
        level.removeBlock(pos, false);
        igniteNoRad(level, pos, radius);
        return BombReturnCode.DETONATED;
    }

    public static void igniteNoRad(Level level, BlockPos pos, int radius) {
        if (level.isClientSide) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                1.0F, 0.9F + level.random.nextFloat() * 0.1F);

        level.addFreshEntity(EntityNukeExplosionMK5.statFacNoRad(level, radius, x, y, z));
        EntityNukeTorex.statFacStandard(level, x, y + 0.5D, z, radius);
    }
}
