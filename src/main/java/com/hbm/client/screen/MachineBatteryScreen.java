package com.hbm.client.screen;

import com.hbm.energy.ConnectionPriority;
import com.hbm.energy.ItemChargeStorage;
import com.hbm.inventory.menu.MachineBatteryMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.MachineBatteryPacket;
import com.hbm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MachineBatteryScreen extends AbstractContainerScreen<MachineBatteryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/storage/gui_battery.png");

    public MachineBatteryScreen(MachineBatteryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        int maxEnergy = this.menu.getMaxEnergy();
        if (maxEnergy > 0) {
            int energy = this.menu.getEnergy();
            int h = energy * 52 / maxEnergy;
            if (h > 0) {
                graphics.blit(TEXTURE, x + 62, y + 69 - h, 176, 52 - h, 52, h);
            }
        }
        graphics.blit(TEXTURE, x + 133, y + 16, 176, 52 + this.menu.getRedLow() * 18, 18, 18);
        graphics.blit(TEXTURE, x + 133, y + 52, 176, 52 + this.menu.getRedHigh() * 18, 18, 18);
        graphics.blit(TEXTURE, x + 152, y + 35, 194, 52 + this.menu.getPriority().ordinal() * 16 - 16, 16, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(62, 17, 52, 52, mouseX, mouseY)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(ItemChargeStorage.shortNumber(this.menu.getEnergy())
                    + "/" + ItemChargeStorage.shortNumber(this.menu.getMaxEnergy()) + "FE"));
            int delta = this.menu.getDelta();
            String rate = ItemChargeStorage.shortNumber(Math.abs(delta)) + "FE/s";
            ChatFormatting color = delta > 0 ? ChatFormatting.GREEN
                    : delta < 0 ? ChatFormatting.RED : ChatFormatting.YELLOW;
            String signed = (delta >= 0 ? "+" : "-") + rate;
            lines.add(Component.literal(signed).withStyle(color));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
        if (isHovering(152, 35, 16, 16, mouseX, mouseY)) {
            graphics.renderComponentTooltip(this.font, priorityTooltip(this.menu.getPriority()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isHovering(133, 16, 18, 18, (int) mouseX, (int) mouseY)) {
                click(0);
                return true;
            }
            if (isHovering(133, 52, 18, 18, (int) mouseX, (int) mouseY)) {
                click(1);
                return true;
            }
            if (isHovering(152, 35, 16, 16, (int) mouseX, (int) mouseY)) {
                click(2);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void click(int id) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        ModMessages.CHANNEL.sendToServer(new MachineBatteryPacket(this.menu.getBlockEntity().getBlockPos(), id));
    }

    private static List<Component> priorityTooltip(ConnectionPriority priority) {
        String key = switch (priority) {
            case HIGH, HIGHEST -> "high";
            case LOW, LOWEST -> "low";
            default -> "normal";
        };
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("battery.priority." + key));
        lines.add(Component.translatable("battery.priority.recommended"));
        String desc = Component.translatable("battery.priority." + key + ".desc").getString();
        for (String part : desc.split("\\$")) {
            if (!part.isEmpty()) {
                lines.add(Component.literal(part));
            }
        }
        return lines;
    }
}
