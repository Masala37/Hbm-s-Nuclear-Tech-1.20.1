$ErrorActionPreference = "Stop"
$root = "C:\Users\PC\Projects\Hbm-s-Nuclear-Tech-GIT-master\src\main\resources"
$bs = "$root\assets\hbm\blockstates"
$bm = "$root\assets\hbm\models\block"
$im = "$root\assets\hbm\models\item"
$loot = "$root\data\hbm\loot_tables\blocks"

function Write-Utf8($path, $text) {
  $dir = Split-Path $path
  if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
  [IO.File]::WriteAllText($path, $text.TrimStart() + "`n")
}

function CubeAll($id, $tex) {
  Write-Utf8 "$bs\$id.json" '{ "variants": { "": { "model": "hbm:block/' + $id + '" } } }'
  Write-Utf8 "$bm\$id.json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "$tex" }
}
"@
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/$id`" }"
}

function Loot($id) {
  Write-Utf8 "$loot\$id.json" @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [ { "type": "minecraft:item", "name": "hbm:$id" } ],
      "conditions": [ { "condition": "minecraft:survives_explosion" } ]
    }
  ]
}
"@
}

function FacingObj($id, $obj, $particle) {
  $variants = @"
{
  "variants": {
    "facing=north": { "model": "hbm:block/$id" },
    "facing=south": { "model": "hbm:block/$id", "y": 180 },
    "facing=west":  { "model": "hbm:block/$id", "y": 270 },
    "facing=east":  { "model": "hbm:block/$id", "y": 90 }
  }
}
"@
  Write-Utf8 "$bs\$id.json" $variants
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
  Write-Utf8 "$im\$id.json" @"
{
  "parent": "hbm:block/$id",
  "display": {
    "gui": { "rotation": [30, 225, 0], "scale": [0.65, 0.65, 0.65] },
    "ground": { "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35] },
    "fixed": { "scale": [0.5, 0.5, 0.5] },
    "thirdperson_righthand": { "rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375] },
    "firstperson_righthand": { "rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4] }
  }
}
"@
}

function FacingCube($id, $tex) {
  $variants = @"
{
  "variants": {
    "facing=north": { "model": "hbm:block/$id" },
    "facing=south": { "model": "hbm:block/$id", "y": 180 },
    "facing=west":  { "model": "hbm:block/$id", "y": 270 },
    "facing=east":  { "model": "hbm:block/$id", "y": 90 }
  }
}
"@
  Write-Utf8 "$bs\$id.json" $variants
  Write-Utf8 "$bm\$id.json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "$tex" }
}
"@
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/$id`" }"
}

