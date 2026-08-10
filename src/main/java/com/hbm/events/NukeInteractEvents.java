package com.hbm.events;

import com.hbm.blocks.bomb.AssembledNukeBlock;
import com.hbm.lib.RefStrings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Only handles look-misses (right-click item when pick is not a nuke).
 * Does NOT cancel RightClickBlock — that was stealing machine GUI opens when a nuke was nearby.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NukeInteractEvents {
    private static final double RANGE = 7.0D;
    private static final double INFLATE = 2.0D;
    private static final double MIN_DOT = 0.75D;

    private NukeInteractEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }
        Player player = event.getEntity();
        HitResult hit = player.pick(RANGE, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            // Clicking a real block — let that block's use() run. Never steal.
            return;
        }
        if (tryOpenNearbyNuke(player, level, event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static boolean tryOpenNearbyNuke(Player player, Level level, InteractionHand hand) {
        if (!(player instanceof ServerPlayer)) {
            return false;
        }

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(RANGE));
        AABB sweep = new AABB(start, end).inflate(INFLATE);

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = (int) Math.floor(sweep.minX);
        int minY = (int) Math.floor(sweep.minY);
        int minZ = (int) Math.floor(sweep.minZ);
        int maxX = (int) Math.floor(sweep.maxX);
        int maxY = (int) Math.floor(sweep.maxY);
        int maxZ = (int) Math.floor(sweep.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!(state.getBlock() instanceof AssembledNukeBlock)) {
                        continue;
                    }
                    Vec3 center = Vec3.atCenterOf(cursor);
                    Vec3 toNuke = center.subtract(start);
                    double len = toNuke.length();
                    if (len < 1.0E-4D || look.dot(toNuke.normalize()) < MIN_DOT) {
                        continue;
                    }
                    double d = start.distanceToSqr(center);
                    if (d < bestDist) {
                        bestDist = d;
                        best = cursor.immutable();
                    }
                }
            }
        }

        if (best == null) {
            return false;
        }

        BlockState state = level.getBlockState(best);
        if (!(state.getBlock() instanceof AssembledNukeBlock nuke)) {
            return false;
        }

        BlockHitResult fakeHit = new BlockHitResult(Vec3.atCenterOf(best), player.getDirection().getOpposite(), best, false);
        InteractionResult result = nuke.use(state, level, best, player, hand, fakeHit);
        return result.consumesAction();
    }
}
