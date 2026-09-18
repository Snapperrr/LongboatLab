scoreboard players set #inside lr_tmp 0
execute positioned 363.07 58 709.46 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 379.61 58 709.99 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 395.93 58 710.31 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 412.01 58 710.40 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 427.86 58 710.24 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 443.43 58 710.51 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 457.24 58 716.56 if entity @s[dx=58.3,dy=20,dz=58.3] run scoreboard players set #inside lr_tmp 1
execute positioned 466.26 58 728.28 if entity @s[dx=59.8,dy=20,dz=59.8] run scoreboard players set #inside lr_tmp 1
execute positioned 468.46 58 742.98 if entity @s[dx=61.5,dy=20,dz=61.5] run scoreboard players set #inside lr_tmp 1
execute positioned 467.54 58 758.07 if entity @s[dx=63.3,dy=20,dz=63.3] run scoreboard players set #inside lr_tmp 1
execute positioned 466.64 58 773.17 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 465.81 58 788.33 if entity @s[dx=66.8,dy=20,dz=66.8] run scoreboard players set #inside lr_tmp 1
execute positioned 465.57 58 793.15 if entity @s[dx=67.3,dy=20,dz=67.3] run scoreboard players set #inside lr_tmp 1
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
