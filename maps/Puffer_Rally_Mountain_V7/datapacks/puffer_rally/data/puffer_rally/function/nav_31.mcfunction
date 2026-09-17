scoreboard players set #inside lr_tmp 0
execute positioned 1009.66 58 745.97 if entity @s[dx=68.1,dy=20,dz=68.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1026.37 58 746.68 if entity @s[dx=66.6,dy=20,dz=66.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1043.21 58 747.52 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1060.12 58 748.43 if entity @s[dx=63.1,dy=20,dz=63.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1077.03 58 749.34 if entity @s[dx=61.3,dy=20,dz=61.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1093.89 58 750.20 if entity @s[dx=59.6,dy=20,dz=59.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1112.68 58 753.12 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1128.58 58 754.55 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1144.24 59 757.37 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1159.56 59 761.50 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1174.40 60 767.00 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1183.25 60 771.17 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
