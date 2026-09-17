execute as @a[tag=lr_racer] run ride @s dismount
kill @e[type=minecraft:boat,tag=lr_raceboat]
execute as @a[tag=lr_racer] run function puffer_rally:leave
scoreboard players set #phase lr_state 0
tellraw @a {"text":"赛道已待命。重新站到准备色块开始下一轮。","color":"aqua","bold":false}
