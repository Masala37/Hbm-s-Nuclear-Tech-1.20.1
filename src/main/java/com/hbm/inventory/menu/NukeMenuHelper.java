package com.hbm.inventory.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

/**
 * Shared helpers for menus that read a BlockPos from the open-screen buffer.
 */
public final class NukeMenuHelper {
    private NukeMenuHelper() {
    }

    public static <T extends BlockEntity> T readBlockEntity(Inventory inv, FriendlyByteBuf buf, Class<T> type,
                                                            BiFunction<BlockPos, BlockState, T> factory) {
        return HbmMenuHelper.resolve(inv, buf, type, factory);
    }

    /** @deprecated use {@link #readBlockEntity(Inventory, FriendlyByteBuf, Class, BiFunction)} */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T readBlockEntity(Inventory inv, FriendlyByteBuf buf, Class<T> type) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (type.isInstance(be)) {
            return (T) be;
        }
        // Last-resort: re-read without factory (legacy call sites). Prefer factory overload.
        throw new IllegalStateException("Expected " + type.getSimpleName() + " at " + pos
                + " but got " + (be == null ? "null" : be.getClass().getSimpleName())
                + " — update menu to pass a BE factory");
    }
}
