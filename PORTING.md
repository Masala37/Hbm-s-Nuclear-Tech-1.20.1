# HBM Nuclear Tech — 1.20.1 Port Roadmap

This repository is being migrated from **Minecraft 1.7.10 (Forge 10.13)** to **Minecraft 1.20.1 (Forge 47.x)**.

The original 1.7.10 source (~3,400 Java files) is preserved in `legacy-1.7.10/` for reference during the rewrite.

## Current status

- [x] Forge 1.20.1 Gradle toolchain (Java 17, Parchment mappings)
- [x] Mod bootstrap (`HbmNuclearTechMod`, client/server proxies)
- [x] `DeferredRegister` infrastructure for blocks, items, tabs, entities, block entities
- [x] Proof-of-concept content: uranium ore, uranium block, uranium ingot
- [x] Early materials: titanium, tungsten, aluminium, copper, red copper, lead, beryllium, thorium, steel
- [x] RBMK skeleton: vertical column multiblock, passive columns (blank/reflector/absorber/moderator), deco casing
- [x] Core config (`ForgeConfigSpec` GeneralConfig + RBMKConfig)
- [x] Networking scaffold (`SimpleChannel` + ping packet)
- [x] Block/item tags (`ModTags` + datapack JSON, including forge ore/ingot tags)
- [x] Loot tables + mineable/pickaxe tags for registered blocks
- [x] Fluids: coolant/steam ladder, oil products, acids/solvent, petroleum + buckets; placeable liquid blocks for liquids
- [x] First machine: steel fluid barrel (16 buckets, Forge fluid capability, bucket interact)
- [x] Energy scaffold: machine battery (1M FE) + combustion generator (100 FE/t, solid fuels, hopper-capable)
- [x] Machine GUIs: electric furnace, battery, fluid barrel, diesel generator
- [x] Diesel generator (16k mB tank, diesel/gasoline/light oil → FE, redstone-off)
- [x] Red power cable (pull/push FE relay) + electric furnace (20 FE/t smelting)
- [x] Bombs/explosions: ExplosionNT lite + primed bomb entity; dynamite/TNT/semtex/C4
- [x] Little Boy + MK5 batched nuke dig (no fallout/radiation/mushroom yet)
- [x] Fat Man (manRadius 175) with igniter / lenses / core
- [x] The Gadget + remote detonator; shared nuke assembly base; MK5 flash polish
- [x] Mushroom cloud (Torex cloudlets + billboard render) + fallout rain (waste earth)
- [x] Nuke casings use legacy OBJ models (boy/man/gadget/mike)
- [x] Ivy Mike (dual yield: primary manRadius / full mikeRadius 250)
- [x] Nuke assembly schematic GUIs (Boy / Man / Gadget / Mike)
- [x] Combustion generator GUI
- [x] Det cord / charge / nuke charge + AP/HE mines
- [x] Mid-tier parts: plates, wires, circuits, powders, billets/nuggets, specialty ingots
- [x] Specialty materials: cobalt (ore/block/parts), lithium, graphite, desh
- [x] Tsar Bomba (dual yield: primary manRadius / full tsarRadius 500)
- [x] GUI reliability (furnace/gens/nukes) + nuke look-click helper + blast fluid vaporize
- [x] More ores (sulfur/niter/fluorite/lignite/asbestos/rare) + storage (dura/polymer/combine/magnetized/schrar/solinium) + building (brick/concrete/asphalt)
- [x] FLEIJA nuke (assembly GUI + MK5 dig stub at fleijaRadius 50; MK3 fleija dig/cloud still TODO)
- [x] Solinium nuke (assembly GUI + MK5 dig stub at soliniumRadius 150)
- [x] Expanded building/ores/storage (ducrete, reinforced*, gneiss ores, coltan, rare metals) + classic/coated red cable
- [x] Second material/building wave (nether ores, actinides U/Pu blocks, jungle bricks, CMB brick, corium, etc.)
- [x] Cable switch / detector / diode (FE gate + one-way relay)
- [x] Iron/steel storage crates (36/54 slots, vanilla chest GUI)
- [x] Building wave 3: basalt/meteor/factory hulls, colored concrete, lab tiles, depth stone, specialty glass, trinitite, dirt/sand variants
- [x] Building wave 4: more concrete colors, sellafield cubes, deco metals, meteor blocks, quartz/ash glass + expanded parts (ingots/powders/plates/wires/nuggets/billets)
- [x] Building wave 5: steel scaffold/beam/wall, ladders, more concrete, waste/scrap cubes, schrabidium/saturnite/tcalloy/rubber blocks + more powders/plates/billets/coils/scrap
- [x] Building wave 6: fuel/actinide storage cubes, decorative barrels, barbed-wire variants + billets/crystals/fragments/more powders
- [x] 3D models + shapes for ladders, steel scaffold/beam/wall/roof, barbed wire, spikes, barrels, fence, glass pane
- [x] Building wave 7: colored scaffolds, CM casings/sheets/tanks/ports, more concrete/bricks/deco + RTG pellets / specialty ingots / crystals
- [x] Building wave 8: jungle glyphs, CM engines/circuits, basalt/depth ores, meteor variants, gas cells, potato/spark batteries
- [x] Building wave 9: schrabidium/lithium/tikite ores, scorched/nether ores, oil sand, deco appliances + Pu/Po/Tc/Sr ingots, nuggets, RTG pellets, motors/pistons, bottle caps
- [x] Building wave 10: specialty sands, sellafield 5/slaked variants, reinforced ducrete/laminate/stone, stone variants, demon lamp + U/Th fuels, plates, powders, wires
- [x] Building wave 11: ash/scrap/absorbers/caps/charges/clusters/CRTs/crystals/graphite inserts + powders/waste/bottles/parts/empty rods
- [x] Building wave 12: gneiss/frozen/gas cubes, PWR casings, lamps, ladders, mines, laminate pane + stamps/keys/ZIRNOX rods/masks/designators
- [x] Building wave 13: glyphid/toaster/RTG/therm cubes + machine upgrades, solid fuels, warheads/thrusters/missiles, ammo, fluid containers
- [x] Building wave 14: emitter + missile parts/particles/sat chips/gun kits; fixed barbed-wire OBJ + metal fence chain-link models
- [x] Building wave 15: bulk catalog finish (~313 blocks + ~1149 items via ModBulkContent); creative tabs auto-list registries
- [x] Creative tabs restored to legacy 9 (Parts/Control/Template/Blocks/Machine/Nuke/Missile/Weapon/Consumable) with sorted classification
- [x] Bombs tab fill-out: missing bombs ported (multi/float/EMP/fireworks/fissure/crashed/prototype/custom/det_miner/igniter); explosive barrels reclassified; OBJ/cube models fixed
- [x] Multi-Purpose Bomb TE/GUI (4× TNT + dual warheads; gunpowder/TNT/cluster/fire/poison/gas) + multi_kit
- [x] Crashed Bomb duds (4 types + OBJ BER crash pose, defuser loot, detonator blasts) + surface worldgen (dudStructure)
- [x] Bombs helpers Phase 0: ExplosionNT ALLDROP, ExplosionChaos floater/move, ExplosionThermo freeze/scorch/freezer/fire, EMP drain + EMP blast wave entity
- [x] Landmines Phase 1: AP/HE/Shrap/Fat/Naval all use proximity TE + defuser; distinct type blasts (NOBLOCK AP/shrap, HE dig, Fat mini-Torex, Naval water FX)
- [x] Bombs Phases 2–5: sticky charges, specialty (float/EMP/flame/thermo), fireworks+fissure, det cord/charge/miner/nuke
- [x] Explosive barrels Phase 6: red/pink fire+shot, LOX freezer, taint scatter, yellow/vitrified waste+radon (chunk rad deferred)
- [x] Volcano cores + guide Phase 7: ticking cores (5 modes), lava dig/smoke, guide opens nucleartech.wiki
- [x] Nuke FX Phase 8: FLEIJA/Solinium MK3 dig+clouds, moreFallout, Torex nuclear sound + camera shake
- [x] Chunk radiation + entity contamination + Sellafield live decay (5→0→gravel/sand) + hazmat/Geiger
- [x] Bomber flybys (EntityBomber types 0–4) + fallout column undercut + crashed-bomb rad aura + taint spread lite
- [x] Digamma living stack + HazardSystem (RAD/DIGAMMA inventory + tooltips) + typed BombletZeta + Missile MVP (designator/pad/generic)
- [x] Missile depth: Tier-1 warheads, FE launch gate (~75k), size/preset assembly machine
- [x] Missile pad roster: T0–T3 + stealth/decoy (schrab FLEIJA, EMP field, spare huge HE); radar hooks; launch pad + assembly
- [ ] RBMK GUIs / simulation

