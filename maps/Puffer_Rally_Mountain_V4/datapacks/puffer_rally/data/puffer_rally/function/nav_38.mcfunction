scoreboard players set #inside lr_tmp 0
execute positioned 2088.06 79 1879.32 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 2073.59 80 1872.29 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2059.95 80 1863.86 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2047.35 81 1854.02 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 2036.02 81 1842.82 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 2026.10 82 1830.43 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 2017.61 82 1817.05 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 2010.50 83 1802.93 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 2004.59 83 1788.30 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1999.67 84 1773.32 if entity @s[dx=56.6,dy=20,dz=56.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1995.46 84 1758.12 if entity @s[dx=56.2,dy=20,dz=56.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1991.78 84 1742.79 if entity @s[dx=55.8,dy=20,dz=55.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1988.49 85 1727.37 if entity @s[dx=55.4,dy=20,dz=55.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1985.65 85 1711.83 if entity @s[dx=55.0,dy=20,dz=55.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1983.40 86 1696.18 if entity @s[dx=54.7,dy=20,dz=54.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1982.00 86 1680.39 if entity @s[dx=54.5,dy=20,dz=54.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1981.72 87 1664.51 if entity @s[dx=54.2,dy=20,dz=54.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1982.14 87 1655.56 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