function Pillar($id, $side, $end) {
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
  "parent": "minecraft:block/cube_column",
  "textures": { "end": "$end", "side": "$side" }
}
"@
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/$id`" }"
}

function Stairs($id, $tex) {
  Write-Utf8 "$bs\$id.json" @"
{
  "variants": {
    "facing=east,half=bottom,shape=straight":  { "model": "hbm:block/$id" },
    "facing=west,half=bottom,shape=straight":  { "model": "hbm:block/$id", "y": 180, "uvlock": true },
    "facing=south,half=bottom,shape=straight": { "model": "hbm:block/$id", "y": 90, "uvlock": true },
    "facing=north,half=bottom,shape=straight": { "model": "hbm:block/$id", "y": 270, "uvlock": true },
    "facing=east,half=bottom,shape=outer_right":  { "model": "hbm:block/${id}_outer" },
    "facing=west,half=bottom,shape=outer_right":  { "model": "hbm:block/${id}_outer", "y": 180, "uvlock": true },
    "facing=south,half=bottom,shape=outer_right": { "model": "hbm:block/${id}_outer", "y": 90, "uvlock": true },
    "facing=north,half=bottom,shape=outer_right": { "model": "hbm:block/${id}_outer", "y": 270, "uvlock": true },
    "facing=east,half=bottom,shape=outer_left":  { "model": "hbm:block/${id}_outer", "y": 270, "uvlock": true },
    "facing=west,half=bottom,shape=outer_left":  { "model": "hbm:block/${id}_outer", "y": 90, "uvlock": true },
    "facing=south,half=bottom,shape=outer_left": { "model": "hbm:block/${id}_outer" },
    "facing=north,half=bottom,shape=outer_left": { "model": "hbm:block/${id}_outer", "y": 180, "uvlock": true },
    "facing=east,half=bottom,shape=inner_right":  { "model": "hbm:block/${id}_inner" },
    "facing=west,half=bottom,shape=inner_right":  { "model": "hbm:block/${id}_inner", "y": 180, "uvlock": true },
    "facing=south,half=bottom,shape=inner_right": { "model": "hbm:block/${id}_inner", "y": 90, "uvlock": true },
    "facing=north,half=bottom,shape=inner_right": { "model": "hbm:block/${id}_inner", "y": 270, "uvlock": true },
    "facing=east,half=bottom,shape=inner_left":  { "model": "hbm:block/${id}_inner", "y": 270, "uvlock": true },
    "facing=west,half=bottom,shape=inner_left":  { "model": "hbm:block/${id}_inner", "y": 90, "uvlock": true },
    "facing=south,half=bottom,shape=inner_left": { "model": "hbm:block/${id}_inner" },
    "facing=north,half=bottom,shape=inner_left": { "model": "hbm:block/${id}_inner", "y": 180, "uvlock": true },
    "facing=east,half=top,shape=straight":  { "model": "hbm:block/$id", "x": 180, "uvlock": true },
    "facing=west,half=top,shape=straight":  { "model": "hbm:block/$id", "x": 180, "y": 180, "uvlock": true },
    "facing=south,half=top,shape=straight": { "model": "hbm:block/$id", "x": 180, "y": 90, "uvlock": true },
    "facing=north,half=top,shape=straight": { "model": "hbm:block/$id", "x": 180, "y": 270, "uvlock": true },
    "facing=east,half=top,shape=outer_right":  { "model": "hbm:block/${id}_outer", "x": 180, "y": 90, "uvlock": true },
    "facing=west,half=top,shape=outer_right":  { "model": "hbm:block/${id}_outer", "x": 180, "y": 270, "uvlock": true },
    "facing=south,half=top,shape=outer_right": { "model": "hbm:block/${id}_outer", "x": 180, "y": 180, "uvlock": true },
    "facing=north,half=top,shape=outer_right": { "model": "hbm:block/${id}_outer", "x": 180, "uvlock": true },
    "facing=east,half=top,shape=outer_left":  { "model": "hbm:block/${id}_outer", "x": 180, "uvlock": true },
    "facing=west,half=top,shape=outer_left":  { "model": "hbm:block/${id}_outer", "x": 180, "y": 180, "uvlock": true },
    "facing=south,half=top,shape=outer_left": { "model": "hbm:block/${id}_outer", "x": 180, "y": 90, "uvlock": true },
    "facing=north,half=top,shape=outer_left": { "model": "hbm:block/${id}_outer", "x": 180, "y": 270, "uvlock": true },
    "facing=east,half=top,shape=inner_right":  { "model": "hbm:block/${id}_inner", "x": 180, "y": 90, "uvlock": true },
    "facing=west,half=top,shape=inner_right":  { "model": "hbm:block/${id}_inner", "x": 180, "y": 270, "uvlock": true },
    "facing=south,half=top,shape=inner_right": { "model": "hbm:block/${id}_inner", "x": 180, "y": 180, "uvlock": true },
    "facing=north,half=top,shape=inner_right": { "model": "hbm:block/${id}_inner", "x": 180, "uvlock": true },
    "facing=east,half=top,shape=inner_left":  { "model": "hbm:block/${id}_inner", "x": 180, "uvlock": true },
    "facing=west,half=top,shape=inner_left":  { "model": "hbm:block/${id}_inner", "x": 180, "y": 180, "uvlock": true },
    "facing=south,half=top,shape=inner_left": { "model": "hbm:block/${id}_inner", "x": 180, "y": 90, "uvlock": true },
    "facing=north,half=top,shape=inner_left": { "model": "hbm:block/${id}_inner", "x": 180, "y": 270, "uvlock": true }
  }
}
"@
  foreach ($kind in @("", "_inner", "_outer")) {
    $parent = switch ($kind) { "_inner" { "minecraft:block/inner_stairs" } "_outer" { "minecraft:block/outer_stairs" } default { "minecraft:block/stairs" } }
    Write-Utf8 "$bm\$id$kind.json" @"
{
  "parent": "$parent",
  "textures": { "bottom": "$tex", "top": "$tex", "side": "$tex" }
}
"@
  }
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/$id`" }"
}

