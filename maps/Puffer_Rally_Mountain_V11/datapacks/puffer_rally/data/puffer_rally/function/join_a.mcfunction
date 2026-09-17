tag @s add lr_a
tag @s add lr_racer
scoreboard players operation @s lr_epoch = #round lr_epoch
scoreboard players set @s lr_cp 0
scoreboard players set @s lr_place 0
tellraw @s {"text":"已就位，等待对手。绿宝石：单练。","color":"aqua","bold":false}
