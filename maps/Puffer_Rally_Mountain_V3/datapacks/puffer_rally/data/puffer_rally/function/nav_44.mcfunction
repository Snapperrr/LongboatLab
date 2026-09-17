scoreboard players set #inside lr_tmp 0
execute positioned 2249.88 85 1734.13 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2246.83 85 1718.64 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2244.29 86 1703.04 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2242.49 86 1687.31 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2241.69 87 1671.46 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2242.14 87 1655.56 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2244.04 88 1639.73 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2247.48 88 1624.13 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2252.46 88 1608.92 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2258.87 89 1594.23 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2266.55 89 1580.16 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2275.35 90 1566.76 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2285.10 90 1554.05 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2295.69 91 1542.04 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2307.06 91 1530.79 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2319.17 92 1520.37 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2332.00 92 1510.89 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2339.53 93 1506.02 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
