scoreboard players set #inside lr_tmp 0
execute positioned 329.06 58 564.45 if entity @s[dx=69.0,dy=20,dz=69.0] run scoreboard players set #inside lr_tmp 1
execute positioned 344.40 58 560.47 if entity @s[dx=69.4,dy=20,dz=69.4] run scoreboard players set #inside lr_tmp 1
execute positioned 358.87 58 553.85 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 371.99 58 544.78 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 383.37 58 533.56 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 392.63 58 520.53 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 399.46 58 506.09 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 403.63 58 490.66 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 474.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 458.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 442.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 426.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 410.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 394.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 378.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 362.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 405.00 58 357.74 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
