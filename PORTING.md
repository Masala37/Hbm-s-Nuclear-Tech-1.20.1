# HBM Nuclear Tech — 1.20.1 Port Roadmap

This repository is being migrated from **Minecraft 1.7.10 (Forge 10.13)** to **Minecraft 1.20.1 (Forge 47.x)**.

The original 1.7.10 source (~3,400 Java files) is preserved in `legacy-1.7.10/` for reference during the rewrite.

## Current status

- [x] Forge 1.20.1 Gradle toolchain (Java 17, Parchment mappings)
- [x] Mod bootstrap (`HbmNuclearTechMod`, client/server proxies)
- [x] `DeferredRegister` infrastructure for blocks, items, tabs, entities, block entities
- [x] Proof-of-concept content: uranium ore, uranium block, uranium ingot
- [x] RBMK skeleton: vertical column multiblock, passive columns (blank/reflector/absorber/moderator), deco casing
- [ ] RBMK simulation: fuel rods, control rods, neutron flux, heat, meltdown

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

1. **Core** — config, networking scaffold, creative tabs, materials/tags
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

Requires **Java 17+** (Java 21 works).

```bat
gradlew.bat build
gradlew.bat runClient
```

## Related projects

- [HbmMods/Hbm-s-Nuclear-Tech-GIT](https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT) — original 1.7.10 mod
- [Raptor324/HBM-Modernized](https://github.com/Raptor324/HBM-Modernized) — independent 1.20.1 rewrite (pre-alpha)
