package com.hbm.entity.missile;

import com.hbm.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/**
 * Micro-nuclear missile — legacy {@code EntityMissileTier0.EntityMissileMicro}.
 * Impact: {@code ExplosionNukeSmall.PARAMS_HIGH} at {@code (posX, posY + 0.5, posZ)}.
 */
public class EntityMissileMicro extends EntityMissileBaseNT {
    public EntityMissileMicro(EntityType<? extends EntityMissileMicro> type, Level level) {
        super(type, level);
    }

    public EntityMissileMicro(Level level) {
        this(ModEntities.MISSILE_MICRO.get(), level);
    }

    public EntityMissileMicro(Level level, double x, double y, double z,
                              int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_MICRO.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected float getContrailScale() {
        return 0.5F;
    }

    @Override
    protected void onImpact(HitResult hit) {
        MissileImpacts.microNuke(level(), this);
    }
}
