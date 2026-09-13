package com.hbm.network;

import com.hbm.blockentity.machine.AssemblyMachineBlockEntity;
import com.hbm.blockentity.machine.ChemicalPlantBlockEntity;
import com.hbm.blockentity.machine.PurexBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server 1.7 {@code NBTControlPacket} recipe selection for the assembly machine.
 */
public final class AssemblyRecipePacket {
    private final BlockPos pos;
    private final int index;
    private final String selection;

    public AssemblyRecipePacket(BlockPos pos, int index, String selection) {
        this.pos = pos;
        this.index = index;
        this.selection = selection == null ? "null" : selection;
    }

    public static void encode(AssemblyRecipePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeVarInt(packet.index);
        buf.writeUtf(packet.selection);
    }

    public static AssemblyRecipePacket decode(FriendlyByteBuf buf) {
        return new AssemblyRecipePacket(buf.readBlockPos(), buf.readVarInt(), buf.readUtf());
    }

    public static void handle(AssemblyRecipePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.level() == null || !player.level().hasChunkAt(packet.pos)) {
                return;
            }
            if (player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D) {
                return;
            }
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof AssemblyMachineBlockEntity assembler && packet.index == 0) {
                assembler.setRecipe(packet.selection);
            }
            if (be instanceof ChemicalPlantBlockEntity plant && packet.index == 0) {
                plant.setRecipe(packet.selection);
            }
            if (be instanceof PurexBlockEntity purex && packet.index == 0) {
                purex.setRecipe(packet.selection);
            }
        });
        ctx.setPacketHandled(true);
    }
}
