package com.hbm.inventory.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Always-valid while in range. Instant-close was leaving server menus open with no client screen.
 */
public final class MenuValidity {
    private MenuValidity() {
    }

    public static boolean closeEnough(Player player, BlockEntity be) {
        if (player == null) {
            return false;
        }
        if (be == null) {
            return true; // don't force-close during client stub edge cases
        }
        BlockPos pos = be.getBlockPos();
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
