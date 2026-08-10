package com.hbm.registry;

import com.hbm.lib.RefStrings;
import com.hbm.world.feature.DudFeature;
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

    private ModFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
