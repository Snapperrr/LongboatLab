scoreboard players set #inside lr_tmp 0
execute positioned 142.99 58 569.36 if entity @s[dx=61.3,dy=20,dz=61.3] run scoreboard players set #inside lr_tmp 1
execute positioned 158.61 58 568.98 if entity @s[dx=62.0,dy=20,dz=62.0] run scoreboard players set #inside lr_tmp 1
execute positioned 174.22 58 568.59 if entity @s[dx=62.8,dy=20,dz=62.8] run scoreboard players set #inside lr_tmp 1
execute positioned 189.84 58 568.21 if entity @s[dx=63.6,dy=20,dz=63.6] run scoreboard players set #inside lr_tmp 1
execute positioned 205.47 58 567.84 if entity @s[dx=64.3,dy=20,dz=64.3] run scoreboard players set #inside lr_tmp 1
execute positioned 221.10 58 567.48 if entity @s[dx=65.0,dy=20,dz=65.0] run scoreboard players set #inside lr_tmp 1
execute positioned 236.75 58 567.12 if entity @s[dx=65.8,dy=20,dz=65.8] run scoreboard players set #inside lr_tmp 1
execute positioned 252.42 58 566.79 if entity @s[dx=66.4,dy=20,dz=66.4] run scoreboard players set #inside lr_tmp 1
execute positioned 268.10 58 566.47 if entity @s[dx=67.1,dy=20,dz=67.1] run scoreboard players set #inside lr_tmp 1
execute positioned 283.81 58 566.18 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 299.54 58 565.91 if entity @s[dx=68.2,dy=20,dz=68.2] run scoreboard players set #inside lr_tmp 1
execute positioned 315.30 58 565.67 if entity @s[dx=68.7,dy=20,dz=68.7] run scoreboard players set #inside lr_tmp 1
execute positioned 331.01 58 564.11 if entity @s[dx=69.1,dy=20,dz=69.1] run scoreboard players set #inside lr_tmp 1
execute positioned 346.27 58 559.79 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 360.59 58 552.84 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 373.52 58 543.49 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 384.65 58 532.02 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 392.63 58 520.53 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 279 65 576 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
