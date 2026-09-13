const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
const ASSETS = path.join(ROOT, "src/main/resources/assets/hbm");
const DATA = path.join(ROOT, "src/main/resources/data");

function write(file, text) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, text.replace(/\r\n/g, "\n"), "utf8");
}

function copyObj(src, dest, mtlName) {
  let text = fs.readFileSync(src, "utf8");
  if (!text.startsWith("mtllib ")) {
    text = `mtllib ${mtlName}\nusemtl material\n` + text;
  }
  write(dest, text);
}

function writeMtl(file, texture) {
  write(file, `newmtl material\nKd 1 1 1\nmap_Kd ${texture}\n`);
}

function dummyableBlockstate(model) {
  const lines = ["{\n  \"variants\": {\n"];
  for (let i = 0; i < 16; i++) {
    const comma = i < 15 ? "," : "";
    lines.push(`    "meta=${i}": { "model": "${model}" }${comma}\n`);
  }
  lines.push("  }\n}\n");
  return lines.join("");
}

function dummyableLoot(blockId) {
  const terms = [12, 13, 14, 15].map((meta) =>
    `          { "condition": "minecraft:block_state_property", "block": "${blockId}", "properties": { "meta": "${meta}" } }`
  ).join(",\n");
  return `{
  "type": "minecraft:block",
  "pools": [{
    "rolls": 1,
    "entries": [{ "type": "minecraft:item", "name": "${blockId}" }],
    "conditions": [
      { "condition": "minecraft:survives_explosion" },
      { "condition": "minecraft:any_of", "terms": [
${terms}
      ]}
    ]
  }]
}
`;
}

function simpleLoot(blockId) {
  return `{
  "type": "minecraft:block",
  "pools": [{ "rolls": 1, "entries": [{ "type": "minecraft:item", "name": "${blockId}" }], "conditions": [{ "condition": "minecraft:survives_explosion" }] }]
}
`;
}

function shaped(pattern, key, result) {
  const keyJson = Object.entries(key).map(([k, v]) => `    "${k}": ${v}`).join(",\n");
  const patternJson = pattern.map((row) => `    "${row}"`).join(",\n");
  return `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
${patternJson}
  ],
  "key": {
${keyJson}
  },
  "result": { "item": "${result}" }
}
`;
}

const raw = path.join(ASSETS, "models/legacy_raw");
const objDir = path.join(ASSETS, "models/obj");
copyObj(path.join(raw, "press_body.obj"), path.join(objDir, "press_body.obj"), "press_body.mtl");
copyObj(path.join(raw, "press_head.obj"), path.join(objDir, "press_head.obj"), "press_head.mtl");
copyObj(path.join(raw, "epress_body.obj"), path.join(objDir, "epress_body.obj"), "epress_body.mtl");
copyObj(path.join(raw, "epress_head.obj"), path.join(objDir, "epress_head.obj"), "epress_head.mtl");
copyObj(path.join(raw, "machines/blast_furnace.obj"), path.join(objDir, "blast_furnace.obj"), "blast_furnace.mtl");
writeMtl(path.join(objDir, "press_body.mtl"), "hbm:models/press_body");
writeMtl(path.join(objDir, "press_head.mtl"), "hbm:models/press_head");
writeMtl(path.join(objDir, "epress_body.mtl"), "hbm:models/epress_body");
writeMtl(path.join(objDir, "epress_head.mtl"), "hbm:models/epress_head");
writeMtl(path.join(objDir, "blast_furnace.mtl"), "hbm:models/machines/blast_furnace");

const blockModels = path.join(ASSETS, "models/block");
const itemModels = path.join(ASSETS, "models/item");
const states = path.join(ASSETS, "blockstates");
const loot = path.join(DATA, "hbm/loot_tables/blocks");
const recipes = path.join(DATA, "hbm/recipes");

write(path.join(blockModels, "press_dummy.json"),
  '{ "textures": { "particle": "hbm:models/press_body" }, "elements": [] }\n');
write(path.join(blockModels, "epress_dummy.json"),
  '{ "textures": { "particle": "hbm:models/epress_body" }, "elements": [] }\n');
write(path.join(blockModels, "blast_furnace_dummy.json"),
  '{ "textures": { "particle": "hbm:models/machines/blast_furnace" }, "elements": [] }\n');
