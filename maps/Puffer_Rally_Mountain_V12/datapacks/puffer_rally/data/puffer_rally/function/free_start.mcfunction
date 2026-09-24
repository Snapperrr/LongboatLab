scoreboard players set #phase lr_state 4
tag @s remove lr_a
tag @s remove lr_b
tag @s add lr_racer
tag @s add lr_free
scoreboard players operation @s lr_epoch = #round lr_epoch
scoreboard players set @s lr_cp 0
scoreboard players set @s lr_place 0
scoreboard players reset @s lr_time
scoreboard players reset @s lr_sec
scoreboard players set @s lr_off 0
scoreboard players set @s lr_warn 0
scoreboard objectives setdisplay sidebar
ride @s dismount
gamemode survival @s
effect give @s minecraft:resistance infinite 4 true
effect give @s minecraft:saturation infinite 0 true
effect give @s minecraft:water_breathing infinite 0 true
spawnpoint @s 0 65 -32
function puffer_rally:inventory
item replace entity @s hotbar.0 with minecraft:barrier
give @s minecraft:pufferfish_bucket 16
give @s minecraft:wooden_shovel 16
give @s minecraft:oak_boat 4
give @s minecraft:wooden_shovel[minecraft:custom_data={GiantOarUnits:64,GiantOarScale:4.0d},minecraft:item_name='{"translate":"item.longboatlab.giant_oar"}'] 2
item replace entity @s hotbar.0 with minecraft:air
tag @s add lr_select_empty
execute if score #free_slot lr_tmp matches 0 run function puffer_rally:free_launch_0
execute if score #free_slot lr_tmp matches 1 run function puffer_rally:free_launch_1
execute if score #free_slot lr_tmp matches 2 run function puffer_rally:free_launch_2
execute if score #free_slot lr_tmp matches 3 run function puffer_rally:free_launch_3
execute if score #free_slot lr_tmp matches 4 run function puffer_rally:free_launch_4
title @s times 5 40 10
title @s title {"text": "自由练习", "color": "light_purple"}
tellraw @s {"text": "自由练习 · 不计时、不限路线。紫水晶加入，羽毛退出。", "color": "aqua"}
tellraw @s {"text": "重锤拆下普通桨后可装巨大桨；补给站可重复领取。", "color": "aqua"}
