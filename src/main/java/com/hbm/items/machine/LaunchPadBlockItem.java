package com.hbm.items.machine;

import com.hbm.blocks.machine.DummyablePlacement;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Launch pad block item — 3D silo / erector / rusted in inventory.
 */
public class LaunchPadBlockItem extends BlockItem {
    public LaunchPadBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        DummyablePlacement.begin();
        try {
            return super.place(context);
        } finally {
            DummyablePlacement.end();
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.hbm.client.render.item.MissileItemRenderer.get();
            }
        });
    }
}
