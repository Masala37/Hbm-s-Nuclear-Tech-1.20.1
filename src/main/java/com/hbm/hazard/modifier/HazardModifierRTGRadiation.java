package com.hbm.hazard.modifier;

import com.hbm.items.machine.ItemRTGPellet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 1.7.10 {@code HazardModifierRTGRadiation}: lerp radiation toward {@code target} as the pellet depletes.
 */
public class HazardModifierRTGRadiation extends HazardModifier {
    private final float target;

    public HazardModifierRTGRadiation(float target) {
        this.target = target;
    }

    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        if (stack.getItem() instanceof ItemRTGPellet fuel) {
            double depletion = fuel.depletionFraction(stack);
            level = (float) (level + (this.target - level) * depletion);
        }
        return level;
    }
}
