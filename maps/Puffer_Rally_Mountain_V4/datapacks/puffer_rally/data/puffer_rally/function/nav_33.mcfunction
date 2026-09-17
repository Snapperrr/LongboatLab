scoreboard players set #inside lr_tmp 0
execute positioned 1913.72 58 1465.12 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1929.87 58 1465.27 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1946.09 58 1465.49 if entity @s[dx=69.0,dy=20,dz=69.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1962.36 58 1465.76 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1978.68 58 1466.08 if entity @s[dx=67.8,dy=20,dz=67.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1995.05 58 1466.45 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2011.46 58 1466.86 if entity @s[dx=66.3,dy=20,dz=66.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2027.89 58 1467.30 if entity @s[dx=65.4,dy=20,dz=65.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2044.35 58 1467.75 if entity @s[dx=64.5,dy=20,dz=64.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2060.83 58 1468.23 if entity @s[dx=63.5,dy=20,dz=63.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2077.30 58 1468.71 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2093.78 58 1469.18 if entity @s[dx=61.6,dy=20,dz=61.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2110.24 58 1469.64 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2126.68 58 1470.08 if entity @s[dx=59.8,dy=20,dz=59.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2143.09 58 1470.49 if entity @s[dx=59.0,dy=20,dz=59.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2161.59 58 1473.17 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2177.50 58 1474.36 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2193.26 59 1476.55 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2208.82 59 1479.67 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2224.14 60 1483.69 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2231.12 60 1485.86 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
