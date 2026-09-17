scoreboard players set #phase lr_state 1
scoreboard players set #count lr_state 100
scoreboard players set #rank lr_place 0
scoreboard players set @a[tag=lr_racer] lr_time 0
scoreboard players set @a[tag=lr_racer] lr_place 0
scoreboard players set @a[tag=lr_racer] lr_cp 0
scoreboard players set @a[tag=lr_racer] lr_penalty 0
execute as @a[tag=lr_a] run function puffer_rally:start_a
execute as @a[tag=lr_b] run function puffer_rally:start_b
scoreboard players set @a[tag=lr_racer] lr_rescue 0
scoreboard players set @a[tag=lr_racer] lr_boat 0
scoreboard players set @a[tag=lr_racer] lr_practice 0
scoreboard players set @a[tag=lr_racer] lr_leave 0
scoreboard players set @a[tag=lr_racer] lr_reset 0
scoreboard players set @a[tag=lr_racer] lr_help 0
title @a[tag=lr_racer] times 0 24 0
tellraw @a[tag=lr_racer] {"text":"准备！两条路线对称，按顺序通过拱门，补给可选择不停。","color":"yellow","bold":false}
