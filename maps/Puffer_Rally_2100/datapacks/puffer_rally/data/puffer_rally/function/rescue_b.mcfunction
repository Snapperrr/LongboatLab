scoreboard players set @s lr_rescue 0
scoreboard players add @s lr_time 200
scoreboard players add @s lr_penalty 200
execute if score @s lr_cp matches 0 run function puffer_rally:return_b_0
execute if score @s lr_cp matches 1 run function puffer_rally:return_b_1
execute if score @s lr_cp matches 2 run function puffer_rally:return_b_2
execute if score @s lr_cp matches 3 run function puffer_rally:return_b_3
execute if score @s lr_cp matches 4 run function puffer_rally:return_b_4
execute if score @s lr_cp matches 5 run function puffer_rally:return_b_5
execute if score @s lr_cp matches 6 run function puffer_rally:return_b_6
execute if score @s lr_cp matches 7 run function puffer_rally:return_b_7
execute if score @s lr_cp matches 8 run function puffer_rally:return_b_8
execute if score @s lr_cp matches 9 run function puffer_rally:return_b_9
execute if score @s lr_cp matches 10 run function puffer_rally:return_b_10
