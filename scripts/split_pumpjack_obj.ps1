$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$src = Join-Path $root "src/main/resources/assets/hbm/models/legacy_raw/machines/pumpjack.obj"
$outDir = Join-Path $root "src/main/resources/assets/hbm/models/obj"
$blockDir = Join-Path $root "src/main/resources/assets/hbm/models/block"
$tex = "hbm:models/machines/pumpjack"
$mtlBody = "newmtl material`nKd 1 1 1`nmap_Kd $tex`n"
$utf8 = New-Object System.Text.UTF8Encoding $false

function Write-Utf8NoBom([string]$path, [string]$content) {
  [System.IO.File]::WriteAllText($path, $content, $utf8)
}

function Abs-Index([string]$raw, [int]$count) {
  if ([string]::IsNullOrEmpty($raw)) { return 0 }
  $n = [int]$raw
  if ($n -eq 0) { return 0 }
  if ($n -lt 0) { return $count + $n + 1 }
  return $n
}

$verts = New-Object System.Collections.Generic.List[string]
$texs = New-Object System.Collections.Generic.List[string]
$norms = New-Object System.Collections.Generic.List[string]
$objects = New-Object System.Collections.Generic.List[hashtable]
$current = $null

function Start-Obj([string]$name) {
  $script:current = @{ name = $name; faces = New-Object System.Collections.Generic.List[object]; smoothing = $null }
  $script:objects.Add($script:current)
}

Get-Content -LiteralPath $src -Encoding UTF8 | ForEach-Object {
  $line = $_
  if ($line.StartsWith("o ")) {
    Start-Obj $line.Substring(2).Trim()
    return
  }
  if ($line.StartsWith("v ")) { $verts.Add($line); return }
  if ($line.StartsWith("vt ")) { $texs.Add($line); return }
  if ($line.StartsWith("vn ")) { $norms.Add($line); return }
  if ($line.StartsWith("s ") -and $null -ne $current) { $current.smoothing = $line; return }
  if (-not $line.StartsWith("f ")) { return }
  if ($null -eq $current) { Start-Obj "unnamed" }
  $corners = New-Object System.Collections.Generic.List[hashtable]
  foreach ($token in $line.Substring(2).Trim().Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)) {
    $bits = $token.Split("/")
    $corners.Add(@{
      v = Abs-Index $(if ($bits.Length -gt 0) { $bits[0] } else { "" }) $verts.Count
      vt = Abs-Index $(if ($bits.Length -gt 1) { $bits[1] } else { "" }) $texs.Count
      vn = Abs-Index $(if ($bits.Length -gt 2) { $bits[2] } else { "" }) $norms.Count
      style = $bits.Length
    })
  }
  $current.faces.Add($corners)
}

function Unique-Sorted($values) {
  $set = New-Object "System.Collections.Generic.SortedSet[int]"
  foreach ($n in $values) { if ($n -gt 0) { [void]$set.Add($n) } }
  return @($set)
}

function Index-Map([int[]]$used) {
  $map = @{}
  for ($i = 0; $i -lt $used.Length; $i++) {
    $map[[int]$used[$i]] = $i + 1
  }
  return $map
}

function Remap($map, [int]$old) {
  if ($old -le 0) { return 0 }
  if (-not $map.ContainsKey($old)) { return 0 }
  return [int]$map[$old]
}

function Format-Corner($corner, $mapV, $mapVt, $mapVn) {
  $v = Remap $mapV $corner.v
  $vt = Remap $mapVt $corner.vt
  $vn = Remap $mapVn $corner.vn
  if ($corner.style -le 1) { return "$v" }
  if ($corner.style -eq 2) { return "$v/$vt" }
  if ($corner.vt -eq 0) { return "$v//$vn" }
  return "$v/$vt/$vn"
}

function Assert-Faces([string]$text, [string]$slug) {
  $vCount = 0
  $faceMax = 0
  foreach ($line in $text -split "`n") {
    if ($line.StartsWith("v ")) { $vCount++ }
    if ($line.StartsWith("f ")) {
      foreach ($token in $line.Substring(2).Trim().Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)) {
        $n = 0
        [void][int]::TryParse($token.Split("/")[0], [ref]$n)
        if ($n -gt $faceMax) { $faceMax = $n }
      }
    }
  }
  if ($faceMax -gt $vCount) { throw "$slug remapped face $faceMax exceeds $vCount verts" }
}

