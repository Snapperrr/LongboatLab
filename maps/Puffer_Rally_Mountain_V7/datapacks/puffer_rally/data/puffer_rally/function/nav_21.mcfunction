scoreboard players set #inside lr_tmp 0
execute positioned 227.13 58 703.51 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 243.47 58 703.86 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 260.03 58 704.41 if entity @s[dx=68.0,dy=20,dz=68.0] run scoreboard players set #inside lr_tmp 1
execute positioned 276.75 58 705.14 if entity @s[dx=66.5,dy=20,dz=66.5] run scoreboard players set #inside lr_tmp 1
execute positioned 293.60 58 705.98 if entity @s[dx=64.8,dy=20,dz=64.8] run scoreboard players set #inside lr_tmp 1
execute positioned 310.51 58 706.89 if entity @s[dx=63.0,dy=20,dz=63.0] run scoreboard players set #inside lr_tmp 1
execute positioned 327.42 58 707.80 if entity @s[dx=61.2,dy=20,dz=61.2] run scoreboard players set #inside lr_tmp 1
execute positioned 344.27 58 708.65 if entity @s[dx=59.5,dy=20,dz=59.5] run scoreboard players set #inside lr_tmp 1
execute positioned 360.99 58 709.38 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 377.55 58 709.94 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 393.90 58 710.28 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 410.02 58 710.40 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 425.89 58 710.28 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 441.52 58 710.24 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 443.43 58 710.51 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
