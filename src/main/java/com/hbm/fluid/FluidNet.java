package com.hbm.fluid;

import java.util.ArrayList;
import java.util.List;

/**
 * Fair-share fluid allocation matching 1.7.10 {@code FluidNetMK2} (no pressure bands).
 */
public final class FluidNet {
    private FluidNet() {
    }

    public interface Tank {
        int available();

        int room();

        int fill(int amount);

        int drain(int amount);
    }

    public static final class BufferTank implements Tank {
        public int amount;
        public final int capacity;

        public BufferTank(int amount, int capacity) {
            this.amount = amount;
            this.capacity = capacity;
        }

        @Override
        public int available() {
            return Math.max(0, amount);
        }

        @Override
        public int room() {
            return Math.max(0, capacity - amount);
        }

        @Override
        public int fill(int toFill) {
            int accepted = Math.min(toFill, room());
            amount += accepted;
            return accepted;
        }

        @Override
        public int drain(int toDrain) {
            int taken = Math.min(toDrain, amount);
            amount -= taken;
            return taken;
        }
    }

    /**
     * @return millibuckets actually moved
     */
    public static int transfer(List<? extends Tank> providers, List<? extends Tank> receivers) {
        if (providers == null || receivers == null || providers.isEmpty() || receivers.isEmpty()) {
            return 0;
        }
        long supply = 0L;
        List<Tank> src = new ArrayList<>();
        for (Tank tank : providers) {
            int available = tank.available();
            if (available > 0) {
                src.add(tank);
                supply += available;
            }
        }
        long demand = 0L;
        List<Tank> dst = new ArrayList<>();
        for (Tank tank : receivers) {
            int room = tank.room();
            if (room > 0) {
                dst.add(tank);
                demand += room;
            }
        }
        if (supply <= 0L || demand <= 0L) {
            return 0;
        }
        long toMove = Math.min(supply, demand);
        int moved = 0;
        for (Tank tank : dst) {
            double weight = (double) tank.room() / (double) demand;
            int send = (int) Math.min(Math.max(toMove * weight, 0D), tank.room());
            moved += tank.fill(send);
        }
        int leftover = moved;
        for (Tank tank : src) {
            double weight = (double) tank.available() / (double) supply;
            int take = (int) Math.max(moved * weight, 0D);
            leftover -= tank.drain(Math.min(take, tank.available()));
        }
        for (Tank tank : src) {
            if (leftover <= 0) {
                break;
            }
            leftover -= tank.drain(Math.min(leftover, tank.available()));
        }
        return moved;
    }
}
