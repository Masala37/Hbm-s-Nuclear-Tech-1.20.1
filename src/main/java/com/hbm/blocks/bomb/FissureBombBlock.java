package com.hbm.blocks.bomb;

import com.hbm.entity.bomb.PrimedBombEntity;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.lib.RefStrings;
import com.hbm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Fissure charge (legacy {@code BlockFissureBomb}): medium muke + bedrock/bedrock-ore conversion.
 */
public class FissureBombBlock extends BombBlock {
    public FissureBombBlock() {
        super(20.0F);
    }

    @Override
    public void explodeEntity(Level level, double x, double y, double z, PrimedBombEntity entity) {
        ExplosionNukeSmall.explode(level, x, y, z, ExplosionNukeSmall.PARAMS_MEDIUM);
        convertBedrockOres(level, x, y, z);
    }

    private static void convertBedrockOres(Level level, double x, double y, double z) {
        int range = 5;
        int ox = (int) Math.floor(x);
        int oy = (int) Math.floor(y);
        int oz = (int) Math.floor(z);

        Block oreBedrock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(RefStrings.MODID, "ore_bedrock"));
        Block volcano = ModBlocks.ORE_VOLCANO.get();
        BlockState volcanoState = volcano.defaultBlockState();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                for (int k = -range; k <= range; k++) {
                    cursor.set(ox + i, oy + j, oz + k);
                    if (!level.isInWorldBounds(cursor)) {
                        continue;
                    }
                    Block block = level.getBlockState(cursor).getBlock();
                    // Vanilla bedrock + legacy bedrock ore → fissure. Oil bedrock ore → plain bedrock.
                    if (block == Blocks.BEDROCK
                            || (oreBedrock != null && oreBedrock != Blocks.AIR && block == oreBedrock)) {
                        level.setBlock(cursor, volcanoState, 3);
                    } else if (block == ModBlocks.ORE_BEDROCK_OIL.get()) {
                        level.setBlock(cursor, Blocks.BEDROCK.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}
