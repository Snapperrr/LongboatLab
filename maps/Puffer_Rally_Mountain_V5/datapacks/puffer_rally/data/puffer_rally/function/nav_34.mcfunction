scoreboard players set #inside lr_tmp 0
execute positioned 2153.00 58 1473.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2168.97 58 1473.59 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2184.81 58 1475.25 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2200.49 59 1477.88 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2215.94 59 1481.41 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2231.12 60 1485.86 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2245.93 60 1491.30 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2260.26 61 1497.83 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2273.97 61 1505.55 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2286.88 62 1514.52 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2298.84 62 1524.75 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2309.69 63 1536.17 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2319.34 63 1548.65 if entity @s[dx=57.4,dy=20,dz=57.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2327.79 64 1562.02 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2335.07 64 1576.12 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2341.30 65 1590.76 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2346.61 65 1605.82 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2349.24 65 1614.43 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
