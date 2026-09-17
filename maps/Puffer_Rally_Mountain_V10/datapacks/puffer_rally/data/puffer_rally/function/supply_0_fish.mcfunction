execute unless score #phase lr_state matches 2 run return 0
execute unless entity @s[tag=lr_racer,scores={lr_place=0}] run return 0
execute positioned 27 64 65 unless entity @s[distance=..8] run return 0
execute if score @s lr_p0_fish = #round lr_epoch run return 0
scoreboard players set #granted lr_tmp 0
execute store result score #granted lr_tmp run longboatlab race_supply fish
execute unless score #granted lr_tmp matches 1 run return 0
scoreboard players operation @s lr_p0_fish = #round lr_epoch
tellraw @s {"text":"河豚 ×4 · 已领取","color":"aqua","bold":false}
