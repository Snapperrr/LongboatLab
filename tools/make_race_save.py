"""Build a complete Minecraft 1.21.1 Anvil save, not a schematic or a runtime /fill installer.

Requires numpy (geometry) and Pillow (map overview). Never reads/copies existing player saves.
The generated datapack is self-contained; only its boat NBT depends on Longboat Lab >=0.10.1.
"""
from pathlib import Path
from collections import defaultdict
import argparse
import json
import math
import time
import zipfile
import numpy as np
from race_nbt import *

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'maps' / 'Puffer_Rally_2100'
DATA_VERSION = 3955
SECTIONS = {}
BLOCK_ENTITIES = defaultdict(list)
STATES = []
IDS = {}
SUPPLIES = []
STAGES = [
    (0, 192, '01 起航港 / START', '展开 R：直道加速', 'cyan'),
    (192, 384, '02 弹簧水闸 / JUMP', '压缩 R + 空格：提前跳栏', 'lime'),
    (384, 576, '03 冰面货运 / ICE', '先压缩跳上冰面，再展开加速', 'light_blue'),
    (576, 768, '04 珊瑚回旋 / SLALOM', '过短弯用压缩，出弯再展开', 'orange'),
    (768, 960, '05 河豚船坞 / PIT STOP', '停靠两岸补给箱：尾部装鱼', 'yellow'),
    (960, 1152, '06 钩爪峡谷 / GRAPPLE', '压缩 + G 瞄准金色锚板；W 收索', 'purple'),
    (1152, 1344, '07 跃浪台阶 / SPRING', '压缩空格：连续上台，再拍水落地', 'lime'),
    (1344, 1536, '08 冰川长廊 / OVERDRIVE', '压缩登台，展开 + 尾喷加速', 'light_blue'),
    (1536, 1728, '09 改装港湾 / UPGRADE', '选停补给；底部均匀装鱼更稳', 'yellow'),
    (1728, 1920, '10 双重试炼 / COMBO', '跳栏、钩爪捷径、宽阔落水区', 'magenta'),
    (1920, 2112, '11 终点冲刺 / FINISH', '展开、多桨、尾喷：冲过黑白拱门', 'red'),
]
LANES = [('a', -24, 'cyan'), ('b', 24, 'orange')]
FINISH = 2080

def state(name, **props):
    key = ('minecraft:'+name, tuple(sorted(props.items())))
    if key not in IDS:
        IDS[key] = len(STATES)
        entry = {'Name':string(key[0])}
        if props: entry['Properties']=compound({k:string(v) for k,v in sorted(props.items())})
        STATES.append(entry)
    return IDS[key]

