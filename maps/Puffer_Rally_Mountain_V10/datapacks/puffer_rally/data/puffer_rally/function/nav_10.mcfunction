scoreboard players set #inside lr_tmp 0
execute positioned 250.85 58 1.58 if entity @s[dx=80.0,dy=20,dz=80.0] run scoreboard players set #inside lr_tmp 1
execute positioned 266.08 58 0.81 if entity @s[dx=81.6,dy=20,dz=81.6] run scoreboard players set #inside lr_tmp 1
execute positioned 281.37 58 0.11 if entity @s[dx=83.0,dy=20,dz=83.0] run scoreboard players set #inside lr_tmp 1
execute positioned 296.77 58 -0.49 if entity @s[dx=84.2,dy=20,dz=84.2] run scoreboard players set #inside lr_tmp 1
execute positioned 312.30 58 -0.97 if entity @s[dx=85.1,dy=20,dz=85.1] run scoreboard players set #inside lr_tmp 1
execute positioned 327.98 58 -1.28 if entity @s[dx=85.8,dy=20,dz=85.8] run scoreboard players set #inside lr_tmp 1
execute positioned 343.86 58 -1.40 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 359.86 58 -1.40 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 375.82 58 -0.71 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 390.82 58 4.65 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 403.15 58 14.73 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 411.38 58 28.35 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 414.57 58 43.95 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 414.60 58 51.55 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 310 65 8 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
