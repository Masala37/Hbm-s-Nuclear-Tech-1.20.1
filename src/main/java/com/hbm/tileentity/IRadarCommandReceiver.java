package com.hbm.tileentity;

import net.minecraft.world.entity.Entity;

public interface IRadarCommandReceiver {

    boolean sendCommandPosition(int x, int y, int z);

    boolean sendCommandEntity(Entity target);
}
