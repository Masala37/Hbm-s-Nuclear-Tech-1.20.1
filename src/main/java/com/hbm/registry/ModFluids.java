package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Fluid types and fluids. Liquid blocks/buckets come later; tank FluidStacks work with these alone.
 */
public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, RefStrings.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, RefStrings.MODID);

    public static final RegistryObject<FluidType> COOLANT_TYPE = FLUID_TYPES.register("coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .density(1000)
                    .viscosity(1000)
                    .temperature(300)
                    .descriptionId("fluid.hbm.coolant")) {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private final ResourceLocation still =
                                new ResourceLocation(RefStrings.MODID, "fluid/coolant_still");
                        private final ResourceLocation flowing =
                                new ResourceLocation(RefStrings.MODID, "fluid/coolant_flowing");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return still;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return flowing;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFD8FCFF;
                        }
                    });
                }
            });

    public static final RegistryObject<FlowingFluid> COOLANT = FLUIDS.register("coolant",
            () -> new ForgeFlowingFluid.Source(coolantProperties()));

    public static final RegistryObject<FlowingFluid> COOLANT_FLOWING = FLUIDS.register("coolant_flowing",
            () -> new ForgeFlowingFluid.Flowing(coolantProperties()));

    private ModFluids() {
    }

    private static ForgeFlowingFluid.Properties coolantProperties() {
        return new ForgeFlowingFluid.Properties(COOLANT_TYPE, COOLANT, COOLANT_FLOWING)
                .slopeFindDistance(3)
                .levelDecreasePerBlock(1)
                .tickRate(5);
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }
}
