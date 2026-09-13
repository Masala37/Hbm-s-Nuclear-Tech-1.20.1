$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$src = Join-Path $root "src/main/resources/assets/hbm/models/legacy_raw/refinery.obj"
$outDir = Join-Path $root "src/main/resources/assets/hbm/models/obj"
$blockDir = Join-Path $root "src/main/resources/assets/hbm/models/block"
$tex = "hbm:models/refinery"
$utf8 = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$path, [string]$content) {
  [System.IO.File]::WriteAllText($path, $content, $utf8)
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null

$raw = [System.IO.File]::ReadAllText($src)
if ($raw.StartsWith([char]0xFEFF)) { $raw = $raw.Substring(1) }
if (-not $raw.Contains("mtllib ")) {
  $raw = "mtllib refinery.mtl`n" + $raw
}
if (-not $raw.Contains("usemtl ")) {
  $raw = $raw -replace "(?m)^o ", "usemtl material`no "
}
Write-Utf8NoBom (Join-Path $outDir "refinery.obj") $raw
Write-Utf8NoBom (Join-Path $outDir "refinery.mtl") "newmtl material`nKd 1 1 1`nmap_Kd $tex`n"
$blockJson = @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/refinery.obj",
  "mtl_override": "hbm:models/obj/refinery.mtl",
  "textures": {
    "particle": "$tex",
    "material": "$tex",
    "#material": "$tex"
  }
}
"@
Write-Utf8NoBom (Join-Path $blockDir "refinery.json") $blockJson
Write-Utf8NoBom (Join-Path $blockDir "machine_refinery.json") $blockJson
Write-Utf8NoBom (Join-Path $blockDir "refinery_dummy.json") @"
{
  "parent": "minecraft:builtin/entity",
  "textures": { "particle": "hbm:models/refinery" }
}
"@
Write-Host "refinery.obj copied"
