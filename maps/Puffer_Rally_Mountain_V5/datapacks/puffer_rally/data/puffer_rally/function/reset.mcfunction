scoreboard players add #round lr_epoch 1
scoreboard players set #phase lr_state 0
execute as @a[tag=lr_racer] run function puffer_rally:reset_player
scoreboard players set #phase lr_state 0
function puffer_rally:scene_tick
scoreboard players set #pit0 lr_pit 0
scoreboard players set #pit1 lr_pit 0
scoreboard players set #pit2 lr_pit 0
scoreboard players set #pit3 lr_pit 0
execute if loaded 17 64 140 run function puffer_rally:restock_0
execute if loaded 279 64 576 run function puffer_rally:restock_1
execute if loaded 596 64 49 run function puffer_rally:restock_2
execute if loaded 353 64 1182 run function puffer_rally:restock_3
