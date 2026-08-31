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
$BoxcarUrl = "https://raw.githubusercontent.com/HbmMods/Hbm-s-Nuclear-Tech-GIT/master/src/main/resources/assets/hbm/textures/models/boxcar.png"

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
    "mp_thruster_15_kerosene" = @("mp_t_15_kerosene", "thrusters/mp_t_15_kerosene.png")
    "mp_thruster_15_kerosene_dual" = @("mp_t_15_kerosene_dual", "thrusters/mp_t_15_kerosene_dual.png")
    "mp_thruster_15_kerosene_triple" = @("mp_t_15_kerosene_triple", "thrusters/mp_t_15_kerosene_dual.png")
    "mp_thruster_15_solid" = @("mp_t_15_solid", "thrusters/mp_t_15_solid.png")
    "mp_thruster_15_solid_hexdecuple" = @("mp_t_15_solid_hexdecuple", "thrusters/mp_t_15_solid_hexdecuple.png")
    "mp_thruster_15_hydrogen" = @("mp_t_15_kerosene", "thrusters/mp_t_15_hydrogen.png")
    "mp_thruster_15_hydrogen_dual" = @("mp_t_15_kerosene_dual", "thrusters/mp_t_15_hydrogen_dual.png")
    "mp_thruster_15_balefire_short" = @("mp_t_15_balefire_short", "thrusters/mp_t_15_balefire_short.png")
    "mp_thruster_15_balefire" = @("mp_t_15_balefire", "thrusters/mp_t_15_balefire.png")
    "mp_thruster_15_balefire_large" = @("mp_t_15_balefire_large", "thrusters/mp_t_15_balefire_large.png")
    "mp_thruster_15_balefire_large_rad" = @("mp_t_15_balefire_large", "thrusters/mp_t_15_balefire_large_rad.png")
    "mp_stability_15_flat" = @("mp_s_15_flat", "stability/mp_s_15_flat.png")
    "mp_stability_15_thin" = @("mp_s_15_thin", "stability/mp_s_15_thin.png")
    "mp_stability_15_soyuz" = @("mp_s_15_soyuz", "stability/mp_s_15_soyuz.png")
    "mp_fuselage_15_kerosene" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene.png")
    "mp_fuselage_15_kerosene_camo" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_camo.png")
    "mp_fuselage_15_kerosene_desert" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_desert.png")
    "mp_fuselage_15_kerosene_sky" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_sky.png")
    "mp_fuselage_15_kerosene_insulation" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_insulation.png")
    "mp_fuselage_15_kerosene_metal" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_metal.png")
    "mp_fuselage_15_kerosene_decorated" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_decorated.png")
    "mp_fuselage_15_kerosene_steampunk" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_steampunk.png")
    "mp_fuselage_15_kerosene_polite" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_polite.png")
    "mp_fuselage_15_kerosene_blackjack" = @("mp_f_15_kerosene", "fuselages/base/mp_f_15_kerosene_blackjack.png")
    "mp_fuselage_15_kerosene_lambda" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_kerosene_lambda.png")
    "mp_fuselage_15_kerosene_minuteman" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_kerosene_minuteman.png")
    "mp_fuselage_15_kerosene_pip" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_kerosene_pip.png")
    "mp_fuselage_15_kerosene_taint" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_kerosene_taint.png")
    "mp_fuselage_15_kerosene_yuck" = @("mp_f_15_kerosene", "fuselages/mp_f_15_kerosene_yuck.png")
    "mp_fuselage_15_solid" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid.png")
    "mp_fuselage_15_solid_insulation" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_insulation.png")
    "mp_fuselage_15_solid_desh" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_desh.png")
    "mp_fuselage_15_solid_soviet_glory" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_soviet_glory.png")
    "mp_fuselage_15_solid_soviet_stank" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_soviet_stank.png")
    "mp_fuselage_15_solid_faust" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_solid_faust.png")
    "mp_fuselage_15_solid_silvermoonlight" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_solid_silvermoonlight.png")
    "mp_fuselage_15_solid_snowy" = @("mp_f_15_kerosene", "fuselages/contest/mp_f_15_solid_snowy.png")
    "mp_fuselage_15_solid_panorama" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_panorama.png")
    "mp_fuselage_15_solid_roses" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_roses.png")
    "mp_fuselage_15_solid_mimi" = @("mp_f_15_kerosene", "fuselages/mp_f_15_solid_mimi.png")
    "mp_fuselage_15_hydrogen" = @("mp_f_15_hydrogen", "fuselages/mp_f_15_hydrogen.png")
    "mp_fuselage_15_hydrogen_cathedral" = @("mp_f_15_hydrogen", "fuselages/contest/mp_f_15_hydrogen_cathedral.png")
    "mp_fuselage_15_balefire" = @("mp_f_15_hydrogen", "fuselages/mp_f_15_balefire.png")
    "mp_fuselage_15_20_kerosene" = @("mp_f_15_20_kerosene", "fuselages/mp_f_15_20_kerosene.png")
    "mp_fuselage_15_20_kerosene_magnusson" = @("mp_f_15_20_kerosene", "fuselages/mp_f_15_20_kerosene_magnusson.png")
    "mp_fuselage_15_20_solid" = @("mp_f_15_20_kerosene", "fuselages/mp_f_15_20_solid.png")
    "mp_warhead_15_he" = @("mp_w_15_he", "warheads/mp_w_15_he.png")
    "mp_warhead_15_incendiary" = @("mp_w_15_incendiary", "warheads/mp_w_15_incendiary.png")
    "mp_warhead_15_nuclear" = @("mp_w_15_nuclear", "warheads/mp_w_15_nuclear.png")
    "mp_warhead_15_nuclear_shark" = @("mp_w_15_nuclear", "warheads/mp_w_15_nuclear_shark.png")
    "mp_warhead_15_nuclear_mimi" = @("mp_w_15_nuclear", "warheads/mp_w_15_nuclear_mimi.png")
    "mp_warhead_15_boxcar" = @("mp_w_15_boxcar", "boxcar.png")
    "mp_warhead_15_n2" = @("mp_w_15_n2", "warheads/mp_w_15_n2.png")
    "mp_warhead_15_balefire" = @("mp_w_15_balefire", "warheads/mp_w_15_balefire.png")
    "mp_warhead_15_turbine" = @("mp_w_15_turbine", "warheads/mp_w_15_turbine.png")
}

