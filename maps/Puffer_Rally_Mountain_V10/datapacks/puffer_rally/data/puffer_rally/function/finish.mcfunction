scoreboard players add #rank lr_place 1
scoreboard players operation @s lr_place = #rank lr_place
scoreboard players set #twenty lr_tmp 20
scoreboard players operation @s lr_sec = @s lr_time
scoreboard players operation @s lr_sec /= #twenty lr_tmp
title @s title {"text":"完赛！","color":"gold","bold":false}
tellraw @a [{"selector": "@s"}, {"text": " 完赛，名次 "}, {"score": {"name": "@s", "objective": "lr_place"}}, {"text": "，含罚时 "}, {"score": {"name": "@s", "objective": "lr_sec"}}, {"text": " 秒。"}]
tellraw @s {"text":"全员完赛后右键时钟重开；右键羽毛返回大厅。","color":"aqua","bold":false}
