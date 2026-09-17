scoreboard players set #inside lr_tmp 0
execute positioned 2267.45 74 1871.82 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2253.59 74 1879.87 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2238.95 75 1886.47 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2223.67 75 1891.50 if entity @s[dx=55.0,dy=20,dz=55.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2207.91 76 1894.92 if entity @s[dx=55.3,dy=20,dz=55.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2191.86 76 1896.80 if entity @s[dx=55.7,dy=20,dz=55.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2175.69 77 1897.27 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2159.51 77 1896.49 if entity @s[dx=56.5,dy=20,dz=56.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2143.42 78 1894.61 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2127.49 78 1891.69 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2111.81 79 1887.75 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2096.48 79 1882.69 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2081.64 79 1876.41 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2067.51 80 1868.78 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2054.29 80 1859.73 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2042.23 81 1849.28 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2031.50 81 1837.53 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2026.10 82 1830.43 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
