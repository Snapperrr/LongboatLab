ride @s dismount
kill @e[type=minecraft:boat,tag=lr_boat_a]
summon minecraft:boat -5 64 4 {Type:"oak",Tags:["lr_raceboat","lr_boat_a"],Rotation:[0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}
tp @s -5 64 4 0 0
ride @s mount @e[type=minecraft:boat,tag=lr_boat_a,limit=1]
gamemode survival @s
effect give @s minecraft:resistance infinite 4 true
effect give @s minecraft:saturation infinite 0 true
effect give @s minecraft:water_breathing infinite 0 true
spawnpoint @s 0 65 -32
function puffer_rally:inventory
scoreboard players operation @s lr_epoch = #round lr_epoch
tellraw @s {"text":"单格船起航：2个河豚桶、4把木铲、1把重锤；首格空手。补给每人独立领取。","color":"aqua","bold":false}
