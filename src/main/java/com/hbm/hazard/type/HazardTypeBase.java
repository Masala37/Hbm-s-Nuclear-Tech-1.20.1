package com.hbm.hazard.type;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Base hazard effect applied from inventory stacks (legacy {@code HazardTypeBase}).
 */
public abstract class HazardTypeBase {

    /**
     * Applies this hazard to the holder each tick while the stack is in inventory.
     *
     * @param target holder
     * @param level  final level after modifiers
     * @param stack  stack being updated
     */
    public abstract void onUpdate(LivingEntity target, float level, ItemStack stack);

    /**
     * Adds tooltip lines describing this hazard.
     */
    public abstract void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack);
}
