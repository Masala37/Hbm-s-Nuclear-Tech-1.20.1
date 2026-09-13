package com.hbm.world.gen.component;

import java.util.Random;

import com.hbm.blockentity.machine.DecoLootBlockEntity;
import com.hbm.inventory.loot.StructureLoot;
import com.hbm.registry.ModStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/* Described as "Civilian", as that's the overarching connection between all of these structures. Unlike the ruins, there's not enough to
 * compartmentalize even further. Just in general many of the structures I consider lower-quality (except for the sandstone houses; those are actually pretty nice).
 */
public class CivilianFeatures {

	/** 1.7 {@code LootGenerator.lootX}: deco loot pile with a pile pool. */
	private static void placeLootPile(Component piece, WorldGenLevel world, BoundingBox box, Random rand, String pool,
			int featureX, int featureY, int featureZ) {
		piece.placeBlockAtCurrentPosition(world, "hbm:tile.deco_loot", 0, featureX, featureY, featureZ, box);

		int posX = piece.getXWithOffset(featureX, featureZ);
		int posY = piece.getYWithOffset(featureY);
		int posZ = piece.getZWithOffset(featureX, featureZ);

		if(!piece.inside(box, posX, posY, posZ)) return;

		if(world.getBlockEntity(new BlockPos(posX, posY, posZ)) instanceof DecoLootBlockEntity loot)
			loot.setItems(StructureLoot.decoLoot(pool, RandomSource.create(rand.nextLong())));
	}

	/** Sandstone Ruin 1 */
	public static class NTMHouse1 extends Component {

		private boolean hasPlacedChest;

		private static Sandstone RandomSandstone = new Sandstone();

		public NTMHouse1(StructurePieceSerializationContext context, CompoundTag tag) {
			super(ModStructures.HOUSE_1_PIECE.get(), tag);
			this.hasPlacedChest = tag.getBoolean("hasChest");
		}

		/** Constructor for this feature; takes coordinates for bounding box */
		public NTMHouse1(Random rand, int minX, int minZ) {
			super(ModStructures.HOUSE_1_PIECE.get(), rand, minX, 64, minZ, 9, 4, 6);
			this.hasPlacedChest = false;
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			super.addAdditionalSaveData(context, tag);
			tag.putBoolean("hasChest", this.hasPlacedChest);
		}

		@Override
		protected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {
			if(!this.setAverageHeight(world, box, this.boundingBox.minY())) {
				return false;
			}

			placeFoundationUnderneath(world, "minecraft:sandstone", 0, 0, 0, 9, 6, -1, box);

			//Walls
			this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 9, 0, 0, false, rand, RandomSandstone); //Back Wall
			this.fillWithRandomizedBlocks(world, box, 0, 1, 0, 1, 1, 0, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:oak_fence", 0, 2, 1, 0, box);
			this.fillWithRandomizedBlocks(world, box, 3, 1, 0, 5, 1, 0, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:oak_fence", 0, 6, 1, 0, box);
			this.placeBlockAtCurrentPosition(world, "minecraft:oak_fence", 0, 7, 1, 0, box);
			this.fillWithRandomizedBlocks(world, box, 9 - 1, 1, 0, 9, 1, 0, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 0, 2, 0, 9 - 2, 2, 0, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 0, 1, 6, false, rand, RandomSandstone); //Left Wall
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 0, 2, 1, box);
			this.fillWithMetadataBlocks(world, box, 0, 2, 3, 0, 2, 6, "minecraft:sandstone_slab", 0);
			this.fillWithRandomizedBlocks(world, box, 1, 0, 6, 1, 1, 6, false, rand, RandomSandstone); //Front Wall
			this.fillWithRandomizedBlocks(world, box, 3, 0, 6, 9, 1, 6, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 1, 2, 6, 3, 2, 6, false, rand, RandomSandstone);
			this.fillWithMetadataBlocks(world, box, 4, 2, 6, 5, 2, 6, "minecraft:sandstone_slab", 0);
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 9 - 2, 2, 6, box);
			this.fillWithRandomizedBlocks(world, box, 9, 0, 0, 9, 0, 6, false, rand, RandomSandstone); //Right Wall
			this.randomlyFillWithBlocks(world, box, rand, 0.65F, 9, 1, 1, 9, 1, 6 - 1, "minecraft:sand");

			this.fillWithRandomizedBlocks(world, box, 4, 0, 1, 4, 1, 3, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.reinforced_sand", 0, 4, 0, 4, box);

			//Loot/Sand
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate_weapon", 0, 1, 0, 1, box);
			if(!this.hasPlacedChest)
				this.hasPlacedChest = this.generateInvContents(world, box, rand, "minecraft:chest", 3, 0, 1, "POOL_GENERIC", rand.nextInt(2) + 8);
			this.fillWithBlocks(world, box, 5, 0, 1, 6, 0, 1, "hbm:tile.crate");
			this.placeBlockAtCurrentPosition(world, "minecraft:sand", 0, 7, 0, 1, box);
			if(rand.nextFloat() <= 0.25)
				 this.placeBlockAtCurrentPosition(world, "hbm:tile.crate_metal", 0, 9 - 1, 0, 1, box);
			this.randomlyFillWithBlocks(world, box, rand, 0.25F, 1, 0, 2, 3, 0, 6 - 1, "minecraft:sand");
			this.randomlyFillWithBlocks(world, box, rand, 0.25F, 5, 0, 2, 9 - 1, 0, 6 - 1, "minecraft:sand");

			return true;
		}

	}

	public static class NTMHouse2 extends Component {

		private static Sandstone RandomSandstone = new Sandstone();

		private boolean[] hasPlacedLoot = new boolean[2];

		public NTMHouse2(StructurePieceSerializationContext context, CompoundTag tag) {
			super(ModStructures.HOUSE_2_PIECE.get(), tag);
			this.hasPlacedLoot[0] = tag.getBoolean("hasLoot1");
			this.hasPlacedLoot[1] = tag.getBoolean("hasLoot2");
		}

		public NTMHouse2(Random rand, int minX, int minZ) {
			super(ModStructures.HOUSE_2_PIECE.get(), rand, minX, 64, minZ, 15, 5, 9);
			this.hasPlacedLoot[0] = false;
			this.hasPlacedLoot[1] = false;
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			super.addAdditionalSaveData(context, tag);
			tag.putBoolean("hasLoot1", this.hasPlacedLoot[0]);
			tag.putBoolean("hasLoot2", this.hasPlacedLoot[1]);
		}

