scoreboard players set #inside lr_tmp 0
execute positioned 1840.08 58 1145.68 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1855.82 58 1145.43 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1871.62 58 1145.23 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1887.48 58 1145.09 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1903.41 58 1145.01 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1919.40 58 1145.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1935.45 58 1145.06 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1951.57 58 1145.18 if entity @s[dx=69.6,dy=20,dz=69.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1967.76 58 1145.36 if entity @s[dx=69.3,dy=20,dz=69.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1984.00 58 1145.60 if entity @s[dx=68.8,dy=20,dz=68.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2000.26 58 1146.57 if entity @s[dx=68.2,dy=20,dz=68.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2015.99 58 1151.14 if entity @s[dx=67.5,dy=20,dz=67.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2030.28 58 1159.37 if entity @s[dx=66.8,dy=20,dz=66.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2042.27 58 1170.78 if entity @s[dx=65.9,dy=20,dz=65.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2051.25 58 1184.71 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2056.72 58 1200.33 if entity @s[dx=64.1,dy=20,dz=64.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2058.43 58 1216.73 if entity @s[dx=63.1,dy=20,dz=63.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2058.91 58 1233.21 if entity @s[dx=62.2,dy=20,dz=62.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2059.38 58 1249.68 if entity @s[dx=61.2,dy=20,dz=61.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2059.44 58 1251.73 if entity @s[dx=61.1,dy=20,dz=61.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
