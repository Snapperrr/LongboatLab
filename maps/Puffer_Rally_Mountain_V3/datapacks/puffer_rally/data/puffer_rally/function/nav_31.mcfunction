scoreboard players set #inside lr_tmp 0
execute positioned 1328.48 58 1095.68 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1344.22 58 1095.42 if entity @s[dx=69.2,dy=20,dz=69.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1360.02 58 1095.23 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1375.89 58 1095.09 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1391.81 58 1095.04 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1407.58 58 1097.43 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1422.36 58 1103.65 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1435.25 58 1113.34 if entity @s[dx=69.6,dy=20,dz=69.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1445.49 58 1125.92 if entity @s[dx=69.3,dy=20,dz=69.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1452.46 58 1140.63 if entity @s[dx=68.8,dy=20,dz=68.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1455.76 58 1156.60 if entity @s[dx=68.2,dy=20,dz=68.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1456.24 58 1172.94 if entity @s[dx=67.5,dy=20,dz=67.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1456.63 58 1189.33 if entity @s[dx=66.7,dy=20,dz=66.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1457.05 58 1205.75 if entity @s[dx=65.9,dy=20,dz=65.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1457.50 58 1222.20 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1457.96 58 1238.66 if entity @s[dx=64.1,dy=20,dz=64.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1458.44 58 1255.14 if entity @s[dx=63.1,dy=20,dz=63.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1458.92 58 1271.62 if entity @s[dx=62.2,dy=20,dz=62.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1459.39 58 1288.09 if entity @s[dx=61.2,dy=20,dz=61.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1459.45 58 1290.14 if entity @s[dx=61.1,dy=20,dz=61.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
