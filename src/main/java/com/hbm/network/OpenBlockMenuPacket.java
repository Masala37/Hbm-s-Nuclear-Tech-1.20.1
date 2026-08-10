package com.hbm.network;

import com.hbm.HbmNuclearTechMod;
import com.hbm.inventory.menu.HbmMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client asks server to open a block's MenuProvider.
 */
public final class OpenBlockMenuPacket {
    private final BlockPos pos;

    public OpenBlockMenuPacket(BlockPos pos) {
        this.pos = pos.immutable();
    }

    public static void encode(OpenBlockMenuPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
    }

    public static OpenBlockMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenBlockMenuPacket(buf.readBlockPos());
    }

    public static void handle(OpenBlockMenuPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                HbmNuclearTechMod.LOGGER.warn("OpenBlockMenuPacket with null sender");
                return;
            }
            Level level = player.level();
            BlockPos pos = packet.pos;
            HbmNuclearTechMod.LOGGER.info("Server OpenBlockMenuPacket from {} at {}", player.getGameProfile().getName(), pos);

            if (!level.isLoaded(pos)) {
                HbmNuclearTechMod.LOGGER.warn("OpenBlockMenuPacket: chunk not loaded at {}", pos);
                return;
            }
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
                HbmNuclearTechMod.LOGGER.warn("OpenBlockMenuPacket: too far from {}", pos);
                return;
            }

            BlockState state = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);
            MenuProvider provider = be instanceof MenuProvider menuBe ? menuBe : state.getMenuProvider(level, pos);
            if (provider == null) {
                HbmNuclearTechMod.LOGGER.warn("OpenBlockMenuPacket: no MenuProvider at {} (block={}, be={})",
                        pos, state.getBlock(), be == null ? "null" : be.getClass().getSimpleName());
                player.displayClientMessage(Component.literal("No GUI at " + pos.toShortString()), true);
                return;
            }

            HbmMenuHelper.open(player, provider, pos);
            HbmNuclearTechMod.LOGGER.info("Opened menu {} at {}", provider.getClass().getSimpleName(), pos);
        });
        ctx.setPacketHandled(true);
    }
}
