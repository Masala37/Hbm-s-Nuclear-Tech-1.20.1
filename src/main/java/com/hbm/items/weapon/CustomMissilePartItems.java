package com.hbm.items.weapon;

import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartSize;
import com.hbm.items.weapon.ItemCustomMissilePart.Rarity;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import com.hbm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Size-10, size-15, and size-20 custom missile parts plus targeting chips. IDs match 1.7.10 {@code ModItems}.
 */
public final class CustomMissilePartItems {
    private CustomMissilePartItems() {
    }

    public static final RegistryObject<Item> MP_THRUSTER_10_KEROSENE = thruster(
            "mp_thruster_10_kerosene", FuelType.KEROSENE, 1.0F, 1.5F, 10.0F, PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_THRUSTER_10_SOLID = thruster(
            "mp_thruster_10_solid", FuelType.SOLID, 1.0F, 1.5F, 15.0F, PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_THRUSTER_10_XENON = thruster(
            "mp_thruster_10_xenon", FuelType.XENON, 1.0F, 1.5F, 5.0F, PartSize.SIZE_10);

    public static final RegistryObject<Item> MP_STABILITY_10_FLAT = fin("mp_stability_10_flat", 0.5F, 10.0F,
            PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_STABILITY_10_CRUISE = fin("mp_stability_10_cruise", 0.25F, 5.0F,
            PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_STABILITY_10_SPACE = ModItems.ITEMS.register("mp_stability_10_space",
            () -> new ItemCustomMissilePart().makeStability(0.35F, PartSize.SIZE_10).setHealth(5.0F)
                    .setRarity(Rarity.COMMON)
                    .setWittyText("Standing there alone, the ship is waiting / All systems are go, are you sure?"));

    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE = ModItems.ITEMS.register(
            "mp_fuselage_10_kerosene",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 2500.0F, PartSize.SIZE_10, PartSize.SIZE_10)
                    .setAuthor("Hoboy").setHealth(20.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_CAMO = skin("mp_fuselage_10_kerosene_camo",
            () -> MP_FUSELAGE_10_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_DESERT = skin("mp_fuselage_10_kerosene_desert",
            () -> MP_FUSELAGE_10_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Desert Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_SKY = skin("mp_fuselage_10_kerosene_sky",
            () -> MP_FUSELAGE_10_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Sky Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_FLAMES = skin("mp_fuselage_10_kerosene_flames",
            () -> MP_FUSELAGE_10_KEROSENE, p -> p.setRarity(Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_INSULATION = skin(
            "mp_fuselage_10_kerosene_insulation",
            () -> MP_FUSELAGE_10_KEROSENE,
            p -> p.setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(25.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_SLEEK = skin("mp_fuselage_10_kerosene_sleek",
            () -> MP_FUSELAGE_10_KEROSENE, p -> p.setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(35.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_METAL = skin("mp_fuselage_10_kerosene_metal",
            () -> MP_FUSELAGE_10_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setTitle("Bolted Metal").setHealth(30.0F).setAuthor("Hoboy"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_KEROSENE_TAINT = skin("mp_fuselage_10_kerosene_taint",
            () -> MP_FUSELAGE_10_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted"));

    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID = ModItems.ITEMS.register("mp_fuselage_10_solid",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 2500.0F, PartSize.SIZE_10, PartSize.SIZE_10)
                    .setHealth(25.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_FLAMES = skin("mp_fuselage_10_solid_flames",
            () -> MP_FUSELAGE_10_SOLID, p -> p.setRarity(Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_INSULATION = skin("mp_fuselage_10_solid_insulation",
            () -> MP_FUSELAGE_10_SOLID,
            p -> p.setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(30.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_SLEEK = skin("mp_fuselage_10_solid_sleek",
            () -> MP_FUSELAGE_10_SOLID, p -> p.setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(35.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_SOVIET_GLORY = skin(
            "mp_fuselage_10_solid_soviet_glory",
            () -> MP_FUSELAGE_10_SOLID,
            p -> p.setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(35.0F).setTitle("Soviet Glory"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_CATHEDRAL = skin("mp_fuselage_10_solid_cathedral",
            () -> MP_FUSELAGE_10_SOLID,
            p -> p.setRarity(Rarity.RARE).setAuthor("Satan").setTitle("Unholy Cathedral").setWittyText("Quakeesque!"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_MOONLIT = skin("mp_fuselage_10_solid_moonlit",
            () -> MP_FUSELAGE_10_SOLID,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("The Master & Hoboy").setTitle("Moonlit"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_BATTERY = skin("mp_fuselage_10_solid_battery",
            () -> MP_FUSELAGE_10_SOLID,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("wolfmonster222").setHealth(30.0F)
                    .setTitle("Ecstatic").setWittyText("I got caught eating batteries again :("));
    public static final RegistryObject<Item> MP_FUSELAGE_10_SOLID_DURACELL = skin("mp_fuselage_10_solid_duracell",
            () -> MP_FUSELAGE_10_SOLID,
            p -> p.setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Duracell").setHealth(30.0F)
                    .setWittyText("The crunchiest battery on the market!"));

    public static final RegistryObject<Item> MP_FUSELAGE_10_XENON = ModItems.ITEMS.register("mp_fuselage_10_xenon",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.XENON, 5000.0F, PartSize.SIZE_10, PartSize.SIZE_10)
                    .setHealth(20.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_XENON_BHOLE = skin("mp_fuselage_10_xenon_bhole",
            () -> MP_FUSELAGE_10_XENON,
            p -> p.setRarity(Rarity.RARE).setAuthor("Sten89").setTitle("Morceus-1457"));

    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE = ModItems.ITEMS.register(
            "mp_fuselage_10_long_kerosene",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 5000.0F, PartSize.SIZE_10, PartSize.SIZE_10)
                    .setAuthor("Hoboy").setHealth(30.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_CAMO = skin(
            "mp_fuselage_10_long_kerosene_camo",
            () -> MP_FUSELAGE_10_LONG_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_DESERT = skin(
            "mp_fuselage_10_long_kerosene_desert",
            () -> MP_FUSELAGE_10_LONG_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Desert Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_SKY = skin(
            "mp_fuselage_10_long_kerosene_sky",
            () -> MP_FUSELAGE_10_LONG_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Sky Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_FLAMES = skin(
            "mp_fuselage_10_long_kerosene_flames",
            () -> MP_FUSELAGE_10_LONG_KEROSENE, p -> p.setRarity(Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_INSULATION = skin(
            "mp_fuselage_10_long_kerosene_insulation",
            () -> MP_FUSELAGE_10_LONG_KEROSENE,
            p -> p.setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(35.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_SLEEK = skin(
            "mp_fuselage_10_long_kerosene_sleek",
            () -> MP_FUSELAGE_10_LONG_KEROSENE, p -> p.setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(40.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_METAL = skin(
            "mp_fuselage_10_long_kerosene_metal",
            () -> MP_FUSELAGE_10_LONG_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setHealth(35.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_DASH = skin(
            "mp_fuselage_10_long_kerosene_dash",
            () -> MP_FUSELAGE_10_LONG_KEROSENE,
            p -> p.setRarity(Rarity.EPIC).setAuthor("Sam").setTitle("Dash")
                    .setWittyText("I wash my hands of it.").hideFromCreative());
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_TAINT = skin(
            "mp_fuselage_10_long_kerosene_taint",
            () -> MP_FUSELAGE_10_LONG_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_KEROSENE_VAP = skin(
            "mp_fuselage_10_long_kerosene_vap",
            () -> MP_FUSELAGE_10_LONG_KEROSENE,
            p -> p.setRarity(Rarity.EPIC).setAuthor("VT-6/24").setTitle("Minty Contrail").setWittyText("Upper rivet!"));

    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID = ModItems.ITEMS.register(
            "mp_fuselage_10_long_solid",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 5000.0F, PartSize.SIZE_10, PartSize.SIZE_10)
                    .setHealth(35.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID_FLAMES = skin(
            "mp_fuselage_10_long_solid_flames",
            () -> MP_FUSELAGE_10_LONG_SOLID, p -> p.setRarity(Rarity.UNCOMMON).setTitle("Sick Flames"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID_INSULATION = skin(
            "mp_fuselage_10_long_solid_insulation",
            () -> MP_FUSELAGE_10_LONG_SOLID,
            p -> p.setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(40.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID_SLEEK = skin(
            "mp_fuselage_10_long_solid_sleek",
            () -> MP_FUSELAGE_10_LONG_SOLID, p -> p.setRarity(Rarity.RARE).setTitle("IF-R&D").setHealth(45.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID_SOVIET_GLORY = skin(
            "mp_fuselage_10_long_solid_soviet_glory",
            () -> MP_FUSELAGE_10_LONG_SOLID,
            p -> p.setRarity(Rarity.EPIC).setAuthor("Hoboy").setHealth(45.0F).setTitle("Soviet Glory")
                    .setWittyText("Fully Automated Luxury Gay Space Communism!"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID_BULLET = skin(
            "mp_fuselage_10_long_solid_bullet",
            () -> MP_FUSELAGE_10_LONG_SOLID,
            p -> p.setRarity(Rarity.COMMON).setAuthor("Sam").setTitle("Bullet Bill"));
    public static final RegistryObject<Item> MP_FUSELAGE_10_LONG_SOLID_SILVERMOONLIGHT = skin(
            "mp_fuselage_10_long_solid_silvermoonlight",
            () -> MP_FUSELAGE_10_LONG_SOLID,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight"));

    public static final RegistryObject<Item> MP_FUSELAGE_10_15_KEROSENE = ModItems.ITEMS.register(
            "mp_fuselage_10_15_kerosene",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 10000.0F, PartSize.SIZE_10, PartSize.SIZE_15)
                    .setHealth(40.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_15_SOLID = ModItems.ITEMS.register(
            "mp_fuselage_10_15_solid",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 10000.0F, PartSize.SIZE_10, PartSize.SIZE_15)
                    .setHealth(40.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_15_HYDROGEN = ModItems.ITEMS.register(
            "mp_fuselage_10_15_hydrogen",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 10000.0F, PartSize.SIZE_10, PartSize.SIZE_15)
                    .setHealth(40.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_10_15_BALEFIRE = ModItems.ITEMS.register(
            "mp_fuselage_10_15_balefire",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.BALEFIRE, 10000.0F, PartSize.SIZE_10, PartSize.SIZE_15)
                    .setHealth(40.0F));

    public static final RegistryObject<Item> MP_THRUSTER_15_KEROSENE = thruster(
            "mp_thruster_15_kerosene", FuelType.KEROSENE, 1.0F, 7.5F, 15.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_KEROSENE_DUAL = thruster(
            "mp_thruster_15_kerosene_dual", FuelType.KEROSENE, 1.0F, 2.5F, 15.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_KEROSENE_TRIPLE = thruster(
            "mp_thruster_15_kerosene_triple", FuelType.KEROSENE, 1.0F, 5.0F, 15.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_SOLID = thruster(
            "mp_thruster_15_solid", FuelType.SOLID, 1.0F, 5.0F, 20.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_SOLID_HEXDECUPLE = ModItems.ITEMS.register(
            "mp_thruster_15_solid_hexdecuple",
            () -> new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1.0F, 5.0F, PartSize.SIZE_15)
                    .setHealth(25.0F).setRarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item> MP_THRUSTER_15_HYDROGEN = thruster(
            "mp_thruster_15_hydrogen", FuelType.HYDROGEN, 1.0F, 7.5F, 20.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_HYDROGEN_DUAL = thruster(
            "mp_thruster_15_hydrogen_dual", FuelType.HYDROGEN, 1.0F, 2.5F, 15.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_BALEFIRE_SHORT = thruster(
            "mp_thruster_15_balefire_short", FuelType.BALEFIRE, 1.0F, 5.0F, 25.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_BALEFIRE = thruster(
            "mp_thruster_15_balefire", FuelType.BALEFIRE, 1.0F, 5.0F, 25.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_BALEFIRE_LARGE = thruster(
            "mp_thruster_15_balefire_large", FuelType.BALEFIRE, 1.0F, 7.5F, 35.0F, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_THRUSTER_15_BALEFIRE_LARGE_RAD = ModItems.ITEMS.register(
            "mp_thruster_15_balefire_large_rad",
            () -> new ItemCustomMissilePart().makeThruster(FuelType.BALEFIRE, 1.0F, 7.5F, PartSize.SIZE_15)
                    .setAuthor("The Master").setHealth(35.0F).setRarity(Rarity.UNCOMMON));

    public static final RegistryObject<Item> MP_STABILITY_15_FLAT = fin("mp_stability_15_flat", 0.5F, 10.0F,
            PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_STABILITY_15_THIN = fin("mp_stability_15_thin", 0.35F, 5.0F,
            PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_STABILITY_15_SOYUZ = ModItems.ITEMS.register("mp_stability_15_soyuz",
            () -> new ItemCustomMissilePart().makeStability(0.25F, PartSize.SIZE_15).setHealth(15.0F)
                    .setRarity(Rarity.COMMON).setWittyText("Союз!"));

    public static final RegistryObject<Item> MP_THRUSTER_20_KEROSENE = thruster(
            "mp_thruster_20_kerosene", FuelType.KEROSENE, 1.0F, 100.0F, 30.0F, PartSize.SIZE_20);
    public static final RegistryObject<Item> MP_THRUSTER_20_KEROSENE_DUAL = thruster(
            "mp_thruster_20_kerosene_dual", FuelType.KEROSENE, 1.0F, 100.0F, 30.0F, PartSize.SIZE_20);
    public static final RegistryObject<Item> MP_THRUSTER_20_KEROSENE_TRIPLE = thruster(
            "mp_thruster_20_kerosene_triple", FuelType.KEROSENE, 1.0F, 100.0F, 30.0F, PartSize.SIZE_20);
    public static final RegistryObject<Item> MP_THRUSTER_20_SOLID = ModItems.ITEMS.register(
            "mp_thruster_20_solid",
            () -> new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1.0F, 100.0F, PartSize.SIZE_20)
                    .setHealth(35.0F)
                    .setWittyText("It's basically just a big hole at the end of the fuel tank."));
    public static final RegistryObject<Item> MP_THRUSTER_20_SOLID_MULTI = thruster(
            "mp_thruster_20_solid_multi", FuelType.SOLID, 1.0F, 100.0F, 35.0F, PartSize.SIZE_20);
    public static final RegistryObject<Item> MP_THRUSTER_20_SOLID_MULTIER = ModItems.ITEMS.register(
            "mp_thruster_20_solid_multier",
            () -> new ItemCustomMissilePart().makeThruster(FuelType.SOLID, 1.0F, 100.0F, PartSize.SIZE_20)
                    .setHealth(35.0F)
                    .setWittyText("Did I miscount? Hope not."));
    public static final RegistryObject<Item> MP_STABILITY_20_FLAT = fin("mp_s_20", 0.5F, 0.0F, PartSize.SIZE_20);

    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE = ModItems.ITEMS.register(
            "mp_fuselage_15_kerosene",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 15000.0F, PartSize.SIZE_15, PartSize.SIZE_15)
                    .setAuthor("Hoboy").setHealth(50.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_CAMO = skin("mp_fuselage_15_kerosene_camo",
            () -> MP_FUSELAGE_15_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_DESERT = skin("mp_fuselage_15_kerosene_desert",
            () -> MP_FUSELAGE_15_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Desert Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_SKY = skin("mp_fuselage_15_kerosene_sky",
            () -> MP_FUSELAGE_15_KEROSENE, p -> p.setRarity(Rarity.COMMON).setTitle("Sky Camo"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_INSULATION = skin(
            "mp_fuselage_15_kerosene_insulation",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(55.0F)
                    .setWittyText("Rest in spaghetti Columbia :("));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_METAL = skin("mp_fuselage_15_kerosene_metal",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bolted Metal").setHealth(60.0F)
                    .setWittyText("Metal frame with metal plating reinforced with bolted metal sheets and metal."));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_DECORATED = skin(
            "mp_fuselage_15_kerosene_decorated",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Decorated").setHealth(60.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_STEAMPUNK = skin(
            "mp_fuselage_15_kerosene_steampunk",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Steampunk").setHealth(60.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_POLITE = skin("mp_fuselage_15_kerosene_polite",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.LEGENDARY).setAuthor("Hoboy").setTitle("Polite").setHealth(60.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_BLACKJACK = skin(
            "mp_fuselage_15_kerosene_blackjack",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.LEGENDARY).setTitle("Queen Whiskey").setHealth(100.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_LAMBDA = skin("mp_fuselage_15_kerosene_lambda",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("Lambda Complex").setHealth(75.0F)
                    .setWittyText("MAGNIFICENT MICROWAVE CASSEROLE"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_MINUTEMAN = skin(
            "mp_fuselage_15_kerosene_minuteman",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Spexta").setTitle("MX 1702"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_PIP = skin("mp_fuselage_15_kerosene_pip",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.EPIC).setAuthor("The Doctor").setTitle("LittlePip")
                    .setWittyText("31!").hideFromCreative());
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_TAINT = skin("mp_fuselage_15_kerosene_taint",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Sam").setTitle("Tainted").setWittyText("DUN-DUN!"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_KEROSENE_YUCK = skin("mp_fuselage_15_kerosene_yuck",
            () -> MP_FUSELAGE_15_KEROSENE,
            p -> p.setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Flesh")
                    .setWittyText("Note: Never clean DNA vials with your own spit.").setHealth(60.0F));

    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID = ModItems.ITEMS.register("mp_fuselage_15_solid",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 15000.0F, PartSize.SIZE_15, PartSize.SIZE_15)
                    .setHealth(60.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_INSULATION = skin("mp_fuselage_15_solid_insulation",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.COMMON).setTitle("Orange Insulation").setHealth(65.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_DESH = skin("mp_fuselage_15_solid_desh",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Desh Plating").setHealth(80.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_SOVIET_GLORY = skin(
            "mp_fuselage_15_solid_soviet_glory",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Soviet Glory").setHealth(70.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_SOVIET_STANK = skin(
            "mp_fuselage_15_solid_soviet_stank",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.EPIC).setAuthor("Hoboy").setTitle("Soviet Stank").setHealth(15.0F)
                    .setWittyText("Aged like a fine wine! Well, almost."));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_FAUST = skin("mp_fuselage_15_solid_faust",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.LEGENDARY).setAuthor("Dr.Nostalgia").setTitle("Mighty Lauren").setHealth(250.0F)
                    .setWittyText("Welcome to Subway, may I take your order?"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_SILVERMOONLIGHT = skin(
            "mp_fuselage_15_solid_silvermoonlight",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("The Master").setTitle("Silver Moonlight"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_SNOWY = skin("mp_fuselage_15_solid_snowy",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Dr.Nostalgia").setTitle("Chilly Day"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_PANORAMA = skin("mp_fuselage_15_solid_panorama",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.RARE).setAuthor("Hoboy").setTitle("Panorama"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_ROSES = skin("mp_fuselage_15_solid_roses",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Hoboy").setTitle("Bed of roses"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_SOLID_MIMI = skin("mp_fuselage_15_solid_mimi",
            () -> MP_FUSELAGE_15_SOLID,
            p -> p.setRarity(Rarity.RARE).setTitle("Mimi-chan"));

    public static final RegistryObject<Item> MP_FUSELAGE_15_HYDROGEN = ModItems.ITEMS.register(
            "mp_fuselage_15_hydrogen",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.HYDROGEN, 15000.0F, PartSize.SIZE_15, PartSize.SIZE_15)
                    .setHealth(50.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_HYDROGEN_CATHEDRAL = skin(
            "mp_fuselage_15_hydrogen_cathedral",
            () -> MP_FUSELAGE_15_HYDROGEN,
            p -> p.setRarity(Rarity.UNCOMMON).setAuthor("Satan").setTitle("Unholy Cathedral"));

    public static final RegistryObject<Item> MP_FUSELAGE_15_BALEFIRE = ModItems.ITEMS.register(
            "mp_fuselage_15_balefire",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.BALEFIRE, 15000.0F, PartSize.SIZE_15, PartSize.SIZE_15)
                    .setHealth(75.0F));

    public static final RegistryObject<Item> MP_FUSELAGE_15_20_KEROSENE = ModItems.ITEMS.register(
            "mp_fuselage_15_20_kerosene",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.KEROSENE, 20000.0F, PartSize.SIZE_15, PartSize.SIZE_20)
                    .setAuthor("Hoboy").setHealth(70.0F));
    public static final RegistryObject<Item> MP_FUSELAGE_15_20_KEROSENE_MAGNUSSON = skin(
            "mp_fuselage_15_20_kerosene_magnusson",
            () -> MP_FUSELAGE_15_20_KEROSENE,
            p -> p.setRarity(Rarity.RARE).setAuthor("VT-6/24").setTitle("White Forest Rocket")
                    .setWittyText("And get your cranio-conjugal parasite away from my nose cone!"));
    public static final RegistryObject<Item> MP_FUSELAGE_15_20_SOLID = ModItems.ITEMS.register(
            "mp_fuselage_15_20_solid",
            () -> new ItemCustomMissilePart().makeFuselage(FuelType.SOLID, 20000.0F, PartSize.SIZE_15, PartSize.SIZE_20)
                    .setHealth(70.0F));

    public static final RegistryObject<Item> MP_WARHEAD_10_HE = warhead(
            "mp_warhead_10_he", WarheadType.HE, 15.0F, 1.5F, 5.0F, null, PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_WARHEAD_10_INCENDIARY = warhead(
            "mp_warhead_10_incendiary", WarheadType.INC, 15.0F, 1.5F, 5.0F, null, PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_WARHEAD_10_BUSTER = warhead(
            "mp_warhead_10_buster", WarheadType.BUSTER, 5.0F, 1.5F, 5.0F, null, PartSize.SIZE_10);
    public static final RegistryObject<Item> MP_WARHEAD_10_NUCLEAR = ModItems.ITEMS.register("mp_warhead_10_nuclear",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, 35.0F, 1.5F, PartSize.SIZE_10)
                    .setTitle("Tater Tot").setHealth(10.0F));
    public static final RegistryObject<Item> MP_WARHEAD_10_NUCLEAR_LARGE = ModItems.ITEMS.register(
            "mp_warhead_10_nuclear_large",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, 75.0F, 2.5F, PartSize.SIZE_10)
                    .setTitle("Chernobyl Boris").setHealth(15.0F));
    public static final RegistryObject<Item> MP_WARHEAD_10_TAINT = ModItems.ITEMS.register("mp_warhead_10_taint",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.TAINT, 15.0F, 1.5F, PartSize.SIZE_10)
                    .setHealth(20.0F).setRarity(Rarity.UNCOMMON)
                    .setWittyText("Eat my taint! Bureaucracy is dead and we killed it!"));
    public static final RegistryObject<Item> MP_WARHEAD_10_CLOUD = ModItems.ITEMS.register("mp_warhead_10_cloud",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.CLOUD, 15.0F, 1.5F, PartSize.SIZE_10)
                    .setHealth(20.0F).setRarity(Rarity.RARE));

    public static final RegistryObject<Item> MP_WARHEAD_15_HE = warhead(
            "mp_warhead_15_he", WarheadType.HE, 50.0F, 2.5F, 10.0F, null, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_WARHEAD_15_INCENDIARY = warhead(
            "mp_warhead_15_incendiary", WarheadType.INC, 35.0F, 2.5F, 10.0F, null, PartSize.SIZE_15);
    public static final RegistryObject<Item> MP_WARHEAD_15_NUCLEAR = ModItems.ITEMS.register("mp_warhead_15_nuclear",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.NUCLEAR, 125.0F, 5.0F, PartSize.SIZE_15)
                    .setTitle("Auntie Bertha").setHealth(15.0F));
    public static final RegistryObject<Item> MP_WARHEAD_15_NUCLEAR_SHARK = skin("mp_warhead_15_nuclear_shark",
            () -> MP_WARHEAD_15_NUCLEAR,
            p -> p.setRarity(Rarity.UNCOMMON).setTitle("Discount Bullet Bill")
                    .setWittyText("Nose art on a cannon bullet? Who does that?"));
    public static final RegistryObject<Item> MP_WARHEAD_15_NUCLEAR_MIMI = skin("mp_warhead_15_nuclear_mimi",
            () -> MP_WARHEAD_15_NUCLEAR,
            p -> p.setRarity(Rarity.RARE).setTitle("FASHIONABLE MISSILE"));
    public static final RegistryObject<Item> MP_WARHEAD_15_BOXCAR = ModItems.ITEMS.register("mp_warhead_15_boxcar",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.TX, 250.0F, 7.5F, PartSize.SIZE_15)
                    .setWittyText("?!?!").setHealth(35.0F).setRarity(Rarity.LEGENDARY));
    public static final RegistryObject<Item> MP_WARHEAD_15_N2 = ModItems.ITEMS.register("mp_warhead_15_n2",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.N2, 100.0F, 5.0F, PartSize.SIZE_15)
                    .setWittyText("[screams geometrically]").setHealth(20.0F).setRarity(Rarity.RARE));
    public static final RegistryObject<Item> MP_WARHEAD_15_BALEFIRE = ModItems.ITEMS.register("mp_warhead_15_balefire",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.BALEFIRE, 100.0F, 7.5F, PartSize.SIZE_15)
                    .setRarity(Rarity.LEGENDARY).setAuthor("VT-6/24").setHealth(15.0F)
                    .setWittyText("Hightower, never forgetti."));
    public static final RegistryObject<Item> MP_WARHEAD_15_TURBINE = ModItems.ITEMS.register("mp_warhead_15_turbine",
            () -> new ItemCustomMissilePart().makeWarhead(WarheadType.TURBINE, 200.0F, 5.0F, PartSize.SIZE_15)
                    .setRarity(Rarity.SEWS_CLOTHES_AND_SUCKS_HORSE_COCK).setHealth(250.0F));

    public static final RegistryObject<Item> MP_C_1 = chip("mp_c_1", 0.1F);
    public static final RegistryObject<Item> MP_C_2 = chip("mp_c_2", 0.05F);
    public static final RegistryObject<Item> MP_C_3 = chip("mp_c_3", 0.01F);
    public static final RegistryObject<Item> MP_C_4 = chip("mp_c_4", 0.005F);
    public static final RegistryObject<Item> MP_C_5 = chip("mp_c_5", 0.0F);

    private static RegistryObject<Item> thruster(String id, FuelType fuel, float consumption, float lift, float hp,
                                                     PartSize size) {
        return ModItems.ITEMS.register(id, () -> new ItemCustomMissilePart()
                .makeThruster(fuel, consumption, lift, size).setHealth(hp));
    }

    private static RegistryObject<Item> fin(String id, float inaccuracy, float hp, PartSize size) {
        return ModItems.ITEMS.register(id, () -> new ItemCustomMissilePart()
                .makeStability(inaccuracy, size).setHealth(hp));
    }

    private static RegistryObject<Item> warhead(String id, WarheadType type, float punch, float weight, float hp,
                                                  String title, PartSize size) {
        return ModItems.ITEMS.register(id, () -> {
            ItemCustomMissilePart part = new ItemCustomMissilePart()
                    .makeWarhead(type, punch, weight, size).setHealth(hp);
            if (title != null) {
                part.setTitle(title);
            }
            return part;
        });
    }

    private static RegistryObject<Item> chip(String id, float inaccuracy) {
        return ModItems.ITEMS.register(id, () -> new ItemCustomMissilePart().makeChip(inaccuracy));
    }

    @FunctionalInterface
    private interface SkinOp {
        ItemCustomMissilePart apply(ItemCustomMissilePart part);
    }

    private static RegistryObject<Item> skin(String id, Supplier<RegistryObject<Item>> parent, SkinOp op) {
        return ModItems.ITEMS.register(id, () -> op.apply(((ItemCustomMissilePart) parent.get().get()).copy()));
    }
}
