$ErrorActionPreference = "Stop"
$root = "C:\Users\PC\Projects\Hbm-s-Nuclear-Tech-GIT-master"
$res = "$root\src\main\resources"
$objDir = "$res\assets\hbm\models\obj"
$legacyObj = "$root\legacy-1.7.10\src\main\resources\assets\hbm\models"
$bs = "$res\assets\hbm\blockstates"
$bm = "$res\assets\hbm\models\block"
$im = "$res\assets\hbm\models\item"
$loot = "$res\data\hbm\loot_tables\blocks"
$recipes = "$res\data\hbm\recipes"

function Write-Utf8($path, $text) {
  $dir = Split-Path $path
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
  [IO.File]::WriteAllText($path, ($text.TrimStart() + "`n"))
}

function Offset-Obj([string]$src, [string]$dst, [double]$dx, [double]$dy, [double]$dz, [string]$mtllib) {
  $lines = Get-Content $src
  $out = New-Object System.Collections.Generic.List[string]
  if ($mtllib) { $out.Add("mtllib $mtllib") }
  foreach ($line in $lines) {
    if ($line -match '^(mtllib|o |#)') { continue }
    if ($line -match '^v\s+([-\d.eE+]+)\s+([-\d.eE+]+)\s+([-\d.eE+]+)') {
      $x = [double]$Matches[1] + $dx
      $y = [double]$Matches[2] + $dy
      $z = [double]$Matches[3] + $dz
      $out.Add(("v {0:0.######} {1:0.######} {2:0.######}" -f $x, $y, $z))
    } else {
      $out.Add($line)
    }
  }
  [IO.File]::WriteAllLines($dst, $out)
}

function Write-Mtl([string]$name, [string]$tex) {
  Write-Utf8 "$objDir\$name.mtl" @"
newmtl material
Kd 1 1 1
map_Kd $tex
"@
}

function FacingObjJson($id, $obj, $particle) {
  Write-Utf8 "$bs\$id.json" @"
{
  "variants": {
    "facing=north": { "model": "hbm:block/$id" },
    "facing=south": { "model": "hbm:block/$id", "y": 180 },
    "facing=west":  { "model": "hbm:block/$id", "y": 270 },
    "facing=east":  { "model": "hbm:block/$id", "y": 90 }
  }
}
"@
  Write-Utf8 "$bm\$id.json" @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": true,
  "model": "hbm:models/obj/$obj",
  "textures": { "particle": "$particle" }
}
"@
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/$id`" }"
}

function PillarObj($id, $obj, $particle) {
  Write-Utf8 "$bs\$id.json" @"
{
  "variants": {
    "axis=y": { "model": "hbm:block/$id" },
    "axis=z": { "model": "hbm:block/$id", "x": 90 },
    "axis=x": { "model": "hbm:block/$id", "x": 90, "y": 90 }
  }
}
"@
  Write-Utf8 "$bm\$id.json" @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": true,
  "model": "hbm:models/obj/$obj",
  "textures": { "particle": "$particle" }
}
"@
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/$id`" }"
}

# --- copy / offset objs ---
Offset-Obj "$legacyObj\blocks\pipe.obj" "$objDir\pipe.obj" 0.5 0.5 0.5 "pipe.mtl"
Offset-Obj "$legacyObj\blocks\pipe_rim.obj" "$objDir\pipe_rim.obj" 0.5 0.5 0.5 "pipe_rim.mtl"
Offset-Obj "$legacyObj\blocks\pipe_quad.obj" "$objDir\pipe_quad.obj" 0.5 0.5 0.5 "pipe_quad.mtl"
Offset-Obj "$legacyObj\blocks\pipe_frame.obj" "$objDir\pipe_frame.obj" 0.5 0.5 0.5 "pipe_frame.mtl"
Offset-Obj "$legacyObj\blocks\pole.obj" "$objDir\pole.obj" 0.5 0 0.5 "pole.mtl"
Offset-Obj "$legacyObj\blocks\charger.obj" "$objDir\charger.obj" 0.5 0 0.5 "charger.mtl"
Copy-Item "$legacyObj\blocks\rail_narrow.obj" "$objDir\rail_narrow.obj" -Force
Copy-Item "$legacyObj\blocks\skeleton_holder.obj" "$objDir\skeleton_holder.obj" -Force
Copy-Item "$legacyObj\turrets\turret_howard_damaged.obj" "$objDir\turret_howard_damaged.obj" -Force
Copy-Item "$legacyObj\turrets\turret_sentry.obj" "$objDir\turret_sentry.obj" -Force
Copy-Item "$legacyObj\trinkets\bobble.obj" "$objDir\bobble.obj" -Force

