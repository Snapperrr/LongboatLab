scoreboard players set #inside lr_tmp 0
execute positioned 991.20 81 898.36 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 991.31 81 882.23 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 993.54 82 866.30 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 998.05 82 850.90 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1004.70 83 836.33 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1013.16 83 822.76 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1023.11 84 810.24 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1034.27 85 798.79 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1046.51 85 788.48 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1059.77 86 779.48 if entity @s[dx=57.4,dy=20,dz=57.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1073.97 86 771.99 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1088.99 87 766.22 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1104.62 87 762.26 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1120.62 88 760.03 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1136.78 88 759.40 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1152.95 89 760.19 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1168.99 89 762.36 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1184.75 90 766.01 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1199.97 90 771.38 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1214.32 90 778.71 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1227.34 90 788.12 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1229.50 90 790.01 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
