scoreboard players add #rank lr_place 1
scoreboard players operation @s lr_place = #rank lr_place
scoreboard players set #twenty lr_tmp 20
scoreboard players operation @s lr_sec = @s lr_time
scoreboard players operation @s lr_sec /= #twenty lr_tmp
title @s title {"text":"完赛！","color":"gold","bold":true}
tellraw @a [{"selector": "@s"}, {"text": " 完赛！名次 "}, {"score": {"name": "@s", "objective": "lr_place"}}, {"text": " / 总用时（含罚时） "}, {"score": {"name": "@s", "objective": "lr_sec"}}, {"text": " 秒"}]
execute at @s run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 0.8 1
tellraw @s {"text":"全员完赛后 /trigger lr_reset 重置；/trigger lr_leave 返回大厅。","color":"aqua","bold":false}
