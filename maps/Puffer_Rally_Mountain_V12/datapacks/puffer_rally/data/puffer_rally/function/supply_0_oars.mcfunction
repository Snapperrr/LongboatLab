execute if entity @s[tag=lr_free] unless score #phase lr_state matches 4 run return 0
execute if entity @s[tag=lr_free,scores={lr_fstock=1..}] run return 0
execute unless score #phase lr_state matches 2 unless entity @s[tag=lr_free] run return 0
execute unless entity @s[tag=lr_racer,scores={lr_place=0}] run return 0
execute positioned 26 64 73 unless entity @s[distance=..8] run return 0
execute unless entity @s[tag=lr_free] if score @s lr_p0_oars = #round lr_epoch run return 0
scoreboard players set #granted lr_tmp 0
execute store result score #granted lr_tmp run longboatlab race_supply oars
execute unless score #granted lr_tmp matches 1 run return 0
scoreboard players operation @s lr_p0_oars = #round lr_epoch
tellraw @s {"text":"木铲 ×6 · 已领取","color":"aqua","bold":false}
execute if entity @s[tag=lr_free] run scoreboard players set @s lr_fstock 20
