package com.hbm.energy;

import java.util.ArrayList;
import java.util.List;

/**
 * Lossless HE-style allocation (1.7.10 {@code PowerNetMK2.update}).
 * This port stores HE as Forge Energy 1:1. Neighbor {@link EnergyNetworkHelper}
 * remains for machine-to-machine adjacency without cables.
 */
public final class PowerNet {
    private PowerNet() {
    }

    public interface Node {
        long offer();

        long demand();

        ConnectionPriority priority();

        long receive(long amount);

        void extract(long amount);
    }

    public static final class BufferNode implements Node {
        public long stored;
        public final long max;
        public final long speed;
        public final ConnectionPriority priority;

        public BufferNode(long stored, long max, long speed, ConnectionPriority priority) {
            this.stored = stored;
            this.max = max;
            this.speed = speed;
            this.priority = priority;
        }

        @Override
        public long offer() {
            return Math.min(stored, speed);
        }

        @Override
        public long demand() {
            return Math.min(Math.max(0, max - stored), speed);
        }

        @Override
        public ConnectionPriority priority() {
            return priority;
        }

        @Override
        public long receive(long amount) {
            long accepted = Math.min(amount, max - stored);
            stored += accepted;
            return accepted;
        }

        @Override
        public void extract(long amount) {
            stored = Math.max(0, stored - amount);
        }
    }

    /**
     * Move energy from providers to receivers, highest priority first, weighted by offer/demand.
     *
     * @return energy actually received
     */
    public static long transfer(List<? extends Node> providers, List<? extends Node> receivers) {
        if (providers == null || receivers == null || providers.isEmpty() || receivers.isEmpty()) {
            return 0L;
        }

        List<Node> src = new ArrayList<>();
        long powerAvailable = 0L;
        for (Node provider : providers) {
            long offer = provider.offer();
            if (offer > 0) {
                src.add(provider);
                powerAvailable += offer;
            }
        }
        if (powerAvailable <= 0) {
            return 0L;
        }

        ConnectionPriority[] bands = ConnectionPriority.values();
        List<List<Node>> byPriority = new ArrayList<>();
        long[] demand = new long[bands.length];
        for (int i = 0; i < bands.length; i++) {
            byPriority.add(new ArrayList<>());
        }
        long totalDemand = 0L;
        for (Node receiver : receivers) {
            long rec = receiver.demand();
            if (rec > 0) {
                int p = receiver.priority().ordinal();
                byPriority.get(p).add(receiver);
                demand[p] += rec;
                totalDemand += rec;
            }
        }
        if (totalDemand <= 0) {
            return 0L;
        }

        long remaining = Math.min(powerAvailable, totalDemand);
        long energyUsed = 0L;
        for (int i = bands.length - 1; i >= 0 && remaining > 0; i--) {
            List<Node> list = byPriority.get(i);
            long priorityDemand = demand[i];
            if (list.isEmpty() || priorityDemand <= 0) {
                continue;
            }
            long bandBudget = Math.min(remaining, priorityDemand);
            long bandUsed = 0L;
            for (Node entry : list) {
                double weight = (double) entry.demand() / (double) priorityDemand;
                long toSend = (long) Math.min(Math.max(bandBudget * weight, 0D), entry.demand());
                long leftover = toSend - entry.receive(toSend);
                bandUsed += (toSend - leftover);
            }
            remaining -= bandUsed;
            energyUsed += bandUsed;
        }

        long leftover = energyUsed;
        for (Node entry : src) {
            double weight = (double) entry.offer() / (double) powerAvailable;
            long toUse = (long) Math.max(energyUsed * weight, 0D);
            toUse = Math.min(toUse, entry.offer());
            entry.extract(toUse);
            leftover -= toUse;
        }
        for (Node entry : src) {
            if (leftover <= 0) {
                break;
            }
            long toUse = Math.min(leftover, entry.offer());
            if (toUse <= 0) {
                continue;
            }
            entry.extract(toUse);
            leftover -= toUse;
        }
        return energyUsed;
    }
}
