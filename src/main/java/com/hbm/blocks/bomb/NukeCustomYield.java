package com.hbm.blocks.bomb;

import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * Custom Nuke stage resolution (legacy {@code NukeCustom.explodeCustom}).
 * Caller passes block-center coords ({@code x+0.5, y+0.5, z+0.5}).
 */
public final class NukeCustomYield {
    public static final int MAX_TNT = 150;
    public static final int MAX_NUKE = 200;
    public static final int MAX_HYDRO = 350;
    public static final int MAX_AMAT = 350;
    public static final int MAX_SCHRAB = 250;

    private NukeCustomYield() {
    }

    public static void explodeCustom(Level level, double x, double y, double z,
                                     float tnt, float nuke, float hydro, float amat,
                                     float dirty, float schrab, float euph) {
        if (level.isClientSide) {
            return;
        }

        dirty = Math.min(dirty, 100.0F);

        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        if (euph > 0.0F) {
            int r = 150;
            level.addFreshEntity(EntityNukeExplosionMK3.statFacFleija(level, x, y, z, r));
            EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(level, 50);
            cloud.setPos(x, y, z);
            level.addFreshEntity(cloud);
            return;
        }

        if (schrab > 0.0F) {
            schrab += amat / 2.0F + hydro / 4.0F + nuke / 8.0F + tnt / 16.0F;
            schrab = Math.min(schrab, MAX_SCHRAB);
            int r = Math.max(1, (int) schrab);
            level.addFreshEntity(EntityNukeExplosionMK3.statFacFleija(level, x, y, z, r));
            EntityCloudFleija cloud = new EntityCloudFleija(level, r);
            cloud.setPos(x, y, z);
            level.addFreshEntity(cloud);
            return;
        }

        if (amat > 0.0F) {
            amat += hydro / 2.0F + nuke / 4.0F + tnt / 8.0F;
            amat = Math.min(amat, MAX_AMAT);
            int r = Math.max(1, (int) amat);
            level.addFreshEntity(EntityBalefire.statFac(level, x, y, z, r));
            EntityNukeTorex.statFacBale(level, x, y + 5.0D, z, r);
            return;
        }

        if (hydro > 0.0F) {
            hydro += nuke / 2.0F + tnt / 4.0F;
            hydro = Math.min(hydro, MAX_HYDRO);
            dirty *= 0.25F;
            int r = Math.max(1, (int) hydro);
            level.addFreshEntity(EntityNukeExplosionMK5.statFac(level, r, x, y, z).moreFallout((int) dirty));
            EntityNukeTorex.statFacStandard(level, x, y + 5.0D, z, r);
            return;
        }

        if (nuke > 0.0F) {
            nuke += tnt / 2.0F;
            nuke = Math.min(nuke, MAX_NUKE);
            int r = Math.max(1, (int) nuke);
            level.addFreshEntity(EntityNukeExplosionMK5.statFac(level, r, x, y + 5.0D, z).moreFallout((int) dirty));
            EntityNukeTorex.statFacStandard(level, x, y + 5.0D, z, r);
            return;
        }

        if (tnt >= 75.0F) {
            tnt = Math.min(tnt, MAX_TNT);
            int r = Math.max(1, (int) tnt);
            level.addFreshEntity(EntityNukeExplosionMK5.statFacNoRad(level, r, x, y, z));
            EntityNukeTorex.statFacStandard(level, x, y + 5.0D, z, r);
            return;
        }

        if (tnt > 0.0F) {
            float power = Math.min(tnt, MAX_TNT);
            ExplosionLarge.explode(level, x, y, z, power, true, true, true);
        }
    }
}
