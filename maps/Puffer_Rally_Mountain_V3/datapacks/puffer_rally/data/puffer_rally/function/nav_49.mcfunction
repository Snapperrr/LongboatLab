scoreboard players set #inside lr_tmp 0
execute positioned 2356.25 117 1882.49 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2341.50 118 1876.54 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2327.38 118 1869.13 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2314.04 119 1860.29 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2301.60 119 1850.14 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2290.08 120 1838.89 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2279.41 120 1826.77 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2269.40 121 1814.04 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2259.83 121 1800.94 if entity @s[dx=55.1,dy=20,dz=55.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2250.48 122 1787.65 if entity @s[dx=55.4,dy=20,dz=55.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2241.24 122 1774.26 if entity @s[dx=55.8,dy=20,dz=55.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2232.16 123 1760.77 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2223.42 123 1747.05 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2215.31 123 1732.98 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2208.15 124 1718.43 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2202.29 124 1703.36 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2198.05 125 1687.80 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2196.48 125 1678.89 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
