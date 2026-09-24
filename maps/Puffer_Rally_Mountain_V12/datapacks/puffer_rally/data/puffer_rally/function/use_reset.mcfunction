execute if score #phase lr_state matches 4 if entity @s[tag=lr_free] run return run function puffer_rally:reset
execute unless score #phase lr_state matches 3 run tellraw @s {"text":"全员完赛后才可重开，退出用羽毛。","color":"yellow","bold":false}
execute if score #phase lr_state matches 3 if entity @s[tag=lr_racer] run function puffer_rally:reset
