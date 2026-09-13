$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$out = Join-Path $root "src/main/resources/data/hbm/machine_recipes/purex.json"
$utf8 = New-Object System.Text.UTF8Encoding $false

$recipes = New-Object System.Collections.Generic.List[object]

function Item([string]$id, [int]$count = 1, [string]$meta = $null, $chance = $null) {
  $o = [ordered]@{ item = $id; count = $count }
  if ($meta) { $o.meta = $meta }
  if ($null -ne $chance) { $o.chance = [double]$chance }
  return $o
}
function Ore([string]$ore, [int]$count = 1) {
  return [ordered]@{ ore = $ore; count = $count }
}
function Fluid([string]$id, [int]$amount) {
  return [ordered]@{ fluid = $id; amount = $amount }
}
function Add-Recipe {
  param(
    [string]$Name,
    [int]$Duration,
    [long]$Power,
    [string]$Group = $null,
    $InItems = $null,
    $InFluids = $null,
    $OutItems = $null,
    $OutFluids = $null
  )
  $r = [ordered]@{ name = $Name; duration = $Duration; power = $Power }
  if ($Group) { $r.autoSwitchGroup = $Group }
  if ($InItems) { $r.inputItem = @($InItems) }
  if ($InFluids) { $r.inputFluid = @($InFluids) }
  if ($OutItems) { $r.outputItem = @($OutItems) }
  if ($OutFluids) { $r.outputFluid = @($OutFluids) }
  [void]$recipes.Add($r)
}

$pile = 100
$zirnox = 1000
$plate = 1500
$pwr = 2500
$watz = 10000
$vit = 1000
$keroNitric = @( (Fluid "hbm:kerosene" 500), (Fluid "hbm:nitric_acid" 250) )
$watzFluid = Fluid "hbm:watz" 1000

Add-Recipe -Name "purex.uzh" -Duration 600 -Power 1000 `
  -InItems @( (Item "hbm:billet_uranium_fuel"), (Ore "billetZirconium" 3) ) `
  -InFluids @( (Fluid "hbm:nitric_acid" 1000), (Fluid "hbm:hydrogen" 4000) ) `
  -OutItems @( (Item "hbm:billet_uzh" 4) )

Add-Recipe -Name "purex.flashgold" -Duration 600 -Power 1000 `
  -InItems @( (Ore "billetGold198"), (Item "hbm:pellet_charged") ) `
  -InFluids @( (Fluid "hbm:amat" 1000) ) `
  -OutItems @( (Item "hbm:billet_balefire_gold" 2) )

Add-Recipe -Name "purex.flashlead" -Duration 600 -Power 1000 `
  -InItems @( (Ore "billetLead209"), (Item "hbm:billet_balefire_gold") ) `
  -InFluids @( (Fluid "hbm:amat" 1000) ) `
  -OutItems @( (Item "hbm:billet_flashlead") )

Add-Recipe -Name "purex.pilepu" -Duration 40 -Power $pile -Group "autoswitch.pile" `
  -InItems @( (Item "hbm:pile_rod_plutonium") ) `
  -InFluids @( (Fluid "hbm:sulfuric_acid" 100) ) `
  -OutItems @( (Item "hbm:billet_pu_mix" 2), (Item "hbm:billet_uranium"), (Item "hbm:plate_iron" 2) )

