# Restore 1.7.10 MineralRecipes compression and related loot/anvil rows.
# Does not invent recipes. Writes a pair only when both IDs exist in this port.
# Skips enum/meta (coke, slag, waste-class, RTG depleted, ore byproduct) and fuel mixes.
$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding $false
$root = Split-Path -Parent $PSScriptRoot
$recipes = Join-Path $root "src\main\resources\data\hbm\recipes"
$anvilPath = Join-Path $root "src\main\resources\data\hbm\machine_recipes\anvil.json"
$lootDir = Join-Path $root "src\main\resources\data\hbm\loot_tables\blocks"
$java = Join-Path $root "src\main\java"

function Get-QuotedIds([string]$text) {
    return [regex]::Matches($text, '"([a-z0-9_/]+)"') | ForEach-Object { $_.Groups[1].Value }
}

function Get-RegisteredIds {
    $ids = New-Object 'System.Collections.Generic.HashSet[string]'
    foreach ($name in @("ModItems.java", "ModBlocks.java")) {
        $text = [IO.File]::ReadAllText((Join-Path $java "com\hbm\registry\$name"))
        foreach ($m in [regex]::Matches($text, '(?:register(?:Ingot|BuildingBlock|MetalBlock|Ore|GlassBlock|BlockItem)?|\.register)\(\s*"([a-z0-9_/]+)"')) {
            [void]$ids.Add($m.Groups[1].Value)
        }
        foreach ($m in [regex]::Matches($text, '(?:ITEMS|BLOCKS)\.register\(\s*"([a-z0-9_/]+)"')) {
            [void]$ids.Add($m.Groups[1].Value)
        }
    }
    $bulk = [IO.File]::ReadAllText((Join-Path $java "com\hbm\registry\ModBulkContent.java"))
    $skipItems = New-Object 'System.Collections.Generic.HashSet[string]'
    $skipBlocks = New-Object 'System.Collections.Generic.HashSet[string]'
    $skipItemBlock = ($bulk -split "SKIP_ITEMS = Set.of\(", 2)[1]
    $skipItemBlock = ($skipItemBlock -split "\);", 2)[0]
    foreach ($id in (Get-QuotedIds $skipItemBlock)) { [void]$skipItems.Add($id) }
    $skipBlockBlock = ($bulk -split "SKIP_BLOCKS = Set.of\(", 2)[1]
    $skipBlockBlock = ($skipBlockBlock -split "\);", 2)[0]
    foreach ($id in (Get-QuotedIds $skipBlockBlock)) { [void]$skipBlocks.Add($id) }
    $itemArr = ($bulk -split "String\[\] ITEM_IDS = \{", 2)[1]
    $itemArr = ($itemArr -split "\};", 2)[0]
    foreach ($id in (Get-QuotedIds $itemArr)) {
        if (-not $skipItems.Contains($id) -and -not $skipBlocks.Contains($id)) { [void]$ids.Add($id) }
    }
    $blockArr = ($bulk -split "String\[\] BLOCK_IDS = \{", 2)[1]
    $blockArr = ($blockArr -split "\};", 2)[0]
    foreach ($id in (Get-QuotedIds $blockArr)) {
        if (-not $skipBlocks.Contains($id)) { [void]$ids.Add($id) }
    }
    return $ids
}

function Test-Id($ids, [string]$path) {
    if ($path.StartsWith("minecraft:")) { return $true }
    $key = ($path -split ":", 2)[-1]
    return $ids.Contains($key)
}

function Write-Utf8([string]$path, [string]$text) {
    $dir = Split-Path -Parent $path
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    [IO.File]::WriteAllText($path, $text, $utf8)
}

