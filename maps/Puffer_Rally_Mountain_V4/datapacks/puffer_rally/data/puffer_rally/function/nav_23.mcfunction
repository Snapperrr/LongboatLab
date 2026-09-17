scoreboard players set #inside lr_tmp 0
execute positioned 895.45 58 1398.13 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 908.37 58 1406.68 if entity @s[dx=58.4,dy=20,dz=58.4] run scoreboard players set #inside lr_tmp 1
execute positioned 918.67 58 1418.15 if entity @s[dx=59.2,dy=20,dz=59.2] run scoreboard players set #inside lr_tmp 1
execute positioned 925.74 58 1431.83 if entity @s[dx=60.0,dy=20,dz=60.0] run scoreboard players set #inside lr_tmp 1
execute positioned 929.11 58 1446.88 if entity @s[dx=60.9,dy=20,dz=60.9] run scoreboard players set #inside lr_tmp 1
execute positioned 929.10 58 1462.40 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute positioned 928.62 58 1477.93 if entity @s[dx=62.8,dy=20,dz=62.8] run scoreboard players set #inside lr_tmp 1
execute positioned 928.15 58 1493.45 if entity @s[dx=63.7,dy=20,dz=63.7] run scoreboard players set #inside lr_tmp 1
execute positioned 927.67 58 1508.98 if entity @s[dx=64.7,dy=20,dz=64.7] run scoreboard players set #inside lr_tmp 1
execute positioned 927.22 58 1524.52 if entity @s[dx=65.6,dy=20,dz=65.6] run scoreboard players set #inside lr_tmp 1
execute positioned 926.79 58 1540.09 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 926.39 58 1555.69 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 926.37 58 1556.37 if entity @s[dx=67.3,dy=20,dz=67.3] run scoreboard players set #inside lr_tmp 1
execute positioned 960.00 64 1590.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 961.17 64 1598.64 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 964.54 64 1606.56 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 969.94 64 1613.76 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 977.15 64 1620.24 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 986.00 64 1626.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 996.29 64 1631.04 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1007.82 64 1635.36 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1020.42 64 1638.96 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1033.87 64 1641.84 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1048.00 64 1644.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1062.61 64 1645.44 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1077.50 64 1646.16 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1092.50 64 1646.16 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1107.39 64 1645.44 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1122.00 64 1644.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1136.13 64 1641.84 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1149.58 64 1638.96 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1162.18 64 1635.36 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1173.71 64 1631.04 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1184.00 64 1626.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1192.85 64 1620.24 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1200.06 64 1613.76 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1205.46 64 1606.56 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1208.83 64 1598.64 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1210.00 64 1590.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
