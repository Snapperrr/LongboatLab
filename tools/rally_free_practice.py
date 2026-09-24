"""Add a separate free-roam session to the existing race datapack, idempotently.

Only datapack text and the guide are changed; no chunks, player data or scores
are rewritten. The generator calls install() after producing race functions.
"""
from pathlib import Path
import json

VERSION = '0.10.20'
GUIDE = '''
## 自由练习

大厅右键紫水晶碎片，或输入 `/trigger lr_free`，进入自由练习。绿宝石仍是沿赛道计时的单人练习。
自由练习是本地图独立的一种会话：无倒计时、无计时排名、无检查点要求、无偏航提醒，也不会因离开赛道或下降到低处被自动救援。可在全世界探索，其他玩家可用紫水晶加入同一自由会话。比赛进行中请先用羽毛退出；大厅有其他已就位选手时，需要他们先退出待赛。
自由练习从岸上发船，初始额外提供 16 桶河豚、16 把木铲、4 艘木船和两把 4 倍巨大桨，便于改装。先用重锤拆掉普通桨，才能安装巨大桨。沿途各补给站可反复领取，仍按原有单种资源数量发放；背包容量检查保持有效。
指南针保留改装返回起点水域，追溯指针返回并换基础船，均不罚时。羽毛退出；时钟重置本次自由会话并清理已加载的旧装备。最后一名玩家退出后恢复大厅及竞赛入口。
'''


def message(text, color='aqua'):
    return 'tellraw @s ' + json.dumps({'text': text, 'color': color}, ensure_ascii=False)


