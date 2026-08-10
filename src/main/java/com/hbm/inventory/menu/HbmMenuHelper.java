package com.hbm.inventory.menu;

import com.hbm.HbmNuclearTechMod;
import com.hbm.network.ModMessages;
import com.hbm.network.OpenMenuS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Opens menus via our own S2C packet (main thread). Avoids Forge 1.20.1 OpenContainer
 * network-thread failures that open the server menu but never show a client screen.
 */
public final class HbmMenuHelper {
    private HbmMenuHelper() {
    }

    public static void open(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        try {
            player.closeContainer();
            player.nextContainerCounter();
            int containerId = player.containerCounter;

            AbstractContainerMenu menu = provider.createMenu(containerId, player.getInventory(), player);
            if (menu == null) {
                HbmNuclearTechMod.LOGGER.error("createMenu returned null for {} at {}",
                        provider.getClass().getSimpleName(), pos);
                return;
            }

            ResourceLocation typeId = ForgeRegistries.MENU_TYPES.getKey(menu.getType());
            if (typeId == null) {
                HbmNuclearTechMod.LOGGER.error("Menu type not registered for {}", menu.getClass().getSimpleName());
                return;
            }

            Component title = provider.getDisplayName();
            player.containerMenu = menu;
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, menu));

            // Open on client BEFORE slot sync so the client menu exists to receive updates.
            ModMessages.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new OpenMenuS2CPacket(containerId, typeId, title, pos));
            player.initMenu(menu);

            HbmNuclearTechMod.LOGGER.info("Sent OpenMenuS2C {} id={} at {}",
                    typeId, containerId, pos);
        } catch (Exception e) {
            HbmNuclearTechMod.LOGGER.error("Failed to open menu {} at {}",
                    provider.getClass().getSimpleName(), pos, e);
        }
    }

    public static void open(ServerPlayer player, BlockEntity be) {
        if (be instanceof MenuProvider provider) {
            open(player, provider, be.getBlockPos());
        } else {
            HbmNuclearTechMod.LOGGER.warn("Block entity {} at {} is not a MenuProvider",
                    be == null ? "null" : be.getClass().getSimpleName(),
                    be == null ? BlockPos.ZERO : be.getBlockPos());
        }
    }

    /**
     * Resolve BE for client menu factory. Never throws — falls back to a fresh unbound instance
     * so the screen can open even if the client BE packet is late.
     */
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T resolve(Inventory inv, FriendlyByteBuf buf, Class<T> type,
                                                    BiFunction<BlockPos, BlockState, T> factory) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity existing = inv.player.level().getBlockEntity(pos);
        if (type.isInstance(existing)) {
            return (T) existing;
        }

        HbmNuclearTechMod.LOGGER.warn("Client menu expected {} at {} but got {} — using stub",
                type.getSimpleName(), pos,
                existing == null ? "null" : existing.getClass().getSimpleName());

        BlockState state = inv.player.level().getBlockState(pos);
        T stub = factory.apply(pos, state);
        stub.setLevel(inv.player.level());
        return stub;
    }

    public static boolean stillValid(Player player, @Nullable BlockEntity be, BlockPos pos) {
        return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public static boolean stillValid(Player player, BlockEntity be) {
        return be != null && stillValid(player, be, be.getBlockPos());
    }
}
