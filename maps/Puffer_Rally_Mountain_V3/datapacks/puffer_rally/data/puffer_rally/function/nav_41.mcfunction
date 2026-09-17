scoreboard players set #inside lr_tmp 0
execute positioned 2575.27 63 1543.07 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2584.24 63 1556.07 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2592.02 64 1569.87 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2598.69 64 1584.30 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2604.39 65 1599.19 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2609.24 65 1614.43 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2613.36 66 1629.93 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2616.81 66 1645.63 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2619.57 67 1661.51 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2621.56 67 1677.53 if entity @s[dx=57.3,dy=20,dz=57.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2622.62 68 1693.66 if entity @s[dx=57.0,dy=20,dz=57.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2622.58 68 1709.83 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2621.28 69 1725.94 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2618.60 69 1741.87 if entity @s[dx=55.9,dy=20,dz=55.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2614.50 70 1757.47 if entity @s[dx=55.5,dy=20,dz=55.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2609.02 70 1772.61 if entity @s[dx=55.1,dy=20,dz=55.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2602.27 70 1787.20 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2594.39 71 1801.19 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2585.54 71 1814.55 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2575.84 72 1827.29 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2565.36 72 1839.38 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2554.10 73 1850.75 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2542.03 73 1861.25 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2529.12 74 1870.72 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2515.37 74 1878.94 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2500.82 75 1885.73 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2485.61 75 1890.96 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2469.90 76 1894.58 if entity @s[dx=55.3,dy=20,dz=55.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2453.88 76 1896.65 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2451.86 76 1896.80 if entity @s[dx=55.7,dy=20,dz=55.7] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
