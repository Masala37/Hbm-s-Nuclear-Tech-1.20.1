package com.hbm.registry;

import com.hbm.lib.RefStrings;
import com.hbm.world.feature.BroadcasterFeature;
import com.hbm.world.feature.DepthDepositFeature;
import com.hbm.world.feature.DudFeature;
import com.hbm.world.feature.GasPocketFeature;
import com.hbm.world.feature.NetherSmolderingFeature;
import com.hbm.world.feature.OilBubbleFeature;
import com.hbm.world.feature.OreLayer3DFeature;
import com.hbm.world.feature.SchistStratumFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, RefStrings.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DUD =
            FEATURES.register("dud", () -> new DudFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BROADCASTER =
            FEATURES.register("broadcaster", () -> new BroadcasterFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OIL_BUBBLE =
            FEATURES.register("oil_bubble",
                    () -> new OilBubbleFeature(NoneFeatureConfiguration.CODEC, OilBubbleFeature.Kind.STONE));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> OIL_SAND_BUBBLE =
            FEATURES.register("oil_sand_bubble",
                    () -> new OilBubbleFeature(NoneFeatureConfiguration.CODEC, OilBubbleFeature.Kind.SAND));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ORE_LAYER_HEMATITE =
            FEATURES.register("ore_layer_hematite",
                    () -> new OreLayer3DFeature(NoneFeatureConfiguration.CODEC, OreLayer3DFeature.Kind.HEMATITE));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ORE_LAYER_BAUXITE =
            FEATURES.register("ore_layer_bauxite",
                    () -> new OreLayer3DFeature(NoneFeatureConfiguration.CODEC, OreLayer3DFeature.Kind.BAUXITE));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ORE_LAYER_MALACHITE =
            FEATURES.register("ore_layer_malachite",
                    () -> new OreLayer3DFeature(NoneFeatureConfiguration.CODEC, OreLayer3DFeature.Kind.MALACHITE));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SCHIST_STRATUM =
            FEATURES.register("schist_stratum", () -> new SchistStratumFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GAS_FLAMMABLE =
            FEATURES.register("gas_flammable",
                    () -> new GasPocketFeature(NoneFeatureConfiguration.CODEC, GasPocketFeature.Kind.FLAMMABLE));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GAS_EXPLOSIVE =
            FEATURES.register("gas_explosive",
                    () -> new GasPocketFeature(NoneFeatureConfiguration.CODEC, GasPocketFeature.Kind.EXPLOSIVE));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_IRON =
            FEATURES.register("depth_iron",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.IRON));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_TITANIUM =
            FEATURES.register("depth_titanium",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.TITANIUM));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_TUNGSTEN =
            FEATURES.register("depth_tungsten",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.TUNGSTEN));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_CINNEBAR =
            FEATURES.register("depth_cinnebar",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.CINNEBAR));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_ZIRCONIUM =
            FEATURES.register("depth_zirconium",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.ZIRCONIUM));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_BORAX =
            FEATURES.register("depth_borax",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.BORAX));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_NETHER_NEODYMIUM_FLOOR =
            FEATURES.register("depth_nether_neodymium_floor",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.NETHER_FLOOR));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEPTH_NETHER_NEODYMIUM_CEILING =
            FEATURES.register("depth_nether_neodymium_ceiling",
                    () -> new DepthDepositFeature(NoneFeatureConfiguration.CODEC, DepthDepositFeature.Kind.NETHER_CEILING));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NETHER_SMOLDERING =
            FEATURES.register("nether_smoldering",
                    () -> new NetherSmolderingFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
