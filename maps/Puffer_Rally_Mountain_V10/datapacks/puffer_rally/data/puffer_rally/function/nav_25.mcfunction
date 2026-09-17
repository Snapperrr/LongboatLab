scoreboard players set #inside lr_tmp 0
execute positioned 597.90 58 775.96 if entity @s[dx=62.6,dy=20,dz=62.6] run scoreboard players set #inside lr_tmp 1
execute positioned 596.99 58 759.04 if entity @s[dx=64.4,dy=20,dz=64.4] run scoreboard players set #inside lr_tmp 1
execute positioned 596.12 58 742.18 if entity @s[dx=66.2,dy=20,dz=66.2] run scoreboard players set #inside lr_tmp 1
execute positioned 595.36 58 725.42 if entity @s[dx=67.7,dy=20,dz=67.7] run scoreboard players set #inside lr_tmp 1
execute positioned 594.77 58 708.82 if entity @s[dx=68.9,dy=20,dz=68.9] run scoreboard players set #inside lr_tmp 1
execute positioned 594.37 58 692.43 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 594.20 58 676.26 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 594.28 58 660.33 if entity @s[dx=69.8,dy=20,dz=69.8] run scoreboard players set #inside lr_tmp 1
execute positioned 594.59 58 644.64 if entity @s[dx=69.2,dy=20,dz=69.2] run scoreboard players set #inside lr_tmp 1
execute positioned 598.54 58 629.69 if entity @s[dx=68.2,dy=20,dz=68.2] run scoreboard players set #inside lr_tmp 1
execute positioned 609.33 58 618.17 if entity @s[dx=66.8,dy=20,dz=66.8] run scoreboard players set #inside lr_tmp 1
execute positioned 624.72 58 612.73 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 641.60 58 613.15 if entity @s[dx=63.3,dy=20,dz=63.3] run scoreboard players set #inside lr_tmp 1
execute positioned 658.51 58 614.06 if entity @s[dx=61.5,dy=20,dz=61.5] run scoreboard players set #inside lr_tmp 1
execute positioned 660.63 58 614.17 if entity @s[dx=61.3,dy=20,dz=61.3] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
