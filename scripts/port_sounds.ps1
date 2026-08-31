# Convert 1.7.10 sounds.json + mixed-case oggs into 1.20.1-valid assets.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$legacyJson = Join-Path $root "legacy-1.7.10\src\main\resources\assets\hbm\sounds.json"
$currentJson = Join-Path $root "src\main\resources\assets\hbm\sounds.json"
$soundsDir = Join-Path $root "src\main\resources\assets\hbm\sounds"
$catalogPath = Join-Path $root "src\main\java\com\hbm\registry\SoundEventCatalog.java"

function Convert-CamelToSnake([string]$s) {
    $s = [regex]::Replace($s, '([a-z0-9])([A-Z])', '${1}_${2}')
    $s = [regex]::Replace($s, '([A-Z]+)([A-Z][a-z])', '${1}_${2}')
    return $s.ToLowerInvariant()
}

function Test-ResourcePath([string]$path) {
    return $path -cmatch '^[a-z0-9/._-]+$'
}

function Find-Ogg([string]$relativeNoExt) {
    $slash = $relativeNoExt.Replace('/', '\')
    $full = Join-Path $soundsDir ($slash + ".ogg")
    if (Test-Path -LiteralPath $full) {
        return (Resolve-Path -LiteralPath $full).Path
    }
    $parent = Split-Path $full
    $leaf = Split-Path $full -Leaf
    if (Test-Path -LiteralPath $parent) {
        $match = Get-ChildItem -LiteralPath $parent -Filter *.ogg | Where-Object { $_.Name -ieq $leaf } | Select-Object -First 1
        if ($match) { return $match.FullName }
    }
    return $null
}

function Ensure-SnakeOgg([string]$originalRel, [string]$snakeRel) {
    $dest = Join-Path $soundsDir ($snakeRel.Replace('/', '\') + ".ogg")
    $destDir = Split-Path $dest
    if (-not (Test-Path -LiteralPath $destDir)) {
        New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    }
    $src = Find-Ogg $originalRel
    if (-not $src) { $src = Find-Ogg $snakeRel }
    if (-not $src) { return $false }
    $destName = Split-Path $dest -Leaf
    $srcName = Split-Path $src -Leaf
    $sameDir = (Split-Path $src) -ieq $destDir
    if ($sameDir -and ($srcName -ceq $destName)) {
        return $true
    }
    if ($sameDir -and ($srcName -ieq $destName) -and ($srcName -cne $destName)) {
        # Windows case-only rename: copy via temp then replace.
        $tmp = Join-Path $destDir ("__tmp_" + [guid]::NewGuid().ToString("N") + ".ogg")
        Copy-Item -LiteralPath $src -Destination $tmp -Force
        Remove-Item -LiteralPath $src -Force
        Move-Item -LiteralPath $tmp -Destination $dest -Force
        return $true
    }
    if (-not (Test-Path -LiteralPath $dest)) {
        Copy-Item -LiteralPath $src -Destination $dest -Force
    }
    return $true
}

function Convert-SoundName([string]$name) {
    $rel = $name
    if ($rel.StartsWith("hbm:")) { $rel = $rel.Substring(4) }
    $rel = $rel.Replace('\', '/')
    return Convert-CamelToSnake $rel
}

$legacy = Get-Content -Raw -Encoding UTF8 -Path $legacyJson | ConvertFrom-Json
$current = Get-Content -Raw -Encoding UTF8 -Path $currentJson | ConvertFrom-Json

$events = [ordered]@{}
$copied = 0
$missing = New-Object System.Collections.Generic.List[string]

function Add-Event([string]$key, $soundValues) {
    if (-not (Test-ResourcePath $key)) {
        throw "Invalid event key: $key"
    }
    $entries = New-Object System.Collections.Generic.List[object]
    foreach ($s in @($soundValues)) {
        $origName = $null
        $stream = $false
        if ($s -is [string]) {
            $origName = $s
        } else {
            $origName = [string]$s.name
            if ($null -ne $s.stream) { $stream = [bool]$s.stream }
        }
        $origRel = $origName
        if ($origRel.StartsWith("hbm:")) { $origRel = $origRel.Substring(4) }
        $origRel = $origRel.Replace('\', '/')
        $snakeRel = Convert-CamelToSnake $origRel
        if (-not (Test-ResourcePath $snakeRel)) {
            throw "Invalid sound path: $snakeRel (from $origName)"
        }
        if (-not (Ensure-SnakeOgg $origRel $snakeRel)) {
            $missing.Add("$key -> $origRel") | Out-Null
            continue
        }
        $copied++
        $entries.Add([ordered]@{
            name = "hbm:$snakeRel"
            stream = $stream
        }) | Out-Null
    }
    if ($entries.Count -gt 0) {
        $events[$key] = [ordered]@{ sounds = $entries }
    }
}

foreach ($p in $legacy.PSObject.Properties) {
    $key = Convert-CamelToSnake $p.Name
    Add-Event $key $p.Value.sounds
}

# Existing port entries win (keep Java registry names / file names).
foreach ($p in $current.PSObject.Properties) {
    Add-Event $p.Name $p.Value.sounds
}

$sortedKeys = @($events.Keys | Sort-Object)
$jsonObj = [ordered]@{}
foreach ($k in $sortedKeys) { $jsonObj[$k] = $events[$k] }

$json = ($jsonObj | ConvertTo-Json -Depth 8) + [Environment]::NewLine
# PowerShell 5 may emit uppercase True/False — Minecraft Gson needs lowercase.
$json = $json -replace ': True', ': true' -replace ': False', ': false'
[System.IO.File]::WriteAllText($currentJson, $json, [System.Text.UTF8Encoding]::new($false))

$nl = [Environment]::NewLine
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine("package com.hbm.registry;")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("/** Sound event paths generated from 1.7.10 sounds.json (1.20-safe). */")
[void]$sb.AppendLine("public final class SoundEventCatalog {")
[void]$sb.AppendLine("    private SoundEventCatalog() {")
[void]$sb.AppendLine("    }")
[void]$sb.AppendLine("")
[void]$sb.AppendLine("    public static final String[] PATHS = {")
for ($i = 0; $i -lt $sortedKeys.Count; $i++) {
    $comma = if ($i -lt $sortedKeys.Count - 1) { "," } else { "" }
    [void]$sb.AppendLine(('        "' + $sortedKeys[$i] + '"' + $comma))
}
[void]$sb.AppendLine("    };")
[void]$sb.AppendLine("}")
[System.IO.File]::WriteAllText($catalogPath, $sb.ToString(), [System.Text.UTF8Encoding]::new($false))

Write-Output ("events=" + $sortedKeys.Count)
Write-Output ("missing=" + $missing.Count)
if ($missing.Count -gt 0) {
    $missing | Select-Object -First 30 | ForEach-Object { Write-Output ("  " + $_) }
}
Write-Output ("sonar_ping=" + (Test-Path (Join-Path $soundsDir "block\sonar_ping.ogg")))
Write-Output ("engine=" + (Test-Path (Join-Path $soundsDir "block\engine.ogg")))
Write-Output ("diesel_operate=" + (Test-Path (Join-Path $soundsDir "block\diesel_operate.ogg")))
Write-Output ("crate_open=" + (Test-Path (Join-Path $soundsDir "block\crate_open.ogg")))
Write-Output ("wrote $currentJson")
Write-Output ("wrote $catalogPath")
