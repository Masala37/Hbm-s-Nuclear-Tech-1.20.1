#!/usr/bin/env python3
"""Copy size-10 custom-missile OBJs, download 1.7.10 part textures, emit forge:obj JSONs."""
from __future__ import annotations

import shutil
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/hbm"
OBJ_SRC = ROOT / "legacy-1.7.10/src/main/resources/assets/hbm/models/missile_parts"
OBJ_DST = ASSETS / "models/obj/missile_parts"
BLOCK_DST = ASSETS / "models/block/missile_part"
ITEM_DST = ASSETS / "models/item"
TEX_DST = ASSETS / "textures/block/missile_parts"
SOUND_SRC = ASSETS / "sounds/block/missileAssembly2.ogg"
SOUND_DST = ASSETS / "sounds/block/missile_assembly2.ogg"

TEX_BASE = (
    "https://raw.githubusercontent.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/master"
    "/src/main/resources/assets/hbm/textures/models/missile_parts/"
)

# item_id -> (obj stem, github-relative texture path, height, guiheight)
PARTS: dict[str, tuple[str, str, float, float]] = {
    "mp_thruster_10_kerosene": ("mp_t_10_kerosene", "thrusters/mp_t_10_kerosene.png", 1, 1),
    "mp_thruster_10_solid": ("mp_t_10_solid", "thrusters/mp_t_10_solid.png", 0.5, 1),
    "mp_thruster_10_xenon": ("mp_t_10_xenon", "thrusters/mp_t_10_xenon.png", 0.5, 1),
    "mp_stability_10_flat": ("mp_s_10_flat", "stability/mp_s_10_flat.png", 0, 2),
    "mp_stability_10_cruise": ("mp_s_10_cruise", "stability/mp_s_10_cruise.png", 0, 3),
    "mp_stability_10_space": ("mp_s_10_space", "stability/mp_s_10_space.png", 0, 2),
}

SHORT = "mp_f_10_kerosene"
LONG = "mp_f_10_long_kerosene"
ADAPTER = "mp_f_10_15_kerosene"

SHORT_SKINS = {
    "mp_fuselage_10_kerosene": "fuselages/mp_f_10_kerosene.png",
    "mp_fuselage_10_kerosene_camo": "fuselages/mp_f_10_kerosene_camo.png",
    "mp_fuselage_10_kerosene_desert": "fuselages/mp_f_10_kerosene_desert.png",
    "mp_fuselage_10_kerosene_sky": "fuselages/mp_f_10_kerosene_sky.png",
    "mp_fuselage_10_kerosene_flames": "fuselages/mp_f_10_kerosene_flames.png",
    "mp_fuselage_10_kerosene_insulation": "fuselages/mp_f_10_kerosene_insulation.png",
    "mp_fuselage_10_kerosene_sleek": "fuselages/mp_f_10_kerosene_sleek.png",
    "mp_fuselage_10_kerosene_metal": "fuselages/mp_f_10_kerosene_metal.png",
    "mp_fuselage_10_kerosene_taint": "fuselages/contest/mp_f_10_kerosene_taint.png",
    "mp_fuselage_10_solid": "fuselages/mp_f_10_solid.png",
    "mp_fuselage_10_solid_flames": "fuselages/mp_f_10_solid_flames.png",
    "mp_fuselage_10_solid_insulation": "fuselages/mp_f_10_solid_insulation.png",
    "mp_fuselage_10_solid_sleek": "fuselages/mp_f_10_solid_sleek.png",
    "mp_fuselage_10_solid_soviet_glory": "fuselages/mp_f_10_solid_soviet_glory.png",
    "mp_fuselage_10_solid_cathedral": "fuselages/contest/mp_f_10_solid_cathedral.png",
    "mp_fuselage_10_solid_moonlit": "fuselages/contest/mp_f_10_solid_moonlit.png",
    "mp_fuselage_10_solid_battery": "fuselages/contest/mp_f_10_solid_battery.png",
    "mp_fuselage_10_solid_duracell": "fuselages/mp_f_10_solid_duracell.png",
    "mp_fuselage_10_xenon": "fuselages/mp_f_10_xenon.png",
    "mp_fuselage_10_xenon_bhole": "fuselages/contest/mp_f_10_xenon_bhole.png",
}
LONG_SKINS = {
    "mp_fuselage_10_long_kerosene": "fuselages/mp_f_10_long_kerosene.png",
    "mp_fuselage_10_long_kerosene_camo": "fuselages/mp_f_10_long_kerosene_camo.png",
    "mp_fuselage_10_long_kerosene_desert": "fuselages/mp_f_10_long_kerosene_desert.png",
    "mp_fuselage_10_long_kerosene_sky": "fuselages/mp_f_10_long_kerosene_sky.png",
    "mp_fuselage_10_long_kerosene_flames": "fuselages/mp_f_10_long_kerosene_flames.png",
    "mp_fuselage_10_long_kerosene_insulation": "fuselages/mp_f_10_long_kerosene_insulation.png",
    "mp_fuselage_10_long_kerosene_sleek": "fuselages/mp_f_10_long_kerosene_sleek.png",
    "mp_fuselage_10_long_kerosene_metal": "fuselages/mp_f_10_long_kerosene_metal.png",
    "mp_fuselage_10_long_kerosene_dash": "fuselages/contest/mp_f_10_long_kerosene_dash.png",
    "mp_fuselage_10_long_kerosene_taint": "fuselages/contest/mp_f_10_long_kerosene_taint.png",
    "mp_fuselage_10_long_kerosene_vap": "fuselages/contest/mp_f_10_long_kerosene_vap.png",
    "mp_fuselage_10_long_solid": "fuselages/mp_f_10_long_solid.png",
    "mp_fuselage_10_long_solid_flames": "fuselages/mp_f_10_long_solid_flames.png",
    "mp_fuselage_10_long_solid_insulation": "fuselages/mp_f_10_long_solid_insulation.png",
    "mp_fuselage_10_long_solid_sleek": "fuselages/mp_f_10_long_solid_sleek.png",
    "mp_fuselage_10_long_solid_soviet_glory": "fuselages/mp_f_10_long_solid_soviet_glory.png",
    "mp_fuselage_10_long_solid_bullet": "fuselages/contest/mp_f_10_long_solid_bullet.png",
    "mp_fuselage_10_long_solid_silvermoonlight": "fuselages/contest/mp_f_10_long_solid_silvermoonlight.png",
}
ADAPTER_SKINS = {
    "mp_fuselage_10_15_kerosene": "fuselages/mp_f_10_15_kerosene.png",
    "mp_fuselage_10_15_solid": "fuselages/mp_f_10_15_solid.png",
    "mp_fuselage_10_15_hydrogen": "fuselages/mp_f_10_15_hydrogen.png",
    "mp_fuselage_10_15_balefire": "fuselages/mp_f_10_15_balefire.png",
}
WARHEADS = {
    "mp_warhead_10_he": ("mp_w_10_he", "warheads/mp_w_10_he.png", 2, 1.5),
    "mp_warhead_10_incendiary": ("mp_w_10_incendiary", "warheads/mp_w_10_incendiary.png", 2.5, 2),
    "mp_warhead_10_buster": ("mp_w_10_buster", "warheads/mp_w_10_buster.png", 0.5, 1),
    "mp_warhead_10_nuclear": ("mp_w_10_nuclear", "warheads/mp_w_10_nuclear.png", 2, 1.5),
    "mp_warhead_10_nuclear_large": ("mp_w_10_nuclear_large", "warheads/mp_w_10_nuclear_large.png", 2.5, 1.5),
    "mp_warhead_10_taint": ("mp_w_10_taint", "warheads/mp_w_10_taint.png", 2.25, 1.5),
    "mp_warhead_10_cloud": ("mp_w_10_taint", "warheads/mp_w_10_cloud.png", 2.25, 1.5),
}


