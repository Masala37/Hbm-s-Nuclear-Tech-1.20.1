package com.hbm.client;

import com.hbm.client.screen.DesignatorManualScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientDesignatorScreens {
    private ClientDesignatorScreens() {
    }

    public static void open(Player player) {
        Minecraft.getInstance().setScreen(new DesignatorManualScreen(player));
    }
}
