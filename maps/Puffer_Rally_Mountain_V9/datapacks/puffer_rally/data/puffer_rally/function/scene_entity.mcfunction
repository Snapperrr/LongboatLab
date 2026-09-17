execute unless score @s lr_epoch matches -2147483648..2147483647 run scoreboard players operation @s lr_epoch = #round lr_epoch
execute unless score @s lr_epoch = #round lr_epoch run return run function puffer_rally:discard
execute if score #phase lr_state matches 0 run function puffer_rally:discard