function SlabBlockstate($id, $texMap) {
  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add("{")
  $lines.Add('  "variants": {')
  $first = $true
  foreach ($pair in $texMap.GetEnumerator()) {
    $v = $pair.Key
    foreach ($type in @("bottom","top","double")) {
      $model = if ($type -eq "double") { "hbm:block/${id}_$v" } elseif ($type -eq "top") { "hbm:block/${id}_${v}_top" } else { "hbm:block/${id}_${v}_slab" }
      $comma = if ($first) { "" } else { "," }
      $first = $false
      $lines.Add("$comma    `"type=$type,variant=$v`": { `"model`": `"$model`" }")
    }
  }
  $lines.Add("  }")
  $lines.Add("}")
  Write-Utf8 "$bs\$id.json" ($lines -join "`n")
  foreach ($pair in $texMap.GetEnumerator()) {
    $v = $pair.Key
    $tex = $pair.Value
    Write-Utf8 "$bm\${id}_$v.json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "$tex" }
}
"@
    Write-Utf8 "$bm\${id}_${v}_slab.json" @"
{
  "parent": "minecraft:block/slab",
  "textures": { "bottom": "$tex", "top": "$tex", "side": "$tex" }
}
"@
    Write-Utf8 "$bm\${id}_${v}_top.json" @"
{
  "parent": "minecraft:block/slab_top",
  "textures": { "bottom": "$tex", "top": "$tex", "side": "$tex" }
}
"@
  }
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/${id}_0_slab`" }"
}

# --- filing cabinet / safe ---
$cabVars = New-Object System.Collections.Generic.List[string]
$cabVars.Add("{")
$cabVars.Add('  "variants": {')
$i = 0
foreach ($steel in @("false","true")) {
  $model = if ($steel -eq "true") { "filing_cabinet_steel" } else { "filing_cabinet" }
  foreach ($f in @(@("north",0),@("south",180),@("west",270),@("east",90))) {
    $comma = if ($i -eq 0) { "" } else { "," }
    $i++
    $y = if ($f[1] -eq 0) { "" } else { ", `"y`": $($f[1])" }
    $cabVars.Add("$comma    `"facing=$($f[0]),steel=$steel`": { `"model`": `"hbm:block/$model`"$y }")
  }
}
$cabVars.Add("  }")
$cabVars.Add("}")
Write-Utf8 "$bs\filing_cabinet.json" ($cabVars -join "`n")
FacingObj "filing_cabinet" "file_cabinet.obj" "hbm:block/block_steel" | Out-Null
# overwrite block model for steel
Write-Utf8 "$bm\filing_cabinet.json" @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": true,
  "model": "hbm:models/obj/file_cabinet.obj",
  "textures": { "particle": "hbm:block/block_steel" }
}
"@
Write-Utf8 "$bm\filing_cabinet_steel.json" @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": true,
  "model": "hbm:models/obj/file_cabinet_steel.obj",
  "textures": { "particle": "hbm:block/block_steel" }
}
"@
Write-Utf8 "$im\filing_cabinet.json" @"
{
  "parent": "hbm:block/filing_cabinet",
  "display": {
    "gui": { "rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.7, 0.7, 0.7] },
    "ground": { "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35] },
    "fixed": { "scale": [0.5, 0.5, 0.5] }
  }
}
"@
Loot "filing_cabinet"

Write-Utf8 "$bs\safe.json" @"
{
  "variants": {
    "facing=north": { "model": "hbm:block/safe" },
    "facing=south": { "model": "hbm:block/safe", "y": 180 },
    "facing=west":  { "model": "hbm:block/safe", "y": 270 },
    "facing=east":  { "model": "hbm:block/safe", "y": 90 }
  }
}
"@
Write-Utf8 "$bm\safe.json" @"
{
  "parent": "minecraft:block/orientable",
  "textures": {
    "top": "hbm:block/safe_side",
    "front": "hbm:block/safe_front",
    "side": "hbm:block/safe_side"
  }
}
"@
Write-Utf8 "$im\safe.json" "{ `"parent`": `"hbm:block/safe`" }"
Loot "safe"

# --- pipes ---
$pipes = @{
  "deco_pipe" = @("hbm:block/pipe_side","hbm:block/pipe_top")
  "deco_pipe_rusted" = @("hbm:block/pipe_side_rusty","hbm:block/pipe_top_rusty")
  "deco_pipe_red" = @("hbm:block/pipe_side_red","hbm:block/pipe_top_red")
  "deco_pipe_marked" = @("hbm:block/pipe_side_marked","hbm:block/pipe_top_marked")
  "deco_pipe_rim_green" = @("hbm:block/pipe_side_green","hbm:block/pipe_top_green")
  "deco_pipe_rim_marked" = @("hbm:block/pipe_side_marked","hbm:block/pipe_top_marked")
  "deco_pipe_rim_rusted" = @("hbm:block/pipe_side_rusty","hbm:block/pipe_top_rusty")
  "deco_pipe_framed" = @("hbm:block/pipe_side","hbm:block/pipe_top")
  "deco_pipe_framed_rusted" = @("hbm:block/pipe_side_rusty","hbm:block/pipe_top_rusty")
  "deco_pipe_framed_red" = @("hbm:block/pipe_side_red","hbm:block/pipe_top_red")
  "deco_pipe_framed_green_rusted" = @("hbm:block/pipe_side_green_rusty","hbm:block/pipe_top_green_rusty")
  "deco_pipe_quad" = @("hbm:block/pipe_side","hbm:block/pipe_top")
  "deco_pipe_quad_rusted" = @("hbm:block/pipe_side_rusty","hbm:block/pipe_top_rusty")
  "deco_pipe_quad_red" = @("hbm:block/pipe_side_red","hbm:block/pipe_top_red")
  "deco_pipe_quad_marked" = @("hbm:block/pipe_side_marked","hbm:block/pipe_top_marked")
}
foreach ($id in $pipes.Keys) {
  Pillar $id $pipes[$id][0] $pipes[$id][1]
  Loot $id
}

# --- grates ---
function Grate($id, $topTex) {
  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add("{")
  $lines.Add('  "variants": {')
  for ($o = 0; $o -le 9; $o++) {
    $comma = if ($o -eq 0) { "" } else { "," }
    $lines.Add("$comma    `"offset=$o`": { `"model`": `"hbm:block/${id}_$o`" }")
    $y = if ($o -eq 9) { -2 } else { $o * 2 }
    $y2 = $y + 2
    Write-Utf8 "$bm\${id}_$o.json" @"
{
  "parent": "minecraft:block/block",
  "textures": { "all": "$topTex", "particle": "$topTex" },
  "elements": [
    {
      "from": [0, $y, 0],
      "to": [16, $y2, 16],
      "faces": {
        "up": { "texture": "#all" }, "down": { "texture": "#all" },
        "north": { "texture": "hbm:block/grate_side" },
        "south": { "texture": "hbm:block/grate_side" },
        "west": { "texture": "hbm:block/grate_side" },
        "east": { "texture": "hbm:block/grate_side" }
      }
    }
  ]
}
"@
  }
  $lines.Add("  }")
  $lines.Add("}")
  Write-Utf8 "$bs\$id.json" ($lines -join "`n")
  Write-Utf8 "$im\$id.json" "{ `"parent`": `"hbm:block/${id}_0`" }"
  Loot $id
}
Grate "steel_grate" "hbm:block/grate_top"
Grate "steel_grate_wide" "hbm:block/grate_wide_top"

