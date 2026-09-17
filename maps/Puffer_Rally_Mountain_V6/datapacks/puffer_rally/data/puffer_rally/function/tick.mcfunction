scoreboard players add #tick lr_state 1
execute as @a[tag=lr_racer] unless score @s lr_epoch = #round lr_epoch run function puffer_rally:reset_player
execute as @a[tag=!lr_tools] run function puffer_rally:kit
execute if score #tick lr_state matches 100.. as @a run function puffer_rally:kit
execute if score #tick lr_state matches 100.. run scoreboard players set #tick lr_state 0
execute if score #phase lr_state matches 0 unless entity @a[tag=lr_a] as @a[tag=!lr_racer,x=-10,y=66,z=-24,dx=4,dy=3,dz=4,limit=1] run function puffer_rally:join_a
execute if score #phase lr_state matches 0 unless entity @a[tag=lr_b] as @a[tag=!lr_racer,x=6,y=66,z=-24,dx=4,dy=3,dz=4,limit=1] run function puffer_rally:join_b
execute as @a[tag=lr_tools,tag=lr_use_rescue] at @s run function puffer_rally:use_rescue
tag @a[tag=lr_use_rescue] remove lr_use_rescue
execute as @a[tag=lr_tools,tag=lr_use_boat] at @s run function puffer_rally:use_boat
tag @a[tag=lr_use_boat] remove lr_use_boat
execute as @a[tag=lr_tools,tag=lr_use_practice] at @s run function puffer_rally:use_practice
tag @a[tag=lr_use_practice] remove lr_use_practice
execute as @a[tag=lr_tools,tag=lr_use_leave] at @s run function puffer_rally:use_leave
tag @a[tag=lr_use_leave] remove lr_use_leave
execute as @a[tag=lr_tools,tag=lr_use_reset] at @s run function puffer_rally:use_reset
tag @a[tag=lr_use_reset] remove lr_use_reset
execute as @a[tag=lr_tools,tag=lr_use_help] at @s run function puffer_rally:use_help
tag @a[tag=lr_use_help] remove lr_use_help
execute if score #phase lr_state matches 0 if entity @a[tag=lr_a] if entity @a[tag=lr_b] run function puffer_rally:countdown
execute if score #phase lr_state matches 1 run function puffer_rally:count_tick
execute if score #phase lr_state matches 2 run function puffer_rally:race_tick
function puffer_rally:scene_tick
execute if loaded 17 64 73 if block 17 64 73 minecraft:chest unless data block 17 64 73 {components:{"minecraft:custom_data":{LongboatRaceStation:0,LongboatSupplyVersion:1}}} run function puffer_rally:restock_0
execute as @a[tag=lr_tools,tag=lr_supply_0] at @s run function puffer_rally:supply_0
tag @a[tag=lr_supply_0] remove lr_supply_0
execute if loaded 18 65 67 if block 18 65 67 minecraft:oak_sign unless data block 18 65 67 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 18 65 67 {"front_text":{"messages":["{\"text\":\"个人补给 1\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 1\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if loaded 145 64 288 if block 145 64 288 minecraft:chest unless data block 145 64 288 {components:{"minecraft:custom_data":{LongboatRaceStation:1,LongboatSupplyVersion:1}}} run function puffer_rally:restock_1
execute as @a[tag=lr_tools,tag=lr_supply_1] at @s run function puffer_rally:supply_1
tag @a[tag=lr_supply_1] remove lr_supply_1
execute if loaded 139 65 287 if block 139 65 287 minecraft:oak_sign unless data block 139 65 287 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 139 65 287 {"front_text":{"messages":["{\"text\":\"个人补给 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if loaded 310 64 11 if block 310 64 11 minecraft:chest unless data block 310 64 11 {components:{"minecraft:custom_data":{LongboatRaceStation:2,LongboatSupplyVersion:1}}} run function puffer_rally:restock_2
execute as @a[tag=lr_tools,tag=lr_supply_2] at @s run function puffer_rally:supply_2
tag @a[tag=lr_supply_2] remove lr_supply_2
execute if loaded 304 65 10 if block 304 65 10 minecraft:oak_sign unless data block 304 65 10 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 304 65 10 {"front_text":{"messages":["{\"text\":\"个人补给 3\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 3\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if loaded 199 64 615 if block 199 64 615 minecraft:chest unless data block 199 64 615 {components:{"minecraft:custom_data":{LongboatRaceStation:3,LongboatSupplyVersion:1}}} run function puffer_rally:restock_3
execute as @a[tag=lr_tools,tag=lr_supply_3] at @s run function puffer_rally:supply_3
tag @a[tag=lr_supply_3] remove lr_supply_3
execute if loaded 200 65 609 if block 200 65 609 minecraft:oak_sign unless data block 200 65 609 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 200 65 609 {"front_text":{"messages":["{\"text\":\"个人补给 4\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 4\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if score #phase lr_state matches 2 run function puffer_rally:navigation_tick
