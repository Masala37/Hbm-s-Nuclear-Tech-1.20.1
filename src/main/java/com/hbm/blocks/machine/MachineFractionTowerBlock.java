package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blockentity.machine.FractionTowerBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.items.machine.InfiniteFluidBarrelItem;
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

import java.util.List;

/**
 * 1.7.10 {@code MachineFractionTower}: 3×3×3 Dummyable, extras on the four ground cardinals.
 */
public class MachineFractionTowerBlock extends BlockDummyable implements ILookOverlay {
    public static final int[] DIM = {2, 0, 1, 1, 1, 1};

    public MachineFractionTowerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
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
        return 1;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new FractionTowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (DummyableMeta.isCore(state.getValue(META))) {
            return newCoreEntity(pos, state);
        }
        if (DummyableMeta.isExtra(state.getValue(META))) {
            return new DummyableProxyBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        makeExtra(level, core.north());
        DummyableProxyBlockEntity.spawn(level, core.north());
        makeExtra(level, core.south());
        DummyableProxyBlockEntity.spawn(level, core.south());
        makeExtra(level, core.west());
        DummyableProxyBlockEntity.spawn(level, core.west());
        makeExtra(level, core.east());
        DummyableProxyBlockEntity.spawn(level, core.east());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_FRACTION_TOWER.get(), FractionTowerBlockEntity::tick);
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
        if (!(be instanceof FractionTowerBlockEntity tower)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        boolean stacked = level.getBlockEntity(core.below(FractionTowerBlockEntity.STACK_OFFSET))
                instanceof FractionTowerBlockEntity;
        Fluid fromItem = fluidFromHeld(held);
        if (held.is(ModItems.FLUID_IDENTIFIER.get())) {
            if (stacked) {
                player.sendSystemMessage(Component.literal("You can only change the type in the bottom segment!")
                        .withStyle(ChatFormatting.RED));
            } else {
                tower.setInputType(FractionRecipes.cycleNext(tower.getInputType()));
                player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                        .append(new FluidStack(tower.getInputType(), 1).getDisplayName())
                        .append(Component.literal("!")));
            }
            return InteractionResult.CONSUME;
        }
        if (fromItem != null && !stacked && fromItem != tower.getInputType()) {
            tower.setInputType(fromItem);
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(tower.getInputType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        }
        if (InfiniteFluidBarrelItem.interact(player, hand, tower.getFluidHandler(), tower.getInputType())) {
            tower.setChanged();
            level.sendBlockUpdated(core, level.getBlockState(core), level.getBlockState(core), 3);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void printHook(Level level, BlockPos pos, List<Component> lines) {
        BlockPos core = findCore(level, pos);
        if (core == null) {
            return;
        }
        BlockEntity be = level.getBlockEntity(core);
        if (!(be instanceof FractionTowerBlockEntity tower)) {
            return;
        }
        lines.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(name(tower.getInput().getFluid(), tower.getInputType())
                        + ": " + tower.getInput().getFluidAmount() + "/" + tower.getInput().getCapacity() + "mB")));
        FractionRecipes.Outputs recipe = FractionRecipes.get(tower.getInputType());
        Fluid leftType = recipe != null ? recipe.left() : null;
        Fluid rightType = recipe != null ? recipe.right() : null;
        lines.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(name(tower.getLeft().getFluid(), leftType)
                        + ": " + tower.getLeft().getFluidAmount() + "/" + tower.getLeft().getCapacity() + "mB")));
        lines.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(name(tower.getRight().getFluid(), rightType)
                        + ": " + tower.getRight().getFluidAmount() + "/" + tower.getRight().getCapacity() + "mB")));
    }

    private static @Nullable Fluid fluidFromHeld(ItemStack held) {
        return FluidUtil.getFluidContained(held)
                .filter(stack -> !stack.isEmpty() && FractionRecipes.get(stack.getFluid()) != null)
                .map(FluidStack::getFluid)
                .orElse(null);
    }

    private static String name(FluidStack stack, @Nullable Fluid fallback) {
        if (!stack.isEmpty()) {
            return stack.getDisplayName().getString();
        }
        if (fallback != null) {
            return new FluidStack(fallback, 1).getDisplayName().getString();
        }
        return "Empty";
    }
}
