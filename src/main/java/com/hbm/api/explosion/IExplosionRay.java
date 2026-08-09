package com.hbm.api.explosion;

/**
 * Procedural multi-tick explosion dig (legacy IExplosionRay).
 */
public interface IExplosionRay {
    void cacheChunksTick(int processTimeMs);

    void destructionTick(int processTimeMs);

    void cancel();

    boolean isComplete();
}
