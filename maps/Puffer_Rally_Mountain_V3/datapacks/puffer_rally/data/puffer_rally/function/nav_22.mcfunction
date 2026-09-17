scoreboard players set #inside lr_tmp 0
execute positioned 597.23 58 1444.05 if entity @s[dx=59.7,dy=20,dz=59.7] run scoreboard players set #inside lr_tmp 1
execute positioned 605.54 58 1430.59 if entity @s[dx=58.9,dy=20,dz=58.9] run scoreboard players set #inside lr_tmp 1
execute positioned 610.20 58 1415.58 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 611.24 58 1399.94 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 611.51 58 1384.21 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 611.73 58 1368.43 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 611.88 58 1352.58 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 611.97 58 1336.67 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 612.00 58 1320.70 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 611.96 58 1304.66 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 611.86 58 1288.55 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 611.69 58 1272.39 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 611.46 58 1256.16 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 611.18 58 1239.88 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 610.85 58 1223.55 if entity @s[dx=58.3,dy=20,dz=58.3] run scoreboard players set #inside lr_tmp 1
execute positioned 610.47 58 1207.17 if entity @s[dx=59.1,dy=20,dz=59.1] run scoreboard players set #inside lr_tmp 1
execute positioned 610.06 58 1190.76 if entity @s[dx=59.9,dy=20,dz=59.9] run scoreboard players set #inside lr_tmp 1
execute positioned 609.62 58 1174.32 if entity @s[dx=60.8,dy=20,dz=60.8] run scoreboard players set #inside lr_tmp 1
execute positioned 609.16 58 1157.86 if entity @s[dx=61.7,dy=20,dz=61.7] run scoreboard players set #inside lr_tmp 1
execute positioned 609.10 58 1155.80 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
