execute as @e[type=minecraft:boat,x=-160,y=-64,z=-160,dx=1607,dy=384,dz=1406] run function puffer_rally:scene_entity
execute as @e[type=minecraft:chest_boat,x=-160,y=-64,z=-160,dx=1607,dy=384,dz=1406] run function puffer_rally:scene_entity
execute as @e[type=minecraft:pufferfish,x=-160,y=-64,z=-160,dx=1607,dy=384,dz=1406] run function puffer_rally:scene_entity
execute as @e[type=minecraft:item,x=-160,y=-64,z=-160,dx=1607,dy=384,dz=1406] run function puffer_rally:scene_entity
execute if score #phase lr_state matches 4 as @e[type=minecraft:boat] run function puffer_rally:free_scene
execute if score #phase lr_state matches 4 as @e[type=minecraft:chest_boat] run function puffer_rally:free_scene
execute if score #phase lr_state matches 4 as @e[type=minecraft:pufferfish] run function puffer_rally:free_scene
execute if score #phase lr_state matches 4 as @e[type=minecraft:item] run function puffer_rally:free_scene
execute unless score #phase lr_state matches 4 as @e[tag=lr_free_entity] run function puffer_rally:scene_entity
