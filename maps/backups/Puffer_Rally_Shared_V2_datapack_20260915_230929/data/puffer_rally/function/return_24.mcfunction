scoreboard players set #mounted lr_tmp 0
execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1
execute if score #mounted lr_tmp matches 1 on vehicle run tp @s 1260.42 64 1500.0 -90.0 0
execute if score #mounted lr_tmp matches 1 on vehicle run data merge entity @s {Motion:[0d,0d,0d],LongboatBody:{Pitch:0d,Roll:0d,PitchSpeed:0d,RollSpeed:0d}}
execute if score #mounted lr_tmp matches 0 run tp @s 1260.42 64 1500.0 -90.0 0
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_a] run kill @e[type=minecraft:boat,tag=lr_boat_a]
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_a] run summon minecraft:boat 1260.42 64 1500.0 {Type:"oak",Tags:["lr_raceboat","lr_boat_a"],Rotation:[-90.0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_a] run ride @s mount @e[type=minecraft:boat,tag=lr_boat_a,limit=1]
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_b] run kill @e[type=minecraft:boat,tag=lr_boat_b]
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_b] run summon minecraft:boat 1260.42 64 1500.0 {Type:"oak",Tags:["lr_raceboat","lr_boat_b"],Rotation:[-90.0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_b] run ride @s mount @e[type=minecraft:boat,tag=lr_boat_b,limit=1]
