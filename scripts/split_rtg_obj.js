const fs = require("fs");
const path = require("path");

/**
 * Split Wavefront objects with remapped 1-based v/vt/vn indices.
 * TESR parts come from the centered 1.7 machine mesh (legacy_raw), not deco rtg.obj.
 */
const tesrSrc = path.join(__dirname, "..", "src/main/resources/assets/hbm/models/legacy_raw/machines/rtg.obj");
const outDir = path.join(__dirname, "..", "src/main/resources/assets/hbm/models/obj");
const mtlBody = "newmtl material\nKd 1 1 1\nmap_Kd hbm:models/machines/rtg\n";

function absIndex(raw, count) {
  if (raw === undefined || raw === "") {
    return 0;
  }
  const n = parseInt(raw, 10);
  if (!n) {
    return 0;
  }
  return n < 0 ? count + n + 1 : n;
}

function parseObj(text) {
  const verts = [];
  const texs = [];
  const norms = [];
  const objects = [];
  let current = null;

  const startObject = (name) => {
    current = { name, faces: [], smoothing: null };
    objects.push(current);
  };

  for (const line of text.split(/\r?\n/)) {
    if (line.startsWith("o ")) {
      startObject(line.slice(2).trim());
      continue;
    }
    if (line.startsWith("v ")) {
      verts.push(line);
      continue;
    }
    if (line.startsWith("vt ")) {
      texs.push(line);
      continue;
    }
    if (line.startsWith("vn ")) {
      norms.push(line);
      continue;
    }
    if (line.startsWith("s ") && current) {
      current.smoothing = line;
      continue;
    }
    if (!line.startsWith("f ")) {
      continue;
    }
    if (!current) {
      startObject("unnamed");
    }
    const corners = line.slice(2).trim().split(/\s+/).map((token) => {
      const bits = token.split("/");
      return {
        v: absIndex(bits[0], verts.length),
        vt: absIndex(bits[1], texs.length),
        vn: absIndex(bits[2], norms.length),
        style: bits.length
      };
    });
    current.faces.push(corners);
  }
  return { verts, texs, norms, objects };
}

function remapIndex(used, oldIndex) {
  if (!oldIndex) {
    return 0;
  }
  const next = used.indexOf(oldIndex);
  return next < 0 ? 0 : next + 1;
}

function formatCorner(corner, usedV, usedVt, usedVn) {
  const v = remapIndex(usedV, corner.v);
  const vt = remapIndex(usedVt, corner.vt);
  const vn = remapIndex(usedVn, corner.vn);
  if (corner.style <= 1) {
    return String(v);
  }
  if (corner.style === 2) {
    return v + "/" + vt;
  }
  if (!corner.vt) {
    return v + "//" + vn;
  }
  return v + "/" + vt + "/" + vn;
}

function uniqueSorted(values) {
  return [...new Set(values.filter((n) => n > 0))].sort((a, b) => a - b);
}

function emitObject(parsed, object, mtllib, slug) {
  const usedV = uniqueSorted(object.faces.flatMap((face) => face.map((c) => c.v)));
  const usedVt = uniqueSorted(object.faces.flatMap((face) => face.map((c) => c.vt)));
  const usedVn = uniqueSorted(object.faces.flatMap((face) => face.map((c) => c.vn)));
  const lines = [
    "mtllib " + mtllib,
    "usemtl material",
    "o " + object.name
  ];
  for (const index of usedV) {
    lines.push(parsed.verts[index - 1]);
  }
  for (const index of usedVt) {
    lines.push(parsed.texs[index - 1]);
  }
  for (const index of usedVn) {
    lines.push(parsed.norms[index - 1]);
  }
  if (object.smoothing) {
    lines.push(object.smoothing);
  }
  for (const face of object.faces) {
    lines.push("f " + face.map((c) => formatCorner(c, usedV, usedVt, usedVn)).join(" "));
  }
  lines.push("");
  const objPath = path.join(outDir, slug + ".obj");
  fs.writeFileSync(objPath, lines.join("\n"));
  fs.writeFileSync(path.join(outDir, slug + ".mtl"), mtlBody);
  const maxV = Math.max(0, ...object.faces.flatMap((face) => face.map((c) => c.v)));
  if (maxV > parsed.verts.length) {
    throw new Error(slug + " face vertex " + maxV + " exceeds " + parsed.verts.length);
  }
  const remappedMax = usedV.length;
  const written = fs.readFileSync(objPath, "utf8");
  let vCount = 0;
  let faceMax = 0;
  for (const line of written.split(/\r?\n/)) {
    if (line.startsWith("v ")) {
      vCount++;
    }
    if (line.startsWith("f ")) {
      for (const token of line.slice(2).trim().split(/\s+/)) {
        faceMax = Math.max(faceMax, parseInt(token.split("/")[0], 10) || 0);
      }
    }
  }
  if (faceMax > vCount) {
    throw new Error(slug + " remapped face " + faceMax + " exceeds " + vCount + " verts");
  }
  console.log(slug + ".obj faces " + object.faces.length + " verts " + remappedMax);
  return written;
}

function offsetVertices(text, dx, dy, dz) {
  return text.replace(/^v ([^\s]+) ([^\s]+) ([^\s]+)/gm, (_, x, y, z) => {
    const nx = (parseFloat(x) + dx).toFixed(6);
    const ny = (parseFloat(y) + dy).toFixed(6);
    const nz = (parseFloat(z) + dz).toFixed(6);
    return "v " + nx + " " + ny + " " + nz;
  });
}

const parsed = parseObj(fs.readFileSync(tesrSrc, "utf8"));
const byName = Object.fromEntries(parsed.objects.map((o) => [o.name, o]));
if (!byName.Gen || !byName.Connector) {
  throw new Error("legacy_raw rtg.obj missing Gen or Connector");
}

const genText = emitObject(parsed, byName.Gen, "rtg_gen.mtl", "rtg_gen");
emitObject(parsed, byName.Connector, "rtg_connector.mtl", "rtg_connector");

const itemText = offsetVertices(genText, 0.5, 0, 0.5).replace("mtllib rtg_gen.mtl", "mtllib rtg_gen_item.mtl");
fs.writeFileSync(path.join(outDir, "rtg_gen_item.obj"), itemText);
fs.writeFileSync(path.join(outDir, "rtg_gen_item.mtl"), mtlBody);
console.log("rtg_gen_item.obj offset +0.5 xz for inventory");
