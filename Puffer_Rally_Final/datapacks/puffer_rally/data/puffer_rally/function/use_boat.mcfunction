execute if score #phase lr_state matches 4 if entity @s[tag=lr_free] run return run function puffer_rally:free_replace
execute if score #phase lr_state matches 2 if entity @s[tag=lr_racer,scores={lr_place=0}] run function puffer_rally:replace
execute unless score #phase lr_state matches 2 run tellraw @s {"text":"救援道具在比赛进行中使用。","color":"yellow","bold":false}
