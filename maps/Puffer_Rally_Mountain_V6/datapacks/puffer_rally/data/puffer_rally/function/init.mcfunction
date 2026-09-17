scoreboard objectives add lr_cp dummy
scoreboard objectives add lr_time dummy
scoreboard objectives add lr_place dummy
scoreboard objectives add lr_sec dummy
scoreboard objectives add lr_tmp dummy
scoreboard objectives add lr_pit dummy
scoreboard objectives add lr_epoch dummy
scoreboard players set #init lr_state 1
scoreboard players set #phase lr_state 0
scoreboard players set #tick lr_state 0
scoreboard objectives setdisplay sidebar lr_time
scoreboard objectives modify lr_time displayname {"text":"比赛时间 / 20 ticks = 1秒","color":"white","bold":false}
gamerule keepInventory true
gamerule doImmediateRespawn true
gamerule fallDamage false
gamerule drowningDamage false
gamerule doMobSpawning false
gamerule doDaylightCycle false
gamerule doWeatherCycle false
gamerule doFireTick false
gamerule mobGriefing false
gamerule sendCommandFeedback false
gamerule announceAdvancements false
gamerule spawnRadius 0
scoreboard players set #round lr_epoch 1
time set day
weather clear
setworldspawn 0 65 -32 0
forceload add -16 -32 15 -17
