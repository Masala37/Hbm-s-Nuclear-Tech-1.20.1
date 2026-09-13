package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DiFurnaceBlockEntity;
import com.hbm.blockentity.machine.DiFurnaceExtensionBlockEntity;
import com.hbm.inventory.menu.HbmMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code MachineDiFurnaceExtension}: chimney on top of {@code machine_difurnace}.
 * Opens the furnace GUI below and triples cook speed.
 */
public class MachineDiFurnaceExtensionBlock extends BaseEntityBlock {
    public MachineDiFurnaceExtensionBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiFurnaceExtensionBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below instanceof DiFurnaceBlockEntity) {
            HbmMenuHelper.open(sp, below);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
