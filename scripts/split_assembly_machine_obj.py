"""Split assembly_machine.obj into TESR parts plus an inventory mesh."""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/resources/assets/hbm/models/legacy_raw/machines/assembly_machine.obj"
OUT = ROOT / "src/main/resources/assets/hbm/models/obj"
BLOCK = ROOT / "src/main/resources/assets/hbm/models/block"
MTL = "newmtl material\nKd 1 1 1\nmap_Kd hbm:models/machines/assembly_machine\n"

TESR = {
    "Base": "assembly_machine_base",
    "Frame": "assembly_machine_frame",
    "Ring": "assembly_machine_ring",
    "ArmLower1": "assembly_machine_arm_lower_1",
    "ArmUpper1": "assembly_machine_arm_upper_1",
    "Head1": "assembly_machine_head_1",
    "Spike1": "assembly_machine_spike_1",
    "ArmLower2": "assembly_machine_arm_lower_2",
    "ArmUpper2": "assembly_machine_arm_upper_2",
    "Head2": "assembly_machine_head_2",
    "Spike2": "assembly_machine_spike_2",
}


def abs_index(raw: str, count: int) -> int:
    if not raw:
        return 0
    n = int(raw)
    if not n:
        return 0
    return count + n + 1 if n < 0 else n


def parse_obj(text: str):
    verts, texs, norms = [], [], []
    objects = []
    current = None

    def start(name: str):
        nonlocal current
        current = {"name": name, "faces": [], "smoothing": None}
        objects.append(current)

    for line in text.splitlines():
        if line.startswith("o "):
            start(line[2:].strip())
            continue
        if line.startswith("v "):
            verts.append(line)
            continue
        if line.startswith("vt "):
            texs.append(line)
            continue
        if line.startswith("vn "):
            norms.append(line)
            continue
        if line.startswith("s ") and current is not None:
            current["smoothing"] = line
            continue
        if not line.startswith("f "):
            continue
        if current is None:
            start("unnamed")
        corners = []
        for token in line[2:].split():
            bits = token.split("/")
            corners.append({
                "v": abs_index(bits[0] if bits else "", len(verts)),
                "vt": abs_index(bits[1] if len(bits) > 1 else "", len(texs)),
                "vn": abs_index(bits[2] if len(bits) > 2 else "", len(norms)),
                "style": len(bits),
            })
        current["faces"].append(corners)
    return {"verts": verts, "texs": texs, "norms": norms, "objects": objects}


def remap(used: list[int], old: int) -> int:
    if not old:
        return 0
    try:
        return used.index(old) + 1
    except ValueError:
        return 0


def format_corner(corner, used_v, used_vt, used_vn) -> str:
    v = remap(used_v, corner["v"])
    vt = remap(used_vt, corner["vt"])
    vn = remap(used_vn, corner["vn"])
    if corner["style"] <= 1:
        return str(v)
    if corner["style"] == 2:
        return f"{v}/{vt}"
    if not corner["vt"]:
        return f"{v}//{vn}"
    return f"{v}/{vt}/{vn}"


def unique_sorted(values: list[int]) -> list[int]:
    return sorted({n for n in values if n > 0})


