package com.hbm.world.gen.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Weighted set of jigsaw pieces plus an optional fallback pool name.
 */
public final class JigsawPool {
    public record Entry(JigsawPiece piece, int weight) {
    }

    private final List<Entry> pieces = new ArrayList<>();
    private int totalWeight;
    public String fallback;
    private boolean clone;

    public JigsawPool add(JigsawPiece piece, int weight) {
        if (weight <= 0) {
            throw new IllegalStateException("Jigsaw pool weight must be positive");
        }
        pieces.add(new Entry(piece, weight));
        totalWeight += weight;
        return this;
    }

    public JigsawPool fallback(String name) {
        this.fallback = name;
        return this;
    }

    public int totalWeight() {
        return totalWeight;
    }

    public List<Entry> pieces() {
        return pieces;
    }

    public JigsawPool copy() {
        JigsawPool copy = new JigsawPool();
        copy.pieces.addAll(this.pieces);
        copy.fallback = this.fallback;
        copy.totalWeight = this.totalWeight;
        copy.clone = true;
        return copy;
    }

    public JigsawPiece get(Random random) {
        if (totalWeight <= 0) {
            return null;
        }
        int weight = random.nextInt(totalWeight);
        for (int i = 0; i < pieces.size(); i++) {
            Entry entry = pieces.get(i);
            weight -= entry.weight;
            if (weight < 0) {
                if (clone) {
                    pieces.remove(i);
                    totalWeight -= entry.weight;
                }
                return entry.piece;
            }
        }
        return null;
    }
}
