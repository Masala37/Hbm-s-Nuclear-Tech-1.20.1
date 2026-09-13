package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.hazard.type.HazardTypeDigamma;
import com.hbm.hazard.type.HazardTypeRadiation;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlocks;
import com.hbm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Hazard constants + registration subset (legacy {@code HazardRegistry}).
 * <p>
 * {@code PARTICLE_DIGAMMA} is skipped — {@link com.hbm.items.special.DigammaParticleItem}
 * already applies digamma in {@code inventoryTick}.
 */
public final class HazardRegistry {

    public static final float u = 0.35F;
    public static final float u238 = 0.25F;
    public static final float pu238 = 10.0F;
    public static final float ra226 = 7.5F;
    public static final float sr90 = 15.0F;
    public static final float co60 = 30.0F;
    public static final float ac227 = 30.0F;
    public static final float po210 = 75.0F;
    public static final float pb209 = 10000.0F;
    public static final float au198 = 500.0F;
    public static final float am241 = 8.5F;
    public static final float np237 = 2.5F;
    public static final float sa326 = 15.0F;
    public static final float wst = 15.0F;
    public static final float wstv = 7.5F;
    public static final float nugget = 0.1F;
    public static final float ingot = 1.0F;
    public static final float billet = 0.5F;
    public static final float rtg = billet * 3;
    public static final float block = 10.0F;

    public static final HazardTypeBase RADIATION = new HazardTypeRadiation();
    public static final HazardTypeBase DIGAMMA = new HazardTypeDigamma();
    public static final HazardTypeBase HOT = new com.hbm.hazard.type.HazardTypeHot();
    public static final HazardTypeBase BLINDING = new com.hbm.hazard.type.HazardTypeBlinding();

    private HazardRegistry() {
    }

    public static void registerItems() {
        HazardSystem.itemMap.clear();

        HazardSystem.register(ModItems.URANIUM_INGOT.get(), makeData(RADIATION, u * ingot));
        HazardSystem.register(ModBlocks.BLOCK_URANIUM.get(), makeData(RADIATION, u * block));

        HazardSystem.register(ModItems.SCHRABIDIUM_INGOT.get(), makeData(RADIATION, sa326 * ingot));
        HazardSystem.register(ModBlocks.BLOCK_SCHRABIDIUM.get(), makeData(RADIATION, sa326 * block));

        registerBulk("nuclear_waste", RADIATION, wst * ingot);
        registerBulk("nuclear_waste_tiny", RADIATION, wst * nugget);
        registerBulk("nuclear_waste_vitrified", RADIATION, wstv * ingot);
        registerBulk("nuclear_waste_vitrified_tiny", RADIATION, wstv * nugget);
        registerBulk("nuclear_waste_long", RADIATION, 5.0F);
        registerBulk("nuclear_waste_long_tiny", RADIATION, 0.5F);
        registerBulk("nuclear_waste_short", RADIATION, 30.0F);
        registerBulk("nuclear_waste_short_tiny", RADIATION, 3.0F);
        registerBulk("nuclear_waste_long_depleted", RADIATION, 0.5F);
        registerBulk("nuclear_waste_long_depleted_tiny", RADIATION, 0.05F);
        registerBulk("nuclear_waste_short_depleted", RADIATION, 3.0F);
        registerBulk("nuclear_waste_short_depleted_tiny", RADIATION, 0.3F);

        HazardSystem.register(ModItems.BILLET_NUCLEAR_WASTE.get(), makeData(RADIATION, wst * billet));

        HazardSystem.register(ModBlocks.BARREL_YELLOW.get(), makeData(RADIATION, wst * ingot * 10.0F));
        HazardSystem.register(ModBlocks.BLOCK_WASTE.get(), makeData(RADIATION, wst * block));
        HazardSystem.register(ModBlocks.BLOCK_WASTE_PAINTED.get(), makeData(RADIATION, wst * block));
        HazardSystem.register(ModBlocks.BLOCK_WASTE_VITRIFIED.get(), makeData(RADIATION, wstv * block));
        HazardSystem.register(ModBlocks.WASTE_EARTH.get(), makeData(RADIATION, 0.5F));

        HazardSystem.register(ModBlocks.SELLAFIELD_0.get(), makeData(RADIATION, 0.5F));
        HazardSystem.register(ModBlocks.SELLAFIELD_1.get(), makeData(RADIATION, 1.0F));
        HazardSystem.register(ModBlocks.SELLAFIELD_2.get(), makeData(RADIATION, 2.5F));
        HazardSystem.register(ModBlocks.SELLAFIELD_3.get(), makeData(RADIATION, 4.0F));
        HazardSystem.register(ModBlocks.SELLAFIELD_4.get(), makeData(RADIATION, 5.0F));
        HazardSystem.register(ModBlocks.SELLAFIELD_5.get(), makeData(RADIATION, 10.0F));

        registerRtgPellet(ModItems.PELLET_RTG.get(), pu238 * rtg, 3.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_RADIUM.get(), ra226 * rtg, 0.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_WEAK.get(), (pu238 + (u238 * 2)) * billet, 0.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_STRONTIUM.get(), sr90 * rtg, 0.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_COBALT.get(), co60 * rtg, 0.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_ACTINIUM.get(), ac227 * rtg, 0.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_POLONIUM.get(), po210 * rtg, 3.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_LEAD.get(), pb209 * rtg, 7.0F, 50.0F);
        registerRtgPellet(ModItems.PELLET_RTG_GOLD.get(), au198 * rtg, 5.0F, 0.0F);
        registerRtgPellet(ModItems.PELLET_RTG_AMERICIUM.get(), am241 * rtg, 0.0F, 0.0F);
    }

    /**
     * Depleted neptunium pellets keep 1.7 radiation; other depleted types have none.
     */
    public static List<HazardEntry> extraFromStack(net.minecraft.world.item.ItemStack stack) {
        if (!(stack.getItem() instanceof com.hbm.items.machine.ItemRTGPelletDepleted)) {
            return null;
        }
        if (com.hbm.items.machine.ItemRTGPelletDepleted.getType(stack)
                != com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial.NEPTUNIUM) {
            return java.util.Collections.emptyList();
        }
        java.util.List<HazardEntry> list = new java.util.ArrayList<>();
        list.add(new HazardEntry(RADIATION, np237 * rtg));
        return list;
    }

    private static void registerRtgPellet(Item pellet, float base, float hot, float blinding) {
        HazardData data = new HazardData();
        data.addEntry(new HazardEntry(RADIATION, base)
                .addMod(new com.hbm.hazard.modifier.HazardModifierRTGRadiation(0.0F)));
        if (hot > 0.0F) {
            data.addEntry(HOT, hot);
        }
        if (blinding > 0.0F) {
            data.addEntry(BLINDING, blinding);
        }
        HazardSystem.register(pellet, data);
    }

    private static void registerBulk(String path, HazardTypeBase type, float level) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(RefStrings.MODID, path));
        if (item != null) {
            HazardSystem.register(item, makeData(type, level));
        }
    }

    private static HazardData makeData(HazardTypeBase hazard, float level) {
        return new HazardData().addEntry(hazard, level);
    }
}
