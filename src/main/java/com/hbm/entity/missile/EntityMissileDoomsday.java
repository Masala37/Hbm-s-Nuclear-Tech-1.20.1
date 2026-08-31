package com.hbm.entity.missile;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityMissileDoomsday extends EntityMissileTier4 {
    public EntityMissileDoomsday(EntityType<? extends EntityMissileDoomsday> type, Level level) {
        super(type, level);
    }

    protected EntityMissileDoomsday(EntityType<? extends EntityMissileDoomsday> type, Level level,
                                     double x, double y, double z, int targetX, int targetY, int targetZ) {
        super(type, level, x, y, z, targetX, targetY, targetZ);
    }

    public EntityMissileDoomsday(Level level) {
        this(ModEntities.MISSILE_DOOMSDAY.get(), level);
    }

    public EntityMissileDoomsday(Level level, double x, double y, double z,
                                  int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_DOOMSDAY.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    public String getUnlocalizedName() {
        return "radar.target.doomsday";
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        int radius = BombConfig.missileRadius.get() * 2;
        level().addFreshEntity(EntityNukeExplosionMK5.statFac(level(), radius, getX(), getY(), getZ())
                .moreFallout(100)
                .suppressFlashFx());
        EntityNukeTorex.statFacStandard(level(), getX(), getY(), getZ(), radius);
    }
}
