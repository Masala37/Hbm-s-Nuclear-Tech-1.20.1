package com.hbm.network;

import com.hbm.items.tool.DesignatorManualItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ItemDesignatorPacket {
    private static final int[] STEPS = {1, 5, 10, 50, 100};

    private final int operator;
    private final int value;
    private final int reference;

    public ItemDesignatorPacket(int operator, int value, int reference) {
        this.operator = operator;
        this.value = value;
        this.reference = reference;
    }

    public static void encode(ItemDesignatorPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.operator);
        buf.writeInt(packet.value);
        buf.writeInt(packet.reference);
    }

    public static ItemDesignatorPacket decode(FriendlyByteBuf buf) {
        return new ItemDesignatorPacket(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(ItemDesignatorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (packet.operator < 0 || packet.operator > 2 || packet.reference < 0 || packet.reference > 1) {
                return;
            }
            if (packet.operator != 2 && !allowedStep(packet.value)) {
                return;
            }
            ItemStack stack = DesignatorManualItem.held(player);
            if (stack.isEmpty()) {
                return;
            }
            DesignatorManualItem.apply(stack, packet.operator, packet.value, packet.reference,
                    (int) Math.round(player.getX()), (int) Math.round(player.getZ()));
            player.getInventory().setChanged();
        });
        ctx.setPacketHandled(true);
    }

    private static boolean allowedStep(int value) {
        for (int step : STEPS) {
            if (step == value) {
                return true;
            }
        }
        return false;
    }
}
