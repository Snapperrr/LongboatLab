tag @s add lr_b
tag @s add lr_racer
scoreboard players operation @s lr_epoch = #round lr_epoch
scoreboard players set @s lr_cp 0
scoreboard players set @s lr_place 0
tellraw @s {"text":"已就位，另一位玩家就位后自动倒计时；右键绿宝石可单人练习。","color":"aqua","bold":false}
