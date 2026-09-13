package com.hbm.items.machine;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code ItemFELCrystal}. Stack size 1. Wavelength is per item, not NBT.
 */
public class ItemFELCrystal extends Item {
    public enum EnumWavelengths {
        NULL("la creatura", "6 dollar", 0x010101, 0x010101, ChatFormatting.WHITE),
        IR("wavelengths.name.ir", "wavelengths.waveRange.ir", 0xBB1010, 0xCC4040, ChatFormatting.RED),
        VISIBLE("wavelengths.name.visible", "wavelengths.waveRange.visible", 0, 0, ChatFormatting.GREEN),
        UV("wavelengths.name.uv", "wavelengths.waveRange.uv", 0x0A1FC4, 0x00EFFF, ChatFormatting.AQUA),
        GAMMA("wavelengths.name.gamma", "wavelengths.waveRange.gamma", 0x150560, 0xEF00FF, ChatFormatting.LIGHT_PURPLE),
        DRX("wavelengths.name.drx", "wavelengths.waveRange.drx", 0xFF0000, 0xFF0000, ChatFormatting.DARK_RED);

        public final String nameKey;
        public final String rangeKey;
        public final int renderedBeamColor;
        public final int guiColor;
        public final ChatFormatting textColor;

        EnumWavelengths(String nameKey, String rangeKey, int beam, int gui, ChatFormatting text) {
            this.nameKey = nameKey;
            this.rangeKey = rangeKey;
            this.renderedBeamColor = beam;
            this.guiColor = gui;
            this.textColor = text;
        }

        public static EnumWavelengths byName(String name) {
            if (name == null || name.isEmpty()) {
                return NULL;
            }
            try {
                return valueOf(name);
            } catch (IllegalArgumentException ignored) {
                return NULL;
            }
        }
    }

    private final EnumWavelengths wavelength;

    public ItemFELCrystal(EnumWavelengths wavelength) {
        super(new Item.Properties().stacksTo(1));
        this.wavelength = wavelength;
    }

    public EnumWavelengths wavelength() {
        return wavelength;
    }

    public static EnumWavelengths of(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemFELCrystal crystal)) {
            return EnumWavelengths.NULL;
        }
        return crystal.wavelength;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (wavelength == EnumWavelengths.DRX) {
            tooltip.add(Component.literal("THERADIANCEOFATHOUSANDSUNS").withStyle(ChatFormatting.OBFUSCATED));
        } else {
            tooltip.add(Component.translatable(stack.getDescriptionId() + ".desc"));
        }
        tooltip.add(Component.translatable(wavelength.nameKey).withStyle(wavelength.textColor)
                .append(Component.literal(" - ").withStyle(wavelength.textColor))
                .append(Component.translatable(wavelength.rangeKey).withStyle(wavelength.textColor)));
    }
}