write(path.join(blockModels, "press_body.json"), `{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/press_body.obj",
  "textures": { "particle": "hbm:models/press_body" }
}
`);
write(path.join(blockModels, "press_head.json"), `{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/press_head.obj",
  "textures": { "particle": "hbm:models/press_head" }
}
`);
write(path.join(blockModels, "epress_body.json"), `{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/epress_body.obj",
  "textures": { "particle": "hbm:models/epress_body" }
}
`);
write(path.join(blockModels, "epress_head.json"), `{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/epress_head.obj",
  "textures": { "particle": "hbm:models/epress_head" }
}
`);
write(path.join(blockModels, "blast_furnace.json"), `{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/blast_furnace.obj",
  "textures": { "particle": "hbm:models/machines/blast_furnace" }
}
`);
write(path.join(blockModels, "machine_press.json"),
  '{ "parent": "minecraft:block/cube_all", "textures": { "all": "hbm:models/press_body" } }\n');
write(path.join(blockModels, "machine_epress.json"),
  '{ "parent": "minecraft:block/cube_all", "textures": { "all": "hbm:models/epress_body" } }\n');
write(path.join(blockModels, "machine_shredder.json"),
  '{ "parent": "minecraft:block/cube_all", "textures": { "all": "hbm:block/machine_shredder_side_alt" } }\n');
write(path.join(blockModels, "machine_difurnace.json"), `{
  "parent": "minecraft:block/orientable",
  "textures": {
    "top": "hbm:block/difurnace_top_off_alt",
    "front": "hbm:block/difurnace_front_off_alt",
    "side": "hbm:block/difurnace_side_alt"
  }
}
`);
write(path.join(blockModels, "machine_difurnace_on.json"), `{
  "parent": "minecraft:block/orientable",
  "textures": {
    "top": "hbm:block/difurnace_top_on_alt",
    "front": "hbm:block/difurnace_front_on_alt",
    "side": "hbm:block/difurnace_side_alt"
  }
}
`);

write(path.join(states, "machine_press.json"), dummyableBlockstate("hbm:block/press_dummy"));
write(path.join(states, "machine_epress.json"), dummyableBlockstate("hbm:block/epress_dummy"));
write(path.join(states, "machine_blast_furnace.json"), dummyableBlockstate("hbm:block/blast_furnace_dummy"));
write(path.join(states, "machine_shredder.json"), `{
  "variants": {
    "facing=north": { "model": "hbm:block/machine_shredder" },
    "facing=south": { "model": "hbm:block/machine_shredder", "y": 180 },
    "facing=west": { "model": "hbm:block/machine_shredder", "y": 270 },
    "facing=east": { "model": "hbm:block/machine_shredder", "y": 90 }
  }
}
`);
write(path.join(states, "machine_difurnace.json"), `{
  "variants": {
    "facing=north,lit=false": { "model": "hbm:block/machine_difurnace" },
    "facing=south,lit=false": { "model": "hbm:block/machine_difurnace", "y": 180 },
    "facing=west,lit=false": { "model": "hbm:block/machine_difurnace", "y": 270 },
    "facing=east,lit=false": { "model": "hbm:block/machine_difurnace", "y": 90 },
    "facing=north,lit=true": { "model": "hbm:block/machine_difurnace_on" },
    "facing=south,lit=true": { "model": "hbm:block/machine_difurnace_on", "y": 180 },
    "facing=west,lit=true": { "model": "hbm:block/machine_difurnace_on", "y": 270 },
    "facing=east,lit=true": { "model": "hbm:block/machine_difurnace_on", "y": 90 }
  }
}
`);

write(path.join(itemModels, "machine_press.json"), '{ "parent": "hbm:block/machine_press" }\n');
write(path.join(itemModels, "machine_epress.json"), `{
  "parent": "hbm:block/epress_body",
  "display": {
    "gui": { "rotation": [30, 225, 0], "translation": [0, -2, 0], "scale": [0.35, 0.35, 0.35] },
    "ground": { "rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.2, 0.2, 0.2] },
    "fixed": { "rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.3, 0.3, 0.3] },
    "thirdperson_righthand": { "rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.2, 0.2, 0.2] },
    "firstperson_righthand": { "rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.25, 0.25, 0.25] }
  }
}
`);
write(path.join(itemModels, "machine_shredder.json"), '{ "parent": "hbm:block/machine_shredder" }\n');
write(path.join(itemModels, "machine_difurnace.json"), '{ "parent": "hbm:block/machine_difurnace" }\n');
write(path.join(itemModels, "machine_blast_furnace.json"), `{
  "parent": "hbm:block/blast_furnace",
  "display": {
    "gui": { "rotation": [30, 225, 0], "translation": [0, -2, 0], "scale": [0.35, 0.35, 0.35] },
    "ground": { "rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.2, 0.2, 0.2] },
    "fixed": { "rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.3, 0.3, 0.3] },
    "thirdperson_righthand": { "rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.2, 0.2, 0.2] },
    "firstperson_righthand": { "rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.25, 0.25, 0.25] }
  }
}
`);

