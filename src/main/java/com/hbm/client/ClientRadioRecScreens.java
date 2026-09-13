package com.hbm.client;

import com.hbm.blockentity.machine.RadioRecBlockEntity;
import com.hbm.client.screen.RadioRecScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientRadioRecScreens {
    private ClientRadioRecScreens() {
    }

    public static void open(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (be instanceof RadioRecBlockEntity radio) {
            mc.setScreen(new RadioRecScreen(radio));
        }
    }
}
