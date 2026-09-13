package com.hbm.network;

import com.hbm.inventory.menu.AnvilMenu;
import com.hbm.inventory.recipes.PlayerIngredientStore;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class AnvilCraftPacket {
    private final int recipeIndex;
    private final int mode;

    public AnvilCraftPacket(int recipeIndex, int mode) {
        this.recipeIndex = recipeIndex;
        this.mode = mode;
    }

    public static void encode(AnvilCraftPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.recipeIndex);
        buf.writeInt(packet.mode);
    }

    public static AnvilCraftPacket decode(FriendlyByteBuf buf) {
        return new AnvilCraftPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(AnvilCraftPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (packet.recipeIndex < 0 || packet.recipeIndex >= AnvilRecipes.getConstruction().size()) {
                return;
            }
            if (!(player.containerMenu instanceof AnvilMenu anvil)) {
                return;
            }
            AnvilConstructionRecipe recipe = AnvilRecipes.getConstruction().get(packet.recipeIndex);
            if (!recipe.isTierValid(anvil.tier)) {
                return;
            }
            int count = 1;
            if (packet.mode == 1) {
                if (recipe.output.size() > 1) {
                    count = 64;
                } else if (!recipe.output.isEmpty()) {
                    ItemStack out = recipe.output.get(0).resultStack();
                    if (!out.isEmpty()) {
                        count = Math.max(1, out.getMaxStackSize() / Math.max(1, out.getCount()));
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                if (PlayerIngredientStore.consume(player, recipe.input, true)) {
                    PlayerIngredientStore.give(player, recipe.output);
                } else {
                    break;
                }
            }
            player.containerMenu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }
}
