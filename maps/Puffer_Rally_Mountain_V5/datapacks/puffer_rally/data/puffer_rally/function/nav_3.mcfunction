scoreboard players set #inside lr_tmp 0
execute positioned -23.66 58 507.57 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned -18.05 58 522.36 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned -9.94 58 535.87 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute positioned 0.43 58 547.67 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 12.72 58 557.37 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 26.54 58 564.67 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 41.44 58 569.34 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 56.96 58 571.22 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 72.63 58 571.00 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 88.29 58 570.66 if entity @s[dx=58.7,dy=20,dz=58.7] run scoreboard players set #inside lr_tmp 1
execute positioned 103.93 58 570.30 if entity @s[dx=59.4,dy=20,dz=59.4] run scoreboard players set #inside lr_tmp 1
execute positioned 119.56 58 569.93 if entity @s[dx=60.1,dy=20,dz=60.1] run scoreboard players set #inside lr_tmp 1
execute positioned 135.18 58 569.55 if entity @s[dx=60.9,dy=20,dz=60.9] run scoreboard players set #inside lr_tmp 1
execute positioned 150.80 58 569.17 if entity @s[dx=61.7,dy=20,dz=61.7] run scoreboard players set #inside lr_tmp 1
execute positioned 166.41 58 568.78 if entity @s[dx=62.4,dy=20,dz=62.4] run scoreboard players set #inside lr_tmp 1
execute positioned 182.03 58 568.40 if entity @s[dx=63.2,dy=20,dz=63.2] run scoreboard players set #inside lr_tmp 1
execute positioned 197.65 58 568.02 if entity @s[dx=64.0,dy=20,dz=64.0] run scoreboard players set #inside lr_tmp 1
execute positioned 213.28 58 567.66 if entity @s[dx=64.7,dy=20,dz=64.7] run scoreboard players set #inside lr_tmp 1
execute positioned 221.10 58 567.48 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 279 65 576 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
