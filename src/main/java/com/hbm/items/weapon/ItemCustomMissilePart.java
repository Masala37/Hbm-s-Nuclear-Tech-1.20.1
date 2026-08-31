package com.hbm.items.weapon;

import com.hbm.entity.missile.EntityMissileCustom;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ItemCustomMissilePart extends Item {
    public PartType type;
    public PartSize top;
    public PartSize bottom;
    public Rarity rarity;
    public float health;
    private String title;
    private String author;
    private String witty;
    private boolean hiddenFromCreative;

    /**
     * Chips: [0] inaccuracy.
     * Warheads: [0] type, [1] strength, [2] weight.
     * Fuselages: [0] fuel type, [1] tank size.
     * Fins: [0] inaccuracy mod.
     * Thrusters: [0] fuel type, [1] consumption, [2] lift.
     */
    public Object[] attributes;

    public ItemCustomMissilePart() {
        super(new Item.Properties().stacksTo(1));
    }

    public enum PartType {
        CHIP,
        WARHEAD,
        FUSELAGE,
        FINS,
        THRUSTER
    }

    public enum PartSize {
        ANY,
        NONE,
        SIZE_10,
        SIZE_15,
        SIZE_20
    }

    public enum WarheadType {
        HE,
        INC,
        BUSTER,
        CLUSTER,
        NUCLEAR,
        TX,
        N2,
        BALEFIRE,
        SCHRAB,
        TAINT,
        CLOUD,
        TURBINE,
        CUSTOM0, CUSTOM1, CUSTOM2, CUSTOM3, CUSTOM4, CUSTOM5, CUSTOM6, CUSTOM7, CUSTOM8, CUSTOM9;

        public Consumer<EntityMissileCustom> impactCustom;
        public Consumer<EntityMissileCustom> updateCustom;
        public String labelCustom;
    }

    public enum FuelType {
        KEROSENE,
        SOLID,
        HYDROGEN,
        XENON,
        BALEFIRE
    }

    public enum Rarity {
        COMMON("item.missile.part.rarity.common", ChatFormatting.GRAY),
        UNCOMMON("item.missile.part.rarity.uncommon", ChatFormatting.YELLOW),
        RARE("item.missile.part.rarity.rare", ChatFormatting.AQUA),
        EPIC("item.missile.part.rarity.epic", ChatFormatting.LIGHT_PURPLE),
        LEGENDARY("item.missile.part.rarity.legendary", ChatFormatting.DARK_GREEN),
        SEWS_CLOTHES_AND_SUCKS_HORSE_COCK("item.missile.part.rarity.strange", ChatFormatting.DARK_AQUA);

        private final String key;
        private final ChatFormatting color;

        Rarity(String key, ChatFormatting color) {
            this.key = key;
            this.color = color;
        }

        public Component getDisplay() {
            return Component.translatable(key).withStyle(color);
        }
    }

    public ItemCustomMissilePart makeChip(float inaccuracy) {
        this.type = PartType.CHIP;
        this.top = PartSize.ANY;
        this.bottom = PartSize.ANY;
        this.attributes = new Object[] { inaccuracy };
        return this;
    }

    public ItemCustomMissilePart makeWarhead(WarheadType warhead, float punch, float weight, PartSize size) {
        this.type = PartType.WARHEAD;
        this.top = PartSize.NONE;
        this.bottom = size;
        this.attributes = new Object[] { warhead, punch, weight };
        return this;
    }

    public ItemCustomMissilePart makeFuselage(FuelType fuel, float tank, PartSize top, PartSize bottom) {
        this.type = PartType.FUSELAGE;
        this.top = top;
        this.bottom = bottom;
        this.attributes = new Object[] { fuel, tank };
        return this;
    }

    public ItemCustomMissilePart makeStability(float inaccuracy, PartSize size) {
        this.type = PartType.FINS;
        this.top = size;
        this.bottom = size;
        this.attributes = new Object[] { inaccuracy };
        return this;
    }

    public ItemCustomMissilePart makeThruster(FuelType fuel, float consumption, float lift, PartSize size) {
        this.type = PartType.THRUSTER;
        this.top = size;
        this.bottom = PartSize.NONE;
        this.attributes = new Object[] { fuel, consumption, lift };
        return this;
    }

    public ItemCustomMissilePart copy() {
        ItemCustomMissilePart part = new ItemCustomMissilePart();
        part.type = this.type;
        part.top = this.top;
        part.bottom = this.bottom;
        part.health = this.health;
        part.attributes = this.attributes;
        return part;
    }

    public ItemCustomMissilePart setAuthor(String author) {
        this.author = author;
        return this;
    }

    public ItemCustomMissilePart setTitle(String title) {
        this.title = title;
        return this;
    }

    public ItemCustomMissilePart setWittyText(String witty) {
        this.witty = witty;
        return this;
    }

    public ItemCustomMissilePart setHealth(float health) {
        this.health = health;
        return this;
    }

    public ItemCustomMissilePart setRarity(Rarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemCustomMissilePart hideFromCreative() {
        this.hiddenFromCreative = true;
        return this;
    }

    public boolean isHiddenFromCreative() {
        return hiddenFromCreative;
    }

    public static ItemCustomMissilePart of(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCustomMissilePart part)) {
            return null;
        }
        return part;
    }

    public static ItemCustomMissilePart of(Item item) {
        return item instanceof ItemCustomMissilePart part ? part : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (title != null) {
            tooltip.add(Component.literal("\"" + title + "\"").withStyle(ChatFormatting.DARK_PURPLE));
        }
        try {
            switch (type) {
                case CHIP -> tooltip.add(stat("item.missile.part.inaccuracy", (Float) attributes[0] * 100 + "%"));
                case WARHEAD -> {
                    tooltip.add(stat("item.missile.part.size", getSize(bottom)));
                    tooltip.add(stat("item.missile.part.type", getWarhead((WarheadType) attributes[0])));
                    tooltip.add(stat("item.missile.part.strength", String.valueOf((Float) attributes[1])));
                    tooltip.add(stat("item.missile.part.weight", (Float) attributes[2] + "t"));
                }
                case FUSELAGE -> {
                    tooltip.add(stat("item.missile.part.topSize", getSize(top)));
                    tooltip.add(stat("item.missile.part.bottomSize", getSize(bottom)));
                    tooltip.add(stat("item.missile.part.fuelType", getFuel((FuelType) attributes[0])));
                    tooltip.add(stat("item.missile.part.fuelAmount", (Float) attributes[1] + "l"));
                }
                case FINS -> {
                    tooltip.add(stat("item.missile.part.size", getSize(top)));
                    tooltip.add(stat("item.missile.part.inaccuracy", (Float) attributes[0] * 100 + "%"));
                }
                case THRUSTER -> {
                    tooltip.add(stat("item.missile.part.size", getSize(top)));
                    tooltip.add(stat("item.missile.part.fuelType", getFuel((FuelType) attributes[0])));
                    tooltip.add(stat("item.missile.part.fuelConsumption", (Float) attributes[1] + "l/tick"));
                    tooltip.add(stat("item.missile.part.maxPayload", (Float) attributes[2] + "t"));
                }
            }
        } catch (Exception ex) {
            tooltip.add(Component.translatable("error.generic"));
        }
        if (type != PartType.CHIP) {
            tooltip.add(stat("item.missile.part.health", health + "HP"));
        }
        if (rarity != null) {
            tooltip.add(boldKey("item.missile.part.rarity").append(": ").withStyle(ChatFormatting.BOLD)
                    .append(rarity.getDisplay()));
        }
        if (author != null) {
            tooltip.add(Component.literal("   ").append(Component.translatable("item.missile.part.by"))
                    .append(" " + author).withStyle(ChatFormatting.WHITE));
        }
        if (witty != null) {
            tooltip.add(Component.literal("   \"" + witty + "\"")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
    }

    private static MutableComponent boldKey(String key) {
        return Component.translatable(key).withStyle(ChatFormatting.BOLD);
    }

    private static Component stat(String key, String value) {
        return boldKey(key).append(": ").append(Component.literal(value).withStyle(ChatFormatting.GRAY));
    }

    private static Component stat(String key, Component value) {
        return boldKey(key).append(": ").append(value);
    }

    public String getSize(PartSize size) {
        return switch (size) {
            case ANY -> Component.translatable("item.missile.part.size.any").getString();
            case SIZE_10 -> "1.0m";
            case SIZE_15 -> "1.5m";
            case SIZE_20 -> "2.0m";
            default -> Component.translatable("item.missile.part.size.none").getString();
        };
    }

    public Component getWarhead(WarheadType warhead) {
        if (warhead.labelCustom != null) {
            return Component.literal(warhead.labelCustom);
        }
        return switch (warhead) {
            case HE -> Component.translatable("item.warhead.desc.he").withStyle(ChatFormatting.YELLOW);
            case INC -> Component.translatable("item.warhead.desc.incendiary").withStyle(ChatFormatting.GOLD);
            case CLUSTER -> Component.translatable("item.warhead.desc.cluster").withStyle(ChatFormatting.GRAY);
            case BUSTER -> Component.translatable("item.warhead.desc.bunker_buster").withStyle(ChatFormatting.WHITE);
            case NUCLEAR -> Component.translatable("item.warhead.desc.nuclear").withStyle(ChatFormatting.DARK_GREEN);
            case TX -> Component.translatable("item.warhead.desc.thermonuclear").withStyle(ChatFormatting.DARK_PURPLE);
            case N2 -> Component.translatable("item.warhead.desc.n2").withStyle(ChatFormatting.RED);
            case BALEFIRE -> Component.translatable("item.warhead.desc.balefire").withStyle(ChatFormatting.GREEN);
            case SCHRAB -> Component.translatable("item.warhead.desc.schrabidium").withStyle(ChatFormatting.AQUA);
            case TAINT -> Component.translatable("item.warhead.desc.taint").withStyle(ChatFormatting.DARK_PURPLE);
            case CLOUD -> Component.translatable("item.warhead.desc.cloud").withStyle(ChatFormatting.LIGHT_PURPLE);
            case TURBINE -> Component.translatable("item.warhead.desc.turbine")
                    .withStyle(System.currentTimeMillis() % 1000 < 500 ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            default -> Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        };
    }

    public Component getFuel(FuelType fuel) {
        return switch (fuel) {
            case KEROSENE -> Component.translatable("item.missile.fuel.kerosene_peroxide").withStyle(ChatFormatting.LIGHT_PURPLE);
            case SOLID -> Component.translatable("item.missile.fuel.solid").withStyle(ChatFormatting.GOLD);
            case HYDROGEN -> Component.translatable("item.missile.fuel.hydrogen").withStyle(ChatFormatting.DARK_AQUA);
            case XENON -> Component.translatable("item.missile.fuel.xenon").withStyle(ChatFormatting.DARK_PURPLE);
            case BALEFIRE -> Component.translatable("item.missile.fuel.balefire").withStyle(ChatFormatting.GREEN);
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (type == PartType.CHIP) {
            return;
        }
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.hbm.client.render.item.MissilePartItemRenderer.get();
            }
        });
    }
}
