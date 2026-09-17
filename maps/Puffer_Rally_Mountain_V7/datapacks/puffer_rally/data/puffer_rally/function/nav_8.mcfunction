scoreboard players set #inside lr_tmp 0
execute positioned 193.80 58 172.62 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 156.62 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 140.62 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 124.62 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.80 58 108.62 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 193.77 58 92.59 if entity @s[dx=70.1,dy=20,dz=70.1] run scoreboard players set #inside lr_tmp 1
execute positioned 193.55 58 76.37 if entity @s[dx=70.5,dy=20,dz=70.5] run scoreboard players set #inside lr_tmp 1
execute positioned 193.15 58 59.97 if entity @s[dx=71.3,dy=20,dz=71.3] run scoreboard players set #inside lr_tmp 1
execute positioned 193.43 58 43.48 if entity @s[dx=72.4,dy=20,dz=72.4] run scoreboard players set #inside lr_tmp 1
execute positioned 198.36 58 27.91 if entity @s[dx=73.7,dy=20,dz=73.7] run scoreboard players set #inside lr_tmp 1
execute positioned 207.88 58 15.00 if entity @s[dx=75.2,dy=20,dz=75.2] run scoreboard players set #inside lr_tmp 1
execute positioned 218.73 58 7.19 if entity @s[dx=76.5,dy=20,dz=76.5] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
