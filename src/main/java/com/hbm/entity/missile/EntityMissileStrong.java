package com.hbm.entity.missile;

import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Strong HE missile — legacy Tier-2 {@code explodeStandard(30, 32)} + composeEffectStandard.
 */
public class EntityMissileStrong extends EntityMissileBaseNT {
    public EntityMissileStrong(EntityType<? extends EntityMissileStrong> type, Level level) {
        super(type, level);
    }

    public EntityMissileStrong(Level level, double x, double y, double z,
                               int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_STRONG.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.heTier2(level(), this, getX(), getY(), getZ());
    }
}
