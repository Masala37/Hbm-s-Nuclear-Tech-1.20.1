package com.hbm.items.tool;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.GeigerClicks;
import com.hbm.handler.GeigerSound;
import com.hbm.registry.ModSounds;
import com.hbm.util.ContaminationUtil;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Handheld Geiger counter (legacy {@code ItemGeigerCounter}).
 */
public class GeigerCounterItem extends Item {
    public GeigerCounterItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (level.getGameTime() % 5L != 0L) {
            return;
        }
        int track = GeigerClicks.pickTrack(HbmLivingProps.getRadBuf(living), level.random::nextInt);
        GeigerSound.play(level, entity.getX(), entity.getY(), entity.getZ(), track);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.TECH_BOOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            ContaminationUtil.printGeigerData(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
