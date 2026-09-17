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
execute positioned 278.95 58 1289.73 if entity @s[dx=82.1,dy=20,dz=82.1] run scoreboard players set #inside lr_tmp 1
execute positioned 279.91 58 1306.69 if entity @s[dx=80.2,dy=20,dz=80.2] run scoreboard players set #inside lr_tmp 1
execute positioned 280.79 58 1323.56 if entity @s[dx=78.4,dy=20,dz=78.4] run scoreboard players set #inside lr_tmp 1
execute positioned 281.58 58 1340.35 if entity @s[dx=76.8,dy=20,dz=76.8] run scoreboard players set #inside lr_tmp 1
execute positioned 282.29 58 1357.07 if entity @s[dx=75.4,dy=20,dz=75.4] run scoreboard players set #inside lr_tmp 1
execute positioned 282.94 58 1373.71 if entity @s[dx=74.1,dy=20,dz=74.1] run scoreboard players set #inside lr_tmp 1
execute positioned 285.09 58 1390.21 if entity @s[dx=72.9,dy=20,dz=72.9] run scoreboard players set #inside lr_tmp 1
execute positioned 287.84 58 1400.26 if entity @s[dx=72.2,dy=20,dz=72.2] run scoreboard players set #inside lr_tmp 1
execute positioned 353 65 1182 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
