$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$outDir = Join-Path $root "src/main/resources/assets/hbm/models/obj"
$blockDir = Join-Path $root "src/main/resources/assets/hbm/models/block"
$utf8 = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$path, [string]$content) {
  [System.IO.File]::WriteAllText($path, $content, $utf8)
}

function Copy-FractionObj([string]$name, [string]$tex) {
  $src = Join-Path $root "src/main/resources/assets/hbm/models/legacy_raw/machines/$name.obj"
  $raw = [System.IO.File]::ReadAllText($src)
  if ($raw.StartsWith([char]0xFEFF)) { $raw = $raw.Substring(1) }
  if (-not $raw.Contains("mtllib ")) {
    $raw = "mtllib $name.mtl`n" + $raw
  }
  if (-not $raw.Contains("usemtl ")) {
    $raw = $raw -replace "(?m)^o ", "usemtl material`no "
  }
  Write-Utf8NoBom (Join-Path $outDir "$name.obj") $raw
  Write-Utf8NoBom (Join-Path $outDir "$name.mtl") "newmtl material`nKd 1 1 1`nmap_Kd $tex`n"
  $blockJson = @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/$name.obj",
  "mtl_override": "hbm:models/obj/$name.mtl",
  "textures": {
    "particle": "$tex",
    "material": "$tex",
    "#material": "$tex"
  }
}
"@
  Write-Utf8NoBom (Join-Path $blockDir "$name.json") $blockJson
  Write-Host "$name.obj copied"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null
Copy-FractionObj "fraction_tower" "hbm:models/machines/fraction_tower"
Copy-FractionObj "fraction_spacer" "hbm:models/machines/fraction_spacer"
Write-Utf8NoBom (Join-Path $blockDir "machine_fraction_tower.json") (Get-Content (Join-Path $blockDir "fraction_tower.json") -Raw)
Write-Utf8NoBom (Join-Path $blockDir "fraction_tower_dummy.json") @"
{
  "parent": "minecraft:builtin/entity",
  "textures": { "particle": "hbm:models/machines/fraction_tower" }
}
"@
Write-Utf8NoBom (Join-Path $blockDir "fraction_spacer_dummy.json") @"
{
  "parent": "minecraft:builtin/entity",
  "textures": { "particle": "hbm:models/machines/fraction_spacer" }
}
"@
Write-Host "fraction models ready"
