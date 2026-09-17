scoreboard objectives add lr_state dummy
execute unless score #init lr_state matches 1 run function puffer_rally:init
scoreboard objectives add lr_pit0 dummy
scoreboard objectives add lr_pit1 dummy
scoreboard objectives add lr_pit2 dummy
scoreboard objectives add lr_pit3 dummy
function puffer_rally:display_setup
scoreboard objectives add lr_navclock dummy
scoreboard objectives add lr_off dummy
scoreboard objectives add lr_warn dummy
