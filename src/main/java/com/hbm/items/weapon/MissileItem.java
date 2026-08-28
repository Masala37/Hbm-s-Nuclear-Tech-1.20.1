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
 * (MICRO/TIER0 = solid pre-fueled → 0 mB; V2 = 4000; Strong = 8000; Huge = 12000 kerosene/loxy).
 */
public class MissileItem extends Item {
    public enum GuiTier {
        /** Micro / Tier0 — legacy guiScale 3.75, solid pre-fueled (fuelCap 0). */
        TIER0(3.75F, 10.75F, 1.0F, 0),
        /** V2 / Tier1 — legacy guiScale 2.5, ethanol/peroxide 4000 mB. */
        TIER1(2.5F, 8.5F, 1.0F, 4_000),
        /** Strong / Tier2 — legacy guiScale 2.0 + mesh 1.5.
         * Legacy fuelCap is 8000 kerosene; this pad only stocks ethanol/peroxide,
         * so keep the previous 4000 mB drain until kerosene tanks are ported. */
        TIER2(2.0F, 6.5F, 1.5F, 4_000),
        /**
         * Huge / Tier3 — legacy TYPE_TIER3 guiScale 1.25 / guiOffset 1.0, generateStandard
         * (meshScale 1.0 — do not 1.5× like strong). Legacy fuelCap is 12_000 mB
         * kerosene/loxy; this pad only stocks ethanol/peroxide, so 4000 mB like other
         * liquid missiles until loxy/kerosene tanks exist.
         */
        TIER3(1.25F, 1.0F, 1.0F, 4_000),
        /**
         * Stealth — unique mesh. Legacy TYPE_STEALTH guiScale 1.75 / guiOffset 4.75.
         * Form factor is STRONG but tooltip stays Tier 1. Legacy fuel is 8000
         * kerosene/peroxide; this pad only stocks ethanol/peroxide, so 4000 mB
         * like other strong missiles until kerosene tanks are ported.
         * meshScale 1.0 — do not 1.5x the unique mesh.
         */
        STEALTH(1.75F, 4.75F, 1.0F, 4_000);

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

    public MissileItem(GuiTier tier) {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
        this.tier = tier;
    }

    public GuiTier getTier() {
        return tier;
    }

    /** Legacy {@code ItemMissile.fuelCap} — 0 means no fluid propellant required. */
    public int getFuelCap() {
        return tier.fuelCap;
    }

    public boolean requiresFluidFuel() {
        return tier.fuelCap > 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String tierKey = tier == GuiTier.STEALTH
                ? "item.missile.tier.tier1"
                : "item.missile.tier." + tier.name().toLowerCase();
        tooltip.add(Component.translatable(tierKey).withStyle(ChatFormatting.ITALIC));

        Component fuelName = tier.fuelCap <= 0
                ? Component.translatable("item.missile.fuel.solid.prefueled").withStyle(ChatFormatting.GOLD)
                : Component.translatable("item.missile.fuel.ethanol_peroxide").withStyle(ChatFormatting.AQUA);
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
