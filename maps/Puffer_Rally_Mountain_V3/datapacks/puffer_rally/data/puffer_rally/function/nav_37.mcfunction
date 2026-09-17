scoreboard players set #inside lr_tmp 0
execute positioned 2043.57 58 1172.40 if entity @s[dx=65.8,dy=20,dz=65.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2052.14 58 1186.59 if entity @s[dx=64.9,dy=20,dz=64.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2057.14 58 1202.36 if entity @s[dx=64.0,dy=20,dz=64.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2058.49 58 1218.79 if entity @s[dx=63.0,dy=20,dz=63.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2058.97 58 1235.27 if entity @s[dx=62.1,dy=20,dz=62.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2059.44 58 1251.73 if entity @s[dx=61.1,dy=20,dz=61.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2059.89 58 1268.18 if entity @s[dx=60.2,dy=20,dz=60.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2060.31 58 1284.61 if entity @s[dx=59.4,dy=20,dz=59.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2060.71 58 1301.00 if entity @s[dx=58.6,dy=20,dz=58.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2061.06 58 1317.35 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2061.36 58 1333.65 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2061.61 58 1349.90 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2061.80 58 1366.09 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2061.93 58 1382.22 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2061.99 58 1398.29 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2062.40 58 1414.27 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2066.06 58 1429.73 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2073.32 58 1443.74 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2083.74 58 1455.47 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2085.24 58 1456.74 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
