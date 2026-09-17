scoreboard objectives add lr_state dummy
scoreboard objectives add lr_time dummy
scoreboard objectives add lr_cp dummy
scoreboard objectives add lr_place dummy
scoreboard objectives add lr_sec dummy
scoreboard objectives add lr_part dummy
scoreboard objectives add lr_tmp dummy
scoreboard objectives add lr_penalty dummy
scoreboard objectives add lr_pit1 dummy
scoreboard objectives add lr_pit2 dummy
scoreboard objectives add lr_pit3 dummy
scoreboard objectives add lr_pit4 dummy
scoreboard objectives add lr_rescue trigger
scoreboard objectives add lr_boat trigger
scoreboard objectives add lr_practice trigger
scoreboard objectives add lr_leave trigger
scoreboard objectives add lr_reset trigger
scoreboard objectives add lr_help trigger
scoreboard players set #phase lr_state 0
scoreboard players set #init lr_state 1
scoreboard objectives setdisplay sidebar lr_time
scoreboard objectives modify lr_time displayname {"text":"用时 / ticks（20=1秒）","color":"aqua","bold":false}
gamerule doDaylightCycle false
gamerule doWeatherCycle false
gamerule doMobSpawning false
gamerule doFireTick false
gamerule mobGriefing false
gamerule keepInventory true
gamerule doImmediateRespawn true
gamerule announceAdvancements false
gamerule commandBlockOutput false
gamerule sendCommandFeedback false
gamerule fallDamage false
gamerule drowningDamage false
gamerule spawnRadius 0
gamerule spectatorsGenerateChunks false
time set day
weather clear
setworldspawn 0 65 -94 0
forceload add -32 -80 31 -64
tellraw @a {"text":"河豚拉力赛：走到青色/橙色准备区加入；/trigger lr_help 查看帮助。","color":"aqua","bold":false}
