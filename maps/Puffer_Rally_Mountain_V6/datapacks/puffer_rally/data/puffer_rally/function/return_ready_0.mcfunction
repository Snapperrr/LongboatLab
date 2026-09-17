execute unless entity @s[tag=lr_rescue_pending,tag=lr_racer] run return 0
execute if entity @s[tag=lr_replace_pending] run ride @s dismount
scoreboard players add @s lr_time 200
execute if entity @s[tag=lr_replace_pending] run scoreboard players add @s lr_time 200
scoreboard players set #mounted lr_tmp 0
execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1
execute if score #mounted lr_tmp matches 1 on vehicle run longboatlab race_move 0.0 64 4.0 0.0
execute if score #mounted lr_tmp matches 0 run tp @s 0.0 64 4.0 0.0 0
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_a] run kill @e[type=minecraft:boat,tag=lr_boat_a]
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_a] run summon minecraft:boat 0.0 64 4.0 {Type:"oak",Tags:["lr_raceboat","lr_boat_a"],Rotation:[0.0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_a] run ride @s mount @e[type=minecraft:boat,tag=lr_boat_a,limit=1]
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_b] run kill @e[type=minecraft:boat,tag=lr_boat_b]
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_b] run summon minecraft:boat 0.0 64 4.0 {Type:"oak",Tags:["lr_raceboat","lr_boat_b"],Rotation:[0.0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_b] run ride @s mount @e[type=minecraft:boat,tag=lr_boat_b,limit=1]
execute unless entity @s[tag=lr_replace_pending] run tellraw @s {"text":"已回到检查点，保留船只改装；罚时10秒。","color":"yellow","bold":false}
execute if entity @s[tag=lr_replace_pending] run tellraw @s {"text":"已更换基础船并返回检查点；罚时20秒。","color":"yellow","bold":false}
