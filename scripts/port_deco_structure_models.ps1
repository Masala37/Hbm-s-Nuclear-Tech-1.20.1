$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$obj = Join-Path $root "src\main\resources\assets\hbm\models\obj"
$raw = Join-Path $root "src\main\resources\assets\hbm\models\legacy_raw"
New-Item -ItemType Directory -Force -Path $obj | Out-Null

function Shift-Obj([string]$src, [string]$dest, [string]$mtlName, [string]$mapKd, [double]$dx = 0.5, [double]$dy = 0, [double]$dz = 0.5) {
    $mtl = [IO.Path]::ChangeExtension($dest, ".mtl")
    Set-Content -Path $mtl -Value "newmtl material`nKd 1 1 1`nmap_Kd $mapKd`n" -NoNewline
    $out = New-Object System.Collections.Generic.List[string]
    [void]$out.Add("mtllib $mtlName")
    [void]$out.Add("usemtl material")
    foreach ($line in [IO.File]::ReadAllLines($src)) {
        if ($line.StartsWith("v ")) {
            $p = $line.Split(" ", [StringSplitOptions]::RemoveEmptyEntries)
            $x = [double]$p[1] + $dx
            $y = [double]$p[2] + $dy
            $z = [double]$p[3] + $dz
            [void]$out.Add(("v {0:0.000000} {1:0.000000} {2:0.000000}" -f $x, $y, $z))
        } elseif ($line.StartsWith("mtllib ") -or $line.StartsWith("usemtl ")) {
            continue
        } else {
            [void]$out.Add($line)
        }
    }
    [IO.File]::WriteAllLines($dest, $out)
}

function Vec([double]$x, [double]$y, [double]$z) {
    return [pscustomobject]@{ X = $x; Y = $y; Z = $z }
}
function RotX($v, $a) {
    $c = [math]::Cos($a); $s = [math]::Sin($a)
    return (Vec $v.X ($v.Y * $c - $v.Z * $s) ($v.Y * $s + $v.Z * $c))
}
function RotY($v, $a) {
    $c = [math]::Cos($a); $s = [math]::Sin($a)
    return (Vec ($v.X * $c + $v.Z * $s) $v.Y (-$v.X * $s + $v.Z * $c))
}
function RotZ($v, $a) {
    $c = [math]::Cos($a); $s = [math]::Sin($a)
    return (Vec ($v.X * $c - $v.Y * $s) ($v.X * $s + $v.Y * $c) $v.Z)
}
function ApplyModelRot($v, $rx, $ry, $rz) {
    return (RotZ (RotY (RotX $v $rx) $ry) $rz)
}
function TesrToBlock($px, $py, $pz) {
    $v = RotZ (Vec ($px * 0.0625) ($py * 0.0625) ($pz * 0.0625)) ([math]::PI)
    return (Vec ($v.X + 0.5) ($v.Y + 1.5) ($v.Z + 0.5))
}

$script:Verts = New-Object System.Collections.Generic.List[object]
$script:Texs = New-Object System.Collections.Generic.List[object]
$script:Faces = New-Object System.Collections.Generic.List[object]

function Emit-TechneBox($ox, $oy, $oz, $w, $h, $d, $px, $py, $pz, $rx, $ry, $rz, $u, $v) {
    $corners = @(
        (Vec $ox $oy $oz),
        (Vec ($ox + $w) $oy $oz),
        (Vec ($ox + $w) ($oy + $h) $oz),
        (Vec $ox ($oy + $h) $oz),
        (Vec $ox $oy ($oz + $d)),
        (Vec ($ox + $w) $oy ($oz + $d)),
        (Vec ($ox + $w) ($oy + $h) ($oz + $d)),
        (Vec $ox ($oy + $h) ($oz + $d))
    )
    $world = New-Object System.Collections.Generic.List[object]
    foreach ($c in $corners) {
        $m = ApplyModelRot $c $rx $ry $rz
        [void]$world.Add((TesrToBlock ($m.X + $px) ($m.Y + $py) ($m.Z + $pz)))
    }
    $tw = 64.0; $th = 64.0
    function Add-Uv([double]$pxu, [double]$pyv) {
        [void]$script:Texs.Add((Vec ($pxu / $tw) (1.0 - $pyv / $th) 0))
        return $script:Texs.Count
    }
    $facesUv = @{
        down  = @(@(($u + $d), ($v + $d)), @(($u + $d + $w), ($v + $d)), @(($u + $d + $w), $v), @(($u + $d), $v))
        up    = @(@(($u + $d + $w), ($v + $d)), @(($u + $d + $w + $w), ($v + $d)), @(($u + $d + $w + $w), $v), @(($u + $d + $w), $v))
        west  = @(@($u, ($v + $d)), @(($u + $d), ($v + $d)), @(($u + $d), ($v + $d + $h)), @($u, ($v + $d + $h)))
        north = @(@(($u + $d), ($v + $d)), @(($u + $d + $w), ($v + $d)), @(($u + $d + $w), ($v + $d + $h)), @(($u + $d), ($v + $d + $h)))
        east  = @(@(($u + $d + $w), ($v + $d)), @(($u + $d + $w + $d), ($v + $d)), @(($u + $d + $w + $d), ($v + $d + $h)), @(($u + $d + $w), ($v + $d + $h)))
        south = @(@(($u + $d + $w + $d), ($v + $d)), @(($u + $d + $w + $d + $w), ($v + $d)), @(($u + $d + $w + $d + $w), ($v + $d + $h)), @(($u + $d + $w + $d), ($v + $d + $h)))
    }
    $quads = @(
        @("down", @(0, 1, 5, 4)),
        @("up", @(3, 7, 6, 2)),
        @("west", @(0, 4, 7, 3)),
        @("east", @(1, 2, 6, 5)),
        @("north", @(0, 3, 2, 1)),
        @("south", @(4, 5, 6, 7))
    )
    $base = $script:Verts.Count
    foreach ($pt in $world) { [void]$script:Verts.Add($pt) }
    foreach ($q in $quads) {
        $name = $q[0]; $idx = $q[1]
        $ti = foreach ($p in $facesUv[$name]) { Add-Uv $p[0] $p[1] }
        [void]$script:Faces.Add(@(($base + $idx[0]), $ti[0], ($base + $idx[1]), $ti[1], ($base + $idx[2]), $ti[2], ($base + $idx[3]), $ti[3]))
    }
}

