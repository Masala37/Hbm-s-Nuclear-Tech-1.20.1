package com.hbm.blocks.bomb;

import com.hbm.blockentity.bomb.AssembledNuke;
import com.hbm.blockentity.bomb.NukeCustomBlockEntity;
import com.hbm.config.BombConfig;
import com.hbm.entity.projectile.EntityFallingNuke;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Custom Nuke — freeform payload; yield stages from inventory contents.
 * World mesh drawn by {@link com.hbm.client.render.blockentity.AssembledNukeRenderer}.
 */
public class NukeCustomBlock extends AssembledNukeBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NukeCustomBlockEntity(pos, state);
    }

    @Override
    protected int blastRadius() {
        return BombConfig.customRadius.get();
    }

    @Override
    protected String langKey() {
        return "block.hbm.nuke_custom";
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }

        AssembledNuke assembly = asAssembly(level.getBlockEntity(pos));
        if (!(assembly instanceof NukeCustomBlockEntity nuke) || !nuke.isReady()) {
            return BombReturnCode.ERROR_MISSING_COMPONENT;
        }

        float tnt = nuke.getTnt();
        float n = nuke.getNuke();
        float hydro = nuke.getHydro();
        float amat = nuke.getAmat();
        float dirty = nuke.getDirty();
        float schrab = nuke.getSchrab();
        float euph = nuke.getEuph();
        boolean falling = nuke.isFalling();

        nuke.clearSlots();
        level.removeBlock(pos, false);

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;

        if (falling) {
            level.addFreshEntity(EntityFallingNuke.create(level, x, y, z, tnt, n, hydro, amat, dirty, schrab, euph));
            return BombReturnCode.LAUNCHED;
        }

        NukeCustomYield.explodeCustom(level, x, y, z, tnt, n, hydro, amat, dirty, schrab, euph);
        return BombReturnCode.DETONATED;
    }
}
