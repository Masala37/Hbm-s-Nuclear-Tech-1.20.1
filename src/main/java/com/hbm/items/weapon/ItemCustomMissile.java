package com.hbm.items.weapon;

import com.hbm.entity.missile.MissileSystemRules;
import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissilePart.FuelType;
import com.hbm.items.weapon.ItemCustomMissilePart.PartType;
import com.hbm.items.weapon.ItemCustomMissilePart.WarheadType;
import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ItemCustomMissile extends Item {
    public static final String TAG_CHIP = "chip";
    public static final String TAG_WARHEAD = "warhead";
    public static final String TAG_FUSELAGE = "fuselage";
    public static final String TAG_STABILITY = "stability";
    public static final String TAG_THRUSTER = "thruster";

    public ItemCustomMissile() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack buildMissile(ItemStack chip, ItemStack warhead, ItemStack fuselage,
                                         ItemStack stability, ItemStack thruster) {
        ItemStack missile = new ItemStack(ModItems.MISSILE_CUSTOM.get());
        writePart(missile, TAG_CHIP, chip);
        writeToNBT(missile, TAG_WARHEAD, warhead);
        writeToNBT(missile, TAG_FUSELAGE, fuselage);
        writeToNBT(missile, TAG_THRUSTER, thruster);
        if (stability != null && !stability.isEmpty()) {
            writeToNBT(missile, TAG_STABILITY, stability);
        }
        return missile;
    }

    private static void writePart(ItemStack missile, String key, ItemStack part) {
        if (part != null && !part.isEmpty()) {
            writeToNBT(missile, key, part);
        }
    }

    private static void writeToNBT(ItemStack stack, String key, ItemStack part) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(part.getItem());
        if (id == null) {
            return;
        }
        stack.getOrCreateTag().putString(key, id.toString());
    }

    public static String readId(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(key);
    }

    public static ItemCustomMissilePart readPart(ItemStack stack, String key) {
        String id = readId(stack, key);
        if (id == null || id.isEmpty()) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return ItemCustomMissilePart.of(item);
    }

    public static MissileStruct getStruct(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCustomMissile)) {
            return null;
        }
        return new MissileStruct(
                readPart(stack, TAG_WARHEAD),
                readPart(stack, TAG_FUSELAGE),
                readPart(stack, TAG_STABILITY),
                readPart(stack, TAG_THRUSTER));
    }

    public static boolean isPadLaunchable(ItemStack stack) {
        ItemCustomMissilePart fuselage = readPart(stack, TAG_FUSELAGE);
        return fuselage != null && fuselage.type == PartType.FUSELAGE
                && MissileSystemRules.isPadLaunchable(fuselage.top.name(), fuselage.bottom.name())
                && readPart(stack, TAG_WARHEAD) != null && readPart(stack, TAG_THRUSTER) != null;
    }

    public static int getFuelCap(ItemStack stack) {
        ItemCustomMissilePart fuselage = readPart(stack, TAG_FUSELAGE);
        if (fuselage == null || fuselage.attributes == null || fuselage.attributes.length < 2) {
            return 0;
        }
        FuelType fuel = (FuelType) fuselage.attributes[0];
        return MissileSystemRules.fuelCapacity(fuel.name(), (Float) fuselage.attributes[1]);
    }

    /**
     * Legacy compact-launcher miss: scale (pad - target) by chip * fin, rotate by a random angle.
     */
    public static int[] applyInaccuracy(ItemStack stack, int padX, int padZ, int targetX, int targetZ,
                                         RandomSource random) {
        ItemCustomMissilePart chip = readPart(stack, TAG_CHIP);
        float c = chip != null && chip.attributes != null ? (Float) chip.attributes[0] : 1.0F;
        float f = 1.0F;
        ItemCustomMissilePart fins = readPart(stack, TAG_STABILITY);
        if (fins != null && fins.attributes != null) {
            f = (Float) fins.attributes[0];
        }
        return MissileSystemRules.scatterTarget(padX, padZ, targetX, targetZ, c, f, random.nextFloat() * 360.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!stack.hasTag()) {
            return;
        }
        try {
            ItemCustomMissilePart chip = readPart(stack, TAG_CHIP);
            ItemCustomMissilePart warhead = readPart(stack, TAG_WARHEAD);
            ItemCustomMissilePart fuselage = readPart(stack, TAG_FUSELAGE);
            ItemCustomMissilePart stability = readPart(stack, TAG_STABILITY);
            ItemCustomMissilePart thruster = readPart(stack, TAG_THRUSTER);
            if (warhead == null || fuselage == null) {
                tooltip.add(Component.translatable("error.generic").withStyle(ChatFormatting.RED));
                return;
            }
            tooltip.add(Component.translatable("item.missile.desc.warhead").withStyle(ChatFormatting.BOLD)
                    .append(": ").append(warhead.getWarhead((WarheadType) warhead.attributes[0])));
            tooltip.add(Component.translatable("item.missile.desc.strength").withStyle(ChatFormatting.BOLD)
                    .append(": ").append(Component.literal(String.valueOf((Float) warhead.attributes[1]))
                            .withStyle(ChatFormatting.GRAY)));
            tooltip.add(Component.translatable("item.missile.desc.fuelType").withStyle(ChatFormatting.BOLD)
                    .append(": ").append(fuselage.getFuel((FuelType) fuselage.attributes[0])));
            tooltip.add(Component.translatable("item.missile.desc.fuelAmount").withStyle(ChatFormatting.BOLD)
                    .append(": ").append(Component.literal((Float) fuselage.attributes[1] + "l")
                            .withStyle(ChatFormatting.GRAY)));
            if (chip != null) {
                tooltip.add(Component.translatable("item.missile.desc.chipInaccuracy").withStyle(ChatFormatting.BOLD)
                        .append(": ").append(Component.literal((Float) chip.attributes[0] * 100 + "%")
                                .withStyle(ChatFormatting.GRAY)));
            }
            if (stability != null) {
                tooltip.add(Component.translatable("item.missile.desc.finInaccuracy").withStyle(ChatFormatting.BOLD)
                        .append(": ").append(Component.literal((Float) stability.attributes[0] * 100 + "%")
                                .withStyle(ChatFormatting.GRAY)));
            } else {
                tooltip.add(Component.translatable("item.missile.desc.finInaccuracy").withStyle(ChatFormatting.BOLD)
                        .append(": ").append(Component.literal("100%").withStyle(ChatFormatting.GRAY)));
            }
            tooltip.add(Component.translatable("item.missile.desc.size").withStyle(ChatFormatting.BOLD)
                    .append(": ").append(Component.literal(fuselage.getSize(fuselage.top) + "/"
                            + fuselage.getSize(fuselage.bottom)).withStyle(ChatFormatting.GRAY)));
            float health = warhead.health + fuselage.health + (thruster != null ? thruster.health : 0);
            if (stability != null) {
                health += stability.health;
            }
            tooltip.add(Component.translatable("item.missile.desc.health").withStyle(ChatFormatting.BOLD)
                    .append(": ").append(Component.literal(health + "HP").withStyle(ChatFormatting.GRAY)));
        } catch (Exception ex) {
            tooltip.add(Component.translatable("error.generic").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.hbm.client.render.item.CustomMissileItemRenderer.get();
            }
        });
    }
}