# --- stairs ---
$stairs = @{
  "concrete_stairs" = "hbm:block/concrete"
  "concrete_smooth_stairs" = "hbm:block/concrete_smooth"
  "concrete_asbestos_stairs" = "hbm:block/concrete_asbestos"
  "brick_concrete_stairs" = "hbm:block/brick_concrete"
  "brick_concrete_mossy_stairs" = "hbm:block/brick_concrete_mossy"
  "brick_concrete_cracked_stairs" = "hbm:block/brick_concrete_cracked"
  "brick_concrete_broken_stairs" = "hbm:block/brick_concrete_broken"
  "brick_light_stairs" = "hbm:block/brick_light"
  "brick_compound_stairs" = "hbm:block/brick_compound"
  "brick_obsidian_stairs" = "hbm:block/brick_obsidian"
  "reinforced_brick_stairs" = "hbm:block/reinforced_brick"
  "reinforced_stone_stairs" = "hbm:block/reinforced_stone"
  "lightstone_bricks_stairs" = "hbm:block/lightstone.bricks"
}
foreach ($id in $stairs.Keys) { Stairs $id $stairs[$id]; Loot $id }

# --- slabs ---
$cSlab = [ordered]@{
  0 = "hbm:block/concrete_smooth"
  1 = "hbm:block/concrete"
  2 = "hbm:block/concrete_asbestos"
  3 = "hbm:block/ducrete_tile"
  4 = "hbm:block/ducrete"
  5 = "hbm:block/asphalt"
  6 = "hbm:block/concrete"
  7 = "hbm:block/concrete"
}
$cbSlab = [ordered]@{
  0 = "hbm:block/brick_concrete"
  1 = "hbm:block/brick_concrete_mossy"
  2 = "hbm:block/brick_concrete_cracked"
  3 = "hbm:block/brick_concrete_broken"
  4 = "hbm:block/brick_ducrete"
  5 = "hbm:block/brick_concrete"
  6 = "hbm:block/brick_concrete"
  7 = "hbm:block/brick_concrete"
}
$bSlab = [ordered]@{
  0 = "hbm:block/reinforced_stone"
  1 = "hbm:block/reinforced_brick"
  2 = "hbm:block/brick_obsidian"
  3 = "hbm:block/brick_light"
  4 = "hbm:block/brick_compound"
  5 = "hbm:block/brick_asbestos"
  6 = "hbm:block/brick_fire"
  7 = "hbm:block/reinforced_stone"
}
SlabBlockstate "concrete_slab" $cSlab; Loot "concrete_slab"
SlabBlockstate "concrete_brick_slab" $cbSlab; Loot "concrete_brick_slab"
SlabBlockstate "brick_slab" $bSlab; Loot "brick_slab"
# item parent fix
Write-Utf8 "$im\concrete_slab.json" '{ "parent": "hbm:block/concrete_slab_0_slab" }'
Write-Utf8 "$im\concrete_brick_slab.json" '{ "parent": "hbm:block/concrete_brick_slab_0_slab" }'
Write-Utf8 "$im\brick_slab.json" '{ "parent": "hbm:block/brick_slab_0_slab" }'

