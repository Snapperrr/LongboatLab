scoreboard players set #inside lr_tmp 0
execute positioned 123.40 58 510.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 526.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 542.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 558.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 574.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 590.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 606.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 123.40 58 622.93 if entity @s[dx=86.0,dy=20,dz=86.0] run scoreboard players set #inside lr_tmp 1
execute positioned 124.08 58 639.61 if entity @s[dx=84.6,dy=20,dz=84.6] run scoreboard players set #inside lr_tmp 1
execute positioned 126.47 58 657.55 if entity @s[dx=80.7,dy=20,dz=80.7] run scoreboard players set #inside lr_tmp 1
execute positioned 132.93 58 674.39 if entity @s[dx=77.4,dy=20,dz=77.4] run scoreboard players set #inside lr_tmp 1
execute positioned 143.90 58 688.44 if entity @s[dx=74.7,dy=20,dz=74.7] run scoreboard players set #inside lr_tmp 1
execute positioned 158.35 58 698.32 if entity @s[dx=72.4,dy=20,dz=72.4] run scoreboard players set #inside lr_tmp 1
execute positioned 174.84 58 703.11 if entity @s[dx=70.4,dy=20,dz=70.4] run scoreboard players set #inside lr_tmp 1
execute positioned 176.96 58 703.33 if entity @s[dx=70.1,dy=20,dz=70.1] run scoreboard players set #inside lr_tmp 1
execute positioned 199 65 615 if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
