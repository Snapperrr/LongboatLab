scoreboard players remove #count lr_state 1
execute as @a[tag=lr_a] on vehicle run tp @s -24 63.85 -20 0 0
execute as @e[type=minecraft:boat,tag=lr_boat_a] run data merge entity @s {Motion:[0d,0d,0d]}
execute as @a[tag=lr_b] on vehicle run tp @s 24 63.85 -20 0 0
execute as @e[type=minecraft:boat,tag=lr_boat_b] run data merge entity @s {Motion:[0d,0d,0d]}
execute if score #count lr_state matches 60 run title @a[tag=lr_racer] title {"text":"3","color":"yellow","bold":true}
execute if score #count lr_state matches 60 as @a[tag=lr_racer] at @s run playsound minecraft:block.note_block.hat master @s ~ ~ ~ 1 1
execute if score #count lr_state matches 40 run title @a[tag=lr_racer] title {"text":"2","color":"yellow","bold":true}
execute if score #count lr_state matches 40 as @a[tag=lr_racer] at @s run playsound minecraft:block.note_block.hat master @s ~ ~ ~ 1 1
execute if score #count lr_state matches 20 run title @a[tag=lr_racer] title {"text":"1","color":"yellow","bold":true}
execute if score #count lr_state matches 20 as @a[tag=lr_racer] at @s run playsound minecraft:block.note_block.hat master @s ~ ~ ~ 1 1
execute if score #count lr_state matches ..0 run function puffer_rally:go
