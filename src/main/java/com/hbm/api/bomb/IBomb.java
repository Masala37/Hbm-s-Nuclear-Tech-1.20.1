package com.hbm.api.bomb;

import com.hbm.entity.bomb.PrimedBombEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Blocks that can be detonated (conventional primed bombs or in-place nukes).
 */
public interface IBomb {
    enum BombReturnCode {
        UNDEFINED(false),
        DETONATED(true),
        TRIGGERED(true),
        LAUNCHED(true),
        ERROR_MISSING_COMPONENT(false),
        ERROR_INCOMPATIBLE(false),
        ERROR_NO_BOMB(false);

        private final boolean success;

        BombReturnCode(boolean success) {
            this.success = success;
        }

        public boolean wasSuccessful() {
            return success;
        }
    }

    /**
     * Detonate in place (nukes / detonator path). Default is unused for primed bombs.
     */
    default BombReturnCode explode(Level level, BlockPos pos) {
        return BombReturnCode.UNDEFINED;
    }

    /**
     * Called when a primed bomb entity's fuse expires.
     */
    default void explodeEntity(Level level, double x, double y, double z, PrimedBombEntity entity) {
    }
}