function Ensure-Mtllib($file, $mtl) {
  $raw = Get-Content $file -Raw
  if ($raw -notmatch "mtllib") {
    Set-Content -Encoding ascii $file ("mtllib $mtl`n" + $raw)
  }
}
Ensure-Mtllib "$objDir\rail_narrow.obj" "rail_narrow.mtl"
Ensure-Mtllib "$objDir\skeleton_holder.obj" "skeleton_holder.mtl"
Ensure-Mtllib "$objDir\turret_howard_damaged.obj" "turret_howard_damaged.mtl"
Ensure-Mtllib "$objDir\turret_sentry.obj" "turret_sentry.mtl"
Ensure-Mtllib "$objDir\bobble.obj" "bobble.mtl"

Write-Mtl "pipe" "hbm:block/pipe_side"
Write-Mtl "pipe_rim" "hbm:block/pipe_side"
Write-Mtl "pipe_quad" "hbm:block/pipe_side"
Write-Mtl "pipe_frame" "hbm:block/pipe_frame"
Write-Mtl "pole" "hbm:block/steel_beam"
Write-Mtl "charger" "hbm:models/machines/charger"
Write-Mtl "rail_narrow" "hbm:block/rail_narrow"
Write-Mtl "skeleton_holder" "hbm:particle/skeleton"
Write-Mtl "turret_howard_damaged" "hbm:models/turrets/rusted/howard"
Write-Mtl "turret_sentry" "hbm:models/turrets/sentry_damaged"
Write-Mtl "bobble" "hbm:block/block_steel"
Write-Mtl "tesla" "hbm:models/tesla"

# colored pipe mtls + obj copies
$pipeColors = @{
  "pipe" = "hbm:block/pipe_side"
  "pipe_rusty" = "hbm:block/pipe_side_rusty"
  "pipe_red" = "hbm:block/pipe_side_red"
  "pipe_marked" = "hbm:block/pipe_side_marked"
  "pipe_green" = "hbm:block/pipe_side_green"
  "pipe_green_rusty" = "hbm:block/pipe_side_green_rusty"
}
foreach ($key in $pipeColors.Keys) {
  Write-Mtl $key $pipeColors[$key]
}

function ColorObj($srcObj, $dstName, $mtl) {
  $raw = Get-Content "$objDir\$srcObj" -Raw
  $raw = $raw -replace "mtllib \S+", "mtllib $mtl"
  if ($raw -notmatch "mtllib") { $raw = "mtllib $mtl`n$raw" }
  Set-Content -Encoding ascii "$objDir\$dstName" $raw
}

foreach ($pair in @(
  @("pipe.obj","pipe_rusty.obj","pipe_rusty.mtl"),
  @("pipe.obj","pipe_red.obj","pipe_red.mtl"),
  @("pipe.obj","pipe_marked.obj","pipe_marked.mtl"),
  @("pipe_rim.obj","pipe_rim_rusty.obj","pipe_rusty.mtl"),
  @("pipe_rim.obj","pipe_rim_green.obj","pipe_green.mtl"),
  @("pipe_rim.obj","pipe_rim_marked.obj","pipe_marked.mtl"),
  @("pipe_rim.obj","pipe_rim_red.obj","pipe_red.mtl"),
  @("pipe_rim.obj","pipe_rim_green_rusty.obj","pipe_green_rusty.mtl"),
  @("pipe_quad.obj","pipe_quad_rusty.obj","pipe_rusty.mtl"),
  @("pipe_quad.obj","pipe_quad_red.obj","pipe_red.mtl"),
  @("pipe_quad.obj","pipe_quad_marked.obj","pipe_marked.mtl")
)) { ColorObj $pair[0] $pair[1] $pair[2] }

