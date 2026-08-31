$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $Root "src"))) {
    $Root = (Get-Location).Path
}
$Assets = Join-Path $Root "src\main\resources\assets\hbm"
$ObjSrc = Join-Path $Root "legacy-1.7.10\src\main\resources\assets\hbm\models\missile_parts"
$ObjAlt = Join-Path $Assets "models\legacy_raw\missile_parts"
$ObjDst = Join-Path $Assets "models\obj\missile_parts"
$BlockDst = Join-Path $Assets "models\block\missile_part"
$ItemDst = Join-Path $Assets "models\item"
$TexDst = Join-Path $Assets "textures\block\missile_parts"
$TexBase = "https://raw.githubusercontent.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/master/src/main/resources/assets/hbm/textures/models/missile_parts/"

New-Item -ItemType Directory -Force -Path $ObjDst, $BlockDst, $ItemDst, $TexDst | Out-Null

function Convert-Obj([string]$stem) {
    $src = Join-Path $ObjSrc "$stem.obj"
    if (-not (Test-Path $src)) { $src = Join-Path $ObjAlt "$stem.obj" }
    if (-not (Test-Path $src)) { Write-Host "missing obj $stem"; return }
    $text = Get-Content -Raw -Path $src
    if ($text -notmatch "^mtllib") {
        $text = "mtllib $stem.mtl`nusemtl material`n$text"
    }
    Set-Content -Path (Join-Path $ObjDst "$stem.obj") -Value $text -NoNewline
    Set-Content -Path (Join-Path $ObjDst "$stem.mtl") -Value "newmtl material`nKd 1 1 1`nmap_Kd hbm:block/missile_parts/$stem`n"
}

function Write-BlockJson([string]$item, [string]$obj, [string]$texStem) {
    $json = @"
{
  "loader": "forge:obj",
  "flip_v": true,
  "automatic_culling": false,
  "shade_quads": true,
  "model": "hbm:models/obj/missile_parts/$obj.obj",
  "textures": {
    "particle": "hbm:block/missile_parts/$texStem",
    "material": "hbm:block/missile_parts/$texStem"
  }
}
"@
    Set-Content -Path (Join-Path $BlockDst "$item.json") -Value $json
}

function Write-ItemJson([string]$item) {
    Set-Content -Path (Join-Path $ItemDst "$item.json") -Value '{ "parent": "hbm:item/missile_bewlr" }'
}

