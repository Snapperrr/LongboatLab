ride @s dismount
tp @s 0 65 -94 0 0
effect clear @s minecraft:resistance
effect clear @s minecraft:saturation
effect clear @s minecraft:water_breathing
tag @s remove lr_a
tag @s remove lr_b
tag @s remove lr_racer
scoreboard players set @s lr_leave 0
execute unless entity @a[tag=lr_racer] run scoreboard players set #phase lr_state 0
tellraw @s {"text":"已退出比赛，回到大厅。","color":"aqua","bold":false}
