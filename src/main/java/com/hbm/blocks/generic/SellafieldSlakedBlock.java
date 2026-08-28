package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Legacy {@code BlockSellafieldSlaked}: crater lining with intensity tint (meta 0–9)
 * and position-hashed texture variants (0–3).
 */
public class SellafieldSlakedBlock extends Block {

    /** Legacy metadata: higher = darker (center rings). */
    public static final IntegerProperty INTENSITY = IntegerProperty.create("intensity", 0, 9);
    /** Legacy world-hash icon pick among sellafield_slaked / _1 / _2 / _3. */
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 3);

    public SellafieldSlakedBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE)
                .isValidSpawn((state, level, pos, type) -> false));
        registerDefaultState(stateDefinition.any()
                .setValue(INTENSITY, 0)
                .setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INTENSITY, VARIANT);
    }

    public BlockState stateFor(int intensity, BlockPos pos) {
        int i = Math.max(0, Math.min(9, intensity));
        return defaultBlockState()
                .setValue(INTENSITY, i)
                .setValue(VARIANT, variantFor(pos));
    }

    /** Same hash as legacy {@code BlockSellafieldSlaked#getIcon(IBlockAccess, ...)}. */
    public static int variantFor(BlockPos pos) {
        long l = (long) (pos.getX() * 3129871) ^ (long) pos.getY() * 116129781L ^ (long) pos.getZ();
        l = l * l * 42317861L + l * 11L;
        int i = (int) (l >> 16 & 3L);
        return Math.floorMod(i, 4);
    }

    /**
     * Legacy {@code Color.HSBtoRGB(0, 0, 1 - meta / 15)} — grayscale brightness by intensity.
     */
    public static int tintColor(int intensity) {
        float brightness = 1.0F - Math.max(0, Math.min(9, intensity)) / 15.0F;
        int v = Math.round(brightness * 255.0F);
        return (v << 16) | (v << 8) | v;
    }
}
