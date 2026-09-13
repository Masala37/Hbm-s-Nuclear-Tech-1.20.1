package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.MachineDrops;
import com.hbm.blockentity.machine.MixerBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 {@code MachineMixer}: Dummyable {@code {2,0,0,0,0,0}} (1×1×3). No extras.
 */
public class MachineMixerBlock extends BlockDummyable {
    public static final int[] DIM = {2, 0, 0, 0, 0, 0};

    public MachineMixerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 30.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    public int[] getDimensions() {
        return DIM;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new MixerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state)) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_MIXER.get(), MixerBlockEntity::tick);
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
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (!(be instanceof MixerBlockEntity mixer)) {
            return super.use(state, level, pos, player, hand, hit);
        }
        ItemStack held = player.getItemInHand(hand);
        Fluid fromItem = fluidFromHeld(held);
        if (held.is(ModItems.FLUID_IDENTIFIER.get())) {
            mixer.setOutputType(MixerRecipes.cycleNext(mixer.getOutputType()));
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(mixer.getOutputType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        } else if (fromItem != null && fromItem != mixer.getOutputType()) {
            mixer.setOutputType(fromItem);
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(mixer.getOutputType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && isCore(state)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MixerBlockEntity mixer) {
                MachineDrops.drop(level, pos, mixer.getItems());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static @Nullable Fluid fluidFromHeld(ItemStack held) {
        return FluidUtil.getFluidContained(held)
                .filter(stack -> !stack.isEmpty() && MixerRecipes.isRecipeOutput(stack.getFluid()))
                .map(FluidStack::getFluid)
                .orElse(null);
    }
}
