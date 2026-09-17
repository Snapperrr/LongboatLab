scoreboard players set #inside lr_tmp 0
execute positioned 390.53 58 1465.12 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 406.68 58 1465.28 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 422.90 58 1465.50 if entity @s[dx=69.0,dy=20,dz=69.0] run scoreboard players set #inside lr_tmp 1
execute positioned 439.18 58 1465.77 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 455.50 58 1466.10 if entity @s[dx=67.8,dy=20,dz=67.8] run scoreboard players set #inside lr_tmp 1
execute positioned 471.87 58 1466.47 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute positioned 488.28 58 1466.87 if entity @s[dx=66.3,dy=20,dz=66.3] run scoreboard players set #inside lr_tmp 1
execute positioned 504.71 58 1467.31 if entity @s[dx=65.4,dy=20,dz=65.4] run scoreboard players set #inside lr_tmp 1
execute positioned 521.17 58 1467.77 if entity @s[dx=64.5,dy=20,dz=64.5] run scoreboard players set #inside lr_tmp 1
execute positioned 537.65 58 1468.24 if entity @s[dx=63.5,dy=20,dz=63.5] run scoreboard players set #inside lr_tmp 1
execute positioned 554.08 58 1467.89 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 569.88 58 1463.91 if entity @s[dx=61.6,dy=20,dz=61.6] run scoreboard players set #inside lr_tmp 1
execute positioned 584.12 58 1456.31 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 595.95 58 1445.58 if entity @s[dx=59.8,dy=20,dz=59.8] run scoreboard players set #inside lr_tmp 1
execute positioned 604.69 58 1432.37 if entity @s[dx=59.0,dy=20,dz=59.0] run scoreboard players set #inside lr_tmp 1
execute positioned 609.83 58 1417.51 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 611.21 58 1401.90 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 611.48 58 1386.18 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 611.71 58 1370.40 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 611.73 58 1368.43 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
