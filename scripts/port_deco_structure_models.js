/**
 * Shift 1.7 centered deco meshes into block space and emit the satellite dish OBJ.
 */
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..");
const OBJ = path.join(ROOT, "src/main/resources/assets/hbm/models/obj");
const RAW = path.join(ROOT, "src/main/resources/assets/hbm/models/legacy_raw");

function shiftObj(src, dest, mtlName, mapKd, dx = 0.5, dy = 0, dz = 0.5) {
  fs.mkdirSync(path.dirname(dest), { recursive: true });
  fs.writeFileSync(dest.replace(/\.obj$/, ".mtl"), `newmtl material\nKd 1 1 1\nmap_Kd ${mapKd}\n`);
  const out = [`mtllib ${mtlName}`, "usemtl material"];
  for (const line of fs.readFileSync(src, "utf8").split(/\r?\n/)) {
    if (line.startsWith("v ")) {
      const p = line.split(/\s+/);
      const x = parseFloat(p[1]) + dx;
      const y = parseFloat(p[2]) + dy;
      const z = parseFloat(p[3]) + dz;
      out.push(`v ${x.toFixed(6)} ${y.toFixed(6)} ${z.toFixed(6)}`);
    } else if (line.startsWith("mtllib ") || line.startsWith("usemtl ")) {
      continue;
    } else {
      out.push(line);
    }
  }
  fs.writeFileSync(dest, out.join("\n") + "\n");
}

function rotX(v, a) {
  const c = Math.cos(a);
  const s = Math.sin(a);
  return [v[0], v[1] * c - v[2] * s, v[1] * s + v[2] * c];
}

function rotY(v, a) {
  const c = Math.cos(a);
  const s = Math.sin(a);
  return [v[0] * c + v[2] * s, v[1], -v[0] * s + v[2] * c];
}

function rotZ(v, a) {
  const c = Math.cos(a);
  const s = Math.sin(a);
  return [v[0] * c - v[1] * s, v[0] * s + v[1] * c, v[2]];
}

function applyModelRot(v, rx, ry, rz) {
  return rotZ(rotY(rotX(v, rx), ry), rz);
}

function tesrToBlock(px, py, pz) {
  let v = rotZ([px * 0.0625, py * 0.0625, pz * 0.0625], Math.PI);
  return [v[0] + 0.5, v[1] + 1.5, v[2] + 0.5];
}

function boxCorners(ox, oy, oz, w, h, d) {
  return [
    [ox, oy, oz],
    [ox + w, oy, oz],
    [ox + w, oy + h, oz],
    [ox, oy + h, oz],
    [ox, oy, oz + d],
    [ox + w, oy, oz + d],
    [ox + w, oy + h, oz + d],
    [ox, oy + h, oz + d]
  ];
}

function emitTechneBox(verts, faces, texs, ox, oy, oz, w, h, d, px, py, pz, rx, ry, rz, u, v, tw = 64, th = 64) {
  const corners = boxCorners(ox, oy, oz, w, h, d).map((c) => {
    const m = applyModelRot(c, rx, ry, rz);
    return tesrToBlock(m[0] + px, m[1] + py, m[2] + pz);
  });
  const uv = (pxu, pyv) => {
    texs.push([pxu / tw, 1 - pyv / th]);
    return texs.length;
  };
  const facesUv = {
    down: [[u + d, v + d], [u + d + w, v + d], [u + d + w, v], [u + d, v]],
    up: [[u + d + w, v + d], [u + d + w + w, v + d], [u + d + w + w, v], [u + d + w, v]],
    west: [[u, v + d], [u + d, v + d], [u + d, v + d + h], [u, v + d + h]],
    north: [[u + d, v + d], [u + d + w, v + d], [u + d + w, v + d + h], [u + d, v + d + h]],
    east: [[u + d + w, v + d], [u + d + w + d, v + d], [u + d + w + d, v + d + h], [u + d + w, v + d + h]],
    south: [[u + d + w + d, v + d], [u + d + w + d + w, v + d], [u + d + w + d + w, v + d + h], [u + d + w + d, v + d + h]]
  };
  const quads = [
    ["down", [0, 1, 5, 4]],
    ["up", [3, 7, 6, 2]],
    ["west", [0, 4, 7, 3]],
    ["east", [1, 2, 6, 5]],
    ["north", [0, 3, 2, 1]],
    ["south", [4, 5, 6, 7]]
  ];
  const base = verts.length;
  verts.push(...corners);
  for (const [name, idx] of quads) {
    const ti = facesUv[name].map((p) => uv(p[0], p[1]));
    faces.push([base + idx[0], ti[0], base + idx[1], ti[1], base + idx[2], ti[2], base + idx[3], ti[3]]);
  }
}