## Radiation / Sellafield QA checklist

- Place Sellafield_5; Geiger shows rising chunk rad; with shortened `sellafieldTickInterval` decays toward gravel (or sand in desert/badlands).
- Wear full yellow/red/grey hazmat; dose accumulation drops vs naked (resistance on Geiger).
- Detonate a nuke; fallout leaves rad field that spreads to neighbors and drains over time; undercut may collapse unsupported columns inside ~65% radius.
- Bomb caller types 0–4 spawn a bomber flyby and drop payload.
- Yellow barrel / radon / waste earth / toxic fluid add entity dose via Contaminate.
- Crashed bomb TE increments local chunk rad every second; taint can slowly infect soft neighbors.

## Hazard / Digamma / Missile QA checklist

- Hold uranium ingot / nuclear waste: inventory applies RAD; tooltip shows RAD/s; Geiger env rises.
- Hold `particle_digamma` or use `digamma_diagnostic`: digamma dose / DRX readout; max health drops with digamma.
- Bomb caller: whistle + multi-box plane; carpet HE ~4; napalm sets fire; chlorine mist on ground impact.
- Designator RMB on ground → coords in tooltip → RMB designator on launch pad (with coords stored) programs pad.
- Power the pad (≥75k FE via cables/battery) → insert `missile_generic` / incendiary / cluster / buster / strong variants → empty hand or redstone → matching warhead impact.
- Missile assembly: chip + matching-size warhead/thruster + fuselage (+ optional fins) → Assemble → preset missile item.

