scoreboard players add @s lr_time 200
execute if score @s lr_cp matches 0 run function puffer_rally:return_0
execute if score @s lr_cp matches 1 run function puffer_rally:return_1
execute if score @s lr_cp matches 2 run function puffer_rally:return_2
execute if score @s lr_cp matches 3 run function puffer_rally:return_3
execute if score @s lr_cp matches 4 run function puffer_rally:return_4
execute if score @s lr_cp matches 5 run function puffer_rally:return_5
execute if score @s lr_cp matches 6 run function puffer_rally:return_6
execute if score @s lr_cp matches 7 run function puffer_rally:return_7
execute if score @s lr_cp matches 8 run function puffer_rally:return_8
execute if score @s lr_cp matches 9 run function puffer_rally:return_9
execute if score @s lr_cp matches 10 run function puffer_rally:return_10
execute if score @s lr_cp matches 11 run function puffer_rally:return_11
execute if score @s lr_cp matches 12 run function puffer_rally:return_12
execute if score @s lr_cp matches 13 run function puffer_rally:return_13
execute if score @s lr_cp matches 14 run function puffer_rally:return_14
execute if score @s lr_cp matches 15 run function puffer_rally:return_15
execute if score @s lr_cp matches 16 run function puffer_rally:return_16
execute if score @s lr_cp matches 17 run function puffer_rally:return_17
execute if score @s lr_cp matches 18 run function puffer_rally:return_18
execute if score @s lr_cp matches 19 run function puffer_rally:return_19
execute if score @s lr_cp matches 20 run function puffer_rally:return_20
execute if score @s lr_cp matches 21 run function puffer_rally:return_21
execute if score @s lr_cp matches 22 run function puffer_rally:return_22
execute if score @s lr_cp matches 23 run function puffer_rally:return_23
tellraw @s {"text":"已回最近检查点，保留乘坐船改装；罚时10秒。未乘船则补发基础船。","color":"yellow","bold":false}
