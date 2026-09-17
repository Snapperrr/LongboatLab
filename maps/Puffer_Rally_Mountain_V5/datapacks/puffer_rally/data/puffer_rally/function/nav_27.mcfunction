scoreboard players set #inside lr_tmp 0
execute positioned 1175.12 58 1296.02 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.28 58 1280.18 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1175.78 58 1264.40 if entity @s[dx=69.0,dy=20,dz=69.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1179.49 58 1249.09 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1186.94 58 1235.14 if entity @s[dx=67.8,dy=20,dz=67.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1197.71 58 1223.39 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1211.14 58 1214.59 if entity @s[dx=66.3,dy=20,dz=66.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1226.46 58 1209.26 if entity @s[dx=65.4,dy=20,dz=65.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1242.76 58 1207.76 if entity @s[dx=64.5,dy=20,dz=64.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1259.24 58 1208.24 if entity @s[dx=63.5,dy=20,dz=63.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1275.71 58 1208.71 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1292.19 58 1209.19 if entity @s[dx=61.6,dy=20,dz=61.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1308.65 58 1209.65 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1325.09 58 1210.09 if entity @s[dx=59.8,dy=20,dz=59.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1341.50 58 1210.50 if entity @s[dx=59.0,dy=20,dz=59.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1357.87 58 1210.87 if entity @s[dx=58.3,dy=20,dz=58.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1374.20 58 1211.20 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1390.48 58 1211.48 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1406.66 58 1212.47 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1408.66 58 1212.83 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
