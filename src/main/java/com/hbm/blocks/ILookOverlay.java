package com.hbm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 1.7.10 {@code ILookOverlay}: HUD text while looking at a machine with no GUI.
 */
public interface ILookOverlay {
    void printHook(Level level, BlockPos pos, List<Component> lines);
}