# --- colored concrete ---
$dye = @("white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black")
$dyeTex = @{ "light_gray" = "hbm:block/concrete_silver"; "light_blue" = "hbm:block/concrete_light_blue" }
$cc = New-Object System.Collections.Generic.List[string]
$cc.Add("{"); $cc.Add('  "variants": {')
for ($d = 0; $d -lt 16; $d++) {
  $name = $dye[$d]
  $tex = if ($dyeTex.ContainsKey($name)) { $dyeTex[$name] } else { "hbm:block/concrete_$name" }
  $comma = if ($d -eq 0) { "" } else { "," }
  $cc.Add("$comma    `"color=$name`": { `"model`": `"hbm:block/concrete_colored_$name`" }")
  Write-Utf8 "$bm\concrete_colored_$name.json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "$tex" }
}
"@
}
$cc.Add("  }"); $cc.Add("}")
Write-Utf8 "$bs\concrete_colored.json" ($cc -join "`n")
Write-Utf8 "$im\concrete_colored.json" '{ "parent": "hbm:block/concrete_colored_white" }'
Loot "concrete_colored"

$ext = @("machine","machine_stripe","indigo","purple","pink","hazard","sand","bronze")
$ce = New-Object System.Collections.Generic.List[string]
$ce.Add("{"); $ce.Add('  "variants": {')
for ($v = 0; $v -le 8; $v++) {
  $name = if ($v -lt $ext.Count) { $ext[$v] } else { "machine" }
  $tex = "hbm:block/concrete_colored_ext.$name"
  $comma = if ($v -eq 0) { "" } else { "," }
  $ce.Add("$comma    `"variant=$v`": { `"model`": `"hbm:block/concrete_colored_ext_$v`" }")
  Write-Utf8 "$bm\concrete_colored_ext_$v.json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "$tex" }
}
"@
}
$ce.Add("  }"); $ce.Add("}")
Write-Utf8 "$bs\concrete_colored_ext.json" ($ce -join "`n")
Write-Utf8 "$im\concrete_colored_ext.json" '{ "parent": "hbm:block/concrete_colored_ext_0" }'
Loot "concrete_colored_ext"

