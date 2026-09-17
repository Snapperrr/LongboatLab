scoreboard players set #inside lr_tmp 0
execute positioned 696.88 58 1085.68 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 712.62 58 1085.42 if entity @s[dx=69.2,dy=20,dz=69.2] run scoreboard players set #inside lr_tmp 1
execute positioned 728.42 58 1085.22 if entity @s[dx=69.6,dy=20,dz=69.6] run scoreboard players set #inside lr_tmp 1
execute positioned 744.29 58 1085.09 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 760.21 58 1085.01 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 776.21 58 1085.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 792.26 58 1085.06 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 808.39 58 1085.18 if entity @s[dx=69.6,dy=20,dz=69.6] run scoreboard players set #inside lr_tmp 1
execute positioned 824.57 58 1085.37 if entity @s[dx=69.3,dy=20,dz=69.3] run scoreboard players set #inside lr_tmp 1
execute positioned 840.81 58 1085.61 if entity @s[dx=68.8,dy=20,dz=68.8] run scoreboard players set #inside lr_tmp 1
execute positioned 857.11 58 1085.91 if entity @s[dx=68.2,dy=20,dz=68.2] run scoreboard players set #inside lr_tmp 1
execute positioned 873.38 58 1087.39 if entity @s[dx=67.5,dy=20,dz=67.5] run scoreboard players set #inside lr_tmp 1
execute positioned 888.96 58 1092.66 if entity @s[dx=66.7,dy=20,dz=66.7] run scoreboard players set #inside lr_tmp 1
execute positioned 902.93 58 1101.52 if entity @s[dx=65.9,dy=20,dz=65.9] run scoreboard players set #inside lr_tmp 1
execute positioned 914.46 58 1113.45 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 922.88 58 1127.75 if entity @s[dx=64.1,dy=20,dz=64.1] run scoreboard players set #inside lr_tmp 1
execute positioned 927.70 58 1143.59 if entity @s[dx=63.1,dy=20,dz=63.1] run scoreboard players set #inside lr_tmp 1
execute positioned 928.93 58 1160.03 if entity @s[dx=62.1,dy=20,dz=62.1] run scoreboard players set #inside lr_tmp 1
execute positioned 929.40 58 1176.50 if entity @s[dx=61.2,dy=20,dz=61.2] run scoreboard players set #inside lr_tmp 1
execute positioned 929.45 58 1178.56 if entity @s[dx=61.1,dy=20,dz=61.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
