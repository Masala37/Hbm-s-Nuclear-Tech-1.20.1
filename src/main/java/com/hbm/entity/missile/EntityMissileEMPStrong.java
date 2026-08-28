package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Strong EMP missile — legacy {@code EntityMissileTier2.EntityMissileEMPStrong}.
 * Impact: spawns the sustained {@code EntityEMP} logic field (not the visual ring).
 */
public class EntityMissileEMPStrong extends EntityMissileBaseNT {
    public EntityMissileEMPStrong(EntityType<? extends EntityMissileEMPStrong> type, Level level) {
        super(type, level);
    }

    public EntityMissileEMPStrong(Level level, double x, double y, double z,
                                  int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_EMP_STRONG.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER2;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.empStrong(level(), this);
    }
}
