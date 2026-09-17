scoreboard players add @a[tag=lr_racer,scores={lr_place=0}] lr_time 1
execute as @a[tag=lr_racer,scores={lr_cp=0,lr_place=0},x=-14,y=60,z=113,dx=28,dy=25,dz=14] at @s run function puffer_rally:cp_1
execute as @a[tag=lr_racer,scores={lr_cp=1,lr_place=0},x=-14,y=60,z=313,dx=28,dy=25,dz=14] at @s run function puffer_rally:cp_2
execute as @a[tag=lr_racer,scores={lr_cp=2,lr_place=0},x=9,y=60,z=558,dx=32,dy=25,dz=32] at @s run function puffer_rally:cp_3
execute as @a[tag=lr_racer,scores={lr_cp=3,lr_place=0},x=211,y=60,z=581,dx=15,dy=25,dz=38] at @s run function puffer_rally:cp_4
execute as @a[tag=lr_racer,scores={lr_cp=4,lr_place=0},x=386,y=60,z=560,dx=39,dy=25,dz=45] at @s run function puffer_rally:cp_5
execute as @a[tag=lr_racer,scores={lr_cp=5,lr_place=0},x=418,y=60,z=420,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_6
execute as @a[tag=lr_racer,scores={lr_cp=6,lr_place=0},x=418,y=65,z=359,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_7
execute as @a[tag=lr_racer,scores={lr_cp=7,lr_place=0},x=418,y=60,z=305,dx=44,dy=25,dz=15] at @s run function puffer_rally:cp_8
execute as @a[tag=lr_racer,scores={lr_cp=8,lr_place=0},x=422,y=60,z=116,dx=50,dy=25,dz=33] at @s run function puffer_rally:cp_9
execute as @a[tag=lr_racer,scores={lr_cp=9,lr_place=0},x=638,y=60,z=51,dx=15,dy=25,dz=58] at @s run function puffer_rally:cp_10
execute as @a[tag=lr_racer,scores={lr_cp=10,lr_place=0},x=835,y=60,z=93,dx=59,dy=25,dz=47] at @s run function puffer_rally:cp_11
execute as @a[tag=lr_racer,scores={lr_cp=11,lr_place=0},x=850,y=60,z=277,dx=60,dy=25,dz=15] at @s run function puffer_rally:cp_12
execute as @a[tag=lr_racer,scores={lr_cp=12,lr_place=0},x=850,y=60,z=497,dx=60,dy=25,dz=15] at @s run function puffer_rally:cp_13
execute as @a[tag=lr_racer,scores={lr_cp=13,lr_place=0},x=850,y=60,z=647,dx=60,dy=25,dz=15] at @s run function puffer_rally:cp_14
execute as @a[tag=lr_racer,scores={lr_cp=14,lr_place=0},x=843,y=60,z=847,dx=62,dy=25,dz=37] at @s run function puffer_rally:cp_15
execute as @a[tag=lr_racer,scores={lr_cp=15,lr_place=0},x=749,y=60,z=890,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_16
execute as @a[tag=lr_racer,scores={lr_cp=16,lr_place=0},x=479,y=60,z=890,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_17
execute as @a[tag=lr_racer,scores={lr_cp=17,lr_place=0},x=290,y=60,z=989,dx=61,dy=25,dz=21] at @s run function puffer_rally:cp_18
execute as @a[tag=lr_racer,scores={lr_cp=18,lr_place=0},x=290,y=60,z=1144,dx=60,dy=25,dz=15] at @s run function puffer_rally:cp_19
execute as @a[tag=lr_racer,scores={lr_cp=19,lr_place=0},x=294,y=60,z=1426,dx=62,dy=25,dz=34] at @s run function puffer_rally:cp_20
execute as @a[tag=lr_racer,scores={lr_cp=20,lr_place=0},x=503,y=60,z=1470,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_21
execute as @a[tag=lr_racer,scores={lr_cp=21,lr_place=0},x=723,y=60,z=1470,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_22
execute as @a[tag=lr_racer,scores={lr_cp=22,lr_place=0},x=983,y=60,z=1470,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_23
execute as @a[tag=lr_racer,scores={lr_cp=23,lr_place=0},x=1253,y=60,z=1470,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_24
execute as @a[tag=lr_racer,scores={lr_cp=24,lr_place=0},x=1523,y=60,z=1470,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_25
execute as @a[tag=lr_racer,scores={lr_cp=25,lr_place=0},x=1763,y=60,z=1470,dx=15,dy=25,dz=60] at @s run function puffer_rally:cp_26
execute as @a[tag=lr_racer,scores={lr_cp=26,lr_place=0},x=1965,y=60,z=1470,dx=14,dy=25,dz=60] at @s run function puffer_rally:cp_27
execute if score #pit0 lr_pit matches 0 if entity @a[tag=lr_racer,x=-38,y=45,z=85,dx=110,dy=55,dz=110] if loaded 17 64 140 run function puffer_rally:restock_0
execute if score #pit1 lr_pit matches 0 if entity @a[tag=lr_racer,x=224,y=45,z=521,dx=110,dy=55,dz=110] if loaded 279 64 576 run function puffer_rally:restock_1
execute if score #pit2 lr_pit matches 0 if entity @a[tag=lr_racer,x=541,y=45,z=-6,dx=110,dy=55,dz=110] if loaded 596 64 49 run function puffer_rally:restock_2
execute if score #pit3 lr_pit matches 0 if entity @a[tag=lr_racer,x=298,y=45,z=1127,dx=110,dy=55,dz=110] if loaded 353 64 1182 run function puffer_rally:restock_3
execute as @a[tag=lr_racer,scores={lr_place=0},y=-64,dy=118] at @s run function puffer_rally:rescue
execute unless entity @a[tag=lr_racer,scores={lr_place=0}] run scoreboard players set #phase lr_state 3
