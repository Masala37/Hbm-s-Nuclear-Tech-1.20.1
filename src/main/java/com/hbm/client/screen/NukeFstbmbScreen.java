package com.hbm.client.screen;

import com.hbm.inventory.menu.NukeFstbmbMenu;
import com.hbm.lib.RefStrings;
import com.hbm.network.FstbmbButtonPacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class NukeFstbmbScreen extends AbstractContainerScreen<NukeFstbmbMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/weapon/fstbmb_schematic.png");

    private EditBox timerField;

    public NukeFstbmbScreen(NukeFstbmbMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.timerField = new EditBox(this.font, this.leftPos + 94, this.topPos + 40, 29, 12, Component.empty());
        this.timerField.setTextColor(0xff0000);
        this.timerField.setTextColorUneditable(0x800000);
        this.timerField.setBordered(false);
        this.timerField.setMaxLength(3);
        this.timerField.setValue(String.valueOf(this.menu.getBlockEntity().getTimer() / 20));
        this.timerField.setResponder(this::onTimerTyped);
        this.timerField.setEditable(!this.menu.getBlockEntity().isStarted());
        this.addRenderableWidget(this.timerField);
    }

    private void onTimerTyped(String text) {
        if (this.menu.getBlockEntity().isStarted()) {
            return;
        }
        try {
            int seconds = Mth.clamp((int) Double.parseDouble(text.trim()), 1, 999);
            ModMessages.CHANNEL.sendToServer(new FstbmbButtonPacket(
                    this.menu.getBlockEntity().getBlockPos(), seconds, 1));
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.menu.getBlockEntity().isStarted()
                && mouseX >= this.leftPos + 142 && mouseX < this.leftPos + 160
                && mouseY > this.topPos + 35 && mouseY <= this.topPos + 53) {
            ModMessages.CHANNEL.sendToServer(new FstbmbButtonPacket(
                    this.menu.getBlockEntity().getBlockPos(), 0, 0));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var bomb = this.menu.getBlockEntity();
        this.timerField.setEditable(!bomb.isStarted());
        if (bomb.isStarted()) {
            this.timerField.setValue(String.valueOf(bomb.getTimer() / 20));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        if (this.menu.getBlockEntity().getBatteryTier() > 0) {
            String timer = this.menu.getBlockEntity().getMinutes() + ":" + this.menu.getBlockEntity().getSeconds();
            float scale = 0.75F;
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, scale);
            int tx = (int) ((69 - this.font.width(timer) / 2.0F) / scale);
            int ty = (int) (95.5F / scale);
            graphics.drawString(this.font, timer, tx, ty, 0xff0000, false);
            graphics.pose().popPose();
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        var bomb = this.menu.getBlockEntity();
        if (bomb.hasEgg()) {
            graphics.blit(TEXTURE, x + 19, y + 90, 176, 0, 30, 16);
        }

        int battery = bomb.getBatteryTier();
        if (battery == 1) {
            graphics.blit(TEXTURE, x + 88, y + 93, 176, 16, 18, 10);
        } else if (battery == 2) {
            graphics.blit(TEXTURE, x + 88, y + 93, 194, 16, 18, 10);
        }

        if (bomb.isStarted()) {
            graphics.blit(TEXTURE, x + 142, y + 35, 176, 26, 18, 18);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
