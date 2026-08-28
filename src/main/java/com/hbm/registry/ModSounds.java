package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RefStrings.MODID);

    public static final RegistryObject<SoundEvent> FSTBMB_START = register("weapon.fstbmb_start");
    public static final RegistryObject<SoundEvent> FSTBMB_PING = register("weapon.fstbmb_ping");
    public static final RegistryObject<SoundEvent> TECH_BOOP = register("item.tech_boop");
    public static final RegistryObject<SoundEvent> TECH_BLEEP = register("tool.tech_bleep");
    public static final RegistryObject<SoundEvent> SPARK_SHOOT = register("weapon.spark_shoot");
    public static final RegistryObject<SoundEvent> ROCKET_FLAME = register("weapon.rocket_flame");
    public static final RegistryObject<SoundEvent> MISSILE_TAKEOFF = register("weapon.missile_takeoff");
    public static final RegistryObject<SoundEvent> EXPLOSION_LARGE_NEAR = register("weapon.explosion_large_near");
    public static final RegistryObject<SoundEvent> EXPLOSION_LARGE_FAR = register("weapon.explosion_large_far");
    public static final RegistryObject<SoundEvent> EXPLOSION_SMALL_NEAR = register("weapon.explosion_small_near");
    public static final RegistryObject<SoundEvent> EXPLOSION_SMALL_FAR = register("weapon.explosion_small_far");
    public static final RegistryObject<SoundEvent> MUKE_EXPLOSION = register("weapon.muke_explosion");
    public static final RegistryObject<SoundEvent> NUCLEAR_EXPLOSION = register("weapon.nuclear_explosion");
    public static final RegistryObject<SoundEvent> BOMB_WHISTLE = register("entity.bomb_whistle");
    public static final RegistryObject<SoundEvent> BOMBER_LOOP = register("entity.bomber_loop");
    public static final RegistryObject<SoundEvent> BOMBER_SMALL_LOOP = register("entity.bomber_small_loop");
    public static final RegistryObject<SoundEvent> DEBRIS = register("block.debris");
    public static final RegistryObject<SoundEvent> BLACK_HOLE = register("entity.black_hole");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String path) {
        // Variable-range (vanilla default): volume drives audible distance.
        // Fixed-range events were unreliable for launch SFX in-game.
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(RefStrings.MODID, path)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
