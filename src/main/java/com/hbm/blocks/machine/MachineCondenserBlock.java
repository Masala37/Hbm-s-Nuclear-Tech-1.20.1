package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.CondenserBlockEntity;
import com.hbm.blocks.ILookOverlay;
import com.hbm.fluid.SteamCycle;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
import com.hbm.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 {@code MachineCondenser}: 1-block overlay-only condenser.
 */
public class MachineCondenserBlock extends BaseEntityBlock implements ILookOverlay {
    public MachineCondenserBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CondenserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_CONDENSER.get(), CondenserBlockEntity::tick);
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
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CondenserBlockEntity condenser && player instanceof ServerPlayer
                && InfiniteFluidBarrelItem.interact(player, hand, condenser.getFluidHandler(), SteamCycle.spentSteam())) {
            condenser.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void printHook(Level level, BlockPos pos, List<Component> lines) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CondenserBlockEntity condenser)) {
            return;
        }
        var spent = condenser.getSpent().getFluid();
        var water = condenser.getWater().getFluid();
        String spentName = spent.isEmpty() ? "Spent Steam" : spent.getDisplayName().getString();
        String waterName = water.isEmpty() ? "Water" : water.getDisplayName().getString();
        lines.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(spentName + ": " + condenser.getSpent().getFluidAmount()
                        + "/" + condenser.getSpent().getCapacity() + "mB")));
        lines.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(waterName + ": " + condenser.getWater().getFluidAmount()
                        + "/" + condenser.getWater().getCapacity() + "mB")));
    }
}
