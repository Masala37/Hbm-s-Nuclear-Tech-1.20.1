package com.hbm.client.render.blockentity;

import com.hbm.blockentity.machine.LaunchPadBlockEntity;
import com.hbm.client.render.ObjModelRenderer;
import com.hbm.client.render.entity.RenderMissile;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

/**
 * Legacy {@code RenderLaunchPad}: silo deck OBJ + standing missile (stable fullbright lighting).
 */
public class RenderLaunchPad implements BlockEntityRenderer<LaunchPadBlockEntity> {
    public static final ResourceLocation PAD_MODEL =
            new ResourceLocation(RefStrings.MODID, "block/launch_pad_silo");

    public RenderLaunchPad(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaunchPadBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel padModel = dispatcher.getBlockModel(state);
        if (padModel == Minecraft.getInstance().getModelManager().getMissingModel()) {
            padModel = ObjModelRenderer.get(PAD_MODEL);
        }

        // Blend world light with fullbright so silo isn't pitch-black / blinking.
        int light = Math.max(packedLight, LightTexture.pack(12, 12));

        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        if (padModel != null) {
            RandomSource random = RandomSource.create(42L);
            for (RenderType type : padModel.getRenderTypes(state, random, ModelData.EMPTY)) {
                dispatcher.getModelRenderer().renderModel(
                        pose.last(),
                        buffers.getBuffer(type),
                        state,
                        padModel,
                        1.0F, 1.0F, 1.0F,
                        light,
                        packedOverlay,
                        ModelData.EMPTY,
                        type);
            }
        }
        pose.popPose();

        ItemStack missile = be.getItems().getStackInSlot(LaunchPadBlockEntity.SLOT_MISSILE);
        if (missile.isEmpty()) {
            return;
        }

        pose.pushPose();
        pose.translate(0.5D, 1.0D, 0.5D);
        RenderMissile.renderStanding(
                pose, buffers,
                RenderMissile.modelForItem(missile),
                LightTexture.FULL_BRIGHT,
                RenderMissile.isStrongItem(missile));
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaunchPadBlockEntity be) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
