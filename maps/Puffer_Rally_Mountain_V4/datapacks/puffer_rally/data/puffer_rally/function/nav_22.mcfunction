scoreboard players set #inside lr_tmp 0
execute positioned 685.55 58 1390.15 if entity @s[dx=59.7,dy=20,dz=59.7] run scoreboard players set #inside lr_tmp 1
execute positioned 701.96 58 1390.56 if entity @s[dx=58.9,dy=20,dz=58.9] run scoreboard players set #inside lr_tmp 1
execute positioned 718.33 58 1390.92 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 734.65 58 1391.24 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 750.92 58 1391.51 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 767.13 58 1391.73 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 783.29 58 1391.88 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 799.38 58 1391.97 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 815.40 58 1392.00 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 831.36 58 1391.96 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 847.26 58 1391.86 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 863.09 58 1391.69 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 878.79 58 1392.65 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 893.69 58 1397.29 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 906.88 58 1405.44 if entity @s[dx=58.3,dy=20,dz=58.3] run scoreboard players set #inside lr_tmp 1
execute positioned 917.55 58 1416.58 if entity @s[dx=59.1,dy=20,dz=59.1] run scoreboard players set #inside lr_tmp 1
execute positioned 925.05 58 1430.03 if entity @s[dx=59.9,dy=20,dz=59.9] run scoreboard players set #inside lr_tmp 1
execute positioned 928.90 58 1444.96 if entity @s[dx=60.8,dy=20,dz=60.8] run scoreboard players set #inside lr_tmp 1
execute positioned 929.16 58 1460.46 if entity @s[dx=61.7,dy=20,dz=61.7] run scoreboard players set #inside lr_tmp 1
execute positioned 929.10 58 1462.40 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
