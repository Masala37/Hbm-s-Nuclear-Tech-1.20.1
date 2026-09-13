package com.hbm.client.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * 1.7 Techne {@code ModelM65} for red/grey hazmat hoods.
 */
public class ModelM65 extends HumanoidModel<LivingEntity> {
    private static final float SCALE = (18.0F / 16.0F) * 1.01F;

    public ModelM65(ModelPart root) {
        super(root);
    }

    public static ModelM65 create() {
        return new ModelM65(createBodyLayer().bakeRoot());
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

        float y = 0.5F;
        head.addOrReplaceChild("mask_head", CubeListBuilder.create().texOffs(0, 0).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(-4.0F, -8.0F + y, -4.0F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 16).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F), PartPose.offset(-1.5F, -3.5F + y, -5.0F));
        head.addOrReplaceChild("outlet", CubeListBuilder.create().texOffs(0, 20).mirror(true)
                .addBox(0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -3.5F + y, -5.0F, -0.4799655F, 0.0F, 0.0F));
        head.addOrReplaceChild("nose_slope", CubeListBuilder.create().texOffs(8, 16).mirror(true)
                .addBox(0.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F));
        head.addOrReplaceChild("eye1", CubeListBuilder.create().texOffs(0, 23).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 0.0F), PartPose.offset(-3.5F, -6.0F + y, -4.2F));
        head.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(0, 26).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 0.0F), PartPose.offset(0.5F, -6.0F + y, -4.2F));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(6, 20).mirror(true)
                .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F), PartPose.offset(-1.0F, -3.2F + y, -6.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        pose.pushPose();
        pose.scale(SCALE, SCALE, SCALE);
        super.renderToBuffer(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha);
        pose.popPose();
    }
}
