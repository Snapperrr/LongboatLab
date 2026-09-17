scoreboard players set #inside lr_tmp 0
execute positioned -27.00 58 229.60 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -26.47 58 245.51 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned -21.54 58 260.37 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned -11.99 58 272.48 if entity @s[dx=55.3,dy=20,dz=55.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1.04 58 280.44 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 16.01 58 283.28 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 31.37 58 282.68 if entity @s[dx=58.6,dy=20,dz=58.6] run scoreboard players set #inside lr_tmp 1
execute positioned 46.67 58 281.98 if entity @s[dx=60.0,dy=20,dz=60.0] run scoreboard players set #inside lr_tmp 1
execute positioned 61.94 58 281.25 if entity @s[dx=61.5,dy=20,dz=61.5] run scoreboard players set #inside lr_tmp 1
execute positioned 77.20 58 280.51 if entity @s[dx=63.0,dy=20,dz=63.0] run scoreboard players set #inside lr_tmp 1
execute positioned 92.48 58 279.79 if entity @s[dx=64.4,dy=20,dz=64.4] run scoreboard players set #inside lr_tmp 1
execute positioned 107.79 58 279.10 if entity @s[dx=65.8,dy=20,dz=65.8] run scoreboard players set #inside lr_tmp 1
execute positioned 115.47 58 278.78 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 145 65 288 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
