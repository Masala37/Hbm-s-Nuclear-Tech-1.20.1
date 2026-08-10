package com.hbm.events;

import com.hbm.blocks.bomb.ExplosiveBarrelBlock;
import com.hbm.lib.RefStrings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Shot-sensitive explosive barrels (legacy {@code BlockDetonatable#onShot}).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BarrelShotEvents {
    private BarrelShotEvents() {
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getRayTraceResult().getType() != HitResult.Type.BLOCK) {
            return;
        }
        Projectile projectile = event.getProjectile();
        Level level = projectile.level();
        if (level.isClientSide) {
            return;
        }
        BlockHitResult hit = (BlockHitResult) event.getRayTraceResult();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ExplosiveBarrelBlock barrel && barrel.isShotSensitive()) {
            barrel.onShot(level, pos);
            projectile.discard();
        }
    }
}