function New-Shaped9([string]$ingredient, [string]$result, [int]$count = 1) {
    $countBit = if ($count -ne 1) { ", `"count`": $count" } else { "" }
    return @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["###","###","###"],
  "key": { "#": { "item": "$ingredient" } },
  "result": { "item": "$result"$countBit }
}
"@
}

function New-Shaped4([string]$ingredient, [string]$result) {
    return @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["##","##"],
  "key": { "#": { "item": "$ingredient" } },
  "result": { "item": "$result" }
}
"@
}

function New-Shapeless([string]$ingredient, [string]$result, [int]$count) {
    return @"
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [ { "item": "$ingredient" } ],
  "result": { "item": "$result", "count": $count }
}
"@
}

function Test-AlreadyPacked([string]$one, [string]$nine) {
    $names = @(
        "${one}_from_${nine}.json"
    )
    if ($nine.EndsWith("_tiny")) { $names += "${one}_from_tiny.json" }
    if ($nine.StartsWith("nugget_")) { $names += "${one}_from_nuggets.json" }
    foreach ($name in $names) {
        if (Test-Path (Join-Path $recipes $name)) { return $true }
    }
    return $false
}

function Test-AlreadyUnpacked([string]$one, [string]$nine) {
    $names = @(
        "${nine}_from_${one}.json"
    )
    if ($nine.EndsWith("_tiny")) { $names += "${nine}_from_powder.json" }
    if ($nine.StartsWith("nugget_") -and $one.StartsWith("ingot_")) { $names += "${nine}_from_ingot.json" }
    foreach ($name in $names) {
        if (Test-Path (Join-Path $recipes $name)) { return $true }
    }
    return $false
}

function Write-IfNew([string]$path, [string]$text, [System.Collections.Generic.List[string]]$written) {
    if (Test-Path $path) { return }
    Write-Utf8 $path ($text.TrimEnd() + "`n")
    [void]$written.Add((Split-Path -Leaf $path))
}

function Hbm([string]$path) {
    if ($path.StartsWith("minecraft:") -or $path.StartsWith("hbm:")) { return $path }
    return "hbm:$path"
}

$pairs = @(
    @("dust", "dust_tiny"),
    @("powder_coal", "powder_coal_tiny"),
    @("ingot_mercury", "nugget_mercury"),
    @("block_aluminium", "ingot_aluminium"),
    @("block_graphite", "ingot_graphite"),
    @("block_boron", "ingot_boron"),
    @("block_schraranium", "ingot_schraranium"),
    @("block_lanthanium", "ingot_lanthanium"),
    @("block_ra226", "ingot_ra226"),
    @("block_actinium", "ingot_actinium"),
    @("block_schrabidate", "ingot_schrabidate"),
    @("block_coltan", "fragment_coltan"),
    @("block_smore", "ingot_smore"),
    @("block_semtex", "ingot_semtex"),
    @("block_c4", "ingot_c4"),
    @("block_polymer", "ingot_polymer"),
    @("block_bakelite", "ingot_bakelite"),
    @("block_rubber", "ingot_rubber"),
    @("block_cadmium", "ingot_cadmium"),
    @("block_tcalloy", "ingot_tcalloy"),
    @("block_cdalloy", "ingot_cdalloy"),
    @("ingot_niobium", "nugget_niobium"),
    @("block_niobium", "ingot_niobium"),
    @("ingot_bismuth", "nugget_bismuth"),
    @("block_bismuth", "ingot_bismuth"),
    @("ingot_tantalium", "nugget_tantalium"),
    @("block_tantalium", "ingot_tantalium"),
    @("ingot_zirconium", "nugget_zirconium"),
    @("block_zirconium", "ingot_zirconium"),
    @("ingot_dineutronium", "nugget_dineutronium"),
    @("block_dineutronium", "ingot_dineutronium"),
    @("nuclear_waste_vitrified", "nuclear_waste_vitrified_tiny"),
    @("block_waste_vitrified", "nuclear_waste_vitrified"),
    @("ingot_silicon", "nugget_silicon"),
    @("powder_boron", "powder_boron_tiny"),
    @("powder_sr90", "powder_sr90_tiny"),
    @("powder_xe135", "powder_xe135_tiny"),
    @("powder_cs137", "powder_cs137_tiny"),
    @("powder_i131", "powder_i131_tiny"),
    @("ingot_technetium", "nugget_technetium"),
    @("ingot_co60", "nugget_co60"),
    @("ingot_sr90", "nugget_sr90"),
    @("ingot_au198", "nugget_au198"),
    @("ingot_pb209", "nugget_pb209"),
    @("ingot_ra226", "nugget_ra226"),
    @("ingot_actinium", "nugget_actinium"),
    @("ingot_arsenic", "nugget_arsenic"),
    @("ingot_pu241", "nugget_pu241"),
    @("ingot_am241", "nugget_am241"),
    @("ingot_am242", "nugget_am242"),
    @("ingot_am_mix", "nugget_am_mix"),
    @("ingot_americium_fuel", "nugget_americium_fuel"),
    @("ingot_gh336", "nugget_gh336"),
    @("ingot_pu_mix", "nugget_pu_mix"),
    @("block_pu_mix", "ingot_pu_mix"),
    @("ingot_neptunium_fuel", "nugget_neptunium_fuel"),
    @("block_copper", "ingot_copper"),
    @("block_fluorite", "fluorite"),
    @("block_niter", "niter"),
    @("block_red_copper", "ingot_red_copper"),
    @("block_steel", "ingot_steel"),
    @("block_sulfur", "sulfur"),
    @("block_titanium", "ingot_titanium"),
    @("block_tungsten", "ingot_tungsten"),
    @("block_uranium", "ingot_uranium"),
    @("block_thorium", "ingot_th232"),
    @("block_lead", "ingot_lead"),
    @("block_trinitite", "trinitite"),
    @("block_waste", "nuclear_waste"),
    @("block_beryllium", "ingot_beryllium"),
    @("block_schrabidium", "ingot_schrabidium"),
    @("block_euphemium", "ingot_euphemium"),
    @("block_magnetized_tungsten", "ingot_magnetized_tungsten"),
    @("block_combine_steel", "ingot_combine_steel"),
    @("block_australium", "ingot_australium"),
    @("block_desh", "ingot_desh"),
    @("block_dura_steel", "ingot_dura_steel"),
    @("block_yellowcake", "powder_yellowcake"),
    @("block_starmetal", "ingot_starmetal"),
    @("block_u233", "ingot_u233"),
    @("block_u235", "ingot_u235"),
    @("block_u238", "ingot_u238"),
    @("block_uranium_fuel", "ingot_uranium_fuel"),
    @("block_neptunium", "ingot_neptunium"),
    @("block_polonium", "ingot_polonium"),
    @("block_plutonium", "ingot_plutonium"),
    @("block_pu238", "ingot_pu238"),
    @("block_pu239", "ingot_pu239"),
    @("block_pu240", "ingot_pu240"),
    @("block_mox_fuel", "ingot_mox_fuel"),
    @("block_plutonium_fuel", "ingot_plutonium_fuel"),
    @("block_thorium_fuel", "ingot_thorium_fuel"),
    @("block_solinium", "ingot_solinium"),
    @("block_schrabidium_fuel", "ingot_schrabidium_fuel"),
    @("block_lithium", "lithium"),
    @("block_white_phosphorus", "ingot_phosphorus"),
    @("block_red_phosphorus", "powder_fire"),
    @("block_insulator", "plate_polymer"),
    @("block_asbestos", "ingot_asbestos"),
    @("block_fiberglass", "ingot_fiberglass"),
    @("block_cobalt", "ingot_cobalt"),
    @("ingot_plutonium", "nugget_plutonium"),
    @("ingot_pu238", "nugget_pu238"),
    @("ingot_pu239", "nugget_pu239"),
    @("ingot_pu240", "nugget_pu240"),
    @("ingot_th232", "nugget_th232"),
    @("ingot_uranium", "nugget_uranium"),
    @("ingot_u233", "nugget_u233"),
    @("ingot_u235", "nugget_u235"),
    @("ingot_u238", "nugget_u238"),
    @("ingot_neptunium", "nugget_neptunium"),
    @("ingot_polonium", "nugget_polonium"),
    @("ingot_lead", "nugget_lead"),
    @("ingot_beryllium", "nugget_beryllium"),
    @("ingot_schrabidium", "nugget_schrabidium"),
    @("ingot_uranium_fuel", "nugget_uranium_fuel"),
    @("ingot_thorium_fuel", "nugget_thorium_fuel"),
    @("ingot_plutonium_fuel", "nugget_plutonium_fuel"),
    @("ingot_mox_fuel", "nugget_mox_fuel"),
    @("ingot_schrabidium_fuel", "nugget_schrabidium_fuel"),
    @("ingot_hes", "nugget_hes"),
    @("ingot_les", "nugget_les"),
    @("ingot_australium", "nugget_australium"),
    @("powder_steel", "powder_steel_tiny"),
    @("powder_lithium", "powder_lithium_tiny"),
    @("powder_cobalt", "powder_cobalt_tiny"),
    @("powder_neodymium", "powder_neodymium_tiny"),
    @("powder_niobium", "powder_niobium_tiny"),
    @("powder_cerium", "powder_cerium_tiny"),
    @("powder_lanthanium", "powder_lanthanium_tiny"),
    @("powder_actinium", "powder_actinium_tiny"),
    @("powder_meteorite", "powder_meteorite_tiny"),
    @("ingot_solinium", "nugget_solinium"),
    @("nuclear_waste", "nuclear_waste_tiny"),
    @("egg_balefire", "egg_balefire_shard"),
    @("powder_paleogenite", "powder_paleogenite_tiny"),
    @("ingot_osmiridium", "nugget_osmiridium"),
    @("ingot_euphemium", "nugget_euphemium")
)

function New-SilkOrDrop([string]$silkItem, [string]$dropItem, $count) {
    $countFn = if ($null -ne $count) {
@"
              "functions": [
                {
                  "function": "minecraft:set_count",
                  "count": $count,
                  "add": false
                },
                { "function": "minecraft:explosion_decay" }
              ]
"@
    } else {
@"
              "functions": [ { "function": "minecraft:explosion_decay" } ]
"@
    }
    return @"
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "bonus_rolls": 0,
      "entries": [
        {
          "type": "minecraft:alternatives",
          "children": [
            {
              "type": "minecraft:item",
              "name": "$silkItem",
              "conditions": [
                {
                  "condition": "minecraft:match_tool",
                  "predicate": {
                    "enchantments": [
                      { "enchantment": "minecraft:silk_touch", "levels": { "min": 1 } }
                    ]
                  }
                }
              ]
            },
            {
              "type": "minecraft:item",
              "name": "$dropItem",
$countFn
            }
          ]
        }
      ]
    }
  ]
}
"@
}

