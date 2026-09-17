scoreboard players reset @s lr_epoch
execute store result score #entitydrops lr_tmp run gamerule doEntityDrops
execute store result score #mobdrops lr_tmp run gamerule doMobLoot
gamerule doEntityDrops false
gamerule doMobLoot false
kill @s
execute if score #entitydrops lr_tmp matches 1 run gamerule doEntityDrops true
execute if score #mobdrops lr_tmp matches 1 run gamerule doMobLoot true
