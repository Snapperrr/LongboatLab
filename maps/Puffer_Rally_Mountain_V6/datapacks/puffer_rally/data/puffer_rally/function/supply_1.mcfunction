execute unless score #phase lr_state matches 2 run return 0
execute unless entity @s[tag=lr_racer,scores={lr_place=0}] run return 0
execute positioned 145 64 288 unless entity @s[distance=..8] run return 0
execute if score @s lr_pit1 = #round lr_epoch run return 0
scoreboard players set #room lr_tmp 0
execute unless data entity @s Inventory[{Slot:0b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:1b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:2b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:3b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:4b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:5b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:6b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:7b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:8b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:9b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:10b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:11b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:12b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:13b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:14b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:15b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:16b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:17b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:18b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:19b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:20b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:21b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:22b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:23b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:24b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:25b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:26b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:27b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:28b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:29b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:30b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:31b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:32b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:33b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:34b}] run scoreboard players add #room lr_tmp 1
execute unless data entity @s Inventory[{Slot:35b}] run scoreboard players add #room lr_tmp 1
execute unless score #room lr_tmp matches 6.. run tellraw @s {"text":"请先腾出6个背包空位，再领取整份补给。","color":"yellow","bold":false}
execute unless score #room lr_tmp matches 6.. run return 0
give @s minecraft:oak_boat 2
give @s minecraft:wooden_shovel 2
give @s minecraft:pufferfish_bucket 2
scoreboard players operation @s lr_pit1 = #round lr_epoch
tellraw @s {"text":"已领取本站个人补给。其他玩家可独立领取。","color":"aqua","bold":false}
