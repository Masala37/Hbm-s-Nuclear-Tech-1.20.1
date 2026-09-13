$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$src = Join-Path $root "src/main/resources/assets/hbm/models/legacy_raw/machines/boiler.obj"
$outDir = Join-Path $root "src/main/resources/assets/hbm/models/obj"
$blockDir = Join-Path $root "src/main/resources/assets/hbm/models/block"
$tex = "hbm:models/machines/boiler"
$utf8 = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$path, [string]$content) {
  [System.IO.File]::WriteAllText($path, $content, $utf8)
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null

$raw = [System.IO.File]::ReadAllText($src)
if ($raw.StartsWith([char]0xFEFF)) { $raw = $raw.Substring(1) }
if (-not $raw.Contains("mtllib ")) {
  $raw = "mtllib boiler.mtl`n" + $raw
}
if (-not $raw.Contains("usemtl ")) {
  $raw = $raw -replace "(?m)^o ", "usemtl material`no "
}
Write-Utf8NoBom (Join-Path $outDir "boiler.obj") $raw
Write-Utf8NoBom (Join-Path $outDir "boiler.mtl") "newmtl material`nKd 1 1 1`nmap_Kd $tex`n"
Write-Utf8NoBom (Join-Path $blockDir "boiler.json") @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/boiler.obj",
  "mtl_override": "hbm:models/obj/boiler.mtl",
  "textures": {
    "particle": "$tex",
    "material": "$tex",
    "#material": "$tex"
  }
}
"@
Write-Utf8NoBom (Join-Path $blockDir "machine_boiler.json") @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/boiler.obj",
  "mtl_override": "hbm:models/obj/boiler.mtl",
  "textures": {
    "particle": "$tex",
    "material": "$tex",
    "#material": "$tex"
  }
}
"@
Write-Host "boiler.obj copied"