Add-Recipe -Name "purex.pilepu239" -Duration 40 -Power $pile -Group "autoswitch.pile" `
  -InItems @( (Item "hbm:pile_rod_pu239") ) `
  -InFluids @( (Fluid "hbm:sulfuric_acid" 100) ) `
  -OutItems @( (Item "hbm:billet_pu239"), (Item "hbm:billet_pu_mix"), (Item "hbm:billet_uranium"), (Item "hbm:plate_iron" 2) )

Add-Recipe -Name "purex.zirnoxnu" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_natural_uranium") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_u238"), (Item "hbm:nugget_pu_mix" 2), (Item "hbm:nugget_pu239"), (Item "hbm:nuclear_waste_tiny" 2) )

Add-Recipe -Name "purex.zirnoxmeu" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_uranium") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_pu_mix"), (Item "hbm:nugget_plutonium" 2), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 2) )

Add-Recipe -Name "purex.zirnoxthmeu" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_thorium") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_u238"), (Item "hbm:nugget_th232"), (Item "hbm:nugget_u233" 2), (Item "hbm:nuclear_waste_tiny" 2) )

Add-Recipe -Name "purex.zirnoxmox" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_mox") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_pu_mix"), (Item "hbm:nugget_technetium"), (Item "hbm:nugget_u238"), (Item "hbm:nuclear_waste_tiny" 3) )

Add-Recipe -Name "purex.zirnoxmep" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_plutonium") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_pu_mix" 2), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 3) )

Add-Recipe -Name "purex.zirnoxheu233" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_u233") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_u235"), (Item "hbm:nugget_neptunium"), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 3) )

Add-Recipe -Name "purex.zirnoxheu235" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_u235") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_pu238"), (Item "hbm:nugget_neptunium"), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 3) )

Add-Recipe -Name "purex.zirnoxles" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_schrabidium") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_beryllium" 2), (Item "hbm:nugget_pu239"), (Item "hbm:nuclear_waste_tiny"), (Item "hbm:nuclear_waste_tiny" 2) )

Add-Recipe -Name "purex.zirnoxzfbmox" -Duration 100 -Power $zirnox -Group "autoswitch.zirnox" `
  -InItems @( (Item "hbm:waste_zfb_mox") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_zirconium" 3), (Item "hbm:nugget_technetium"), (Item "hbm:nugget_pu_mix"), (Item "hbm:nuclear_waste_tiny") )

Add-Recipe -Name "purex.platemox" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_mox") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:powder_sr90_tiny"), (Item "hbm:nugget_pu_mix" 3), (Item "hbm:powder_cs137_tiny"), (Item "hbm:nuclear_waste_tiny" 4) )

Add-Recipe -Name "purex.platepu238be" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_pu238be") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_beryllium"), (Item "hbm:nugget_pu238"), (Item "hbm:powder_coal_tiny" 2), (Item "hbm:nugget_lead" 2) )

Add-Recipe -Name "purex.platepu239" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_pu239") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_pu240" 2), (Item "hbm:nugget_technetium"), (Item "hbm:powder_cs137_tiny"), (Item "hbm:nuclear_waste_tiny" 5) )

Add-Recipe -Name "purex.platera226be" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_ra226be") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_beryllium" 2), (Item "hbm:nugget_polonium" 2), (Item "hbm:powder_coal_tiny"), (Item "hbm:nugget_lead") )

Add-Recipe -Name "purex.platesa326" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_sa326") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_solinium"), (Item "hbm:powder_neodymium_tiny"), (Item "hbm:nugget_tantalium"), (Item "hbm:nuclear_waste_tiny" 6) )

Add-Recipe -Name "purex.plateu233" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_u233") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_u235"), (Item "hbm:powder_i131_tiny"), (Item "hbm:powder_sr90_tiny"), (Item "hbm:nuclear_waste_tiny" 6) )

Add-Recipe -Name "purex.plateu235" -Duration 100 -Power $plate -Group "autoswitch.plate" `
  -InItems @( (Item "hbm:waste_plate_u235") ) -InFluids $keroNitric `
  -OutItems @( (Item "hbm:nugget_neptunium"), (Item "hbm:nugget_pu238"), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 6) )

