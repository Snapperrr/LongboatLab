scoreboard players set #phase lr_state 1
scoreboard players set #count lr_state 200
scoreboard players set #rank lr_place 0
scoreboard players set @a[tag=lr_racer] lr_time 0
scoreboard players set @a[tag=lr_racer] lr_cp 0
scoreboard players set @a[tag=lr_racer] lr_place 0
scoreboard players set #pit0 lr_pit 0
scoreboard players set #pit1 lr_pit 0
scoreboard players set #pit2 lr_pit 0
scoreboard players set #pit3 lr_pit 0
execute as @a[tag=lr_a] at @s run function puffer_rally:start_a
execute as @a[tag=lr_b] at @s run function puffer_rally:start_b
title @a[tag=lr_racer] times 0 24 0
title @a[tag=lr_racer] title {"text":"10","color":"yellow","bold":false}
