package com.hbm.world.feature;

import java.util.Random;

/**
 * 1.7 {@code NoiseGeneratorPerlin} (4 octaves) used by {@code OreLayer3D}.
 * <p>
 * Vanilla 4-octave Perlin is about {@code [-2, 2]}, so a product never reaches the 1.7
 * thresholds (230–300). 1.7 ImprovedNoise seeds offsets in {@code 0..256}; deposits were
 * common enough that players asked for a disable toggle. Scale by that permutation range
 * so the same thresholds form large strata.
 */
public final class OreLayer3DNoise {
    public static final double THRESHOLD_SCALE = 256.0D;
    public static final int MAX_Y = 64;
    /** 1.7 loop was {@code y > 5}; shifted by the 1.18 world-bottom change (−64). */
    public static final int MIN_Y_EXCLUSIVE = -59;
    private static final int OCTAVES = 4;

    private final ImprovedNoise[] octaves;

    public OreLayer3DNoise(long seed) {
        Random random = new Random(seed);
        this.octaves = new ImprovedNoise[OCTAVES];
        for (int i = 0; i < OCTAVES; i++) {
            this.octaves[i] = new ImprovedNoise(random);
        }
    }

    public double sample(double x, double y) {
        double sum = 0.0D;
        double amplitude = 1.0D;
        for (int i = 0; i < OCTAVES; i++) {
            sum += this.octaves[i].sample2d(x * amplitude, y * amplitude) / amplitude;
            amplitude /= 2.0D;
        }
        return sum * THRESHOLD_SCALE;
    }

    public static boolean inside(double nx, double ny, double nz, double threshold) {
        return nx * ny * nz > threshold;
    }

    static final class ImprovedNoise {
        private final int[] permutations = new int[512];
        private final double xCoord;
        private final double yCoord;
        private final double zCoord;

        ImprovedNoise(Random random) {
            this.xCoord = random.nextDouble() * 256.0D;
            this.yCoord = random.nextDouble() * 256.0D;
            this.zCoord = random.nextDouble() * 256.0D;
            for (int i = 0; i < 256; i++) {
                this.permutations[i] = i;
            }
            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256 - i) + i;
                int swap = this.permutations[i];
                this.permutations[i] = this.permutations[j];
                this.permutations[j] = swap;
                this.permutations[i + 256] = this.permutations[i];
            }
        }

        double sample2d(double x, double y) {
            return sample3d(x, y, 0.0D);
        }

        private double sample3d(double x, double y, double z) {
            double dx = x + this.xCoord;
            double dy = y + this.yCoord;
            double dz = z + this.zCoord;
            int ix = floor(dx);
            int iy = floor(dy);
            int iz = floor(dz);
            dx -= ix;
            dy -= iy;
            dz -= iz;
            // 1.7 NoiseGeneratorImproved: wrap lattice after subtracting integer parts.
            ix &= 255;
            iy &= 255;
            iz &= 255;
            double u = fade(dx);
            double v = fade(dy);
            double w = fade(dz);
            int a = this.permutations[ix] + iy;
            int aa = this.permutations[a] + iz;
            int ab = this.permutations[a + 1] + iz;
            int b = this.permutations[ix + 1] + iy;
            int ba = this.permutations[b] + iz;
            int bb = this.permutations[b + 1] + iz;
            return lerp(w,
                    lerp(v,
                            lerp(u, grad(this.permutations[aa], dx, dy, dz),
                                    grad(this.permutations[ba], dx - 1.0D, dy, dz)),
                            lerp(u, grad(this.permutations[ab], dx, dy - 1.0D, dz),
                                    grad(this.permutations[bb], dx - 1.0D, dy - 1.0D, dz))),
                    lerp(v,
                            lerp(u, grad(this.permutations[aa + 1], dx, dy, dz - 1.0D),
                                    grad(this.permutations[ba + 1], dx - 1.0D, dy, dz - 1.0D)),
                            lerp(u, grad(this.permutations[ab + 1], dx, dy - 1.0D, dz - 1.0D),
                                    grad(this.permutations[bb + 1], dx - 1.0D, dy - 1.0D, dz - 1.0D))));
        }

        private static double fade(double t) {
            return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
        }

        private static double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }

        private static double grad(int hash, double x, double y, double z) {
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : (h != 12 && h != 14 ? z : x);
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }

        private static int floor(double value) {
            int i = (int) value;
            return value < i ? i - 1 : i;
        }
    }
}
