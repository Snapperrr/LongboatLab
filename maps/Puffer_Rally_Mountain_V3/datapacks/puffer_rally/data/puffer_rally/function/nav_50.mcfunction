scoreboard players set #inside lr_tmp 0
execute positioned 2227.19 123 1753.09 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2218.76 123 1739.19 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2211.14 124 1724.86 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2204.67 124 1710.02 if entity @s[dx=57.4,dy=20,dz=57.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2199.68 125 1694.66 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2196.48 125 1678.89 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2195.29 126 1662.88 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2196.22 126 1646.90 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2199.26 127 1631.22 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2204.26 127 1616.07 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2210.99 128 1601.60 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2219.16 128 1587.91 if entity @s[dx=57.4,dy=20,dz=57.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2228.51 128 1574.97 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2238.80 129 1562.75 if entity @s[dx=56.8,dy=20,dz=56.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2249.85 129 1551.19 if entity @s[dx=56.4,dy=20,dz=56.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2261.52 130 1540.23 if entity @s[dx=56.1,dy=20,dz=56.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2273.74 130 1529.89 if entity @s[dx=55.7,dy=20,dz=55.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2286.50 131 1520.18 if entity @s[dx=55.3,dy=20,dz=55.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2299.79 131 1511.19 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2313.61 132 1503.04 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 2327.95 132 1495.81 if entity @s[dx=54.4,dy=20,dz=54.4] run scoreboard players set #inside lr_tmp 1
execute positioned 2342.75 133 1489.62 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2357.96 133 1484.51 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2373.47 134 1480.54 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2389.21 134 1477.72 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2405.08 135 1476.06 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 2420.98 135 1475.58 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 2436.84 136 1476.33 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2452.54 136 1478.38 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2454.49 136 1478.73 if entity @s[dx=54.8,dy=20,dz=54.8] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
