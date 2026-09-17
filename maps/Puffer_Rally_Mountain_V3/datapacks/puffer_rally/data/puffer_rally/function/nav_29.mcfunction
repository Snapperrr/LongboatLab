scoreboard players set #inside lr_tmp 0
execute positioned 1180.14 58 1395.24 if entity @s[dx=59.7,dy=20,dz=59.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1180.55 58 1379.65 if entity @s[dx=58.9,dy=20,dz=58.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1180.92 58 1364.02 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.24 58 1348.34 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.51 58 1332.61 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.73 58 1316.83 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.88 58 1300.98 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.97 58 1285.07 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1182.00 58 1269.10 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.96 58 1253.06 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.86 58 1236.96 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.69 58 1220.79 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.47 58 1204.57 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.19 58 1188.29 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1180.86 58 1171.96 if entity @s[dx=58.3,dy=20,dz=58.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.23 58 1155.62 if entity @s[dx=59.0,dy=20,dz=59.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1185.16 58 1139.85 if entity @s[dx=59.9,dy=20,dz=59.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1192.67 58 1125.57 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1203.29 58 1113.63 if entity @s[dx=61.7,dy=20,dz=61.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1204.81 58 1112.33 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
