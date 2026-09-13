package com.hbm.blocks.generic;

import com.hbm.inventory.loot.StructureLoot;
import com.hbm.registry.ModItems;
import com.hbm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 1.7 crowbar crates ({@code BlockCrate} / ammo / can). Contents skip missing items.
 */
public class LootCrateBlock extends Block {
    public enum Kind {
        SUPPLY,
        WEAPON,
        LEAD,
        METAL,
        RED,
        AMMO,
        CAN
    }

    private final Kind kind;

    public LootCrateBlock(Kind kind, MapColor color, SoundType sound) {
        this(kind, color, sound, false);
    }

    public LootCrateBlock(Kind kind, MapColor color, SoundType sound, boolean cutout) {
        super(cutout
                ? BlockBehaviour.Properties.of()
                        .mapColor(color)
                        .strength(1.0F, 2.5F)
                        .sound(sound)
                        .noOcclusion()
                : BlockBehaviour.Properties.of()
                        .mapColor(color)
                        .strength(1.0F, 2.5F)
                        .sound(sound));
        this.kind = kind;
    }

    public static LootCrateBlock supply() {
        return new LootCrateBlock(Kind.SUPPLY, MapColor.WOOD, SoundType.WOOD);
    }

    public static LootCrateBlock weapon() {
        return new LootCrateBlock(Kind.WEAPON, MapColor.WOOD, SoundType.WOOD);
    }

    public static LootCrateBlock lead() {
        return new LootCrateBlock(Kind.LEAD, MapColor.METAL, SoundType.METAL);
    }

    public static LootCrateBlock metal() {
        return new LootCrateBlock(Kind.METAL, MapColor.METAL, SoundType.METAL);
    }

    public static LootCrateBlock red() {
        return new LootCrateBlock(Kind.RED, MapColor.COLOR_RED, SoundType.WOOD);
    }

    public static LootCrateBlock ammo() {
        return new LootCrateBlock(Kind.AMMO, MapColor.METAL, SoundType.METAL);
    }

    public static LootCrateBlock can() {
        return new LootCrateBlock(Kind.CAN, MapColor.WOOD, SoundType.WOOD, true);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModItems.CROWBAR.get())) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            StructureLoot.dropCrate(level, pos, kind, level.random);
            level.removeBlock(pos, false);
            level.playSound(null, pos, ModSounds.CRATE_BREAK.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
