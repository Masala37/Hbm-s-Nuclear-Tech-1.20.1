#!/usr/bin/env node
/** Extract 1.7.10 anvil / press / shredder / di-furnace / blast-furnace recipes. */
const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..");
const LEGACY = path.join(ROOT, "legacy-1.7.10", "src", "main", "java");
const OUT = path.join(ROOT, "src", "main", "resources", "data", "hbm", "machine_recipes");

const VANILLA_ITEMS = {
  iron_ingot: "minecraft:iron_ingot",
  gold_ingot: "minecraft:gold_ingot",
  gold_nugget: "minecraft:gold_nugget",
  coal: "minecraft:coal",
  redstone: "minecraft:redstone",
  diamond: "minecraft:diamond",
  emerald: "minecraft:emerald",
  quartz: "minecraft:quartz",
  paper: "minecraft:paper",
  clay_ball: "minecraft:clay_ball",
  glowstone_dust: "minecraft:glowstone_dust",
  gunpowder: "minecraft:gunpowder",
  brick: "minecraft:brick",
  flower_pot: "minecraft:flower_pot",
  lava_bucket: "minecraft:lava_bucket",
  blaze_rod: "minecraft:blaze_rod",
  blaze_powder: "minecraft:blaze_powder",
  sugar: "minecraft:sugar",
  reeds: "minecraft:sugar_cane",
  apple: "minecraft:apple",
  carrot: "minecraft:carrot",
  poisonous_potato: "minecraft:poisonous_potato",
  fermented_spider_eye: "minecraft:fermented_spider_eye",
  enchanted_book: "minecraft:enchanted_book",
  dye: "minecraft:ink_sac",
};

const VANILLA_BLOCKS = {
  stone: "minecraft:stone",
  cobblestone: "minecraft:cobblestone",
  gravel: "minecraft:gravel",
  sand: "minecraft:sand",
  dirt: "minecraft:dirt",
  furnace: "minecraft:furnace",
  stonebrick: "minecraft:stone_bricks",
  glowstone: "minecraft:glowstone",
  obsidian: "minecraft:obsidian",
  tnt: "minecraft:tnt",
  clay: "minecraft:clay",
  brick_block: "minecraft:bricks",
  brick_stairs: "minecraft:brick_stairs",
  sandstone: "minecraft:sandstone",
  sandstone_stairs: "minecraft:sandstone_stairs",
  packed_ice: "minecraft:packed_ice",
  quartz_block: "minecraft:quartz_block",
  quartz_ore: "minecraft:nether_quartz_ore",
  quartz_stairs: "minecraft:quartz_stairs",
  coal_block: "minecraft:coal_block",
  diamond_ore: "minecraft:diamond_ore",
  iron_ore: "minecraft:iron_ore",
  gold_ore: "minecraft:gold_ore",
  hardened_clay: "minecraft:terracotta",
  log: "minecraft:oak_log",
};

const SHAPES = {
  ingot: "ingot",
  dust: "dust",
  dustTiny: "dustTiny",
  plate: "plate",
  plateCast: "plateTriple",
  plateWelded: "plateSextuple",
  gem: "gem",
  ore: "ore",
  nugget: "nugget",
  billet: "billet",
  block: "block",
  wireFine: "wireFine",
  wireDense: "wireDense",
  pipe: "ntmpipe",
  shell: "shell",
  crystal: "crystal",
  any: "any",
};

const SKIP_MARKERS = [
  "for(",
  "for (",
  "DictFrame.fromOne",
  "EnumCircuitType",
  "EnumPages",
  "EnumCasingType",
  "EnumBriquetteType",
  "EnumCokeType",
  "EnumStoneType",
  "EnumChunkType",
  "EnumInfusion",
  "EnumBatterySC",
  "mat.id",
  "mat.names",
  "exp ?",
  "GeneralConfig.",
  "AnvilSmithingHotRecipe",
  "AnvilSmithingMold",
  "AnvilSmithingCyanide",
  "AnvilSmithingRename",
  "IMCBlastFurnace",
  "enableLBSM",
  "MaterialShapes.",
  "Fluids.",
  "Compat.isModLoaded",
];

let FRAMES = {};

function readJava(rel) {
  return fs.readFileSync(path.join(LEGACY, rel), "utf8");
}

function stripComments(src) {
  return src.replace(/\/\*[\s\S]*?\*\//g, "").replace(/\/\/.*$/gm, "");
}

function parseDictFrames(src) {
  const frames = {};
  for (const m of src.matchAll(/public static final DictFrame (\w+)\s*=\s*new DictFrame\(\s*"([^"]+)"/g)) {
    frames[m[1]] = m[2];
  }
  for (const m of src.matchAll(/public static final DictGroup (\w+)\s*=\s*new DictGroup\(\s*"([^"]+)"/g)) {
    frames[m[1]] = m[2];
  }
  for (const m of src.matchAll(/public static final String (KEY_\w+)\s*=\s*"([^"]+)"/g)) {
    frames[m[1]] = m[2];
  }
  return frames;
}

