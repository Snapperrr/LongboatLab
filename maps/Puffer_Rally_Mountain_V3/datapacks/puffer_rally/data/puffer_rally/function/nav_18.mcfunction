scoreboard players set #inside lr_tmp 0
execute positioned 292.29 58 916.82 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 284.59 58 930.83 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 279.49 58 945.97 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.15 58 961.78 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 977.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 993.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1009.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1025.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1041.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1057.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1073.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1089.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1105.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1121.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1137.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 277.00 58 1143.77 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 353 65 1182 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
