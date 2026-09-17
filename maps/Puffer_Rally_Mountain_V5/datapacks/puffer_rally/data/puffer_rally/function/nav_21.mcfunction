scoreboard players set #inside lr_tmp 0
execute positioned 470.53 58 1385.12 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 486.68 58 1385.28 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 502.90 58 1385.50 if entity @s[dx=69.0,dy=20,dz=69.0] run scoreboard players set #inside lr_tmp 1
execute positioned 519.18 58 1385.77 if entity @s[dx=68.5,dy=20,dz=68.5] run scoreboard players set #inside lr_tmp 1
execute positioned 535.50 58 1386.10 if entity @s[dx=67.8,dy=20,dz=67.8] run scoreboard players set #inside lr_tmp 1
execute positioned 551.87 58 1386.47 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute positioned 568.28 58 1386.87 if entity @s[dx=66.3,dy=20,dz=66.3] run scoreboard players set #inside lr_tmp 1
execute positioned 584.71 58 1387.31 if entity @s[dx=65.4,dy=20,dz=65.4] run scoreboard players set #inside lr_tmp 1
execute positioned 601.17 58 1387.77 if entity @s[dx=64.5,dy=20,dz=64.5] run scoreboard players set #inside lr_tmp 1
execute positioned 617.65 58 1388.24 if entity @s[dx=63.5,dy=20,dz=63.5] run scoreboard players set #inside lr_tmp 1
execute positioned 634.13 58 1388.72 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 650.60 58 1389.20 if entity @s[dx=61.6,dy=20,dz=61.6] run scoreboard players set #inside lr_tmp 1
execute positioned 667.06 58 1389.66 if entity @s[dx=60.7,dy=20,dz=60.7] run scoreboard players set #inside lr_tmp 1
execute positioned 683.50 58 1390.10 if entity @s[dx=59.8,dy=20,dz=59.8] run scoreboard players set #inside lr_tmp 1
execute positioned 699.91 58 1390.51 if entity @s[dx=59.0,dy=20,dz=59.0] run scoreboard players set #inside lr_tmp 1
execute positioned 716.28 58 1390.88 if entity @s[dx=58.2,dy=20,dz=58.2] run scoreboard players set #inside lr_tmp 1
execute positioned 732.61 58 1391.21 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 748.89 58 1391.48 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 765.11 58 1391.71 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 767.13 58 1391.73 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
