execute unless entity @s[tag=lr_racer] unless entity @a[tag=lr_a] run function puffer_rally:join_a
execute unless entity @s[tag=lr_racer] unless entity @a[tag=lr_b] run function puffer_rally:join_b
execute if entity @s[tag=lr_racer] run function puffer_rally:countdown
