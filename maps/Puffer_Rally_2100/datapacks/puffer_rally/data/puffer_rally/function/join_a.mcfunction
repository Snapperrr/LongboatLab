tag @s add lr_a
tag @s add lr_racer
scoreboard players set @s lr_cp 0
scoreboard players set @s lr_time 0
scoreboard players set @s lr_place 0
scoreboard players set @s lr_penalty 0
tp @s -24 66 -70 0 0
tellraw @s {"text":"已加入青队，另一名玩家就位后自动开始。","color":"aqua","bold":false}
