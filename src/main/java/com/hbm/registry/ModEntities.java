package com.hbm.registry;

import com.hbm.entity.bomb.PrimedBombEntity;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.entity.effect.EntityMist;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.item.EntityFireworks;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityBomber;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.missile.EntityMissileBuster;
import com.hbm.entity.missile.EntityMissileCluster;
import com.hbm.entity.missile.EntityMissileGeneric;
import com.hbm.entity.missile.EntityMissileIncendiary;
import com.hbm.entity.missile.EntityMissileStrong;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.entity.projectile.EntityClusterBomblet;
import com.hbm.entity.projectile.EntityFallingNuke;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.lib.RefStrings;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RefStrings.MODID);

    public static final RegistryObject<EntityType<PrimedBombEntity>> PRIMED_BOMB = ENTITIES.register("primed_bomb",
            () -> EntityType.Builder.<PrimedBombEntity>of(PrimedBombEntity::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_bomb"));

    public static final RegistryObject<EntityType<EntityNukeExplosionMK5>> NUKE_EXPLOSION_MK5 = ENTITIES.register(
            "nuke_explosion_mk5",
            () -> EntityType.Builder.<EntityNukeExplosionMK5>of(EntityNukeExplosionMK5::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(200)
                    .updateInterval(1)
                    .build("nuke_explosion_mk5"));

    public static final RegistryObject<EntityType<EntityNukeExplosionMK3>> NUKE_EXPLOSION_MK3 = ENTITIES.register(
            "nuke_explosion_mk3",
            () -> EntityType.Builder.<EntityNukeExplosionMK3>of(EntityNukeExplosionMK3::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(200)
                    .updateInterval(1)
                    .build("nuke_explosion_mk3"));

    public static final RegistryObject<EntityType<EntityBalefire>> BALEFIRE_BLAST = ENTITIES.register(
            "balefire_blast",
            () -> EntityType.Builder.<EntityBalefire>of(EntityBalefire::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(200)
                    .updateInterval(1)
                    .build("balefire_blast"));

    public static final RegistryObject<EntityType<EntityCloudFleija>> CLOUD_FLEIJA = ENTITIES.register(
            "cloud_fleija",
            () -> EntityType.Builder.<EntityCloudFleija>of(EntityCloudFleija::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .build("cloud_fleija"));

    public static final RegistryObject<EntityType<EntityCloudFleijaRainbow>> CLOUD_FLEIJA_RAINBOW = ENTITIES.register(
            "cloud_fleija_rainbow",
            () -> EntityType.Builder.<EntityCloudFleijaRainbow>of(EntityCloudFleijaRainbow::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .build("cloud_fleija_rainbow"));

    public static final RegistryObject<EntityType<EntityCloudSolinium>> CLOUD_SOLINIUM = ENTITIES.register(
            "cloud_solinium",
            () -> EntityType.Builder.<EntityCloudSolinium>of(EntityCloudSolinium::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(20.0F, 40.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .build("cloud_solinium"));

    public static final RegistryObject<EntityType<EntityNukeTorex>> NUKE_TOREX = ENTITIES.register(
            "nuke_torex",
            () -> EntityType.Builder.<EntityNukeTorex>of(EntityNukeTorex::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 50.0F)
                    .clientTrackingRange(250)
                    .updateInterval(1)
                    .build("nuke_torex"));

    public static final RegistryObject<EntityType<EntityFalloutRain>> FALLOUT_RAIN = ENTITIES.register(
            "fallout_rain",
            () -> EntityType.Builder.<EntityFalloutRain>of(EntityFalloutRain::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(4.0F, 20.0F)
                    .clientTrackingRange(200)
                    .updateInterval(5)
                    .build("fallout_rain"));

    public static final RegistryObject<EntityType<EntityEMPBlast>> EMP_BLAST = ENTITIES.register(
            "emp_blast",
            () -> EntityType.Builder.<EntityEMPBlast>of(EntityEMPBlast::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.5F, 1.5F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("emp_blast"));

    public static final RegistryObject<EntityType<EntityFireworks>> FIREWORKS = ENTITIES.register(
            "fireworks",
            () -> EntityType.Builder.<EntityFireworks>of(EntityFireworks::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("fireworks"));

    public static final RegistryObject<EntityType<EntityShrapnel>> SHRAPNEL = ENTITIES.register(
            "shrapnel",
            () -> EntityType.Builder.<EntityShrapnel>of(EntityShrapnel::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("shrapnel"));

    public static final RegistryObject<EntityType<EntityRubble>> RUBBLE = ENTITIES.register(
            "rubble",
            () -> EntityType.Builder.<EntityRubble>of(EntityRubble::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("rubble"));

    public static final RegistryObject<EntityType<EntityClusterBomblet>> CLUSTER_BOMBLET = ENTITIES.register(
            "cluster_bomblet",
            () -> EntityType.Builder.<EntityClusterBomblet>of(EntityClusterBomblet::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("cluster_bomblet"));

    public static final RegistryObject<EntityType<EntityBombletZeta>> BOMBLET_ZETA = ENTITIES.register(
            "bomblet_zeta",
            () -> EntityType.Builder.<EntityBombletZeta>of(EntityBombletZeta::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.35F, 0.5F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("bomblet_zeta"));

    public static final RegistryObject<EntityType<EntityMissileGeneric>> MISSILE_GENERIC = ENTITIES.register(
            "missile_generic",
            () -> EntityType.Builder.<EntityMissileGeneric>of(EntityMissileGeneric::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 4.0F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("missile_generic"));

    public static final RegistryObject<EntityType<EntityMissileStrong>> MISSILE_STRONG = ENTITIES.register(
            "missile_strong",
            () -> EntityType.Builder.<EntityMissileStrong>of(EntityMissileStrong::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 4.5F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("missile_strong"));

    public static final RegistryObject<EntityType<EntityMissileIncendiary>> MISSILE_INCENDIARY = ENTITIES.register(
            "missile_incendiary",
            () -> EntityType.Builder.<EntityMissileIncendiary>of(EntityMissileIncendiary::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 4.0F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("missile_incendiary"));

    public static final RegistryObject<EntityType<EntityMissileCluster>> MISSILE_CLUSTER = ENTITIES.register(
            "missile_cluster",
            () -> EntityType.Builder.<EntityMissileCluster>of(EntityMissileCluster::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 4.0F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("missile_cluster"));

    public static final RegistryObject<EntityType<EntityMissileBuster>> MISSILE_BUSTER = ENTITIES.register(
            "missile_buster",
            () -> EntityType.Builder.<EntityMissileBuster>of(EntityMissileBuster::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(1.0F, 4.0F)
                    .clientTrackingRange(256)
                    .updateInterval(1)
                    .build("missile_buster"));

    public static final RegistryObject<EntityType<EntityMist>> MIST = ENTITIES.register(
            "mist",
            () -> EntityType.Builder.<EntityMist>of(EntityMist::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(5.0F, 3.0F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("mist"));

    public static final RegistryObject<EntityType<EntityBomber>> BOMBER = ENTITIES.register(
            "entity_bomber",
            () -> EntityType.Builder.<EntityBomber>of(EntityBomber::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(8.0F, 4.0F)
                    .clientTrackingRange(200)
                    .updateInterval(1)
                    .build("entity_bomber"));

    public static final RegistryObject<EntityType<EntityFallingNuke>> FALLING_NUKE = ENTITIES.register(
            "falling_nuke",
            () -> EntityType.Builder.<EntityFallingNuke>of(EntityFallingNuke::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("falling_nuke"));

    private ModEntities() {
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}
