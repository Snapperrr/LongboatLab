scoreboard players set #inside lr_tmp 0
execute positioned 611.14 58 1237.84 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 610.81 58 1221.50 if entity @s[dx=58.4,dy=20,dz=58.4] run scoreboard players set #inside lr_tmp 1
execute positioned 610.42 58 1205.12 if entity @s[dx=59.2,dy=20,dz=59.2] run scoreboard players set #inside lr_tmp 1
execute positioned 610.01 58 1188.71 if entity @s[dx=60.0,dy=20,dz=60.0] run scoreboard players set #inside lr_tmp 1
execute positioned 609.56 58 1172.26 if entity @s[dx=60.9,dy=20,dz=60.9] run scoreboard players set #inside lr_tmp 1
execute positioned 609.10 58 1155.80 if entity @s[dx=61.8,dy=20,dz=61.8] run scoreboard players set #inside lr_tmp 1
execute positioned 610.19 58 1139.44 if entity @s[dx=62.8,dy=20,dz=62.8] run scoreboard players set #inside lr_tmp 1
execute positioned 615.08 58 1123.93 if entity @s[dx=63.7,dy=20,dz=63.7] run scoreboard players set #inside lr_tmp 1
execute positioned 623.48 58 1110.19 if entity @s[dx=64.7,dy=20,dz=64.7] run scoreboard players set #inside lr_tmp 1
execute positioned 634.86 58 1099.03 if entity @s[dx=65.6,dy=20,dz=65.6] run scoreboard players set #inside lr_tmp 1
execute positioned 648.52 58 1091.09 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 663.61 58 1086.85 if entity @s[dx=67.2,dy=20,dz=67.2] run scoreboard players set #inside lr_tmp 1
execute positioned 679.23 58 1086.03 if entity @s[dx=67.9,dy=20,dz=67.9] run scoreboard players set #inside lr_tmp 1
execute positioned 694.91 58 1085.71 if entity @s[dx=68.6,dy=20,dz=68.6] run scoreboard players set #inside lr_tmp 1
execute positioned 710.65 58 1085.45 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 726.45 58 1085.24 if entity @s[dx=69.5,dy=20,dz=69.5] run scoreboard players set #inside lr_tmp 1
execute positioned 742.30 58 1085.10 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 758.22 58 1085.02 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 774.20 58 1085.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 776.21 58 1085.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
