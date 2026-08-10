package com.hbm.hazard;

import com.hbm.hazard.type.HazardTypeBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Item/block hazard lookup and application (legacy {@code HazardSystem}, item-map subset).
 */
public final class HazardSystem {

    public static final Map<Item, HazardData> itemMap = new IdentityHashMap<>();

    private HazardSystem() {
    }

    public static void register(Item item, HazardData data) {
        if (item != null && data != null) {
            itemMap.put(item, data);
        }
    }

    public static void register(Block block, HazardData data) {
        if (block != null) {
            register(block.asItem(), data);
        }
    }

    public static List<HazardEntry> getHazardsFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }
        HazardData data = itemMap.get(stack.getItem());
        if (data == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(data.getEntries());
    }

    public static float getHazardLevelFromStack(ItemStack stack, HazardTypeBase hazard) {
        for (HazardEntry entry : getHazardsFromStack(stack)) {
            if (entry.getType() == hazard) {
                return entry.getBaseLevel();
            }
        }
        return 0.0F;
    }

    public static void applyHazards(ItemStack stack, LivingEntity entity) {
        if (stack == null || stack.isEmpty() || entity == null) {
            return;
        }
        for (HazardEntry hazard : getHazardsFromStack(stack)) {
            hazard.applyHazard(stack, entity);
        }
    }

    public static void updatePlayerInventory(Player player) {
        if (player == null) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                applyHazards(stack, player);
            }
        }
    }

    public static void updateLivingInventory(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                applyHazards(stack, entity);
            }
        }
    }

    public static void addFullTooltip(ItemStack stack, Player player, List<Component> list) {
        if (stack == null || stack.isEmpty() || list == null) {
            return;
        }
        for (HazardEntry hazard : getHazardsFromStack(stack)) {
            hazard.getType().addHazardInformation(player, list, hazard.getBaseLevel(), stack);
        }
    }
}
