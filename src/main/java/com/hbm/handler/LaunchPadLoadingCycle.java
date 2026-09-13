package com.hbm.handler;

/** Server-side erector/lift sequence, independent of rendering and world access. */
public final class LaunchPadLoadingCycle {
    private LaunchPadLoadingCycle() {
    }

    public record State(float lift, float erector, int delay, boolean erected,
                        boolean readyToLoad, boolean scheduleErect) {
        public State missileChanged() {
            return new State(lift, erector, 20, false, false, false);
        }
    }

    public static State tick(State previous, boolean missileValid, boolean powered, boolean slow) {
        float lift = previous.lift();
        float erector = previous.erector();
        int delay = missileValid ? previous.delay() : 20;
        boolean erected = missileValid && previous.erected();
        boolean ready = missileValid && (previous.readyToLoad() || (erector == 90.0F && lift == 1.0F));
        boolean scheduled = missileValid && previous.scheduleErect();
        float erectorSpeed = slow ? 0.75F : 1.5F;
        float liftSpeed = slow ? 0.0125F : 0.025F;

        if (powered) {
            boolean retract = false;
            if (delay > 0) {
                delay--;
                if (delay < 10 && scheduled && ready) {
                    erected = true;
                    scheduled = false;
                }
                retract = !missileValid || !ready;
            } else if (!erected && ready) {
                if (erector > 0.0F) {
                    erector = Math.max(erector - erectorSpeed, 0.0F);
                    if (erector == 0.0F) {
                        delay = 20;
                    }
                } else if (lift > 0.0F) {
                    lift = Math.max(lift - liftSpeed, 0.0F);
                    if (lift == 0.0F) {
                        scheduled = true;
                        delay = 20;
                    }
                }
            } else {
                retract = true;
            }
            if (retract) {
                if (erector < 90.0F) {
                    erector = Math.min(erector + erectorSpeed, 90.0F);
                    if (erector == 90.0F) {
                        delay = 20;
                    }
                } else if (lift < 1.0F) {
                    lift = Math.min(lift + liftSpeed, 1.0F);
                    if (lift == 1.0F) {
                        ready = missileValid;
                        delay = 20;
                    }
                }
            }
        }
        return new State(lift, erector, delay, erected, ready, scheduled);
    }
}