function Test-AnvilInput([string]$text, [string]$item, [int]$count) {
    $compact = [regex]::Replace($text, '\s+', ' ')
    return $compact.Contains('"inputs": [ { "item": "hbm:' + $item + '", "count": ' + $count + ' } ]')
}

$ids = Get-RegisteredIds
$written = New-Object 'System.Collections.Generic.List[string]'
$skipped = New-Object 'System.Collections.Generic.List[string]'
$seen = New-Object 'System.Collections.Generic.HashSet[string]'

foreach ($pair in $pairs) {
    $one = $pair[0]
    $nine = $pair[1]
    $key = "$one|$nine"
    if (-not $seen.Add($key)) { continue }
    if (-not (Test-Id $ids $one) -or -not (Test-Id $ids $nine)) {
        [void]$skipped.Add("$one / $nine")
        continue
    }
    if (-not (Test-AlreadyPacked $one $nine)) {
        Write-IfNew (Join-Path $recipes "${one}_from_${nine}.json") (New-Shaped9 (Hbm $nine) (Hbm $one)) $written
    }
    if (-not (Test-AlreadyUnpacked $one $nine)) {
        Write-IfNew (Join-Path $recipes "${nine}_from_${one}.json") (New-Shapeless (Hbm $one) (Hbm $nine) 9) $written
    }
}

