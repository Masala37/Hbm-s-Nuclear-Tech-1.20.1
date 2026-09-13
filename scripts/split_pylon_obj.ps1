$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$srcDir = Join-Path $root "src/main/resources/assets/hbm/models/legacy_raw/network"
$outDir = Join-Path $root "src/main/resources/assets/hbm/models/obj/network"
$blockDir = Join-Path $root "src/main/resources/assets/hbm/models/block"
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

function Mtl([string]$tex) {
  return "newmtl material`nKd 1 1 1`nmap_Kd $tex`n"
}

function Block-Json([string]$objSlug, [string]$mtlSlug, [string]$partTex) {
  return @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "detect_cullable_faces": false,
  "shade_quads": false,
  "model": "hbm:models/obj/network/$objSlug.obj",
  "mtl_override": "hbm:models/obj/network/$mtlSlug.mtl",
  "textures": {
    "particle": "$partTex",
    "material": "$partTex",
    "#material": "$partTex"
  }
}
"@
}

function Parse-Obj([string]$src) {
  $verts = New-Object System.Collections.Generic.List[string]
  $texs = New-Object System.Collections.Generic.List[string]
  $norms = New-Object System.Collections.Generic.List[string]
  $objects = New-Object System.Collections.Generic.List[hashtable]
  $current = $null
  Get-Content -LiteralPath $src -Encoding UTF8 | ForEach-Object {
    $line = $_
    if ($line.StartsWith("o ")) {
      $current = @{ name = $line.Substring(2).Trim(); faces = New-Object System.Collections.Generic.List[object]; smoothing = $null }
      $objects.Add($current)
      return
    }
    if ($line.StartsWith("v ")) { $verts.Add($line); return }
    if ($line.StartsWith("vt ")) { $texs.Add($line); return }
    if ($line.StartsWith("vn ")) { $norms.Add($line); return }
    if ($line.StartsWith("s ") -and $null -ne $current) { $current.smoothing = $line; return }
    if (-not $line.StartsWith("f ")) { return }
    if ($null -eq $current) {
      $current = @{ name = "unnamed"; faces = New-Object System.Collections.Generic.List[object]; smoothing = $null }
      $objects.Add($current)
    }
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
  return @{ verts = $verts; texs = $texs; norms = $norms; objects = $objects }
}

function Emit-Parts($parsed, [string[]]$names, [string]$slug, [string]$tex) {
  $vs = New-Object System.Collections.Generic.List[int]
  $vts = New-Object System.Collections.Generic.List[int]
  $vns = New-Object System.Collections.Generic.List[int]
  $byName = @{}
  foreach ($obj in $parsed.objects) { $byName[$obj.name] = $obj }
  foreach ($name in $names) {
    if (-not $byName.ContainsKey($name)) { throw "missing object $name in $slug" }
    foreach ($face in $byName[$name].faces) {
      foreach ($c in $face) {
        [void]$vs.Add($c.v)
        [void]$vts.Add($c.vt)
        [void]$vns.Add($c.vn)
      }
    }
  }
  $usedV = Unique-Sorted $vs
  $usedVt = Unique-Sorted $vts
  $usedVn = Unique-Sorted $vns
  $mapV = Index-Map $usedV
  $mapVt = Index-Map $usedVt
  $mapVn = Index-Map $usedVn
  $lines = New-Object System.Collections.Generic.List[string]
  [void]$lines.Add("mtllib $slug.mtl")
  [void]$lines.Add("usemtl material")
  foreach ($index in $usedV) { [void]$lines.Add($parsed.verts[$index - 1]) }
  foreach ($index in $usedVt) { [void]$lines.Add($parsed.texs[$index - 1]) }
  foreach ($index in $usedVn) { [void]$lines.Add($parsed.norms[$index - 1]) }
  foreach ($name in $names) {
    $obj = $byName[$name]
    [void]$lines.Add("o $($obj.name)")
    if ($obj.smoothing) { [void]$lines.Add($obj.smoothing) }
    foreach ($face in $obj.faces) {
      $bits = @()
      foreach ($c in $face) { $bits += (Format-Corner $c $mapV $mapVt $mapVn) }
      [void]$lines.Add("f " + ($bits -join " "))
    }
  }
  $text = ($lines -join "`n") + "`n"
  Assert-Faces $text $slug
  Write-Utf8NoBom (Join-Path $outDir "$slug.obj") $text
  Write-Utf8NoBom (Join-Path $outDir "$slug.mtl") (Mtl $tex)
  Write-Host "$slug.obj verts $($usedV.Count)"
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null

$medium = Parse-Obj (Join-Path $srcDir "pylon_medium.obj")
Emit-Parts $medium @("Pylon") "pylon_medium" "hbm:models/network/pylon_medium"
Emit-Parts $medium @("Pylon", "Transformer") "pylon_medium_full" "hbm:models/network/pylon_medium"

function Copy-Whole([string]$srcName, [string]$slug, [string]$tex) {
  $parsed = Parse-Obj (Join-Path $srcDir $srcName)
  $names = @()
  foreach ($obj in $parsed.objects) { $names += $obj.name }
  if ($names.Count -eq 0) { throw "no objects in $srcName" }
  Emit-Parts $parsed $names $slug $tex
}

Copy-Whole "connector.obj" "connector" "hbm:models/network/connector"
Copy-Whole "pylon_large.obj" "pylon_large" "hbm:models/network/pylon_large"
Copy-Whole "substation.obj" "substation" "hbm:models/network/substation"

Write-Utf8NoBom (Join-Path $outDir "pylon_medium_steel.mtl") (Mtl "hbm:models/network/pylon_medium_steel")
Write-Utf8NoBom (Join-Path $outDir "pylon_medium_steel_full.mtl") (Mtl "hbm:models/network/pylon_medium_steel")

Write-Utf8NoBom (Join-Path $blockDir "red_connector.json") (Block-Json "connector" "connector" "hbm:models/network/connector")
Write-Utf8NoBom (Join-Path $blockDir "red_pylon_medium_wood.json") (Block-Json "pylon_medium" "pylon_medium" "hbm:models/network/pylon_medium")
Write-Utf8NoBom (Join-Path $blockDir "red_pylon_medium_wood_transformer.json") (Block-Json "pylon_medium_full" "pylon_medium_full" "hbm:models/network/pylon_medium")
Write-Utf8NoBom (Join-Path $blockDir "red_pylon_medium_steel.json") (Block-Json "pylon_medium" "pylon_medium_steel" "hbm:models/network/pylon_medium_steel")
Write-Utf8NoBom (Join-Path $blockDir "red_pylon_medium_steel_transformer.json") (Block-Json "pylon_medium_full" "pylon_medium_steel_full" "hbm:models/network/pylon_medium_steel")
Write-Utf8NoBom (Join-Path $blockDir "red_pylon_large.json") (Block-Json "pylon_large" "pylon_large" "hbm:models/network/pylon_large")
Write-Utf8NoBom (Join-Path $blockDir "substation.json") (Block-Json "substation" "substation" "hbm:models/network/substation")

Write-Host "done"
