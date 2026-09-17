scoreboard players set #inside lr_tmp 0
execute positioned 280.47 58 1317.24 if entity @s[dx=79.1,dy=20,dz=79.1] run scoreboard players set #inside lr_tmp 1
execute positioned 281.29 58 1334.06 if entity @s[dx=77.4,dy=20,dz=77.4] run scoreboard players set #inside lr_tmp 1
execute positioned 282.03 58 1350.81 if entity @s[dx=75.9,dy=20,dz=75.9] run scoreboard players set #inside lr_tmp 1
execute positioned 282.70 58 1367.48 if entity @s[dx=74.6,dy=20,dz=74.6] run scoreboard players set #inside lr_tmp 1
execute positioned 283.96 58 1384.06 if entity @s[dx=73.4,dy=20,dz=73.4] run scoreboard players set #inside lr_tmp 1
execute positioned 287.84 58 1400.26 if entity @s[dx=72.2,dy=20,dz=72.2] run scoreboard players set #inside lr_tmp 1
execute positioned 294.39 58 1415.60 if entity @s[dx=71.2,dy=20,dz=71.2] run scoreboard players set #inside lr_tmp 1
execute positioned 303.46 58 1429.62 if entity @s[dx=70.1,dy=20,dz=70.1] run scoreboard players set #inside lr_tmp 1
execute positioned 314.64 58 1441.79 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 327.18 58 1451.30 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 341.29 58 1458.48 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 356.54 58 1463.09 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 372.43 58 1464.98 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 388.51 58 1465.11 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 404.66 58 1465.26 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 420.87 58 1465.47 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 437.14 58 1465.73 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 453.46 58 1466.05 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 469.82 58 1466.42 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 471.87 58 1466.47 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
