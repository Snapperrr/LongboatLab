scoreboard players set #inside lr_tmp 0
execute positioned 2208.57 58 1468.37 if entity @s[dx=63.3,dy=20,dz=63.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2224.09 58 1467.90 if entity @s[dx=64.2,dy=20,dz=64.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2239.63 58 1467.43 if entity @s[dx=65.1,dy=20,dz=65.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2255.18 58 1466.99 if entity @s[dx=66.0,dy=20,dz=66.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2270.77 58 1466.57 if entity @s[dx=66.9,dy=20,dz=66.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2286.39 58 1466.19 if entity @s[dx=67.6,dy=20,dz=67.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2302.05 58 1465.86 if entity @s[dx=68.3,dy=20,dz=68.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2317.76 58 1465.57 if entity @s[dx=68.9,dy=20,dz=68.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2333.53 58 1465.33 if entity @s[dx=69.3,dy=20,dz=69.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2349.35 58 1465.16 if entity @s[dx=69.7,dy=20,dz=69.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2365.24 58 1465.05 if entity @s[dx=69.9,dy=20,dz=69.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2381.20 58 1465.00 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2397.21 58 1465.02 if entity @s[dx=70.0,dy=20,dz=70.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2421.19 58 1473.16 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2437.10 58 1474.31 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2452.87 59 1476.48 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2468.43 59 1479.58 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2483.75 60 1483.57 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2491.12 60 1485.86 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
