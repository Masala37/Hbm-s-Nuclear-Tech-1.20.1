package com.hbm.items.armor;

import com.hbm.item.HbmArmorMaterials;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Yellow / red / grey hazmat pieces (legacy {@code ArmorHazmat} / {@code ArmorHazmatMask}).
 */
public class HazmatArmorItem extends ArmorItem {
    public enum Worn {
        YELLOW("hbm:textures/armor/hazmat_1.png", "hbm:textures/armor/hazmat_2.png", false, true),
        RED("hbm:textures/armor/hazmat_1_red.png", "hbm:textures/armor/hazmat_2_red.png", true, false),
        GREY("hbm:textures/armor/hazmat_1_grey.png", "hbm:textures/armor/hazmat_2_grey.png", true, false);

        public final String outer;
        public final String inner;
        public final boolean maskHelmet;
        public final boolean helmetOverlay;

        Worn(String outer, String inner, boolean maskHelmet, boolean helmetOverlay) {
            this.outer = outer;
            this.inner = inner;
            this.maskHelmet = maskHelmet;
            this.helmetOverlay = helmetOverlay;
        }
    }

    private final Worn worn;

    public HazmatArmorItem(HbmArmorMaterials material, Type type, Worn worn) {
        super(material, type, new Item.Properties());
        this.worn = worn;
    }

    public Worn worn() {
        return worn;
    }

    public boolean helmetOverlay() {
        return worn.helmetOverlay && getType() == Type.HELMET;
    }

    public boolean maskHelmet() {
        return worn.maskHelmet && getType() == Type.HELMET;
    }

    public String wornTexture() {
        if (maskHelmet()) {
            return worn == Worn.GREY
                    ? "hbm:textures/models/ModelHazGrey.png"
                    : "hbm:textures/models/ModelHazRed.png";
        }
        return getType() == Type.LEGGINGS ? worn.inner : worn.outer;
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return wornTexture();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        com.hbm.client.HazmatArmorClient.attach(this, consumer);
    }
}
