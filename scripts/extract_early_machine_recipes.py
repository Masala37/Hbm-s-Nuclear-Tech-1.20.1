#!/usr/bin/env python3
"""Extract 1.7.10 anvil / press / shredder / di-furnace / blast-furnace recipes.

Does not invent rows. Skips loops, meta/enum stacks, and ternary config branches.
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LEGACY = ROOT / "legacy-1.7.10" / "src" / "main" / "java"
OUT = ROOT / "src" / "main" / "resources" / "data" / "hbm" / "machine_recipes"

VANILLA_ITEMS = {
    "iron_ingot": "minecraft:iron_ingot",
    "gold_ingot": "minecraft:gold_ingot",
    "gold_nugget": "minecraft:gold_nugget",
    "coal": "minecraft:coal",
    "redstone": "minecraft:redstone",
    "diamond": "minecraft:diamond",
    "emerald": "minecraft:emerald",
    "quartz": "minecraft:quartz",
    "paper": "minecraft:paper",
    "clay_ball": "minecraft:clay_ball",
    "glowstone_dust": "minecraft:glowstone_dust",
    "gunpowder": "minecraft:gunpowder",
    "brick": "minecraft:brick",
    "flower_pot": "minecraft:flower_pot",
    "lava_bucket": "minecraft:lava_bucket",
    "blaze_rod": "minecraft:blaze_rod",
    "blaze_powder": "minecraft:blaze_powder",
    "sugar": "minecraft:sugar",
    "reeds": "minecraft:sugar_cane",
    "apple": "minecraft:apple",
    "carrot": "minecraft:carrot",
    "poisonous_potato": "minecraft:poisonous_potato",
    "fermented_spider_eye": "minecraft:fermented_spider_eye",
    "enchanted_book": "minecraft:enchanted_book",
    "dye": "minecraft:ink_sac",
}

VANILLA_BLOCKS = {
    "stone": "minecraft:stone",
    "cobblestone": "minecraft:cobblestone",
    "gravel": "minecraft:gravel",
    "sand": "minecraft:sand",
    "dirt": "minecraft:dirt",
    "furnace": "minecraft:furnace",
    "stonebrick": "minecraft:stone_bricks",
    "glowstone": "minecraft:glowstone",
    "obsidian": "minecraft:obsidian",
    "tnt": "minecraft:tnt",
    "clay": "minecraft:clay",
    "brick_block": "minecraft:bricks",
    "brick_stairs": "minecraft:brick_stairs",
    "sandstone": "minecraft:sandstone",
    "sandstone_stairs": "minecraft:sandstone_stairs",
    "packed_ice": "minecraft:packed_ice",
    "quartz_block": "minecraft:quartz_block",
    "quartz_ore": "minecraft:nether_quartz_ore",
    "netherrack": "minecraft:netherrack",
    "end_stone": "minecraft:end_stone",
    "quartz_stairs": "minecraft:quartz_stairs",
    "coal_block": "minecraft:coal_block",
    "diamond_ore": "minecraft:diamond_ore",
    "iron_ore": "minecraft:iron_ore",
    "gold_ore": "minecraft:gold_ore",
    "hardened_clay": "minecraft:terracotta",
    "log": "minecraft:oak_log",
}

SHAPES = {
    "ingot": "ingot",
    "dust": "dust",
    "dustTiny": "dustTiny",
    "plate": "plate",
    "plateCast": "plateTriple",
    "plateWelded": "plateSextuple",
    "gem": "gem",
    "ore": "ore",
    "nugget": "nugget",
    "billet": "billet",
    "block": "block",
    "wireFine": "wireFine",
    "wireDense": "wireDense",
    "pipe": "ntmpipe",
    "shell": "shell",
    "crystal": "crystal",
    "any": "any",
}

SKIP_MARKERS = (
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
)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def strip_comments(src: str) -> str:
    src = re.sub(r"/\*.*?\*/", "", src, flags=re.S)
    src = re.sub(r"//.*?$", "", src, flags=re.M)
    return src


def parse_dict_frames(src: str) -> dict[str, str]:
    frames = {}
    for m in re.finditer(
        r"public static final DictFrame (\w+)\s*=\s*new DictFrame\(\s*\"([^\"]+)\"",
        src,
    ):
        frames[m.group(1)] = m.group(2)
    for m in re.finditer(
        r"public static final DictGroup (\w+)\s*=\s*new DictGroup\(\s*\"([^\"]+)\"",
        src,
    ):
        frames[m.group(1)] = m.group(2)
    keys = {}
    for m in re.finditer(
        r"public static final String (KEY_\w+)\s*=\s*\"([^\"]+)\"",
        src,
    ):
        keys[m.group(1)] = m.group(2)
    frames.update(keys)
    return frames


def vanilla_item(name: str, count: int = 1, meta: str | None = None) -> dict | None:
    if name == "dye" and meta in ("4",):
        return {"item": "minecraft:lapis_lazuli", "count": count}
    if name not in VANILLA_ITEMS:
        return None
    return {"item": VANILLA_ITEMS[name], "count": count}


def vanilla_block(name: str, count: int = 1) -> dict | None:
    if name not in VANILLA_BLOCKS:
        return None
    return {"item": VANILLA_BLOCKS[name], "count": count}


def parse_int(expr: str, default: int = 1) -> int:
    expr = expr.strip()
    if not expr:
        return default
    m = re.fullmatch(r"-?\d+", expr)
    return int(m.group(0)) if m else default


FRAMES: dict[str, str] = {}


def ore_from_call(token: str) -> str | None:
    token = token.strip()
    if token.startswith("KEY_") and token in FRAMES:
        return FRAMES[token]
    if token.startswith("\"") and token.endswith("\""):
        return token[1:-1]
    m = re.fullmatch(r"(\w+)\.(\w+)\(\s*\)", token)
    if m:
        frame, shape = m.group(1), m.group(2)
        if frame in FRAMES and shape in SHAPES:
            return SHAPES[shape] + FRAMES[frame]
    return None


def parse_stack(expr: str) -> dict | None:
    expr = expr.strip().rstrip(",")
    if not expr or any(s in expr for s in SKIP_MARKERS):
        return None
    if expr.startswith("new OreDictStack("):
        inner = expr[len("new OreDictStack(") : -1] if expr.endswith(")") else expr[len("new OreDictStack(") :]
        parts = split_args(inner)
        ore = ore_from_call(parts[0])
        if not ore:
            return None
        count = parse_int(parts[1], 1) if len(parts) > 1 else 1
        return {"ore": ore, "count": count}
    if expr.startswith("new ComparableStack("):
        inner = strip_ctor(expr, "new ComparableStack")
        return parse_comparable(inner)
    if expr.startswith("new ItemStack("):
        inner = strip_ctor(expr, "new ItemStack")
        return parse_item_stack_args(inner)
    if expr.startswith("new AnvilOutput("):
        inner = strip_ctor(expr, "new AnvilOutput")
        parts = split_args(inner)
        stack = parse_stack(parts[0])
        if stack is None:
            return None
        if len(parts) > 1:
            try:
                stack["chance"] = float(parts[1])
            except ValueError:
                pass
        return stack
    return parse_bare_item(expr)


def strip_ctor(expr: str, name: str) -> str:
    expr = expr.strip()
    prefix = name + "("
    if not expr.startswith(prefix):
        return expr
    depth = 0
    for i, ch in enumerate(expr[len(name) :]):
        if ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
            if depth == 0:
                return expr[len(name) + 1 : len(name) + i]
    return expr[len(prefix) : -1 if expr.endswith(")") else None]


def parse_comparable(inner: str) -> dict | None:
    parts = split_args(inner)
    if not parts:
        return None
    count = parse_int(parts[1], 1) if len(parts) > 1 else 1
    if len(parts) > 2 and not re.fullmatch(r"-?\d+", parts[2].strip()):
        return None
    if len(parts) > 2 and parse_int(parts[2], 0) != 0:
        return None
    return parse_bare_item(parts[0], count)


def parse_item_stack_args(inner: str) -> dict | None:
    parts = split_args(inner)
    if not parts:
        return None
    count = parse_int(parts[1], 1) if len(parts) > 1 else 1
    if len(parts) > 2:
        meta = parts[2].strip()
        if not re.fullmatch(r"0", meta):
            if "Items.dye" in parts[0] and meta == "4":
                return {"item": "minecraft:lapis_lazuli", "count": count}
            return None
    return parse_bare_item(parts[0], count)


def parse_bare_item(token: str, count: int = 1) -> dict | None:
    token = token.strip()
    m = re.fullmatch(r"ModItems\.(\w+)", token)
    if m:
        return {"item": "hbm:" + m.group(1), "count": count}
    m = re.fullmatch(r"ModBlocks\.(\w+)", token)
    if m:
        return {"item": "hbm:" + m.group(1), "count": count}
    m = re.fullmatch(r"Items\.(\w+)", token)
    if m:
        return vanilla_item(m.group(1), count)
    m = re.fullmatch(r"Blocks\.(\w+)", token)
    if m:
        return vanilla_block(m.group(1), count)
    return None


def split_args(s: str) -> list[str]:
    args = []
    buf = []
    depth = 0
    in_str = False
    for ch in s:
        if ch == '"' and (not buf or buf[-1] != "\\"):
            in_str = not in_str
            buf.append(ch)
            continue
        if in_str:
            buf.append(ch)
            continue
        if ch in "([{":
            depth += 1
            buf.append(ch)
        elif ch in ")]}":
            depth -= 1
            buf.append(ch)
        elif ch == "," and depth == 0:
            args.append("".join(buf).strip())
            buf = []
        else:
            buf.append(ch)
    tail = "".join(buf).strip()
    if tail:
        args.append(tail)
    return args


def extract_balanced(src: str, start: int) -> tuple[str, int]:
    depth = 0
    i = start
    while i < len(src):
        if src[i] == "(":
            depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0:
                return src[start : i + 1], i + 1
        i += 1
    return src[start:], len(src)


def parse_astack_array(expr: str) -> list[dict] | None:
    expr = expr.strip()
    if not expr.startswith("new AStack[]"):
        one = parse_stack(expr)
        return [one] if one else None
    inner = expr[expr.find("{") + 1 : expr.rfind("}")]
    items = []
    for part in split_args(inner):
        stack = parse_stack(part)
        if stack is None:
            return None
        items.append(stack)
    return items


def dictframe_any_of(frame: str) -> dict | None:
    if frame not in FRAMES:
        return None
    mat = FRAMES[frame]
    return {
        "anyOf": [
            {"ore": prefix + mat, "count": 1}
            for prefix in ("ingot", "plate", "gem", "dust")
        ]
    }


def write_json(name: str, payload: dict) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / name
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {path} ({len(payload.get('recipes', []))} recipes)")


def extract_anvil() -> None:
    src = strip_comments(read(LEGACY / "com/hbm/inventory/recipes/anvil/AnvilRecipes.java"))
    smithing = []
    for m in re.finditer(r"smithingRecipes\.add\(\s*new AnvilSmithingRecipe\(", src):
        call, _ = extract_balanced(src, m.end() - 1)
        inner = call[1:-1]
        if any(s in call for s in SKIP_MARKERS):
            continue
        parts = split_args(inner)
        if len(parts) < 4:
            continue
        tier = parse_int(parts[0], None)  # type: ignore[arg-type]
        if tier is None:
            continue
        out = parse_stack(parts[1])
        left = parse_stack(parts[2])
        right = parse_stack(parts[3])
        if not out or not left or not right:
            continue
        smithing.append({"tier": tier, "left": left, "right": right, "output": out})

    construction = []
    for m in re.finditer(r"constructionRecipes\.add\(\s*new AnvilConstructionRecipe\(", src):
        call, end = extract_balanced(src, m.end() - 1)
        inner = call[1:-1]
        if any(s in call for s in SKIP_MARKERS):
            continue
        parts = split_args(inner)
        if len(parts) < 2:
            continue
        inputs = parse_astack_array(parts[0])
        output = parse_stack(parts[1])
        if not inputs or not output:
            continue
        tail = src[end : end + 180]
        tier = 1
        tm = re.search(r"setTier\((\d+)\)", tail)
        if tm:
            tier = int(tm.group(1))
        overlay = None
        om = re.search(r"setOverlay\(OverlayType\.(\w+)\)", tail)
        if om:
            overlay = om.group(1)
        construction.append(
            {
                "tierLower": tier,
                "tierUpper": -1,
                "overlay": overlay,
                "inputs": inputs,
                "outputs": [output],
            }
        )

    write_json(
        "anvil.json",
        {
            "source": "legacy-1.7.10 AnvilRecipes.registerSmithing/registerConstruction",
            "smithing": smithing,
            "recipes": construction,
        },
    )


def extract_press() -> None:
    src = strip_comments(read(LEGACY / "com/hbm/inventory/recipes/PressRecipes.java"))
    recipes = []
    for m in re.finditer(r"makeRecipe\(\s*StampType\.(\w+)\s*,", src):
        line_end = src.find(";", m.start())
        snippet = src[m.start() : line_end]
        if any(s in snippet for s in SKIP_MARKERS):
            continue
        call, _ = extract_balanced(src, m.start() + len("makeRecipe") )
        inner = call[1:-1]
        parts = split_args(inner)
        if len(parts) < 3:
            continue
        stamp = parts[0].split(".")[-1]
        inp = parse_stack(parts[1])
        out = parse_stack(parts[2]) or parse_bare_item(parts[2])
        if not inp or not out:
            continue
        recipes.append({"stamp": stamp, "input": inp, "output": out})
    write_json(
        "press.json",
        {"source": "legacy-1.7.10 PressRecipes.registerDefaults", "recipes": recipes},
    )


def extract_shredder() -> None:
    src = strip_comments(read(LEGACY / "com/hbm/inventory/recipes/ShredderRecipes.java"))
    recipes = []
    for m in re.finditer(r"ShredderRecipes\.setRecipe\(", src):
        snippet = src[m.start() : m.start() + 400]
        if any(s in snippet for s in SKIP_MARKERS):
            continue
        call, _ = extract_balanced(src, m.end() - 1)
        inner = call[1:-1]
        parts = split_args(inner)
        if len(parts) != 2:
            continue
        inp = parse_stack(parts[0]) or parse_bare_item(parts[0])
        out = parse_stack(parts[1]) or parse_bare_item(parts[1])
        if not inp or not out:
            continue
        recipes.append({"input": inp, "output": out})
    write_json(
        "shredder.json",
        {"source": "legacy-1.7.10 ShredderRecipes.registerDefaults", "recipes": recipes},
    )


def extract_di_furnace() -> None:
    src = strip_comments(read(LEGACY / "com/hbm/inventory/recipes/BlastFurnaceRecipes.java"))
    recipes = []
    for m in re.finditer(r"addRecipe\(", src):
        snippet = src[m.start() : m.start() + 350]
        if any(s in snippet for s in SKIP_MARKERS):
            continue
        call, _ = extract_balanced(src, m.start() + len("addRecipe"))
        inner = call[1:-1]
        parts = split_args(inner)
        if len(parts) != 3:
            continue
        a = parse_di_input(parts[0])
        b = parse_di_input(parts[1])
        out = parse_stack(parts[2]) or parse_bare_item(parts[2])
        if not a or not b or not out:
            continue
        recipes.append({"inputA": a, "inputB": b, "output": out})
    write_json(
        "di_furnace.json",
        {"source": "legacy-1.7.10 BlastFurnaceRecipes.registerDefaults", "recipes": recipes},
    )


def parse_di_input(token: str) -> dict | None:
    token = token.strip()
    if re.fullmatch(r"[A-Z][A-Z0-9_]*", token) and token in FRAMES and not token.startswith("KEY_"):
        return dictframe_any_of(token)
    return parse_stack(token) or parse_bare_item(token)


def rewrite_lbs_false(expr: str) -> str:
    """Default 1.7 config is LBSM-off, so keep the false branch of `lbs ? A : B`."""
    while True:
        i = expr.find("lbs ?")
        if i < 0:
            i = expr.find("lbs?")
        if i < 0:
            return expr
        q = expr.find("?", i)
        depth = 0
        colon = -1
        for j in range(q + 1, len(expr)):
            ch = expr[j]
            if ch in "([{":
                depth += 1
            elif ch in ")]}":
                depth -= 1
            elif ch == ":" and depth == 0:
                colon = j
                break
        if colon < 0:
            return expr
        depth = 0
        end = len(expr)
        for j in range(colon + 1, len(expr)):
            ch = expr[j]
            if ch in "([{":
                depth += 1
            elif ch in ")]}":
                depth -= 1
                if depth < 0:
                    end = j
                    break
            elif ch == "," and depth == 0:
                end = j
                break
        false_branch = expr[colon + 1 : end].strip()
        expr = expr[:i] + false_branch + expr[end:]


def parse_itemstack_array(expr: str) -> list[dict] | None:
    expr = rewrite_lbs_false(expr.strip())
    brace = expr.find("{")
    if brace < 0:
        one = parse_stack(expr)
        return [one] if one else None
    inner = expr[brace + 1 : expr.rfind("}")]
    items = []
    for part in split_args(inner):
        stack = parse_stack(part) or parse_bare_item(part)
        if stack is None:
            return None
        items.append(stack)
    return items


def extract_centrifuge() -> None:
    src = strip_comments(read(LEGACY / "com/hbm/inventory/recipes/CentrifugeRecipes.java"))
    start = src.find("public void registerDefaults()")
    end = src.find("public void registerPost()")
    body = src[start:end] if start >= 0 and end > start else src
    recipes = []
    idx = 0
    skip_tokens = (
        "EnumBedrockOre",
        "BedrockOreType",
        "ItemBedrockOre",
        "EnumAshType",
        "EnumChunkType",
        "DictFrame",
        "oreCertusQuartz",
        "crystalCertusQuartz",
        "IMCCentrifuge",
    )
    while True:
        m = re.search(r"recipes\.put\(", body[idx:])
        if not m:
            break
        abs_start = idx + m.start()
        call, nxt = extract_balanced(body, abs_start + len("recipes.put"))
        idx = nxt
        inner = rewrite_lbs_false(call[1:-1])
        if any(tok in inner for tok in skip_tokens):
            continue
        parts = split_args(inner)
        if len(parts) != 2:
            continue
        inp = parse_stack(parts[0])
        outs = parse_itemstack_array(parts[1])
        if not inp or not outs:
            continue
        recipes.append({"input": inp, "output": outs})
    write_json(
        "centrifuge.json",
        {"source": "legacy-1.7.10 CentrifugeRecipes.registerDefaults", "recipes": recipes},
    )


def extract_blast_nt() -> None:
    src = strip_comments(read(LEGACY / "com/hbm/inventory/recipes/BlastFurnaceRecipesNT.java"))
    recipes = []
    for m in re.finditer(r"new BlastFurnaceRecipe\(\s*\"([^\"]+)\"\s*\)", src):
        name = m.group(1)
        window = src[m.start() : m.start() + 900]
        if any(s in window for s in ("Enum", "Mats.MAT_", "ingot_raw")):
            # keep recipe but drop unported slag later at load; still record outputs we can parse
            pass
        dm = re.search(r"setDuration\(([\d_]+)\)", window)
        duration = int(dm.group(1).replace("_", "")) if dm else 400
        im = re.search(r"inputItems\((.*?)\)\s*\n", window, re.S)
        om = re.search(r"outputItems\((.*?)\)\s*\)", window, re.S)
        if not im or not om:
            continue
        inputs = []
        ok = True
        for part in split_args(im.group(1)):
            stack = parse_stack(part)
            if stack is None:
                ok = False
                break
            inputs.append(stack)
        outputs = []
        for part in split_args(om.group(1)):
            stack = parse_stack(part)
            if stack is None:
                continue
            outputs.append(stack)
        if not ok or not inputs or not outputs:
            continue
        recipes.append(
            {
                "name": name,
                "duration": duration,
                "inputItem": inputs,
                "outputItem": outputs,
            }
        )
    write_json(
        "blast_furnace.json",
        {"source": "legacy-1.7.10 BlastFurnaceRecipesNT.registerDefaults", "recipes": recipes},
    )


def main() -> None:
    global FRAMES
    FRAMES = parse_dict_frames(read(LEGACY / "com/hbm/inventory/OreDictManager.java"))
    extract_anvil()
    extract_press()
    extract_shredder()
    extract_di_furnace()
    extract_blast_nt()
    extract_centrifuge()


if __name__ == "__main__":
    main()
