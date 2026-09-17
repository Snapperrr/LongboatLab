scoreboard players set #inside lr_tmp 0
execute positioned 2362.01 68 1718.91 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2359.94 69 1734.94 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2356.47 69 1750.70 if entity @s[dx=55.7,dy=20,dz=55.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2351.58 70 1766.05 if entity @s[dx=55.3,dy=20,dz=55.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2345.37 70 1780.89 if entity @s[dx=55.0,dy=20,dz=55.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2337.96 71 1795.14 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2329.52 71 1808.78 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2320.19 72 1821.80 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2310.04 72 1834.18 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2299.12 73 1845.87 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2287.41 73 1856.77 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2274.88 74 1866.72 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2261.49 74 1875.51 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2247.28 75 1882.95 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2232.34 75 1888.87 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2216.82 75 1893.19 if entity @s[dx=55.1,dy=20,dz=55.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2200.92 76 1895.93 if entity @s[dx=55.5,dy=20,dz=55.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2191.86 76 1896.80 if entity @s[dx=55.7,dy=20,dz=55.7] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
