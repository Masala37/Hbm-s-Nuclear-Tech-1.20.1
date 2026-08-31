package com.hbm.client.screen;

import com.hbm.items.tool.DesignatorManualCoords;
import com.hbm.items.tool.DesignatorManualItem;
import com.hbm.lib.RefStrings;
import com.hbm.network.ItemDesignatorPacket;
import com.hbm.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DesignatorManualScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/gui_designator.png");

    private final Player player;
    private final int imageWidth = 176;
    private final int imageHeight = 178;
    private int leftPos;
    private int topPos;
    private int shownX;
    private int shownZ;
    private final List<FolderButton> buttons = new ArrayList<>();

    public DesignatorManualScreen(Player player) {
        super(Component.empty());
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.shownX = 0;
        this.shownZ = 0;
        ItemStack stack = DesignatorManualItem.held(this.player);
        if (!stack.isEmpty() && stack.hasTag()) {
            this.shownX = DesignatorManualItem.readX(stack);
            this.shownZ = DesignatorManualItem.readZ(stack);
        }
        updateButtons();
    }

    private void updateButtons() {
        this.buttons.clear();
        this.buttons.add(new FolderButton(this.leftPos + 25, this.topPos + 26, 0, 0, 0, 1, null));
        this.buttons.add(new FolderButton(this.leftPos + 52, this.topPos + 26, 1, 0, 0, 5, null));
        this.buttons.add(new FolderButton(this.leftPos + 79, this.topPos + 26, 2, 0, 0, 10, null));
        this.buttons.add(new FolderButton(this.leftPos + 106, this.topPos + 26, 3, 0, 0, 50, null));
        this.buttons.add(new FolderButton(this.leftPos + 133, this.topPos + 26, 4, 0, 0, 100, null));

        this.buttons.add(new FolderButton(this.leftPos + 25, this.topPos + 62, 5, 1, 0, 1, null));
        this.buttons.add(new FolderButton(this.leftPos + 52, this.topPos + 62, 6, 1, 0, 5, null));
        this.buttons.add(new FolderButton(this.leftPos + 79, this.topPos + 62, 7, 1, 0, 10, null));
        this.buttons.add(new FolderButton(this.leftPos + 106, this.topPos + 62, 8, 1, 0, 50, null));
        this.buttons.add(new FolderButton(this.leftPos + 133, this.topPos + 62, 9, 1, 0, 100, null));

        this.buttons.add(new FolderButton(this.leftPos + 133, this.topPos + 44, 10, 2, 0, 0,
                "Set coord to current X position..."));

        this.buttons.add(new FolderButton(this.leftPos + 25, this.topPos + 26 + 72, 0, 0, 1, 1, null));
        this.buttons.add(new FolderButton(this.leftPos + 52, this.topPos + 26 + 72, 1, 0, 1, 5, null));
        this.buttons.add(new FolderButton(this.leftPos + 79, this.topPos + 26 + 72, 2, 0, 1, 10, null));
        this.buttons.add(new FolderButton(this.leftPos + 106, this.topPos + 26 + 72, 3, 0, 1, 50, null));
        this.buttons.add(new FolderButton(this.leftPos + 133, this.topPos + 26 + 72, 4, 0, 1, 100, null));

        this.buttons.add(new FolderButton(this.leftPos + 25, this.topPos + 62 + 72, 5, 1, 1, 1, null));
        this.buttons.add(new FolderButton(this.leftPos + 52, this.topPos + 62 + 72, 6, 1, 1, 5, null));
        this.buttons.add(new FolderButton(this.leftPos + 79, this.topPos + 62 + 72, 7, 1, 1, 10, null));
        this.buttons.add(new FolderButton(this.leftPos + 106, this.topPos + 62 + 72, 8, 1, 1, 50, null));
        this.buttons.add(new FolderButton(this.leftPos + 133, this.topPos + 62 + 72, 9, 1, 1, 100, null));

        this.buttons.add(new FolderButton(this.leftPos + 133, this.topPos + 44 + 72, 10, 2, 1, 0,
                "Set coord to current Z position..."));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        FolderButton hovered = null;
        for (FolderButton button : this.buttons) {
            boolean over = button.isMouseOnButton(mouseX, mouseY);
            button.drawButton(graphics, over);
            if (over) {
                hovered = button;
            }
        }
        String x = "X: " + this.shownX;
        String z = "Z: " + this.shownZ;
        graphics.drawString(this.font, x,
                this.leftPos + this.imageWidth / 2 - this.font.width(x) / 2, this.topPos + 50, 0x404040, false);
        graphics.drawString(this.font, z,
                this.leftPos + this.imageWidth / 2 - this.font.width(z) / 2, this.topPos + 50 + 18 * 4, 0x404040, false);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (hovered != null && hovered.info != null && !hovered.info.isEmpty()) {
            graphics.renderTooltip(this.font, Component.literal(hovered.info), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (int) mouseX;
            int y = (int) mouseY;
            for (FolderButton folderButton : this.buttons) {
                if (folderButton.isMouseOnButton(x, y)) {
                    folderButton.executeAction();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || this.minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        if (DesignatorManualItem.held(this.player).isEmpty()) {
            onClose();
        }
    }

    class FolderButton {
        final int xPos;
        private final int yPos;
        private final int type;
        private final int operator;
        private final int value;
        private final int reference;
        final String info;

        FolderButton(int x, int y, int t, int o, int r, int v, String i) {
            this.xPos = x;
            this.yPos = y;
            this.type = t;
            this.operator = o;
            this.value = v;
            this.reference = r;
            this.info = i;
        }

        boolean isMouseOnButton(int mouseX, int mouseY) {
            return this.xPos <= mouseX && this.xPos + 18 > mouseX && this.yPos < mouseY && this.yPos + 18 >= mouseY;
        }

        void drawButton(GuiGraphics graphics, boolean hovered) {
            graphics.blit(TEXTURE, this.xPos, this.yPos, hovered ? 176 + 18 : 176, this.type * 18, 18, 18);
        }

        void executeAction() {
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            ModMessages.CHANNEL.sendToServer(new ItemDesignatorPacket(this.operator, this.value, this.reference));
            if (this.operator == 2) {
                if (this.reference == 0) {
                    shownX = (int) Math.round(player.getX());
                } else {
                    shownZ = (int) Math.round(player.getZ());
                }
                return;
            }
            int result = DesignatorManualCoords.next(0, this.operator, this.value, 0);
            if (this.reference == 0) {
                shownX += result;
            } else {
                shownZ += result;
            }
        }
    }
}
