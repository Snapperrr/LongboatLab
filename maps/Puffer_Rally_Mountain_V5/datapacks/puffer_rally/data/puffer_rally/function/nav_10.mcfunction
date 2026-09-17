scoreboard players set #inside lr_tmp 0
execute positioned 559.84 58 38.96 if entity @s[dx=82.1,dy=20,dz=82.1] run scoreboard players set #inside lr_tmp 1
execute positioned 575.48 58 38.60 if entity @s[dx=82.8,dy=20,dz=82.8] run scoreboard players set #inside lr_tmp 1
execute positioned 591.15 58 38.26 if entity @s[dx=83.5,dy=20,dz=83.5] run scoreboard players set #inside lr_tmp 1
execute positioned 606.84 58 37.95 if entity @s[dx=84.1,dy=20,dz=84.1] run scoreboard players set #inside lr_tmp 1
execute positioned 622.57 58 37.68 if entity @s[dx=84.6,dy=20,dz=84.6] run scoreboard players set #inside lr_tmp 1
execute positioned 638.34 58 37.45 if entity @s[dx=85.1,dy=20,dz=85.1] run scoreboard players set #inside lr_tmp 1
execute positioned 654.15 58 37.26 if entity @s[dx=85.5,dy=20,dz=85.5] run scoreboard players set #inside lr_tmp 1
execute positioned 670.01 58 37.12 if entity @s[dx=85.8,dy=20,dz=85.8] run scoreboard players set #inside lr_tmp 1
execute positioned 685.92 58 37.03 if entity @s[dx=85.9,dy=20,dz=85.9] run scoreboard players set #inside lr_tmp 1
execute positioned 701.89 58 37.00 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 717.89 58 37.00 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 733.89 58 37.00 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 749.89 58 37.05 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 765.75 58 38.97 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 781.02 58 43.68 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 795.22 58 51.01 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 807.90 58 60.73 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 818.66 58 72.55 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 827.16 58 86.08 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 833.13 58 100.90 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 834.68 58 106.69 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 596 65 49 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