# copy textures that live under models/
$texRoot = "$res\assets\hbm\textures"
$legacyTex = "$root\legacy-1.7.10\src\main\resources\assets\hbm\textures"
function CopyTex($rel) {
  $src = "$legacyTex\$rel"
  $dst = "$texRoot\$rel"
  if (Test-Path $src) {
    $dir = Split-Path $dst
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    if (-not (Test-Path $dst)) { Copy-Item $src $dst -Force }
  }
}
CopyTex "models\machines\charger.png"
CopyTex "models\tesla.png"
CopyTex "models\turrets\rusted\howard.png"
CopyTex "models\turrets\sentry_damaged.png"
CopyTex "particle\skeleton.png"
CopyTex "block\pipe_frame.png"
CopyTex "block\pipe_mesh.png"
CopyTex "block\pipe_side.png"
CopyTex "block\pipe_top.png"
CopyTex "block\grate_top.png"
CopyTex "block\grate_wide_top.png"
CopyTex "block\grate_side.png"
CopyTex "block\deco_pole_top.png"
CopyTex "block\steel_beam.png"
CopyTex "block\rail_narrow.png"
CopyTex "block\safe_front.png"
CopyTex "block\safe_side.png"
CopyTex "models\file_cabinet.png"
CopyTex "models\file_cabinet_steel.png"

# --- filing cabinet blockstate ---
$cab = @"
{
  "variants": {
    "facing=north,steel=false": { "model": "hbm:block/filing_cabinet" },
    "facing=south,steel=false": { "model": "hbm:block/filing_cabinet", "y": 180 },
    "facing=west,steel=false":  { "model": "hbm:block/filing_cabinet", "y": 270 },
    "facing=east,steel=false":  { "model": "hbm:block/filing_cabinet", "y": 90 },
    "facing=north,steel=true": { "model": "hbm:block/filing_cabinet_steel" },
    "facing=south,steel=true": { "model": "hbm:block/filing_cabinet_steel", "y": 180 },
    "facing=west,steel=true":  { "model": "hbm:block/filing_cabinet_steel", "y": 270 },
    "facing=east,steel=true":  { "model": "hbm:block/filing_cabinet_steel", "y": 90 }
  }
}
"@
Write-Utf8 "$bs\filing_cabinet.json" $cab

# --- pipes ---
$pipeMap = @{
  "deco_pipe" = @("pipe.obj","hbm:block/pipe_side")
  "deco_pipe_rusted" = @("pipe_rusty.obj","hbm:block/pipe_side_rusty")
  "deco_pipe_red" = @("pipe_red.obj","hbm:block/pipe_side_red")
  "deco_pipe_marked" = @("pipe_marked.obj","hbm:block/pipe_side_marked")
  "deco_pipe_rim_green" = @("pipe_rim_green.obj","hbm:block/pipe_side_green")
  "deco_pipe_rim_marked" = @("pipe_rim_marked.obj","hbm:block/pipe_side_marked")
  "deco_pipe_rim_rusted" = @("pipe_rim_rusty.obj","hbm:block/pipe_side_rusty")
  "deco_pipe_framed" = @("pipe_rim.obj","hbm:block/pipe_side")
  "deco_pipe_framed_rusted" = @("pipe_rim_rusty.obj","hbm:block/pipe_side_rusty")
  "deco_pipe_framed_red" = @("pipe_rim_red.obj","hbm:block/pipe_side_red")
  "deco_pipe_framed_green_rusted" = @("pipe_rim_green_rusty.obj","hbm:block/pipe_side_green_rusty")
  "deco_pipe_quad" = @("pipe_quad.obj","hbm:block/pipe_side")
  "deco_pipe_quad_rusted" = @("pipe_quad_rusty.obj","hbm:block/pipe_side_rusty")
  "deco_pipe_quad_red" = @("pipe_quad_red.obj","hbm:block/pipe_side_red")
  "deco_pipe_quad_marked" = @("pipe_quad_marked.obj","hbm:block/pipe_side_marked")
}
foreach ($id in $pipeMap.Keys) {
  PillarObj $id $pipeMap[$id][0] $pipeMap[$id][1]
}

