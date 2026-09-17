scoreboard players set #inside lr_tmp 0
execute positioned 1106.60 58 753.00 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1122.55 58 753.83 if entity @s[dx=54.0,dy=20,dz=54.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1138.31 59 756.14 if entity @s[dx=54.1,dy=20,dz=54.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1153.78 59 759.78 if entity @s[dx=54.3,dy=20,dz=54.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1168.82 60 764.74 if entity @s[dx=54.6,dy=20,dz=54.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1183.25 60 771.17 if entity @s[dx=54.9,dy=20,dz=54.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1196.79 61 779.24 if entity @s[dx=55.2,dy=20,dz=55.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1209.11 61 789.01 if entity @s[dx=55.6,dy=20,dz=55.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1219.94 62 800.40 if entity @s[dx=56.0,dy=20,dz=56.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1229.12 62 813.16 if entity @s[dx=56.3,dy=20,dz=56.3] run scoreboard players set #inside lr_tmp 1
execute positioned 1236.67 63 826.97 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1242.78 63 841.50 if entity @s[dx=57.1,dy=20,dz=57.1] run scoreboard players set #inside lr_tmp 1
execute positioned 1247.70 64 856.52 if entity @s[dx=57.4,dy=20,dz=57.4] run scoreboard players set #inside lr_tmp 1
execute positioned 1251.66 64 871.86 if entity @s[dx=57.6,dy=20,dz=57.6] run scoreboard players set #inside lr_tmp 1
execute positioned 1254.71 65 887.45 if entity @s[dx=57.8,dy=20,dz=57.8] run scoreboard players set #inside lr_tmp 1
execute positioned 1256.72 66 903.25 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1257.40 66 919.20 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1256.40 67 935.17 if entity @s[dx=58.0,dy=20,dz=58.0] run scoreboard players set #inside lr_tmp 1
execute positioned 1253.50 67 950.93 if entity @s[dx=57.9,dy=20,dz=57.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1248.64 68 966.22 if entity @s[dx=57.7,dy=20,dz=57.7] run scoreboard players set #inside lr_tmp 1
execute positioned 1241.98 68 980.82 if entity @s[dx=57.5,dy=20,dz=57.5] run scoreboard players set #inside lr_tmp 1
execute positioned 1233.80 69 994.62 if entity @s[dx=57.2,dy=20,dz=57.2] run scoreboard players set #inside lr_tmp 1
execute positioned 1224.39 69 1007.60 if entity @s[dx=56.9,dy=20,dz=56.9] run scoreboard players set #inside lr_tmp 1
execute positioned 1219.28 70 1013.77 if entity @s[dx=56.7,dy=20,dz=56.7] run scoreboard players set #inside lr_tmp 1
execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0
execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1
execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course