$objs = $parts.Values | ForEach-Object { $_[0] } | Sort-Object -Unique
foreach ($stem in $objs) { Convert-Obj $stem }

$downloaded = @{}
$fail = 0
foreach ($item in $parts.Keys) {
    $obj = $parts[$item][0]
    $rel = $parts[$item][1]
    $texStem = [IO.Path]::GetFileNameWithoutExtension($rel)
    if ($item -eq "mp_warhead_15_boxcar") { $texStem = "mp_w_15_boxcar" }
    if (-not $downloaded.ContainsKey($rel)) {
        $dest = Join-Path $TexDst "$texStem.png"
        if ($item -eq "mp_warhead_15_boxcar") {
            $url = $BoxcarUrl
        } else {
            $url = $TexBase + $rel
        }
        try {
            Invoke-WebRequest -Uri $url -OutFile $dest -UseBasicParsing -TimeoutSec 30
            Write-Host "ok $texStem $((Get-Item $dest).Length)"
        } catch {
            Write-Host "FAIL $rel $($_.Exception.Message)"
            $fail++
        }
        $downloaded[$rel] = $true
    }
    Write-BlockJson $item $obj $texStem
    Write-ItemJson $item
}
Write-Host "done $($parts.Count) parts, $fail fails"
if ($fail -gt 0) { exit 1 }
