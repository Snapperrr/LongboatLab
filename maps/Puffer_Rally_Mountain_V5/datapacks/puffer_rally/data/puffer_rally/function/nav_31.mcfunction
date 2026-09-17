scoreboard players set #inside lr_tmp 0
execute positioned 1758.39 58 1548.39 if entity @s[dx=63.2,dy=20,dz=63.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1757.92 58 1531.91 if entity @s[dx=64.2,dy=20,dz=64.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1759.66 58 1515.64 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1765.20 58 1500.40 if entity @s[dx=66.0,dy=20,dz=66.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1774.20 58 1487.10 if entity @s[dx=66.8,dy=20,dz=66.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1786.08 58 1476.51 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1786.66 58 1476.12 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1490.00 64 1570.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1490.98 64 1577.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1493.82 64 1584.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1498.40 64 1590.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1504.58 64 1596.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1512.22 64 1601.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1521.20 64 1606.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1531.38 64 1610.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1542.62 64 1614.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1554.80 64 1617.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1567.78 64 1620.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1581.42 64 1622.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1595.60 64 1624.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1610.18 64 1625.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1625.02 64 1626.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1640.00 64 1626.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1654.98 64 1626.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1669.82 64 1625.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1684.40 64 1624.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1698.58 64 1622.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1712.22 64 1620.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1725.20 64 1617.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1737.38 64 1614.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1748.62 64 1610.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1758.80 64 1606.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1767.78 64 1601.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1775.42 64 1596.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1781.60 64 1590.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1786.18 64 1584.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1789.02 64 1577.25 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 1790.00 64 1570.00 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
