package com.hbm.entity.missile;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityMissileMirv extends EntityMissileTier4 {
    public EntityMissileMirv(EntityType<? extends EntityMissileMirv> type, Level level) {
        super(type, level);
    }

    public EntityMissileMirv(Level level) {
        this(ModEntities.MISSILE_NUCLEAR_CLUSTER.get(), level);
    }

    public EntityMissileMirv(Level level, double x, double y, double z,
                             int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_NUCLEAR_CLUSTER.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        int radius = BombConfig.missileRadius.get() * 2;
        level().addFreshEntity(EntityNukeExplosionMK5.statFac(level(), radius, getX(), getY(), getZ())
                .suppressFlashFx());
        EntityNukeTorex.statFacStandard(level(), getX(), getY(), getZ(), radius);
    }
}