Pillar "concrete_pillar" "hbm:block/concrete_pillar_side" "hbm:block/concrete_pillar_top"; Loot "concrete_pillar"

# lightstone
$ls = @("unrefined","tile","bricks","bricks_chiseled","chiseled")
$lsj = New-Object System.Collections.Generic.List[string]
$lsj.Add("{"); $lsj.Add('  "variants": {')
for ($v = 0; $v -lt 5; $v++) {
  $comma = if ($v -eq 0) { "" } else { "," }
  $lsj.Add("$comma    `"variant=$($ls[$v])`": { `"model`": `"hbm:block/lightstone_$($ls[$v])`" }")
  Write-Utf8 "$bm\lightstone_$($ls[$v]).json" @"
{
  "parent": "minecraft:block/cube_all",
  "textures": { "all": "hbm:block/lightstone.$($ls[$v])" }
}
"@
}
$lsj.Add("  }"); $lsj.Add("}")
Write-Utf8 "$bs\lightstone.json" ($lsj -join "`n")
Write-Utf8 "$im\lightstone.json" '{ "parent": "hbm:block/lightstone_unrefined" }'
Loot "lightstone"

# remaining cubes / facing
CubeAll "ntm_dirt" "minecraft:block/dirt"; Loot "ntm_dirt"
CubeAll "wood_structure" "hbm:block/wood_barrier"; Loot "wood_structure"
CubeAll "steel_corner" "hbm:block/steel_wall"; Loot "steel_corner"
# overwrite steel_corner as wall-facing - SteelWallBlock uses facing
FacingCube "steel_corner" "hbm:block/steel_wall"
Loot "steel_corner"

FacingCube "steel_poles" "hbm:block/deco_steel_poles"; Loot "steel_poles"
FacingCube "pole_top" "hbm:block/deco_pole_top"; Loot "pole_top"
FacingCube "charger" "hbm:block/block_steel"; Loot "charger"
FacingObj "tesla" "tesla.obj" "hbm:block/tesla"; Loot "tesla"
FacingCube "radiorec" "hbm:block/radiorec"; Loot "radiorec"
FacingCube "hev_battery" "hbm:block/hev_battery"; Loot "hev_battery"
FacingCube "machine_funnel" "hbm:block/machine_funnel"; Loot "machine_funnel"
FacingCube "machine_microwave" "hbm:block/machine_microwave"; Loot "machine_microwave"
FacingCube "machine_controller" "hbm:block/machine_controller"; Loot "machine_controller"
FacingCube "machine_fluidtank" "hbm:block/machine_fluidtank"; Loot "machine_fluidtank"
FacingCube "machine_weapon_table" "hbm:block/block_steel"; Loot "machine_weapon_table"
FacingCube "machine_rotary_furnace" "hbm:block/brick_fire"; Loot "machine_rotary_furnace"
FacingCube "bobblehead" "hbm:block/block_steel"; Loot "bobblehead"
FacingCube "skeleton_holder" "minecraft:block/soul_sand"; Loot "skeleton_holder"
FacingCube "turret_howard_damaged" "hbm:block/block_steel"; Loot "turret_howard_damaged"
FacingCube "turret_sentry_damaged" "hbm:block/block_steel"; Loot "turret_sentry_damaged"
FacingCube "rail_narrow" "hbm:block/rail_narrow"; Loot "rail_narrow"
Pillar "meteor_battery" "hbm:block/meteor_spawner_side" "hbm:block/meteor_power"; Loot "meteor_battery"

