# NTM Bombs bugfix plan

Source: legacy gap audit (~85% playable). Thin-gaps 1–8 treated as done.

## Scope (do all)

| # | Item | Severity | Status |
|---|------|----------|--------|
| 1 | Naval mine water dig | Critical | Done — `ExplosionNT.WATER_DIG` + naval dig at +5/+5/+5 |
| 2 | `wasteNoSchrab` + multi poison | Critical | Done — `ExplosionNukeGeneric.wasteNoSchrab` + multi poison path |
| 3 | Chlorine `EntityMist` | Critical | Done — `EntityMist` + multi gas cloud |
| 4 | `ExplosionChaos.cluster` | High | Done — `EntityClusterBomblet` + multi cluster |
| 5 | Persist chunk radiation | High | Done — `ChunkRadiationSavedData` |
| 6 | Deeper fallout stomp | High | Done — sellafield rings 0–50%, wood/leaf rules, trinitite outer sand (no biomes) |
| 7 | Align BombConfig mk5/fatman | High | Done — mk5 default 50, `fatmanRadius`, MK5 floor removed |
| 8 | Promote screwdriver + components | High | Done — items, tooltips, demon-core open/close |

## Out of scope this pass
Full Sellafield biomes, ContamUtil/hazmat, ExplosionVNT full pipeline, missile tab, Boy-length BER for all nukes.

## Done when
All 8 items implemented, jar built + deployed to HBM Tester, user notified.

**Build:** `build/libs/HBM-NTM-1.0.28-1.20.1-port-alpha.jar` (2026-08-10).

**Deploy note:** HBM Tester was running at finish time — do **not** hot-swap the jar (prior `MenuValidity` CNFE). Close the instance, then copy the jar into Prism `mods/`.