function writeSatellite() {
  const verts = [];
  const texs = [];
  const faces = [];
  const rx = -0.2617994;
  const ry = -0.4363323;
  emitTechneBox(verts, faces, texs, 0, 0, 0, 12, 16, 12, -6, 8, -6, 0, 0, 0, 0, 0);
  emitTechneBox(verts, faces, texs, 3, 9, -8, 8, 8, 2, -3, 6, 0, rx, ry, 0, 10, 28);
  emitTechneBox(verts, faces, texs, 3, 7, -10, 8, 2, 3, -3, 6, 0, rx, ry, 0, 0, 39);
  emitTechneBox(verts, faces, texs, 1, 9, -10, 2, 8, 3, -3, 6, 0, rx, ry, 0, 0, 28);
  emitTechneBox(verts, faces, texs, 11, 9, -10, 2, 8, 3, -3, 6, 0, rx, ry, 0, 0, 28);
  emitTechneBox(verts, faces, texs, 3, 17, -10, 8, 2, 3, -3, 6, 0, rx, ry, 0, 0, 39);
  emitTechneBox(verts, faces, texs, 6, 12, -11, 2, 2, 3, -3, 6, 0, rx, ry, 0, 0, 44);
  emitTechneBox(verts, faces, texs, 6.5, 12.5, -14, 1, 1, 3, -3, 6, 0, rx, ry, 0, 0, 49);
  emitTechneBox(verts, faces, texs, 6, 12, -16, 2, 2, 2, -3, 6, 0, rx, ry, 0, 0, 53);

  const lines = [
    "mtllib deco_satellite_receiver.mtl",
    "usemtl material",
    "# generated from 1.7 ModelSatelliteReceiver",
    "o Dish"
  ];
  for (const v of verts) {
    lines.push(`v ${v[0].toFixed(6)} ${v[1].toFixed(6)} ${v[2].toFixed(6)}`);
  }
  for (const t of texs) {
    lines.push(`vt ${t[0].toFixed(6)} ${t[1].toFixed(6)}`);
  }
  for (const f of faces) {
    lines.push(`f ${f[0] + 1}/${f[1]} ${f[2] + 1}/${f[3]} ${f[4] + 1}/${f[5]} ${f[6] + 1}/${f[7]}`);
  }
  fs.writeFileSync(path.join(OBJ, "deco_satellite_receiver.obj"), lines.join("\n") + "\n");
  fs.writeFileSync(
    path.join(OBJ, "deco_satellite_receiver.mtl"),
    "newmtl material\nKd 1 1 1\nmap_Kd hbm:models/PoleSatelliteReceiver\n"
  );
}

function writeMicrowave() {
  const src = path.join(RAW, "machines/microwave.obj");
  const dest = path.join(OBJ, "microwave.obj");
  fs.writeFileSync(dest.replace(/\.obj$/, ".mtl"), "newmtl material\nKd 1 1 1\nmap_Kd hbm:models/machines/microwave\n");
  const out = ["mtllib microwave.mtl", "usemtl material"];
  for (const line of fs.readFileSync(src, "utf8").split(/\r?\n/)) {
    if (line.startsWith("v ")) {
      const p = line.split(/\s+/);
      const x = parseFloat(p[1]);
      const y = parseFloat(p[2]) - 0.785;
      const z = parseFloat(p[3]) + 1.15;
      out.push(`v ${x.toFixed(6)} ${y.toFixed(6)} ${z.toFixed(6)}`);
    } else if (line.startsWith("mtllib ") || line.startsWith("usemtl ")) {
      continue;
    } else {
      out.push(line);
    }
  }
  fs.writeFileSync(dest, out.join("\n") + "\n");
}

fs.mkdirSync(OBJ, { recursive: true });
shiftObj(
  path.join(RAW, "blocks/antenna_top.obj"),
  path.join(OBJ, "antenna_top.obj"),
  "antenna_top.mtl",
  "hbm:block/deco_pole_top"
);
writeSatellite();
writeMicrowave();
console.log("wrote antenna_top.obj, deco_satellite_receiver.obj, microwave.obj");
