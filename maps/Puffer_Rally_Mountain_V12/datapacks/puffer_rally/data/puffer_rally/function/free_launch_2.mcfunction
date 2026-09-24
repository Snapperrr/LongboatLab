tp @s -15 65 -17 0 0
execute positioned -15 65 -17 run summon minecraft:boat ~ ~ ~ {Type:"oak",Tags:["lr_freeboat","lr_free_spawn"],Rotation:[0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
execute positioned -15 65 -17 run ride @s mount @e[type=minecraft:boat,tag=lr_free_spawn,distance=..1,limit=1,sort=nearest]
execute on vehicle run scoreboard players operation @s lr_epoch = #round lr_epoch
tag @e[tag=lr_free_spawn] remove lr_free_spawn
