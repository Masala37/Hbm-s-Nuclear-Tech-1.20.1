# Generates 1.7 HbmWorldGen / BlockOre / SmeltingRecipes datapack files.
# UTF-8 no BOM. Does not invent spawn rates, drops, or furnace rows.

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$res = Join-Path $root 'src\main\resources'

function Write-Utf8([string]$path, [string]$text) {
    $dir = Split-Path -Parent $path
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }
    $utf8 = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($path, $text.Replace("`n", "`r`n"), $utf8)
}

function OreConfigured([string]$block, [string]$target) {
    if ($target -eq 'nether') {
        return @"
{
  "type": "minecraft:ore",
  "config": {
    "size": SIZE,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:block_match", "block": "minecraft:netherrack" },
        "state": { "Name": "hbm:$block" }
      }
    ]
  }
}
"@
    }
    return @"
{
  "type": "minecraft:ore",
  "config": {
    "size": SIZE,
    "discard_chance_on_air_exposure": 0.0,
    "targets": [
      {
        "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables" },
        "state": { "Name": "hbm:$block" }
      },
      {
        "target": { "predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables" },
        "state": { "Name": "hbm:$block" }
      }
    ]
  }
}
"@
}

function OrePlaced([string]$feature, [int]$count, [int]$minY, [int]$maxY, [int]$rarity) {
    $rarityJson = ''
    if ($rarity -gt 1) {
        $rarityJson = "    { `"type`": `"minecraft:rarity_filter`", `"chance`": $rarity },`r`n"
    }
    $countJson = "    { `"type`": `"minecraft:count`", `"count`": $count },`r`n"
    if ($rarity -gt 1) {
        $countJson = ''
    }
    return @"
{
  "feature": "hbm:$feature",
  "placement": [
$rarityJson$countJson    { "type": "minecraft:in_square" },
    {
      "type": "minecraft:height_range",
      "height": {
        "type": "minecraft:uniform",
        "min_inclusive": { "absolute": $minY },
        "max_inclusive": { "absolute": $maxY }
      }
    },
    { "type": "minecraft:biome" }
  ]
}
"@
}

# 1.7 DungeonToolbox: y = minHeight + rand(variance) => [min, min+variance-1]
# Config defaults from WorldConfig.loadFromConfig, not field initializers.
$overworld = @(
    @{ id = 'ore_uranium'; size = 5; count = 7; min = 5; var = 20 },
    @{ id = 'ore_thorium'; size = 5; count = 7; min = 5; var = 25 },
    @{ id = 'ore_titanium'; size = 6; count = 8; min = 5; var = 30 },
    @{ id = 'ore_sulfur'; size = 8; count = 5; min = 5; var = 30 },
    @{ id = 'ore_aluminium'; size = 6; count = 7; min = 5; var = 40 },
    @{ id = 'ore_copper'; size = 6; count = 12; min = 5; var = 45 },
    @{ id = 'ore_fluorite'; size = 4; count = 6; min = 5; var = 45 },
    @{ id = 'ore_niter'; size = 6; count = 6; min = 5; var = 30 },
    @{ id = 'ore_tungsten'; size = 8; count = 10; min = 5; var = 30 },
    @{ id = 'ore_lead'; size = 9; count = 6; min = 5; var = 30 },
    @{ id = 'ore_beryllium'; size = 4; count = 6; min = 5; var = 30 },
    @{ id = 'ore_rare'; size = 5; count = 6; min = 5; var = 20 },
    @{ id = 'ore_lignite'; size = 24; count = 2; min = 35; var = 25 },
    @{ id = 'ore_asbestos'; size = 4; count = 2; min = 16; var = 16 },
    @{ id = 'ore_cinnebar'; size = 4; count = 1; min = 8; var = 16 },
    @{ id = 'ore_cobalt'; size = 4; count = 2; min = 4; var = 8 },
    @{ id = 'cluster_iron'; size = 6; count = 4; min = 15; var = 45 },
    @{ id = 'cluster_titanium'; size = 6; count = 2; min = 15; var = 30 },
    @{ id = 'cluster_aluminium'; size = 6; count = 3; min = 15; var = 35 },
    @{ id = 'cluster_copper'; size = 6; count = 4; min = 15; var = 20 },
    @{ id = 'ore_alexandrite'; size = 3; count = 1; min = 10; var = 5; rarity = 100 }
)

