package com.hbm.entity.missile;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class EntityMissileVolcano extends EntityMissileTier4 {
    public EntityMissileVolcano(EntityType<? extends EntityMissileVolcano> type, Level level) {
        super(type, level);
    }

    public EntityMissileVolcano(Level level) {
        this(ModEntities.MISSILE_VOLCANO.get(), level);
    }

    public EntityMissileVolcano(Level level, double x, double y, double z,
                                int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_VOLCANO.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        double x = getX();
        double y = getY();
        double z = getZ();
        ExplosionLarge.explode(level(), x, y, z, 10.0F, true, true, true);
        BlockState lava = ModBlocks.VOLCANIC_LAVA.get().defaultBlockState();
        int fx = (int) Math.floor(x);
        int fy = (int) Math.floor(y);
        int fz = (int) Math.floor(z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level().setBlock(new BlockPos(fx + dx, fy + dy, fz + dz), lava, 3);
                }
            }
        }
        level().setBlock(new BlockPos(fx, fy, fz), ModBlocks.VOLCANO_CORE.get().defaultBlockState(), 3);
    }
}
