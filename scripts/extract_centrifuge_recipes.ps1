# Extract 1.7.10 CentrifugeRecipes.registerDefaults. Does not invent rows.
# Skips loops, enum/meta stacks, IMC, and AE2 certus. LBSM ternaries keep the false branch.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$srcPath = Join-Path $root "legacy-1.7.10\src\main\java\com\hbm\inventory\recipes\CentrifugeRecipes.java"
$outPath = Join-Path $root "src\main\resources\data\hbm\machine_recipes\centrifuge.json"

$frames = @{
    COAL = "Coal"; IRON = "Iron"; GOLD = "Gold"; LAPIS = "Lapis"; REDSTONE = "Redstone"
    NETHERQUARTZ = "NetherQuartz"; DIAMOND = "Diamond"; EMERALD = "Emerald"
    U = "Uranium"; TH232 = "Thorium232"; PU = "Plutonium"; SA326 = "Schrabidium"
    TI = "Titanium"; CU = "Copper"; W = "Tungsten"; AL = "Aluminum"; PB = "Lead"
    BE = "Beryllium"; CO = "Cobalt"; F = "Fluorite"; LIGNITE = "Lignite"; RAREEARTH = "RareEarth"
}
$shapes = @{ ore = "ore"; ingot = "ingot"; dust = "dust"; nugget = "nugget"; gem = "gem"; block = "block" }
$vanillaItems = @{
    iron_ingot = "minecraft:iron_ingot"; gold_ingot = "minecraft:gold_ingot"; redstone = "minecraft:redstone"
    diamond = "minecraft:diamond"; emerald = "minecraft:emerald"; quartz = "minecraft:quartz"
    blaze_rod = "minecraft:blaze_rod"; blaze_powder = "minecraft:blaze_powder"; coal = "minecraft:coal"
}
$vanillaBlocks = @{
    gravel = "minecraft:gravel"; netherrack = "minecraft:netherrack"; end_stone = "minecraft:end_stone"
    sand = "minecraft:sand"; dirt = "minecraft:dirt"; stone = "minecraft:stone"
}

function Strip-Comments([string]$src) {
    $src = [regex]::Replace($src, '/\*[\s\S]*?\*/', '')
    $src = [regex]::Replace($src, '(?m)//.*?$', '')
    return $src
}

