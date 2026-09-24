execute unless entity @s[tag=lr_rescue_pending,tag=lr_free,tag=lr_racer] run return 0
execute if entity @s[tag=lr_replace_pending] run ride @s dismount
scoreboard players set #mounted lr_tmp 0
execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1
execute if score #mounted lr_tmp matches 1 on vehicle run longboatlab race_move 0.0 64 4.0 0.0
execute if score #mounted lr_tmp matches 0 run tp @s 0.0 64 4.0 0.0 0
execute if score #mounted lr_tmp matches 0 positioned 0.0 64 4.0 run summon minecraft:boat ~ ~ ~ {Type:"oak",Tags:["lr_freeboat","lr_free_spawn"],Rotation:[0.0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
execute if score #mounted lr_tmp matches 0 positioned 0.0 64 4.0 run ride @s mount @e[type=minecraft:boat,tag=lr_free_spawn,distance=..1,limit=1,sort=nearest]
execute on vehicle run scoreboard players operation @s lr_epoch = #round lr_epoch
tag @e[tag=lr_free_spawn] remove lr_free_spawn
tellraw @s {"text": "已返回起点水域。", "color": "aqua"}
