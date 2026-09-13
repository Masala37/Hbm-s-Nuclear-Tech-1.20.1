package com.hbm.registry;

import com.hbm.inventory.recipes.StackedSmeltingSerializer;
import com.hbm.lib.RefStrings;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, RefStrings.MODID);

    public static final RegistryObject<RecipeSerializer<SmeltingRecipe>> SMELTING =
            SERIALIZERS.register("smelting", StackedSmeltingSerializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
