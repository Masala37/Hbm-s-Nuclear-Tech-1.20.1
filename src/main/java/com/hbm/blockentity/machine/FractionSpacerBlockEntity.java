package com.hbm.blockentity.machine;

import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 1.7.10 {@code TileEntitySpacer}. TESR only; breaks stacked fraction towers.
 */
public class FractionSpacerBlockEntity extends BlockEntity {
    public FractionSpacerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FRACTION_SPACER.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 1, 2));
    }
}
