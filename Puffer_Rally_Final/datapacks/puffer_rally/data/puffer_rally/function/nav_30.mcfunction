scoreboard players set #inside lr_tmp 0
execute positioned 898.69 58 779.44 if entity @s[dx=64.4,dy=20,dz=64.4] run scoreboard players set #inside lr_tmp 1
execute positioned 902.53 58 763.44 if entity @s[dx=66.1,dy=20,dz=66.1] run scoreboard players set #inside lr_tmp 1
execute positioned 912.85 58 751.34 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 927.28 58 745.71 if entity @s[dx=68.8,dy=20,dz=68.8] run scoreboard players set #inside lr_tmp 1
execute positioned 942.87 58 745.18 if entity @s[dx=69.6,dy=20,dz=69.6] run scoreboard players set #inside lr_tmp 1
execute positioned 958.69 58 745.01 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 974.76 58 745.07 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 991.06 58 745.37 if entity @s[dx=69.3,dy=20,dz=69.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1007.58 58 745.89 if entity @s[dx=68.2,dy=20,dz=68.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1024.27 58 746.59 if entity @s[dx=66.8,dy=20,dz=66.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1041.10 58 747.41 if entity @s[dx=65.2,dy=20,dz=65.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1058.00 58 748.32 if entity @s[dx=63.4,dy=20,dz=63.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1074.92 58 749.23 if entity @s[dx=61.5,dy=20,dz=61.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1091.78 58 750.10 if entity @s[dx=59.8,dy=20,dz=59.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1093.89 58 750.20 if entity @s[dx=59.6,dy=20,dz=59.6] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
