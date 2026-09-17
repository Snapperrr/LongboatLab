ride @s dismount
kill @e[type=minecraft:boat,tag=lr_boat_b]
execute positioned 24 64 -20 run summon minecraft:boat 24 63.85 -20 {Type:"oak",Tags:["lr_raceboat","lr_boat_b"],Rotation:[0f,0f],Invulnerable:0b,LongboatRig:{Segments:4,Width:1,LeftCount:8,RightCount:8,Compressed:1b,Puffers:0}}
tp @s 24 64 -20 0 0
ride @s mount @e[type=minecraft:boat,tag=lr_boat_b,limit=1,sort=nearest]
gamemode survival @s
effect give @s minecraft:resistance infinite 4 true
effect give @s minecraft:saturation infinite 0 true
effect give @s minecraft:water_breathing infinite 0 true
give @s minecraft:mace
give @s minecraft:cooked_beef 16
spawnpoint @s 24 66 -70
tellraw @s {"text":"基础艇：4 节、左右各 8 桨。比赛装备自由使用；请勿拆赛道抄近路。","color":"aqua","bold":false}
scoreboard players set @s lr_pit1 0
scoreboard players set @s lr_pit2 0
scoreboard players set @s lr_pit3 0
scoreboard players set @s lr_pit4 0
