package com.hbm.client.screen;

import com.hbm.inventory.menu.CompactLauncherMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CompactLauncherScreen extends CustomLauncherScreen<CompactLauncherMenu> {
    public CompactLauncherScreen(CompactLauncherMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }
}