# --- poles / charger / tesla / machines as obj ---
FacingObjJson "steel_poles" "pole.obj" "hbm:block/steel_beam"
FacingObjJson "charger" "charger.obj" "hbm:block/block_steel"
FacingObjJson "tesla" "tesla.obj" "hbm:models/tesla"
FacingObjJson "skeleton_holder" "skeleton_holder.obj" "hbm:particle/skeleton"
FacingObjJson "turret_howard_damaged" "turret_howard_damaged.obj" "hbm:models/turrets/rusted/howard"
FacingObjJson "turret_sentry_damaged" "turret_sentry.obj" "hbm:models/turrets/sentry_damaged"
FacingObjJson "bobblehead" "bobble.obj" "hbm:block/block_steel"
FacingObjJson "rail_narrow" "rail_narrow.obj" "hbm:block/rail_narrow"

# steel_corner uses wall template
Write-Utf8 "$bs\steel_corner.json" (Get-Content "$bs\steel_wall.json" -Raw)
Write-Utf8 "$bm\steel_corner.json" @"
{
  "parent": "hbm:block/template_steel_wall",
  "textures": { "all": "hbm:block/steel_wall" }
}
"@
Write-Utf8 "$im\steel_corner.json" '{ "parent": "hbm:block/steel_corner" }'

# pole_top keep facing cube with deco_pole_top if texture exists
if (Test-Path "$texRoot\block\deco_pole_top.png") {
  Write-Utf8 "$bm\pole_top.json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "hbm:block/deco_pole_top" }
}
"@
}

# --- grate models with #side ---
function FixGrate($id, $top) {
  for ($o = 0; $o -le 9; $o++) {
    $y = if ($o -eq 9) { -2 } else { $o * 2 }
    $y2 = $y + 2
    Write-Utf8 "$bm\${id}_$o.json" @"
{
  "parent": "minecraft:block/block",
  "textures": { "all": "$top", "particle": "$top", "side": "hbm:block/grate_side" },
  "elements": [
    {
      "from": [0, $y, 0],
      "to": [16, $y2, 16],
      "faces": {
        "up": { "texture": "#all" }, "down": { "texture": "#all" },
        "north": { "texture": "#side" }, "south": { "texture": "#side" },
        "west": { "texture": "#side" }, "east": { "texture": "#side" }
      }
    }
  ]
}
"@
  }
}
FixGrate "steel_grate" "hbm:block/grate_top"
FixGrate "steel_grate_wide" "hbm:block/grate_wide_top"

# --- dead plants ---
$plants = @("generic","grass","flower","bigflower","fern")
$pj = New-Object System.Collections.Generic.List[string]
$pj.Add("{"); $pj.Add('  "variants": {')
for ($i = 0; $i -lt $plants.Count; $i++) {
  $comma = if ($i -eq 0) { "" } else { "," }
  $pj.Add("$comma    `"variant=$($plants[$i])`": { `"model`": `"hbm:block/plant_dead_$($plants[$i])`" }")
  Write-Utf8 "$bm\plant_dead_$($plants[$i]).json" @"
{
  "parent": "minecraft:block/cross",
  "textures": { "cross": "hbm:block/plant_dead.$($plants[$i])" }
}
"@
}
$pj.Add("  }"); $pj.Add("}")
Write-Utf8 "$bs\plant_dead.json" ($pj -join "`n")
Write-Utf8 "$im\plant_dead.json" '{ "parent": "minecraft:item/generated", "textures": { "layer0": "hbm:block/plant_dead.generic" } }'
Write-Utf8 "$loot\plant_dead.json" '{ "type": "minecraft:block" }'