function vanillaItem(name, count = 1, meta) {
  if (name === "dye" && meta === "4") return { item: "minecraft:lapis_lazuli", count };
  if (!VANILLA_ITEMS[name]) return null;
  return { item: VANILLA_ITEMS[name], count };
}

function vanillaBlock(name, count = 1) {
  if (!VANILLA_BLOCKS[name]) return null;
  return { item: VANILLA_BLOCKS[name], count };
}

function parseIntToken(expr, fallback = 1) {
  const t = (expr || "").trim();
  if (/^-?\d+$/.test(t)) return parseInt(t, 10);
  return fallback;
}

function oreFromCall(token) {
  token = token.trim();
  if (token.startsWith("KEY_") && FRAMES[token]) return FRAMES[token];
  if (token.startsWith('"') && token.endsWith('"')) return token.slice(1, -1);
  const m = token.match(/^(\w+)\.(\w+)\(\s*\)$/);
  if (m && FRAMES[m[1]] && SHAPES[m[2]]) return SHAPES[m[2]] + FRAMES[m[1]];
  return null;
}

function hasSkip(text) {
  return SKIP_MARKERS.some((s) => text.includes(s));
}

function parseStack(expr) {
  expr = (expr || "").trim().replace(/,$/, "");
  if (!expr || hasSkip(expr)) return null;
  if (expr.startsWith("new OreDictStack(")) {
    const inner = expr.endsWith(")") ? expr.slice("new OreDictStack(".length, -1) : expr.slice("new OreDictStack(".length);
    const parts = splitArgs(inner);
    const ore = oreFromCall(parts[0]);
    if (!ore) return null;
    return { ore, count: parts[1] ? parseIntToken(parts[1], 1) : 1 };
  }
  if (expr.startsWith("new ComparableStack(")) return parseComparable(stripCtor(expr, "new ComparableStack"));
  if (expr.startsWith("new ItemStack(")) return parseItemStackArgs(stripCtor(expr, "new ItemStack"));
  if (expr.startsWith("new AnvilOutput(")) {
    const parts = splitArgs(stripCtor(expr, "new AnvilOutput"));
    const stack = parseStack(parts[0]);
    if (!stack) return null;
    if (parts[1]) {
      const chance = parseFloat(parts[1]);
      if (!Number.isNaN(chance)) stack.chance = chance;
    }
    return stack;
  }
  return parseBareItem(expr);
}

function stripCtor(expr, name) {
  expr = expr.trim();
  const prefix = name + "(";
  if (!expr.startsWith(prefix)) return expr;
  let depth = 0;
  const rest = expr.slice(name.length);
  for (let i = 0; i < rest.length; i++) {
    const ch = rest[i];
    if (ch === "(") depth++;
    else if (ch === ")") {
      depth--;
      if (depth === 0) return expr.slice(name.length + 1, name.length + i);
    }
  }
  return expr.slice(prefix.length, expr.endsWith(")") ? -1 : undefined);
}

function parseComparable(inner) {
  const parts = splitArgs(inner);
  if (!parts.length) return null;
  const count = parts[1] ? parseIntToken(parts[1], 1) : 1;
  if (parts[2] && !/^-?\d+$/.test(parts[2].trim())) return null;
  if (parts[2] && parseIntToken(parts[2], 0) !== 0) return null;
  return parseBareItem(parts[0], count);
}

function parseItemStackArgs(inner) {
  const parts = splitArgs(inner);
  if (!parts.length) return null;
  const count = parts[1] ? parseIntToken(parts[1], 1) : 1;
  if (parts[2]) {
    const meta = parts[2].trim();
    if (meta !== "0") {
      if (parts[0].includes("Items.dye") && meta === "4") return { item: "minecraft:lapis_lazuli", count };
      return null;
    }
  }
  return parseBareItem(parts[0], count);
}

function parseBareItem(token, count = 1) {
  token = token.trim();
  let m = token.match(/^ModItems\.(\w+)$/);
  if (m) return { item: "hbm:" + m[1], count };
  m = token.match(/^ModBlocks\.(\w+)$/);
  if (m) return { item: "hbm:" + m[1], count };
  m = token.match(/^Items\.(\w+)$/);
  if (m) return vanillaItem(m[1], count);
  m = token.match(/^Blocks\.(\w+)$/);
  if (m) return vanillaBlock(m[1], count);
  return null;
}

