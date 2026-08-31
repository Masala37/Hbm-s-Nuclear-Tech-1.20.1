package com.hbm.items.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Missile item with legacy-style 3D inventory / hand rendering (BEWLR).
 * Fuel caps follow legacy {@code ItemMissile} form-factor defaults
 * (MICRO/TIER0 = solid pre-fueled → 0 mB; V2 = 4000 ethanol; Strong/Stealth/Robin = 8000
 * kerosene; Huge = 12000 kerosene/loxy; Atlas = 16000 jetfuel/loxy).
 */
public class MissileItem extends Item {
    public enum GuiTier {
        /** Micro / Tier0 — legacy guiScale 3.75, solid pre-fueled (fuelCap 0). */
        TIER0(3.75F, 10.75F, 1.0F, 0),
        /** V2 / Tier1 — legacy guiScale 2.5, ethanol/peroxide 4000 mB. */
        TIER1(2.5F, 8.5F, 1.0F, 4_000),
        /** Strong / Tier2 — legacy guiScale 2.0 + mesh 1.5, kerosene/peroxide 8000 mB. */
        TIER2(2.0F, 6.5F, 1.5F, 8_000),
        /**
         * Huge / Tier3 — legacy TYPE_TIER3 guiScale 1.25 / guiOffset 1.0, generateStandard
         * (meshScale 1.0 — do not 1.5× like strong). Kerosene/loxy 12_000 mB.
         */
        TIER3(1.25F, 1.0F, 1.0F, 12_000),
        /**
         * Stealth — unique mesh. Legacy TYPE_STEALTH guiScale 1.75 / guiOffset 4.75.
         * Form factor is STRONG but tooltip stays Tier 1. Kerosene/peroxide 8000 mB.
         * meshScale 1.0 — do not 1.5x the unique mesh.
         */
        STEALTH(1.75F, 4.75F, 1.0F, 8_000),
        /**
         * Reliant Robin — unique shuttle mesh. Legacy TYPE_ROBIN guiScale 1.25 /
         * guiOffset 2. Form factor OTHER, fuelCap 8000 kerosene/peroxide.
         * Pad GUI scale is OTHER (1.0), not huge 0.925. meshScale 1.0.
         */
        ROBIN(1.25F, 2.0F, 1.0F, 8_000),
        /**
         * Atlas / Tier4 — legacy TYPE_NUCLEAR guiScale 1.375 / guiOffset 1.5,
         * generateStandard (meshScale 1.0). Form factor ATLAS, fuelCap 16_000
         * jetfuel/loxy.
         */
        TIER4(1.375F, 1.5F, 1.0F, 16_000),
        /**
         * Anti-ballistic interceptor — legacy TYPE_ABM guiScale 2.25 / guiOffset 7.
         * Form factor ABM, solid pre-fueled (fuelCap 0). Tooltip stays Tier 1.
         */
        ABM(2.25F, 7.0F, 1.0F, 0);

        public final float guiScale;
        public final float guiOffset;
        public final float meshScale;
        /** Pad tank drain per launch; 0 = solid / pre-fueled (power only). */
        public final int fuelCap;

        GuiTier(float guiScale, float guiOffset, float meshScale, int fuelCap) {
            this.guiScale = guiScale;
            this.guiOffset = guiOffset;
            this.meshScale = meshScale;
            this.fuelCap = fuelCap;
        }
    }

    private final GuiTier tier;
    private final boolean launchable;

    public MissileItem(GuiTier tier) {
        this(tier, true);
    }

    public MissileItem(GuiTier tier, boolean launchable) {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
        this.tier = tier;
        this.launchable = launchable;
    }

    public GuiTier getTier() {
        return tier;
    }

    public boolean isLaunchable() {
        return launchable;
    }

    /** Legacy {@code ItemMissile.fuelCap} — 0 means no fluid propellant required. */
    public int getFuelCap() {
        return tier.fuelCap;
    }

    public boolean requiresFluidFuel() {
        return tier.fuelCap > 0;
    }

    /** Legacy {@code ItemMissile.fuel} lang key for tooltip and pad status. */
    public String getFuelLangKey() {
        if (tier.fuelCap <= 0) {
            return "item.missile.fuel.solid.prefueled";
        }
        return switch (tier) {
            case TIER4 -> "item.missile.fuel.jetfuel_loxy";
            case TIER3 -> "item.missile.fuel.kerosene_loxy";
            case TIER2, STEALTH, ROBIN -> "item.missile.fuel.kerosene_peroxide";
            default -> "item.missile.fuel.ethanol_peroxide";
        };
    }

    private ChatFormatting getFuelStyle() {
        if (tier.fuelCap <= 0) {
            return ChatFormatting.GOLD;
        }
        return switch (tier) {
            case TIER4 -> ChatFormatting.RED;
            case TIER3 -> ChatFormatting.LIGHT_PURPLE;
            case TIER2, STEALTH, ROBIN -> ChatFormatting.BLUE;
            default -> ChatFormatting.AQUA;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String tierKey = switch (tier) {
            case STEALTH, ABM -> "item.missile.tier.tier1";
            case ROBIN -> "item.missile.tier.tier3";
            default -> "item.missile.tier." + tier.name().toLowerCase();
        };
        tooltip.add(Component.translatable(tierKey).withStyle(ChatFormatting.ITALIC));

        if (!launchable) {
            tooltip.add(Component.translatable("item.missile.desc.notLaunchable").withStyle(ChatFormatting.RED));
            return;
        }

        Component fuelName = Component.translatable(getFuelLangKey()).withStyle(getFuelStyle());
        tooltip.add(Component.translatable("item.missile.desc.fuel")
                .append(": ")
                .append(fuelName));
        if (tier.fuelCap > 0) {
            tooltip.add(Component.translatable("item.missile.desc.fuelCapacity")
                    .append(": " + tier.fuelCap + "mB")
                    .withStyle(ChatFormatting.GRAY));
        }

        var key = ForgeRegistries.ITEMS.getKey(this);
        if (key != null && "missile_taint".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_taint.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_schrabidium".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_schrabidium.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_emp_strong".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_emp_strong.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_emp".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_emp.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_decoy".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_decoy.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_stealth".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_stealth.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_burst".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_burst.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_inferno".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_inferno.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_rain".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_rain.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_drill".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_drill.desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (key != null && "missile_volcano".equals(key.getPath())) {
            tooltip.add(Component.translatable("item.hbm.missile_volcano.desc")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.hbm.client.render.item.MissileItemRenderer.get();
            }
        });
    }
}