# --- door loot ---
foreach ($d in @("door_metal","door_office","door_bunker")) {
  Write-Utf8 "$loot\$d.json" @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0,
      "conditions": [ { "condition": "minecraft:survives_explosion" } ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "hbm:$d",
          "conditions": [
            {
              "condition": "minecraft:block_state_property",
              "block": "hbm:$d",
              "properties": { "half": "lower" }
            }
          ]
        }
      ],
      "rolls": 1
    }
  ]
}
"@
}

# --- recipes ---
function Shaped($file, $pattern, $key, $result, $count = 1) {
  $pat = ($pattern | ForEach-Object { '      "' + $_ + '"' }) -join ",`n"
  $keys = ($key.GetEnumerator() | ForEach-Object { '    "' + $_.Key + '": { "item": "' + $_.Value + '" }' }) -join ",`n"
  $cnt = if ($count -gt 1) { ", `"count`": $count" } else { "" }
  Write-Utf8 "$recipes\$file.json" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
$pat
  ],
  "key": {
$keys
  },
  "result": { "item": "$result"$cnt }
}
"@
}

Write-Utf8 "$recipes\filing_cabinet.json" @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": [ " P ", "PIP", " P " ],
  "key": {
    "P": { "item": "hbm:plate_steel" },
    "I": { "item": "hbm:plate_polymer" }
  },
  "result": { "item": "hbm:filing_cabinet", "nbt": "{BlockStateTag:{steel:`"true`"}}" }
}
"@

Shaped "steel_grate" @("SS","SS") @{ S = "hbm:steel_beam" } "hbm:steel_grate" 4
Shaped "steel_grate_wide" @("SS") @{ S = "hbm:steel_grate" } "hbm:steel_grate_wide" 4
Shaped "steel_grate_from_wide" @("SS") @{ S = "hbm:steel_grate_wide" } "hbm:steel_grate" 1
Shaped "steel_poles" @("S S","SSS","S S") @{ S = "hbm:ingot_steel" } "hbm:steel_poles" 16
Shaped "pole_top" @("T T","TRT","BBB") @{ T = "hbm:ingot_tungsten"; B = "hbm:ingot_beryllium"; R = "hbm:ingot_red_copper" } "hbm:pole_top" 1
Shaped "door_metal" @("II","SS","II") @{ I = "hbm:plate_iron"; S = "hbm:plate_steel" } "hbm:door_metal" 1
Shaped "door_office" @("II","SS","II") @{ I = "minecraft:oak_planks"; S = "hbm:plate_iron" } "hbm:door_office" 1
Shaped "door_bunker" @("II","SS","II") @{ I = "hbm:plate_steel"; S = "hbm:plate_lead" } "hbm:door_bunker" 1
Shaped "charger" @("G","S","C") @{ G = "minecraft:glowstone_dust"; S = "hbm:ingot_steel"; C = "hbm:coil_copper" } "hbm:charger" 1
Shaped "deco_pipe" @("PP") @{ P = "hbm:pipe_steel" } "hbm:deco_pipe" 6

Write-Utf8 "$recipes\steel_corner.json" @"
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [ { "item": "hbm:steel_wall" }, { "item": "hbm:steel_wall" } ],
  "result": { "item": "hbm:steel_corner" }
}
"@
Write-Utf8 "$recipes\trapdoor_steel.json" @"
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [ { "item": "minecraft:oak_trapdoor" }, { "item": "hbm:ingot_steel" } ],
  "result": { "item": "hbm:trapdoor_steel" }
}
"@

Write-Host "finish assets done"
