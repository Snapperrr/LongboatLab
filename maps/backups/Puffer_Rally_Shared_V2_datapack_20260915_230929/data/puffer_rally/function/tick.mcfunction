scoreboard players add #tick lr_state 1
execute as @a[tag=lr_racer] unless score @s lr_epoch = #round lr_epoch run function puffer_rally:reset_player
execute as @a[tag=!lr_tools] run function puffer_rally:kit
execute if score #tick lr_state matches 100.. as @a run function puffer_rally:kit
execute if score #tick lr_state matches 100.. run scoreboard players set #tick lr_state 0
execute if score #phase lr_state matches 0 unless entity @a[tag=lr_a] as @a[tag=!lr_racer,x=-10,y=66,z=-24,dx=4,dy=3,dz=4,limit=1] run function puffer_rally:join_a
execute if score #phase lr_state matches 0 unless entity @a[tag=lr_b] as @a[tag=!lr_racer,x=6,y=66,z=-24,dx=4,dy=3,dz=4,limit=1] run function puffer_rally:join_b
execute as @a[tag=lr_tools,tag=lr_use_rescue] at @s run function puffer_rally:use_rescue
tag @a[tag=lr_use_rescue] remove lr_use_rescue
execute as @a[tag=lr_tools,tag=lr_use_boat] at @s run function puffer_rally:use_boat
tag @a[tag=lr_use_boat] remove lr_use_boat
execute as @a[tag=lr_tools,tag=lr_use_practice] at @s run function puffer_rally:use_practice
tag @a[tag=lr_use_practice] remove lr_use_practice
execute as @a[tag=lr_tools,tag=lr_use_leave] at @s run function puffer_rally:use_leave
tag @a[tag=lr_use_leave] remove lr_use_leave
execute as @a[tag=lr_tools,tag=lr_use_reset] at @s run function puffer_rally:use_reset
tag @a[tag=lr_use_reset] remove lr_use_reset
execute as @a[tag=lr_tools,tag=lr_use_help] at @s run function puffer_rally:use_help
tag @a[tag=lr_use_help] remove lr_use_help
execute if score #phase lr_state matches 0 if entity @a[tag=lr_a] if entity @a[tag=lr_b] run function puffer_rally:countdown
execute if score #phase lr_state matches 1 run function puffer_rally:count_tick
execute if score #phase lr_state matches 2 run function puffer_rally:race_tick
function puffer_rally:scene_tick
