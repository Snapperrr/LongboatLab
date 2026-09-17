scoreboard players set #inside lr_tmp 0
execute positioned 284.42 58 1316.85 if entity @s[dx=79.1,dy=20,dz=79.1] run scoreboard players set #inside lr_tmp 1
execute positioned 291.26 58 1332.47 if entity @s[dx=77.4,dy=20,dz=77.4] run scoreboard players set #inside lr_tmp 1
execute positioned 300.55 58 1346.72 if entity @s[dx=75.9,dy=20,dz=75.9] run scoreboard players set #inside lr_tmp 1
execute positioned 312.02 58 1359.17 if entity @s[dx=74.6,dy=20,dz=74.6] run scoreboard players set #inside lr_tmp 1
execute positioned 325.34 58 1369.46 if entity @s[dx=73.4,dy=20,dz=73.4] run scoreboard players set #inside lr_tmp 1
execute positioned 340.13 58 1377.31 if entity @s[dx=72.2,dy=20,dz=72.2] run scoreboard players set #inside lr_tmp 1
execute positioned 355.95 58 1382.50 if entity @s[dx=71.2,dy=20,dz=71.2] run scoreboard players set #inside lr_tmp 1
execute positioned 372.34 58 1384.90 if entity @s[dx=70.1,dy=20,dz=70.1] run scoreboard players set #inside lr_tmp 1
execute positioned 388.73 58 1385.32 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 404.56 58 1385.15 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 420.45 58 1385.05 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 436.40 58 1385.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 452.43 58 1385.02 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 468.51 58 1385.11 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 484.66 58 1385.26 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 500.87 58 1385.47 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 517.14 58 1385.73 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 533.46 58 1386.05 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 549.82 58 1386.42 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 551.87 58 1386.47 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
