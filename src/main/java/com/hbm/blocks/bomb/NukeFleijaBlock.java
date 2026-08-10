package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.NukeFleijaBlockEntity;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * FLEIJA — MK3 fleija dig + cyan cloud (legacy {@code NukeFleija}).
 */
public class NukeFleijaBlock extends AssembledNukeBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeFleijaBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.fleijaRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_fleija";
    }

    @Override
    protected void detonateBlast(Level level, BlockPos pos, int radius) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        EntityNukeExplosionMK3 dig = EntityNukeExplosionMK3.statFacFleija(level, x, y, z, radius);
        level.addFreshEntity(dig);
        EntityCloudFleija cloud = new EntityCloudFleija(level, radius);
        cloud.setPos(x, y, z);
        level.addFreshEntity(cloud);
    }
}