write(path.join(loot, "machine_press.json"), dummyableLoot("hbm:machine_press"));
write(path.join(loot, "machine_epress.json"), dummyableLoot("hbm:machine_epress"));
write(path.join(loot, "machine_blast_furnace.json"), dummyableLoot("hbm:machine_blast_furnace"));
write(path.join(loot, "machine_shredder.json"), simpleLoot("hbm:machine_shredder"));
write(path.join(loot, "machine_difurnace.json"), simpleLoot("hbm:machine_difurnace"));

const iron = '{ "item": "minecraft:iron_ingot" }';
const ironBlock = '{ "item": "minecraft:iron_block" }';
const furnace = '{ "item": "minecraft:furnace" }';
const piston = '{ "item": "minecraft:piston" }';
const lead = '{ "item": "hbm:ingot_lead" }';
const leadBlock = '{ "item": "hbm:block_lead" }';
const brick = '{ "item": "minecraft:brick" }';
const nether = '{ "item": "minecraft:nether_brick" }';
const stone = '{ "item": "minecraft:stone" }';
const steelP = '{ "item": "hbm:plate_steel" }';
const steelI = '{ "item": "hbm:ingot_steel" }';
const tiP = '{ "item": "hbm:plate_titanium" }';
const tiI = '{ "item": "hbm:ingot_titanium" }';
const deshP = '{ "item": "hbm:plate_desh" }';
const bladesTi = '{ "item": "hbm:blades_titanium" }';
const bladesSt = '{ "item": "hbm:blades_steel" }';

write(path.join(recipes, "machine_press.json"), shaped(["IRI", "IPI", "IBI"], { I: iron, R: furnace, P: piston, B: ironBlock }, "hbm:machine_press"));
write(path.join(recipes, "anvil_iron.json"), shaped(["III", " B ", "III"], { I: iron, B: ironBlock }, "hbm:anvil_iron"));
write(path.join(recipes, "anvil_lead.json"), shaped(["III", " B ", "III"], { I: lead, B: leadBlock }, "hbm:anvil_lead"));
write(path.join(recipes, "stamp_stone_flat.json"), shaped(["III", "SSS"], { I: brick, S: stone }, "hbm:stamp_stone_flat"));
write(path.join(recipes, "stamp_stone_flat_from_nether_brick.json"), shaped(["III", "SSS"], { I: nether, S: stone }, "hbm:stamp_stone_flat"));
write(path.join(recipes, "stamp_iron_flat.json"), shaped(["III", "SSS"], { I: brick, S: iron }, "hbm:stamp_iron_flat"));
write(path.join(recipes, "stamp_iron_flat_from_nether_brick.json"), shaped(["III", "SSS"], { I: nether, S: iron }, "hbm:stamp_iron_flat"));
write(path.join(recipes, "blades_steel.json"), shaped([" P ", "PIP", " P "], { P: steelP, I: steelI }, "hbm:blades_steel"));
write(path.join(recipes, "blades_titanium.json"), shaped([" P ", "PIP", " P "], { P: tiP, I: tiI }, "hbm:blades_titanium"));
write(path.join(recipes, "blades_desh.json"), shaped([" P ", "PBP", " P "], { P: deshP, B: bladesTi }, "hbm:blades_desh"));
write(path.join(recipes, "blades_steel_repair.json"), shaped(["PIP"], { P: steelP, I: bladesSt }, "hbm:blades_steel"));
write(path.join(recipes, "blades_titanium_repair.json"), shaped(["PIP"], { P: tiP, I: bladesTi }, "hbm:blades_titanium"));

