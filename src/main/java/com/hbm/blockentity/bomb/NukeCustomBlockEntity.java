package com.hbm.blockentity.bomb;

import com.hbm.blocks.bomb.NukeCustomYield;
import com.hbm.inventory.menu.NukeCustomMenu;
import com.hbm.registry.ModBlockEntities;
import com.hbm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Freeform 27-slot Custom Nuke — yields from registered explosives / fission / fusion packs.
 */
public class NukeCustomBlockEntity extends BlockEntity implements AssembledNuke, MenuProvider {
    public static final int SLOT_COUNT = 27;

    private float tnt;
    private float nuke;
    private float hydro;
    private float amat;
    private float dirty;
    private float schrab;
    private float euph;

    /** Pre-gate totals for GUI tooltips (materials present but stage not unlocked). */
    private float nukeRaw;
    private float hydroRaw;
    private float amatRaw;
    private float schrabRaw;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            recalculate();
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> itemOptional = LazyOptional.of(() -> items);

    public NukeCustomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_CUSTOM.get(), pos, state);
    }

    public void recalculate() {
        float tntAdd = 0.0F, tntMod = 1.0F;
        float nukeAdd = 0.0F, nukeMod = 1.0F;
        float hydroAdd = 0.0F, hydroMod = 1.0F;
        float amatAdd = 0.0F, amatMod = 1.0F;
        float dirtyAdd = 0.0F, dirtyMod = 1.0F;
        float schrabAdd = 0.0F, schrabMod = 1.0F;
        float euphAdd = 0.0F;

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            NukeCustomEntries.Entry ent = NukeCustomEntries.get(stack.getItem());
            if (ent == null) {
                continue;
            }
            float amount = ent.value() * stack.getCount();
            if (ent.entry() == NukeCustomEntries.EntryType.ADD) {
                switch (ent.type()) {
                    case TNT -> tntAdd += amount;
                    case NUKE -> nukeAdd += amount;
                    case HYDRO -> hydroAdd += amount;
                    case AMAT -> amatAdd += amount;
                    case DIRTY -> dirtyAdd += amount;
                    case SCHRAB -> schrabAdd += amount;
                    case EUPH -> euphAdd += amount;
                }
            } else {
                float mult = ent.value() * stack.getCount();
                switch (ent.type()) {
                    case TNT -> tntMod *= mult;
                    case NUKE -> nukeMod *= mult;
                    case HYDRO -> hydroMod *= mult;
                    case AMAT -> amatMod *= mult;
                    case DIRTY -> dirtyMod *= mult;
                    case SCHRAB -> schrabMod *= mult;
                    default -> {
                    }
                }
            }
        }

        tntAdd *= tntMod;
        nukeAdd *= nukeMod;
        hydroAdd *= hydroMod;
        amatAdd *= amatMod;
        dirtyAdd *= dirtyMod;
        schrabAdd *= schrabMod;

        this.nukeRaw = nukeAdd;
        this.hydroRaw = hydroAdd;
        this.amatRaw = amatAdd;
        this.schrabRaw = schrabAdd;

        if (tntAdd < 16.0F) {
            nukeAdd = 0.0F;
        }
        if (nukeAdd < 100.0F) {
            hydroAdd = 0.0F;
        }
        if (nukeAdd < 50.0F) {
            amatAdd = 0.0F;
            schrabAdd = 0.0F;
        }
        if (schrabAdd == 0.0F) {
            euphAdd = 0.0F;
        }

        this.tnt = tntAdd;
        this.nuke = nukeAdd;
        this.hydro = hydroAdd;
        this.amat = amatAdd;
        this.dirty = dirtyAdd;
        this.schrab = schrabAdd;
        this.euph = euphAdd;
    }

    public float getTnt() {
        return tnt;
    }

    public float getNuke() {
        return nuke;
    }

    public float getHydro() {
        return hydro;
    }

    public float getAmat() {
        return amat;
    }

    public float getDirty() {
        return dirty;
    }

    public float getSchrab() {
        return schrab;
    }

    public float getEuph() {
        return euph;
    }

    public float getNukeRaw() {
        return nukeRaw;
    }

    public float getHydroRaw() {
        return hydroRaw;
    }

    public float getAmatRaw() {
        return amatRaw;
    }

    public float getSchrabRaw() {
        return schrabRaw;
    }

    public float getNukeAdj() {
        if (nuke == 0.0F) {
            return 0.0F;
        }
        return Math.min(nuke + tnt / 2.0F, NukeCustomYield.MAX_NUKE);
    }

    public float getHydroAdj() {
        if (hydro == 0.0F) {
            return 0.0F;
        }
        return Math.min(hydro + nuke / 2.0F + tnt / 4.0F, NukeCustomYield.MAX_HYDRO);
    }

    public float getAmatAdj() {
        if (amat == 0.0F) {
            return 0.0F;
        }
        return Math.min(amat + hydro / 2.0F + nuke / 4.0F + tnt / 8.0F, NukeCustomYield.MAX_AMAT);
    }

    public float getSchrabAdj() {
        if (schrab == 0.0F) {
            return 0.0F;
        }
        return Math.min(schrab + amat / 2.0F + hydro / 4.0F + nuke / 8.0F + tnt / 16.0F, NukeCustomYield.MAX_SCHRAB);
    }

    /** True when inventory contains custom_fall — detonation spawns EntityFallingNuke. */
    public boolean isFalling() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (items.getStackInSlot(i).is(ModItems.CUSTOM_FALL.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isReady() {
        recalculate();
        return tnt > 0.0F || nuke > 0.0F || hydro > 0.0F || amat > 0.0F
                || schrab > 0.0F || euph > 0.0F;
    }

    @Override
    public int resolveBlastRadius(int configuredRadius) {
        recalculate();
        if (euph > 0.0F) {
            return 150;
        }
        if (schrab > 0.0F) {
            return Math.max(1, (int) getSchrabAdj());
        }
        if (amat > 0.0F) {
            return Math.max(1, (int) getAmatAdj());
        }
        if (hydro > 0.0F) {
            return Math.max(1, (int) getHydroAdj());
        }
        if (nuke > 0.0F) {
            return Math.max(1, (int) getNukeAdj());
        }
        if (tnt > 0.0F) {
            return Math.max(1, (int) Math.min(tnt, NukeCustomYield.MAX_TNT));
        }
        return configuredRadius;
    }

    @Override
    public void clearSlots() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        recalculate();
    }

    @Override
    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropContents(level, worldPosition, new net.minecraft.world.SimpleContainer(copyStacks()));
            clearSlots();
        }
    }

    @Override
    public Component statusMessage() {
        return Component.translatable(isReady() ? "block.hbm.nuke_custom.ready" : "block.hbm.nuke_custom.incomplete");
    }

    @Override
    public int findInsertSlot(Item item) {
        if (!NukeCustomEntries.isRegistered(item) && item != ModItems.CUSTOM_FALL.get()) {
            return -1;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (items.getStackInSlot(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public ItemStack[] copyStacks() {
        ItemStack[] copy = new ItemStack[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            copy[i] = items.getStackInSlot(i).copy();
        }
        return copy;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.hbm.nuke_custom");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NukeCustomMenu(id, inv, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Items")) {
            items.deserializeNBT(tag.getCompound("Items"));
        }
        recalculate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }
}
