package com.hbm.entity.missile;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityMissileDoomsdayRusted extends EntityMissileDoomsday {
    public EntityMissileDoomsdayRusted(EntityType<? extends EntityMissileDoomsdayRusted> type, Level level) {
        super(type, level);
    }

    public EntityMissileDoomsdayRusted(Level level) {
        this(ModEntities.MISSILE_DOOMSDAY_RUSTED.get(), level);
    }

    public EntityMissileDoomsdayRusted(Level level, double x, double y, double z,
                                        int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_DOOMSDAY_RUSTED.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        int radius = BombConfig.missileRadius.get();
        level().addFreshEntity(EntityNukeExplosionMK5.statFac(level(), radius, getX(), getY(), getZ())
                .moreFallout(100)
                .suppressFlashFx());
        EntityNukeTorex.statFacStandard(level(), getX(), getY(), getZ(), radius);
    }
}
