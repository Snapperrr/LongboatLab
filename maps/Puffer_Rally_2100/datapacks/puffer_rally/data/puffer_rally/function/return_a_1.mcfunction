scoreboard players set #mounted lr_tmp 0
execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1
execute if score #mounted lr_tmp matches 1 on vehicle run tp @s -24 64 216 0 0
execute if score #mounted lr_tmp matches 1 on vehicle run data merge entity @s {Motion:[0d,0d,0d]}
execute if score #mounted lr_tmp matches 0 run tp @s -24 64 216 0 0
execute if score #mounted lr_tmp matches 0 run kill @e[type=minecraft:boat,tag=lr_boat_a]
execute if score #mounted lr_tmp matches 0 run summon minecraft:boat -24 63.85 216 {Type:"oak",Tags:["lr_raceboat","lr_boat_a"],Rotation:[0f,0f],Invulnerable:0b,LongboatRig:{Segments:4,Width:1,LeftCount:8,RightCount:8,Compressed:1b,Puffers:0}}
execute if score #mounted lr_tmp matches 0 run ride @s mount @e[type=minecraft:boat,tag=lr_boat_a,limit=1,sort=nearest]
tellraw @s {"text":"已回最近检查点。救援 +10秒；未乘船时补发基础艇。若翻转未恢复可 /trigger lr_boat。","color":"yellow","bold":false}
