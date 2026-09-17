scoreboard players add #decor lr_tmp 1
execute unless score #decor lr_tmp matches 10.. run return 0
scoreboard players set #decor lr_tmp 0
execute positioned 101 165 312 if entity @a[distance=..72] run particle minecraft:end_rod ~ ~ ~ 5 3 5 0.015 4 normal @a[distance=..72]
execute positioned 1249 132 942 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1226 144 975 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1206 136 1011 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1163 140 1019 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1121 130 1031 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1084 138 1010 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1114 144 843 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1157 131 833 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1193 143 857 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1231 138 877 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
execute positioned 1237 142 915 if entity @a[distance=..72] run particle minecraft:campfire_cosy_smoke ~ ~ ~ 0.5 0.5 0.5 0.01 1 normal @a[distance=..72]