$nether = @(
    @{ id = 'ore_nether_uranium'; size = 6; count = 8; min = 0; var = 127 },
    @{ id = 'ore_nether_tungsten'; size = 10; count = 10; min = 0; var = 127 },
    @{ id = 'ore_nether_sulfur'; size = 12; count = 26; min = 0; var = 127 },
    @{ id = 'ore_nether_fire'; size = 6; count = 24; min = 0; var = 127 },
    @{ id = 'ore_nether_coal'; size = 32; count = 8; min = 16; var = 96 },
    @{ id = 'ore_nether_cobalt'; size = 6; count = 2; min = 100; var = 26 }
)

$owFeatures = @()
foreach ($ore in $overworld) {
    $max = $ore.min + $ore.var - 1
    $cfg = (OreConfigured $ore.id 'stone').Replace('SIZE', [string]$ore.size)
    Write-Utf8 (Join-Path $res "data\hbm\worldgen\configured_feature\$($ore.id).json") $cfg
    $rarity = 0
    if ($ore.ContainsKey('rarity')) { $rarity = $ore.rarity }
    Write-Utf8 (Join-Path $res "data\hbm\worldgen\placed_feature\$($ore.id).json") (OrePlaced $ore.id $ore.count $ore.min $max $rarity)
    $owFeatures += "hbm:$($ore.id)"
}

$netherFeatures = @()
foreach ($ore in $nether) {
    $max = $ore.min + $ore.var - 1
    $cfg = (OreConfigured $ore.id 'nether').Replace('SIZE', [string]$ore.size)
    Write-Utf8 (Join-Path $res "data\hbm\worldgen\configured_feature\$($ore.id).json") $cfg
    Write-Utf8 (Join-Path $res "data\hbm\worldgen\placed_feature\$($ore.id).json") (OrePlaced $ore.id $ore.count $ore.min $max 0)
    $netherFeatures += "hbm:$($ore.id)"
}