$parts = [ordered]@{
    "mp_thruster_10_kerosene" = @("mp_t_10_kerosene", "thrusters/mp_t_10_kerosene.png")
    "mp_thruster_10_solid" = @("mp_t_10_solid", "thrusters/mp_t_10_solid.png")
    "mp_thruster_10_xenon" = @("mp_t_10_xenon", "thrusters/mp_t_10_xenon.png")
    "mp_stability_10_flat" = @("mp_s_10_flat", "stability/mp_s_10_flat.png")
    "mp_stability_10_cruise" = @("mp_s_10_cruise", "stability/mp_s_10_cruise.png")
    "mp_stability_10_space" = @("mp_s_10_space", "stability/mp_s_10_space.png")
    "mp_fuselage_10_kerosene" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene.png")
    "mp_fuselage_10_kerosene_camo" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_camo.png")
    "mp_fuselage_10_kerosene_desert" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_desert.png")
    "mp_fuselage_10_kerosene_sky" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_sky.png")
    "mp_fuselage_10_kerosene_flames" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_flames.png")
    "mp_fuselage_10_kerosene_insulation" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_insulation.png")
    "mp_fuselage_10_kerosene_sleek" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_sleek.png")
    "mp_fuselage_10_kerosene_metal" = @("mp_f_10_kerosene", "fuselages/mp_f_10_kerosene_metal.png")
    "mp_fuselage_10_kerosene_taint" = @("mp_f_10_kerosene", "fuselages/contest/mp_f_10_kerosene_taint.png")
    "mp_fuselage_10_solid" = @("mp_f_10_kerosene", "fuselages/mp_f_10_solid.png")
    "mp_fuselage_10_solid_flames" = @("mp_f_10_kerosene", "fuselages/mp_f_10_solid_flames.png")
    "mp_fuselage_10_solid_insulation" = @("mp_f_10_kerosene", "fuselages/mp_f_10_solid_insulation.png")
    "mp_fuselage_10_solid_sleek" = @("mp_f_10_kerosene", "fuselages/mp_f_10_solid_sleek.png")
    "mp_fuselage_10_solid_soviet_glory" = @("mp_f_10_kerosene", "fuselages/mp_f_10_solid_soviet_glory.png")
    "mp_fuselage_10_solid_cathedral" = @("mp_f_10_kerosene", "fuselages/contest/mp_f_10_solid_cathedral.png")
    "mp_fuselage_10_solid_moonlit" = @("mp_f_10_kerosene", "fuselages/contest/mp_f_10_solid_moonlit.png")
    "mp_fuselage_10_solid_battery" = @("mp_f_10_kerosene", "fuselages/contest/mp_f_10_solid_battery.png")
    "mp_fuselage_10_solid_duracell" = @("mp_f_10_kerosene", "fuselages/mp_f_10_solid_duracell.png")
    "mp_fuselage_10_xenon" = @("mp_f_10_kerosene", "fuselages/mp_f_10_xenon.png")
    "mp_fuselage_10_xenon_bhole" = @("mp_f_10_kerosene", "fuselages/contest/mp_f_10_xenon_bhole.png")
    "mp_fuselage_10_long_kerosene" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene.png")
    "mp_fuselage_10_long_kerosene_camo" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_camo.png")
    "mp_fuselage_10_long_kerosene_desert" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_desert.png")
    "mp_fuselage_10_long_kerosene_sky" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_sky.png")
    "mp_fuselage_10_long_kerosene_flames" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_flames.png")
    "mp_fuselage_10_long_kerosene_insulation" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_insulation.png")
    "mp_fuselage_10_long_kerosene_sleek" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_sleek.png")
    "mp_fuselage_10_long_kerosene_metal" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_kerosene_metal.png")
    "mp_fuselage_10_long_kerosene_dash" = @("mp_f_10_long_kerosene", "fuselages/contest/mp_f_10_long_kerosene_dash.png")
    "mp_fuselage_10_long_kerosene_taint" = @("mp_f_10_long_kerosene", "fuselages/contest/mp_f_10_long_kerosene_taint.png")
    "mp_fuselage_10_long_kerosene_vap" = @("mp_f_10_long_kerosene", "fuselages/contest/mp_f_10_long_kerosene_vap.png")
    "mp_fuselage_10_long_solid" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_solid.png")
    "mp_fuselage_10_long_solid_flames" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_solid_flames.png")
    "mp_fuselage_10_long_solid_insulation" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_solid_insulation.png")
    "mp_fuselage_10_long_solid_sleek" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_solid_sleek.png")
    "mp_fuselage_10_long_solid_soviet_glory" = @("mp_f_10_long_kerosene", "fuselages/mp_f_10_long_solid_soviet_glory.png")
    "mp_fuselage_10_long_solid_bullet" = @("mp_f_10_long_kerosene", "fuselages/contest/mp_f_10_long_solid_bullet.png")
    "mp_fuselage_10_long_solid_silvermoonlight" = @("mp_f_10_long_kerosene", "fuselages/contest/mp_f_10_long_solid_silvermoonlight.png")
    "mp_fuselage_10_15_kerosene" = @("mp_f_10_15_kerosene", "fuselages/mp_f_10_15_kerosene.png")
    "mp_fuselage_10_15_solid" = @("mp_f_10_15_kerosene", "fuselages/mp_f_10_15_solid.png")
    "mp_fuselage_10_15_hydrogen" = @("mp_f_10_15_kerosene", "fuselages/mp_f_10_15_hydrogen.png")
    "mp_fuselage_10_15_balefire" = @("mp_f_10_15_kerosene", "fuselages/mp_f_10_15_balefire.png")
    "mp_warhead_10_he" = @("mp_w_10_he", "warheads/mp_w_10_he.png")
    "mp_warhead_10_incendiary" = @("mp_w_10_incendiary", "warheads/mp_w_10_incendiary.png")
    "mp_warhead_10_buster" = @("mp_w_10_buster", "warheads/mp_w_10_buster.png")
    "mp_warhead_10_nuclear" = @("mp_w_10_nuclear", "warheads/mp_w_10_nuclear.png")
    "mp_warhead_10_nuclear_large" = @("mp_w_10_nuclear_large", "warheads/mp_w_10_nuclear_large.png")
    "mp_warhead_10_taint" = @("mp_w_10_taint", "warheads/mp_w_10_taint.png")
    "mp_warhead_10_cloud" = @("mp_w_10_taint", "warheads/mp_w_10_cloud.png")
}

$objs = $parts.Values | ForEach-Object { $_[0] } | Sort-Object -Unique
foreach ($stem in $objs) { Convert-Obj $stem }

$downloaded = @{}
foreach ($item in $parts.Keys) {
    $obj = $parts[$item][0]
    $rel = $parts[$item][1]
    $texStem = [IO.Path]::GetFileNameWithoutExtension($rel)
    if (-not $downloaded.ContainsKey($rel)) {
        $dest = Join-Path $TexDst "$texStem.png"
        $url = $TexBase + $rel
        try {
            Invoke-WebRequest -Uri $url -OutFile $dest -UseBasicParsing -TimeoutSec 30
            Write-Host "ok $texStem $((Get-Item $dest).Length)"
        } catch {
            Write-Host "FAIL $rel $($_.Exception.Message)"
        }
        $downloaded[$rel] = $true
    }
    Write-BlockJson $item $obj $texStem
    Write-ItemJson $item
}
Write-ItemJson "missile_custom"

$soundSrc = Join-Path $Assets "sounds\block\missileAssembly2.ogg"
$soundDst = Join-Path $Assets "sounds\block\missile_assembly2.ogg"
if ((Test-Path $soundSrc) -and -not (Test-Path $soundDst)) {
    Copy-Item $soundSrc $soundDst
}
Write-Host "done $($parts.Count) parts"
