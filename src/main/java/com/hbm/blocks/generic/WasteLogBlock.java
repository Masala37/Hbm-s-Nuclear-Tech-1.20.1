package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * Petrified waste wood (legacy {@code WasteLog}).
 */
public class WasteLogBlock extends RotatedPillarBlock {
    public WasteLogBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(5.0F, 2.5F)
                .sound(SoundType.WOOD));
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity be, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, be, tool);
        if (!level.isClientSide) {
            int count = 2 + level.random.nextInt(3);
            popResource(level, pos, new ItemStack(Items.COAL, count));
        }
    }
}
