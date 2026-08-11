package com.hbm.items.weapon;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Missile item with legacy-style 3D inventory / hand rendering (BEWLR).
 */
public class MissileItem extends Item {
    public enum GuiTier {
        /** V2 / Tier1 — legacy guiScale 2.5 */
        TIER1(2.5F, 8.5F, 1.0F),
        /** Strong / Tier2 — legacy guiScale 2.0 + mesh 1.5 */
        TIER2(2.0F, 6.5F, 1.5F);

        public final float guiScale;
        public final float guiOffset;
        public final float meshScale;

        GuiTier(float guiScale, float guiOffset, float meshScale) {
            this.guiScale = guiScale;
            this.guiOffset = guiOffset;
            this.meshScale = meshScale;
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
