package com.hbm.client.render;

import com.hbm.HbmNuclearTechMod;
import com.hbm.lib.RefStrings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Matrix4f;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wavefront OBJ with named {@code o}/{@code g} groups. Used for the launch-pad erector
 * (1.7.10 {@code HFRWavefrontObject.renderPart}).
 */
public final class ObjPartModel {
    private static final Map<ResourceLocation, ObjPartModel> CACHE = new HashMap<>();

    private final Map<String, float[]> parts = new HashMap<>();

    private ObjPartModel() {
    }

    public static ObjPartModel get(ResourceLocation id) {
        return CACHE.computeIfAbsent(id, ObjPartModel::load);
    }

    public boolean hasPart(String name) {
        return parts.containsKey(name);
    }

    public void renderAll(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                          int packedLight, int packedOverlay) {
        renderAll(pose, buffers, texture, packedLight, packedOverlay, false);
    }

    public void renderAll(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                          int packedLight, int packedOverlay, boolean noCull) {
        for (String name : parts.keySet()) {
            render(pose, buffers, texture, name, packedLight, packedOverlay, noCull);
        }
    }

    public void render(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                       String part, int packedLight, int packedOverlay) {
        render(pose, buffers, texture, part, packedLight, packedOverlay, false);
    }

    public void render(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                       String part, int packedLight, int packedOverlay, boolean noCull) {
        float[] tris = parts.get(part);
        if (tris == null || tris.length == 0) {
            return;
        }
        VertexConsumer buffer = buffers.getBuffer(noCull
                ? RenderType.entityCutoutNoCull(texture)
                : RenderType.entityCutout(texture));
        Matrix4f mat = pose.last().pose();
        org.joml.Matrix3f nrm = pose.last().normal();
        org.joml.Vector3f normal = new org.joml.Vector3f();
        // entityCutout is Mode.QUADS; emit a degenerate 4th vertex per triangle.
        for (int i = 0; i + 23 < tris.length; i += 24) {
            emitVert(buffer, mat, nrm, normal, tris, i, packedLight, packedOverlay);
            emitVert(buffer, mat, nrm, normal, tris, i + 8, packedLight, packedOverlay);
            emitVert(buffer, mat, nrm, normal, tris, i + 16, packedLight, packedOverlay);
            emitVert(buffer, mat, nrm, normal, tris, i + 16, packedLight, packedOverlay);
        }
    }

    private static void emitVert(VertexConsumer buffer, Matrix4f mat, org.joml.Matrix3f nrm,
                                 org.joml.Vector3f normal, float[] tris, int i,
                                 int packedLight, int packedOverlay) {
        nrm.transform(tris[i + 5], tris[i + 6], tris[i + 7], normal);
        if (normal.lengthSquared() < 1.0E-8F) {
            normal.set(0.0F, 1.0F, 0.0F);
        } else {
            normal.normalize();
        }
        buffer.vertex(mat, tris[i], tris[i + 1], tris[i + 2])
                .color(255, 255, 255, 255)
                .uv(tris[i + 3], tris[i + 4])
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal.x, normal.y, normal.z)
                .endVertex();
    }

    private static ObjPartModel load(ResourceLocation id) {
        ObjPartModel model = new ObjPartModel();
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
        if (resource.isEmpty()) {
            HbmNuclearTechMod.LOGGER.warn("Missing OBJ {}", id);
            return model;
        }
        List<float[]> verts = new ArrayList<>();
        List<float[]> uvs = new ArrayList<>();
        List<float[]> norms = new ArrayList<>();
        String current = "default";
        Map<String, List<Float>> building = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("o ") || line.startsWith("g ")) {
                    current = line.substring(2).trim();
                    if (!current.isEmpty()) {
                        building.computeIfAbsent(current, k -> new ArrayList<>());
                    }
                    continue;
                }
                String[] tok = line.split("\\s+");
                if (tok[0].equals("v") && tok.length >= 4) {
                    verts.add(new float[]{
                            Float.parseFloat(tok[1]), Float.parseFloat(tok[2]), Float.parseFloat(tok[3])});
                } else if (tok[0].equals("vt") && tok.length >= 3) {
                    uvs.add(new float[]{Float.parseFloat(tok[1]), Float.parseFloat(tok[2])});
                } else if (tok[0].equals("vn") && tok.length >= 4) {
                    norms.add(new float[]{
                            Float.parseFloat(tok[1]), Float.parseFloat(tok[2]), Float.parseFloat(tok[3])});
                } else if (tok[0].equals("f") && tok.length >= 4) {
                    List<Float> dest = building.computeIfAbsent(current, k -> new ArrayList<>());
                    int[][] idx = new int[tok.length - 1][3];
                    for (int i = 1; i < tok.length; i++) {
                        idx[i - 1] = parseIndex(tok[i], verts.size(), uvs.size(), norms.size());
                    }
                    for (int i = 1; i < idx.length - 1; i++) {
                        pushVert(dest, verts, uvs, norms, idx[0]);
                        pushVert(dest, verts, uvs, norms, idx[i]);
                        pushVert(dest, verts, uvs, norms, idx[i + 1]);
                    }
                }
            }
        } catch (Exception e) {
            HbmNuclearTechMod.LOGGER.warn("Failed to parse OBJ {}", id, e);
            return model;
        }
        for (Map.Entry<String, List<Float>> entry : building.entrySet()) {
            List<Float> list = entry.getValue();
            float[] arr = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            model.parts.put(entry.getKey(), arr);
        }
        return model;
    }

    private static void pushVert(List<Float> dest, List<float[]> verts, List<float[]> uvs,
                                  List<float[]> norms, int[] idx) {
        float[] v = verts.get(idx[0]);
        float[] vt = idx[1] >= 0 ? uvs.get(idx[1]) : new float[]{0.0F, 0.0F};
        float[] vn = idx[2] >= 0 ? norms.get(idx[2]) : new float[]{0.0F, 1.0F, 0.0F};
        dest.add(v[0]);
        dest.add(v[1]);
        dest.add(v[2]);
        dest.add(vt[0]);
        dest.add(1.0F - vt[1]);
        dest.add(vn[0]);
        dest.add(vn[1]);
        dest.add(vn[2]);
    }

    private static int[] parseIndex(String token, int vCount, int vtCount, int vnCount) {
        String[] bits = token.split("/");
        int v = resolve(bits, 0, vCount);
        int vt = resolve(bits, 1, vtCount);
        int vn = resolve(bits, 2, vnCount);
        return new int[]{v, vt, vn};
    }

    private static int resolve(String[] bits, int slot, int count) {
        if (slot >= bits.length || bits[slot].isEmpty()) {
            return -1;
        }
        int raw = Integer.parseInt(bits[slot]);
        if (raw < 0) {
            return count + raw;
        }
        return raw - 1;
    }

    public static ResourceLocation erectorObj() {
        return new ResourceLocation(RefStrings.MODID, "models/obj/launch_pad_erector.obj");
    }
}