$unpackOnly = @(
    @{ One = "block_waste_painted"; Nine = "nuclear_waste" }
)
foreach ($pair in $unpackOnly) {
    $one = $pair.One; $nine = $pair.Nine
    if (-not (Test-Id $ids $one) -or -not (Test-Id $ids $nine)) {
        [void]$skipped.Add("unpack $one / $nine")
        continue
    }
    Write-IfNew (Join-Path $recipes "${nine}_from_${one}.json") (New-Shapeless (Hbm $one) (Hbm $nine) 9) $written
}

$specials = @(
    @{ One = "block_scrap"; Nine = "scrap"; N = 4; File = "block_scrap_from_scrap.json" },
    @{ One = "block_scrap"; Nine = "dust"; N = 9; File = "block_scrap_from_dust.json" },
    @{ One = "block_meteor_cobble"; Nine = "fragment_meteorite"; N = 4; File = "block_meteor_cobble_from_fragment_meteorite.json" },
    @{ One = "block_meteor_broken"; Nine = "fragment_meteorite"; N = 9; File = "block_meteor_broken_from_fragment_meteorite.json" },
    @{ One = "nitra"; Nine = "nitra_small"; N = 4; File = "nitra_from_nitra_small.json" },
    @{ One = "egg_balefire_shard"; Nine = "powder_balefire"; N = 4; File = "egg_balefire_shard_from_powder_balefire.json" }
)
foreach ($s in $specials) {
    if (-not (Test-Id $ids $s.One) -or -not (Test-Id $ids $s.Nine)) {
        [void]$skipped.Add($s.File)
        continue
    }
    $body = if ($s.N -eq 4) { New-Shaped4 (Hbm $s.Nine) (Hbm $s.One) } else { New-Shaped9 (Hbm $s.Nine) (Hbm $s.One) }
    Write-IfNew (Join-Path $recipes $s.File) $body $written
}

