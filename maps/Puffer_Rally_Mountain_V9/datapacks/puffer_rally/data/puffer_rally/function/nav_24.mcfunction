scoreboard players set #inside lr_tmp 0
execute positioned 599.49 58 806.62 if entity @s[dx=59.4,dy=20,dz=59.4] run scoreboard players set #inside lr_tmp 1
execute positioned 598.64 58 789.78 if entity @s[dx=61.1,dy=20,dz=61.1] run scoreboard players set #inside lr_tmp 1
execute positioned 597.74 58 772.87 if entity @s[dx=62.9,dy=20,dz=62.9] run scoreboard players set #inside lr_tmp 1
execute positioned 596.82 58 755.96 if entity @s[dx=64.8,dy=20,dz=64.8] run scoreboard players set #inside lr_tmp 1
execute positioned 595.97 58 739.11 if entity @s[dx=66.5,dy=20,dz=66.5] run scoreboard players set #inside lr_tmp 1
execute positioned 595.24 58 722.38 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 594.68 58 705.81 if entity @s[dx=69.0,dy=20,dz=69.0] run scoreboard players set #inside lr_tmp 1
execute positioned 594.37 58 692.43 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 499.20 64 826.78 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 501.36 64 836.30 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 507.38 64 844.23 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 516.57 64 850.58 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 528.22 64 855.34 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 541.65 64 858.51 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 556.15 64 860.10 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 571.02 64 860.12 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 585.58 64 858.56 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 599.12 64 855.42 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 610.94 64 850.72 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 620.36 64 844.44 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 626.67 64 836.60 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 629.18 64 827.19 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
