package com.hbm.client.screen;

import api.hbm.entity.RadarEntry;
import com.hbm.blockentity.machine.RadarNTBlockEntity;
import com.hbm.handler.RadarRules;
import com.hbm.lib.RefStrings;
import com.hbm.network.ModMessages;
import com.hbm.network.RadarControlPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class RadarNTScreen extends Screen {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(RefStrings.MODID, "textures/gui/machine/gui_radar_nt.png");

    private final BlockPos pos;
    protected int imageWidth = 216;
    protected int imageHeight = 234;
    protected int leftPos;
    protected int topPos;
    public int lastMouseX;
    public int lastMouseY;

    public RadarNTScreen(BlockPos pos) {
        super(Component.translatable("container.radar"));
        this.pos = pos;
    }

    public BlockPos getRadarPos() {
        return pos;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    private RadarNTBlockEntity radar() {
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        BlockEntity be = minecraft.level.getBlockEntity(pos);
        return be instanceof RadarNTBlockEntity radar ? radar : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        if (!minecraft.player.isAlive() || radar() == null) {
            onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String cmd = null;
        int x = (int) mouseX;
        int y = (int) mouseY;
        if (checkClick(x, y, -10, 88, 8, 8)) {
            cmd = "missiles";
        }
        if (checkClick(x, y, -10, 98, 8, 8)) {
            cmd = "shells";
        }
        if (checkClick(x, y, -10, 108, 8, 8)) {
            cmd = "players";
        }
        if (checkClick(x, y, -10, 118, 8, 8)) {
            cmd = "smart";
        }
        if (checkClick(x, y, -10, 128, 8, 8)) {
            cmd = "red";
        }
        if (checkClick(x, y, -10, 138, 8, 8)) {
            cmd = "map";
        }
        if (checkClick(x, y, -10, 158, 8, 8)) {
            cmd = "gui1";
        }
        if (checkClick(x, y, -10, 178, 8, 8)) {
            cmd = "clear";
        }
        if (cmd != null) {
            CompoundTag data = new CompoundTag();
            data.putBoolean(cmd, true);
            ModMessages.CHANNEL.sendToServer(new RadarControlPacket(pos, data));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (minecraft != null && (keyCode == GLFW.GLFW_KEY_ESCAPE
                || minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        RadarNTBlockEntity radar = radar();
        if (radar != null && checkClick(lastMouseX, lastMouseY, 8, 17, 200, 200)
                && codePoint >= '1' && codePoint <= '8') {
            int id = codePoint - '1';
            if (!radar.entries.isEmpty()) {
                for (RadarEntry m : radar.entries) {
                    int bx = leftPos + (int) ((m.posX - pos.getX()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 108;
                    int bz = topPos + (int) ((m.posZ - pos.getZ()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 117;
                    if (lastMouseX + 5 > bx && lastMouseX - 4 <= bx && lastMouseY + 5 > bz && lastMouseY - 4 <= bz) {
                        CompoundTag data = new CompoundTag();
                        data.putInt("launchEntity", m.entityID);
                        data.putInt("link", id);
                        ModMessages.CHANNEL.sendToServer(new RadarControlPacket(pos, data));
                        return true;
                    }
                }
            }
            int tX = (int) ((lastMouseX - leftPos - 108) * ((double) radar.getRange() * 2 + 1) / 192D + pos.getX());
            int tZ = (int) ((lastMouseY - topPos - 117) * ((double) radar.getRange() * 2 + 1) / 192D + pos.getZ());
            CompoundTag data = new CompoundTag();
            data.putInt("launchPosX", tX);
            data.putInt("launchPosZ", tZ);
            data.putInt("link", id);
            ModMessages.CHANNEL.sendToServer(new RadarControlPacket(pos, data));
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        renderBg(graphics, partialTick, mouseX, mouseY);
        renderFg(graphics, mouseX, mouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderFg(GuiGraphics graphics, int mouseX, int mouseY) {
        RadarNTBlockEntity radar = radar();
        if (radar == null) {
            return;
        }
        if (checkClick(mouseX, mouseY, 8, 221, 200, 7)) {
            graphics.renderComponentTooltip(font, List.of(Component.literal(
                    radar.getEnergy().getEnergyStored() + "/" + RadarRules.MAX_POWER + "HE")), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 88, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.detectMissiles"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 98, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.detectShells"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 108, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.detectPlayers"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 118, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.smartMode"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 128, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.redMode"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 138, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.showMap"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 158, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.toggleGui"), mouseX, mouseY);
        }
        if (checkClick(mouseX, mouseY, -10, 178, 8, 8)) {
            graphics.renderComponentTooltip(font, dollar("radar.clearMap"), mouseX, mouseY);
        }
        if (!radar.entries.isEmpty()) {
            for (RadarEntry m : radar.entries) {
                int x = leftPos + (int) ((m.posX - pos.getX()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 108;
                int z = topPos + (int) ((m.posZ - pos.getZ()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D)) + 117;
                if (mouseX + 5 > x && mouseX - 4 <= x && mouseY + 5 > z && mouseY - 4 <= z) {
                    List<Component> text = List.of(
                            Component.translatable(m.unlocalizedName),
                            Component.literal(m.posX + " / " + m.posZ),
                            Component.literal("Alt.: " + m.posY));
                    graphics.renderComponentTooltip(font, text, x, z);
                    return;
                }
            }
        }
        if (checkClick(mouseX, mouseY, 8, 17, 200, 200)) {
            int tX = (int) ((lastMouseX - leftPos - 108) * ((double) radar.getRange() * 2 + 1) / 192D + pos.getX());
            int tZ = (int) ((lastMouseY - topPos - 117) * ((double) radar.getRange() * 2 + 1) / 192D + pos.getZ());
            graphics.renderComponentTooltip(font, List.of(Component.literal(tX + " / " + tZ)), lastMouseX, lastMouseY);
        }
    }

    private void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RadarNTBlockEntity radar = radar();
        if (radar == null || minecraft == null || minecraft.level == null) {
            return;
        }
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        graphics.blit(TEXTURE, leftPos - 14, topPos + 84, 224, 0, 14, 66);
        graphics.blit(TEXTURE, leftPos - 14, topPos + 154, 224, 66, 14, 36);

        if (radar.getEnergy().getEnergyStored() > 0) {
            int i = (int) ((long) radar.getEnergy().getEnergyStored() * 200 / RadarRules.MAX_POWER);
            graphics.blit(TEXTURE, leftPos + 8, topPos + 221, 0, 234, i, 16);
        }

        boolean jam = radar.jammed && minecraft.level.random.nextBoolean();
        if (radar.scanMissiles ^ jam) {
            graphics.blit(TEXTURE, leftPos - 10, topPos + 88, 238, 4, 8, 8);
        }
        if (radar.scanShells ^ jam) {
            graphics.blit(TEXTURE, leftPos - 10, topPos + 98, 238, 14, 8, 8);
        }
        if (radar.scanPlayers ^ jam) {
            graphics.blit(TEXTURE, leftPos - 10, topPos + 108, 238, 24, 8, 8);
        }
        if (radar.smartMode ^ jam) {
            graphics.blit(TEXTURE, leftPos - 10, topPos + 118, 238, 34, 8, 8);
        }
        if (radar.redMode ^ jam) {
            graphics.blit(TEXTURE, leftPos - 10, topPos + 128, 238, 44, 8, 8);
        }
        if (radar.showMap ^ jam) {
            graphics.blit(TEXTURE, leftPos - 10, topPos + 138, 238, 54, 8, 8);
        }

        if (radar.getEnergy().getEnergyStored() < RadarRules.CONSUMPTION) {
            return;
        }

        if (radar.jammed) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    graphics.blit(TEXTURE, leftPos + 8 + i * 40, topPos + 17 + j * 40,
                            216, 118 + minecraft.level.random.nextInt(81), 40, 40);
                }
            }
            return;
        }

        if (radar.showMap && radar.map != null && radar.map.length == RadarRules.MAP_SIZE) {
            Matrix4f mat = graphics.pose().last().pose();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.getBuilder();
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < RadarRules.MAP_SIZE; i++) {
                byte b = radar.map[i];
                if (b > 0) {
                    int iX = i % 200;
                    int iZ = i / 200;
                    int g = (b - 50) * 255 / 78;
                    int color = 0xFF000000 | (g << 8);
                    int x0 = leftPos + 8 + iX;
                    int y0 = topPos + 17 + iZ;
                    fill(buf, mat, x0, y0, x0 + 1, y0 + 1, color);
                }
            }
            tess.end();
        }

        float rot = (float) -Math.toRadians(radar.prevRotation + (radar.rotation - radar.prevRotation) * partialTick + 180.0F);
        double trX = 100.0D * Math.cos(rot);
        double trY = 100.0D * Math.sin(rot);
        double tlX = 100.0D * Math.cos(rot + 0.25D);
        double tlY = 100.0D * Math.sin(rot + 0.25D);
        double blX = -5.0D * Math.sin(rot);
        double blY = -5.0D * Math.cos(rot);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f mat = graphics.pose().last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float cx = leftPos + 108;
        float cy = topPos + 117;
        buf.vertex(mat, cx, cy, 0).color(0, 255, 0, 0).endVertex();
        buf.vertex(mat, cx + (float) trX, cy + (float) trY, 0).color(0, 255, 0, 255).endVertex();
        buf.vertex(mat, cx + (float) tlX, cy + (float) tlY, 0).color(0, 255, 0, 0).endVertex();
        buf.vertex(mat, cx + (float) blX, cy + (float) blY, 0).color(0, 255, 0, 0).endVertex();
        tess.end();
        RenderSystem.disableBlend();

        if (!radar.entries.isEmpty()) {
            for (RadarEntry m : radar.entries) {
                double x = (m.posX - pos.getX()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D) - 4D;
                double z = (m.posZ - pos.getZ()) / ((double) radar.getRange() * 2 + 1) * (200D - 8D) - 4D;
                blitDouble(graphics, leftPos + 108 + x, topPos + 117 + z, 216, 8 * m.blipLevel, 8, 8);
            }
        }
    }

    private static void fill(BufferBuilder buf, Matrix4f mat, int x0, int y0, int x1, int y1, int argb) {
        int a = (argb >> 24) & 255;
        int r = (argb >> 16) & 255;
        int g = (argb >> 8) & 255;
        int b = argb & 255;
        buf.vertex(mat, x0, y1, 0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, 0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, 0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y0, 0).color(r, g, b, a).endVertex();
    }

    private void blitDouble(GuiGraphics graphics, double x, double y, int u, int v, int w, int h) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.blit(TEXTURE, 0, 0, u, v, w, h);
        graphics.pose().popPose();
    }

    private static List<Component> dollar(String key) {
        String raw = I18n.get(key);
        String[] parts = raw.split("\\$");
        List<Component> list = new ArrayList<>(parts.length);
        for (String part : parts) {
            list.add(Component.literal(part));
        }
        return list;
    }

    protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
        return leftPos + left <= x && leftPos + left + sizeX > x && topPos + top < y && topPos + top + sizeY >= y;
    }
}
