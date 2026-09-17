scoreboard players set #inside lr_tmp 0
execute positioned 712.83 58 616.38 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 728.54 58 620.54 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 740.57 58 631.06 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 746.31 58 645.63 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 746.27 58 661.24 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 745.69 58 676.65 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 744.95 58 691.91 if entity @s[dx=59.7,dy=20,dz=59.7] run scoreboard players set #inside lr_tmp 1
execute positioned 744.09 58 707.05 if entity @s[dx=61.4,dy=20,dz=61.4] run scoreboard players set #inside lr_tmp 1
execute positioned 743.17 58 722.14 if entity @s[dx=63.3,dy=20,dz=63.3] run scoreboard players set #inside lr_tmp 1
execute positioned 742.27 58 737.23 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 741.43 58 752.40 if entity @s[dx=66.7,dy=20,dz=66.7] run scoreboard players set #inside lr_tmp 1
execute positioned 740.73 58 767.69 if entity @s[dx=68.1,dy=20,dz=68.1] run scoreboard players set #inside lr_tmp 1
execute positioned 740.24 58 781.90 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
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
