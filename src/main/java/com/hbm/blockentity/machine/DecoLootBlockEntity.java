package com.hbm.blockentity.machine;

import com.hbm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** 1.7 {@code TileEntityLoot}: item stacks with floor offsets. */
public class DecoLootBlockEntity extends BlockEntity {
    private final List<Entry> items = new ArrayList<>();

    public DecoLootBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECO_LOOT.get(), pos, state);
    }

    public List<Entry> items() {
        return items;
    }

    public void addItem(ItemStack stack, double x, double y, double z) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        items.add(new Entry(stack.copy(), x, y, z));
        sync();
    }

    public void setItems(List<ItemStack> stacks) {
        setItems(stacks, RandomSource.create(worldPosition.asLong()));
    }

    public void setItems(List<ItemStack> stacks, RandomSource random) {
        items.clear();
        if (stacks == null) {
            sync();
            return;
        }
        int i = 0;
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            double x = random.nextDouble() - 0.5D;
            double y = i * 0.03125D;
            double z = random.nextDouble() - 0.5D;
            items.add(new Entry(stack.copy(), x, y, z));
            i++;
        }
        sync();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (Entry entry : items) {
            Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, entry.stack());
        }
        items.clear();
    }

    private void sync() {
        setChanged();
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = worldPosition;
        return new AABB(pos.getX() - 1.0D, pos.getY(), pos.getZ() - 1.0D,
                pos.getX() + 2.0D, pos.getY() + 3.0D, pos.getZ() + 2.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeItems(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        if (tag.contains("count")) {
            int count = tag.getInt("count");
            for (int i = 0; i < count; i++) {
                ItemStack stack = ItemStack.of(tag.getCompound("item" + i));
                if (stack.isEmpty()) {
                    continue;
                }
                items.add(new Entry(stack, tag.getDouble("x" + i), tag.getDouble("y" + i), tag.getDouble("z" + i)));
            }
            return;
        }
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag item = list.getCompound(i);
            ItemStack stack = ItemStack.of(item);
            if (stack.isEmpty()) {
                continue;
            }
            items.add(new Entry(stack, item.getDouble("x"), item.getDouble("y"), item.getDouble("z")));
        }
    }

    private void writeItems(CompoundTag tag) {
        tag.putInt("count", items.size());
        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            Entry entry = items.get(i);
            CompoundTag stack = new CompoundTag();
            entry.stack().save(stack);
            tag.put("item" + i, stack);
            tag.putDouble("x" + i, entry.x());
            tag.putDouble("y" + i, entry.y());
            tag.putDouble("z" + i, entry.z());
            CompoundTag listed = stack.copy();
            listed.putDouble("x", entry.x());
            listed.putDouble("y", entry.y());
            listed.putDouble("z", entry.z());
            list.add(listed);
        }
        tag.put("Items", list);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeItems(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    public record Entry(ItemStack stack, double x, double y, double z) {
    }
}
