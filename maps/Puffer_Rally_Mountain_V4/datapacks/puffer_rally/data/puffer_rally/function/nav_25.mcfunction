scoreboard players set #inside lr_tmp 0
execute positioned 1180.07 58 1570.07 if entity @s[dx=59.9,dy=20,dz=59.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1179.63 58 1553.63 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1179.17 58 1537.17 if entity @s[dx=61.7,dy=20,dz=61.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1178.69 58 1520.69 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1178.21 58 1504.21 if entity @s[dx=63.6,dy=20,dz=63.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1177.74 58 1487.74 if entity @s[dx=64.5,dy=20,dz=64.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1177.28 58 1471.28 if entity @s[dx=65.4,dy=20,dz=65.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1176.84 58 1454.84 if entity @s[dx=66.3,dy=20,dz=66.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1176.44 58 1438.44 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1176.18 58 1427.08 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
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
