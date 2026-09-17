scoreboard players set #inside lr_tmp 0
execute positioned 2020.72 90 1559.52 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2030.96 91 1547.21 if entity @s[dx=55.1,dy=20,dz=55.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2042.00 91 1535.62 if entity @s[dx=55.4,dy=20,dz=55.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2053.78 92 1524.82 if entity @s[dx=55.8,dy=20,dz=55.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2066.30 92 1514.92 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2079.53 93 1506.02 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2093.43 93 1498.26 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2107.95 93 1491.74 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2122.98 94 1486.52 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2138.41 94 1482.62 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2154.12 95 1479.99 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2170.01 95 1478.55 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2186.00 96 1478.23 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2202.02 96 1478.94 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2218.00 97 1480.69 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2233.86 97 1483.52 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2249.51 98 1487.53 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2264.81 98 1492.85 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2279.56 99 1499.61 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2293.54 99 1507.89 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2306.52 100 1517.69 if entity @s[dx=55.9,dy=20,dz=55.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2318.31 100 1528.89 if entity @s[dx=55.5,dy=20,dz=55.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2328.79 101 1541.31 if entity @s[dx=55.1,dy=20,dz=55.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2337.98 101 1554.68 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2346.02 101 1568.73 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2353.16 102 1583.23 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2359.70 102 1597.95 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2365.90 103 1612.77 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2371.89 103 1627.62 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2372.62 103 1629.48 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
