scoreboard players set #inside lr_tmp 0
execute positioned 124.34 58 641.87 if entity @s[dx=84.1,dy=20,dz=84.1] run scoreboard players set #inside lr_tmp 1
execute positioned 127.01 58 659.75 if entity @s[dx=80.3,dy=20,dz=80.3] run scoreboard players set #inside lr_tmp 1
execute positioned 134.07 58 676.33 if entity @s[dx=77.0,dy=20,dz=77.0] run scoreboard players set #inside lr_tmp 1
execute positioned 145.54 58 689.93 if entity @s[dx=74.4,dy=20,dz=74.4] run scoreboard players set #inside lr_tmp 1
execute positioned 160.33 58 699.21 if entity @s[dx=72.1,dy=20,dz=72.1] run scoreboard players set #inside lr_tmp 1
execute positioned 176.96 58 703.33 if entity @s[dx=70.1,dy=20,dz=70.1] run scoreboard players set #inside lr_tmp 1
execute positioned 193.18 58 703.56 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 209.02 58 703.40 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 225.10 58 703.48 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 241.42 58 703.80 if entity @s[dx=69.2,dy=20,dz=69.2] run scoreboard players set #inside lr_tmp 1
execute positioned 257.95 58 704.33 if entity @s[dx=68.1,dy=20,dz=68.1] run scoreboard players set #inside lr_tmp 1
execute positioned 274.65 58 705.04 if entity @s[dx=66.7,dy=20,dz=66.7] run scoreboard players set #inside lr_tmp 1
execute positioned 291.49 58 705.87 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 308.39 58 706.78 if entity @s[dx=63.2,dy=20,dz=63.2] run scoreboard players set #inside lr_tmp 1
execute positioned 310.51 58 706.89 if entity @s[dx=63.0,dy=20,dz=63.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