if ((Test-Id $ids "nitra") -and (Test-Id $ids "nitra_small")) {
    Write-IfNew (Join-Path $recipes "nitra_small_from_nitra.json") (New-Shapeless (Hbm "nitra") (Hbm "nitra_small") 4) $written
}

if ((Test-Id $ids "ingot_mercury") -and (Test-Id $ids "bottle_mercury")) {
    $bottle = @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["###","#B#","###"],
  "key": {
    "#": { "item": "hbm:ingot_mercury" },
    "B": { "item": "minecraft:glass_bottle" }
  },
  "result": { "item": "hbm:bottle_mercury" }
}
"@
    Write-IfNew (Join-Path $recipes "bottle_mercury.json") $bottle $written
    Write-IfNew (Join-Path $recipes "ingot_mercury_from_bottle.json") (New-Shapeless (Hbm "bottle_mercury") (Hbm "ingot_mercury") 8) $written
}

if ((Test-Id $ids "block_schrabidium_cluster") -and (Test-Id $ids "ingot_schrabidium") -and (Test-Id $ids "ingot_starmetal") -and (Test-Id $ids "ingot_schrabidate")) {
    $cluster = @"
{
  "type": "minecraft:crafting_shaped",
  "pattern": ["#S#","SXS","#S#"],
  "key": {
    "#": { "item": "hbm:ingot_schrabidium" },
    "S": { "item": "hbm:ingot_starmetal" },
    "X": { "item": "hbm:ingot_schrabidate" }
  },
  "result": { "item": "hbm:block_schrabidium_cluster" }
}
"@
    Write-IfNew (Join-Path $recipes "block_schrabidium_cluster.json") $cluster $written
}

if ((Test-Id $ids "cell_balefire") -and (Test-Id $ids "egg_balefire_shard")) {
    Write-IfNew (Join-Path $recipes "egg_balefire_shard_from_cell_balefire.json") (New-Shaped9 (Hbm "cell_balefire") (Hbm "egg_balefire_shard")) $written
}

