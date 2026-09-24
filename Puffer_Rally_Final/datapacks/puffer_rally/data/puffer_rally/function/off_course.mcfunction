execute if score #phase lr_state matches 4 run return 0
scoreboard players set @s lr_warn 6
title @s actionbar {"text":"偏航 · 沿黄色箭头返回","color":"yellow","bold":false}
playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.7 0.8
