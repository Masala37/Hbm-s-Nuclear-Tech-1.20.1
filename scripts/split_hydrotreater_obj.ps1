$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$src = Join-Path $root "src/main/resources/assets/hbm/models/legacy_raw/machines/hydrotreater.obj"
$outDir = Join-Path $root "src/main/resources/assets/hbm/models/obj"
$blockDir = Join-Path $root "src/main/resources/assets/hbm/models/block"
$tex = "hbm:models/machines/hydrotreater"
$utf8 = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$path, [string]$content) {
  [System.IO.File]::WriteAllText($path, $content, $utf8)
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null

$raw = [System.IO.File]::ReadAllText($src)
if ($raw.StartsWith([char]0xFEFF)) { $raw = $raw.Substring(1) }
$lines = New-Object System.Collections.Generic.List[string]
foreach ($line in $raw -split "`r?`n") {
  $trim = $line.Trim()
  if ($trim.Length -eq 0) { continue }
  if ($trim.StartsWith("#")) { continue }
  if ($trim.StartsWith("mtllib ")) { continue }
  if ($trim.StartsWith("usemtl ")) { continue }
  if ($trim -match "Blender|www\.blender") { continue }
  [void]$lines.Add($line)
}
$raw = "mtllib hydrotreater.mtl`nusemtl material`n" + ($lines -join "`n") + "`n"
Write-Utf8NoBom (Join-Path $outDir "hydrotreater.obj") $raw
Write-Utf8NoBom (Join-Path $outDir "hydrotreater.mtl") "newmtl material`nKd 1 1 1`nmap_Kd $tex`n"
$blockJson = @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/hydrotreater.obj",
  "mtl_override": "hbm:models/obj/hydrotreater.mtl",
  "textures": {
    "particle": "$tex",
    "material": "$tex",
    "#material": "$tex"
  }
}
"@
Write-Utf8NoBom (Join-Path $blockDir "hydrotreater.json") $blockJson
Write-Utf8NoBom (Join-Path $blockDir "machine_hydrotreater.json") $blockJson
Write-Utf8NoBom (Join-Path $blockDir "hydrotreater_dummy.json") @"
{
  "parent": "minecraft:builtin/entity",
  "textures": { "particle": "$tex" }
}
"@
Write-Host "hydrotreater.obj copied"
