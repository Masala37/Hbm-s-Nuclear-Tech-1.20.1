package com.hbm.blocks;

import net.minecraft.world.level.block.Block;

/**
 * Port of the legacy outgassing ore behavior. Full radon outgas simulation will be
 * reintroduced once the chunk radiation system is ported.
 */
public class OutgasOreBlock extends Block {
    public OutgasOreBlock(Properties properties) {
        super(properties);
    }
}
