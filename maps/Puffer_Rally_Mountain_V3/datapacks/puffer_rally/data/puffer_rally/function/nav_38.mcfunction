scoreboard players set #inside lr_tmp 0
execute positioned 2061.94 58 1384.23 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2062.00 58 1400.29 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2062.65 58 1416.25 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2066.78 58 1431.57 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2074.46 58 1445.35 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2085.24 58 1456.74 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2098.45 58 1465.04 if entity @s[dx=57.4,dy=20,dz=57.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2113.29 58 1469.76 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2128.85 58 1470.66 if entity @s[dx=58.7,dy=20,dz=58.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2144.46 58 1470.26 if entity @s[dx=59.5,dy=20,dz=59.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2160.03 58 1469.84 if entity @s[dx=60.3,dy=20,dz=60.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2175.58 58 1469.38 if entity @s[dx=61.2,dy=20,dz=61.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2191.11 58 1468.91 if entity @s[dx=62.2,dy=20,dz=62.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2206.63 58 1468.43 if entity @s[dx=63.1,dy=20,dz=63.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2222.15 58 1467.96 if entity @s[dx=64.1,dy=20,dz=64.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2237.69 58 1467.49 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2253.24 58 1467.04 if entity @s[dx=65.9,dy=20,dz=65.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2268.82 58 1466.62 if entity @s[dx=66.8,dy=20,dz=66.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2284.43 58 1466.24 if entity @s[dx=67.5,dy=20,dz=67.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2286.39 58 1466.19 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
