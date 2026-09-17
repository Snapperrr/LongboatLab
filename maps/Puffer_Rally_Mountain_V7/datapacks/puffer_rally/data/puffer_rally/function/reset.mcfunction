scoreboard players add #round lr_epoch 1
scoreboard players set #phase lr_state 0
execute as @a[tag=lr_racer] run function puffer_rally:reset_player
scoreboard players set #phase lr_state 0
function puffer_rally:scene_tick
scoreboard players set #pit0 lr_pit 0
scoreboard players set #pit1 lr_pit 0
scoreboard players set #pit2 lr_pit 0
scoreboard players set #pit3 lr_pit 0
execute if loaded 17 64 73 run function puffer_rally:restock_0
execute if loaded 145 64 288 run function puffer_rally:restock_1
execute if loaded 310 64 11 run function puffer_rally:restock_2
execute if loaded 199 64 615 run function puffer_rally:restock_3
