execute unless score #phase lr_state matches 0 run tellraw @s {"text":"请在大厅待命阶段开始练习。","color":"yellow","bold":false}
execute if score #phase lr_state matches 0 unless entity @s[tag=lr_racer] unless entity @a[tag=lr_a] run function puffer_rally:join_a
execute if score #phase lr_state matches 0 if entity @s[tag=lr_racer] run function puffer_rally:countdown
