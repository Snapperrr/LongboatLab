scoreboard players remove #count lr_state 1
execute as @a[tag=lr_a] on vehicle run tp @s -5 64 4 0 0
execute as @a[tag=lr_a] on vehicle run data merge entity @s {Motion:[0d,0d,0d]}
execute as @a[tag=lr_b] on vehicle run tp @s 5 64 4 0 0
execute as @a[tag=lr_b] on vehicle run data merge entity @s {Motion:[0d,0d,0d]}
execute if score #count lr_state matches 60 run title @a[tag=lr_racer] title {"text":"3","color":"yellow","bold":false}
execute if score #count lr_state matches 40 run title @a[tag=lr_racer] title {"text":"2","color":"yellow","bold":false}
execute if score #count lr_state matches 20 run title @a[tag=lr_racer] title {"text":"1","color":"yellow","bold":false}
execute if score #count lr_state matches ..0 run title @a[tag=lr_racer] title {"text":"起航！","color":"green","bold":false}
execute if score #count lr_state matches ..0 run scoreboard players set #phase lr_state 2
