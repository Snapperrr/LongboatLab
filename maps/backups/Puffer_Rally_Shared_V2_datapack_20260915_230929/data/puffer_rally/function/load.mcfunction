scoreboard objectives add lr_state dummy
execute unless score #init lr_state matches 1 run function puffer_rally:init
