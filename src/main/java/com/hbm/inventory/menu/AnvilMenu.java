package com.hbm.inventory.menu;

import com.hbm.blocks.machine.NtmAnvilBlock;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilSmithingRecipe;
import com.hbm.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AnvilMenu extends AbstractContainerMenu {
    private final Container input = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            updateSmithing();
        }
    };
    private final ResultContainer output = new ResultContainer();
    private final BlockPos pos;
    public final int tier;

    public AnvilMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
    }

    public AnvilMenu(int id, Inventory inv, BlockPos pos) {
        this(id, inv, pos, NtmAnvilBlock.tierAt(inv.player.level(), pos));
    }

    public AnvilMenu(int id, Inventory inv, BlockPos pos, int tier) {
        super(ModMenus.ANVIL.get(), id);
        this.pos = pos;
        this.tier = tier;

        this.addSlot(new Slot(input, 0, 17, 27) {
            @Override
            public void setChanged() {
                super.setChanged();
                updateSmithing();
            }
        });
        this.addSlot(new Slot(input, 1, 53, 27) {
            @Override
            public void setChanged() {
                super.setChanged();
                updateSmithing();
            }
        });
        this.addSlot(new Slot(output, 0, 89, 27) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                ItemStack left = input.getItem(0);
                ItemStack right = input.getItem(1);
                for (AnvilSmithingRecipe rec : AnvilRecipes.getSmithing()) {
                    int match = rec.matchesInt(left, right);
                    if (match != -1 && rec.tier <= AnvilMenu.this.tier) {
                        input.removeItem(0, rec.amountConsumed(0, match == 1));
                        input.removeItem(1, rec.amountConsumed(1, match == 1));
                        updateSmithing();
                        super.onTake(player, stack);
                        return;
                    }
                }
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18 + 56));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 142 + 56));
        }
        updateSmithing();
    }

    public BlockPos getPos() {
        return pos;
    }

    private void updateSmithing() {
        ItemStack left = input.getItem(0);
        ItemStack right = input.getItem(1);
        if (left.isEmpty() || right.isEmpty()) {
            output.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }
        for (AnvilSmithingRecipe rec : AnvilRecipes.getSmithing()) {
            if (rec.matches(left, right) && rec.tier <= tier) {
                output.setItem(0, rec.getOutput());
                broadcastChanges();
                return;
            }
        }
        output.setItem(0, ItemStack.EMPTY);
        broadcastChanges();
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            clearContainer(player, input);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (!(player.level().getBlockState(pos).getBlock() instanceof NtmAnvilBlock)) {
            return player.level().isClientSide;
        }
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 2) {
                if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else if (index <= 1) {
                if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }
}