function FeatureArray([string[]]$ids) {
    $lines = $ids | ForEach-Object { "    `"$_`"" }
    return ($lines -join ",`r`n")
}

Write-Utf8 (Join-Path $res 'data\hbm\forge\biome_modifier\add_overworld_ores.json') @"
{
  "type": "forge:add_features",
  "biomes": "#minecraft:is_overworld",
  "features": [
$(FeatureArray $owFeatures)
  ],
  "step": "underground_ores"
}
"@

Write-Utf8 (Join-Path $res 'data\hbm\forge\biome_modifier\add_nether_ores.json') @"
{
  "type": "forge:add_features",
  "biomes": "#minecraft:is_nether",
  "features": [
$(FeatureArray $netherFeatures)
  ],
  "step": "underground_ores"
}
"@

function LootSelf([string]$block) {
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
              "name": "hbm:$block",
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
              "name": "hbm:DROP",
              "functions": [
                {
                  "function": "minecraft:set_count",
                  "count": { "type": "minecraft:uniform", "min": MIN, "max": MAX },
                  "add": false
                },
                {
                  "function": "minecraft:apply_bonus",
                  "enchantment": "minecraft:fortune",
                  "formula": "minecraft:ore_drops"
                },
                { "function": "minecraft:explosion_decay" }
              ]
            }
          ]
        }
      ]
    }
  ]
}
"@
}

$itemDrops = @(
    @{ block = 'ore_sulfur'; drop = 'sulfur'; min = 2; max = 4 },
    @{ block = 'ore_nether_sulfur'; drop = 'sulfur'; min = 2; max = 4 },
    @{ block = 'ore_niter'; drop = 'niter'; min = 2; max = 4 },
    @{ block = 'ore_fluorite'; drop = 'fluorite'; min = 2; max = 4 },
    @{ block = 'ore_lignite'; drop = 'lignite'; min = 1; max = 1 },
    @{ block = 'ore_asbestos'; drop = 'ingot_asbestos'; min = 1; max = 1 },
    @{ block = 'ore_gneiss_asbestos'; drop = 'ingot_asbestos'; min = 1; max = 1 },
    @{ block = 'ore_cinnebar'; drop = 'cinnebar'; min = 1; max = 1 },
    @{ block = 'ore_cobalt'; drop = 'fragment_cobalt'; min = 4; max = 9 },
    @{ block = 'ore_nether_cobalt'; drop = 'fragment_cobalt'; min = 5; max = 12 }
)

foreach ($row in $itemDrops) {
    $json = (LootSelf $row.block).Replace('DROP', $row.drop).Replace('MIN', [string]$row.min).Replace('MAX', [string]$row.max)
    Write-Utf8 (Join-Path $res "data\hbm\loot_tables\blocks\$($row.block).json") $json
}

Write-Utf8 (Join-Path $res 'data\hbm\loot_tables\blocks\ore_nether_fire.json') @'
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
              "name": "hbm:ore_nether_fire",
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
              "type": "minecraft:alternatives",
              "children": [
                {
                  "type": "minecraft:item",
                  "name": "hbm:ingot_phosphorus",
                  "weight": 1,
                  "functions": [
                    { "function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops" },
                    { "function": "minecraft:explosion_decay" }
                  ]
                },
                {
                  "type": "minecraft:item",
                  "name": "hbm:powder_fire",
                  "weight": 9,
                  "functions": [
                    { "function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops" },
                    { "function": "minecraft:explosion_decay" }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
'@

function Smelt([string]$id, [string]$inItem, [string]$result, [string]$xp) {
    Write-Utf8 (Join-Path $res "data\hbm\recipes\$id.json") @"
{
  "type": "minecraft:smelting",
  "ingredient": { "item": "$inItem" },
  "result": "$result",
  "experience": $xp,
  "cookingtime": 200
}
"@
}

# 1.7 SmeltingRecipes.AddSmeltingRec — skip missing items, meta/enum, ItemHot, gravel vanilla change.
$smelts = @(
    @{ id = 'smelt_ore_thorium'; in = 'hbm:ore_thorium'; out = 'hbm:ingot_th232'; xp = '3.0' },
    @{ id = 'smelt_ore_uranium'; in = 'hbm:ore_uranium'; out = 'hbm:ingot_uranium'; xp = '6.0' },
    @{ id = 'smelt_ore_uranium_scorched'; in = 'hbm:ore_uranium_scorched'; out = 'hbm:ingot_uranium'; xp = '6.0' },
    @{ id = 'smelt_ore_nether_uranium'; in = 'hbm:ore_nether_uranium'; out = 'hbm:ingot_uranium'; xp = '12.0' },
    @{ id = 'smelt_ore_nether_uranium_scorched'; in = 'hbm:ore_nether_uranium_scorched'; out = 'hbm:ingot_uranium'; xp = '12.0' },
    @{ id = 'smelt_ore_nether_plutonium'; in = 'hbm:ore_nether_plutonium'; out = 'hbm:ingot_plutonium'; xp = '24.0' },
    @{ id = 'smelt_ore_titanium'; in = 'hbm:ore_titanium'; out = 'hbm:ingot_titanium'; xp = '3.0' },
    @{ id = 'smelt_ore_copper'; in = 'hbm:ore_copper'; out = 'hbm:ingot_copper'; xp = '2.5' },
    @{ id = 'smelt_ore_tungsten'; in = 'hbm:ore_tungsten'; out = 'hbm:ingot_tungsten'; xp = '6.0' },
    @{ id = 'smelt_ore_nether_tungsten'; in = 'hbm:ore_nether_tungsten'; out = 'hbm:ingot_tungsten'; xp = '12.0' },
    @{ id = 'smelt_ore_lead'; in = 'hbm:ore_lead'; out = 'hbm:ingot_lead'; xp = '3.0' },
    @{ id = 'smelt_ore_beryllium'; in = 'hbm:ore_beryllium'; out = 'hbm:ingot_beryllium'; xp = '2.0' },
    @{ id = 'smelt_ore_schrabidium'; in = 'hbm:ore_schrabidium'; out = 'hbm:ingot_schrabidium'; xp = '128.0' },
    @{ id = 'smelt_ore_nether_schrabidium'; in = 'hbm:ore_nether_schrabidium'; out = 'hbm:ingot_schrabidium'; xp = '256.0' },
    @{ id = 'smelt_ore_cobalt'; in = 'hbm:ore_cobalt'; out = 'hbm:ingot_cobalt'; xp = '2.0' },
    @{ id = 'smelt_ore_nether_cobalt'; in = 'hbm:ore_nether_cobalt'; out = 'hbm:ingot_cobalt'; xp = '2.0' },
    @{ id = 'smelt_ore_gneiss_iron'; in = 'hbm:ore_gneiss_iron'; out = 'minecraft:iron_ingot'; xp = '5.0' },
    @{ id = 'smelt_ore_gneiss_gold'; in = 'hbm:ore_gneiss_gold'; out = 'minecraft:gold_ingot'; xp = '5.0' },
    @{ id = 'smelt_ore_gneiss_uranium'; in = 'hbm:ore_gneiss_uranium'; out = 'hbm:ingot_uranium'; xp = '12.0' },
    @{ id = 'smelt_ore_gneiss_uranium_scorched'; in = 'hbm:ore_gneiss_uranium_scorched'; out = 'hbm:ingot_uranium'; xp = '12.0' },
    @{ id = 'smelt_ore_gneiss_copper'; in = 'hbm:ore_gneiss_copper'; out = 'hbm:ingot_copper'; xp = '5.0' },
    @{ id = 'smelt_ore_gneiss_lithium'; in = 'hbm:ore_gneiss_lithium'; out = 'hbm:lithium'; xp = '10.0' },
    @{ id = 'smelt_ore_gneiss_schrabidium'; in = 'hbm:ore_gneiss_schrabidium'; out = 'hbm:ingot_schrabidium'; xp = '256.0' },
    @{ id = 'smelt_ore_australium'; in = 'hbm:ore_australium'; out = 'hbm:nugget_australium'; xp = '2.5' },
    @{ id = 'smelt_powder_australium'; in = 'hbm:powder_australium'; out = 'hbm:ingot_australium'; xp = '5.0' },
    @{ id = 'smelt_powder_lead'; in = 'hbm:powder_lead'; out = 'hbm:ingot_lead'; xp = '1.0' },
    @{ id = 'smelt_powder_neptunium'; in = 'hbm:powder_neptunium'; out = 'hbm:ingot_neptunium'; xp = '1.0' },
    @{ id = 'smelt_powder_polonium'; in = 'hbm:powder_polonium'; out = 'hbm:ingot_polonium'; xp = '1.0' },
    @{ id = 'smelt_powder_schrabidium'; in = 'hbm:powder_schrabidium'; out = 'hbm:ingot_schrabidium'; xp = '5.0' },
    @{ id = 'smelt_powder_schrabidate'; in = 'hbm:powder_schrabidate'; out = 'hbm:ingot_schrabidate'; xp = '5.0' },
    @{ id = 'smelt_powder_euphemium'; in = 'hbm:powder_euphemium'; out = 'hbm:ingot_euphemium'; xp = '10.0' },
    @{ id = 'smelt_powder_aluminium'; in = 'hbm:powder_aluminium'; out = 'hbm:ingot_aluminium'; xp = '1.0' },
    @{ id = 'smelt_powder_beryllium'; in = 'hbm:powder_beryllium'; out = 'hbm:ingot_beryllium'; xp = '1.0' },
    @{ id = 'smelt_powder_copper'; in = 'hbm:powder_copper'; out = 'hbm:ingot_copper'; xp = '1.0' },
    @{ id = 'smelt_powder_gold'; in = 'hbm:powder_gold'; out = 'minecraft:gold_ingot'; xp = '1.0' },
    @{ id = 'smelt_powder_iron'; in = 'hbm:powder_iron'; out = 'minecraft:iron_ingot'; xp = '1.0' },
    @{ id = 'smelt_powder_titanium'; in = 'hbm:powder_titanium'; out = 'hbm:ingot_titanium'; xp = '1.0' },
    @{ id = 'smelt_powder_cobalt'; in = 'hbm:powder_cobalt'; out = 'hbm:ingot_cobalt'; xp = '1.0' },
    @{ id = 'smelt_powder_tungsten'; in = 'hbm:powder_tungsten'; out = 'hbm:ingot_tungsten'; xp = '1.0' },
    @{ id = 'smelt_powder_uranium'; in = 'hbm:powder_uranium'; out = 'hbm:ingot_uranium'; xp = '1.0' },
    @{ id = 'smelt_powder_thorium'; in = 'hbm:powder_thorium'; out = 'hbm:ingot_th232'; xp = '1.0' },
    @{ id = 'smelt_powder_plutonium'; in = 'hbm:powder_plutonium'; out = 'hbm:ingot_plutonium'; xp = '1.0' },
    @{ id = 'smelt_powder_combine_steel'; in = 'hbm:powder_combine_steel'; out = 'hbm:ingot_combine_steel'; xp = '1.0' },
    @{ id = 'smelt_powder_magnetized_tungsten'; in = 'hbm:powder_magnetized_tungsten'; out = 'hbm:ingot_magnetized_tungsten'; xp = '1.0' },
    @{ id = 'smelt_powder_red_copper'; in = 'hbm:powder_red_copper'; out = 'hbm:ingot_red_copper'; xp = '1.0' },
    @{ id = 'smelt_powder_steel'; in = 'hbm:powder_steel'; out = 'hbm:ingot_steel'; xp = '1.0' },
    @{ id = 'smelt_powder_lithium'; in = 'hbm:powder_lithium'; out = 'hbm:lithium'; xp = '1.0' },
    @{ id = 'smelt_powder_dura_steel'; in = 'hbm:powder_dura_steel'; out = 'hbm:ingot_dura_steel'; xp = '1.0' },
    @{ id = 'smelt_powder_polymer'; in = 'hbm:powder_polymer'; out = 'hbm:ingot_polymer'; xp = '1.0' },
    @{ id = 'smelt_powder_bakelite'; in = 'hbm:powder_bakelite'; out = 'hbm:ingot_bakelite'; xp = '1.0' },
    @{ id = 'smelt_powder_lanthanium'; in = 'hbm:powder_lanthanium'; out = 'hbm:ingot_lanthanium'; xp = '1.0' },
    @{ id = 'smelt_powder_actinium'; in = 'hbm:powder_actinium'; out = 'hbm:ingot_actinium'; xp = '1.0' },
    @{ id = 'smelt_powder_boron'; in = 'hbm:powder_boron'; out = 'hbm:ingot_boron'; xp = '1.0' },
    @{ id = 'smelt_powder_desh'; in = 'hbm:powder_desh'; out = 'hbm:ingot_desh'; xp = '1.0' },
    @{ id = 'smelt_powder_dineutronium'; in = 'hbm:powder_dineutronium'; out = 'hbm:ingot_dineutronium'; xp = '5.0' },
    @{ id = 'smelt_powder_asbestos'; in = 'hbm:powder_asbestos'; out = 'hbm:ingot_asbestos'; xp = '1.0' },
    @{ id = 'smelt_powder_zirconium'; in = 'hbm:powder_zirconium'; out = 'hbm:ingot_zirconium'; xp = '1.0' },
    @{ id = 'smelt_powder_tcalloy'; in = 'hbm:powder_tcalloy'; out = 'hbm:ingot_tcalloy'; xp = '1.0' },
    @{ id = 'smelt_powder_au198'; in = 'hbm:powder_au198'; out = 'hbm:ingot_au198'; xp = '1.0' },
    @{ id = 'smelt_powder_sr90'; in = 'hbm:powder_sr90'; out = 'hbm:ingot_sr90'; xp = '1.0' },
    @{ id = 'smelt_powder_ra226'; in = 'hbm:powder_ra226'; out = 'hbm:ingot_ra226'; xp = '1.0' },
    @{ id = 'smelt_powder_tantalium'; in = 'hbm:powder_tantalium'; out = 'hbm:ingot_tantalium'; xp = '1.0' },
    @{ id = 'smelt_powder_niobium'; in = 'hbm:powder_niobium'; out = 'hbm:ingot_niobium'; xp = '1.0' },
    @{ id = 'smelt_powder_bismuth'; in = 'hbm:powder_bismuth'; out = 'hbm:ingot_bismuth'; xp = '1.0' },
    @{ id = 'smelt_powder_calcium'; in = 'hbm:powder_calcium'; out = 'hbm:ingot_calcium'; xp = '1.0' },
    @{ id = 'smelt_powder_cadmium'; in = 'hbm:powder_cadmium'; out = 'hbm:ingot_cadmium'; xp = '1.0' },
    @{ id = 'smelt_crystal_rare'; in = 'hbm:crystal_rare'; out = 'hbm:powder_desh_mix'; xp = '2.0' },
    @{ id = 'smelt_crystal_osmiridium'; in = 'hbm:crystal_osmiridium'; out = 'hbm:ingot_osmiridium'; xp = '2.0' },
    @{ id = 'smelt_ingot_schraranium'; in = 'hbm:ingot_schraranium'; out = 'hbm:nugget_schrabidium'; xp = '2.0' }
)

foreach ($row in $smelts) {
    Smelt $row.id $row.in $row.out $row.xp
}

Write-Host ("Wrote overworld ores " + $owFeatures.Count + ", nether ores " + $netherFeatures.Count + ", smelting " + $smelts.Count)
