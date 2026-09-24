execute if entity @s[tag=lr_free] run return run tellraw @s {"text": "自由练习中 · 指南针返航，羽毛退出。", "color": "aqua"}
execute unless score #phase lr_state matches 0 unless score #phase lr_state matches 4 run return run tellraw @s {"text": "先用羽毛退出比赛，再进入自由练习。", "color": "yellow"}
tag @s add lr_free_joining
execute if score #phase lr_state matches 0 if entity @a[tag=lr_racer,tag=!lr_free_joining] run return run function puffer_rally:free_busy
tag @s remove lr_free_joining
scoreboard players set #free_slot lr_tmp -1
execute if score #free_slot lr_tmp matches -1 positioned -5 65 -17 unless entity @e[type=minecraft:boat,distance=..3] run scoreboard players set #free_slot lr_tmp 0
execute if score #free_slot lr_tmp matches -1 positioned 5 65 -17 unless entity @e[type=minecraft:boat,distance=..3] run scoreboard players set #free_slot lr_tmp 1
execute if score #free_slot lr_tmp matches -1 positioned -15 65 -17 unless entity @e[type=minecraft:boat,distance=..3] run scoreboard players set #free_slot lr_tmp 2
execute if score #free_slot lr_tmp matches -1 positioned 15 65 -17 unless entity @e[type=minecraft:boat,distance=..3] run scoreboard players set #free_slot lr_tmp 3
execute if score #free_slot lr_tmp matches -1 positioned 0 65 -17 unless entity @e[type=minecraft:boat,distance=..3] run scoreboard players set #free_slot lr_tmp 4
execute if score #free_slot lr_tmp matches -1 run return run tellraw @s {"text": "发船区已满，请先把岸上的船开走。", "color": "yellow"}
function puffer_rally:free_start
