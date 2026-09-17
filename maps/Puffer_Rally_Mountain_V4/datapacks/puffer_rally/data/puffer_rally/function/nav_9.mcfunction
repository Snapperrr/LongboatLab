scoreboard players set #inside lr_tmp 0
execute positioned 404.25 58 141.99 if entity @s[dx=71.5,dy=20,dz=71.5] run scoreboard players set #inside lr_tmp 1
execute positioned 404.35 58 125.73 if entity @s[dx=72.1,dy=20,dz=72.1] run scoreboard players set #inside lr_tmp 1
execute positioned 406.91 58 109.69 if entity @s[dx=72.7,dy=20,dz=72.7] run scoreboard players set #inside lr_tmp 1
execute positioned 412.17 58 94.39 if entity @s[dx=73.4,dy=20,dz=73.4] run scoreboard players set #inside lr_tmp 1
execute positioned 419.97 58 80.28 if entity @s[dx=74.1,dy=20,dz=74.1] run scoreboard players set #inside lr_tmp 1
execute positioned 430.05 58 67.82 if entity @s[dx=74.9,dy=20,dz=74.9] run scoreboard players set #inside lr_tmp 1
execute positioned 442.08 58 57.38 if entity @s[dx=75.7,dy=20,dz=75.7] run scoreboard players set #inside lr_tmp 1
execute positioned 455.67 58 49.29 if entity @s[dx=76.5,dy=20,dz=76.5] run scoreboard players set #inside lr_tmp 1
execute positioned 470.40 58 43.79 if entity @s[dx=77.4,dy=20,dz=77.4] run scoreboard players set #inside lr_tmp 1
execute positioned 485.78 58 41.04 if entity @s[dx=78.2,dy=20,dz=78.2] run scoreboard players set #inside lr_tmp 1
execute positioned 501.36 58 40.47 if entity @s[dx=79.1,dy=20,dz=79.1] run scoreboard players set #inside lr_tmp 1
execute positioned 516.94 58 40.05 if entity @s[dx=79.9,dy=20,dz=79.9] run scoreboard players set #inside lr_tmp 1
execute positioned 532.53 58 39.64 if entity @s[dx=80.7,dy=20,dz=80.7] run scoreboard players set #inside lr_tmp 1
execute positioned 548.13 58 39.24 if entity @s[dx=81.5,dy=20,dz=81.5] run scoreboard players set #inside lr_tmp 1
execute positioned 563.75 58 38.86 if entity @s[dx=82.3,dy=20,dz=82.3] run scoreboard players set #inside lr_tmp 1
execute positioned 579.40 58 38.51 if entity @s[dx=83.0,dy=20,dz=83.0] run scoreboard players set #inside lr_tmp 1
execute positioned 595.07 58 38.18 if entity @s[dx=83.6,dy=20,dz=83.6] run scoreboard players set #inside lr_tmp 1
execute positioned 610.77 58 37.88 if entity @s[dx=84.2,dy=20,dz=84.2] run scoreboard players set #inside lr_tmp 1
execute positioned 626.51 58 37.62 if entity @s[dx=84.8,dy=20,dz=84.8] run scoreboard players set #inside lr_tmp 1
execute positioned 638.34 58 37.45 if entity @s[dx=85.1,dy=20,dz=85.1] run scoreboard players set #inside lr_tmp 1
execute positioned 596 65 49 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
