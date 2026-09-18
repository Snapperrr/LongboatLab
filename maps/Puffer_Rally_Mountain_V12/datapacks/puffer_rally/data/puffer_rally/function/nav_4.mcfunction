scoreboard players set #inside lr_tmp 0
execute positioned 39.02 58 282.33 if entity @s[dx=59.3,dy=20,dz=59.3] run scoreboard players set #inside lr_tmp 1
execute positioned 54.30 58 281.62 if entity @s[dx=60.8,dy=20,dz=60.8] run scoreboard players set #inside lr_tmp 1
execute positioned 69.57 58 280.88 if entity @s[dx=62.2,dy=20,dz=62.2] run scoreboard players set #inside lr_tmp 1
execute positioned 84.84 58 280.15 if entity @s[dx=63.7,dy=20,dz=63.7] run scoreboard players set #inside lr_tmp 1
execute positioned 100.13 58 279.44 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 115.47 58 278.78 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 130.88 58 278.20 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 146.39 58 277.70 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 161.77 58 275.03 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 175.62 58 267.37 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 186.32 58 255.58 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 192.55 58 240.93 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.11 58 238.18 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 145 65 285 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
