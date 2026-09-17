scoreboard players set #inside lr_tmp 0
execute positioned 899.49 58 795.47 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 898.71 58 778.56 if entity @s[dx=64.5,dy=20,dz=64.5] run scoreboard players set #inside lr_tmp 1
execute positioned 902.92 58 762.68 if entity @s[dx=66.2,dy=20,dz=66.2] run scoreboard players set #inside lr_tmp 1
execute positioned 913.53 58 750.87 if entity @s[dx=67.7,dy=20,dz=67.7] run scoreboard players set #inside lr_tmp 1
execute positioned 928.09 58 745.62 if entity @s[dx=68.9,dy=20,dz=68.9] run scoreboard players set #inside lr_tmp 1
execute positioned 943.69 58 745.17 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 959.53 58 745.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 958.69 58 745.01 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 774.80 64 816.46 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 776.63 64 824.53 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 781.78 64 831.51 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 789.77 64 837.40 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 800.12 64 842.18 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 812.33 64 845.88 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 825.92 64 848.47 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 840.40 64 849.97 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 855.29 64 850.36 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 870.10 64 849.66 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 884.34 64 847.86 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 897.52 64 844.95 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 909.16 64 840.94 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 918.78 64 835.83 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 925.88 64 829.61 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute positioned 929.98 64 822.28 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
