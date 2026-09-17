scoreboard players set #inside lr_tmp 0
execute positioned 837.00 58 196.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 212.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 228.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 244.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 260.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 276.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 292.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 308.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 324.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 340.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 356.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 372.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 388.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 404.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 420.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 436.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 452.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 468.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 484.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 837.00 58 496.52 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
