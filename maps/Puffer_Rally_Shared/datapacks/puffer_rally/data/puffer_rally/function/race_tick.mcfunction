scoreboard players add @a[tag=lr_racer,scores={lr_place=0}] lr_time 1
execute as @a[tag=lr_racer,scores={lr_cp=0,lr_place=0},x=-22,y=60,z=173,dx=44,dy=25,dz=14] at @s run function puffer_rally:cp_1
execute as @a[tag=lr_racer,scores={lr_cp=1,lr_place=0},x=-22,y=60,z=393,dx=44,dy=25,dz=14] at @s run function puffer_rally:cp_2
execute as @a[tag=lr_racer,scores={lr_cp=2,lr_place=0},x=4,y=60,z=553,dx=42,dy=25,dz=42] at @s run function puffer_rally:cp_3
execute as @a[tag=lr_racer,scores={lr_cp=3,lr_place=0},x=211,y=60,z=578,dx=15,dy=25,dz=44] at @s run function puffer_rally:cp_4
execute as @a[tag=lr_racer,scores={lr_cp=4,lr_place=0},x=386,y=60,z=560,dx=39,dy=25,dz=45] at @s run function puffer_rally:cp_5
execute as @a[tag=lr_racer,scores={lr_cp=5,lr_place=0},x=418,y=60,z=420,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_6
execute as @a[tag=lr_racer,scores={lr_cp=6,lr_place=0},x=418,y=65,z=359,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_7
execute as @a[tag=lr_racer,scores={lr_cp=7,lr_place=0},x=418,y=60,z=305,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_8
execute as @a[tag=lr_racer,scores={lr_cp=8,lr_place=0},x=424,y=60,z=117,dx=47,dy=25,dz=31] at @s run function puffer_rally:cp_9
execute as @a[tag=lr_racer,scores={lr_cp=9,lr_place=0},x=638,y=60,z=58,dx=15,dy=25,dz=44] at @s run function puffer_rally:cp_10
execute as @a[tag=lr_racer,scores={lr_cp=10,lr_place=0},x=842,y=60,z=98,dx=45,dy=25,dz=38] at @s run function puffer_rally:cp_11
execute as @a[tag=lr_racer,scores={lr_cp=11,lr_place=0},x=858,y=60,z=277,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_12
execute as @a[tag=lr_racer,scores={lr_cp=12,lr_place=0},x=858,y=60,z=497,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_13
execute as @a[tag=lr_racer,scores={lr_cp=13,lr_place=0},x=858,y=60,z=647,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_14
execute as @a[tag=lr_racer,scores={lr_cp=14,lr_place=0},x=850,y=60,z=850,dx=47,dy=25,dz=31] at @s run function puffer_rally:cp_15
execute as @a[tag=lr_racer,scores={lr_cp=15,lr_place=0},x=699,y=60,z=898,dx=15,dy=25,dz=44] at @s run function puffer_rally:cp_16
execute as @a[tag=lr_racer,scores={lr_cp=16,lr_place=0},x=479,y=60,z=898,dx=15,dy=25,dz=44] at @s run function puffer_rally:cp_17
execute as @a[tag=lr_racer,scores={lr_cp=17,lr_place=0},x=298,y=60,z=991,dx=45,dy=25,dz=19] at @s run function puffer_rally:cp_18
execute as @a[tag=lr_racer,scores={lr_cp=18,lr_place=0},x=298,y=60,z=1214,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_19
execute as @a[tag=lr_racer,scores={lr_cp=19,lr_place=0},x=302,y=60,z=1428,dx=47,dy=25,dz=29] at @s run function puffer_rally:cp_20
execute as @a[tag=lr_racer,scores={lr_cp=20,lr_place=0},x=503,y=60,z=1478,dx=15,dy=25,dz=44] at @s run function puffer_rally:cp_21
execute as @a[tag=lr_racer,scores={lr_cp=21,lr_place=0},x=723,y=60,z=1478,dx=15,dy=25,dz=44] at @s run function puffer_rally:cp_22
execute as @a[tag=lr_racer,scores={lr_cp=22,lr_place=0},x=965,y=60,z=1478,dx=14,dy=25,dz=44] at @s run function puffer_rally:cp_23
execute if score #pit0 lr_pit matches 0 if entity @a[tag=lr_racer,x=-30,y=45,z=365,dx=110,dy=55,dz=110] if loaded 25 64 420 run function puffer_rally:restock_0
execute if score #pit1 lr_pit matches 0 if entity @a[tag=lr_racer,x=541,y=45,z=0,dx=110,dy=55,dz=110] if loaded 596 64 55 run function puffer_rally:restock_1
execute if score #pit2 lr_pit matches 0 if entity @a[tag=lr_racer,x=850,y=45,z=610,dx=110,dy=55,dz=110] if loaded 905 64 665 run function puffer_rally:restock_2
execute if score #pit3 lr_pit matches 0 if entity @a[tag=lr_racer,x=455,y=45,z=1420,dx=110,dy=55,dz=110] if loaded 510 64 1475 run function puffer_rally:restock_3
execute as @a[tag=lr_racer,scores={lr_place=0},y=-64,dy=118] at @s run function puffer_rally:rescue
execute unless entity @a[tag=lr_racer,scores={lr_place=0}] run scoreboard players set #phase lr_state 3