function splitArgs(s) {
  const args = [];
  let buf = "";
  let depth = 0;
  let inStr = false;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch === '"' && (buf.length === 0 || buf[buf.length - 1] !== "\\")) {
      inStr = !inStr;
      buf += ch;
      continue;
    }
    if (inStr) {
      buf += ch;
      continue;
    }
    if ("([{".includes(ch)) {
      depth++;
      buf += ch;
    } else if (")]}".includes(ch)) {
      depth--;
      buf += ch;
    } else if (ch === "," && depth === 0) {
      args.push(buf.trim());
      buf = "";
    } else {
      buf += ch;
    }
  }
  const tail = buf.trim();
  if (tail) args.push(tail);
  return args;
}

function extractBalanced(src, start) {
  let depth = 0;
  for (let i = start; i < src.length; i++) {
    if (src[i] === "(") depth++;
    else if (src[i] === ")") {
      depth--;
      if (depth === 0) return [src.slice(start, i + 1), i + 1];
    }
  }
  return [src.slice(start), src.length];
}

function parseAStackArray(expr) {
  expr = expr.trim();
  if (!expr.startsWith("new AStack[]")) {
    const one = parseStack(expr);
    return one ? [one] : null;
  }
  const inner = expr.slice(expr.indexOf("{") + 1, expr.lastIndexOf("}"));
  const items = [];
  for (const part of splitArgs(inner)) {
    const stack = parseStack(part);
    if (!stack) return null;
    items.push(stack);
  }
  return items;
}

function dictFrameAnyOf(frame) {
  if (!FRAMES[frame]) return null;
  const mat = FRAMES[frame];
  return { anyOf: ["ingot", "plate", "gem", "dust"].map((prefix) => ({ ore: prefix + mat, count: 1 })) };
}

function writeJson(name, payload) {
  fs.mkdirSync(OUT, { recursive: true });
  const dest = path.join(OUT, name);
  fs.writeFileSync(dest, JSON.stringify(payload, null, 2) + "\n");
  console.log("wrote " + dest + " (" + (payload.recipes || []).length + " recipes, smithing=" + (payload.smithing || []).length + ")");
}

function findAll(src, regex) {
  const out = [];
  regex.lastIndex = 0;
  let m;
  while ((m = regex.exec(src))) out.push(m);
  return out;
}