# plant / leaves
Write-Utf8 "$bs\plant_dead.json" '{ "variants": { "": { "model": "hbm:block/plant_dead" } } }'
Write-Utf8 "$bm\plant_dead.json" @"
{
  "parent": "minecraft:block/cross",
  "textures": { "cross": "hbm:block/plant_dead.generic" }
}
"@
Write-Utf8 "$im\plant_dead.json" '{ "parent": "minecraft:item/generated", "textures": { "layer0": "hbm:block/plant_dead.generic" } }'
Loot "plant_dead"

Write-Utf8 "$bs\leaves_layer.json" '{ "variants": { "": { "model": "hbm:block/leaves_layer" } } }'
Write-Utf8 "$bm\leaves_layer.json" @"
{
  "parent": "minecraft:block/block",
  "textures": { "all": "hbm:block/waste_leaves", "particle": "hbm:block/waste_leaves" },
  "elements": [{
    "from": [0, 0, 0], "to": [16, 2, 16],
    "faces": {
      "up": { "texture": "#all" }, "down": { "texture": "#all" },
      "north": { "texture": "#all" }, "south": { "texture": "#all" },
      "west": { "texture": "#all" }, "east": { "texture": "#all" }
    }
  }]
}
"@
Write-Utf8 "$im\leaves_layer.json" '{ "parent": "hbm:block/leaves_layer" }'
Loot "leaves_layer"