def install(save):
    folder = save/'datapacks/puffer_rally/data/puffer_rally/function'
    if not folder.is_dir():
        raise ValueError(f'No rally datapack: {save}')
    original = {p.stem: p.read_text(encoding='utf-8').splitlines() for p in folder.glob('*.mcfunction')}
    # A complete prior application is already patched. A fresh generator always
    # produces a new tick/kit, even if an older free marker remains on disk.
    if any('lr_use_free' in line for line in original['tick']):
        return []
    functions = {name: list(lines) for name, lines in original.items()}
    fn = lambda name, lines: functions.__setitem__(name, lines)
    functions['load'] += ['scoreboard objectives add lr_free trigger',
                          'scoreboard objectives add lr_fstock dummy']
    functions['display_setup'][:0] = ['execute if score #phase lr_state matches 4 run return run scoreboard objectives setdisplay sidebar']
    functions['display_time'][:0] = ['execute if entity @s[tag=lr_free] run return 0']
    marker = '{LongboatRaceTool:"free"}'
    functions['kit'].append('execute unless data entity @s Inventory[{components:{"minecraft:custom_data":'+marker+'}}] run function puffer_rally:give_free')
    fn('give_free', ['scoreboard players set #room lr_tmp 0'] + [
        f'execute unless data entity @s Inventory[{{Slot:{slot}b}}] run scoreboard players set #room lr_tmp 1'
        for slot in range(36)] + [
        'execute if score #room lr_tmp matches 1 run give @s minecraft:amethyst_shard[minecraft:custom_data='+marker+
        ',minecraft:custom_name='+json.dumps(json.dumps({'text':'自由练习：随处驾船','color':'light_purple'},ensure_ascii=False),ensure_ascii=False)+'] 1'])
    functions['tick'][:0] = [
        'scoreboard players enable @a[tag=lr_tools] lr_free',
        'execute as @a[scores={lr_free=1..}] at @s run function puffer_rally:use_free',
        'scoreboard players set @a[scores={lr_free=1..}] lr_free 0',
        'execute as @a[tag=lr_tools,tag=lr_use_free] at @s run function puffer_rally:use_free',
        'tag @a[tag=lr_use_free] remove lr_use_free',
        'scoreboard players remove @a[scores={lr_fstock=1..}] lr_fstock 1']
    fn('use_free', [
        'execute if entity @s[tag=lr_free] run return run '+message('自由练习中 · 指南针返航，羽毛退出。'),
        'execute unless score #phase lr_state matches 0 unless score #phase lr_state matches 4 run return run '+message('先用羽毛退出比赛，再进入自由练习。','yellow'),
        'tag @s add lr_free_joining',
        'execute if score #phase lr_state matches 0 if entity @a[tag=lr_racer,tag=!lr_free_joining] run return run function puffer_rally:free_busy',
        'tag @s remove lr_free_joining',
        'scoreboard players set #free_slot lr_tmp -1',
        *[f'execute if score #free_slot lr_tmp matches -1 positioned {x} 65 -17 unless entity @e[type=minecraft:boat,distance=..3] run scoreboard players set #free_slot lr_tmp {i}'
          for i, x in enumerate((-5,5,-15,15,0))],
        'execute if score #free_slot lr_tmp matches -1 run return run '+message('发船区已满，请先把岸上的船开走。','yellow'),
        'function puffer_rally:free_start'])
    fn('free_busy', ['tag @s remove lr_free_joining',message('大厅还有其他选手待赛，请他们先用羽毛退出待赛。','yellow')])
    fn('free_start', [
        'scoreboard players set #phase lr_state 4',
        'tag @s remove lr_a','tag @s remove lr_b','tag @s add lr_racer','tag @s add lr_free',
        'scoreboard players operation @s lr_epoch = #round lr_epoch',
        'scoreboard players set @s lr_cp 0','scoreboard players set @s lr_place 0',
        'scoreboard players reset @s lr_time','scoreboard players reset @s lr_sec',
        'scoreboard players set @s lr_off 0','scoreboard players set @s lr_warn 0',
        'scoreboard objectives setdisplay sidebar',
        'ride @s dismount','gamemode survival @s',
        *['effect give @s minecraft:'+effect+' infinite '+level+' true'
          for effect,level in [('resistance','4'),('saturation','0'),('water_breathing','0')]],
        'spawnpoint @s 0 65 -32',
        'function puffer_rally:inventory',
        # Keep the first hotbar slot empty after issuing the extra equipment too.
        'item replace entity @s hotbar.0 with minecraft:barrier',
        'give @s minecraft:pufferfish_bucket 16','give @s minecraft:wooden_shovel 16',
        'give @s minecraft:oak_boat 4',
        'give @s minecraft:wooden_shovel[minecraft:custom_data={GiantOarUnits:64,GiantOarScale:4.0d},minecraft:item_name=\'{"translate":"item.longboatlab.giant_oar"}\'] 2',
        'item replace entity @s hotbar.0 with minecraft:air','tag @s add lr_select_empty',
        *[f'execute if score #free_slot lr_tmp matches {i} run function puffer_rally:free_launch_{i}' for i in range(5)],
        'title @s times 5 40 10','title @s title '+json.dumps({'text':'自由练习','color':'light_purple'},ensure_ascii=False),
        message('自由练习 · 不计时、不限路线。紫水晶加入，羽毛退出。'),
        message('重锤拆下普通桨后可装巨大桨；补给站可重复领取。')])
    rig = '{Type:"oak",Tags:["lr_freeboat","lr_free_spawn"],Rotation:[0f,0f],LongboatRig:{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}'
    for i,x in enumerate((-5,5,-15,15,0)):
        fn(f'free_launch_{i}', [f'tp @s {x} 65 -17 0 0',f'execute positioned {x} 65 -17 run summon minecraft:boat ~ ~ ~ '+rig,
            f'execute positioned {x} 65 -17 run ride @s mount @e[type=minecraft:boat,tag=lr_free_spawn,distance=..1,limit=1,sort=nearest]',
            'execute on vehicle run scoreboard players operation @s lr_epoch = #round lr_epoch',
            'tag @e[tag=lr_free_spawn] remove lr_free_spawn'])

    # Reuse the asynchronous chunk-loading rescue. The free branch returns to
    # gate zero without checkpoints, team ownership, timers or penalties.
    prepare = next(line for line in functions['return_0'] if line.startswith('longboatlab race_prepare '))
    x,y,z = prepare.split()[2:5]
    move = next(line for line in functions['return_ready_0'] if ' run longboatlab race_move ' in line)
    yaw = move.split()[-1]
    for action,target in [('rescue','free_rescue'),('boat','free_replace')]:
        functions['use_'+action][:0] = [f'execute if score #phase lr_state matches 4 if entity @s[tag=lr_free] run return run function puffer_rally:{target}']
    fn('free_rescue', ['execute if entity @s[tag=lr_rescue_pending] run return 0',
                       'scoreboard players set @s lr_cp 0',prepare])
    fn('free_replace', ['execute if entity @s[tag=lr_rescue_pending] run return 0',
                        'tag @s add lr_replace_pending','function puffer_rally:free_rescue'])
    functions['return_ready_0'][:0] = ['execute if entity @s[tag=lr_free] run return run function puffer_rally:free_return_ready']
    fn('free_return_ready', [
        'execute unless entity @s[tag=lr_rescue_pending,tag=lr_free,tag=lr_racer] run return 0',
        'execute if entity @s[tag=lr_replace_pending] run ride @s dismount',
        'scoreboard players set #mounted lr_tmp 0',
        'execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1',
        f'execute if score #mounted lr_tmp matches 1 on vehicle run longboatlab race_move {x} {y} {z} {yaw}',
        f'execute if score #mounted lr_tmp matches 0 run tp @s {x} {y} {z} {yaw} 0',
        f'execute if score #mounted lr_tmp matches 0 positioned {x} {y} {z} run summon minecraft:boat ~ ~ ~ '+rig.replace('0f,0f',yaw+'f,0f'),
        f'execute if score #mounted lr_tmp matches 0 positioned {x} {y} {z} run ride @s mount @e[type=minecraft:boat,tag=lr_free_spawn,distance=..1,limit=1,sort=nearest]',
        'execute on vehicle run scoreboard players operation @s lr_epoch = #round lr_epoch',
        'tag @e[tag=lr_free_spawn] remove lr_free_spawn',message('已返回起点水域。')])
    functions['leave'][:0] = ['tag @s remove lr_free','tag @s remove lr_free_joining','scoreboard players set @s lr_fstock 0']
    functions['leave'].append('execute unless score #phase lr_state matches 4 run function puffer_rally:display_setup')
    functions['use_reset'][:0] = ['execute if score #phase lr_state matches 4 if entity @s[tag=lr_free] run return run function puffer_rally:reset']
    for name in ('race_tick','navigation_tick','off_course'):
        functions[name][:0] = ['execute if score #phase lr_state matches 4 run return 0']
    # Track free-session equipment even outside the course. Existing epoch-based
    # cleanup also handles unloaded boats when those chunks are visited later.
    functions['scene_tick'] += [
        f'execute if score #phase lr_state matches 4 as @e[type=minecraft:{kind}] run function puffer_rally:free_scene'
        for kind in ('boat','chest_boat','pufferfish','item')]
    functions['scene_tick'].append('execute unless score #phase lr_state matches 4 as @e[tag=lr_free_entity] run function puffer_rally:scene_entity')
    fn('free_scene',['tag @s add lr_free_entity','function puffer_rally:scene_entity'])
    # Detect the real reward functions, preserving both old mixed and new single
    # supply layouts without touching any station coordinates or block NBT.
    for name,lines in list(functions.items()):
        if not name.startswith('supply_') or not any('race_supply ' in line for line in lines):
            continue
        lines=[line.replace('execute unless score #phase lr_state matches 2 run return 0',
                'execute unless score #phase lr_state matches 2 unless entity @s[tag=lr_free] run return 0')
               .replace('execute if score @s lr_p','execute unless entity @s[tag=lr_free] if score @s lr_p') for line in lines]
        lines[:0] = ['execute if entity @s[tag=lr_free] unless score #phase lr_state matches 4 run return 0',
                     'execute if entity @s[tag=lr_free,scores={lr_fstock=1..}] run return 0']
        lines.append('execute if entity @s[tag=lr_free] run scoreboard players set @s lr_fstock 20')
        fn(name,lines)
    functions['use_help'] += [message('绿宝石：计时单练。紫水晶：自由练习，无偏航提醒。'),
                              message('自由练习：补给可重复领，指南针返航不罚时，时钟重置。')]
    changed=[]
    for name,lines in functions.items():
        if lines == original.get(name):
            continue
        path=folder/(name+'.mcfunction')
        path.write_text('\n'.join(lines)+'\n',encoding='utf-8')
        changed.append(path.relative_to(save).as_posix())
    guide=save/'README_游玩指南.md'
    if guide.exists() and '## 自由练习' not in guide.read_text(encoding='utf-8'):
        guide.write_text(guide.read_text(encoding='utf-8')+GUIDE,encoding='utf-8')
        changed.append(guide.relative_to(save).as_posix())
    return changed