def add_fuselages() -> None:
    for item, tex in SHORT_SKINS.items():
        PARTS[item] = (SHORT, tex, 4, 3)
    for item, tex in LONG_SKINS.items():
        PARTS[item] = (LONG, tex, 7, 5)
    for item, tex in ADAPTER_SKINS.items():
        PARTS[item] = (ADAPTER, tex, 9, 5.5)
    PARTS.update(WARHEADS)


def convert_obj(src: Path, dst: Path, mtl_name: str) -> None:
    text = src.read_text(encoding="utf-8", errors="replace")
    if not text.startswith("mtllib"):
        text = f"mtllib {mtl_name}\nusemtl material\n" + text
    dst.write_text(text, encoding="utf-8")
    (dst.parent / mtl_name).write_text(
        "newmtl material\nKd 1 1 1\nmap_Kd hbm:block/missile_parts/" + dst.stem + "\n",
        encoding="utf-8",
    )


def download(rel: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    url = TEX_BASE + rel
    try:
        urllib.request.urlretrieve(url, dest)
        print("ok", dest.name, dest.stat().st_size)
    except Exception as exc:
        print("FAIL", rel, exc)


def write_block_json(item: str, obj: str, tex_stem: str) -> None:
    BLOCK_DST.mkdir(parents=True, exist_ok=True)
    (BLOCK_DST / f"{item}.json").write_text(
        "{\n"
        '  "loader": "forge:obj",\n'
        '  "flip_v": true,\n'
        '  "automatic_culling": false,\n'
        '  "shade_quads": true,\n'
        f'  "model": "hbm:models/obj/missile_parts/{obj}.obj",\n'
        "  \"textures\": {\n"
        f'    "particle": "hbm:block/missile_parts/{tex_stem}",\n'
        f'    "material": "hbm:block/missile_parts/{tex_stem}"\n'
        "  }\n"
        "}\n",
        encoding="utf-8",
    )


def write_item_json(item: str) -> None:
    (ITEM_DST / f"{item}.json").write_text(
        '{ "parent": "hbm:item/missile_bewlr" }\n', encoding="utf-8"
    )


def main() -> None:
    add_fuselages()
    OBJ_DST.mkdir(parents=True, exist_ok=True)
    TEX_DST.mkdir(parents=True, exist_ok=True)
    BLOCK_DST.mkdir(parents=True, exist_ok=True)

    objs = {spec[0] for spec in PARTS.values()}
    for stem in sorted(objs):
        src = OBJ_SRC / f"{stem}.obj"
        if not src.exists():
            alt = ASSETS / "models/legacy_raw/missile_parts" / f"{stem}.obj"
            src = alt if alt.exists() else src
        if not src.exists():
            print("missing obj", stem)
            continue
        convert_obj(src, OBJ_DST / f"{stem}.obj", f"{stem}.mtl")

    seen_tex: set[str] = set()
    for item, (obj, rel, _h, _g) in PARTS.items():
        tex_stem = Path(rel).stem
        dest = TEX_DST / f"{tex_stem}.png"
        if rel not in seen_tex:
            download(rel, dest)
            seen_tex.add(rel)
        # MTL map_Kd is per-OBJ stem; skins share a mesh so JSON texture overrides the bake.
        write_block_json(item, obj, tex_stem)
        write_item_json(item)

    write_item_json("missile_custom")

    if SOUND_SRC.exists() and not SOUND_DST.exists():
        shutil.copy2(SOUND_SRC, SOUND_DST)

    print("parts", len(PARTS))


if __name__ == "__main__":
    main()