Shift-Obj (Join-Path $raw "blocks\antenna_top.obj") (Join-Path $obj "antenna_top.obj") "antenna_top.mtl" "hbm:block/deco_pole_top"

$rx = -0.2617994
$ry = -0.4363323
Emit-TechneBox 0 0 0 12 16 12 (-6) 8 (-6) 0 0 0 0 0
Emit-TechneBox 3 9 (-8) 8 8 2 (-3) 6 0 $rx $ry 0 10 28
Emit-TechneBox 3 7 (-10) 8 2 3 (-3) 6 0 $rx $ry 0 0 39
Emit-TechneBox 1 9 (-10) 2 8 3 (-3) 6 0 $rx $ry 0 0 28
Emit-TechneBox 11 9 (-10) 2 8 3 (-3) 6 0 $rx $ry 0 0 28
Emit-TechneBox 3 17 (-10) 8 2 3 (-3) 6 0 $rx $ry 0 0 39
Emit-TechneBox 6 12 (-11) 2 2 3 (-3) 6 0 $rx $ry 0 0 44
Emit-TechneBox 6.5 12.5 (-14) 1 1 3 (-3) 6 0 $rx $ry 0 0 49
Emit-TechneBox 6 12 (-16) 2 2 2 (-3) 6 0 $rx $ry 0 0 53

$sat = New-Object System.Collections.Generic.List[string]
[void]$sat.Add("mtllib deco_satellite_receiver.mtl")
[void]$sat.Add("usemtl material")
[void]$sat.Add("# generated from 1.7 ModelSatelliteReceiver")
[void]$sat.Add("o Dish")
foreach ($v in $script:Verts) {
    [void]$sat.Add(("v {0:0.000000} {1:0.000000} {2:0.000000}" -f [double]$v.X, [double]$v.Y, [double]$v.Z))
}
foreach ($t in $script:Texs) {
    [void]$sat.Add(("vt {0:0.000000} {1:0.000000}" -f [double]$t.X, [double]$t.Y))
}
foreach ($f in $script:Faces) {
    [void]$sat.Add(("f {0}/{1} {2}/{3} {4}/{5} {6}/{7}" -f ($f[0]+1), $f[1], ($f[2]+1), $f[3], ($f[4]+1), $f[5], ($f[6]+1), $f[7]))
}
[IO.File]::WriteAllLines((Join-Path $obj "deco_satellite_receiver.obj"), $sat)
Set-Content -Path (Join-Path $obj "deco_satellite_receiver.mtl") -Value "newmtl material`nKd 1 1 1`nmap_Kd hbm:models/PoleSatelliteReceiver`n" -NoNewline

$mwOut = New-Object System.Collections.Generic.List[string]
[void]$mwOut.Add("mtllib microwave.mtl")
[void]$mwOut.Add("usemtl material")
foreach ($line in [IO.File]::ReadAllLines((Join-Path $raw "machines\microwave.obj"))) {
    if ($line.StartsWith("v ")) {
        $p = $line.Split(" ", [StringSplitOptions]::RemoveEmptyEntries)
        $x = [double]$p[1]
        $y = [double]$p[2] - 0.785
        $z = [double]$p[3] + 1.15
        [void]$mwOut.Add(("v {0:0.000000} {1:0.000000} {2:0.000000}" -f $x, $y, $z))
    } elseif ($line.StartsWith("mtllib ") -or $line.StartsWith("usemtl ")) {
        continue
    } else {
        [void]$mwOut.Add($line)
    }
}
[IO.File]::WriteAllLines((Join-Path $obj "microwave.obj"), $mwOut)
Set-Content -Path (Join-Path $obj "microwave.mtl") -Value "newmtl material`nKd 1 1 1`nmap_Kd hbm:models/machines/microwave`n" -NoNewline

Write-Host "wrote antenna_top.obj, deco_satellite_receiver.obj, microwave.obj verts=$($script:Verts.Count)"