function extractAnvil() {
  const src = stripComments(readJava("com/hbm/inventory/recipes/anvil/AnvilRecipes.java"));
  const smithing = [];
  for (const m of findAll(src, /smithingRecipes\.add\(\s*new AnvilSmithingRecipe\(/g)) {
    const [call] = extractBalanced(src, m.index + m[0].length - 1);
    const inner = call.slice(1, -1);
    if (hasSkip(call) && !call.includes("new ComparableStack(anvil)")) continue;
    const parts = splitArgs(inner);
    if (parts.length < 4) continue;
    const tier = parseIntToken(parts[0], NaN);
    if (Number.isNaN(tier)) continue;
    const output = parseStack(parts[1]);
    const right = parseStack(parts[3]);
    if (!output || !right) continue;
    if (parts[2].includes("ComparableStack(anvil)")) {
      for (const anvil of ["hbm:anvil_iron", "hbm:anvil_lead"]) {
        smithing.push({ tier, left: { item: anvil, count: 1 }, right, output });
      }
      continue;
    }
    const left = parseStack(parts[2]);
    if (!left) continue;
    smithing.push({ tier, left, right, output });
  }

  const construction = [];
  for (const m of findAll(src, /constructionRecipes\.add\(\s*new AnvilConstructionRecipe\(/g)) {
    const [call, end] = extractBalanced(src, m.index + m[0].length - 1);
    const inner = call.slice(1, -1);
    if (hasSkip(call)) continue;
    const parts = splitArgs(inner);
    if (parts.length < 2) continue;
    const inputs = parseAStackArray(parts[0]);
    const output = parseStack(parts[1]);
    if (!inputs || !output) continue;
    const tail = src.slice(end, end + 180);
    let tier = 1;
    const tm = tail.match(/setTier\((\d+)\)/);
    if (tm) tier = parseInt(tm[1], 10);
    let overlay = null;
    const om = tail.match(/setOverlay\(OverlayType\.(\w+)\)/);
    if (om) overlay = om[1];
    construction.push({ tierLower: tier, tierUpper: -1, overlay, inputs, outputs: [output] });
  }

  writeJson("anvil.json", {
    source: "legacy-1.7.10 AnvilRecipes.registerSmithing/registerConstruction",
    smithing,
    recipes: construction,
  });
}

function extractPress() {
  const src = stripComments(readJava("com/hbm/inventory/recipes/PressRecipes.java"));
  const recipes = [];
  for (const m of findAll(src, /makeRecipe\(\s*StampType\.(\w+)\s*,/g)) {
    const lineEnd = src.indexOf(";", m.index);
    const snippet = src.slice(m.index, lineEnd);
    if (hasSkip(snippet)) continue;
    const [call] = extractBalanced(src, m.index + "makeRecipe".length);
    const parts = splitArgs(call.slice(1, -1));
    if (parts.length < 3) continue;
    const stamp = parts[0].split(".").pop();
    const input = parseStack(parts[1]);
    const output = parseStack(parts[2]) || parseBareItem(parts[2]);
    if (!input || !output) continue;
    recipes.push({ stamp, input, output });
  }
  writeJson("press.json", { source: "legacy-1.7.10 PressRecipes.registerDefaults", recipes });
}

function extractShredder() {
  const src = stripComments(readJava("com/hbm/inventory/recipes/ShredderRecipes.java"));
  const recipes = [];
  for (const m of findAll(src, /ShredderRecipes\.setRecipe\(/g)) {
    const [call] = extractBalanced(src, m.index + m[0].length - 1);
    if (hasSkip(call)) continue;
    const parts = splitArgs(call.slice(1, -1));
    if (parts.length !== 2) continue;
    const input = parseStack(parts[0]) || parseBareItem(parts[0]);
    const output = parseStack(parts[1]) || parseBareItem(parts[1]);
    if (!input || !output) continue;
    recipes.push({ input, output });
  }
  writeJson("shredder.json", { source: "legacy-1.7.10 ShredderRecipes.registerDefaults", recipes });
}

function parseDiInput(token) {
  token = token.trim();
  if (/^[A-Z][A-Z0-9_]*$/.test(token) && FRAMES[token] && !token.startsWith("KEY_")) return dictFrameAnyOf(token);
  const shapeCall = token.match(/^(\w+)\.(\w+)\(\s*\)$/);
  if (shapeCall && FRAMES[shapeCall[1]] && SHAPES[shapeCall[2]]) {
    return { ore: SHAPES[shapeCall[2]] + FRAMES[shapeCall[1]], count: 1 };
  }
  if (token.startsWith('"') && token.endsWith('"')) return { ore: token.slice(1, -1), count: 1 };
  return parseStack(token) || parseBareItem(token);
}

function extractDiFurnace() {
  const src = stripComments(readJava("com/hbm/inventory/recipes/BlastFurnaceRecipes.java"));
  const recipes = [];
  for (const m of findAll(src, /addRecipe\(/g)) {
    if (src.slice(m.index, m.index + 40).includes("Object ")) continue;
    const [call] = extractBalanced(src, m.index + "addRecipe".length);
    if (hasSkip(call)) continue;
    const parts = splitArgs(call.slice(1, -1));
    if (parts.length !== 3) continue;
    const inputA = parseDiInput(parts[0]);
    const inputB = parseDiInput(parts[1]);
    const output = parseStack(parts[2]) || parseBareItem(parts[2]);
    if (!inputA || !inputB || !output) continue;
    recipes.push({ inputA, inputB, output });
  }
  writeJson("di_furnace.json", { source: "legacy-1.7.10 BlastFurnaceRecipes.registerDefaults", recipes });
}

function extractBlastNt() {
  const src = stripComments(readJava("com/hbm/inventory/recipes/BlastFurnaceRecipesNT.java"));
  const recipes = [];
  for (const m of findAll(src, /new BlastFurnaceRecipe\(\s*"([^"]+)"\s*\)/g)) {
    const name = m[1];
    const window = src.slice(m.index, m.index + 900);
    const dm = window.match(/setDuration\(([\d_]+)\)/);
    const duration = dm ? parseInt(dm[1].replace(/_/g, ""), 10) : 400;
    const im = window.match(/inputItems\(([\s\S]*?)\)\s*\n/);
    const om = window.match(/outputItems\(([\s\S]*?)\)\s*\)/);
    if (!im || !om) continue;
    const inputs = [];
    let ok = true;
    for (const part of splitArgs(im[1])) {
      const stack = parseStack(part);
      if (!stack) {
        ok = false;
        break;
      }
      inputs.push(stack);
    }
    const outputs = [];
    for (const part of splitArgs(om[1])) {
      const stack = parseStack(part);
      if (stack) outputs.push(stack);
    }
    if (!ok || !inputs.length || !outputs.length) continue;
    recipes.push({ name, duration, inputItem: inputs, outputItem: outputs });
  }
  writeJson("blast_furnace.json", { source: "legacy-1.7.10 BlastFurnaceRecipesNT.registerDefaults", recipes });
}

FRAMES = parseDictFrames(readJava("com/hbm/inventory/OreDictManager.java"));
extractAnvil();
extractPress();
extractShredder();
extractDiFurnace();
extractBlastNt();