		@Override
		protected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {

			if(!this.setAverageHeight(world, box, this.boundingBox.minY())) {
				return false;
			}

			placeFoundationUnderneath(world, "minecraft:sandstone", 0, 0, 0, 6, 15, -1, box);
			placeFoundationUnderneath(world, "minecraft:sandstone", 0, 9, 0, 15, 9, -1, box);

			this.fillWithAir(world, box, 1, 0, 1, 5, 5, 9 - 1);

			//House 1
			this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 6, 1, 0, false, rand, RandomSandstone); //Back Wall
			this.fillWithRandomizedBlocks(world, box, 0, 2, 0, 1, 2, 0, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:oak_fence", 0, 2, 2, 0, box);
			this.fillWithRandomizedBlocks(world, box, 3, 2, 0, 3, 2, 0, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:oak_fence", 0, 4, 2, 0, box);
			this.fillWithRandomizedBlocks(world, box, 5, 2, 0, 6, 2, 0, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 0, 3, 0, 6, 3, 0, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 0, 0, 1, 0, 3, 9, false, rand, RandomSandstone); //Left Wall
			this.fillWithRandomizedBlocks(world, box, 1, 0, 9, 6, 1, 9, false, rand, RandomSandstone); //Front Wall
			this.fillWithRandomizedBlocks(world, box, 1, 2, 9, 1, 2, 9, false, rand, RandomSandstone);
			this.fillWithBlocks(world, box, 2, 2, 9, 4, 2, 9, "minecraft:oak_fence");
			this.fillWithRandomizedBlocks(world, box, 5, 2, 9, 6, 2, 9, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 1, 3, 9, 6, 3, 9, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 6, 0, 9 - 1, 6, 3, 9 - 1, false, rand, RandomSandstone); //Right Wall
			this.fillWithRandomizedBlocks(world, box, 6, 0, 9 - 2, 6, 0, 9 - 2, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 6, 3, 9 - 2, 6, 3, 9 - 2, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 6, 0, 1, 6, 3, 9 - 3, false, rand, RandomSandstone);

			this.fillWithBlocks(world, box, 1, 0, 1, 5, 0, 9 - 1, "minecraft:sandstone"); //Floor
			this.fillWithBlocks(world, box, 1, 5 - 1, 0, 5, 5 - 1, 9, "minecraft:sandstone");
			this.fillWithMetadataBlocks(world, box, 0, 5 - 1, 0, 0, 5 - 1, 9, "minecraft:sandstone_slab", 0); //Roof
			this.fillWithMetadataBlocks(world, box, 6, 5 - 1, 0, 6, 5 - 1, 9, "minecraft:sandstone_slab", 0);
			this.fillWithMetadataBlocks(world, box, 2, 5, 0, 4, 5, 0, "minecraft:sandstone_slab", 0);
			this.fillWithMetadataBlocks(world, box, 3, 5, 1, 3, 5, 2, "minecraft:sandstone_slab", 0);
			this.fillWithMetadataBlocks(world, box, 3, 5, 4, 3, 5, 6, "minecraft:sandstone_slab", 0);
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 3, 5, 9 - 1, box);
			this.fillWithMetadataBlocks(world, box, 2, 5, 9, 4, 5, 9, "minecraft:sandstone_slab", 0);

			//House 2
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 0, 0, 15, 0, 0, false, rand, RandomSandstone); //Back Wall
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 1, 0, 15 - 2, 1, 0, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 2, 0, 15 - 6, 2, 0, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 15 - 6, 2, 0, box);
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 15 - 3, 2, 0, box);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 0, 1, 15 - 6, 3, 1, false, rand, RandomSandstone); //Left Wall
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 0, 2, 15 - 6, 0, 2, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 3, 2, 15 - 6, 3, 9 - 1, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 15 - 6, 5 - 1, 2, box);
			this.fillWithMetadataBlocks(world, box, 15 - 6, 5 - 1, 4, 15 - 6, 5 - 1, 9 - 2, "minecraft:sandstone_slab", 0);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 0, 3, 15 - 6, 1, 9, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 0, 2, 15 - 6, 0, 2, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 2, 3, 15 - 6, 2, 3, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:oak_fence", 0, 15 - 6, 2, 4, box);
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 2, 5, 15 - 6, 2, 5, false, rand, RandomSandstone);
			this.fillWithBlocks(world, box, 15 - 6, 2, 9 - 3, 15 - 6, 2, 9 - 2, "minecraft:oak_fence");
			this.fillWithRandomizedBlocks(world, box, 15 - 6, 2, 9 - 1, 15 - 6, 2, 9, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15 - 5, 0, 9, 15, 1, 9, false, rand, RandomSandstone); //Front Wall
			this.fillWithRandomizedBlocks(world, box, 15 - 5, 2, 9, 15 - 5, 2, 9, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15 - 1, 2, 9, 15, 2, 9, false, rand, RandomSandstone);
			this.fillWithRandomizedBlocks(world, box, 15, 0, 1, 15, 0, 9 - 1, false, rand, RandomSandstone); //Right Wall
			this.fillWithRandomizedBlocks(world, box, 15, 1, 3, 15, 1, 3, false, rand, RandomSandstone);
			this.fillWithMetadataBlocks(world, box, 15, 1, 4, 15, 1, 5, "minecraft:sandstone_slab", 0);
			this.fillWithRandomizedBlocks(world, box, 15, 1, 9 - 1, 15, 1, 9 - 3, false, rand, RandomSandstone);
			this.placeBlockAtCurrentPosition(world, "minecraft:sandstone_slab", 0, 15, 1, 9 - 1, box);

			this.fillWithBlocks(world, box, 15 - 5, 0, 1, 15 - 1, 0, 9 - 1, "minecraft:sandstone"); //Floor

			//Loot & Decorations
			//House 1
			int eastMeta = this.getDecoMeta(4);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.machine_boiler_off", 4, 1, 1, 1, box);
			this.fillWithBlocks(world, box, 1, 2, 1, 1, 3, 1, "hbm:tile.deco_pipe_quad_rusted");
			this.placeBlockAtCurrentPosition(world, "hbm:tile.deco_pipe_rim_rusted", 0, 1, 5, 1, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate", 0, 2, 1, 3, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate_can", 0, 1, 1, 9 - 4, box);
			if(!hasPlacedLoot[0]) {
				this.hasPlacedLoot[0] = this.generateInvContents(world, box, rand, "minecraft:chest", this.getDecoMeta(3), 1, 1, 9 - 2, "POOL_MACHINE_PARTS", 10);
			}
			this.fillWithBlocks(world, box, 4, 1, 9 - 1, 5, 1, 9 - 1, "hbm:tile.crate");
			this.fillWithMetadataBlocks(world, box, 5, 1, 4, 5, 3, 4, "hbm:tile.steel_scaffold", eastMeta < 4 ? 0 : 8);
			this.fillWithMetadataBlocks(world, box, 5, 1, 6, 5, 3, 6, "hbm:tile.steel_scaffold", eastMeta < 4 ? 0 : 8);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.steel_grate", 7, 5, 1, 5, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate_weapon", 0, 5, 2, 5, box);

			//House 2
			if(!hasPlacedLoot[1]) {
				this.hasPlacedLoot[1] = this.generateInvContents(world, box, rand, "minecraft:chest", this.getDecoMeta(3), 15 - 5, 1, 1, "POOL_ANTENNA", 10);
			}
			this.placeRandomBobble(world, box, rand, 15 - 5, 1, 4);

			this.randomlyFillWithBlocks(world, box, rand, 0.25F, 15 - 4, 1, 1, 15 - 1, 1, 9 - 1, "minecraft:sand");

			return true;
		}
	}

	public static class NTMLab1 extends Component {

		private static ConcreteBricks RandomConcreteBricks = new ConcreteBricks();
		private static LabTiles RandomLabTiles = new LabTiles();

		private boolean[] hasPlacedLoot = new boolean[2];

		public NTMLab1(StructurePieceSerializationContext context, CompoundTag tag) {
			super(ModStructures.LAB_1_PIECE.get(), tag);
			this.hasPlacedLoot[0] = tag.getBoolean("hasLoot1");
			this.hasPlacedLoot[1] = tag.getBoolean("hasLoot2");
		}

		/** Constructor for this feature; takes coordinates for bounding box */
		public NTMLab1(Random rand, int minX, int minZ) {
			super(ModStructures.LAB_1_PIECE.get(), rand, minX, 64, minZ, 9, 4, 7);
			this.hasPlacedLoot[0] = false;
			this.hasPlacedLoot[1] = false;
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			super.addAdditionalSaveData(context, tag);
			tag.putBoolean("hasLoot1", this.hasPlacedLoot[0]);
			tag.putBoolean("hasLoot2", this.hasPlacedLoot[1]);
		}

		@Override
		protected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {

			if(!this.setAverageHeight(world, box, this.boundingBox.minY())) {
				return false;
			}

			placeFoundationUnderneath(world, "minecraft:stone_bricks", 0, 0, 0, 9, 7 - 2, -1, box);
			placeFoundationUnderneath(world, "minecraft:stone_bricks", 0, 3, 6, 9, 7, -1, box);

			BlockState front = this.getBlockAtCurrentPosition(world, 2, 0, 7 - 1, box);

			if(front.canBeReplaced() || front.isAir()) {
				placeFoundationUnderneath(world, "minecraft:stone_bricks", 0, 2, 7 - 1, 2, 7 - 1, -1, box);
				this.placeBlockAtCurrentPosition(world, "minecraft:stone_brick_stairs", getStairMeta(0), 2, 0, 7 - 1, box);
			}

			this.fillWithAir(world, box, 1, 0, 1, 9 - 1, 4, 4);
			this.fillWithAir(world, box, 4, 0, 4, 9 - 1, 4, 7 - 1);
			this.fillWithAir(world, box, 3, 1, 7 - 1, 3, 2, 7 - 1);

			int pillarMeta = this.getPillarMeta(8);

			//Pillars
			this.fillWithBlocks(world, box, 0, 0, 0, 0, 3, 0, "hbm:tile.concrete_pillar");
			this.fillWithBlocks(world, box, 9, 0, 0, 9, 3, 0, "hbm:tile.concrete_pillar");
			this.fillWithMetadataBlocks(world, box, 0, 0, 1, 0, 0, 4, "hbm:tile.concrete_pillar", pillarMeta);
			this.fillWithMetadataBlocks(world, box, 9, 0, 1, 9, 0, 7 - 1, "hbm:tile.concrete_pillar", pillarMeta);
			this.fillWithBlocks(world, box, 0, 0, 7 - 2, 0, 3, 7 - 2, "hbm:tile.concrete_pillar");
			this.fillWithBlocks(world, box, 3, 0, 7 - 2, 3, 3, 7 - 2, "hbm:tile.concrete_pillar");
			this.fillWithBlocks(world, box, 3, 0, 7, 3, 3, 7, "hbm:tile.concrete_pillar");
			this.fillWithBlocks(world, box, 9, 0, 7, 9, 3, 7, "hbm:tile.concrete_pillar");

			//Walls
			this.fillWithRandomizedBlocks(world, box, 1, 0, 0, 9 - 1, 4 - 1, 0, false, rand, RandomConcreteBricks); //Back Wall
			this.fillWithRandomizedBlocks(world, box, 0, 4, 0, 9, 4, 0, false, rand, RandomConcreteBricks);
			this.fillWithRandomizedBlocks(world, box, 0, 1, 1, 0, 4 - 1, 4, false, rand, RandomConcreteBricks); //Left Wall
			this.fillWithRandomizedBlocks(world, box, 0, 4, 0, 0, 4, 7 - 2, false, rand, RandomConcreteBricks);
			this.fillWithRandomizedBlocks(world, box, 1, 0, 7 - 2, 2, 4, 7 - 2, false, rand, RandomConcreteBricks); //Front Wall Pt. 1
			this.placeBlockAtCurrentPosition(world, "hbm:tile.brick_concrete_broken", 0, 3, 4, 7 - 2, box);
			this.fillWithRandomizedBlocks(world, box, 3, 4 - 1, 7 - 1, 3, 4, 7 - 1, false, rand, RandomConcreteBricks);
			this.fillWithRandomizedBlocks(world, box, 4, 0, 7, 9 - 1, 1, 7, false, rand, RandomConcreteBricks); //Front Wall Pt. 2
			this.fillWithRandomizedBlocks(world, box, 4, 2, 7, 4, 3, 7, false, rand, RandomConcreteBricks);
			this.fillWithRandomizedBlocks(world, box, 9 - 1, 2, 7, 9 - 1, 3, 7, false, rand, RandomConcreteBricks);
			this.randomlyFillWithBlocks(world, box, rand, 0.75F, 5, 2, 7, 9 - 2, 3, 7, "minecraft:glass_pane");
			this.fillWithRandomizedBlocks(world, box, 3, 4, 7, 9, 4, 7, false, rand, RandomConcreteBricks);
			this.fillWithRandomizedBlocks(world, box, 9, 1, 1, 9, 4, 7 - 1, false, rand, RandomConcreteBricks); //Right Wall

			//Floor & Ceiling
			this.fillWithRandomizedBlocks(world, box, 1, 0, 1, 9 - 1, 0, 4, false, rand, RandomLabTiles); //Floor
			this.fillWithRandomizedBlocks(world, box, 4, 0, 7 - 2, 9 - 1, 0, 7 - 1, false, rand, RandomLabTiles);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.tile_lab_cracked", 0, 3, 0, 7 - 1, box);

			this.fillWithBlocks(world, box, 1, 4 - 1, 1, 1, 4, 4, "hbm:tile.reinforced_glass"); //Ceiling
			this.fillWithBlocks(world, box, 2, 4, 1, 9 - 1, 4, 4, "hbm:tile.brick_light");
			this.fillWithBlocks(world, box, 4, 4, 7 - 2, 9 - 1, 4, 7 - 1, "hbm:tile.brick_light");

			//Decorations & Loot
			this.fillWithMetadataBlocks(world, box, 1, 1, 1, 1, 1, 4, "minecraft:dirt", 2);
			int westDecoMeta = this.getDecoMeta(5);
			this.fillWithMetadataBlocks(world, box, 2, 1, 1, 2, 1, 4, "hbm:tile.steel_wall", westDecoMeta);
			this.fillWithMetadataBlocks(world, box, 2, 4 - 1, 1, 2, 4 - 1, 4, "hbm:tile.steel_wall", westDecoMeta);
			for(byte i = 0; i < 4; i++) {
				this.placeBlockAtCurrentPosition(world, "hbm:tile.plant_flower", i, 1, 2, 1 + i, box);
			}

			this.placeDoor(world, box, "hbm:tile.door_office", 2, false, false, 3, 1, 7 - 1);

			int northDecoMeta = this.getDecoMeta(3);
			this.fillWithMetadataBlocks(world, box, 5, 4 - 1, 1, 9 - 1, 4 - 1, 1, "hbm:tile.steel_scaffold", westDecoMeta < 4 ? 0 : 8);
			this.fillWithMetadataBlocks(world, box, 5, 4 - 1, 2, 9 - 1, 4 - 1, 2, "hbm:tile.steel_wall", northDecoMeta);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.machine_electric_furnace_off", northDecoMeta, 5, 1, 1, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.machine_microwave", northDecoMeta, 5, 2, 1, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.deco_titanium", 0, 6, 1, 1, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.machine_shredder", 0, 9 - 2, 1, 1, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.deco_titanium", 0, 9 - 1, 1, 1, box);
			this.fillWithBlocks(world, box, 5, 1, 3, 9 - 1, 1, 3, "hbm:tile.deco_titanium");
			if(!hasPlacedLoot[0]) {
				placeLootPile(this, world, box, rand, "LOOT_MEDICINE", 6, 2, 3);
				this.hasPlacedLoot[0] = true;
			}

			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate_can", 0, 9 - 1, 1, 7 - 2, box);
			if(!hasPlacedLoot[1]) {
				this.hasPlacedLoot[1] = this.generateInvContents(world, box, rand, "hbm:tile.crate_iron", 9 - 1, 1, 7 - 1, "POOL_GENERIC", 8);
			}

			return true;
		}
	}

	public static class NTMLab2 extends Component {

		private static SuperConcrete RandomSuperConcrete = new SuperConcrete();
		private static ConcreteBricks RandomConcreteBricks = new ConcreteBricks();
		private static LabTiles RandomLabTiles = new LabTiles();

		private boolean[] hasPlacedLoot = new boolean[2];

		public NTMLab2(StructurePieceSerializationContext context, CompoundTag tag) {
			super(ModStructures.LAB_2_PIECE.get(), tag);
			this.hasPlacedLoot[0] = tag.getBoolean("hasLoot1");
			this.hasPlacedLoot[1] = tag.getBoolean("hasLoot2");
		}

		public NTMLab2(Random rand, int minX, int minZ) {
			super(ModStructures.LAB_2_PIECE.get(), rand, minX, 64, minZ, 12, 11, 8);
			this.hasPlacedLoot[0] = false;
			this.hasPlacedLoot[1] = false;
		}

		@Override
		protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
			super.addAdditionalSaveData(context, tag);
			tag.putBoolean("hasLoot1", this.hasPlacedLoot[0]);
			tag.putBoolean("hasLoot2", this.hasPlacedLoot[1]);
		}

		@Override
		protected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {

			boolean firstPlacement = this.hpos < 0;
			if(!this.setAverageHeight(world, box, this.boundingBox.minY())) {
				return false;
			}
			if(firstPlacement) {
				this.boundingBox.move(0, -7, 0);
			}

			placeFoundationUnderneath(world, "minecraft:stone_bricks", 0, 0, 0, 12, 8 - 2, 6, box);
			placeFoundationUnderneath(world, "minecraft:stone_bricks", 0, 0, 7, 6, 8, 6, box);

			BlockState entrance = this.getBlockAtCurrentPosition(world, 12 - 3, 11 - 4, 7, box);

			if(entrance.canBeReplaced() || entrance.isAir()) {
				int stairMeta = this.getStairMeta(2);
				placeFoundationUnderneath(world, "minecraft:stone_bricks", 0, 12 - 3, 7, 12 - 2, 7, 11 - 4, box);
				this.fillWithMetadataBlocks(world, box, 12 - 3, 11 - 4, 7, 12 - 2, 11 - 4, 7, "minecraft:stone_brick_stairs", stairMeta);
			}


			this.fillWithAir(world, box, 1, 11 - 4, 1, 12 - 1, 11, 8 - 3);
			this.fillWithAir(world, box, 1, 11 - 4, 8 - 2, 5, 11, 8 - 1);
			this.fillWithAir(world, box, 12 - 3, 11 - 3, 8 - 2, 12 - 2, 11 - 2, 8 - 2);
			this.fillWithAir(world, box, 5, 5, 1, 6, 6, 2);
			this.fillWithAir(world, box, 2, 0, 2, 12 - 2, 3, 8 - 2);

			//Walls
			this.fillWithRandomizedBlocks(world, box, 0, 11 - 4, 0, 12, 11, 0, false, rand, RandomSuperConcrete); //Back Wall
			this.fillWithRandomizedBlocks(world, box, 0, 11 - 4, 0, 0, 11, 8, false, rand, RandomSuperConcrete); //Left Wall
			this.fillWithRandomizedBlocks(world, box, 1, 11 - 4, 8, 5, 11 - 4, 8, false, rand, RandomSuperConcrete); //Front Wall pt. 1
			this.fillWithBlocks(world, box, 1, 11 - 3, 8, 1, 11 - 1, 8, "hbm:tile.reinforced_glass");
			this.fillWithRandomizedBlocks(world, box, 2, 11 - 4, 8, 2, 11 - 1, 8, false, rand, RandomSuperConcrete);
			this.fillWithBlocks(world, box, 3, 11 - 3, 8, 3, 11 - 1, 8, "hbm:tile.reinforced_glass");
			this.fillWithRandomizedBlocks(world, box, 4, 11 - 4, 8, 4, 11 - 1, 8, false, rand, RandomSuperConcrete);
			this.fillWithBlocks(world, box, 5, 11 - 3, 8, 5, 11 - 1, 8, "hbm:tile.reinforced_glass");
			this.fillWithRandomizedBlocks(world, box, 1, 11, 8, 5, 11, 8, false, rand, RandomSuperConcrete);
			this.fillWithRandomizedBlocks(world, box, 6, 11 - 4, 8 - 1, 6, 11, 8, false, rand, RandomSuperConcrete); //Front Wall pt. 2
			this.fillWithRandomizedBlocks(world, box, 6, 11 - 4, 8 - 2, 7, 11 - 2, 8 - 2, false, rand, RandomSuperConcrete); //Front Wall pt. 3
			this.fillWithBlocks(world, box, 6, 11 - 1, 8 - 2, 7, 11 - 1, 8 - 2, "hbm:tile.concrete_super_broken");
			this.fillWithRandomizedBlocks(world, box, 12 - 4, 11 - 4, 8 - 2, 12, 11 - 4, 8 - 2, false, rand, RandomSuperConcrete);
			this.fillWithRandomizedBlocks(world, box, 12 - 4, 11 - 3, 8 - 2, 12 - 4, 11, 8 - 2, false, rand, RandomSuperConcrete);
			this.fillWithRandomizedBlocks(world, box, 12 - 3, 11 - 1, 8 - 2, 12 - 2, 11, 8 - 2, false, rand, RandomSuperConcrete);
			this.fillWithRandomizedBlocks(world, box, 12 - 1, 11 - 4, 8 - 2, 12, 11, 8 - 2, false, rand, RandomSuperConcrete);
			this.fillWithRandomizedBlocks(world, box, 12, 11 - 4, 1, 12, 11 - 4, 8 - 3, false, rand, RandomSuperConcrete); //Right Wall
			this.fillWithBlocks(world, box, 12, 11 - 3, 8 - 3, 12, 11 - 1, 8 - 3, "hbm:tile.reinforced_glass");
			this.fillWithRandomizedBlocks(world, box, 12, 11 - 3, 4, 12, 11 - 1, 4, false, rand, RandomSuperConcrete);
			this.fillWithBlocks(world, box, 12, 11 - 3, 3, 12, 11 - 1, 3, "hbm:tile.reinforced_glass");
			this.fillWithRandomizedBlocks(world, box, 12, 11 - 3, 2, 12, 11 - 1, 2, false, rand, RandomSuperConcrete);
			this.fillWithBlocks(world, box, 12, 11 - 3, 1, 12, 11 - 1, 1, "hbm:tile.reinforced_glass");
			this.fillWithRandomizedBlocks(world, box, 12, 11, 1, 12, 11, 8 - 3, false, rand, RandomSuperConcrete);

			this.fillWithBlocks(world, box, 1, 0, 1, 12 - 1, 3, 1, "hbm:tile.reinforced_stone"); //Back Wall
			this.fillWithBlocks(world, box, 1, 0, 2, 1, 3, 8 - 2, "hbm:tile.reinforced_stone"); //Left Wall
			this.fillWithBlocks(world, box, 1, 0, 8 - 1, 12 - 1, 3, 8 - 1, "hbm:tile.reinforced_stone"); //Front Wall
			this.fillWithBlocks(world, box, 12 - 1, 0, 2, 12 - 1, 3, 8 - 2, "hbm:tile.reinforced_stone"); // Right Wall
			this.fillWithBlocks(world, box, 6, 0, 3, 6, 3, 8 - 2, "hbm:tile.reinforced_stone"); //Internal Wall

			//Floors & Ceiling
			this.fillWithRandomizedBlocks(world, box, 1, 11 - 4, 1, 3, 11 - 4, 8 - 1, false, rand, RandomLabTiles); //Left Floor
			this.fillWithRandomizedBlocks(world, box, 4, 11 - 4, 8 - 2, 5, 11 - 4, 8 - 1, false, rand, RandomLabTiles);
			this.fillWithRandomizedBlocks(world, box, 12 - 4, 11 - 4, 1, 12 - 1, 11 - 4, 8 - 3, false, rand, RandomLabTiles); //Right Floor
			this.fillWithRandomizedBlocks(world, box, 12 - 3, 11 - 4, 8 - 2, 12 - 2, 11 - 4, 8 - 2, false, rand, RandomLabTiles);
			this.fillWithBlocks(world, box, 4, 11 - 4, 1, 7, 11 - 4, 1, "hbm:tile.tile_lab_broken"); //Center Floor (Pain)
			this.placeBlockAtCurrentPosition(world, "hbm:tile.tile_lab_broken", 0, 4, 11 - 4, 2, box);
			this.fillWithBlocks(world, box, 4, 11 - 4, 3, 4, 11 - 4, 5, "hbm:tile.tile_lab_cracked");
			this.placeBlockAtCurrentPosition(world, "hbm:tile.tile_lab_broken", 0, 5, 11 - 4, 3, box);
			this.fillWithBlocks(world, box, 5, 11 - 4, 4, 5, 11 - 4, 5, "hbm:tile.tile_lab_cracked");
			this.placeBlockAtCurrentPosition(world, "hbm:tile.tile_lab_broken", 0, 6, 11 - 4, 4, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.tile_lab_cracked", 0, 6, 11 - 4, 5, box);
			this.fillWithBlocks(world, box, 7, 11 - 4, 2, 7, 11 - 4, 3, "hbm:tile.tile_lab_broken");
			this.fillWithBlocks(world, box, 7, 11 - 4, 4, 7, 11 - 4, 5, "hbm:tile.tile_lab_cracked");

			this.fillWithBlocks(world, box, 1, 11, 1, 2, 11, 8 - 1, "hbm:tile.brick_light"); //Left Ceiling
			this.fillWithBlocks(world, box, 3, 11, 8 - 2, 4, 11, 8 - 1, "hbm:tile.brick_light");
			this.fillWithBlocks(world, box, 12 - 3, 11, 1, 12 - 1, 11, 8 - 3, "hbm:tile.brick_light"); //Right Ceiling
			this.fillWithBlocks(world, box, 3, 11, 1, 8, 11, 1, "hbm:tile.waste_planks"); //Center Ceiling (Pain)
			this.fillWithBlocks(world, box, 3, 11, 2, 4, 11, 2, "hbm:tile.waste_planks");
			this.fillWithBlocks(world, box, 7, 11, 2, 8, 11, 2, "hbm:tile.waste_planks");
			this.fillWithBlocks(world, box, 3, 11, 3, 3, 11, 5, "hbm:tile.waste_planks");
			this.fillWithBlocks(world, box, 4, 11, 4, 4, 11, 5, "hbm:tile.waste_planks");
			this.fillWithBlocks(world, box, 5, 11, 6, 5, 11, 8 - 1, "hbm:tile.waste_planks");
			this.fillWithBlocks(world, box, 8, 11, 3, 8, 11, 5, "hbm:tile.waste_planks");

			this.fillWithRandomizedBlocks(world, box, 2, 0, 2, 5, 0, 8 - 2, false, rand, RandomLabTiles); //Floor
			this.fillWithRandomizedBlocks(world, box, 6, 0, 2, 6, 0, 3, false, rand, RandomLabTiles);
			this.fillWithRandomizedBlocks(world, box, 7, 0, 2, 12 - 2, 0, 8 - 2, false, rand, RandomLabTiles);

			this.fillWithRandomizedBlocks(world, box, 1, 4, 1, 12 - 1, 4, 8 - 1, false, rand, RandomConcreteBricks); //Ceiling

			//Decorations & Loot
			int eastMeta = this.getDecoMeta(4);
			int westMeta = this.getDecoMeta(5);
			int northMeta = this.getDecoMeta(3);
			int southMeta = this.getDecoMeta(2);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crashed_balefire", southMeta, 6, 11 - 2, 3, box);

			this.placeDoor(world, box, "hbm:tile.door_office", 1, false, false, 12 - 3, 11 - 3, 8 - 2);
			this.placeDoor(world, box, "hbm:tile.door_office", 1, false, false, 12 - 2, 11 - 3, 8 - 2);

			this.fillWithBlocks(world, box, 1, 11 - 3, 1, 1, 11 - 1, 1, "hbm:tile.deco_steel");
			this.fillWithMetadataBlocks(world, box, 1, 11 - 3, 2, 1, 11 - 2, 3, "hbm:tile.steel_grate", 7);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.tape_recorder", westMeta, 1, 11 - 1, 2, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.steel_beam", 0, 1, 11 - 1, 3, box);
			this.fillWithBlocks(world, box, 1, 11 - 3, 6, 1, 11 - 1, 6, "hbm:tile.deco_pipe_framed_rusted");

			this.fillWithMetadataBlocks(world, box, 12 - 4, 11 - 3, 1, 12 - 4, 11 - 1, 1, "hbm:tile.steel_wall", eastMeta);
			this.fillWithMetadataBlocks(world, box, 12 - 3, 11 - 1, 1, 12 - 2, 11 - 1, 1, "hbm:tile.steel_grate", 0);
			this.fillWithMetadataBlocks(world, box, 12 - 3, 11 - 2, 1, 12 - 2, 11 - 2, 1, "hbm:tile.tape_recorder", northMeta);
			this.fillWithBlocks(world, box, 12 - 3, 11 - 3, 1, 12 - 2, 11 - 3, 1, "hbm:tile.deco_steel");
			this.fillWithMetadataBlocks(world, box, 12 - 1, 11 - 3, 1, 12 - 1, 11 - 1, 1, "hbm:tile.steel_wall", westMeta);

			this.fillWithMetadataBlocks(world, box, 2, 1, 2, 2, 1, 8 - 2, "hbm:tile.steel_grate", 7);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.vitrified_barrel", 0, 2, 2, 2, box);
			this.fillWithMetadataBlocks(world, box, 3, 1, 2, 3, 3, 2, "hbm:tile.steel_wall", westMeta);
			this.fillWithMetadataBlocks(world, box, 3, 1, 4, 3, 3, 4, "hbm:tile.steel_wall", westMeta);
			this.fillWithMetadataBlocks(world, box, 3, 1, 8 - 2, 3, 3, 8 - 2, "hbm:tile.steel_wall", westMeta);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate", 0, 4, 1, 8 - 2, box);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.crate_lead", 0, 4, 2, 8 - 2, box);
			if(!hasPlacedLoot[0]) {
				this.hasPlacedLoot[0] = this.generateInvContents(world, box, rand, "hbm:tile.crate_iron", 5, 1, 8 - 2, "POOL_NUKE_FUEL", 10);
			}
			this.fillWithBlocks(world, box, 4, 1, 8 - 3, 5, 1, 8 - 3, "hbm:tile.crate_lead");

			this.fillWithBlocks(world, box, 12 - 5, 1, 8 - 2, 12 - 5, 3, 8 - 2, "hbm:tile.deco_steel");
			this.fillWithMetadataBlocks(world, box, 12 - 4, 1, 8 - 2, 12 - 2, 1, 8 - 2, "hbm:tile.steel_grate", 7);
			this.fillWithMetadataBlocks(world, box, 12 - 4, 2, 8 - 2, 12 - 3, 2, 8 - 2, "hbm:tile.tape_recorder", southMeta);
			this.placeBlockAtCurrentPosition(world, "hbm:tile.steel_beam", 0, 12 - 2, 2, 8 - 2, box);
			this.fillWithBlocks(world, box, 12 - 4, 3, 8 - 2, 12 - 2, 3, 8 - 2, "hbm:tile.steel_roof");
			if(!hasPlacedLoot[1]) {
				this.hasPlacedLoot[1] = this.generateInvContents(world, box, rand, "hbm:tile.crate_iron", 12 - 2, 1, 3, "POOL_NUKE_TRASH", 9);
				if(rand.nextInt(2) == 0)
					generateLoreBook(world, box, 12 - 2, 1, 3, 1, ItemStack.EMPTY);
			}

			return true;
		}
	}

	public static class RuralHouse1 extends Component {

		public RuralHouse1(StructurePieceSerializationContext context, CompoundTag tag) {
			super(ModStructures.RURAL_HOUSE_PIECE.get(), tag);
		}

		public RuralHouse1(Random rand, int minX, int minZ) {
			super(ModStructures.RURAL_HOUSE_PIECE.get(), rand, minX, 64, minZ, 14, 8, 14);
		}

		@Override
		protected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {

			if(!this.setAverageHeight(world, box, this.boundingBox.minY())) {
				return false;
			}

			//FillWithAir
			fillWithAir(world, box, 9, 1, 3, 12, 4, 8);
			fillWithAir(world, box, 5, 1, 2, 8, 3, 8);
			fillWithAir(world, box, 2, 1, 5, 4, 3, 8);
			fillWithAir(world, box, 2, 1, 10, 7, 3, 12);

			//Foundations
			fillWithBlocks(world, box, 1, 0, 4, 4, 0, 4, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 4, 0, 2, 4, 0, 3, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 4, 0, 1, 9, 0, 1, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 9, 0, 2, 10, 0, 2, "hbm:tile.concrete_colored_ext");
			placeBlockAtCurrentPosition(world, "hbm:tile.concrete_colored_ext", 0, 12, 0, 2, box);
			fillWithBlocks(world, box, 13, 0, 2, 13, 0, 9, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 5, 0, 9, 12, 0, 9, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 2, 0, 9, 3, 0, 9, "hbm:tile.concrete_colored_ext");
			placeBlockAtCurrentPosition(world, "hbm:tile.concrete_colored_ext", 0, 8, 0, 10, box);
			fillWithBlocks(world, box, 8, 0, 12, 8, 0, 13, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 1, 0, 13, 7, 0, 13, "hbm:tile.concrete_colored_ext");
			fillWithBlocks(world, box, 1, 0, 5, 1, 0, 12, "hbm:tile.concrete_colored_ext");
			placeFoundationUnderneath(world, "hbm:tile.concrete_colored_ext", 0, 1, 10, 8, 13, -1, box);
			placeFoundationUnderneath(world, "hbm:tile.concrete_colored_ext", 0, 1, 4, 3, 9, -1, box);
			placeFoundationUnderneath(world, "hbm:tile.concrete_colored_ext", 0, 4, 1, 13, 9, -1, box);

			placeFoundationUnderneath(world, "minecraft:log", 0, 2, 3, 2, 3, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 3, 2, 3, 2, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 3, 0, 3, 0, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 5, 0, 5, 0, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 8, 0, 8, 0, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 10, 0, 10, 0, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 14, 1, 14, 1, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 14, 3, 14, 3, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 14, 5, 14, 6, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 14, 8, 14, 8, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 14, 10, 14, 10, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 9, 14, 9, 14, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 7, 14, 7, 14, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 4, 14, 5, 14, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 2, 14, 2, 14, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 14, 0, 14, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 13, 0, 13, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 11, 0, 11, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 9, 0, 9, -1, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 6, 0, 7, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 4, 0, 4, 0, box);
			placeFoundationUnderneath(world, "minecraft:log", 0, 0, 3, 0, 4, -1, box);

			//Walls
			//North/Front
			fillWithBlocks(world, box, 1, 1, 4, 4, 4, 4, "minecraft:brick_block");
			fillWithBlocks(world, box, 2, 5, 4, 7, 5, 4, "minecraft:brick_block");
			placeBlockAtCurrentPosition(world, "minecraft:brick_block", 0, 3, 6, 4, box);
			placeBlockAtCurrentPosition(world, "minecraft:brick_block", 0, 6, 6, 4, box);
			fillWithBlocks(world, box, 4, 7, 4, 5, 7, 4, "minecraft:brick_block");
			fillWithBlocks(world, box, 4, 1, 1, 4, 4, 3, "minecraft:brick_block");
			fillWithBlocks(world, box, 5, 1, 1, 8, 1, 1, "minecraft:brick_block");
			fillWithBlocks(world, box, 5, 4, 1, 8, 4, 1, "minecraft:brick_block");
			fillWithBlocks(world, box, 9, 1, 1, 9, 4, 2, "minecraft:brick_block");
			fillWithBlocks(world, box, 10, 1, 2, 10, 3, 2, "minecraft:brick_block");
			fillWithBlocks(world, box, 12, 1, 2, 13, 3, 2, "minecraft:brick_block");
			fillWithBlocks(world, box, 10, 4, 2, 13, 4, 2, "minecraft:brick_block");
			fillWithBlocks(world, box, 9, 5, 2, 12, 5, 2, "minecraft:brick_block");
			fillWithBlocks(world, box, 10, 6, 2, 11, 6, 2, "minecraft:brick_block");
			//East/Left
			fillWithBlocks(world, box, 13, 1, 3, 13, 1, 8, "minecraft:brick_block");
			fillWithBlocks(world, box, 13, 3, 3, 13, 4, 8, "minecraft:brick_block");
			//South/Back
			fillWithBlocks(world, box, 13, 1, 9, 13, 4, 9, "minecraft:brick_block");
			fillWithBlocks(world, box, 9, 1, 9, 12, 1, 9, "minecraft:brick_block");
			fillWithBlocks(world, box, 9, 4, 9, 12, 5, 9, "minecraft:brick_block");
			fillWithBlocks(world, box, 10, 6, 9, 11, 6, 9, "minecraft:brick_block");
			fillWithBlocks(world, box, 8, 1, 9, 8, 4, 10, "minecraft:brick_block");
			fillWithBlocks(world, box, 8, 1, 12, 8, 3, 13, "minecraft:brick_block");
			fillWithBlocks(world, box, 8, 4, 11, 8, 4, 13, "minecraft:brick_block");
			fillWithBlocks(world, box, 7, 1, 13, 7, 3, 13, "minecraft:brick_block");
			fillWithBlocks(world, box, 3, 1, 13, 6, 1, 13, "minecraft:brick_block");
			fillWithBlocks(world, box, 2, 4, 13, 7, 5, 13, "minecraft:brick_block");
			placeBlockAtCurrentPosition(world, "minecraft:brick_block", 0, 6, 6, 13, box);
			placeBlockAtCurrentPosition(world, "minecraft:brick_block", 0, 3, 6, 13, box);
			fillWithBlocks(world, box, 4, 7, 13, 5, 7, 13, "minecraft:brick_block");
			fillWithBlocks(world, box, 2, 1, 13, 2, 3, 13, "minecraft:brick_block");
			//West/Right
			fillWithBlocks(world, box, 1, 1, 13, 1, 4, 13, "minecraft:brick_block");
			fillWithBlocks(world, box, 1, 1, 5, 1, 1, 12, "minecraft:brick_block");
			placeBlockAtCurrentPosition(world, "minecraft:brick_block", 0, 1, 2, 9, box);
			fillWithBlocks(world, box, 1, 3, 5, 1, 3, 12, "minecraft:brick_block");
			//Inside
			fillWithBlocks(world, box, 2, 1, 9, 3, 3, 9, "minecraft:brick_block");
			fillWithBlocks(world, box, 5, 1, 9, 7, 3, 9, "minecraft:brick_block");
			//Wood Paneling
			fillWithMetadataBlocks(world, box, 5, 2, 1, 5, 3, 1, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 8, 2, 1, 8, 3, 1, "minecraft:planks", 1);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 11, 3, 2, box);
			fillWithMetadataBlocks(world, box, 13, 2, 3, 13, 2, 4, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 13, 2, 7, 13, 2, 8, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 12, 2, 9, 12, 3, 9, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 9, 2, 9, 9, 3, 9, "minecraft:planks", 1);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 8, 3, 11, box);
			fillWithMetadataBlocks(world, box, 6, 2, 13, 6, 3, 13, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 3, 2, 13, 3, 3, 13, "minecraft:planks", 1);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 1, 2, 12, box);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 1, 2, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 1, 2, 8, box);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 1, 2, 5, box);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 4, 3, 9, box);
			//Wood Framing
			//North/Front
			int logW = this.getPillarMeta(4);
			int logN = this.getPillarMeta(8);

			fillWithBlocks(world, box, 0, 0, 3, 0, 3, 3, "minecraft:log");
			fillWithMetadataBlocks(world, box, 1, 4, 3, 3, 4, 3, "minecraft:log", logW);
			fillWithMetadataBlocks(world, box, 3, 4, 1, 3, 4, 2, "minecraft:log", logN);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 1, 3, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 3, 3, 1, box);
			fillWithMetadataBlocks(world, box, 1, 1, 3, 2, 1, 3, "minecraft:wooden_slab", 1);
			fillWithMetadataBlocks(world, box, 3, 1, 1, 3, 1, 3, "minecraft:wooden_slab", 1);
			fillWithBlocks(world, box, 3, 0, 0, 3, 3, 0, "minecraft:log");
			fillWithMetadataBlocks(world, box, 4, 1, 0, 9, 1, 0, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 4, 3, 0, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 9, 3, 0, box);
			fillWithBlocks(world, box, 10, 0, 0, 10, 3, 0, "minecraft:log");
			fillWithMetadataBlocks(world, box, 10, 4, 1, 13, 4, 1, "minecraft:log", logW);
			fillWithBlocks(world, box, 14, 0, 1, 14, 3, 1, "minecraft:log");
			//East/Left
			fillWithBlocks(world, box, 14, 0, 3, 14, 3, 3, "minecraft:log");
			fillWithBlocks(world, box, 14, 0, 8, 14, 3, 8, "minecraft:log");
			fillWithBlocks(world, box, 14, 0, 10, 14, 3, 10, "minecraft:log");
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 14, 1, 2, box);
			fillWithMetadataBlocks(world, box, 14, 1, 4, 14, 1, 7, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 14, 1, 9, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 14, 3, 2, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 14, 3, 4, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 14, 3, 7, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 14, 3, 9, box);
			//South/Back
			fillWithMetadataBlocks(world, box, 9, 4, 10, 13, 4, 10, "minecraft:log", logW);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 13, 3, 10, box);
			fillWithBlocks(world, box, 9, 0, 14, 9, 3, 14, "minecraft:log");
			fillWithBlocks(world, box, 7, 0, 14, 7, 3, 14, "minecraft:log");
			fillWithBlocks(world, box, 2, 0, 14, 2, 3, 14, "minecraft:log");
			fillWithBlocks(world, box, 0, 0, 14, 0, 3, 14, "minecraft:log");
			fillWithMetadataBlocks(world, box, 1, 4, 14, 8, 4, 14, "minecraft:log", logW);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 8, 1, 14, box);
			fillWithMetadataBlocks(world, box, 3, 1, 14, 6, 1, 14, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 1, 1, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 8, 3, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 1, 3, 14, box);
			//West/Right
			fillWithBlocks(world, box, 0, 0, 9, 0, 3, 9, "minecraft:log");
			fillWithMetadataBlocks(world, box, 0, 1, 10, 0, 1, 13, "minecraft:wooden_slab", 1);
			fillWithMetadataBlocks(world, box, 0, 1, 4, 0, 1, 8, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 0, 3, 13, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 0, 3, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 0, 3, 8, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 9, 0, 3, 4, box);

			int stairW = this.getStairMeta(0);
			int stairE = this.getStairMeta(1);
			int stairN = this.getStairMeta(2);
			int stairS = this.getStairMeta(3);

			//Floor
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 11, 0, 2, box);
			fillWithMetadataBlocks(world, box, 9, 0, 3, 12, 0, 8, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 5, 0, 2, 8, 0, 8, "minecraft:planks", 1);
			fillWithMetadataBlocks(world, box, 2, 0, 5, 4, 0, 8, "minecraft:planks", 1);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 4, 0, 9, box);
			fillWithMetadataBlocks(world, box, 2, 0, 10, 7, 0, 12, "minecraft:planks", 1);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 8, 0, 11, box);
			fillWithBlocks(world, box, 13, 1, 0, 14, 1, 0, "minecraft:oak_fence");
			//Porches
			fillWithBlocks(world, box, 10, 0, 1, 13, 0, 1, "minecraft:planks");
			fillWithMetadataBlocks(world, box, 11, 0, 0, 12, 0, 0, "minecraft:spruce_stairs", stairN);
			fillWithMetadataBlocks(world, box, 13, 0, 0, 14, 0, 0, "minecraft:planks", 1);
			fillWithBlocks(world, box, 12, 0, 10, 13, 0, 10, "minecraft:planks");
			fillWithBlocks(world, box, 9, 0, 10, 11, 0, 11, "minecraft:planks");
			fillWithBlocks(world, box, 9, 0, 12, 10, 0, 12, "minecraft:planks");
			placeBlockAtCurrentPosition(world, "minecraft:planks", 0, 9, 0, 13, box);
			for(int i = 0; i < 3; i++) {
				fillWithMetadataBlocks(world, box, 10 + i, 0, 13 - i, 11 + i, 0, 13 - i, "minecraft:planks", 1);
				fillWithBlocks(world, box, 10 + i, 1, 13 - i, 11 + i, 1, 13 - i, "minecraft:oak_fence");
			}

			//Ceiling
			fillWithMetadataBlocks(world, box, 12, 4, 3, 12, 4, 8, "minecraft:oak_stairs", stairW | 4);
			fillWithBlocks(world, box, 12, 5, 3, 12, 5, 8, "minecraft:planks");
			fillWithBlocks(world, box, 10, 5, 3, 11, 6, 8, "minecraft:planks");
			fillWithBlocks(world, box, 9, 5, 3, 9, 5, 8, "minecraft:planks");
			fillWithMetadataBlocks(world, box, 9, 4, 3, 9, 4, 8, "minecraft:oak_stairs", stairE | 4);
			fillWithBlocks(world, box, 8, 4, 5, 8, 4, 8, "minecraft:planks");
			fillWithBlocks(world, box, 5, 4, 2, 8, 4, 4, "minecraft:planks");
			fillWithBlocks(world, box, 1, 4, 5, 7, 4, 12, "minecraft:planks");

			//Roofing
			//Framing
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 1, 5, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 2, 6, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 3, 6, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 3, 7, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 4, 7, 3, box);
			fillWithMetadataBlocks(world, box, 4, 8, 3, 5, 8, 3, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 5, 7, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 6, 7, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 6, 6, 3, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 7, 6, 3, box);
			fillWithMetadataBlocks(world, box, 2, 5, 3, 3, 5, 3, "minecraft:planks", 1);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 3, 5, 2, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 3, 5, 1, box);
			fillWithMetadataBlocks(world, box, 3, 4, 0, 14, 4, 0, "minecraft:spruce_stairs", stairN);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 8, 5, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:planks", 1, 9, 5, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 10, 5, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 9, 6, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 10, 6, 1, box);
			fillWithMetadataBlocks(world, box, 10, 7, 1, 11, 7, 1, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 11, 6, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 12, 6, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 12, 5, 1, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 13, 5, 1, box);
			fillWithMetadataBlocks(world, box, 14, 4, 1, 14, 4, 10, "minecraft:spruce_stairs", stairE);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 13, 5, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 12, 5, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 12, 6, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 11, 6, 10, box);
			fillWithMetadataBlocks(world, box, 10, 7, 10, 11, 7, 10, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 10, 6, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 9, 6, 10, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 9, 5, 10, box);
			fillWithMetadataBlocks(world, box, 9, 4, 11, 9, 4, 14, "minecraft:spruce_stairs", stairE);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 8, 5, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 7, 5, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 7, 6, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 6, 6, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE, 6, 7, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 5, 7, 14, box);
			fillWithMetadataBlocks(world, box, 4, 8, 14, 5, 8, 14, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 4, 7, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 3, 7, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 3, 6, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 2, 6, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 2, 5, 14, box);
			placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW, 1, 5, 14, box);
			fillWithMetadataBlocks(world, box, 0, 4, 3, 0, 4, 14, "minecraft:spruce_stairs", stairW);
			//Beams
			for(int z = 6; z <= 11; z += 5) {
				for(int i = 0; i < 3; i++) {
					placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairE | 4, 2 + i, 5 + i, z, box);
					placeBlockAtCurrentPosition(world, "minecraft:spruce_stairs", stairW | 4, 7 - i, 5 + i, z, box);
				}
			}

			//Main (LEFT)
			BrokenStairs roofStairs = new BrokenStairs();
			BrokenBlocks roofBlocks = new BrokenBlocks();

			roofStairs.setMetadata(stairW);
			fillWithBlocks(world, box, 4, 5, 1, 7, 5, 1, "minecraft:wooden_slab");
			fillWithRandomizedBlocks(world, box, 4, 5, 2, 7, 5, 3, rand, roofBlocks);
			fillWithRandomizedBlocks(world, box, 8, 5, 2, 8, 5, 10, rand, roofBlocks);
			fillWithRandomizedBlocks(world, box, 9, 6, 2, 9, 6, 9, rand, roofStairs);
			randomlyFillWithBlocks(world, box, rand, 0.8F, 10, 7, 2, 11, 7, 9, "minecraft:wooden_slab");
			roofStairs.setMetadata(stairE);
			fillWithRandomizedBlocks(world, box, 12, 6, 2, 12, 6, 9, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 13, 5, 2, 13, 5, 9, rand, roofStairs);
			//Main (RIGHT)
			fillWithRandomizedBlocks(world, box, 8, 5, 11, 8, 5, 13, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 7, 6, 4, 7, 6, 13, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 6, 7, 4, 6, 7, 7, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 6, 7, 11, 6, 7, 13, rand, roofStairs);
			roofStairs.setMetadata(stairW);
			fillWithBlocks(world, box, 4, 8, 4, 5, 8, 5, "minecraft:wooden_slab");
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 0, 5, 8, 6, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 0, 4, 8, 11, box);
			fillWithBlocks(world, box, 4, 8, 12, 5, 8, 13, "minecraft:wooden_slab");
			fillWithRandomizedBlocks(world, box, 3, 7, 4, 3, 7, 6, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 3, 7, 10, 3, 7, 13, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 2, 6, 4, 2, 6, 13, rand, roofStairs);
			fillWithRandomizedBlocks(world, box, 1, 5, 4, 1, 5, 13, rand, roofStairs);

			//Deco
			int metaN = getDecoMeta(3);
			int metaE = getDecoMeta(4);

			//Webs
			randomlyFillWithBlocks(world, box, rand, 0.05F, 12, 3, 3, 12, 3, 8, "minecraft:web");
			randomlyFillWithBlocks(world, box, rand, 0.05F, 10, 4, 3, 11, 4, 8, "minecraft:web");
			randomlyFillWithBlocks(world, box, rand, 0.05F, 5, 3, 2, 8, 3, 2, "minecraft:web");
			randomlyFillWithBlocks(world, box, rand, 0.05F, 5, 3, 3, 9, 3, 8, "minecraft:web");
			randomlyFillWithBlocks(world, box, rand, 0.05F, 2, 3, 5, 4, 3, 8, "minecraft:web");
			randomlyFillWithBlocks(world, box, rand, 0.05F, 2, 3, 10, 7, 3, 12, "minecraft:web");
			//Doors
			placeDoor(world, box, "minecraft:oak_door", 1, false, false, 11, 1, 2);
			placeDoor(world, box, "minecraft:oak_door", 1, false, rand.nextBoolean(), 4, 1, 9);
			placeDoor(world, box, "minecraft:oak_door", 2, false, rand.nextBoolean(), 8, 1, 11);
			//Windows
			randomlyFillWithBlocks(world, box, rand, 0.5F, 6, 2, 1, 7, 3, 1, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 13, 2, 5, 13, 2, 6, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 10, 2, 9, 11, 3, 9, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 4, 2, 13, 5, 3, 13, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 1, 2, 11, 1, 2, 11, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 1, 2, 6, 1, 2, 7, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 4, 6, 4, 5, 6, 4, "minecraft:glass_pane");
			randomlyFillWithBlocks(world, box, rand, 0.5F, 4, 6, 13, 5, 6, 13, "minecraft:glass_pane");
			//Attic Access
			placeBlockAtCurrentPosition(world, "minecraft:trapdoor", getDecoModelMeta(4) >> 2, 6, 4, 10, box);
			fillWithMetadataBlocks(world, box, 6, 2, 10, 6, 3, 10, "minecraft:ladder", metaN);
			//Furniture
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairN | 4, 12, 1, 5, box); //tables
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 8, 12, 1, 6, box);
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairS | 4, 12, 1, 7, box);
			fillWithMetadataBlocks(world, box, 9, 1, 4, 9, 1, 5, "minecraft:dark_oak_stairs", stairE | 4);
			fillWithMetadataBlocks(world, box, 8, 1, 4, 8, 1, 5, "minecraft:wooden_slab", 13);
			fillWithMetadataBlocks(world, box, 7, 1, 4, 7, 1, 5, "minecraft:dark_oak_stairs", stairW | 4);
			placeBlockAtCurrentPosition(world, "minecraft:dark_oak_stairs", stairS | 4, 8, 1, 2, box); //couch
			placeBlockAtCurrentPosition(world, "minecraft:dark_oak_stairs", stairW, 7, 1, 2, box);
			placeBlockAtCurrentPosition(world, "minecraft:dark_oak_stairs", stairS, 6, 1, 2, box);
			fillWithMetadataBlocks(world, box, 5, 1, 2, 5, 1, 3, "minecraft:dark_oak_stairs", stairE);
			placeBlockAtCurrentPosition(world, "minecraft:dark_oak_stairs", stairN, 5, 1, 4, box);
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairW, 10, 1, 5, box); //chairs
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairN, 8, 1, 6, box);
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairE, 9, 1, 8, box); //bookshelf
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairE | 4, 9, 2, 8, box);
			fillWithBlocks(world, box, 8, 1, 8, 8, 2, 8, "minecraft:bookshelf");
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairW, 7, 1, 8, box);
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairW | 4, 7, 2, 8, box);
			fillWithMetadataBlocks(world, box, 7, 3, 8, 9, 3, 8, "minecraft:wooden_slab", 1);
			placeBlockAtCurrentPosition(world, "minecraft:double_stone_slab", 0, 4, 1, 5, box); //kitchen
			placeBlockAtCurrentPosition(world, rand.nextBoolean() ? "hbm:tile.machine_electric_furnace_off" : "minecraft:furnace", metaN, 3, 1, 5, box);
			fillWithBlocks(world, box, 2, 1, 5, 2, 1, 6, "minecraft:double_stone_slab");
			placeBlockAtCurrentPosition(world, "minecraft:cauldron", 2, 2, 1, 7, box);
			placeBlockAtCurrentPosition(world, "minecraft:double_stone_slab", 0, 2, 1, 8, box);
			placeBlockAtCurrentPosition(world, "minecraft:double_stone_slab", 0, 4, 3, 5, box);
			placeBlockAtCurrentPosition(world, "minecraft:redstone_lamp", 0, 3, 3, 5, box);
			placeBlockAtCurrentPosition(world, "minecraft:double_stone_slab", 0, 2, 3, 5, box);
			placeBlockAtCurrentPosition(world, "hbm:tile.steel_wall", metaN, 3, 3, 6, box);
			placeBlockAtCurrentPosition(world, "hbm:tile.radiorec", getDecoMeta(2), 8, 2, 2, box);
			placeBlockAtCurrentPosition(world, "minecraft:flower_pot", 0, 7, 2, 4, box);

			fillWithBlocks(world, box, 2, 1, 12, 3, 1, 12, "minecraft:bookshelf"); //bookshelf/desk
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairE | 4, 4, 1, 12, box);
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 8, 5, 1, 12, box);
			placeBlockAtCurrentPosition(world, "minecraft:oak_stairs", stairW | 4, 6, 1, 12, box);
			fillWithBlocks(world, box, 7, 1, 12, 7, 2, 12, "minecraft:bookshelf");
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 5, 5, 1, 11, box); //seat
			placeBed(world, box, 1, 3, 1, 10);
			placeBlockAtCurrentPosition(world, "minecraft:flower_pot", 0, 4, 2, 12, box);
			placeBlockAtCurrentPosition(world, "hbm:tile.deco_computer", getDecoModelMeta(0), 5, 2, 12, box);

			fillWithMetadataBlocks(world, box, 4, 5, 5, 5, 5, 5, "minecraft:dark_oak_stairs", stairS | 4); //seat and desk
			placeBlockAtCurrentPosition(world, "minecraft:wooden_slab", 1, 4, 5, 6, box);
			placeBlockAtCurrentPosition(world, "hbm:tile.crate_can", 0, 7, 5, 7, box); //conserve crates
			placeBlockAtCurrentPosition(world, "hbm:tile.crate_can", 0, 2, 5, 9, box);
			placeBlockAtCurrentPosition(world, "hbm:tile.crate_can", 0, 3, 5, 11, box);
			if(rand.nextBoolean())
				placeBlockAtCurrentPosition(world, "hbm:tile.machine_diesel", metaE, 7, 5, 9, box);
			placeBlockAtCurrentPosition(world, rand.nextBoolean() ? "hbm:tile.crate_weapon" : "hbm:tile.crate", 0, 6, 5, 12, box);

			//inventories
			generateInvContents(world, box, rand, "hbm:tile.filing_cabinet", getDecoModelMeta(2), 7, 1, 10, "POOL_OFFICE_TRASH", 4);
			generateInvContents(world, box, rand, "minecraft:chest", metaE, 7, 5, 5, "POOL_GENERIC", 8);
			//loot
			placeLootPile(this, world, box, rand, "LOOT_BOOKLET", 3, 2, 12);
			placeLootPile(this, world, box, rand, "LOOT_MAKESHIFT_GUN", 5, 6, 5);
			placeRandomBobble(world, box, rand, 5, 5, 12);

			return true;
		}

		public static class BrokenStairs extends BlockSelector {

			private int stairMeta;

			public void setMetadata(int meta) {
				this.stairMeta = meta;
			}

			@Override
			public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
				float chance = rand.nextFloat();

				if(chance < 0.7) {
					this.blockName = "minecraft:oak_stairs";
					this.selectedBlockMetaData = this.stairMeta; //only stairs keep the facing meta
				} else if(chance < 0.97) {
					this.blockName = "minecraft:wooden_slab";
					this.selectedBlockMetaData = 0;
				} else {
					this.blockName = "minecraft:air";
					this.selectedBlockMetaData = 0;
				}
			}
		}

		public static class BrokenBlocks extends BlockSelector {

			@Override
			public void selectBlocks(Random rand, int posX, int posY, int posZ, boolean notInterior) {
				float chance = rand.nextFloat();

				if(chance < 0.6) {
					this.blockName = "minecraft:planks";
					this.selectedBlockMetaData = 0;
				} else if(chance < 0.8) {
					this.blockName = "minecraft:oak_stairs";
					this.selectedBlockMetaData = rand.nextInt(4);
				} else {
					this.blockName = "minecraft:wooden_slab";
					this.selectedBlockMetaData = 0;
				}
			}
		}
	}
}
