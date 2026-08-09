package com.hbm.api.bomb;

import com.hbm.entity.bomb.PrimedBombEntity;
import net.minecraft.world.level.Level;

/**
 * Blocks that detonate via a primed bomb entity.
 */
public interface IBomb {
    void explodeEntity(Level level, double x, double y, double z, PrimedBombEntity entity);
}