# trapdoor
$trap = @"
{
  "variants": {
    "facing=north,half=bottom,open=false": { "model": "hbm:block/trapdoor_steel_bottom" },
    "facing=south,half=bottom,open=false": { "model": "hbm:block/trapdoor_steel_bottom", "y": 180 },
    "facing=east,half=bottom,open=false": { "model": "hbm:block/trapdoor_steel_bottom", "y": 90 },
    "facing=west,half=bottom,open=false": { "model": "hbm:block/trapdoor_steel_bottom", "y": 270 },
    "facing=north,half=top,open=false": { "model": "hbm:block/trapdoor_steel_top" },
    "facing=south,half=top,open=false": { "model": "hbm:block/trapdoor_steel_top", "y": 180 },
    "facing=east,half=top,open=false": { "model": "hbm:block/trapdoor_steel_top", "y": 90 },
    "facing=west,half=top,open=false": { "model": "hbm:block/trapdoor_steel_top", "y": 270 },
    "facing=north,half=bottom,open=true": { "model": "hbm:block/trapdoor_steel_open" },
    "facing=south,half=bottom,open=true": { "model": "hbm:block/trapdoor_steel_open", "y": 180 },
    "facing=east,half=bottom,open=true": { "model": "hbm:block/trapdoor_steel_open", "y": 90 },
    "facing=west,half=bottom,open=true": { "model": "hbm:block/trapdoor_steel_open", "y": 270 },
    "facing=north,half=top,open=true": { "model": "hbm:block/trapdoor_steel_open", "x": 180, "y": 180 },
    "facing=south,half=top,open=true": { "model": "hbm:block/trapdoor_steel_open", "x": 180, "y": 0 },
    "facing=east,half=top,open=true": { "model": "hbm:block/trapdoor_steel_open", "x": 180, "y": 270 },
    "facing=west,half=top,open=true": { "model": "hbm:block/trapdoor_steel_open", "x": 180, "y": 90 }
  }
}
"@
# powered is ignored by using multipart-less extra - Minecraft requires all properties
# regenerate with powered
$trapLines = New-Object System.Collections.Generic.List[string]
$trapLines.Add("{"); $trapLines.Add('  "variants": {')
$ti = 0
foreach ($pow in @("false","true")) {
  foreach ($half in @("bottom","top")) {
    foreach ($open in @("false","true")) {
      foreach ($face in @(@("north",0),@("south",180),@("east",90),@("west",270))) {
        $model = if ($open -eq "true") { "trapdoor_steel_open" } elseif ($half -eq "top") { "trapdoor_steel_top" } else { "trapdoor_steel_bottom" }
        $x = ""; $y = $face[1]
        if ($open -eq "true" -and $half -eq "top") { $x = '"x": 180, ' }
        $comma = if ($ti -eq 0) { "" } else { "," }
        $ti++
        $rot = if ($y -eq 0 -and $x -eq "") { "" } elseif ($x -ne "" -and $y -eq 0) { ", $x`"y`": 0" } elseif ($x -ne "") { ", $x`"y`": $y" } else { ", `"y`": $y" }
        # cleaner:
        $extra = ""
        if ($open -eq "true" -and $half -eq "top") {
          $yy = switch ($face[0]) { "north" { 180 } "south" { 0 } "east" { 270 } default { 90 } }
          $extra = ", `"x`": 180, `"y`": $yy"
        } elseif ($y -ne 0) {
          $extra = ", `"y`": $y"
        }
        $trapLines.Add("$comma    `"facing=$($face[0]),half=$half,open=$open,powered=$pow`": { `"model`": `"hbm:block/$model`"$extra }")
      }
    }
  }
}
$trapLines.Add("  }"); $trapLines.Add("}")
Write-Utf8 "$bs\trapdoor_steel.json" ($trapLines -join "`n")
foreach ($kind in @("bottom","top","open")) {
  $parent = switch ($kind) { "top" { "minecraft:block/template_orientable_trapdoor_top" } "open" { "minecraft:block/template_orientable_trapdoor_open" } default { "minecraft:block/template_orientable_trapdoor_bottom" } }
  Write-Utf8 "$bm\trapdoor_steel_$kind.json" @"
{
  "parent": "$parent",
  "textures": { "texture": "hbm:block/trapdoor_steel" }
}
"@
}
Write-Utf8 "$im\trapdoor_steel.json" '{ "parent": "hbm:block/trapdoor_steel_bottom" }'
Loot "trapdoor_steel"

function DoorAssets($id) {
  $faces = @(@("east",0),@("south",90),@("west",180),@("north",270))
  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add("{"); $lines.Add('  "variants": {')
  $n = 0
  foreach ($pow in @("false","true")) {
    foreach ($open in @("false","true")) {
      foreach ($hinge in @("left","right")) {
        foreach ($half in @("lower","upper")) {
          foreach ($f in $faces) {
            $facing = $f[0]; $baseY = $f[1]
            $suffix = if ($half -eq "lower") { "bottom" } else { "top" }
            $hand = $hinge
            if ($open -eq "true") {
              $model = "hbm:block/${id}_${suffix}_${hand}_open"
              $y = $baseY
            } else {
              $model = "hbm:block/${id}_${suffix}_$hand"
              $y = $baseY
            }
            $comma = if ($n -eq 0) { "" } else { "," }
            $n++
            $extra = if ($y -eq 0) { "" } else { ", `"y`": $y" }
            $lines.Add("$comma    `"facing=$facing,half=$half,hinge=$hinge,open=$open,powered=$pow`": { `"model`": `"$model`"$extra }")
          }
        }
      }
    }
  }
  $lines.Add("  }"); $lines.Add("}")
  Write-Utf8 "$bs\$id.json" ($lines -join "`n")
  $bottom = "hbm:block/${id}_lower"
  $top = "hbm:block/${id}_upper"
  foreach ($part in @("bottom_left","bottom_left_open","bottom_right","bottom_right_open","top_left","top_left_open","top_right","top_right_open")) {
    $parent = "minecraft:block/door_$part"
    Write-Utf8 "$bm\${id}_$part.json" @"
{
  "parent": "$parent",
  "textures": { "bottom": "$bottom", "top": "$top" }
}
"@
  }
  Write-Utf8 "$im\$id.json" @"
{
  "parent": "minecraft:item/generated",
  "textures": { "layer0": "hbm:item/${id}" }
}
"@
  Loot $id
}

# door item textures: use lower panel as item if item png missing
foreach ($d in @("door_metal","door_office","door_bunker")) {
  DoorAssets $d
  $itemTex = "$root\assets\hbm\textures\item\$d.png"
  $src = "$root\assets\hbm\textures\block\${d}_lower.png"
  if (-not (Test-Path $itemTex) -and (Test-Path $src)) {
    Copy-Item $src $itemTex -Force
  }
}

Write-Host "assets generated"
