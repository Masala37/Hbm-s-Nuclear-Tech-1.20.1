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

    /** Server menus must remain bound to the same live machine, not just its former position. */
    public static boolean boundToBlock(Player player, BlockEntity be) {
        if (!closeEnough(player, be)) {
            return false;
        }
        return player.level().isClientSide || (be != null && !be.isRemoved()
                && be.getLevel() == player.level()
                && player.level().getBlockEntity(be.getBlockPos()) == be);
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
