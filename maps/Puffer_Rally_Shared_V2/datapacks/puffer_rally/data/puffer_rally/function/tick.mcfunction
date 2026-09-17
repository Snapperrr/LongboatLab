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
execute if loaded 17 64 140 if block 17 64 140 minecraft:chest unless data block 17 64 140 {components:{"minecraft:custom_data":{LongboatRaceStation:0,LongboatSupplyVersion:1}}} run function puffer_rally:restock_0
execute as @a[tag=lr_tools,tag=lr_supply_0] at @s run function puffer_rally:supply_0
tag @a[tag=lr_supply_0] remove lr_supply_0
execute if loaded 18 65 134 if block 18 65 134 minecraft:oak_sign unless data block 18 65 134 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 18 65 134 {"front_text":{"messages":["{\"text\":\"个人补给 1\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 1\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if loaded 279 64 576 if block 279 64 576 minecraft:chest unless data block 279 64 576 {components:{"minecraft:custom_data":{LongboatRaceStation:1,LongboatSupplyVersion:1}}} run function puffer_rally:restock_1
execute as @a[tag=lr_tools,tag=lr_supply_1] at @s run function puffer_rally:supply_1
tag @a[tag=lr_supply_1] remove lr_supply_1
execute if loaded 273 65 575 if block 273 65 575 minecraft:oak_sign unless data block 273 65 575 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 273 65 575 {"front_text":{"messages":["{\"text\":\"个人补给 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if loaded 596 64 49 if block 596 64 49 minecraft:chest unless data block 596 64 49 {components:{"minecraft:custom_data":{LongboatRaceStation:2,LongboatSupplyVersion:1}}} run function puffer_rally:restock_2
execute as @a[tag=lr_tools,tag=lr_supply_2] at @s run function puffer_rally:supply_2
tag @a[tag=lr_supply_2] remove lr_supply_2
execute if loaded 590 65 48 if block 590 65 48 minecraft:oak_sign unless data block 590 65 48 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 590 65 48 {"front_text":{"messages":["{\"text\":\"个人补给 3\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 3\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
execute if loaded 353 64 1182 if block 353 64 1182 minecraft:chest unless data block 353 64 1182 {components:{"minecraft:custom_data":{LongboatRaceStation:3,LongboatSupplyVersion:1}}} run function puffer_rally:restock_3
execute as @a[tag=lr_tools,tag=lr_supply_3] at @s run function puffer_rally:supply_3
tag @a[tag=lr_supply_3] remove lr_supply_3
execute if loaded 354 65 1176 if block 354 65 1176 minecraft:oak_sign unless data block 354 65 1176 {components:{"minecraft:custom_data":{LongboatSupplyVersion:1}}} run data merge block 354 65 1176 {"front_text":{"messages":["{\"text\":\"个人补给 4\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"back_text":{"messages":["{\"text\":\"个人补给 4\",\"color\":\"white\",\"bold\":false}","{\"text\":\"空手右键领取\",\"color\":\"white\",\"bold\":false}","{\"text\":\"木船 / 木铲 / 河豚各 2\",\"color\":\"white\",\"bold\":false}","{\"text\":\"每人每站每局一份\",\"color\":\"white\",\"bold\":false}"],"color":"black","has_glowing_text":1b},"is_waxed":1b,"components":{"minecraft:custom_data":{"LongboatSupplyVersion":1}}}
