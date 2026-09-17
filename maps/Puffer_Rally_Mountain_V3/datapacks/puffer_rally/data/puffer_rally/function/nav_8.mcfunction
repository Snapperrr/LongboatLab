scoreboard players set #inside lr_tmp 0
execute positioned 405.00 58 322.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 306.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 290.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 274.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 258.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 242.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 226.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 404.97 58 210.71 if entity @s[dx=70.1,dy=20,dz=70.1] run scoreboard players set #inside lr_tmp 1
execute positioned 404.88 58 194.62 if entity @s[dx=70.2,dy=20,dz=70.2] run scoreboard players set #inside lr_tmp 1
execute positioned 404.74 58 178.48 if entity @s[dx=70.5,dy=20,dz=70.5] run scoreboard players set #inside lr_tmp 1
execute positioned 404.55 58 162.29 if entity @s[dx=70.9,dy=20,dz=70.9] run scoreboard players set #inside lr_tmp 1
execute positioned 404.32 58 146.06 if entity @s[dx=71.4,dy=20,dz=71.4] run scoreboard players set #inside lr_tmp 1
execute positioned 404.15 58 129.79 if entity @s[dx=71.9,dy=20,dz=71.9] run scoreboard players set #inside lr_tmp 1
execute positioned 406.01 58 113.65 if entity @s[dx=72.5,dy=20,dz=72.5] run scoreboard players set #inside lr_tmp 1
execute positioned 410.61 58 98.12 if entity @s[dx=73.2,dy=20,dz=73.2] run scoreboard players set #inside lr_tmp 1
execute positioned 417.79 58 83.67 if entity @s[dx=73.9,dy=20,dz=73.9] run scoreboard players set #inside lr_tmp 1
execute positioned 427.33 58 70.76 if entity @s[dx=74.7,dy=20,dz=74.7] run scoreboard players set #inside lr_tmp 1
execute positioned 430.05 58 67.82 if entity @s[dx=74.9,dy=20,dz=74.9] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
