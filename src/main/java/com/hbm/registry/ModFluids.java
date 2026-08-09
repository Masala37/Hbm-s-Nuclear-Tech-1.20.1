package com.hbm.registry;

import com.hbm.lib.RefStrings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fluid types, still/flowing fluids, optional liquid blocks, and buckets.
 */
public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, RefStrings.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, RefStrings.MODID);

    private static final List<FluidEntry> ENTRIES = new ArrayList<>();

    public static final FluidEntry COOLANT = registerLiquid(
            "coolant", 0xFFD8FCFF, 1000, 1000, 300, true);
    public static final FluidEntry COOLANT_HOT = registerLiquid(
            "coolant_hot", 0xFF99525E, 900, 800, 600, true);
    public static final FluidEntry WATER = registerLiquid(
            "water", 0xFF3F76E4, 1000, 1000, 300, true);
    public static final FluidEntry STEAM = registerGas(
            "steam", 0xFFE8E8E8, 373);
    public static final FluidEntry HOTSTEAM = registerGas(
            "hotsteam", 0xFFE0C8C8, 500);
    public static final FluidEntry ULTRAHOTSTEAM = registerGas(
            "ultrahotsteam", 0xFFFFB0B0, 1000);

    public static final FluidEntry OIL = registerLiquid(
            "oil", 0xFF020202, 900, 3000, 300, true);
    public static final FluidEntry HEAVYOIL = registerLiquid(
            "heavyoil", 0xFF141312, 950, 4000, 300, true);
    public static final FluidEntry LIGHTOIL = registerLiquid(
            "lightoil", 0xFF8C7451, 800, 1200, 300, true);
    public static final FluidEntry DIESEL = registerLiquid(
            "diesel", 0xFFF2EED5, 850, 1000, 300, true);
    public static final FluidEntry GASOLINE = registerLiquid(
            "gasoline", 0xFF445772, 750, 800, 300, true);
    public static final FluidEntry LUBRICANT = registerLiquid(
            "lubricant", 0xFF606060, 900, 2000, 300, true);
    public static final FluidEntry SULFURIC_ACID = registerLiquid(
            "sulfuric_acid", 0xFFB0AA64, 1800, 1500, 300, true);
    public static final FluidEntry NITRIC_ACID = registerLiquid(
            "nitric_acid", 0xFFBB7A1E, 1400, 1200, 300, true);
    public static final FluidEntry SOLVENT = registerLiquid(
            "solvent", 0xFFE4E3EF, 800, 900, 300, true);
    public static final FluidEntry PETROLEUM = registerGas(
            "petroleum", 0xFF7CB7C9, 300);

    private ModFluids() {
    }

    private static FluidEntry registerLiquid(String name, int tint, int density, int viscosity, int temperature, boolean placeable) {
        return register(name, tint, density, viscosity, temperature, false, placeable);
    }

    private static FluidEntry registerGas(String name, int tint, int temperature) {
        return register(name, tint, -1000, 200, temperature, true, false);
    }

    private static FluidEntry register(
            String name,
            int tint,
            int density,
            int viscosity,
            int temperature,
            boolean gaseous,
            boolean placeable) {
        ResourceLocation stillTex = new ResourceLocation(RefStrings.MODID, "fluid/" + name + "_still");
        ResourceLocation flowingTex = new ResourceLocation(RefStrings.MODID, "fluid/" + name + "_flowing");

        RegistryObject<FluidType> type = FLUID_TYPES.register(name, () -> new FluidType(
                FluidType.Properties.create()
                        .density(density)
                        .viscosity(viscosity)
                        .temperature(temperature)
                        .fallDistanceModifier(gaseous ? 1.0F : 0.0F)
                        .canExtinguish(!gaseous)
                        .supportsBoating(!gaseous)
                        .sound(SoundActions.BUCKET_FILL, net.minecraft.sounds.SoundEvents.BUCKET_FILL)
                        .sound(SoundActions.BUCKET_EMPTY, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)
                        .descriptionId("fluid.hbm." + name)) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return stillTex;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowingTex;
                    }

                    @Override
                    public int getTintColor() {
                        return tint;
                    }
                });
            }
        });

        // Forward-declared holders filled after suppliers exist.
        RegistryObject<FlowingFluid>[] sourceHolder = new RegistryObject[1];
        RegistryObject<FlowingFluid>[] flowingHolder = new RegistryObject[1];
        RegistryObject<LiquidBlock>[] blockHolder = new RegistryObject[1];
        RegistryObject<Item>[] bucketHolder = new RegistryObject[1];

        Supplier<ForgeFlowingFluid.Properties> props = () -> {
            ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(
                    type, sourceHolder[0], flowingHolder[0])
                    .slopeFindDistance(gaseous ? 2 : 3)
                    .levelDecreasePerBlock(gaseous ? 2 : 1)
                    .tickRate(gaseous ? 3 : 5);
            if (placeable && blockHolder[0] != null) {
                properties.block(blockHolder[0]);
            }
            if (bucketHolder[0] != null) {
                properties.bucket(bucketHolder[0]);
            }
            return properties;
        };

        sourceHolder[0] = FLUIDS.register(name, () -> new ForgeFlowingFluid.Source(props.get()));
        flowingHolder[0] = FLUIDS.register(name + "_flowing", () -> new ForgeFlowingFluid.Flowing(props.get()));

        if (placeable) {
            blockHolder[0] = ModBlocks.BLOCKS.register(name, () -> new LiquidBlock(
                    sourceHolder[0],
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WATER)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)));
        }

        bucketHolder[0] = ModItems.ITEMS.register(name + "_bucket",
                () -> new BucketItem(sourceHolder[0], new Item.Properties()
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1)));

        FluidEntry entry = new FluidEntry(name, type, sourceHolder[0], flowingHolder[0], blockHolder[0], bucketHolder[0]);
        ENTRIES.add(entry);
        return entry;
    }

    public static List<FluidEntry> entries() {
        return List.copyOf(ENTRIES);
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
    }

    public static final class FluidEntry {
        public final String name;
        public final RegistryObject<FluidType> type;
        public final RegistryObject<FlowingFluid> source;
        public final RegistryObject<FlowingFluid> flowing;
        public final RegistryObject<LiquidBlock> block;
        public final RegistryObject<Item> bucket;

        private FluidEntry(
                String name,
                RegistryObject<FluidType> type,
                RegistryObject<FlowingFluid> source,
                RegistryObject<FlowingFluid> flowing,
                RegistryObject<LiquidBlock> block,
                RegistryObject<Item> bucket) {
            this.name = name;
            this.type = type;
            this.source = source;
            this.flowing = flowing;
            this.block = block;
            this.bucket = bucket;
        }

        public boolean hasBlock() {
            return block != null;
        }
    }
}
