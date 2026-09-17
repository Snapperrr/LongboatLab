scoreboard players set @s lr_boat 0
scoreboard players add @s lr_time 200
scoreboard players add @s lr_penalty 200
ride @s dismount
kill @e[type=minecraft:boat,tag=lr_boat_a]
function puffer_rally:rescue_a
