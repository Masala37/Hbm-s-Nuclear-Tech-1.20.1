#!/usr/bin/env python3
"""Size-20 custom-missile parts + anti-ballistic missile OBJ/JSON/skins."""
from __future__ import annotations

import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/hbm"
OBJ_SRC = ROOT / "legacy-1.7.10/src/main/resources/assets/hbm/models/missile_parts"
OBJ_ALT = ASSETS / "models/legacy_raw/missile_parts"
OBJ_DST = ASSETS / "models/obj/missile_parts"
MISSILE_OBJ_DST = ASSETS / "models/obj"
BLOCK_DST = ASSETS / "models/block/missile_part"
MISSILE_BLOCK_DST = ASSETS / "models/block"
ITEM_DST = ASSETS / "models/item"
TEX_DST = ASSETS / "textures/block/missile_parts"
MISSILE_TEX_DST = ASSETS / "textures/block/missile"

TEX_BASE = (
    "https://raw.githubusercontent.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/master"
    "/src/main/resources/assets/hbm/textures/models/"
)

# item_id -> (obj stem, github-relative texture, unique mtl stem, tex dest stem)
PARTS: dict[str, tuple[str, str, str, str]] = {
    "mp_thruster_20_kerosene": (
        "mp_t_20_kerosene", "missile_parts/thrusters/mp_t_20_kerosene.png",
        "mp_t_20_kerosene", "mp_t_20_kerosene"),
    "mp_thruster_20_kerosene_dual": (
        "mp_t_20_kerosene_dual", "missile_parts/thrusters/mp_t_20_kerosene_dual.png",
        "mp_t_20_kerosene_dual", "mp_t_20_kerosene_dual"),
    "mp_thruster_20_kerosene_triple": (
        "mp_t_20_kerosene_triple", "missile_parts/thrusters/mp_t_20_kerosene_dual.png",
        "mp_t_20_kerosene_triple", "mp_t_20_kerosene_dual"),
    "mp_thruster_20_solid": (
        "mp_t_20_solid", "missile_parts/thrusters/mp_t_20_solid.png",
        "mp_t_20_solid", "mp_t_20_solid"),
    "mp_thruster_20_solid_multi": (
        "mp_t_20_solid_multi", "missile_parts/thrusters/mp_t_20_solid_multi.png",
        "mp_t_20_solid_multi", "mp_t_20_solid_multi"),
    "mp_thruster_20_solid_multier": (
        "mp_t_20_solid_multi", "missile_parts/thrusters/mp_t_20_solid_multier.png",
        "mp_t_20_solid_multier", "mp_t_20_solid_multier"),
    "mp_s_20": (
        "mp_s_20", "TheGadget3_.png",
        "mp_s_20", "mp_s_20"),
}


def convert_obj(src: Path, dst: Path, mtl_name: str) -> None:
    text = src.read_text(encoding="utf-8", errors="replace")
    if not text.startswith("mtllib"):
        text = f"mtllib {mtl_name}\nusemtl material\n" + text
    dst.parent.mkdir(parents=True, exist_ok=True)
    dst.write_text(text, encoding="utf-8")


def write_mtl(path: Path, tex_path: str) -> None:
    path.write_text(
        "newmtl material\nKd 1 1 1\nmap_Kd " + tex_path + "\n",
        encoding="utf-8",
    )


def download(url: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    try:
        urllib.request.urlretrieve(url, dest)
        print("ok", dest.name, dest.stat().st_size)
    except Exception as exc:
        print("FAIL", url, exc)
        raise


def write_part_json(item: str, obj: str, mtl: str, tex_stem: str) -> None:
    BLOCK_DST.mkdir(parents=True, exist_ok=True)
    (BLOCK_DST / f"{item}.json").write_text(
        "{\n"
        '  "loader": "forge:obj",\n'
        '  "flip_v": true,\n'
        '  "automatic_culling": false,\n'
        '  "shade_quads": false,\n'
        f'  "model": "hbm:models/obj/missile_parts/{obj}.obj",\n'
        f'  "mtl_override": "hbm:models/obj/missile_parts/{mtl}.mtl",\n'
        "  \"textures\": {\n"
        f'    "particle": "hbm:block/missile_parts/{tex_stem}",\n'
        f'    "material": "hbm:block/missile_parts/{tex_stem}",\n'
        f'    "#material": "hbm:block/missile_parts/{tex_stem}"\n'
        "  }\n"
        "}\n",
        encoding="utf-8",
    )
    (ITEM_DST / f"{item}.json").write_text(
        '{ "parent": "hbm:item/missile_bewlr" }\n', encoding="utf-8"
    )


def write_abm() -> None:
    src = ROOT / "legacy-1.7.10/src/main/resources/assets/hbm/models/missile_abm.obj"
    if not src.exists():
        src = ASSETS / "models/legacy_raw/missile_abm.obj"
    convert_obj(src, MISSILE_OBJ_DST / "missile_abm.obj", "missile_abm.mtl")
    write_mtl(MISSILE_OBJ_DST / "missile_abm.mtl", "hbm:block/missile/missile_abm")
    download(TEX_BASE + "missile/missile_abm.png", MISSILE_TEX_DST / "missile_abm.png")
    MISSILE_BLOCK_DST.mkdir(parents=True, exist_ok=True)
    (MISSILE_BLOCK_DST / "missile_abm.json").write_text(
        "{\n"
        '  "loader": "forge:obj",\n'
        '  "flip_v": true,\n'
        '  "automatic_culling": false,\n'
        '  "shade_quads": false,\n'
        '  "model": "hbm:models/obj/missile_abm.obj",\n'
        '  "mtl_override": "hbm:models/obj/missile_abm.mtl",\n'
        '  "textures": {\n'
        '    "particle": "hbm:block/missile/missile_abm",\n'
        '    "material": "hbm:block/missile/missile_abm",\n'
        '    "#material": "hbm:block/missile/missile_abm"\n'
        "  }\n"
        "}\n",
        encoding="utf-8",
    )
    (ITEM_DST / "missile_anti_ballistic.json").write_text(
        '{ "parent": "hbm:item/missile_bewlr" }\n', encoding="utf-8"
    )


def main() -> None:
    OBJ_DST.mkdir(parents=True, exist_ok=True)
    TEX_DST.mkdir(parents=True, exist_ok=True)
    ITEM_DST.mkdir(parents=True, exist_ok=True)

    objs = {spec[0] for spec in PARTS.values()}
    for stem in sorted(objs):
        src = OBJ_SRC / f"{stem}.obj"
        if not src.exists():
            alt = OBJ_ALT / f"{stem}.obj"
            src = alt if alt.exists() else src
        if not src.exists():
            raise SystemExit(f"missing obj {stem}")
        convert_obj(src, OBJ_DST / f"{stem}.obj", f"{stem}.mtl")

    seen_tex: set[str] = set()
    for item, (obj, rel, mtl, tex_stem) in PARTS.items():
        dest = TEX_DST / f"{tex_stem}.png"
        if rel not in seen_tex:
            download(TEX_BASE + rel, dest)
            seen_tex.add(rel)
        write_mtl(OBJ_DST / f"{mtl}.mtl", f"hbm:block/missile_parts/{tex_stem}")
        write_part_json(item, obj, mtl, tex_stem)

    write_abm()
    print("parts", len(PARTS), "+ abm")


if __name__ == "__main__":
    main()
