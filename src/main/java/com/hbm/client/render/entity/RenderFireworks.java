package com.hbm.client.render.entity;

import com.hbm.entity.item.EntityFireworks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Billboard letter burst for {@link EntityFireworks} (legacy {@code ParticleLetter}).
 */
public class RenderFireworks extends EntityRenderer<EntityFireworks> {
    public RenderFireworks(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityFireworks entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (!entity.isLetterMode()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        String text = String.valueOf(entity.getCharacter());
        float age = entity.getLetterAge() + partialTicks;
        float maxAge = 30.0F;
        float time = age * 4.0F / maxAge;
        double scale = 1.0D - (1.0D / Math.exp(time));
        float alpha = 1.0F - (age / maxAge);
        alpha = Mth.clamp(alpha, 0.04F, 1.0F);
        int a = Mth.clamp((int) (alpha * 255.0F), 10, 255);
        int color = (entity.getColor() & 0xFFFFFF) | (a << 24);

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        // Legacy: flip then grow to ~1 block/pixel scale for skywriting-sized glyphs.
        float s = (float) Math.max(0.15D, scale);
        poseStack.scale(-s, -s, s);

        float x = -font.width(text) * 0.5F;
        float y = -font.lineHeight * 0.5F;
        font.drawInBatch(text, x, y, color, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFireworks entity) {
        return new ResourceLocation("minecraft", "textures/misc/white.png");
    }
}
