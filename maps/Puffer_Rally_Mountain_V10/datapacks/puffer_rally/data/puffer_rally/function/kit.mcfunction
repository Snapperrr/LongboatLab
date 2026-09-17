tag @s add lr_tools
execute unless data entity @s Inventory[{components:{"minecraft:custom_data":{LongboatRaceTool:"rescue"}}}] run function puffer_rally:give_rescue
execute unless data entity @s Inventory[{components:{"minecraft:custom_data":{LongboatRaceTool:"boat"}}}] run function puffer_rally:give_boat
execute unless data entity @s Inventory[{components:{"minecraft:custom_data":{LongboatRaceTool:"practice"}}}] run function puffer_rally:give_practice
execute unless data entity @s Inventory[{components:{"minecraft:custom_data":{LongboatRaceTool:"leave"}}}] run function puffer_rally:give_leave
execute unless data entity @s Inventory[{components:{"minecraft:custom_data":{LongboatRaceTool:"reset"}}}] run function puffer_rally:give_reset
execute unless data entity @s Inventory[{components:{"minecraft:custom_data":{LongboatRaceTool:"help"}}}] run function puffer_rally:give_help
