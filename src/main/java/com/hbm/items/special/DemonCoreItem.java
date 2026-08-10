package com.hbm.items.special;

import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Open demon core — closes when dropped on the ground (legacy {@code ItemDemonCore}).
 */
public class DemonCoreItem extends Item {
    public DemonCoreItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.demon_core_open.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("trait.hbm.drop").withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide || !entity.onGround()) {
            return false;
        }

        Item closed = ForgeRegistries.ITEMS.getValue(new ResourceLocation("hbm", "demon_core_closed"));
        if (closed == null || closed == Items.AIR) {
            return false;
        }

        entity.setItem(new ItemStack(closed));
        ItemEntity tool = new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.2D, entity.getZ(),
                new ItemStack(ModItems.SCREWDRIVER.get()));
        tool.setDeltaMovement(entity.level().random.nextGaussian() * 0.05D, 0.2D,
                entity.level().random.nextGaussian() * 0.05D);
        entity.level().addFreshEntity(tool);
        return false;
    }
}
