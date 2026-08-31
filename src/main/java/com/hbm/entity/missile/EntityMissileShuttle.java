package com.hbm.entity.missile;

import api.hbm.entity.IRadarDetectableNT;
import com.hbm.explosion.ExplosionNT;
import com.hbm.network.ModMessages;
import com.hbm.network.RbmkMushEffectPacket;
import com.hbm.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.PacketDistributor;

public class EntityMissileShuttle extends EntityMissileBaseNT {
    public EntityMissileShuttle(EntityType<? extends EntityMissileShuttle> type, Level level) {
        super(type, level);
    }

    public EntityMissileShuttle(Level level) {
        this(ModEntities.MISSILE_SHUTTLE.get(), level);
    }

    public EntityMissileShuttle(Level level, double x, double y, double z,
                                 int targetX, int targetY, int targetZ) {
        super(ModEntities.MISSILE_SHUTTLE.get(), level, x, y, z, targetX, targetY, targetZ);
    }

    @Override
    protected int radarTier() {
        return IRadarDetectableNT.TIER3;
    }

    @Override
    public String getUnlocalizedName() {
        return "radar.target.shuttle";
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide) {
            return;
        }
        double x = getX() + 0.5D;
        double y = getY() + 0.5D;
        double z = getZ() + 0.5D;
        new ExplosionNT(level(), null, x, y, z, 20.0F)
                .overrideResolution(64)
                .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                .addAttrib(ExplosionNT.ExAttrib.NOPARTICLE)
                .explode();
        if (level() instanceof ServerLevel server) {
            ModMessages.CHANNEL.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            x, y + 0.5D, z, 250.0D, server.dimension())),
                    new RbmkMushEffectPacket(x, y + 0.5D, z, 10.0F));
        }
    }
}
