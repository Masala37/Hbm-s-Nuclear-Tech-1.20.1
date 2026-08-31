package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RefStrings.MODID);

    private static final Map<String, RegistryObject<SoundEvent>> BY_PATH = new LinkedHashMap<>();

    static {
        for (String path : SoundEventCatalog.PATHS) {
            BY_PATH.put(path, SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(RefStrings.MODID, path))));
        }
    }

    public static final RegistryObject<SoundEvent> FSTBMB_START = get("weapon.fstbmb_start");
    public static final RegistryObject<SoundEvent> FSTBMB_PING = get("weapon.fstbmb_ping");
    public static final RegistryObject<SoundEvent> TECH_BOOP = get("item.tech_boop");
    public static final RegistryObject<SoundEvent> TECH_BLEEP = get("tool.tech_bleep");
    public static final RegistryObject<SoundEvent> SPARK_SHOOT = get("weapon.spark_shoot");
    public static final RegistryObject<SoundEvent> ROCKET_FLAME = get("weapon.rocket_flame");
    public static final RegistryObject<SoundEvent> MISSILE_TAKEOFF = get("weapon.missile_takeoff");
    public static final RegistryObject<SoundEvent> EXPLOSION_LARGE_NEAR = get("weapon.explosion_large_near");
    public static final RegistryObject<SoundEvent> EXPLOSION_LARGE_FAR = get("weapon.explosion_large_far");
    public static final RegistryObject<SoundEvent> EXPLOSION_SMALL_NEAR = get("weapon.explosion_small_near");
    public static final RegistryObject<SoundEvent> EXPLOSION_SMALL_FAR = get("weapon.explosion_small_far");
    public static final RegistryObject<SoundEvent> MUKE_EXPLOSION = get("weapon.muke_explosion");
    public static final RegistryObject<SoundEvent> ROBIN_EXPLOSION = get("weapon.robin_explosion");
    public static final RegistryObject<SoundEvent> NUCLEAR_EXPLOSION = get("weapon.nuclear_explosion");
    public static final RegistryObject<SoundEvent> BOMB_WHISTLE = get("entity.bomb_whistle");
    public static final RegistryObject<SoundEvent> BOMBER_LOOP = get("entity.bomber_loop");
    public static final RegistryObject<SoundEvent> BOMBER_SMALL_LOOP = get("entity.bomber_small_loop");
    public static final RegistryObject<SoundEvent> DEBRIS = get("block.debris");
    public static final RegistryObject<SoundEvent> BLACK_HOLE = get("entity.black_hole");
    public static final RegistryObject<SoundEvent> MISSILE_ASSEMBLY = get("block.missile_assembly2");
    public static final RegistryObject<SoundEvent> SONAR_PING = get("block.sonar_ping");
    public static final RegistryObject<SoundEvent> DOOR_WGH_START = get("door.wgh_start");
    public static final RegistryObject<SoundEvent> DOOR_WGH_STOP = get("door.wgh_stop");
    public static final RegistryObject<SoundEvent> DOOR_GARAGE_MOVE = get("door.garage_move");
    public static final RegistryObject<SoundEvent> DOOR_GARAGE_STOP = get("door.garage_stop");
    public static final RegistryObject<SoundEvent> ENGINE = get("block.engine");
    public static final RegistryObject<SoundEvent> DIESEL_OPERATE = get("block.diesel_operate");
    public static final RegistryObject<SoundEvent> CRATE_OPEN = get("block.crate_open");
    public static final RegistryObject<SoundEvent> CRATE_CLOSE = get("block.crate_close");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> get(String path) {
        RegistryObject<SoundEvent> event = BY_PATH.get(path);
        if (event == null) {
            throw new IllegalStateException("Missing sound event: " + path);
        }
        return event;
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
