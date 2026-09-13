package com.hbm.blocks.machine;

import com.hbm.blockentity.machine.CatalyticCrackerBlockEntity;
import com.hbm.blockentity.machine.DummyableProxyBlockEntity;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.DummyableMeta;
import com.hbm.blocks.ILookOverlay;
import com.hbm.fluid.SteamCycle;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.recipes.CrackingRecipes;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 1.7.10 {@code MachineCatalyticCracker}: Dummyable {@code {0,0,3,3,2,3}} plus four extra boxes.
 */
public class MachineCatalyticCrackerBlock extends BlockDummyable implements ILookOverlay {
    public static final int[] DIM = {0, 0, 3, 3, 2, 3};
    public static final int[] EXTRA_A = {8, -1, 3, -1, 2, 0};
    public static final int[] EXTRA_B = {13, 0, 0, 3, 2, 1};
    public static final int[] EXTRA_C = {14, -13, -1, 2, 1, 0};
    public static final int[] EXTRA_D = {3, -1, 2, 3, -1, 3};
    public static final int[][] PORTS = {
            {3, 1}, {3, -2}, {-3, 1}, {-3, -2},
            {2, 2}, {2, -3}, {-2, 2}, {-2, -3}
    };

    public MachineCatalyticCrackerBlock() {
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
        return 3;
    }

    @Override
    protected BlockEntity newCoreEntity(BlockPos pos, BlockState state) {
        return new CatalyticCrackerBlockEntity(pos, state);
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
    protected List<MultiblockHandlerXR.Cell> extraDummyCells(BlockPos core, int facing) {
        List<MultiblockHandlerXR.Cell> extra = new ArrayList<>();
        extra.addAll(MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), EXTRA_A, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), EXTRA_B, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), EXTRA_C, facing));
        extra.addAll(MultiblockHandlerXR.dummyCells(core.getX(), core.getY(), core.getZ(), EXTRA_D, facing));
        return extra;
    }

    @Override
    protected void fillExtras(Level level, BlockPos core, int facing) {
        int rot = DummyableMeta.rotateYClockwise(facing);
        int dx = DummyableMeta.offsetX(facing);
        int dz = DummyableMeta.offsetZ(facing);
        int rx = DummyableMeta.offsetX(rot);
        int rz = DummyableMeta.offsetZ(rot);
        for (int[] port : PORTS) {
            BlockPos at = core.offset(dx * port[0] + rx * port[1], 0, dz * port[0] + rz * port[1]);
            makeExtra(level, at);
            DummyableProxyBlockEntity.spawn(level, at);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!isCore(state) || level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.MACHINE_CATALYTIC_CRACKER.get(),
                CatalyticCrackerBlockEntity::tick);
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
        if (!(be instanceof CatalyticCrackerBlockEntity cracker)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        Fluid fromItem = fluidFromHeld(held);
        if (held.is(ModItems.FLUID_IDENTIFIER.get())) {
            cracker.setOilType(CrackingRecipes.cycleNext(cracker.getOilType()));
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(cracker.getOilType(), 1).getDisplayName())
                    .append(Component.literal("!")));
            return InteractionResult.CONSUME;
        }
        if (fromItem != null && fromItem != cracker.getOilType()) {
            cracker.setOilType(fromItem);
            player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(new FluidStack(cracker.getOilType(), 1).getDisplayName())
                    .append(Component.literal("!")));
        }
        if (InfiniteFluidBarrelItem.interact(player, hand, cracker.getFluidHandler(),
                cracker.getOilType(), SteamCycle.steam())) {
            cracker.setChanged();
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
        if (!(be instanceof CatalyticCrackerBlockEntity cracker)) {
            return;
        }
        CrackingRecipes.Outputs recipe = CrackingRecipes.get(cracker.getOilType());
        Fluid leftType = recipe != null ? recipe.left() : null;
        Fluid rightType = recipe != null ? recipe.right() : null;
        lines.add(in(name(cracker.getOil().getFluid(), cracker.getOilType())
                + ": " + cracker.getOil().getFluidAmount() + "/" + cracker.getOil().getCapacity() + "mB"));
        lines.add(in(name(cracker.getSteam().getFluid(), SteamCycle.steam())
                + ": " + cracker.getSteam().getFluidAmount() + "/" + cracker.getSteam().getCapacity() + "mB"));
        lines.add(out(name(cracker.getLeft().getFluid(), leftType)
                + ": " + cracker.getLeft().getFluidAmount() + "/" + cracker.getLeft().getCapacity() + "mB"));
        lines.add(out(name(cracker.getRight().getFluid(), rightType)
                + ": " + cracker.getRight().getFluidAmount() + "/" + cracker.getRight().getCapacity() + "mB"));
        lines.add(out(name(cracker.getSpent().getFluid(), SteamCycle.spentSteam())
                + ": " + cracker.getSpent().getFluidAmount() + "/" + cracker.getSpent().getCapacity() + "mB"));
    }

    private static Component in(String text) {
        return Component.literal("-> ").withStyle(ChatFormatting.GREEN).append(Component.literal(text));
    }

    private static Component out(String text) {
        return Component.literal("<- ").withStyle(ChatFormatting.RED).append(Component.literal(text));
    }

    private static @Nullable Fluid fluidFromHeld(ItemStack held) {
        return FluidUtil.getFluidContained(held)
                .filter(stack -> !stack.isEmpty() && CrackingRecipes.get(stack.getFluid()) != null)
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
