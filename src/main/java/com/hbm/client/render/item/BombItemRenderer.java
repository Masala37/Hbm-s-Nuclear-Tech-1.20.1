package com.hbm.client.render.item;

import com.hbm.blocks.bomb.AssembledNukeBlock;
import com.hbm.blocks.bomb.BombMultiBlock;
import com.hbm.blocks.bomb.CrashedBombBlock;
import com.hbm.blocks.bomb.DudType;
import com.hbm.client.render.ObjModelRenderer;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Legacy {@code ItemRenderBase} / TESR {@code getRenderer()} inventory for OBJ bombs.
 * Item JSON display must stay identity ({@code missile_bewlr}) so these poses are not doubled.
 */
public class BombItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static BombItemRenderer instance;

    public BombItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    public static BombItemRenderer get() {
        if (instance == null) {
            instance = new BombItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        String path = path(stack);
        if (path == null) {
            return;
        }

        pose.pushPose();
        if (context == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
            ItemRenderBaseInventory.applyInventory(pose);
            applyInventory(path, pose);
        } else {
            pose.translate(0.5F, 0.5F, 0.5F);
            ItemRenderBaseInventory.applyNonInventory(context, pose);
            applyNonInv(path, pose);
        }
        applyCommon(path, stack, pose);

        BlockState state = itemState(blockItem.getBlock(), stack);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        ObjModelRenderer.render(pose, buffers, model, state, LightTexture.FULL_BRIGHT, packedOverlay);
        pose.popPose();
    }

    private static String path(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? null : key.getPath();
    }

    private static BlockState itemState(Block block, ItemStack stack) {
        BlockState state = block.defaultBlockState();
        if (block instanceof AssembledNukeBlock && state.hasProperty(AssembledNukeBlock.FACING)) {
            state = state.setValue(AssembledNukeBlock.FACING, Direction.NORTH);
        }
        if (block instanceof BombMultiBlock && state.hasProperty(BombMultiBlock.FACING)) {
            state = state.setValue(BombMultiBlock.FACING, Direction.NORTH);
        }
        if (block instanceof CrashedBombBlock) {
            state = state.setValue(CrashedBombBlock.TYPE, CrashedBombBlock.typeFromStack(stack));
        }
        return state;
    }

    /** 1.7 {@code renderInventory()} after the shared ItemRenderBase inventory pose. */
    private static void applyInventory(String path, PoseStack pose) {
        switch (path) {
            case "nuke_boy", "nuke_custom" -> pose.scale(5.0F, 5.0F, 5.0F);
            case "nuke_fstbmb" -> pose.scale(2.25F, 2.25F, 2.25F);
            case "nuke_man" -> {
                pose.translate(0.0F, -2.0F, 0.0F);
                pose.scale(5.0F, 5.0F, 5.0F);
            }
            case "nuke_gadget" -> {
                pose.translate(0.0F, -3.0F, 0.0F);
                pose.scale(5.0F, 5.0F, 5.0F);
            }
            case "nuke_tsar" -> pose.scale(2.25F, 2.25F, 2.25F);
            case "nuke_mike", "nuke_n2" -> {
                pose.translate(0.0F, -5.0F, 0.0F);
                pose.scale(2.25F, 2.25F, 2.25F);
            }
            case "nuke_solinium" -> {
                pose.translate(0.0F, -0.125F, 0.0F);
                pose.scale(5.0F, 5.0F, 5.0F);
            }
            case "nuke_prototype" -> {
                pose.translate(0.0F, 0.125F, 0.0F);
                pose.scale(3.0F, 3.0F, 3.0F);
            }
            case "nuke_fleija" -> pose.scale(6.8F, 6.8F, 6.8F);
            case "bomb_multi" -> {
                pose.translate(0.0F, -1.0F, 0.0F);
                pose.scale(4.0F, 4.0F, 4.0F);
            }
            case "mine_ap", "mine_shrap" -> pose.scale(8.0F, 8.0F, 8.0F);
            case "mine_he" -> pose.scale(6.0F, 6.0F, 6.0F);
            case "mine_naval" -> {
                pose.translate(0.0F, 2.0F, -1.0F);
                pose.scale(5.0F, 5.0F, 5.0F);
            }
            case "mine_fat" -> {
                pose.translate(0.0F, -1.0F, 0.0F);
                pose.scale(7.0F, 7.0F, 7.0F);
            }
            case "crashed_bomb" -> {
                pose.translate(0.0F, 3.0F, 0.0F);
                pose.scale(2.125F, 2.125F, 2.125F);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            default -> pose.scale(5.0F, 5.0F, 5.0F);
        }
    }

    /** 1.7 {@code renderNonInv()} — HE mine only. */
    private static void applyNonInv(String path, PoseStack pose) {
        if ("mine_he".equals(path)) {
            pose.translate(0.25F, 0.625F, 0.0F);
            pose.mulPose(Axis.YP.rotationDegrees(45.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(-15.0F));
        }
    }

    /** 1.7 {@code renderCommon()} / {@code renderCommonWithStack()}. */
    private static void applyCommon(String path, ItemStack stack, PoseStack pose) {
        switch (path) {
            case "nuke_boy", "nuke_custom" -> pose.translate(-1.0F, 0.0F, 0.0F);
            case "nuke_fstbmb" -> {
                pose.translate(1.0F, 0.0F, 0.0F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case "nuke_man" -> {
                pose.mulPose(Axis.YP.rotationDegrees(180.0F));
                pose.translate(-0.75F, 0.0F, 0.0F);
            }
            case "nuke_gadget" -> pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case "nuke_tsar" -> pose.translate(1.5F, 0.0F, 0.0F);
            case "nuke_solinium" -> {
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                pose.translate(0.0F, -0.125F, 0.0F);
            }
            case "nuke_prototype" -> {
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                pose.translate(0.0F, 0.125F, 0.0F);
            }
            case "nuke_fleija" -> {
                pose.translate(0.125F, 0.0F, 0.0F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case "bomb_multi" -> {
                pose.translate(0.75F, 0.0F, 0.0F);
                pose.scale(3.0F, 3.0F, 3.0F);
                pose.translate(0.0F, 0.5F, 0.0F);
                pose.mulPose(Axis.XP.rotationDegrees(180.0F));
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case "mine_ap", "mine_shrap" -> pose.scale(1.25F, 1.25F, 1.25F);
            case "mine_he" -> pose.scale(4.0F, 4.0F, 4.0F);
            case "mine_fat" -> {
                pose.translate(0.25F, 0.0F, 0.0F);
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case "crashed_bomb" -> {
                pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                DudType type = CrashedBombBlock.typeFromStack(stack);
                switch (type) {
                    case CONVENTIONAL -> pose.translate(0.0F, 0.0F, -0.5F);
                    case NUKE -> pose.translate(0.0F, 0.0F, 1.25F);
                    case SALTED -> pose.translate(0.0F, 0.0F, 0.5F);
                    default -> {
                    }
                }
            }
            default -> {
            }
        }
    }
}
