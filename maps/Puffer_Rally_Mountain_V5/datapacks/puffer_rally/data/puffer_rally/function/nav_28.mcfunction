scoreboard players set #inside lr_tmp 0
execute positioned 1327.14 58 1210.14 if entity @s[dx=59.7,dy=20,dz=59.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1343.55 58 1210.55 if entity @s[dx=58.9,dy=20,dz=58.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1359.92 58 1210.92 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1376.24 58 1211.24 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1392.51 58 1211.51 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1408.66 58 1212.83 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1424.02 58 1217.82 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1437.69 58 1226.31 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1448.83 58 1237.78 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1456.79 58 1251.56 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1461.07 58 1266.80 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1461.69 58 1282.59 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1461.47 58 1298.37 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1461.19 58 1314.09 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1460.86 58 1329.76 if entity @s[dx=58.3,dy=20,dz=58.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1460.48 58 1345.38 if entity @s[dx=59.0,dy=20,dz=59.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1460.07 58 1360.97 if entity @s[dx=59.9,dy=20,dz=59.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1459.63 58 1376.53 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1459.17 58 1392.07 if entity @s[dx=61.7,dy=20,dz=61.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1459.11 58 1394.01 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
