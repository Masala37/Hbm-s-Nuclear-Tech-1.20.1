package com.hbm.client;

import com.hbm.client.screen.RadarNTScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientRadarScreens {
    private ClientRadarScreens() {
    }

    public static void openRadar(BlockPos pos) {
        Minecraft.getInstance().setScreen(new RadarNTScreen(pos));
    }
}