## Why this is a rewrite, not a version bump

| 1.7.10 API | 1.20.1 equivalent |
|---|---|
| `GameRegistry.registerBlock()` | `DeferredRegister<Block>` |
| `TileEntity` | `BlockEntity` + `BlockEntityType` |
| Numeric block/item IDs | `ResourceLocation` registry keys |
| `CreativeTabs.getNextID()` | `CreativeModeTab.builder()` |
| `World` int coords | `BlockPos`, `Level` |
| `ItemStack` NBT | `CompoundTag` / data components |
| `cpw.mods.fml` events | `net.minecraftforge.eventbus` |
| ISBR / TESR rendering | `BlockEntityRenderer`, baked models |
| Custom fluids (pre-1.13 style) | `ForgeFlowingFluid` + fluid types |
| `PacketDispatcher` | `SimpleChannel` + `FriendlyByteBuf` |

## Recommended port order

Port systems in dependency order so each layer can compile and run:

1. **Core** — config, networking scaffold, creative tabs, materials/tags *(done for early subset)*
2. **Fluids & items** — `Fluids.java`, containers, hazard metadata
3. **Blocks** — ores, machines (start with simple pass-through blocks)
4. **Block entities** — machines, storage, reactors
5. **Recipes** — anvil, assembly, centrifuge, etc. (use JSON/datapack where possible)
6. **Worldgen** — ores, structures, biomes
7. **Entities** — projectiles, mobs, fallout
8. **Hazards & radiation** — chunk radiation manager, contamination
9. **Explosions** — custom explosion algorithms
10. **Rendering** — GUIs, BERs, custom models (OBJ → modern pipeline)
11. **Compat** — JEI, OC, AE2

## Porting a block from legacy

1. Find the legacy class in `legacy-1.7.10/src/main/java/com/hbm/blocks/`
2. Create a 1.20.1 block class under `src/main/java/com/hbm/blocks/`
3. Register it in `ModBlocks` with `DeferredRegister`
4. Add blockstate, model, and texture under `src/main/resources/assets/hbm/`
5. Add lang entry in `assets/hbm/lang/en_us.json`
6. If it has a tile entity, port to `BlockEntity` + register in `ModRegistries.ModBlockEntities`

## Build & run

Requires **Java 17** to run and build (Forge 1.20.1). The Gradle toolchain targets Java 17 class files.

```bat
gradlew.bat build
gradlew.bat runClient
```

## Related projects

- [HbmMods/Hbm-s-Nuclear-Tech-GIT](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) — original 1.7.10 mod
- [Raptor324/HBM-Modernized](https://github.com/Raptor324/HBM-Modernized) — independent 1.20.1 rewrite (pre-alpha)
