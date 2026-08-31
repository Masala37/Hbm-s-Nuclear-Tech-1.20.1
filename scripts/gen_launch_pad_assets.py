#!/usr/bin/env python3
"""Split launch_pad_erector.obj and emit Forge OBJ JSON + 9x9 blockstate."""
from __future__ import annotations

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC_OBJ = ROOT / "src/main/resources/assets/hbm/models/legacy_raw/weapons/launch_pad_erector.obj"
OBJ_OUT = ROOT / "src/main/resources/assets/hbm/models/obj"
JSON_OUT = ROOT / "src/main/resources/assets/hbm/models/block"
BS_OUT = ROOT / "src/main/resources/assets/hbm/blockstates"
TEX_SRC = ROOT / "src/main/resources/assets/hbm/textures/models/launchpad"
TEX_DST = ROOT / "src/main/resources/assets/hbm/textures/block/launchpad"

PARTS = [
    "Pad",
    "V2_Pad", "V2_Erector", "V2_Pivot", "V2_Rope",
    "Micro_Pad", "Micro_Erector", "Micro_Pivot", "Micro_Rope",
    "Strong_Pad", "Strong_Erector", "Strong_Pivot", "Strong_Rope",
    "Huge_Pad", "Huge_Erector", "Huge_Pivot", "Huge_Rope",
    "Atlas_Pad", "Atlas_Erector", "Atlas_Pivot", "Atlas_Rope",
    "ABM_Pad", "ABM_Erector", "ABM_Pivot", "ABM_Rope",
]

TEXTURE_FOR = {
    "Pad": "pad",
    "V2_Pad": "erector_v2", "V2_Erector": "erector_v2", "V2_Pivot": "erector_v2", "V2_Rope": "erector_v2",
    "Micro_Pad": "erector_micro", "Micro_Erector": "erector_micro", "Micro_Pivot": "erector_micro", "Micro_Rope": "erector_micro",
    "Strong_Pad": "erector_strong", "Strong_Erector": "erector_strong", "Strong_Pivot": "erector_strong", "Strong_Rope": "erector_strong",
    "Huge_Pad": "erector_huge", "Huge_Erector": "erector_huge", "Huge_Pivot": "erector_huge", "Huge_Rope": "erector_huge",
    "Atlas_Pad": "erector_atlas", "Atlas_Erector": "erector_atlas", "Atlas_Pivot": "erector_atlas", "Atlas_Rope": "erector_atlas",
    "ABM_Pad": "erector_abm", "ABM_Erector": "erector_abm", "ABM_Pivot": "erector_abm", "ABM_Rope": "erector_abm",
}


def slug(name: str) -> str:
    return "launch_pad_erector_" + name.lower()


def parse_obj(path: Path) -> dict[str, dict]:
    verts: list[str] = []
    uvs: list[str] = []
    norms: list[str] = []
    objects: dict[str, dict] = {}
    current = None
    for raw in path.read_text(encoding="utf-8", errors="replace").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("o "):
            current = line[2:].strip()
            objects[current] = {"faces": [], "s": []}
            continue
        if line.startswith("v "):
            verts.append(line)
            continue
        if line.startswith("vt "):
            uvs.append(line)
            continue
        if line.startswith("vn "):
            norms.append(line)
            continue
        if current is None:
            continue
        if line.startswith("s "):
            objects[current]["s"].append(line)
            continue
        if line.startswith("f "):
            objects[current]["faces"].append(line)
    return {"v": verts, "vt": uvs, "vn": norms, "o": objects}


def face_indices(token: str) -> tuple[int, int, int]:
    parts = token.split("/")
    v = int(parts[0]) if parts[0] else 0
    vt = int(parts[1]) if len(parts) > 1 and parts[1] else 0
    vn = int(parts[2]) if len(parts) > 2 and parts[2] else 0
    return v, vt, vn


