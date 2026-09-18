scoreboard players set #inside lr_tmp 0
execute positioned 193.59 58 78.81 if entity @s[dx=70.4,dy=20,dz=70.4] run scoreboard players set #inside lr_tmp 1
execute positioned 193.22 58 62.44 if entity @s[dx=71.2,dy=20,dz=71.2] run scoreboard players set #inside lr_tmp 1
execute positioned 193.13 58 45.94 if entity @s[dx=72.2,dy=20,dz=72.2] run scoreboard players set #inside lr_tmp 1
execute positioned 197.31 58 30.12 if entity @s[dx=73.5,dy=20,dz=73.5] run scoreboard players set #inside lr_tmp 1
execute positioned 206.20 58 16.71 if entity @s[dx=75.0,dy=20,dz=75.0] run scoreboard players set #inside lr_tmp 1
execute positioned 218.73 58 7.19 if entity @s[dx=76.5,dy=20,dz=76.5] run scoreboard players set #inside lr_tmp 1
execute positioned 233.38 58 2.62 if entity @s[dx=78.2,dy=20,dz=78.2] run scoreboard players set #inside lr_tmp 1
execute positioned 248.57 58 1.70 if entity @s[dx=79.8,dy=20,dz=79.8] run scoreboard players set #inside lr_tmp 1
execute positioned 263.79 58 0.92 if entity @s[dx=81.4,dy=20,dz=81.4] run scoreboard players set #inside lr_tmp 1
execute positioned 279.07 58 0.21 if entity @s[dx=82.8,dy=20,dz=82.8] run scoreboard players set #inside lr_tmp 1
execute positioned 294.45 58 -0.41 if entity @s[dx=84.0,dy=20,dz=84.0] run scoreboard players set #inside lr_tmp 1
execute positioned 309.96 58 -0.91 if entity @s[dx=85.0,dy=20,dz=85.0] run scoreboard players set #inside lr_tmp 1
execute positioned 325.62 58 -1.24 if entity @s[dx=85.7,dy=20,dz=85.7] run scoreboard players set #inside lr_tmp 1
execute positioned 327.98 58 -1.28 if entity @s[dx=85.8,dy=20,dz=85.8] run scoreboard players set #inside lr_tmp 1
execute positioned 310 65 8 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
