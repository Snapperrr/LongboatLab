package com.xc.longboatlab.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xc.longboatlab.LongboatLab;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

/** The packaged Blockbench projects are the actual source of the entity mesh. */
public final class LongboatModels implements SimpleSynchronousResourceReloadListener {
    public static final LongboatModels INSTANCE = new LongboatModels();
    private final Map<Integer, ModelPart> hulls = boundedCache();
    private final Map<Integer, ModelPart> waterMasks = boundedCache();
    private static Map<Integer, ModelPart> boundedCache() {
        return new java.util.LinkedHashMap<>(32, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<Integer, ModelPart> entry) { return size() > 32; }
        };
    }
    private List<Cube> hullTemplate = List.of();
    private ModelPart oar;
    private record Cube(String name, float x, float y, float z, float width, float height, float depth, int u, int v) {}

    @Override public Identifier getFabricId() { return Identifier.of(LongboatLab.ID, "boat_models"); }

    @Override public void reload(ResourceManager manager) {
        hullTemplate = read(manager, "continuous_hull");
        oar = bake(read(manager, "oar"), 2, false);
        hulls.clear();
        waterMasks.clear();
    }

    public ModelPart hull(int segments) {
        return hulls.computeIfAbsent(segments, length -> bake(hullTemplate, length, true));
    }
    /** Stretch the continuous boards while translating the end caps at their original thickness. */
    public ModelPart prepareHull(int segments, double half, float extension, int width) {
        ModelPart hull = hull(segments);
        for (Cube cube : hullTemplate) {
            if (cube.name.startsWith("seat_")) continue;
            ModelPart part = hull.getChild(cube.name);
            part.zScale = 1; part.pivotZ = 0;
            part.xScale=1; part.pivotX=0;
            if(cube.name.startsWith("stretch_left_")) part.pivotX=(width-1)*11f;
            else if(cube.name.startsWith("stretch_right_")) part.pivotX=-(width-1)*11f;
            else part.xScale=width;
            if (cube.name.startsWith("stretch_")) {
                double originalStart = cube.z - (segments - 2) * 16.0;
                double desiredStart = cube.z - (half - 2) * 16;
                part.zScale = (float) ((cube.depth + (half - 2) * 32) / (cube.depth + (segments - 2) * 32.0));
                part.pivotZ = (float) (desiredStart - originalStart * part.zScale);
            } else if (cube.name.startsWith("bow_")) part.pivotZ = (float) ((half - segments) * 16);
            else if (cube.name.startsWith("stern_")) part.pivotZ = (float) ((segments - half) * 16);
        }
        for (int i = 1; i <= Math.min(512, segments - 1); i++) {
            ModelPart divider = hull.getChild("divider_" + i);
            divider.xScale=(22f*width-4)/18;
            divider.zScale = extension;
            divider.yScale = extension;
            divider.visible = extension > 0.001f;
        }
        return hull;
    }
    public ModelPart oar() { return oar; }
    public ModelPart waterMask(int segments) {
        return waterMasks.computeIfAbsent(segments, length -> {
            ModelData data = new ModelData();
            data.getRoot().addChild("water_mask", ModelPartBuilder.create()
                    .cuboid(-9, 2, -length * 16f + 2, 18, 6, length * 32f - 4), ModelTransform.NONE);
            return TexturedModelData.of(data, 2048, 1024).createModel();
        });
    }

    private static List<Cube> read(ResourceManager manager, String file) {
        Identifier id = Identifier.of(LongboatLab.ID, "models/entity/" + file + ".bbmodel");
        try (Reader reader = manager.getResourceOrThrow(id).getReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            List<Cube> cubes = new ArrayList<>();
            for (var element : root.getAsJsonArray("elements")) {
                JsonObject cube = element.getAsJsonObject();
                JsonArray from = cube.getAsJsonArray("from"), to = cube.getAsJsonArray("to");
                JsonArray uv = cube.getAsJsonArray("uv_offset");
                cubes.add(new Cube(cube.get("name").getAsString(), value(from, 0), value(from, 1), value(from, 2),
                        value(to, 0) - value(from, 0), value(to, 1) - value(from, 1), value(to, 2) - value(from, 2),
                        uv.get(0).getAsInt(), uv.get(1).getAsInt()));
            }
            if (cubes.isEmpty()) throw new IllegalStateException("Empty Blockbench model: " + id);
            return List.copyOf(cubes);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read Blockbench model " + id, exception);
        }
    }

    private static float value(JsonArray array, int index) { return array.get(index).getAsFloat(); }

    private static ModelPart bake(List<Cube> cubes, int segments, boolean extend) {
        ModelData model = new ModelData();
        float extension = (segments - 2) * 16f;
        for (Cube cube : cubes) {
            if (extend && cube.name().startsWith("seat_")) continue;
            float z = cube.z(), depth = cube.depth();
            if (extend && cube.name().startsWith("stretch_")) {
                z -= extension;
                depth += extension * 2;
            } else if (extend && cube.name().startsWith("bow_")) z += extension;
            else if (extend && cube.name().startsWith("stern_")) z -= extension;
            model.getRoot().addChild(cube.name(), ModelPartBuilder.create().uv(cube.u(), cube.v())
                    .cuboid(cube.x(), cube.y(), z, cube.width(), cube.height(), depth), ModelTransform.NONE);
        }
        if (extend) {
            Cube crossbar = cubes.stream().filter(c -> c.name().equals("seat_center")).findFirst().orElseThrow();
            int dividers = Math.min(512, segments - 1);
            for (int i = 1; i <= dividers; i++) {
                long boundary = (long) i * segments / (dividers + 1);
                float z = -segments * 16f + boundary * 32f - crossbar.depth() / 2;
                model.getRoot().addChild("divider_" + i, ModelPartBuilder.create().uv(crossbar.u(), crossbar.v())
                        .cuboid(crossbar.x(), crossbar.y(), z, crossbar.width(), crossbar.height(), crossbar.depth()),
                        ModelTransform.NONE);
            }
        }
        return TexturedModelData.of(model, 2048, 1024).createModel();
    }
}