def write_part(name: str, data: dict, out_path: Path) -> None:
    obj = data["o"][name]
    used_v: dict[int, int] = {}
    used_vt: dict[int, int] = {}
    used_vn: dict[int, int] = {}

    def map_idx(store: dict[int, int], idx: int) -> int:
        if idx == 0:
            return 0
        if idx not in store:
            store[idx] = len(store) + 1
        return store[idx]

    remapped_faces: list[str] = []
    for face in obj["faces"]:
        tokens = face.split()[1:]
        mapped = []
        for tok in tokens:
            v, vt, vn = face_indices(token=tok)
            mapped.append(f"{map_idx(used_v, v)}/{map_idx(used_vt, vt)}/{map_idx(used_vn, vn)}")
        remapped_faces.append("f " + " ".join(mapped))

    lines = ["# split from launch_pad_erector.obj", f"o {name}"]
    for old in sorted(used_v, key=lambda i: used_v[i]):
        lines.append(data["v"][old - 1])
    for old in sorted(used_vt, key=lambda i: used_vt[i]):
        lines.append(data["vt"][old - 1])
    for old in sorted(used_vn, key=lambda i: used_vn[i]):
        lines.append(data["vn"][old - 1])
    lines.append("s off")
    lines.extend(remapped_faces)
    out_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def write_json(name: str) -> None:
    tex = TEXTURE_FOR[name]
    payload = {
        "loader": "forge:obj",
        "flip_v": True,
        "automatic_culling": False,
        "shade_quads": False,
        "model": f"hbm:models/obj/{slug(name)}.obj",
        "textures": {
            "particle": f"hbm:block/launchpad/{tex}",
            "material": f"hbm:block/launchpad/{tex}",
        },
    }
    (JSON_OUT / f"{slug(name)}.json").write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def write_silo_rusted_json() -> None:
    payload = {
        "loader": "forge:obj",
        "flip_v": True,
        "automatic_culling": False,
        "shade_quads": False,
        "model": "hbm:models/obj/launch_pad_silo.obj",
        "textures": {
            "particle": "hbm:block/launchpad/silo_rusted",
            "material": "hbm:block/launchpad/silo_rusted",
        },
    }
    (JSON_OUT / "launch_pad_silo_rusted.json").write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def write_large_blockstate() -> None:
    variants = {}
    for ox in range(9):
        for oz in range(9):
            for facing, y in (("north", 90), ("south", 270), ("west", 180), ("east", 0)):
                key = f"facing={facing},ox={ox},oz={oz}"
                variants[key] = {"model": "hbm:block/launch_pad_dummy"}
    (BS_OUT / "launch_pad_large.json").write_text(
        json.dumps({"variants": variants}, indent=2) + "\n", encoding="utf-8"
    )


def write_rusted_blockstate() -> None:
    variants = {}
    for ox in range(3):
        for oz in range(3):
            model = "hbm:block/launch_pad_dummy"
            variants[f"ox={ox},oz={oz}"] = {"model": model}
    (BS_OUT / "launch_pad_rusted.json").write_text(
        json.dumps({"variants": variants}, indent=2) + "\n", encoding="utf-8"
    )


def copy_textures() -> None:
    TEX_DST.mkdir(parents=True, exist_ok=True)
    for name in (
        "pad.png", "erector_v2.png", "erector_micro.png", "erector_strong.png",
        "erector_huge.png", "erector_atlas.png", "erector_abm.png",
        "silo.png", "silo_rusted.png",
    ):
        src = TEX_SRC / name
        if src.exists():
            shutil.copy2(src, TEX_DST / name)


def main() -> None:
    OBJ_OUT.mkdir(parents=True, exist_ok=True)
    JSON_OUT.mkdir(parents=True, exist_ok=True)
    copy_textures()
    data = parse_obj(SRC_OBJ)
    missing = [p for p in PARTS if p not in data["o"]]
    if missing:
        raise SystemExit(f"missing OBJ objects: {missing}")
    for name in PARTS:
        write_part(name, data, OBJ_OUT / f"{slug(name)}.obj")
        write_json(name)
    write_silo_rusted_json()
    write_large_blockstate()
    write_rusted_blockstate()
    print(f"wrote {len(PARTS)} erector parts")


if __name__ == "__main__":
    main()
