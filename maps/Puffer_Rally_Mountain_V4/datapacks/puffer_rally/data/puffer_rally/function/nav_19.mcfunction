scoreboard players set #inside lr_tmp 0
execute positioned 277.00 58 1063.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1079.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1095.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1111.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1127.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1143.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1159.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1175.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1191.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1207.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1223.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1239.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1255.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.91 58 1272.68 if entity @s[dx=84.2,dy=20,dz=84.2] run scoreboard players set #inside lr_tmp 1
execute positioned 278.96 58 1289.73 if entity @s[dx=82.1,dy=20,dz=82.1] run scoreboard players set #inside lr_tmp 1
execute positioned 281.47 58 1306.59 if entity @s[dx=80.2,dy=20,dz=80.2] run scoreboard players set #inside lr_tmp 1
execute positioned 286.69 58 1322.84 if entity @s[dx=78.4,dy=20,dz=78.4] run scoreboard players set #inside lr_tmp 1
execute positioned 294.47 58 1338.00 if entity @s[dx=76.8,dy=20,dz=76.8] run scoreboard players set #inside lr_tmp 1
execute positioned 304.61 58 1351.62 if entity @s[dx=75.4,dy=20,dz=75.4] run scoreboard players set #inside lr_tmp 1
execute positioned 316.82 58 1363.30 if entity @s[dx=74.1,dy=20,dz=74.1] run scoreboard players set #inside lr_tmp 1
execute positioned 330.74 58 1372.71 if entity @s[dx=72.9,dy=20,dz=72.9] run scoreboard players set #inside lr_tmp 1
execute positioned 340.13 58 1377.31 if entity @s[dx=72.2,dy=20,dz=72.2] run scoreboard players set #inside lr_tmp 1
execute positioned 353 65 1182 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