AIR=state('air')
def box(x0,y0,z0,x1,y1,z1,block,**props):
    bid=state(block,**props)
    for cx in range(x0//16,x1//16+1):
        for cz in range(z0//16,z1//16+1):
            for sy in range(y0//16,y1//16+1):
                key=(cx,cz,sy)
                if key not in SECTIONS:
                    if bid==AIR:continue
                    SECTIONS[key]=np.zeros((16,16,16),dtype=np.uint16)
                section=SECTIONS[key]
                section[max(y0-sy*16,0):min(y1-sy*16+1,16),
                        max(z0-cz*16,0):min(z1-cz*16+1,16),
                        max(x0-cx*16,0):min(x1-cx*16+1,16)]=bid

def be(x,y,z,kind,**fields):
    entry={'id':string('minecraft:'+kind),'x':integer(x),'y':integer(y),'z':integer(z),**fields}
    BLOCK_ENTITIES[x//16,z//16].append(entry)

def label(text_,color='white',bold=False):
    return json.dumps({'text':text_,'color':color,'bold':bold},ensure_ascii=False,separators=(',',':'))

def sign(x,y,z,lines,color='black',rotation='8'):
    box(x,y-1,z,x,y-1,z,'dark_oak_planks')
    box(x,y,z,x,y,z,'oak_sign',rotation=rotation,waterlogged='false')
    lines=(list(lines)+['']*4)[:4]
    face=compound({'messages':list_(8,[label(s) for s in lines]),'color':string(color),'has_glowing_text':byte(1)})
    be(x,y,z,'sign',front_text=face,back_text=face,is_waxed=byte(1))

def item(name,count=1,components=None,slot=0):
    result={'id':string('minecraft:'+name),'count':integer(count),'Slot':byte(slot)}
    if components:result['components']=compound(components)
    return result

def boat_item(length=4,width=1,left=8,right=8):
    rig={'Segments':integer(length),'Width':integer(width),'LeftCount':integer(left),'RightCount':integer(right),
         'Compressed':byte(1),'Puffers':integer(0),'BottomPuffers':compound({}),
         'PufferGrid':compound({'Cells':list_(10,[])})}
    return item('oak_boat',components={'minecraft:custom_data':compound({'LongboatRig':compound(rig)}),
                'minecraft:custom_name':string(label(f'体验艇 {length}×{width}','aqua'))})

def chest(x,y,z,kind='pit',lane=None):
    box(x,y,z,x,y,z,'chest',facing='north',type='single',waterlogged='false')
    stacks=[]
    if kind=='pit':
        stacks=[item('pufferfish_bucket',slot=i) for i in range(12)]
        stacks += [item('wooden_shovel',slot=i) for i in range(12,20)]
        stacks += [item('mace',slot=20),item('cooked_beef',32,slot=21),item('oak_planks',32,slot=22)]
    elif kind=='wide':
        stacks=[boat_item(4),boat_item(4),boat_item(4,2)]
        for i,s in enumerate(stacks):s['Slot']=byte(i)
    else:
        giant=item('wooden_shovel',components={'minecraft:custom_data':compound({'GiantOarUnits':integer(64),'GiantOarScale':double(4)}),
                       'minecraft:custom_name':string(label('4 倍巨大桨','gold'))})
        stacks=[boat_item(2,1,0,0),giant,dict(giant),item('mace'),item('pufferfish_bucket')]
        for i,s in enumerate(stacks):s['Slot']=byte(i)
    be(x,y,z,'chest',Items=list_(10,stacks),CustomName=string(label('河豚维修补给' if kind=='pit' else '自由体验装备')))
    if lane:SUPPLIES.append((lane,x,y,z,stacks))

def arch(cx,z,color,title,subtitle,wide=12):
    for x in [cx-wide,cx+wide]:
        box(x,64,z,x+1,76,z+1,color+'_concrete')
        box(x,75,z,x+1,75,z+1,'sea_lantern')
    box(cx-wide,76,z,cx+wide+1,77,z+1,color+'_concrete')
    for x in range(cx-wide+2,cx+wide,4):box(x,76,z,x+1,76,z,'sea_lantern')
    # Readable at player height on both banks, no low overhead beam blocking spring jumps.
    sign(cx-wide+1,65,z-3,[title[:20],subtitle[:22],'路线：向南 +Z','补给箱 / 失误后可救援'])
    sign(cx+wide-1,65,z-3,[title[:20],subtitle[:22],'R 形态 / 空格跳跃','G 钩爪 / Alt 喷气'])

def hazard(cx,z,height=1):
    box(cx-10,64,z,cx+10,63+height,z+2,'orange_concrete')
    for x in range(cx-10,cx+11,4):box(x,64,z,x+1,63+height,z,'black_concrete')
    box(cx-10,63+height,z,cx+10,63+height,z,'honeycomb_block')
    for x in [cx-8,cx+8]:sign(x,64,z-18,['前方跳栏','R 压缩 → 空格','提前约 6 格起跳','失误：/trigger lr_rescue'])

def ice(cx,z0,z1):
    box(cx-10,61,z0,cx+10,63,z1,'packed_ice')
    box(cx-9,63,z0,cx+9,63,z1,'blue_ice')
    for z in range(z0+8,z1-4,16):
        for x in [cx-10,cx+10]:box(x,64,z,x,64,z+3,'light_blue_stained_glass')
    sign(cx-9,64,z0-14,['冰面登台','先压缩 + 跳跃','登台后 R 展开','更多船桨 = 更快'])

def build_geometry():
    # Sealed elevated canal: deep blue body, lit bed, dry spectator spine and exterior buffer.
    box(-64,53,-112,63,55,2143,'stone')
    box(-60,56,-104,59,59,2135,'water',level='0')
    box(-13,56,-104,12,64,2135,'smooth_quartz')
    box(-12,65,-104,-12,66,2135,'cyan_stained_glass')
    box(11,65,-104,11,66,2135,'orange_stained_glass')
    for z in range(-96,2136,16):
        box(-1,64,z,0,64,z+2,'sea_lantern')
        if z%64==0:
            for x in [-10,9]:
                box(x,65,z,x,71,z,'polished_deepslate')
                box(x-1,71,z,x+1,71,z,'sea_lantern')
    for lane,cx,color in LANES:
        box(cx-11,59,-96,cx+11,60,2127,'prismarine_bricks')
        box(cx-10,61,-96,cx+10,63,2127,'water',level='0')
        for side in [-1,1]:
            x=cx+side*11
            box(x,61,-96,x,65,2127,'polished_deepslate')
            # Service walkway and boundary fence.
            outer=cx+side*14
            box(min(x,outer),63,-96,max(x,outer),63,2127,'smooth_quartz')
        for z in range(-80,2120,24):
            for x in [cx-10,cx+10]:box(x,60,z,x,60,z+1,'sea_lantern')
            for x in [cx-12,cx+12]:box(x,64,z,x,64,z,color+'_concrete')
        for start,end,title,subtitle,stagecolor in STAGES:
            arch(cx,start,stagecolor,title,subtitle)
        # Jump slalom, modest one/two-block hurdles within a four-section spring's range.
        for z,h in [(238,1),(296,2),(352,1),(1792,2),(1860,1)]:hazard(cx,z,h)
        ice(cx,414,550);ice(cx,1370,1510)
        # Alternating long islands: tight lengthwise passages reward folding without demanding perfect drift.
        for i,z in enumerate(range(610,750,36)):
            x0,x1=(cx-10,cx+2) if i%2==0 else (cx-2,cx+10)
            box(x0,61,z,x1,65,z+8,'prismarine')
            box(x0,66,z,x1,66,z+8,'orange_stained_glass')
            for x in range(x0,x1+1,3):box(x,65,z,x,65,z,'sea_lantern')
        # Two optional service docks per player per pit; top water stays accessible for hull modifications.
        for z in [804,864,1572,1632]:
            for side in [-1,1]:
                x=cx+side*10
                box(min(x,cx+side*12),64,z-8,max(x,cx+side*12),66,z+8,'air')
                box(min(x,cx+side*14),63,z-8,max(x,cx+side*14),63,z+8,'oak_planks')
                box(x,63,z-6,x,63,z+6,'oak_slab',type='bottom',waterlogged='true')
                chest(cx+side*13,64,z,lane=lane)
                sign(cx+side*12,64,z-5,['可选停靠 / PIT','蹲下 + 桶安装','重锤可拆 / 尾喷 KP2','装底部：潜到船下'])
        # Dry lowered gorge. Slow recovery floor is always available; overhead anchors form the shortcut.
        box(cx-10,56,998,cx+10,63,1126,'air')
        box(cx-10,55,998,cx+10,55,1126,'blue_ice')
        box(cx-10,56,997,cx+10,63,997,'prismarine_bricks')
        for edge in [cx-11,cx+11]:box(edge,55,997,edge,63,1143,'prismarine_bricks')
        # North drop is forgiving; south return is a jumpable three-step staircase.
        for j in range(1,5):
            box(cx-10,55,1127+(j-1)*4,cx+10,55+j*2,1130+(j-1)*4,'packed_ice')
        for z in [988,1024,1060,1096,1132]:
            box(cx-10,64,z,cx-9,82,z+2,'deepslate_tiles')
            box(cx+9,64,z,cx+10,82,z+2,'deepslate_tiles')
            box(cx-10,82,z,cx+10,82,z+2,'polished_deepslate')
            box(cx-4,80,z,cx+4,82,z+2,'gold_block')
            box(cx-3,79,z,cx+3,79,z+2,'ochre_froglight',axis='y')
        sign(cx-8,64,970,['钩爪峡谷','R 压缩 / G 金板','W 收索 / 方向键摆荡','空格解锁，再 G 下一板'])
        # Multi-height spring terraces, generous landings with water gaps.
        for z,h in [(1190,1),(1230,2),(1270,3)]:
            box(cx-9,61,z,cx+9,63+h,z+15,'prismarine_bricks')
            box(cx-9,63+h,z,cx+9,63+h,z+15,'blue_ice')
        # Optional grapple swing in the final combination sector.
        for z in [1760,1818,1888]:
            box(cx-11,79,z,cx+11,80,z+2,'purple_concrete')
            box(cx-3,78,z,cx+3,78,z+2,'gold_block')
        # Direction chevrons and rhythmic thematic towers outside the racing corridor.
        for z in range(48,FINISH,64):
            for d in range(4):
                for side in [-1,1]:box(cx+side*d,60,z-d,cx+side*d,60,z-d,'white_concrete')
        for z in range(96,2050,192):
            x=cx+(-20 if lane=='a' else 20)
            box(x-2,56,z-2,x+2,76,z+2,'prismarine_bricks')
            box(x-3,76,z-3,x+3,77,z+3,'dark_prismarine')
            box(x-1,78,z-1,x+1,81,z+1,'sea_lantern')
        # Finish arch, shallow deceleration lagoon and viewing balcony.
        arch(cx,FINISH,'white','终点 / FINISH','通过全部检查点才计成绩')
        for x in range(cx-12,cx+14):
            for y in [76,77]:box(x,y,FINISH,x,y,FINISH+1,'black_concrete' if (x+y)%2 else 'white_concrete')
        box(cx-10,61,2124,cx+10,66,2127,'prismarine_bricks')
        # Warmup lane; marked ready pad is on shore at the lobby.
        box(cx-8,64,-82,cx+8,64,-64,color+'_concrete')
        box(cx-2,65,-72,cx+2,65,-68,color+'_wool')
        sign(cx,66,-74,[('青队' if lane=='a' else '橙队')+' READY','站在色块上加入','两队就位自动倒数','单人 /trigger lr_practice'])
    # Central clubhouse overlooking both start lanes, stairs from the world spawn.
    box(-44,64,-100,43,64,-82,'smooth_quartz')
    box(-8,64,-81,7,64,-62,'smooth_quartz')
    for x in [-42,40,-8,6]:box(x,65,-98,x+1,74,-97,'dark_prismarine')
    box(-44,74,-100,43,75,-97,'cyan_concrete')
    sign(-4,65,-90,['河豚拉力赛 2100','1.21.1 / 双人竞速','站到青/橙准备色块','看随附地图指南'])
    sign(1,65,-90,['操作与救援','R 形态 / 空格跳','G 钩爪 / KP2 尾喷','/trigger lr_rescue'])
    sign(6,65,-90,['路线：一直向南','11 段 / 2080 米','维修站箱子免费自取','脱困加罚 10 秒'])
    # Free-play laboratory off the lobby; it never lies on checkpoint scoring corridors.
    box(-56,63,-60,-39,63,-10,'blue_ice')
    box(39,63,-60,56,63,-10,'blue_ice')
    chest(-48,64,-53,'giant');chest(48,64,-53,'wide')
    box(47,64,-50,47,64,-50,'crafting_table')
    sign(-48,64,-48,['巨大桨实验场','先用无桨船装两侧','4 倍桨踩冰面移动','装巨大桨不能折叠'])
    sign(48,64,-48,['加宽合成实验场','两艘同长船合成','4×1 + 4×1 → 4×2','加宽艇可并排坐人'])
    # Stair bridges access the side laboratories from the lobby deck.
    box(-56,63,-96,-39,63,-61,'oak_planks');box(39,63,-96,56,63,-61,'oak_planks')
    print('Geometry:',len(SECTIONS),'occupied sections',flush=True)

def nbt_snbt(tag):
    kind,value=tag
    if kind==10:return '{'+','.join(json.dumps(k,ensure_ascii=False)+':'+nbt_snbt(v) for k,v in value.items())+'}'
    if kind==9:return '['+','.join(nbt_snbt((value[0],v)) for v in value[1])+']'
    if kind==8:return json.dumps(value,ensure_ascii=False)
    return str(value)+{1:'b',2:'s',4:'L',5:'f',6:'d'}.get(kind,'')

def function(name,lines):
    p=OUT/'datapacks/puffer_rally/data/puffer_rally/function'/f'{name}.mcfunction'
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text('\n'.join(lines)+'\n',encoding='utf-8')

def msg(selector,text_,color='aqua'):return f'tellraw {selector} '+label(text_,color)
def selector(lane):return f'@a[tag=lr_{lane}]'
def summon_boat(lane,cx,z):
    return f'summon minecraft:boat {cx} 63.85 {z} {{Type:"oak",Tags:["lr_raceboat","lr_boat_{lane}"],Rotation:[0f,0f],Invulnerable:0b,LongboatRig:{{Segments:4,Width:1,LeftCount:8,RightCount:8,Compressed:1b,Puffers:0}}}}'

def build_pack():
    pack=OUT/'datapacks/puffer_rally'
    (pack/'data/minecraft/tags/function').mkdir(parents=True,exist_ok=True)
    (pack/'pack.mcmeta').write_text(json.dumps({'pack':{'pack_format':48,'description':'Puffer Rally 2100 | 双人长船拉力赛'}},ensure_ascii=False),encoding='utf-8')
    for name in ['load','tick']:(pack/f'data/minecraft/tags/function/{name}.json').write_text(json.dumps({'values':['puffer_rally:'+name]}))
    objectives=['lr_state','lr_time','lr_cp','lr_place','lr_sec','lr_part','lr_tmp','lr_penalty','lr_pit1','lr_pit2','lr_pit3','lr_pit4']
    triggers=['lr_rescue','lr_boat','lr_practice','lr_leave','lr_reset','lr_help']
    init=[f'scoreboard objectives add {n} dummy' for n in objectives]+[f'scoreboard objectives add {n} trigger' for n in triggers]
    init+=['scoreboard players set #phase lr_state 0','scoreboard players set #init lr_state 1',
           'scoreboard objectives setdisplay sidebar lr_time','scoreboard objectives modify lr_time displayname '+label('用时 / ticks（20=1秒）','aqua'),
           'gamerule doDaylightCycle false','gamerule doWeatherCycle false','gamerule doMobSpawning false',
           'gamerule doFireTick false','gamerule mobGriefing false','gamerule keepInventory true','gamerule doImmediateRespawn true',
           'gamerule announceAdvancements false','gamerule commandBlockOutput false','gamerule sendCommandFeedback false',
           'gamerule fallDamage false','gamerule drowningDamage false','gamerule spawnRadius 0','gamerule spectatorsGenerateChunks false',
           'time set day','weather clear','setworldspawn 0 65 -94 0',
           'forceload add -32 -80 31 -64',msg('@a','河豚拉力赛：走到青色/橙色准备区加入；/trigger lr_help 查看帮助。')]
    function('init',init)
    # Score conditions cannot reference an objective that does not exist on a fresh world.
    function('load',['scoreboard objectives add lr_state dummy',
                     'execute unless score #init lr_state matches 1 run function puffer_rally:init'])
    function('help',[msg('@s','R 展开/压缩；空格弹跳（锁钩时解锁）；G 发射钩爪；W/S 收放索；方向键球面移动。'),
                     msg('@s','河豚：蹲下拿桶安装，重锤拆卸；KP2 只喷船尾，KP5 只喷船底，Alt 五面同时喷。'),
                     msg('@s','/trigger lr_rescue 保留当前船回最近检查点（+10秒）；/trigger lr_boat 换基础艇（+20秒）。'),
                     msg('@s','/trigger lr_practice 单人练习；/trigger lr_leave 退出；/trigger lr_reset 仅赛后重置。'),
                     'scoreboard players set @s lr_help 0'])
    tick=['execute unless score #init lr_state matches 1 run function puffer_rally:init']
    tick += [f'scoreboard players enable @a {n}' for n in triggers]
    tick += ['execute as @a[scores={lr_help=1..}] run function puffer_rally:help']
    for lane,cx,color in LANES:
        tick += [f'execute if score #phase lr_state matches 0 unless entity @a[tag=lr_{lane}] as @a[tag=!lr_racer,x={cx-2},y=66,z=-72,dx=4,dy=3,dz=4,sort=nearest,limit=1] run function puffer_rally:join_{lane}']
        function('join_'+lane,[f'tag @s add lr_{lane}','tag @s add lr_racer','scoreboard players set @s lr_cp 0',
                              'scoreboard players set @s lr_time 0','scoreboard players set @s lr_place 0','scoreboard players set @s lr_penalty 0',
                              f'tp @s {cx} 66 -70 0 0',msg('@s','已加入'+('青队' if lane=='a' else '橙队')+'，另一名玩家就位后自动开始。')])
    tick+=['execute if score #phase lr_state matches 0 if entity @a[tag=lr_a] if entity @a[tag=lr_b] run function puffer_rally:countdown',
           'execute as @a[scores={lr_practice=1..}] if score #phase lr_state matches 0 run function puffer_rally:practice',
           'scoreboard players set @a[scores={lr_practice=1..}] lr_practice 0',
           'execute if score #phase lr_state matches 1 run function puffer_rally:count_tick',
           'execute if score #phase lr_state matches 2 run function puffer_rally:race_tick',
           'execute as @a[tag=lr_racer,scores={lr_leave=1..}] run function puffer_rally:leave',
           'scoreboard players set @a[scores={lr_leave=1..}] lr_leave 0',
           'execute if score #phase lr_state matches 3 if entity @a[tag=lr_racer,scores={lr_reset=1..}] run function puffer_rally:reset',
           'scoreboard players set @a[scores={lr_reset=1..}] lr_reset 0']
    function('tick',tick)
    function('practice',['execute unless entity @s[tag=lr_racer] unless entity @a[tag=lr_a] run function puffer_rally:join_a',
                         'execute unless entity @s[tag=lr_racer] unless entity @a[tag=lr_b] run function puffer_rally:join_b',
                         'execute if entity @s[tag=lr_racer] run function puffer_rally:countdown'])
    setup=['scoreboard players set #phase lr_state 1','scoreboard players set #count lr_state 100',
           'scoreboard players set #rank lr_place 0','scoreboard players set @a[tag=lr_racer] lr_time 0',
           'scoreboard players set @a[tag=lr_racer] lr_place 0','scoreboard players set @a[tag=lr_racer] lr_cp 0',
           'scoreboard players set @a[tag=lr_racer] lr_penalty 0']
    for lane,cx,color in LANES:
        setup += [f'execute as @a[tag=lr_{lane}] run function puffer_rally:start_{lane}']
        fn=['ride @s dismount',f'kill @e[type=minecraft:boat,tag=lr_boat_{lane}]',f'execute positioned {cx} 64 -20 run '+summon_boat(lane,cx,-20),
            f'tp @s {cx} 64 -20 0 0',f'ride @s mount @e[type=minecraft:boat,tag=lr_boat_{lane},limit=1,sort=nearest]',
            'gamemode survival @s','effect give @s minecraft:resistance infinite 4 true','effect give @s minecraft:saturation infinite 0 true',
            'effect give @s minecraft:water_breathing infinite 0 true','give @s minecraft:mace','give @s minecraft:cooked_beef 16',
            f'spawnpoint @s {cx} 66 -70',msg('@s','基础艇：4 节、左右各 8 桨。比赛装备自由使用；请勿拆赛道抄近路。')]
        fn += [f'scoreboard players set @s lr_pit{i} 0' for i in range(1,5)]
        function('start_'+lane,fn)
    setup += [f'scoreboard players set @a[tag=lr_racer] {trigger} 0' for trigger in triggers]
    setup += ['title @a[tag=lr_racer] times 0 24 0',msg('@a[tag=lr_racer]','准备！两条路线对称，按顺序通过拱门，补给可选择不停。','yellow')]
    function('countdown',setup)
    count=['scoreboard players remove #count lr_state 1']
    for lane,cx,color in LANES:
        count += [f'execute as @a[tag=lr_{lane}] on vehicle run tp @s {cx} 63.85 -20 0 0',
                  f'execute as @e[type=minecraft:boat,tag=lr_boat_{lane}] run data merge entity @s {{Motion:[0d,0d,0d]}}']
    for n,t in [(3,60),(2,40),(1,20)]:
        count += [f'execute if score #count lr_state matches {t} run title @a[tag=lr_racer] title '+label(str(n),'yellow',True),
                  f'execute if score #count lr_state matches {t} as @a[tag=lr_racer] at @s run playsound minecraft:block.note_block.hat master @s ~ ~ ~ 1 1']
    count+=['execute if score #count lr_state matches ..0 run function puffer_rally:go']
    function('count_tick',count)
    function('go',['scoreboard players set #phase lr_state 2','title @a[tag=lr_racer] title '+label('起航！','green',True),
                   'execute as @a[tag=lr_racer] at @s run playsound minecraft:block.note_block.pling master @s ~ ~ ~ 1 1.5'])
    race=['scoreboard players add @a[tag=lr_racer,scores={lr_place=0}] lr_time 1']
    for lane,cx,color in LANES:
        for i,(z,end,title,sub,c) in enumerate(STAGES[1:],1):
            race += [f'execute as @a[tag=lr_{lane},scores={{lr_cp={i-1},lr_place=0}},x={cx-10},y=54,z={z},dx=20,dy=29,dz=20] run function puffer_rally:cp_{lane}_{i}']
            function(f'cp_{lane}_{i}',[f'scoreboard players set @s lr_cp {i}',f'spawnpoint @s {cx+12} 64 {z+8}',
                                     'title @s subtitle '+label(sub,'white'), 'title @s title '+label(title.split(' /')[0],c if c in ['red','yellow'] else 'aqua'),
                                     'execute at @s run playsound minecraft:block.note_block.chime master @s ~ ~ ~ 0.85 1.3'])
        race += [f'execute as @a[tag=lr_{lane},scores={{lr_cp=10,lr_place=0}},x={cx-10},y=54,z={FINISH},dx=20,dy=29,dz=28] run function puffer_rally:finish']
        race += [f'execute as @a[tag=lr_{lane},scores={{lr_rescue=1..,lr_place=0}}] run function puffer_rally:rescue_{lane}',
                 f'execute as @a[tag=lr_{lane},scores={{lr_boat=1..,lr_place=0}}] run function puffer_rally:replace_{lane}']
        # Restock only when the player approaches: distant pit chunks are not force loaded.
        for pit,z in enumerate([804,864,1572,1632],1):
            race += [f'execute as @a[tag=lr_{lane},scores={{lr_pit{pit}=0,lr_place=0}},x={cx-15},y=50,z={z-20},dx=30,dy=45,dz=40] run function puffer_rally:restock_{lane}_{pit}']
            function(f'restock_{lane}_{pit}',[
                f'data modify block {x} {y} {zz} Items set value '+nbt_snbt(list_(10,stacks))
                for l,x,y,zz,stacks in SUPPLIES if l==lane and zz==z]
                +[f'scoreboard players set @s lr_pit{pit} 1'])
        # Falling below the course returns the racer to the latest reached checkpoint.
        race += [f'execute as @a[tag=lr_{lane},scores={{lr_place=0}},y=-64,dy=110] run function puffer_rally:rescue_{lane}']
        recover=['scoreboard players set @s lr_rescue 0','scoreboard players add @s lr_time 200','scoreboard players add @s lr_penalty 200']
        replace=['scoreboard players set @s lr_boat 0','scoreboard players add @s lr_time 200','scoreboard players add @s lr_penalty 200',
                 'ride @s dismount',f'kill @e[type=minecraft:boat,tag=lr_boat_{lane}]',f'function puffer_rally:rescue_{lane}']
        for i in range(11):
            z=8 if i==0 else STAGES[i][0]+24
            recover += [f'execute if score @s lr_cp matches {i} run function puffer_rally:return_{lane}_{i}']
            function(f'return_{lane}_{i}',[
                'scoreboard players set #mounted lr_tmp 0',
                'execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1',
                f'execute if score #mounted lr_tmp matches 1 on vehicle run tp @s {cx} 64 {z} 0 0',
                'execute if score #mounted lr_tmp matches 1 on vehicle run data merge entity @s {Motion:[0d,0d,0d]}',
                f'execute if score #mounted lr_tmp matches 0 run tp @s {cx} 64 {z} 0 0',
                f'execute if score #mounted lr_tmp matches 0 run kill @e[type=minecraft:boat,tag=lr_boat_{lane}]',
                f'execute if score #mounted lr_tmp matches 0 run '+summon_boat(lane,cx,z),
                f'execute if score #mounted lr_tmp matches 0 run ride @s mount @e[type=minecraft:boat,tag=lr_boat_{lane},limit=1,sort=nearest]',
                msg('@s','已回最近检查点。救援 +10秒；未乘船时补发基础艇。若翻转未恢复可 /trigger lr_boat。','yellow')])
        function('rescue_'+lane,recover);function('replace_'+lane,replace)
    race += ['scoreboard players set @a[scores={lr_rescue=1..}] lr_rescue 0','scoreboard players set @a[scores={lr_boat=1..}] lr_boat 0',
             'execute unless entity @a[tag=lr_racer,scores={lr_place=0}] run scoreboard players set #phase lr_state 3']
    function('race_tick',race)
    function('finish',['scoreboard players add #rank lr_place 1','scoreboard players operation @s lr_place = #rank lr_place',
                       'scoreboard players set #twenty lr_tmp 20','scoreboard players operation @s lr_sec = @s lr_time',
                       'scoreboard players operation @s lr_sec /= #twenty lr_tmp',
                       'title @s title '+label('完赛！','gold',True),
                       'tellraw @a '+json.dumps([{'selector':'@s'},{'text':' 完赛！名次 '},{'score':{'name':'@s','objective':'lr_place'}},
                                                  {'text':' / 总用时（含罚时） '},{'score':{'name':'@s','objective':'lr_sec'}},{'text':' 秒'}],ensure_ascii=False),
                       'execute at @s run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 0.8 1',
                       msg('@s','全员完赛后 /trigger lr_reset 重置；/trigger lr_leave 返回大厅。')])
    leave=['ride @s dismount','tp @s 0 65 -94 0 0','effect clear @s minecraft:resistance','effect clear @s minecraft:saturation',
           'effect clear @s minecraft:water_breathing','tag @s remove lr_a','tag @s remove lr_b','tag @s remove lr_racer',
           'scoreboard players set @s lr_leave 0',
           'execute unless entity @a[tag=lr_racer] run scoreboard players set #phase lr_state 0',msg('@s','已退出比赛，回到大厅。')]
    function('leave',leave)
    function('reset',['execute as @a[tag=lr_racer] run ride @s dismount','kill @e[type=minecraft:boat,tag=lr_raceboat]',
                      'execute as @a[tag=lr_racer] run function puffer_rally:leave','scoreboard players set #phase lr_state 0',
                      msg('@a','赛道已待命。重新站到准备色块开始下一轮。')])

def build_level():
    dims={
        'minecraft:overworld':compound({'type':string('minecraft:overworld'),'generator':compound({
            'type':string('minecraft:flat'),'settings':compound({'biome':string('minecraft:ocean'),
                'lakes':byte(0),'features':byte(0),'structure_overrides':list_(8,[]),
                'layers':list_(10,[{'height':integer(1),'block':string('minecraft:bedrock')},
                    {'height':integer(117),'block':string('minecraft:stone')},
                    {'height':integer(6),'block':string('minecraft:water')}])})})}),
        'minecraft:the_nether':compound({'type':string('minecraft:the_nether'),'generator':compound({
            'type':string('minecraft:noise'),'settings':string('minecraft:nether'),
            'biome_source':compound({'type':string('minecraft:multi_noise'),'preset':string('minecraft:nether')})})}),
        'minecraft:the_end':compound({'type':string('minecraft:the_end'),'generator':compound({
            'type':string('minecraft:noise'),'settings':string('minecraft:end'),
            'biome_source':compound({'type':string('minecraft:the_end')})})})}
    rules={'doMobSpawning':'false','doDaylightCycle':'false','doWeatherCycle':'false','doFireTick':'false',
           'keepInventory':'true','doImmediateRespawn':'true','fallDamage':'false','drowningDamage':'false','spawnRadius':'0',
           'spectatorsGenerateChunks':'false','mobGriefing':'false','sendCommandFeedback':'false','announceAdvancements':'false'}
    data={'DataVersion':integer(DATA_VERSION),'version':integer(19133),'LevelName':string('河豚拉力赛 · 2100'),
          'Version':compound({'Id':integer(DATA_VERSION),'Name':string('1.21.1'),'Series':string('main'),'Snapshot':byte(0)}),
          'GameType':integer(0),'hardcore':byte(0),'allowCommands':byte(1),'Difficulty':byte(0),'DifficultyLocked':byte(0),
          'SpawnX':integer(0),'SpawnY':integer(65),'SpawnZ':integer(-94),'SpawnAngle':float_(0),
          'Time':long(0),'DayTime':long(6000),'LastPlayed':long(int(time.time()*1000)),'initialized':byte(1),
          'clearWeatherTime':integer(2147483647),'raining':byte(0),'thundering':byte(0),
          'rainTime':integer(0),'thunderTime':integer(0),'GameRules':compound({k:string(v) for k,v in rules.items()}),
          'WorldGenSettings':compound({'seed':long(210021002100),'generate_features':byte(0),'bonus_chest':byte(0),'dimensions':compound(dims)}),
          'DataPacks':compound({'Enabled':list_(8,['vanilla','fabric','longboatlab','file/puffer_rally']),'Disabled':list_(8,[])}),
          'BorderCenterX':double(0),'BorderCenterZ':double(1000),'BorderSize':double(6000),
          'BorderSizeLerpTarget':double(6000),'BorderSizeLerpTime':long(0),
          'WanderingTraderSpawnDelay':integer(2147483647),'WanderingTraderSpawnChance':integer(0),
          'WasModded':byte(1),'ServerBrands':list_(8,['fabric'])}
    save(OUT/'level.dat',{'Data':compound(data)})

def build_regions():
    regions=defaultdict(dict)
    keys={(cx,cz) for cx,cz,sy in SECTIONS}
    # Include a surrounding pre-generated ocean belt: no abrupt void next to the circuit.
    keys|={(cx,cz) for cx in range(-5,5) for cz in range(-8,135)}
    for index,(cx,cz) in enumerate(sorted(keys)):
        sections=[];height=np.zeros((16,16),dtype=np.int32)
        for sy in range(-4,20):
            blocks=SECTIONS.get((cx,cz,sy))
            if sy<=2: # World floor (-64..47), matching the flat fallback generator.
                blocks=np.full((16,16,16),state('stone'),dtype=np.uint16)
                if sy==-4:blocks[0,:,:]=state('bedrock')
            elif sy==3:
                base=np.full((16,16,16),AIR,dtype=np.uint16)
                base[:6,:,:]=state('stone');base[6:12,:,:]=state('water',level='0')
                if blocks is not None:
                    # Only y<53 is the fallback foundation; keep carved gorge air at y56..63.
                    base[:5,:,:]=state('stone');base[5:,:,:]=blocks[5:,:,:]
                blocks=base
            if blocks is None:blocks=np.zeros((16,16,16),dtype=np.uint16)
            palette,inverse=np.unique(blocks,return_inverse=True)
            bs={'palette':list_(10,[STATES[int(i)] for i in palette])}
            if len(palette)>1:bs['data']=longs(pack_indices(inverse.tolist(),max(4,(len(palette)-1).bit_length())))
            sections.append({'Y':byte(sy),'block_states':compound(bs),'biomes':compound({'palette':list_(8,['minecraft:ocean'])})})
            for ly in range(16):height=np.where(blocks[ly]!=AIR,sy*16+ly+65,height)
        packed=longs(pack_indices(height.reshape(-1).tolist(),9))
        root={'DataVersion':integer(DATA_VERSION),'xPos':integer(cx),'zPos':integer(cz),'yPos':integer(-4),
              'Status':string('minecraft:full'),'LastUpdate':long(0),'InhabitedTime':long(0),'isLightOn':byte(0),
              'sections':list_(10,sections),'block_entities':list_(10,BLOCK_ENTITIES[cx,cz]),
              'block_ticks':list_(10,[]),'fluid_ticks':list_(10,[]),
              'PostProcessing':list_(9,[(2,[]) for _ in range(24)]),
              'Heightmaps':compound({'WORLD_SURFACE':packed,'MOTION_BLOCKING':packed,'MOTION_BLOCKING_NO_LEAVES':packed}),
              'structures':compound({'starts':compound({}),'References':compound({})})}
        regions[cx//32,cz//32][cx,cz]=root
        if index%250==0:print('Chunks:',index,'/',len(keys),flush=True)
    (OUT/'region').mkdir(exist_ok=True)
    for (rx,rz),chunks in regions.items():write_region(OUT/'region'/f'r.{rx}.{rz}.mca',chunks)
    print('Regions:',len(regions),'chunks:',len(keys),flush=True)
    return len(keys)

def overview():
    from PIL import Image,ImageDraw,ImageFont
    image=Image.new('RGB',(1400,1840),'#071c29');d=ImageDraw.Draw(image)
    fontpath=Path('C:/Windows/Fonts/msyh.ttc')
    font=lambda size:ImageFont.truetype(str(fontpath),size)
    d.text((64,38),'河豚拉力赛 2100',font=font(48),fill='#eaf5e6')
    d.text((64,104),'PUFFER RALLY  /  双人 · 11 赛段 · 模组能力竞速',font=font(23),fill='#87b5c8')
    scale=0.66;top=220
    colors={'cyan':'#42d5df','lime':'#a3d254','light_blue':'#7cbdff','orange':'#fa9e52','yellow':'#eac85b','purple':'#aa89d8','magenta':'#d775b5','red':'#ed7563'}
    for start,end,title,sub,c in STAGES:
        y=top+int(start*scale);h=int((end-start)*scale)
        for x in [90,260]:
            d.rounded_rectangle((x,y,x+112,y+h-6),radius=9,fill=colors[c])
            if 'JUMP' in title or 'COMBO' in title:
                for off in [34,68,97]:d.rectangle((x+8,y+off,x+104,y+off+5),fill='#203743')
            if 'GRAPPLE' in title:
                for off in [25,65,100]:d.ellipse((x+48,y+off,x+64,y+off+16),fill='#ffe181')
        d.text((440,y+8),title,font=font(27),fill=colors[c])
        d.text((440,y+53),sub,font=font(22),fill='#d3e0de')
        d.text((1190,y+12),str(start)+' m',font=font(18),fill='#87b5c8')
    d.text((84,170),'青队',font=font(26),fill='#42d5df');d.text((254,170),'橙队',font=font(26),fill='#fa9e52')
    d.text((65,1650),'R 形态   空格 弹跳/解锁   G 钩爪   KP2 尾喷   KP5 底喷',font=font(25),fill='#eaf5e6')
    d.text((65,1700),'救援 /trigger lr_rescue（+10秒） · 换艇 /trigger lr_boat（+20秒）',font=font(22),fill='#eac85b')
    d.text((65,1750),'右侧实验港：加宽合成  |  左侧实验港：巨大桨  |  检查点按顺序计时',font=font(21),fill='#87b5c8')
    image.save(OUT.parent/'Puffer_Rally_2100_overview.png')
    icon=Image.new('RGB',(64,64),'#071c29');p=ImageDraw.Draw(icon)
    p.rectangle((8,0,26,63),fill='#42a6bc');p.rectangle((37,0,55,63),fill='#d69b48')
    for x in [12,41]:
        p.rounded_rectangle((x,27,x+11,49),3,fill='#a97a42');p.rectangle((x+3,30,x+8,45),fill='#342f27')
        p.ellipse((x+2,44,x+10,52),fill='#f6d980')
    for x in range(0,64,8):p.rectangle((x,6,x+7,12),fill='white' if x%16 else '#1d2836')
    icon.save(OUT/'icon.png')

def write_guide():
    guide='''# 河豚拉力赛 · 2100

适用：Minecraft Java 1.21.1 + Fabric + Longboat Lab 0.10.1 或更新版本。两端都装模组和 Fabric API。

## 导入与开局

将整个 `Puffer_Rally_2100` 文件夹复制进你游戏实例的 `saves`，打开“河豚拉力赛 · 2100”。不要只复制 region。
局域网：房主打开存档，再“对局域网开放”，另一名玩家加入。内置数据包自动运行，不要求访客拥有管理员权限。
服务器：将该存档作为服务器世界目录，安装相同模组；使用 MC 1.21.1，不能用基岩版。

从出生大厅分别走到青/橙色 READY 羊毛平台，双方自动领取一艘 4 节、左右各 8 桨的基础船，5 秒后同时起跑。
单人先站一个 READY 平台，再输入 `/trigger lr_practice`。地图设计路线始终向南（+Z），11 段共约 2.1 公里。

## 能力与路线

1. 起航港：R 展开，体验多桨加速。
2. 弹簧水闸：R 压缩，在栏前留出约 6 格，空格起跳。障碍只有 1～2 格高。
3. 冰面货运：先压缩跳上台，再 R 展开加速；冰面属于陆路，避免普通桨在粗糙地面卡住。
4. 珊瑚回旋：左右交替岛礁，压缩短船更容易转弯，出弯可再次展开。
5. 河豚船坞：两岸四个独立补给箱，可停靠下船取鱼桶和铲子。停站消耗时间，尾喷增益能否追回取决于驾驶。
6. 钩爪峡谷：压缩后 G 瞄准上方金色锚板，W 收索前进；方向键调整摆荡，空格解锁后向下一板发射。底部是低位冰道，失败可慢速绕行台阶或用救援。
7. 跃浪台阶：宽阔的 1/2/3 格台面和水间隔，留意弹跳冷却；末端落水体验拍击浪花与新音效。
8. 冰川长廊：纵向展开、尾喷冲刺。喷气加速时提前刹车，前方还有改装港湾。
9. 改装港湾：第二批独立补给，试试船底左右前后对称装鱼。单面大量堆鱼会改变力矩，可能翻船。
10. 双重试炼：跳栏和高位钩爪捷径组合。
11. 终点冲刺：过 2080 米黑白门。必须依次通过全部 10 个检查点才有完赛名次和计时。

维修站箱子在每次开赛时恢复。鱼桶、铲、重锤都是真实物品；河豚安装和拆卸完全使用模组交互。
按小键盘 2 只喷船尾，5 只喷底部；Alt 会一起喷五面，多方向安装时不一定适合一直按 Alt。
船上的控制键可在游戏设置中修改。两人各开一艘船，互不共享库存或计时。

## 救援、重赛与体验港

- `/trigger lr_help`：游戏内帮助。
- `/trigger lr_rescue`：将当前乘坐的船带回最近检查点，保留改装，+10 秒；若已下船则补发基础艇，原比赛艇清除。
- `/trigger lr_boat`：换一艘基础艇并回检查点，+20 秒。适合严重翻转、船丢失或不想保留改装时。
- `/trigger lr_leave`：退出比赛、返回大厅。全员离开后可重新加入。
- `/trigger lr_reset`：全员完赛后重置。管理员也可 `/function puffer_rally:reset` 中止比赛。
- 侧栏显示 tick 用时（20 tick = 1 秒），完赛消息显示含罚时的总秒数。此图是朋友间体验赛，不是防作弊赛事服务器。
- 大厅左侧巨大桨实验区提供无桨船和 4 倍桨；右侧提供同长船与工作台，可合成加宽艇并排乘坐。
- 巨大桨会禁止折叠，因此未强制加入主赛线。体验区不参与检查点计时。

## 存档范围与已知边界

存档已预生成地形、船坞、补给容器、路标和锚点；无需运行生成命令。只强制加载起点少量区块，沿途依靠玩家加载。
没有复制原有世界、玩家背包或身份数据。周边采用平坦海洋生成器，避免赛道外出现突兀随机山脉。
地图为生存交互，允许改船和开箱；请双方约定不拆赛道、不携外部装备、不飞行。救援保留船时也保留其姿态，持续翻滚可使用换艇。
竞速计时按在线玩家执行；此版不支持中途断线继续保持严格竞技公平，断线后建议重开。
同一游戏 tick 内两人抵达终点时，名次由服务器实体处理顺序决定；适合娱乐竞速，非精密判线。

本版仅离线生成存档并做 NBT、区块、容器、命令引用等静态检查，未启动 Minecraft、未编译模组、未进行实机驾驶测试。
赛道通行余量依据当前代码的船尺寸、跳高、钩爪范围设置；实际转向、跳台和多人网络表现仍需你验收。

可复现生成：`python tools/make_race_save.py`。输出路径必须是本生成器专属目录且尚无 level.dat，避免覆盖游玩进度。
'''
    (OUT.parent/'Puffer_Rally_2100_游玩指南.md').write_text(guide,encoding='utf-8')
    (OUT/'README_游玩指南.md').write_text(guide,encoding='utf-8')

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--refresh-pristine',action='store_true');args=parser.parse_args()
    if (OUT/'level.dat').exists():
        marker=OUT/'.generated-pristine'
        if not args.refresh_pristine or not marker.exists() or any((OUT/n).exists() for n in ['session.lock','playerdata','stats','advancements']):
            raise SystemExit('Refusing to overwrite an existing/played save: '+str(OUT))
    OUT.mkdir(parents=True,exist_ok=True)
    build_geometry();build_pack();build_level();chunks=build_regions();overview();write_guide()
    (OUT/'.generated-pristine').write_text('Offline-generated Puffer Rally artifact; never refresh a played copy.\n')
    manifest={'minecraft':'1.21.1','data_version':DATA_VERSION,'required_mod':'longboatlab >=0.10.1',
              'chunks':chunks,'lanes':LANES,'finish_z':FINISH,'stages':STAGES,'supplies':len(SUPPLIES),'runtime_tested':False}
    (OUT.parent/'Puffer_Rally_2100_manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    with zipfile.ZipFile(OUT.parent/'Puffer_Rally_2100.zip','w',zipfile.ZIP_DEFLATED,6) as z:
        for p in sorted(OUT.rglob('*')):
            if p.is_file():z.write(p,p.relative_to(OUT.parent))
    print('Save and ZIP ready:',OUT,flush=True)

if __name__=='__main__':main()
