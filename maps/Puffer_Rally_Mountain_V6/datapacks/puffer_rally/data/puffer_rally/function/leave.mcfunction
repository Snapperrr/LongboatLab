tag @s remove lr_rescue_pending
tag @s remove lr_replace_pending
ride @s dismount
tp @s 0 65 -32 0 0
spawnpoint @s 0 65 -32
effect clear @s minecraft:resistance
effect clear @s minecraft:saturation
effect clear @s minecraft:water_breathing
tag @s remove lr_a
tag @s remove lr_b
tag @s remove lr_racer
execute unless entity @a[tag=lr_racer] unless score #phase lr_state matches 0 run scoreboard players add #round lr_epoch 1
execute unless entity @a[tag=lr_racer] run scoreboard players set #phase lr_state 0
