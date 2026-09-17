clear @s
item replace entity @s hotbar.0 with minecraft:barrier
item replace entity @s hotbar.1 with minecraft:mace
function puffer_rally:kit
give @s minecraft:pufferfish_bucket 2
give @s minecraft:wooden_shovel 4
item replace entity @s hotbar.0 with minecraft:air
tag @s add lr_select_empty
