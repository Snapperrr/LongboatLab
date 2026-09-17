scoreboard players set #inside lr_tmp 0
execute positioned 931.94 58 1311.04 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 932.00 58 1327.10 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 931.99 58 1343.09 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 931.91 58 1359.01 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 931.77 58 1374.87 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 931.57 58 1390.67 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 931.40 58 1401.40 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 960.00 64 1430.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 961.17 64 1438.64 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 964.54 64 1446.56 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 969.94 64 1453.76 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 977.15 64 1460.24 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 986.00 64 1466.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 996.29 64 1471.04 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1007.82 64 1475.36 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1020.42 64 1478.96 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1033.87 64 1481.84 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1048.00 64 1484.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1062.61 64 1485.44 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1077.50 64 1486.16 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1092.50 64 1486.16 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1107.39 64 1485.44 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1122.00 64 1484.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1136.13 64 1481.84 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1149.58 64 1478.96 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1162.18 64 1475.36 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1173.71 64 1471.04 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1184.00 64 1466.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1192.85 64 1460.24 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1200.06 64 1453.76 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1205.46 64 1446.56 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1208.83 64 1438.64 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1210.00 64 1430.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
