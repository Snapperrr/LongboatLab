scoreboard players set #inside lr_tmp 0
execute positioned 1761.15 58 1304.66 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1760.82 58 1288.32 if entity @s[dx=58.4,dy=20,dz=58.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1760.44 58 1271.94 if entity @s[dx=59.1,dy=20,dz=59.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1760.02 58 1255.53 if entity @s[dx=60.0,dy=20,dz=60.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1759.58 58 1239.08 if entity @s[dx=60.8,dy=20,dz=60.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1759.11 58 1222.62 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1759.07 58 1206.16 if entity @s[dx=62.7,dy=20,dz=62.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1762.36 58 1190.17 if entity @s[dx=63.7,dy=20,dz=63.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1769.33 58 1175.58 if entity @s[dx=64.6,dy=20,dz=64.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1779.52 58 1163.24 if entity @s[dx=65.5,dy=20,dz=65.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1792.31 58 1153.87 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1806.92 58 1148.03 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1822.43 58 1146.04 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1838.12 58 1145.72 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1853.85 58 1145.46 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1869.65 58 1145.25 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1885.50 58 1145.10 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1901.42 58 1145.02 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1917.40 58 1145.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1919.40 58 1145.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
