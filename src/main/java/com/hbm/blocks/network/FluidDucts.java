package com.hbm.blocks.network;

import com.hbm.blockentity.network.FluidPipeBlockEntity;
import com.hbm.fluid.IFluidPipe;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Shared 1.7 {@code FluidDuctBase} / valve metadata helpers.
 */
public final class FluidDucts {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    private FluidDucts() {
    }

    public static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    public static boolean isOpen(BlockState state) {
        return state.hasProperty(OPEN) && state.getValue(OPEN);
    }

    public static void setOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        if (isOpen(state) == open) {
            return;
        }
        level.setBlock(pos, state.setValue(OPEN, open), Block.UPDATE_CLIENTS);
        level.playSound(null, pos, ModSounds.require("block.reactor_start"), SoundSource.BLOCKS, 1.0F,
                open ? 1.0F : 0.85F);
    }

    public static InteractionResult trySetType(Level level, ItemStack held, FluidPipeBlockEntity pipe) {
        if (held.isEmpty()) {
            return InteractionResult.PASS;
        }
        IFluidHandlerItem handler = held.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null) {
            return InteractionResult.PASS;
        }
        FluidStack fluid = handler.getFluidInTank(0);
        if (!fluid.isEmpty() && !level.isClientSide) {
            pipe.setPipeFluid(ForgeRegistries.FLUIDS.getKey(fluid.getFluid()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static void overlayType(BlockEntity be, List<Component> lines) {
        if (!(be instanceof IFluidPipe pipe)) {
            return;
        }
        if (pipe.pipeFluid() == null) {
            lines.add(Component.literal("none"));
            return;
        }
        var fluid = ForgeRegistries.FLUIDS.getValue(pipe.pipeFluid());
        if (fluid == null) {
            lines.add(Component.literal(pipe.pipeFluid().toString()));
            return;
        }
        lines.add(new FluidStack(fluid, 1).getDisplayName());
    }

    public static long addCounter(long counter, int moved, boolean typed) {
        if (!typed || moved <= 0) {
            return counter;
        }
        return counter + moved;
    }
}
