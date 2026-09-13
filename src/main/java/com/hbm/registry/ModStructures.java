package com.hbm.registry;

import com.hbm.lib.RefStrings;
import com.hbm.world.gen.NbtNtmStructure;
import com.hbm.world.gen.NbtStructurePiece;
import com.hbm.world.gen.NtmGridPlacement;
import com.hbm.world.gen.component.CivilianFeatures;
import com.hbm.world.gen.component.OfficeFeatures;
import com.hbm.world.gen.component.SiloComponent;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, RefStrings.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, RefStrings.MODID);
    public static final DeferredRegister<StructurePlacementType<?>> PLACEMENT_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, RefStrings.MODID);

    public static final RegistryObject<StructureType<NbtNtmStructure>> NBT_STRUCTURE =
            STRUCTURE_TYPES.register("nbt_structure", () -> explicit(NbtNtmStructure.CODEC));
    public static final RegistryObject<StructurePieceType> NBT_PIECE =
            PIECE_TYPES.register("nbt_piece", () -> NbtStructurePiece::new);
    public static final RegistryObject<StructurePieceType> SILO_PIECE =
            PIECE_TYPES.register("silo", () -> SiloComponent::new);
    public static final RegistryObject<StructurePieceType> HOUSE_1_PIECE =
            PIECE_TYPES.register("house_1", () -> CivilianFeatures.NTMHouse1::new);
    public static final RegistryObject<StructurePieceType> HOUSE_2_PIECE =
            PIECE_TYPES.register("house_2", () -> CivilianFeatures.NTMHouse2::new);
    public static final RegistryObject<StructurePieceType> LAB_1_PIECE =
            PIECE_TYPES.register("lab_1", () -> CivilianFeatures.NTMLab1::new);
    public static final RegistryObject<StructurePieceType> LAB_2_PIECE =
            PIECE_TYPES.register("lab_2", () -> CivilianFeatures.NTMLab2::new);
    public static final RegistryObject<StructurePieceType> RURAL_HOUSE_PIECE =
            PIECE_TYPES.register("rural_house", () -> CivilianFeatures.RuralHouse1::new);
    public static final RegistryObject<StructurePieceType> OFFICE_PIECE =
            PIECE_TYPES.register("office", () -> OfficeFeatures.LargeOffice::new);
    public static final RegistryObject<StructurePieceType> OFFICE_CORNER_PIECE =
            PIECE_TYPES.register("office_corner", () -> OfficeFeatures.LargeOfficeCorner::new);
    public static final RegistryObject<StructurePlacementType<NtmGridPlacement>> NTM_GRID =
            PLACEMENT_TYPES.register("ntm_grid", () -> explicitPlacement(NtmGridPlacement.CODEC));

    private ModStructures() {
    }

    private static <S extends Structure> StructureType<S> explicit(Codec<S> codec) {
        return () -> codec;
    }

    private static <P extends StructurePlacement> StructurePlacementType<P> explicitPlacement(Codec<P> codec) {
        return () -> codec;
    }

    public static void register(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        PIECE_TYPES.register(modBus);
        PLACEMENT_TYPES.register(modBus);
    }
}