def emit_object(parsed, obj, mtllib: str, slug: str, dx=0.0, dy=0.0, dz=0.0) -> str:
    used_v = unique_sorted([c["v"] for f in obj["faces"] for c in f])
    used_vt = unique_sorted([c["vt"] for f in obj["faces"] for c in f])
    used_vn = unique_sorted([c["vn"] for f in obj["faces"] for c in f])
    lines = [f"mtllib {mtllib}", "usemtl material", f"o {obj['name']}"]
    for index in used_v:
        line = parsed["verts"][index - 1]
        if dx or dy or dz:
            _, x, y, z = line.split()[:4]
            line = f"v {float(x) + dx:.6f} {float(y) + dy:.6f} {float(z) + dz:.6f}"
        lines.append(line)
    for index in used_vt:
        lines.append(parsed["texs"][index - 1])
    for index in used_vn:
        lines.append(parsed["norms"][index - 1])
    if obj["smoothing"]:
        lines.append(obj["smoothing"])
    for face in obj["faces"]:
        lines.append("f " + " ".join(format_corner(c, used_v, used_vt, used_vn) for c in face))
    lines.append("")
    text = "\n".join(lines)
    obj_path = OUT / f"{slug}.obj"
    obj_path.write_text(text, encoding="utf-8")
    (OUT / f"{slug}.mtl").write_text(MTL, encoding="utf-8")
    verts = sum(1 for line in text.splitlines() if line.startswith("v "))
    face_max = 0
    for line in text.splitlines():
        if line.startswith("f "):
            for token in line[2:].split():
                face_max = max(face_max, int(token.split("/")[0] or 0))
    if face_max > verts:
        raise SystemExit(f"{slug} remapped face {face_max} exceeds {verts} verts")
    print(f"{slug}.obj faces {len(obj['faces'])} verts {len(used_v)}")
    return text


def write_block_json(slug: str) -> None:
    (BLOCK / f"{slug}.json").write_text(
        "{\n"
        '  "loader": "forge:obj",\n'
        '  "flip_v": true,\n'
        '  "automatic_culling": false,\n'
        '  "shade_quads": false,\n'
        f'  "model": "hbm:models/obj/{slug}.obj",\n'
        '  "textures": { "particle": "hbm:models/machines/assembly_machine" }\n'
        "}\n",
        encoding="utf-8",
    )


def emit_combined(parsed, objects, slug: str, dx=0.0, dy=0.0, dz=0.0) -> None:
    used_v = unique_sorted([c["v"] for obj in objects for f in obj["faces"] for c in f])
    used_vt = unique_sorted([c["vt"] for obj in objects for f in obj["faces"] for c in f])
    used_vn = unique_sorted([c["vn"] for obj in objects for f in obj["faces"] for c in f])
    lines = [f"mtllib {slug}.mtl", "usemtl material"]
    for index in used_v:
        _, x, y, z = parsed["verts"][index - 1].split()[:4]
        lines.append(f"v {float(x) + dx:.6f} {float(y) + dy:.6f} {float(z) + dz:.6f}")
    for index in used_vt:
        lines.append(parsed["texs"][index - 1])
    for index in used_vn:
        lines.append(parsed["norms"][index - 1])
    for obj in objects:
        lines.append(f"o {obj['name']}")
        if obj["smoothing"]:
            lines.append(obj["smoothing"])
        for face in obj["faces"]:
            lines.append("f " + " ".join(format_corner(c, used_v, used_vt, used_vn) for c in face))
    lines.append("")
    text = "\n".join(lines)
    (OUT / f"{slug}.obj").write_text(text, encoding="utf-8")
    (OUT / f"{slug}.mtl").write_text(MTL, encoding="utf-8")
    verts = sum(1 for line in text.splitlines() if line.startswith("v "))
    face_max = 0
    for line in text.splitlines():
        if line.startswith("f "):
            for token in line[2:].split():
                face_max = max(face_max, int(token.split("/")[0] or 0))
    if face_max > verts:
        raise SystemExit(f"{slug} remapped face {face_max} exceeds {verts} verts")
    print(f"{slug}.obj objects {len(objects)} verts {len(used_v)}")


def main() -> None:
    parsed = parse_obj(SRC.read_text(encoding="utf-8"))
    by_name = {obj["name"]: obj for obj in parsed["objects"]}
    missing = [name for name in TESR if name not in by_name]
    if missing:
        raise SystemExit("missing objects: " + ", ".join(missing))
    OUT.mkdir(parents=True, exist_ok=True)
    BLOCK.mkdir(parents=True, exist_ok=True)
    for name, slug in TESR.items():
        emit_object(parsed, by_name[name], f"{slug}.mtl", slug)
        write_block_json(slug)
    emit_combined(parsed, parsed["objects"], "assembly_machine_item", dx=0.5, dz=0.5)
    write_block_json("assembly_machine_item")
    print("done")


if __name__ == "__main__":
    main()
