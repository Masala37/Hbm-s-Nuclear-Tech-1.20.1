# Converts 1.7 SiloComponent.java to the 1.20 Component API (string block names).
from pathlib import Path
import re

src = Path("legacy-1.7.10/src/main/java/com/hbm/world/gen/component/SiloComponent.java")
dst = Path("src/main/java/com/hbm/world/gen/component/SiloComponent.java")
text = src.read_text(encoding="utf-8")

# Drop 1.7 header through addComponentParts signature; keep the method body + helpers.
start = text.index("\t@Override\n\tpublic boolean addComponentParts")
body = text[start:]
body = body.replace(
    "\t@Override\n\tpublic boolean addComponentParts(World world, Random rand, StructureBoundingBox box) {",
    "\t@Override\n\tprotected boolean addComponentParts(WorldGenLevel world, Random rand, BoundingBox box) {",
    1,
)

# Only offset the silo once we actually sampled terrain (avoids a -1 hpos jump).
body = body.replace(
    """		if(this.hpos == -1) {
			StructureBoundingBox area = getRotatedBoundingBox(getXWithOffset(13, 2), getYWithOffset(25), getZWithOffset(13, 2), 29, 3, 18); //anchor offset/world pos already accounted for with offset methods
			this.hpos = this.getAverageHeight(world, area, box, getYWithOffset(25));
			this.boundingBox.offset(0, this.hpos - 1 - getYWithOffset(25), 0);
		}""",
    """		if(this.hpos == -1) {
			BoundingBox area = getRotatedBoundingBox(getXWithOffset(13, 2), getYWithOffset(25), getZWithOffset(13, 2), 29, 3, 18); //anchor offset/world pos already accounted for with offset methods
			int avg = this.getAverageHeight(world, area, box, getYWithOffset(25));
			if(avg >= 0) {
				this.hpos = avg;
				this.boundingBox.move(0, this.hpos - 1 - getYWithOffset(25), 0);
			}
		}""",
)

replacements = [
    ("StructureBoundingBox", "BoundingBox"),
    ("ForgeDirection", "Direction"),
    ("field_151562_a", "blockName"),
    ("World world", "WorldGenLevel world"),
    ("Block block", "String block"),
    ("Block door", "String door"),
    (r"this\.boundingBox\.offset", "this.boundingBox.move"),
]
for a, b in replacements:
    body = body.replace(a, b)

body = re.sub(r"ModBlocks\.([A-Za-z0-9_]+)", r'"hbm:tile.\1"', body)
body = re.sub(r"Blocks\.([A-Za-z0-9_]+)", r'"minecraft:\1"', body)
body = re.sub(r"ItemPool\.getPool\(ItemPools\w+\.([A-Za-z0-9_]+)\)", r'"\1"', body)

body = body.replace(
    "\tpublic static WeightedRandomChestContent[] launchKey = new WeightedRandomChestContent[] { new WeightedRandomChestContent(ModItems.launch_key, 0, 1, 1, 1) };",
    '\tpublic static final String launchKey = "POOL_LAUNCH_KEY";',
)

# Inner selectors now store palette names, not Block references.
body = body.replace("extends BlockSelector", "extends Component.BlockSelector")
body = re.sub(
    r"protected void setRTTYFreq\(WorldGenLevel world, BoundingBox box, int featureX, int featureY, int featureZ, int freq\) \{.*?\n\t\}",
    "protected void setRTTYFreq(WorldGenLevel world, BoundingBox box, int featureX, int featureY, int featureZ, int freq) {\n\t\t// Radio torches are not in this port.\n\t}",
    body,
    count=1,
    flags=re.S,
)

# Drop 1.7 NBT + launchpad helpers that Component already implements.
for marker in (
    "\tprotected void setRTTYFreq",
    "\tprotected void fillWithMines",
    "\tprotected void placeCoreLaunchpad",
    "\tprotected BoundingBox getRotatedBoundingBox",
    "\tprotected StructureBoundingBox getRotatedBoundingBox",
):
    idx = body.find(marker)
    if idx != -1 and marker != "\tprotected void setRTTYFreq":
        # keep setRTTYFreq override in SiloComponent? Component already no-ops it.
        pass

# Remove duplicate helpers copied from 1.7 (Component has them).
for pattern in [
    r"\n\tprotected void setRTTYFreq\(.*?\n\t\}\n",
    r"\n\tprotected void fillWithMines\(.*?\n\t\}\n",
    r"\n\tprotected void placeCoreLaunchpad\(.*?\n\t\}\n",
    r"\n\tprotected BoundingBox getRotatedBoundingBox\(.*?\n\t\}\n",
]:
    body = re.sub(pattern, "\n", body, count=1, flags=re.S)

header = '''package com.hbm.world.gen.component;

import java.util.Random;

import com.hbm.registry.ModStructures;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
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

'''

dst.parent.mkdir(parents=True, exist_ok=True)
dst.write_text(header + body, encoding="utf-8")
print("wrote", dst, "chars", len(header) + len(body))
