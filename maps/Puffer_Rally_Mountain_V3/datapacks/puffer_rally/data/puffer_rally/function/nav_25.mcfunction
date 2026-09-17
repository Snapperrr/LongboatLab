scoreboard players set #inside lr_tmp 0
execute positioned 904.52 58 1102.85 if entity @s[dx=65.8,dy=20,dz=65.8] run scoreboard players set #inside lr_tmp 1
execute positioned 915.70 58 1115.12 if entity @s[dx=64.9,dy=20,dz=64.9] run scoreboard players set #inside lr_tmp 1
execute positioned 923.69 58 1129.67 if entity @s[dx=63.9,dy=20,dz=63.9] run scoreboard players set #inside lr_tmp 1
execute positioned 928.03 58 1145.63 if entity @s[dx=63.0,dy=20,dz=63.0] run scoreboard players set #inside lr_tmp 1
execute positioned 928.99 58 1162.09 if entity @s[dx=62.0,dy=20,dz=62.0] run scoreboard players set #inside lr_tmp 1
execute positioned 929.45 58 1178.56 if entity @s[dx=61.1,dy=20,dz=61.1] run scoreboard players set #inside lr_tmp 1
execute positioned 929.90 58 1195.01 if entity @s[dx=60.2,dy=20,dz=60.2] run scoreboard players set #inside lr_tmp 1
execute positioned 930.33 58 1211.43 if entity @s[dx=59.3,dy=20,dz=59.3] run scoreboard players set #inside lr_tmp 1
execute positioned 930.72 58 1227.82 if entity @s[dx=58.6,dy=20,dz=58.6] run scoreboard players set #inside lr_tmp 1
execute positioned 931.07 58 1244.17 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 931.37 58 1260.47 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 931.61 58 1276.71 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 931.80 58 1292.90 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 931.93 58 1309.03 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 931.99 58 1325.09 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 931.99 58 1341.09 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 931.92 58 1357.02 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 931.79 58 1372.89 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 931.60 58 1388.70 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 931.57 58 1390.67 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
