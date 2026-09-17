execute unless score #phase lr_state matches 2 run return 0
execute unless entity @s[tag=lr_racer,scores={lr_place=0}] run return 0
execute positioned 145 64 278 unless entity @s[distance=..8] run return 0
execute if score @s lr_p1_oars = #round lr_epoch run return 0
scoreboard players set #granted lr_tmp 0
execute store result score #granted lr_tmp run longboatlab race_supply oars
execute unless score #granted lr_tmp matches 1 run return 0
scoreboard players operation @s lr_p1_oars = #round lr_epoch
tellraw @s {"text":"木铲 ×6 · 已领取","color":"aqua","bold":false}