function Block-Json([string]$slug, [string]$partTex) {
  return @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/$slug.obj",
  "mtl_override": "hbm:models/obj/$slug.mtl",
  "textures": {
    "particle": "$partTex",
    "material": "$partTex",
    "#material": "$partTex"
  }
}
"@
}

function Emit-Object($obj, [string]$mtllib, [string]$slug, [string]$partTex, [string]$partMtl) {
  $vs = New-Object System.Collections.Generic.List[int]
  $vts = New-Object System.Collections.Generic.List[int]
  $vns = New-Object System.Collections.Generic.List[int]
  foreach ($face in $obj.faces) {
    foreach ($c in $face) {
      [void]$vs.Add($c.v)
      [void]$vts.Add($c.vt)
      [void]$vns.Add($c.vn)
    }
  }
  $usedV = Unique-Sorted $vs
  $usedVt = Unique-Sorted $vts
  $usedVn = Unique-Sorted $vns
  $mapV = Index-Map $usedV
  $mapVt = Index-Map $usedVt
  $mapVn = Index-Map $usedVn
  $lines = New-Object System.Collections.Generic.List[string]
  [void]$lines.Add("mtllib $mtllib")
  [void]$lines.Add("usemtl material")
  [void]$lines.Add("o $($obj.name)")
  foreach ($index in $usedV) { [void]$lines.Add($verts[$index - 1]) }
  foreach ($index in $usedVt) { [void]$lines.Add($texs[$index - 1]) }
  foreach ($index in $usedVn) { [void]$lines.Add($norms[$index - 1]) }
  if ($obj.smoothing) { [void]$lines.Add($obj.smoothing) }
  foreach ($face in $obj.faces) {
    $bits = @()
    foreach ($c in $face) { $bits += (Format-Corner $c $mapV $mapVt $mapVn) }
    [void]$lines.Add("f " + ($bits -join " "))
  }
  $text = ($lines -join "`n") + "`n"
  Assert-Faces $text $slug
  Write-Utf8NoBom (Join-Path $outDir "$slug.obj") $text
  Write-Utf8NoBom (Join-Path $outDir "$slug.mtl") $partMtl
  Write-Utf8NoBom (Join-Path $blockDir "$slug.json") (Block-Json $slug $partTex)
  Write-Host "$slug.obj faces $($obj.faces.Count) verts $($usedV.Count)"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null

$byName = @{}
foreach ($obj in $objects) { $byName[$obj.name] = $obj }
foreach ($name in @("Base","Rotor","Head","Carriage")) {
  if (-not $byName.ContainsKey($name)) { throw "missing object $name" }
}
Emit-Object $byName["Base"] "pumpjack_base.mtl" "pumpjack_base" $tex $mtlBody
Emit-Object $byName["Rotor"] "pumpjack_rotor.mtl" "pumpjack_rotor" $tex $mtlBody
Emit-Object $byName["Head"] "pumpjack_head.mtl" "pumpjack_head" $tex $mtlBody
Emit-Object $byName["Carriage"] "pumpjack_carriage.mtl" "pumpjack_carriage" $tex $mtlBody

$raw = [System.IO.File]::ReadAllText($src)
if ($raw.StartsWith([char]0xFEFF)) { $raw = $raw.Substring(1) }
if (-not $raw.Contains("mtllib ")) { $raw = "mtllib pumpjack_item.mtl`n" + $raw }
if (-not $raw.Contains("usemtl ")) { $raw = $raw -replace "(?m)^o ", "usemtl material`no " }
Write-Utf8NoBom (Join-Path $outDir "pumpjack_item.obj") $raw
Write-Utf8NoBom (Join-Path $outDir "pumpjack_item.mtl") $mtlBody
Write-Utf8NoBom (Join-Path $blockDir "pumpjack_item.json") (Block-Json "pumpjack_item" $tex)
Write-Utf8NoBom (Join-Path $blockDir "machine_pumpjack.json") (Block-Json "pumpjack_item" $tex)
Write-Utf8NoBom (Join-Path $blockDir "pumpjack_dummy.json") @"
{
  "parent": "minecraft:builtin/entity",
  "textures": { "particle": "hbm:models/machines/pumpjack" }
}
"@
Write-Host "done"
