scoreboard players set #inside lr_tmp 0
execute positioned 594.67 58 642.70 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 599.55 58 628.00 if entity @s[dx=68.0,dy=20,dz=68.0] run scoreboard players set #inside lr_tmp 1
execute positioned 611.07 58 617.12 if entity @s[dx=66.6,dy=20,dz=66.6] run scoreboard players set #inside lr_tmp 1
execute positioned 626.81 58 612.56 if entity @s[dx=64.9,dy=20,dz=64.9] run scoreboard players set #inside lr_tmp 1
execute positioned 643.71 58 613.26 if entity @s[dx=63.1,dy=20,dz=63.1] run scoreboard players set #inside lr_tmp 1
execute positioned 660.63 58 614.17 if entity @s[dx=61.3,dy=20,dz=61.3] run scoreboard players set #inside lr_tmp 1
execute positioned 677.48 58 615.02 if entity @s[dx=59.6,dy=20,dz=59.6] run scoreboard players set #inside lr_tmp 1
execute positioned 694.21 58 615.76 if entity @s[dx=58.1,dy=20,dz=58.1] run scoreboard players set #inside lr_tmp 1
execute positioned 710.77 58 616.32 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 726.71 58 619.64 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 739.37 58 629.46 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 745.99 58 643.69 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 746.33 58 659.30 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 745.78 58 674.74 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 745.69 58 676.65 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
