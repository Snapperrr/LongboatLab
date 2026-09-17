scoreboard players set #inside lr_tmp 0
execute positioned 2375.43 134 1480.12 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2391.19 134 1477.45 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2407.06 135 1475.93 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2422.97 135 1475.60 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2438.82 136 1476.52 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2454.49 136 1478.73 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2469.87 137 1482.32 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2484.82 137 1487.32 if entity @s[dx=55.5,dy=20,dz=55.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2499.19 137 1493.74 if entity @s[dx=55.9,dy=20,dz=55.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2512.86 138 1501.52 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2525.78 138 1510.51 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2537.93 139 1520.52 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2549.43 139 1531.32 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2560.41 140 1542.70 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2571.06 140 1554.46 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2581.48 141 1566.47 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2591.73 141 1578.70 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2601.73 142 1591.20 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2611.32 142 1604.09 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2618.83 143 1615.24 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