function Split-Args([string]$s) {
    $args = New-Object System.Collections.Generic.List[string]
    $buf = New-Object System.Text.StringBuilder
    $depth = 0
    $inStr = $false
    foreach ($ch in $s.ToCharArray()) {
        if ($ch -eq '"' -and ($buf.Length -eq 0 -or $buf[$buf.Length - 1] -ne '\')) {
            $inStr = -not $inStr
            [void]$buf.Append($ch)
            continue
        }
        if ($inStr) { [void]$buf.Append($ch); continue }
        if ('([{' -contains $ch) { $depth++; [void]$buf.Append($ch) }
        elseif (')]}' -contains $ch) { $depth--; [void]$buf.Append($ch) }
        elseif ($ch -eq ',' -and $depth -eq 0) {
            $args.Add($buf.ToString().Trim())
            $buf.Clear() | Out-Null
        }
        else { [void]$buf.Append($ch) }
    }
    $tail = $buf.ToString().Trim()
    if ($tail) { $args.Add($tail) }
    return $args.ToArray()
}

function Extract-Balanced([string]$src, [int]$start) {
    $depth = 0
    for ($i = $start; $i -lt $src.Length; $i++) {
        if ($src[$i] -eq '(') { $depth++ }
        elseif ($src[$i] -eq ')') {
            $depth--
            if ($depth -eq 0) { return ,@($src.Substring($start, $i - $start + 1), ($i + 1)) }
        }
    }
    return ,@($src.Substring($start), $src.Length)
}

function Rewrite-LbsFalse([string]$expr) {
    while ($true) {
        $i = $expr.IndexOf('lbs ?')
        if ($i -lt 0) { $i = $expr.IndexOf('lbs?') }
        if ($i -lt 0) { return $expr }
        $q = $expr.IndexOf('?', $i)
        $depth = 0
        $colon = -1
        for ($j = $q + 1; $j -lt $expr.Length; $j++) {
            $ch = $expr[$j]
            if ('([{' -contains $ch) { $depth++ }
            elseif (')]}' -contains $ch) { $depth-- }
            elseif ($ch -eq ':' -and $depth -eq 0) { $colon = $j; break }
        }
        if ($colon -lt 0) { return $expr }
        $depth = 0
        $end = $expr.Length
        for ($j = $colon + 1; $j -lt $expr.Length; $j++) {
            $ch = $expr[$j]
            if ('([{' -contains $ch) { $depth++ }
            elseif (')]}' -contains $ch) {
                $depth--
                if ($depth -lt 0) { $end = $j; break }
            }
            elseif ($ch -eq ',' -and $depth -eq 0) { $end = $j; break }
        }
        $falseBranch = $expr.Substring($colon + 1, $end - $colon - 1).Trim()
        $expr = $expr.Substring(0, $i) + $falseBranch + $expr.Substring($end)
    }
    return $expr
}

function Parse-Int([string]$expr, [int]$default) {
    $expr = $expr.Trim()
    if ($expr -match '^-?\d+$') { return [int]$expr }
    return $default
}

function Parse-Bare([string]$token, [int]$count) {
    $token = $token.Trim()
    if ($token -match '^ModItems\.(\w+)$') { return @{ item = "hbm:$($Matches[1])"; count = $count } }
    if ($token -match '^ModBlocks\.(\w+)$') { return @{ item = "hbm:$($Matches[1])"; count = $count } }
    if ($token -match '^Items\.(\w+)$') {
        $name = $Matches[1]
        if ($vanillaItems.ContainsKey($name)) { return @{ item = $vanillaItems[$name]; count = $count } }
        return $null
    }
    if ($token -match '^Blocks\.(\w+)$') {
        $name = $Matches[1]
        if ($vanillaBlocks.ContainsKey($name)) { return @{ item = $vanillaBlocks[$name]; count = $count } }
        return $null
    }
    return $null
}

function Strip-Ctor([string]$expr, [string]$name) {
    $expr = $expr.Trim()
    $prefix = "$name("
    if (-not $expr.StartsWith($prefix)) { return $expr }
    $depth = 0
    $body = $expr.Substring($name.Length)
    for ($i = 0; $i -lt $body.Length; $i++) {
        if ($body[$i] -eq '(') { $depth++ }
        elseif ($body[$i] -eq ')') {
            $depth--
            if ($depth -eq 0) { return $expr.Substring($name.Length + 1, $i - 1) }
        }
    }
    return $expr.Substring($prefix.Length).TrimEnd(')')
}

function Parse-Stack([string]$expr) {
    $expr = $expr.Trim().TrimEnd(',')
    if (-not $expr) { return $null }
    if ($expr.StartsWith('new OreDictStack(')) {
        $inner = Strip-Ctor $expr 'new OreDictStack'
        $parts = Split-Args $inner
        $call = $parts[0].Trim()
        if ($call -match '^(\w+)\.(\w+)\(\s*\)$') {
            $frame = $Matches[1]; $shape = $Matches[2]
            if ($frames.ContainsKey($frame) -and $shapes.ContainsKey($shape)) {
                $count = 1
                if ($parts.Length -gt 1) { $count = Parse-Int $parts[1] 1 }
                return @{ ore = ($shapes[$shape] + $frames[$frame]); count = $count }
            }
        }
        if ($call.StartsWith('"') -and $call.EndsWith('"')) {
            return @{ ore = $call.Trim('"'); count = 1 }
        }
        return $null
    }
    if ($expr.StartsWith('new ComparableStack(')) {
        $inner = Strip-Ctor $expr 'new ComparableStack'
        $parts = Split-Args $inner
        $count = 1
        if ($parts.Length -gt 1) { $count = Parse-Int $parts[1] 1 }
        if ($parts.Length -gt 2 -and (Parse-Int $parts[2] 0) -ne 0) { return $null }
        return Parse-Bare $parts[0] $count
    }
    if ($expr.StartsWith('new ItemStack(')) {
        $inner = Strip-Ctor $expr 'new ItemStack'
        $parts = Split-Args $inner
        $count = 1
        if ($parts.Length -gt 1) { $count = Parse-Int $parts[1] 1 }
        if ($parts.Length -gt 2) {
            $meta = $parts[2].Trim()
            if ($meta -ne '0') { return $null }
        }
        return Parse-Bare $parts[0] $count
    }
    return Parse-Bare $expr 1
}

function Parse-OutputArray([string]$expr) {
    $expr = Rewrite-LbsFalse $expr.Trim()
    $brace = $expr.IndexOf('{')
    if ($brace -lt 0) {
        $one = Parse-Stack $expr
        if ($null -eq $one) { return $null }
        return ,@($one)
    }
    $inner = $expr.Substring($brace + 1, $expr.LastIndexOf('}') - $brace - 1)
    $items = @()
    foreach ($part in (Split-Args $inner)) {
        $stack = Parse-Stack $part
        if ($null -eq $stack) { return $null }
        $items += $stack
    }
    return $items
}

$raw = [System.IO.File]::ReadAllText($srcPath)
$src = Strip-Comments $raw
$start = $src.IndexOf('public void registerDefaults()')
$end = $src.IndexOf('public void registerPost()')
$body = if ($start -ge 0 -and $end -gt $start) { $src.Substring($start, $end - $start) } else { $src }

$skip = @('EnumBedrockOre','BedrockOreType','ItemBedrockOre','EnumAshType','EnumChunkType','DictFrame','oreCertusQuartz','crystalCertusQuartz','IMCCentrifuge')
$recipes = New-Object System.Collections.Generic.List[object]
$idx = 0
while ($true) {
    $found = $body.IndexOf('recipes.put(', $idx)
    if ($found -lt 0) { break }
    $pair = Extract-Balanced $body ($found + 'recipes.put'.Length)
    $call = $pair[0]
    $idx = $pair[1]
    $inner = Rewrite-LbsFalse $call.Substring(1, $call.Length - 2)
    $hitSkip = $false
    foreach ($tok in $skip) { if ($inner.Contains($tok)) { $hitSkip = $true; break } }
    if ($hitSkip) { continue }
    $parts = Split-Args $inner
    if ($parts.Length -ne 2) { continue }
    $inp = Parse-Stack $parts[0]
    $outs = Parse-OutputArray $parts[1]
    if ($null -eq $inp -or $null -eq $outs -or $outs.Count -eq 0) { continue }
    $recipes.Add([ordered]@{ input = $inp; output = @($outs) }) | Out-Null
}

$payload = [ordered]@{
    source = 'legacy-1.7.10 CentrifugeRecipes.registerDefaults'
    recipes = $recipes
}
$json = ($payload | ConvertTo-Json -Depth 8) + "`n"
$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($outPath, $json, $utf8)
Write-Output ("wrote {0} ({1} recipes)" -f $outPath, $recipes.Count)
