package com.hbm.registry;

import com.hbm.effect.TaintMobEffect;
import com.hbm.lib.RefStrings;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RefStrings.MODID);

    public static final RegistryObject<MobEffect> TAINT = EFFECTS.register("taint", TaintMobEffect::new);

    private ModMobEffects() {
    }

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
