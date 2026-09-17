scoreboard players set #inside lr_tmp 0
execute positioned 133.59 58 278.10 if entity @s[dx=67.8,dy=20,dz=67.8] run scoreboard players set #inside lr_tmp 1
execute positioned 149.11 58 277.60 if entity @s[dx=68.7,dy=20,dz=68.7] run scoreboard players set #inside lr_tmp 1
execute positioned 164.35 58 274.04 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 177.76 58 265.57 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 187.77 58 253.18 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.11 58 238.18 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 222.22 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 206.22 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 190.22 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 174.22 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 158.22 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 152.42 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 145 65 288 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