$anvilText = [IO.File]::ReadAllText($anvilPath)
$chunks = New-Object 'System.Collections.Generic.List[string]'
$anvilRows = @(
    @("deco_titanium", 4, "ingot_titanium", 1),
    @("deco_red_copper", 4, "ingot_red_copper", 1),
    @("deco_tungsten", 4, "ingot_tungsten", 1),
    @("deco_aluminium", 4, "ingot_aluminium", 1),
    @("deco_steel", 4, "ingot_steel", 1),
    @("deco_rusty_steel", 8, "ingot_steel", 1),
    @("deco_lead", 4, "ingot_lead", 1),
    @("deco_beryllium", 4, "ingot_beryllium", 1),
    @("deco_asbestos", 4, "ingot_asbestos", 1)
)
foreach ($row in $anvilRows) {
    $inp = $row[0]; $inCount = $row[1]; $out = $row[2]; $outCount = $row[3]
    if (-not (Test-Id $ids $inp) -or -not (Test-Id $ids $out)) { continue }
    if (Test-AnvilInput $anvilText $inp $inCount) { continue }
    [void]$chunks.Add(@"
    {
      "tierLower": 1,
      "tierUpper": -1,
      "overlay": "RECYCLING",
      "inputs": [ { "item": "hbm:$inp", "count": $inCount } ],
      "outputs": [ { "item": "hbm:$out", "count": $outCount } ]
    }
"@.TrimEnd())
}
if ((Test-Id $ids "heater_firebox") -and (Test-Id $ids "plate_steel") -and (Test-Id $ids "ingot_copper") -and -not (Test-AnvilInput $anvilText "heater_firebox" 1)) {
    [void]$chunks.Add(@"
    {
      "tierLower": 2,
      "tierUpper": -1,
      "overlay": "RECYCLING",
      "inputs": [ { "item": "hbm:heater_firebox", "count": 1 } ],
      "outputs": [
        { "item": "hbm:plate_steel", "count": 8 },
        { "item": "hbm:ingot_copper", "count": 6 }
      ]
    }
"@.TrimEnd())
}
if ((Test-Id $ids "heater_oven") -and (Test-Id $ids "ingot_firebrick") -and (Test-Id $ids "ingot_copper") -and -not (Test-AnvilInput $anvilText "heater_oven" 1)) {
    [void]$chunks.Add(@"
    {
      "tierLower": 2,
      "tierUpper": -1,
      "overlay": "RECYCLING",
      "inputs": [ { "item": "hbm:heater_oven", "count": 1 } ],
      "outputs": [
        { "item": "hbm:ingot_firebrick", "count": 16 },
        { "item": "hbm:ingot_copper", "count": 8 }
      ]
    }
"@.TrimEnd())
}
if ($chunks.Count -gt 0) {
    $idx = $anvilText.TrimEnd().LastIndexOf("`n  ]")
    if ($idx -lt 0) { throw "anvil.json: could not find recipes array end" }
    $insertion = ",`n" + ($chunks -join ",`n") + "`n"
    $anvilText = $anvilText.Substring(0, $idx) + $insertion + $anvilText.Substring($idx)
    Write-Utf8 $anvilPath $anvilText
    [void]$written.Add("anvil.json+$($chunks.Count)")
}

$caps = @(
    @("block_cap_nuka", "cap_nuka"),
    @("block_cap_quantum", "cap_quantum"),
    @("block_cap_sparkle", "cap_sparkle"),
    @("block_cap_rad", "cap_rad"),
    @("block_cap_korl", "cap_korl"),
    @("block_cap_fritz", "cap_fritz")
)
foreach ($cap in $caps) {
    if ((Test-Id $ids $cap[0]) -and (Test-Id $ids $cap[1])) {
        Write-Utf8 (Join-Path $lootDir "$($cap[0]).json") ((New-SilkOrDrop (Hbm $cap[0]) (Hbm $cap[1]) 128).TrimEnd() + "`n")
        [void]$written.Add("loot $($cap[0])")
    }
}
foreach ($block in @("waste_earth", "waste_mycelium")) {
    if (Test-Id $ids $block) {
        Write-Utf8 (Join-Path $lootDir "$block.json") ((New-SilkOrDrop (Hbm $block) "minecraft:dirt" $null).TrimEnd() + "`n")
        [void]$written.Add("loot $block")
    }
}

Write-Host "wrote $($written.Count) files"
Write-Host "skipped missing IDs:"
$skipped | ForEach-Object { Write-Host "  $_" }