const langPath = path.join(ASSETS, "lang/en_us.json");
let lang = fs.readFileSync(langPath, "utf8").replace(/\r\n/g, "\n");
const replacements = {
  '"block.hbm.anvil_arsenic_bronze": "Anvil Arsenic Bronze"': '"block.hbm.anvil_arsenic_bronze": "Arsenic Bronze Anvil"',
  '"block.hbm.anvil_bismuth": "Anvil Bismuth"': '"block.hbm.anvil_bismuth": "Bismuth Anvil"',
  '"block.hbm.anvil_bismuth_bronze": "Anvil Bismuth Bronze"': '"block.hbm.anvil_bismuth_bronze": "Bismuth Bronze Anvil"',
  '"block.hbm.anvil_desh": "Anvil Desh"': '"block.hbm.anvil_desh": "Desh Anvil"',
  '"block.hbm.anvil_dnt": "Anvil Dnt"': '"block.hbm.anvil_dnt": "Dineutronium Anvil"',
  '"block.hbm.anvil_ferrouranium": "Anvil Ferrouranium"': '"block.hbm.anvil_ferrouranium": "Ferrouranium Anvil"',
  '"block.hbm.anvil_iron": "Anvil Iron"': '"block.hbm.anvil_iron": "Iron Anvil"',
  '"block.hbm.anvil_lead": "Anvil Lead"': '"block.hbm.anvil_lead": "Lead Anvil"',
  '"block.hbm.anvil_meteorite": "Anvil Meteorite"': '"block.hbm.anvil_meteorite": "Meteorite Anvil"',
  '"block.hbm.anvil_murky": "Anvil Murky"': '"block.hbm.anvil_murky": "Murky Anvil"',
  '"block.hbm.anvil_osmiridium": "Anvil Osmiridium"': '"block.hbm.anvil_osmiridium": "Osmiridium Anvil"',
  '"block.hbm.anvil_saturnite": "Anvil Saturnite"': '"block.hbm.anvil_saturnite": "Saturnite Anvil"',
  '"block.hbm.anvil_schrabidate": "Anvil Schrabidate"': '"block.hbm.anvil_schrabidate": "Ferric Schrabidate Anvil"',
  '"block.hbm.anvil_starmetal": "Anvil Starmetal"': '"block.hbm.anvil_starmetal": "Starmetal Anvil"',
  '"block.hbm.anvil_steel": "Anvil Steel"': '"block.hbm.anvil_steel": "Steel Anvil"',
  '"block.hbm.machine_press": "Machine Press"': '"block.hbm.machine_press": "Burner Press"',
  '"item.hbm.blades_desh": "Blades Desh"': '"item.hbm.blades_desh": "Desh Shredder Blades"',
  '"item.hbm.blades_steel": "Blades Steel"': '"item.hbm.blades_steel": "Steel Shredder Blades"',
  '"item.hbm.blades_titanium": "Blades Titanium"': '"item.hbm.blades_titanium": "Titanium Shredder Blades"',
  '"item.hbm.stamp_357": "Stamp 357"': '"item.hbm.stamp_357": ".357 Magnum Stamp"',
  '"item.hbm.stamp_357_desh": "Stamp 357 Desh"': '"item.hbm.stamp_357_desh": ".357 Magnum Stamp (Desh)"',
  '"item.hbm.stamp_44": "Stamp 44"': '"item.hbm.stamp_44": ".44 Magnum Stamp"',
  '"item.hbm.stamp_44_desh": "Stamp 44 Desh"': '"item.hbm.stamp_44_desh": ".44 Magnum Stamp (Desh)"',
  '"item.hbm.stamp_50": "Stamp 50"': '"item.hbm.stamp_50": "Large Caliber Stamp"',
  '"item.hbm.stamp_50_desh": "Stamp 50 Desh"': '"item.hbm.stamp_50_desh": "Large Caliber Stamp (Desh)"',
  '"item.hbm.stamp_9": "Stamp 9"': '"item.hbm.stamp_9": "Small Caliber Stamp"',
  '"item.hbm.stamp_9_desh": "Stamp 9 Desh"': '"item.hbm.stamp_9_desh": "Small Caliber Stamp (Desh)"',
  '"item.hbm.stamp_desh_circuit": "Stamp Desh Circuit"': '"item.hbm.stamp_desh_circuit": "Circuit Stamp (Desh)"',
  '"item.hbm.stamp_desh_flat": "Stamp Desh Flat"': '"item.hbm.stamp_desh_flat": "Flat Stamp (Desh)"',
  '"item.hbm.stamp_desh_plate": "Stamp Desh Plate"': '"item.hbm.stamp_desh_plate": "Plate Stamp (Desh)"',
  '"item.hbm.stamp_desh_wire": "Stamp Desh Wire"': '"item.hbm.stamp_desh_wire": "Wire Stamp (Desh)"',
  '"item.hbm.stamp_iron_circuit": "Stamp Iron Circuit"': '"item.hbm.stamp_iron_circuit": "Circuit Stamp (Iron)"',
  '"item.hbm.stamp_iron_flat": "Stamp Iron Flat"': '"item.hbm.stamp_iron_flat": "Flat Stamp (Iron)"',
  '"item.hbm.stamp_iron_plate": "Stamp Iron Plate"': '"item.hbm.stamp_iron_plate": "Plate Stamp (Iron)"',
  '"item.hbm.stamp_iron_wire": "Stamp Iron Wire"': '"item.hbm.stamp_iron_wire": "Wire Stamp (Iron)"',
  '"item.hbm.stamp_obsidian_circuit": "Stamp Obsidian Circuit"': '"item.hbm.stamp_obsidian_circuit": "Circuit Stamp (Obsidian)"',
  '"item.hbm.stamp_obsidian_flat": "Stamp Obsidian Flat"': '"item.hbm.stamp_obsidian_flat": "Flat Stamp (Obsidian)"',
  '"item.hbm.stamp_obsidian_plate": "Stamp Obsidian Plate"': '"item.hbm.stamp_obsidian_plate": "Plate Stamp (Obsidian)"',
  '"item.hbm.stamp_obsidian_wire": "Stamp Obsidian Wire"': '"item.hbm.stamp_obsidian_wire": "Wire Stamp (Obsidian)"',
  '"item.hbm.stamp_steel_circuit": "Stamp Steel Circuit"': '"item.hbm.stamp_steel_circuit": "Circuit Stamp (Steel)"',
  '"item.hbm.stamp_steel_flat": "Stamp Steel Flat"': '"item.hbm.stamp_steel_flat": "Flat Stamp (Steel)"',
  '"item.hbm.stamp_steel_plate": "Stamp Steel Plate"': '"item.hbm.stamp_steel_plate": "Plate Stamp (Steel)"',
  '"item.hbm.stamp_steel_wire": "Stamp Steel Wire"': '"item.hbm.stamp_steel_wire": "Wire Stamp (Steel)"',
  '"item.hbm.stamp_stone_circuit": "Stamp Stone Circuit"': '"item.hbm.stamp_stone_circuit": "Circuit Stamp (Stone)"',
  '"item.hbm.stamp_stone_flat": "Stamp Stone Flat"': '"item.hbm.stamp_stone_flat": "Flat Stamp (Stone)"',
  '"item.hbm.stamp_stone_plate": "Stamp Stone Plate"': '"item.hbm.stamp_stone_plate": "Plate Stamp (Stone)"',
  '"item.hbm.stamp_stone_wire": "Stamp Stone Wire"': '"item.hbm.stamp_stone_wire": "Wire Stamp (Stone)"',
  '"item.hbm.stamp_titanium_circuit": "Stamp Titanium Circuit"': '"item.hbm.stamp_titanium_circuit": "Circuit Stamp (Titanium)"',
  '"item.hbm.stamp_titanium_flat": "Stamp Titanium Flat"': '"item.hbm.stamp_titanium_flat": "Flat Stamp (Titanium)"',
  '"item.hbm.stamp_titanium_plate": "Stamp Titanium Plate"': '"item.hbm.stamp_titanium_plate": "Plate Stamp (Titanium)"',
  '"item.hbm.stamp_titanium_wire": "Stamp Titanium Wire"': '"item.hbm.stamp_titanium_wire": "Wire Stamp (Titanium)"'
};
for (const [old, next] of Object.entries(replacements)) {
  if (lang.includes(next)) {
    continue;
  }
  if (!lang.includes(old)) {
    throw new Error("missing lang key: " + old);
  }
  lang = lang.replace(old, next);
}

const inserts = [
  ['  "block.hbm.machine_battery_infinite": "Infinite Battery",\n', '  "block.hbm.machine_blast_furnace": "Blast Furnace",\n'],
  ['  "block.hbm.machine_detector_off": "Machine Detector Off",\n', '  "block.hbm.machine_difurnace": "Blast Furnace",\n'],
  ['  "block.hbm.machine_shredder_bottom_alt": "Machine Shredder Bottom Alt",\n', '  "block.hbm.machine_shredder": "Shredder",\n'],
  ['  "container.missileAssembly": "Missile Assembly Station",\n',
    '  "container.anvil": "Tier %s Anvil",\n  "container.press": "Burner Press",\n  "container.machineShredder": "Shredder",\n  "container.diFurnace": "Blast Furnace",\n  "container.blastFurnace": "Blast Furnace",\n']
];
for (const [needle, extra] of inserts) {
  if (lang.includes(extra)) {
    continue;
  }
  if (!lang.includes(needle)) {
    throw new Error("missing insert point: " + needle);
  }
  lang = lang.replace(needle, extra + needle);
}

write(langPath, lang);
console.log("early machine assets written");
