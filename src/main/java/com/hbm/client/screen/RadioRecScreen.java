package com.hbm.client.screen;

import com.hbm.blockentity.machine.RadioRecBlockEntity;
import com.hbm.lib.RefStrings;
import com.hbm.network.ModMessages;
import com.hbm.network.RadioRecControlPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

/** 1.7 {@code GUIRadioRec}. */
public class RadioRecScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/machine/gui_radio.png");

    private final RadioRecBlockEntity radio;
    private final int imageWidth = 220;
    private final int imageHeight = 42;
    private int leftPos;
    private int topPos;
    private EditBox frequency;

    public RadioRecScreen(RadioRecBlockEntity radio) {
        super(Component.translatable("container.radiorec"));
        this.radio = radio;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.frequency = new EditBox(this.font, leftPos + 29, topPos + 21, 82, 14, Component.empty());
        this.frequency.setTextColor(0x00ff00);
        this.frequency.setTextColorUneditable(0x00ff00);
        this.frequency.setBordered(false);
        this.frequency.setMaxLength(10);
        this.frequency.setValue(radio.channel() == null ? "" : radio.channel());
        this.addRenderableWidget(this.frequency);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (radio.isOn()) {
            graphics.blit(TEXTURE, leftPos + 173, topPos + 17, 0, 42, 18, 18);
        }
        graphics.drawString(this.font, this.title,
                leftPos + imageWidth / 2 - this.font.width(this.title) / 2,
                topPos + 6, 0x404040, false);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hover(137, 17, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Save Settings"), mouseX, mouseY);
        }
        if (hover(173, 17, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(this.font, Component.literal("Toggle"), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hover(137, 17, 18, 18, (int) mouseX, (int) mouseY)) {
            click();
            ModMessages.CHANNEL.sendToServer(new RadioRecControlPacket(radio.getBlockPos(), this.frequency.getValue()));
            return true;
        }
        if (hover(173, 17, 18, 18, (int) mouseX, (int) mouseY)) {
            click();
            boolean next = !radio.isOn();
            radio.setOn(next);
            ModMessages.CHANNEL.sendToServer(new RadioRecControlPacket(radio.getBlockPos(), next));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.frequency.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean hover(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w
                && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    private void click() {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
