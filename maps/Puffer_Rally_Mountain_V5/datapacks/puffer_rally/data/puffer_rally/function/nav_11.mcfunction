scoreboard players set #inside lr_tmp 0
execute positioned 786.50 58 46.13 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 800.17 58 54.39 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 812.18 58 64.94 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 822.12 58 77.44 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 829.70 58 91.51 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 834.68 58 106.69 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 836.89 58 122.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 138.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 154.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 170.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 186.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 202.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 218.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 234.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 250.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 266.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 276.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
