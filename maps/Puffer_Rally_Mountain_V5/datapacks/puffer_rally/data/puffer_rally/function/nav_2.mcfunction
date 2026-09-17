scoreboard players set #inside lr_tmp 0
execute positioned -27.00 58 248.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 264.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 280.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 296.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 312.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 328.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 344.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 360.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 376.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 392.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 408.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 424.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 440.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 456.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -27.00 58 472.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -26.88 58 487.98 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned -24.65 58 503.72 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned -19.70 58 518.77 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned -12.19 58 532.64 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned -2.36 58 544.90 if entity @s[dx=55.1,dy=20,dz=55.1] run scoreboard players set #inside lr_tmp 1
execute positioned 9.49 58 555.16 if entity @s[dx=55.5,dy=20,dz=55.5] run scoreboard players set #inside lr_tmp 1
execute positioned 22.96 58 563.09 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 26.54 58 564.67 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
