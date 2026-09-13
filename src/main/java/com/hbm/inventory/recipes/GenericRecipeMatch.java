package com.hbm.inventory.recipes;

import com.hbm.items.machine.ItemBatteryPack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Matches and applies 1.7 {@code GenericRecipe} dump rows. Does not invent recipes.
 * Item meta is mapped onto a sibling id when that item exists ({@code hbm:circuit} + BASIC
 * → {@code hbm:circuit_basic}).
 */
public final class GenericRecipeMatch {
    private GenericRecipeMatch() {
    }

    public static IngredientRef toRef(GenericMachineRecipe.ItemInput input) {
        String item = mappedItemId(input);
        return new IngredientRef(input.ore(), item, input.count(), input.chance(), java.util.List.of());
    }

    public static String mappedItemId(GenericMachineRecipe.ItemInput input) {
        String item = portId(input.item());
        String meta = input.meta();
        if (item == null || item.isEmpty() || meta == null || meta.isEmpty()) {
            return item;
        }
        if ("hbm:battery_pack".equals(item)) {
            return item;
        }
        String suffix = meta.toLowerCase(Locale.ROOT);
        int colon = item.indexOf(':');
        String mapped = colon >= 0
                ? item.substring(0, colon + 1) + item.substring(colon + 1) + "_" + suffix
                : item + "_" + suffix;
        ResourceLocation id = ResourceLocation.tryParse(mapped);
        if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
            return mapped;
        }
        return item;
    }

    /**
     * 1.7 dump ids that this port registers under a different path.
     */
    public static String portId(String item) {
        if (item == null) {
            return null;
        }
        return switch (item) {
            case "hbm:machine_diesel" -> "hbm:diesel_generator";
            case "hbm:machine_combustion_engine" -> "hbm:combustion_generator";
            case "hbm:machine_electric_furnace_off" -> "hbm:electric_furnace";
            default -> item;
        };
    }

    public static boolean exists(GenericMachineRecipe recipe) {
        for (GenericMachineRecipe.ItemInput input : recipe.inputItem()) {
            if (!toRef(input).existsInRegistry()) {
                return false;
            }
        }
        for (GenericMachineRecipe.ItemInput output : recipe.outputItem()) {
            if (!toRef(output).existsInRegistry()) {
                return false;
            }
        }
        for (GenericMachineRecipe.FluidInput input : recipe.inputFluid()) {
            if (fluid(input.fluid()) == null) {
                return false;
            }
        }
        for (GenericMachineRecipe.FluidInput output : recipe.outputFluid()) {
            if (fluid(output.fluid()) == null) {
                return false;
            }
        }
        return true;
    }

    public static boolean matchesItem(GenericMachineRecipe.ItemInput input, ItemStack stack, boolean ignoreCount) {
        if (!toRef(input).matches(stack, ignoreCount)) {
            return false;
        }
        String pack = batteryPackType(input);
        if (pack == null) {
            return true;
        }
        return stack.getItem() instanceof ItemBatteryPack
                && ItemBatteryPack.getType(stack) == ItemBatteryPack.Pack.byName(pack);
    }

    public static ItemStack result(GenericMachineRecipe.ItemInput output) {
        ItemStack stack = toRef(output).resultStack();
        String pack = batteryPackType(output);
        if (pack != null && !stack.isEmpty()) {
            ItemBatteryPack.setType(stack, ItemBatteryPack.Pack.byName(pack));
        }
        return stack;
    }

    private static String batteryPackType(GenericMachineRecipe.ItemInput input) {
        if (input == null || input.meta() == null || input.meta().isEmpty()) {
            return null;
        }
        if (!"hbm:battery_pack".equals(portId(input.item()))) {
            return null;
        }
        return input.meta();
    }

    public static ItemStack icon(GenericMachineRecipe recipe) {
        if (recipe == null) {
            return ItemStack.EMPTY;
        }
        if (!recipe.outputItem().isEmpty()) {
            return result(recipe.outputItem().get(0));
        }
        return new ItemStack(Items.BUCKET);
    }

    public static List<Component> tooltip(GenericMachineRecipe recipe) {
        List<Component> lines = new ArrayList<>();
        if (recipe == null) {
            return lines;
        }
        ItemStack icon = icon(recipe);
        String title = recipe.named()
                ? Component.translatable(recipe.name()).getString()
                : (icon.isEmpty() ? recipe.name() : icon.getHoverName().getString());
        lines.add(Component.literal(title).withStyle(ChatFormatting.YELLOW));
        if (recipe.duration() > 0) {
            lines.add(Component.translatable("gui.recipe.duration")
                    .append(": " + (recipe.duration() / 20.0D) + "s")
                    .withStyle(ChatFormatting.RED));
        }
        if (recipe.power() > 0) {
            lines.add(Component.translatable("gui.recipe.consumption")
                    .append(": " + recipe.power() + " FE/t")
                    .withStyle(ChatFormatting.RED));
        }
        lines.add(Component.translatable("gui.recipe.input").withStyle(ChatFormatting.BOLD));
        for (GenericMachineRecipe.ItemInput input : recipe.inputItem()) {
            ItemStack display = result(input);
            String name = display.isEmpty()
                    ? (input.ore() != null ? input.ore() : String.valueOf(input.item()))
                    : display.getHoverName().getString();
            lines.add(Component.literal("  " + input.count() + "x " + name).withStyle(ChatFormatting.GRAY));
        }
        for (GenericMachineRecipe.FluidInput input : recipe.inputFluid()) {
            lines.add(Component.literal("  " + input.amount() + "mB " + input.fluid()).withStyle(ChatFormatting.BLUE));
        }
        lines.add(Component.translatable("gui.recipe.output").withStyle(ChatFormatting.BOLD));
        for (GenericMachineRecipe.ItemInput output : recipe.outputItem()) {
            ItemStack display = result(output);
            String name = display.isEmpty()
                    ? (output.ore() != null ? output.ore() : String.valueOf(output.item()))
                    : display.getHoverName().getString();
            String chance = output.chance() > 0.0F && output.chance() < 1.0F
                    ? " (" + Math.round(output.chance() * 100.0F) + "%)"
                    : "";
            lines.add(Component.literal("  " + output.count() + "x " + name + chance).withStyle(ChatFormatting.GRAY));
        }
        for (GenericMachineRecipe.FluidInput output : recipe.outputFluid()) {
            lines.add(Component.literal("  " + output.amount() + "mB " + output.fluid()).withStyle(ChatFormatting.BLUE));
        }
        return lines;
    }

    public static Fluid fluid(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        ResourceLocation key = ResourceLocation.tryParse(id.toLowerCase(Locale.ROOT));
        if (key == null || !ForgeRegistries.FLUIDS.containsKey(key)) {
            return null;
        }
        return ForgeRegistries.FLUIDS.getValue(key);
    }

    public static boolean hasItems(GenericMachineRecipe recipe, IItemHandlerModifiable items, int[] inputSlots) {
        for (int i = 0; i < recipe.inputItem().size() && i < inputSlots.length; i++) {
            if (!matchesItem(recipe.inputItem().get(i), items.getStackInSlot(inputSlots[i]), false)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canFitOutput(GenericMachineRecipe recipe, IItemHandlerModifiable items, int outputSlot) {
        return canFitOutputs(recipe, items, new int[]{outputSlot});
    }

    public static boolean canFitOutputs(GenericMachineRecipe recipe, IItemHandlerModifiable items, int[] outputSlots) {
        for (int i = 0; i < recipe.outputItem().size() && i < outputSlots.length; i++) {
            ItemStack produced = result(recipe.outputItem().get(i));
            if (produced.isEmpty()) {
                return false;
            }
            ItemStack existing = items.getStackInSlot(outputSlots[i]);
            if (existing.isEmpty()) {
                continue;
            }
            if (!ItemStack.isSameItemSameTags(existing, produced)
                    || existing.getCount() + produced.getCount() > existing.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasFluids(GenericMachineRecipe recipe, FluidTank inputTank) {
        return hasFluids(recipe, new FluidTank[]{inputTank});
    }

    public static boolean hasFluids(GenericMachineRecipe recipe, FluidTank[] inputTanks) {
        for (int i = 0; i < recipe.inputFluid().size() && i < inputTanks.length; i++) {
            GenericMachineRecipe.FluidInput need = recipe.inputFluid().get(i);
            Fluid fluid = fluid(need.fluid());
            if (fluid == null) {
                return false;
            }
            FluidStack stored = inputTanks[i].getFluid();
            if (stored.getFluid() != fluid || stored.getAmount() < need.amount()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canFitFluidOutput(GenericMachineRecipe recipe, FluidTank outputTank) {
        return canFitFluidOutputs(recipe, new FluidTank[]{outputTank});
    }

    public static boolean canFitFluidOutputs(GenericMachineRecipe recipe, FluidTank[] outputTanks) {
        for (int i = 0; i < recipe.outputFluid().size() && i < outputTanks.length; i++) {
            GenericMachineRecipe.FluidInput make = recipe.outputFluid().get(i);
            Fluid fluid = fluid(make.fluid());
            if (fluid == null) {
                return false;
            }
            FluidTank tank = outputTanks[i];
            if (tank.getFluid().isEmpty()) {
                if (make.amount() > tank.getCapacity()) {
                    return false;
                }
                continue;
            }
            if (tank.getFluid().getFluid() != fluid
                    || tank.getFluid().getAmount() + make.amount() > tank.getCapacity()) {
                return false;
            }
        }
        return true;
    }

    public static void consumeItems(GenericMachineRecipe recipe, IItemHandlerModifiable items, int[] inputSlots) {
        for (int i = 0; i < recipe.inputItem().size() && i < inputSlots.length; i++) {
            ItemStack stack = items.getStackInSlot(inputSlots[i]);
            stack.shrink(recipe.inputItem().get(i).count());
            if (stack.isEmpty()) {
                items.setStackInSlot(inputSlots[i], ItemStack.EMPTY);
            }
        }
    }

    public static void produceItem(GenericMachineRecipe recipe, IItemHandlerModifiable items, int outputSlot) {
        produceItems(recipe, items, new int[]{outputSlot});
    }

    public static void produceItems(GenericMachineRecipe recipe, IItemHandlerModifiable items, int[] outputSlots) {
        for (int i = 0; i < recipe.outputItem().size() && i < outputSlots.length; i++) {
            GenericMachineRecipe.ItemInput output = recipe.outputItem().get(i);
            if (output.chance() > 0.0F && output.chance() < 1.0F
                    && ThreadLocalRandom.current().nextFloat() >= output.chance()) {
                continue;
            }
            ItemStack produced = result(output);
            if (produced.isEmpty()) {
                continue;
            }
            int slot = outputSlots[i];
            ItemStack existing = items.getStackInSlot(slot);
            if (existing.isEmpty()) {
                items.setStackInSlot(slot, produced);
            } else {
                existing.grow(produced.getCount());
            }
        }
    }

    public static void consumeFluid(GenericMachineRecipe recipe, FluidTank inputTank) {
        consumeFluids(recipe, new FluidTank[]{inputTank});
    }

    public static void consumeFluids(GenericMachineRecipe recipe, FluidTank[] inputTanks) {
        for (int i = 0; i < recipe.inputFluid().size() && i < inputTanks.length; i++) {
            inputTanks[i].drain(recipe.inputFluid().get(i).amount(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public static void produceFluid(GenericMachineRecipe recipe, FluidTank outputTank) {
        produceFluids(recipe, new FluidTank[]{outputTank});
    }

    public static void produceFluids(GenericMachineRecipe recipe, FluidTank[] outputTanks) {
        for (int i = 0; i < recipe.outputFluid().size() && i < outputTanks.length; i++) {
            Fluid fluid = fluid(recipe.outputFluid().get(i).fluid());
            if (fluid == null) {
                continue;
            }
            outputTanks[i].fill(new FluidStack(fluid, recipe.outputFluid().get(i).amount()),
                    IFluidHandler.FluidAction.EXECUTE);
        }
    }
}
