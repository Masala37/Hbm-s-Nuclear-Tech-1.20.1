package com.hbm.blocks.generic;

import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Contaminated soil left by nuclear fallout (legacy WasteEarth).
 */
public class WasteEarthBlock extends Block {
    public WasteEarthBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .strength(0.6F)
                .sound(SoundType.GRASS));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.HAZMAT, 0.5F);
        }
        super.stepOn(level, pos, state, entity);
    }
}
