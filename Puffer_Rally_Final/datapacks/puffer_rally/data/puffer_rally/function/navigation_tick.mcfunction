execute if score #phase lr_state matches 4 run return 0
scoreboard players add #nav lr_navclock 1
execute unless score #nav lr_navclock matches 10.. run return 0
scoreboard players set #nav lr_navclock 0
scoreboard players remove @a[scores={lr_warn=1..}] lr_warn 1
execute as @a[tag=lr_racer,scores={lr_cp=0,lr_place=0}] at @s run function puffer_rally:nav_0
execute as @a[tag=lr_racer,scores={lr_cp=1,lr_place=0}] at @s run function puffer_rally:nav_1
execute as @a[tag=lr_racer,scores={lr_cp=2,lr_place=0}] at @s run function puffer_rally:nav_2
execute as @a[tag=lr_racer,scores={lr_cp=3,lr_place=0}] at @s run function puffer_rally:nav_3
execute as @a[tag=lr_racer,scores={lr_cp=4,lr_place=0}] at @s run function puffer_rally:nav_4
execute as @a[tag=lr_racer,scores={lr_cp=5,lr_place=0}] at @s run function puffer_rally:nav_5
execute as @a[tag=lr_racer,scores={lr_cp=6,lr_place=0}] at @s run function puffer_rally:nav_6
execute as @a[tag=lr_racer,scores={lr_cp=7,lr_place=0}] at @s run function puffer_rally:nav_7
execute as @a[tag=lr_racer,scores={lr_cp=8,lr_place=0}] at @s run function puffer_rally:nav_8
execute as @a[tag=lr_racer,scores={lr_cp=9,lr_place=0}] at @s run function puffer_rally:nav_9
execute as @a[tag=lr_racer,scores={lr_cp=10,lr_place=0}] at @s run function puffer_rally:nav_10
execute as @a[tag=lr_racer,scores={lr_cp=11,lr_place=0}] at @s run function puffer_rally:nav_11
execute as @a[tag=lr_racer,scores={lr_cp=12,lr_place=0}] at @s run function puffer_rally:nav_12
execute as @a[tag=lr_racer,scores={lr_cp=13,lr_place=0}] at @s run function puffer_rally:nav_13
execute as @a[tag=lr_racer,scores={lr_cp=14,lr_place=0}] at @s run function puffer_rally:nav_14
execute as @a[tag=lr_racer,scores={lr_cp=15,lr_place=0}] at @s run function puffer_rally:nav_15
execute as @a[tag=lr_racer,scores={lr_cp=16,lr_place=0}] at @s run function puffer_rally:nav_16
execute as @a[tag=lr_racer,scores={lr_cp=17,lr_place=0}] at @s run function puffer_rally:nav_17
execute as @a[tag=lr_racer,scores={lr_cp=18,lr_place=0}] at @s run function puffer_rally:nav_18
execute as @a[tag=lr_racer,scores={lr_cp=19,lr_place=0}] at @s run function puffer_rally:nav_19
execute as @a[tag=lr_racer,scores={lr_cp=20,lr_place=0}] at @s run function puffer_rally:nav_20
execute as @a[tag=lr_racer,scores={lr_cp=21,lr_place=0}] at @s run function puffer_rally:nav_21
execute as @a[tag=lr_racer,scores={lr_cp=22,lr_place=0}] at @s run function puffer_rally:nav_22
execute as @a[tag=lr_racer,scores={lr_cp=23,lr_place=0}] at @s run function puffer_rally:nav_23
execute as @a[tag=lr_racer,scores={lr_cp=24,lr_place=0}] at @s run function puffer_rally:nav_24
execute as @a[tag=lr_racer,scores={lr_cp=25,lr_place=0}] at @s run function puffer_rally:nav_25
execute as @a[tag=lr_racer,scores={lr_cp=26,lr_place=0}] at @s run function puffer_rally:nav_26
execute as @a[tag=lr_racer,scores={lr_cp=27,lr_place=0}] at @s run function puffer_rally:nav_27
execute as @a[tag=lr_racer,scores={lr_cp=28,lr_place=0}] at @s run function puffer_rally:nav_28
execute as @a[tag=lr_racer,scores={lr_cp=29,lr_place=0}] at @s run function puffer_rally:nav_29
execute as @a[tag=lr_racer,scores={lr_cp=30,lr_place=0}] at @s run function puffer_rally:nav_30
execute as @a[tag=lr_racer,scores={lr_cp=31,lr_place=0}] at @s run function puffer_rally:nav_31
execute as @a[tag=lr_racer,scores={lr_cp=32,lr_place=0}] at @s run function puffer_rally:nav_32
execute as @a[tag=lr_racer,scores={lr_cp=33,lr_place=0}] at @s run function puffer_rally:nav_33
execute as @a[tag=lr_racer,scores={lr_cp=34,lr_place=0}] at @s run function puffer_rally:nav_34
execute as @a[tag=lr_racer,scores={lr_cp=35,lr_place=0}] at @s run function puffer_rally:nav_35
