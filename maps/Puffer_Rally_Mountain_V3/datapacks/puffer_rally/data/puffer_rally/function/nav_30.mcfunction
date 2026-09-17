scoreboard players set #inside lr_tmp 0
execute positioned 1181.15 58 1186.25 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1180.81 58 1169.91 if entity @s[dx=58.4,dy=20,dz=58.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1181.52 58 1153.60 if entity @s[dx=59.1,dy=20,dz=59.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1185.91 58 1137.97 if entity @s[dx=60.0,dy=20,dz=60.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1193.84 58 1123.93 if entity @s[dx=60.9,dy=20,dz=60.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1204.81 58 1112.33 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1218.13 58 1103.84 if entity @s[dx=62.7,dy=20,dz=62.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1232.99 58 1098.95 if entity @s[dx=63.7,dy=20,dz=63.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1248.48 58 1097.68 if entity @s[dx=64.6,dy=20,dz=64.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1264.02 58 1097.23 if entity @s[dx=65.5,dy=20,dz=65.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1279.59 58 1096.79 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1295.19 58 1096.39 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1310.83 58 1096.03 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1326.51 58 1095.72 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1342.25 58 1095.45 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1358.05 58 1095.25 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1373.90 58 1095.10 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1389.82 58 1095.02 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1405.65 58 1096.91 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1407.58 58 1097.43 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
