scoreboard players set #inside lr_tmp 0
execute positioned 1178.36 58 1509.26 if entity @s[dx=63.3,dy=20,dz=63.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1177.88 58 1492.78 if entity @s[dx=64.2,dy=20,dz=64.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1177.42 58 1476.32 if entity @s[dx=65.2,dy=20,dz=65.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1176.98 58 1459.87 if entity @s[dx=66.0,dy=20,dz=66.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1176.56 58 1443.46 if entity @s[dx=66.9,dy=20,dz=66.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1176.18 58 1427.08 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.85 58 1410.74 if entity @s[dx=68.3,dy=20,dz=68.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.56 58 1394.46 if entity @s[dx=68.9,dy=20,dz=68.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.33 58 1378.23 if entity @s[dx=69.3,dy=20,dz=69.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.16 58 1362.05 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.05 58 1345.95 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.00 58 1329.90 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.02 58 1313.92 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.11 58 1298.01 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.26 58 1282.15 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.59 58 1266.37 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1178.82 58 1250.95 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1185.82 58 1236.77 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1196.20 58 1224.71 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1197.71 58 1223.39 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
