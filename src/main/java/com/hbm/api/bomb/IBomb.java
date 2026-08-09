package com.hbm.api.bomb;

import com.hbm.entity.bomb.PrimedBombEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Blocks that can be detonated (conventional primed bombs or in-place nukes).
 */
public interface IBomb {
    enum BombReturnCode {
        UNDEFINED(false, "bomb.undefined"),
        DETONATED(true, "bomb.detonated"),
        TRIGGERED(true, "bomb.triggered"),
        LAUNCHED(true, "bomb.launched"),
        ERROR_MISSING_COMPONENT(false, "bomb.missingComponent"),
        ERROR_INCOMPATIBLE(false, "bomb.incompatible"),
        ERROR_NO_BOMB(false, "bomb.nobomb");

        private final boolean success;
        private final String messageKey;

        BombReturnCode(boolean success, String messageKey) {
            this.success = success;
            this.messageKey = messageKey;
        }

        public boolean wasSuccessful() {
            return success;
        }

        public String getMessageKey() {
            return messageKey;
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
