const fs = require("fs");
const path = require("path");

const src = path.join("legacy-1.7.10/src/main/java/com/hbm/world/gen/component/SiloComponent.java");
const dst = path.join("src/main/java/com/hbm/world/gen/component/SiloComponent.java");
let text = fs.readFileSync(src, "utf8");

const start = text.indexOf("\t@Override\n\tpublic boolean addComponentParts");
let body = text.slice(start);
body = body.replace(
  "\t@Override\n\tpublic boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {",
  "\t@Override\n\tprotected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {"
);

body = body.replace(
  `		if(this.hpos == -1) {
			StructureBoundingBox area = getRotatedBoundingBox(getXWithOffset(13, 2), getYWithOffset(25), getZWithOffset(13, 2), 29, 3, 18); //anchor offset/world pos already accounted for with offset methods
			this.hpos = this.getAverageHeight(world, area, box, getYWithOffset(25));
			this.boundingBox.offset(0, this.hpos - 1 - getYWithOffset(25), 0);
		}`,
  `		if(this.hpos == -1) {
			BoundingBox area = getRotatedBoundingBox(getXWithOffset(13, 2), getYWithOffset(25), getZWithOffset(13, 2), 29, 3, 18); //anchor offset/world pos already accounted for with offset methods
			int avg = this.getAverageHeight(world, area, box, getYWithOffset(25));
			if(avg >= 0) {
				this.hpos = avg;
				this.boundingBox.move(0, this.hpos - 1 - getYWithOffset(25), 0);
			}
		}`
);

const replacements = [
  ["StructureBoundingBox", "BoundingBox"],
  ["ForgeDirection", "Direction"],
  ["field_151562_a", "blockName"],
  ["World world", "WorldGenLevel world"],
  ["Block block", "String block"],
  ["Block door", "String door"],
];
for (const [a, b] of replacements) {
  body = body.split(a).join(b);
}

body = body.replace(/ModBlocks\.([A-Za-z0-9_]+)/g, '"hbm:tile.$1"');
body = body.replace(/Blocks\.([A-Za-z0-9_]+)/g, '"minecraft:$1"');
body = body.replace(/ItemPool\.getPool\(ItemPools\w+\.([A-Za-z0-9_]+)\)/g, '"$1"');
body = body.replace(
  "\tpublic static WeightedRandomChestContent[] launchKey = new WeightedRandomChestContent[] { new WeightedRandomChestContent(ModItems.launch_key, 0, 1, 1, 1) };",
  '\tpublic static final String launchKey = "POOL_LAUNCH_KEY";'
);
body = body.split("extends BlockSelector").join("extends Component.BlockSelector");

function cutMethod(marker) {
  const idx = body.indexOf(marker);
  if (idx === -1) {
    return;
  }
  let depth = 0;
  let started = false;
  for (let i = idx; i < body.length; i++) {
    const ch = body[i];
    if (ch === "{") {
      depth++;
      started = true;
    } else if (ch === "}") {
      depth--;
      if (started && depth === 0) {
        let end = i + 1;
        if (body[end] === "\r") {
          end++;
        }
        if (body[end] === "\n") {
          end++;
        }
        body = body.slice(0, idx) + body.slice(end);
        return;
      }
    }
  }
}

cutMethod("\tprotected void setRTTYFreq");
cutMethod("\tprotected void fillWithMines");
cutMethod("\tprotected void placeCoreLaunchpad");
cutMethod("\tprotected BoundingBox getRotatedBoundingBox");
if (!body.endsWith("\n")) {
  body += "\n";
}

const header = `package com.hbm.world.gen.component;

import java.util.Random;

import com.hbm.registry.ModStructures;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class SiloComponent extends Component {

	public int freq = 0;
	public int freqHatch = 0;

	public SiloComponent(StructurePieceSerializationContext context, CompoundTag tag) {
		super(ModStructures.SILO_PIECE.get(), tag);
		this.freq = tag.getInt("freq");
		this.freqHatch = tag.getInt("freqHatch");
	}

	public SiloComponent(Random rand, int minX, int minZ) {
		super(ModStructures.SILO_PIECE.get(), rand, minX, 64, minZ, 42, 29, 26);
		this.freq = rand.nextInt();
		this.freqHatch = rand.nextInt();
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		super.addAdditionalSaveData(context, tag);
		tag.putInt("freq", freq);
		tag.putInt("freqHatch", freqHatch);
	}

`;

fs.mkdirSync(path.dirname(dst), { recursive: true });
fs.writeFileSync(dst, header + body);
console.log("wrote", dst, "bytes", header.length + body.length);
console.log("has ConcreteStairs", body.includes("class ConcreteStairs"));
console.log("has SiloSupplies", body.includes("class SiloSupplies"));
console.log("has fillWithMines method", body.includes("protected void fillWithMines"));
