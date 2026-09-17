execute unless score #init lr_state matches 1 run function puffer_rally:init
scoreboard players enable @a lr_rescue
scoreboard players enable @a lr_boat
scoreboard players enable @a lr_practice
scoreboard players enable @a lr_leave
scoreboard players enable @a lr_reset
scoreboard players enable @a lr_help
execute as @a[scores={lr_help=1..}] run function puffer_rally:help
execute if score #phase lr_state matches 0 unless entity @a[tag=lr_a] as @a[tag=!lr_racer,x=-26,y=66,z=-72,dx=4,dy=3,dz=4,sort=nearest,limit=1] run function puffer_rally:join_a
execute if score #phase lr_state matches 0 unless entity @a[tag=lr_b] as @a[tag=!lr_racer,x=22,y=66,z=-72,dx=4,dy=3,dz=4,sort=nearest,limit=1] run function puffer_rally:join_b
execute if score #phase lr_state matches 0 if entity @a[tag=lr_a] if entity @a[tag=lr_b] run function puffer_rally:countdown
execute as @a[scores={lr_practice=1..}] if score #phase lr_state matches 0 run function puffer_rally:practice
scoreboard players set @a[scores={lr_practice=1..}] lr_practice 0
execute if score #phase lr_state matches 1 run function puffer_rally:count_tick
execute if score #phase lr_state matches 2 run function puffer_rally:race_tick
execute as @a[tag=lr_racer,scores={lr_leave=1..}] run function puffer_rally:leave
scoreboard players set @a[scores={lr_leave=1..}] lr_leave 0
execute if score #phase lr_state matches 3 if entity @a[tag=lr_racer,scores={lr_reset=1..}] run function puffer_rally:reset
scoreboard players set @a[scores={lr_reset=1..}] lr_reset 0
