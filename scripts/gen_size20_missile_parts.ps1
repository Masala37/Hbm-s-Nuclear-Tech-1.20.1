$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $Root "src"))) {
    $Root = (Get-Location).Path
}
$Assets = Join-Path $Root "src\main\resources\assets\hbm"
$ObjSrc = Join-Path $Root "legacy-1.7.10\src\main\resources\assets\hbm\models\missile_parts"
$ObjAlt = Join-Path $Assets "models\legacy_raw\missile_parts"
$ObjDst = Join-Path $Assets "models\obj\missile_parts"
$MissileObjDst = Join-Path $Assets "models\obj"
$BlockDst = Join-Path $Assets "models\block\missile_part"
$MissileBlockDst = Join-Path $Assets "models\block"
$ItemDst = Join-Path $Assets "models\item"
$TexDst = Join-Path $Assets "textures\block\missile_parts"
$MissileTexDst = Join-Path $Assets "textures\block\missile"
$TexBase = "https://raw.githubusercontent.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/master/src/main/resources/assets/hbm/textures/models/"

New-Item -ItemType Directory -Force -Path $ObjDst, $MissileObjDst, $BlockDst, $MissileBlockDst, $ItemDst, $TexDst, $MissileTexDst | Out-Null

function Convert-Obj([string]$srcPath, [string]$dstPath, [string]$mtlName) {
    $text = Get-Content -Raw -Path $srcPath
    if ($text -notmatch "^mtllib") {
        $text = "mtllib $mtlName`nusemtl material`n$text"
    }
    Set-Content -Path $dstPath -Value $text -NoNewline
}

function Write-Mtl([string]$path, [string]$mapKd) {
    Set-Content -Path $path -Value "newmtl material`nKd 1 1 1`nmap_Kd $mapKd`n"
}

function Write-PartJson([string]$item, [string]$obj, [string]$mtl, [string]$texStem) {
    $json = @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/missile_parts/$obj.obj",
  "mtl_override": "hbm:models/obj/missile_parts/$mtl.mtl",
  "textures": {
    "particle": "hbm:block/missile_parts/$texStem",
    "material": "hbm:block/missile_parts/$texStem",
    "#material": "hbm:block/missile_parts/$texStem"
  }
}
"@
    Set-Content -Path (Join-Path $BlockDst "$item.json") -Value $json
    Set-Content -Path (Join-Path $ItemDst "$item.json") -Value '{ "parent": "hbm:item/missile_bewlr" }'
}

$parts = @(
    @{ Item = "mp_thruster_20_kerosene"; Obj = "mp_t_20_kerosene"; Rel = "missile_parts/thrusters/mp_t_20_kerosene.png"; Mtl = "mp_t_20_kerosene"; Tex = "mp_t_20_kerosene" }
    @{ Item = "mp_thruster_20_kerosene_dual"; Obj = "mp_t_20_kerosene_dual"; Rel = "missile_parts/thrusters/mp_t_20_kerosene_dual.png"; Mtl = "mp_t_20_kerosene_dual"; Tex = "mp_t_20_kerosene_dual" }
    @{ Item = "mp_thruster_20_kerosene_triple"; Obj = "mp_t_20_kerosene_triple"; Rel = "missile_parts/thrusters/mp_t_20_kerosene_dual.png"; Mtl = "mp_t_20_kerosene_triple"; Tex = "mp_t_20_kerosene_dual" }
    @{ Item = "mp_thruster_20_solid"; Obj = "mp_t_20_solid"; Rel = "missile_parts/thrusters/mp_t_20_solid.png"; Mtl = "mp_t_20_solid"; Tex = "mp_t_20_solid" }
    @{ Item = "mp_thruster_20_solid_multi"; Obj = "mp_t_20_solid_multi"; Rel = "missile_parts/thrusters/mp_t_20_solid_multi.png"; Mtl = "mp_t_20_solid_multi"; Tex = "mp_t_20_solid_multi" }
    @{ Item = "mp_thruster_20_solid_multier"; Obj = "mp_t_20_solid_multi"; Rel = "missile_parts/thrusters/mp_t_20_solid_multier.png"; Mtl = "mp_t_20_solid_multier"; Tex = "mp_t_20_solid_multier" }
    @{ Item = "mp_s_20"; Obj = "mp_s_20"; Rel = "TheGadget3_.png"; Mtl = "mp_s_20"; Tex = "mp_s_20" }
)

$objs = $parts | ForEach-Object { $_.Obj } | Sort-Object -Unique
foreach ($stem in $objs) {
    $src = Join-Path $ObjSrc "$stem.obj"
    if (-not (Test-Path $src)) { $src = Join-Path $ObjAlt "$stem.obj" }
    if (-not (Test-Path $src)) { throw "missing obj $stem" }
    Convert-Obj $src (Join-Path $ObjDst "$stem.obj") "$stem.mtl"
}

$downloaded = @{}
foreach ($p in $parts) {
    if (-not $downloaded.ContainsKey($p.Rel)) {
        $dest = Join-Path $TexDst "$($p.Tex).png"
        Invoke-WebRequest -Uri ($TexBase + $p.Rel) -OutFile $dest -UseBasicParsing -TimeoutSec 30
        Write-Host "ok $($p.Tex) $((Get-Item $dest).Length)"
        $downloaded[$p.Rel] = $true
    }
    Write-Mtl (Join-Path $ObjDst "$($p.Mtl).mtl") "hbm:block/missile_parts/$($p.Tex)"
    Write-PartJson $p.Item $p.Obj $p.Mtl $p.Tex
}

$abmSrc = Join-Path $Root "legacy-1.7.10\src\main\resources\assets\hbm\models\missile_abm.obj"
if (-not (Test-Path $abmSrc)) { $abmSrc = Join-Path $Assets "models\legacy_raw\missile_abm.obj" }
Convert-Obj $abmSrc (Join-Path $MissileObjDst "missile_abm.obj") "missile_abm.mtl"
Write-Mtl (Join-Path $MissileObjDst "missile_abm.mtl") "hbm:block/missile/missile_abm"
Invoke-WebRequest -Uri ($TexBase + "missile/missile_abm.png") -OutFile (Join-Path $MissileTexDst "missile_abm.png") -UseBasicParsing -TimeoutSec 30
Write-Host "ok missile_abm $((Get-Item (Join-Path $MissileTexDst 'missile_abm.png')).Length)"
$abmJson = @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": false,
  "model": "hbm:models/obj/missile_abm.obj",
  "mtl_override": "hbm:models/obj/missile_abm.mtl",
  "textures": {
    "particle": "hbm:block/missile/missile_abm",
    "material": "hbm:block/missile/missile_abm",
    "#material": "hbm:block/missile/missile_abm"
  }
}
"@
Set-Content -Path (Join-Path $MissileBlockDst "missile_abm.json") -Value $abmJson
Set-Content -Path (Join-Path $ItemDst "missile_anti_ballistic.json") -Value '{ "parent": "hbm:item/missile_bewlr" }'
Write-Host "done $($parts.Count) parts + abm"