function Add-Pwr([string]$name, [string]$meta, $outs) {
  Add-Recipe -Name $name -Duration 100 -Power $pwr -Group "autoswitch.pwr" `
    -InItems @( (Item "hbm:pwr_fuel_depleted" 1 $meta) ) -InFluids $keroNitric -OutItems $outs
}

Add-Pwr "purex.pwrmeu" "MEU" @( (Item "hbm:nugget_u238" 3), (Item "hbm:nugget_plutonium" 4), (Item "hbm:nugget_technetium" 2), (Item "hbm:nuclear_waste_tiny" 3) )
Add-Pwr "purex.pwrheu233" "HEU233" @( (Item "hbm:nugget_u235" 3), (Item "hbm:nugget_pu238" 3), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 5) )
Add-Pwr "purex.pwrheu235" "HEU235" @( (Item "hbm:nugget_neptunium" 3), (Item "hbm:nugget_pu238" 3), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 5) )
Add-Pwr "purex.pwrmen" "MEN" @( (Item "hbm:nugget_u238" 3), (Item "hbm:nugget_pu239" 4), (Item "hbm:nugget_technetium" 2), (Item "hbm:nuclear_waste_tiny" 3) )
Add-Pwr "purex.pwrhen237" "HEN237" @( (Item "hbm:nugget_pu238" 2), (Item "hbm:nugget_pu239" 4), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 5) )
Add-Pwr "purex.pwrmox" "MOX" @( (Item "hbm:nugget_u238" 3), (Item "hbm:nugget_pu240" 4), (Item "hbm:nugget_technetium" 2), (Item "hbm:nuclear_waste_tiny" 3) )
Add-Pwr "purex.pwrmep" "MEP" @( (Item "hbm:nugget_lead" 2), (Item "hbm:nugget_pu_mix" 4), (Item "hbm:nugget_technetium" 2), (Item "hbm:nuclear_waste_tiny" 3) )
Add-Pwr "purex.pwrhep239" "HEP239" @( (Item "hbm:nugget_pu_mix" 2), (Item "hbm:nugget_pu240" 4), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 5) )
Add-Pwr "purex.pwrhep241" "HEP241" @( (Item "hbm:nugget_lead" 3), (Item "hbm:nugget_zirconium" 2), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 6) )
Add-Pwr "purex.pwrmea" "MEA" @( (Item "hbm:nugget_lead" 3), (Item "hbm:nugget_zirconium" 2), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 6) )
Add-Pwr "purex.pwrhea242" "HEA242" @( (Item "hbm:nugget_lead" 3), (Item "hbm:nugget_zirconium" 2), (Item "hbm:nugget_technetium"), (Item "hbm:nuclear_waste_tiny" 6) )
Add-Pwr "purex.pwrhes326" "HES326" @( (Item "hbm:nugget_solinium" 3), (Item "hbm:nugget_lead" 2), (Item "hbm:nugget_euphemium"), (Item "hbm:nuclear_waste_tiny" 6) )
Add-Pwr "purex.pwrhes327" "HES327" @( (Item "hbm:nugget_australium" 4), (Item "hbm:nugget_lead"), (Item "hbm:nugget_euphemium"), (Item "hbm:nuclear_waste_tiny" 6) )
Add-Pwr "purex.pwrbfbam" "BFB_AM_MIX" @( (Item "hbm:nugget_am_mix" 9), (Item "hbm:nugget_pu_mix" 2), (Item "hbm:nugget_bismuth" 6), (Item "hbm:nuclear_waste_tiny") )
Add-Pwr "purex.pwrbfpu241" "BFB_PU241" @( (Item "hbm:nugget_pu241" 9), (Item "hbm:nugget_pu_mix" 2), (Item "hbm:nugget_bismuth" 6), (Item "hbm:nuclear_waste_tiny") )

Add-Recipe -Name "purex.thoriumsalt" -Duration 20 -Power 10000 `
  -InItems @( (Ore "nuggetThorium232" 2) ) `
  -InFluids @( (Fluid "hbm:thorium_salt_depleted" 16000) ) `
  -OutItems @( (Item "hbm:nugget_u233" 1 $null 0.5), (Item "hbm:nuclear_waste_tiny" 1 $null 0.25) ) `
  -OutFluids @( (Fluid "hbm:thorium_salt" 16000) )

function Add-Watz([string]$name, [string]$meta, $outs) {
  Add-Recipe -Name $name -Duration 60 -Power $watz -Group "autoswitch.watz" `
    -InItems @( (Item "hbm:watz_pellet_depleted" 1 $meta) ) -InFluids $keroNitric `
    -OutItems $outs -OutFluids @( $watzFluid )
}

Add-Watz "purex.watzschrab" "SCHRABIDIUM" @( (Item "hbm:nugget_solinium" 15), (Item "hbm:nugget_euphemium" 3), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzhes" "HES" @( (Item "hbm:nugget_solinium" 17), (Item "hbm:nugget_euphemium"), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzmes" "MES" @( (Item "hbm:nugget_solinium" 12), (Item "hbm:nugget_tantalium" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzles" "LES" @( (Item "hbm:nugget_solinium" 9), (Item "hbm:nugget_tantalium" 9), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzhen" "HEN" @( (Item "hbm:nugget_pu239" 12), (Item "hbm:nugget_technetium" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzmeu" "MEU" @( (Item "hbm:nugget_pu239" 12), (Item "hbm:nugget_bismuth" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzmep" "MEP" @( (Item "hbm:nugget_pu241" 12), (Item "hbm:nugget_bismuth" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzlead" "LEAD" @( (Item "hbm:nugget_lead" 6), (Item "hbm:nugget_bismuth" 12), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzboron" "BORON" @( (Item "hbm:powder_coal_tiny" 12), (Item "hbm:nugget_co60" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watzdu" "DU" @( (Item "hbm:nugget_polonium" 12), (Item "hbm:nugget_pu238" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watznaqadah" "NQD" @( (Ore "nuggetNaquadria" 12), (Item "hbm:nugget_euphemium" 6), (Item "hbm:nuclear_waste" 2) )
Add-Watz "purex.watznaqadria" "NQR" @( (Item "hbm:nugget_co60" 12), (Item "hbm:nugget_euphemium" 6), (Item "hbm:nuclear_waste" 2) )

Add-Recipe -Name "purex.icf" -Duration 300 -Power 10000 `
  -InItems @( (Item "hbm:icf_pellet_depleted") ) `
  -OutItems @( (Item "hbm:icf_pellet_empty"), (Item "hbm:pellet_charged"), (Item "hbm:powder_iron") ) `
  -OutFluids @( (Fluid "hbm:helium4" 1250) )

Add-Recipe -Name "purex.vitliquid" -Duration 100 -Power $vit `
  -InItems @( (Item "hbm:sand_mix" 1 "LEAD") ) `
  -InFluids @( (Fluid "hbm:wastefluid" 1000) ) `
  -OutItems @( (Item "hbm:nuclear_waste_vitrified") )

Add-Recipe -Name "purex.vitgaseous" -Duration 100 -Power $vit `
  -InItems @( (Item "hbm:sand_mix" 1 "LEAD") ) `
  -InFluids @( (Fluid "hbm:wastegas" 1000) ) `
  -OutItems @( (Item "hbm:nuclear_waste_vitrified") )

Add-Recipe -Name "purex.vitsolid" -Duration 300 -Power $vit `
  -InItems @( (Item "hbm:sand_mix" 1 "LEAD"), (Item "hbm:nuclear_waste" 4) ) `
  -OutItems @( (Item "hbm:nuclear_waste_vitrified" 4) )

Add-Recipe -Name "purex.schraranium" -Duration 200 -Power 1000 `
  -InItems @( (Item "hbm:ingot_schraranium") ) `
  -InFluids @( (Fluid "hbm:kerosene" 2000), (Fluid "hbm:nitric_acid" 1000) ) `
  -OutItems @( (Item "hbm:nugget_schrabidium" 3), (Item "hbm:nugget_uranium" 3), (Item "hbm:nugget_neptunium" 2) )

$schrabFluids = @( (Fluid "hbm:solvent" 4000), (Fluid "hbm:schrabidic" 250) )
$schrabOut = @( (Item "hbm:powder_schrabidium"), (Item "hbm:nugget_technetium" 3), (Item "hbm:nuclear_waste_tiny" 4) )
Add-Recipe -Name "purex.schrabzirnox" -Duration 200 -Power 50000 -Group "autoswitch.schrab" `
  -InItems @( (Item "hbm:waste_plutonium") ) -InFluids $schrabFluids -OutItems $schrabOut
Add-Recipe -Name "purex.schrabpwr" -Duration 200 -Power 50000 -Group "autoswitch.schrab" `
  -InItems @( (Item "hbm:pwr_fuel_depleted" 1 "MEP") ) -InFluids $schrabFluids -OutItems $schrabOut
Add-Recipe -Name "purex.schrabmen" -Duration 200 -Power 50000 -Group "autoswitch.schrab" `
  -InItems @( (Item "hbm:pwr_fuel_depleted" 1 "MEN") ) -InFluids $schrabFluids -OutItems $schrabOut

$rootObj = [ordered]@{
  source = "legacy-1.7.10 PUREXRecipes.registerDefaults"
  recipes = $recipes
}
$json = $rootObj | ConvertTo-Json -Depth 8
# Windows PowerShell emits a BOM-prone default encoding; write UTF-8 no BOM.
[System.IO.File]::WriteAllText($out, $json + "`n", $utf8)
Write-Host "wrote $($recipes.Count) PUREX recipes"
