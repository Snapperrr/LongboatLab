"""Generate a new shared-course Anvil world. Never opens or overwrites a played world.

Geometry is sampled offline; no Minecraft process or gameplay simulation is used.
"""
from collections import defaultdict
from pathlib import Path
import argparse
import gzip
import json
import math
import zipfile
import numpy as np
import make_race_save as base
from race_nbt import *

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'maps/Puffer_Rally_Shared_V2'
WAYPOINTS = [(0, 0), (0, 600), (440, 600), (440, 80), (880, 80),
             (880, 920), (320, 920), (320, 1500), (2000, 1500)]
RADIUS = 90
START_Y, START_Z = 64, 4
GATE_HEIGHT = 25
SENTINEL = 65535
OVERLAYS = {}
SUPPLIES = []
GATES = []
INDEX = defaultdict(list)
SURFACE = {}
state, item, label, snbt = base.state, base.item, base.label, base.nbt_snbt
AIR = base.AIR


def route():
    points = [np.array(WAYPOINTS[0], dtype=float)]
    def line(end):
        start = points[-1]
        for t in np.linspace(0, 1, max(2, math.ceil(np.linalg.norm(end-start)/2)+1))[1:]:
            points.append(start+(end-start)*t)
    for i in range(1, len(WAYPOINTS)-1):
        prev, p, nxt = (np.array(v, dtype=float) for v in WAYPOINTS[i-1:i+2])
        a, b = (p-prev)/np.linalg.norm(p-prev), (nxt-p)/np.linalg.norm(nxt-p)
        entry, end = p-a*RADIUS, p+b*RADIUS
        line(entry)
        center = entry+b*RADIUS
        start_angle = math.atan2(*(entry-center)[::-1])
        turn = np.sign(a[0]*b[1]-a[1]*b[0])
        for t in np.linspace(0, math.pi/2, 73)[1:]:
            angle = start_angle+turn*t
            points.append(center+RADIUS*np.array([math.cos(angle), math.sin(angle)]))
        assert np.linalg.norm(points[-1]-end) < 1e-6
    line(np.array(WAYPOINTS[-1], dtype=float))
    p = np.array(points)
    distances = np.r_[0, np.cumsum(np.linalg.norm(np.diff(p, axis=0), axis=1))]
    return p, distances


POINTS, DISTANCES = route()
LENGTH = float(DISTANCES[-1])


def half_width(s):
    def smooth(value):
        t = np.clip(value,0,1)
        return t*t*(3-2*t)
    return 12+8*smooth((np.asarray(s)-500)/500)+8*smooth((np.asarray(s)-1300)/450)


def at(s, side=0):
    s = float(np.clip(s, 0, LENGTH))
    i = min(len(POINTS)-2, max(0, np.searchsorted(DISTANCES, s)-1))
    t = (s-DISTANCES[i])/(DISTANCES[i+1]-DISTANCES[i])
    tangent = POINTS[i+1]-POINTS[i]
    tangent /= np.linalg.norm(tangent)
    p = POINTS[i]*(1-t)+POINTS[i+1]*t
    return p+np.array([tangent[1], -tangent[0]])*side, tangent


def pos(s, side=0):
    return tuple(int(round(v)) for v in at(s, side)[0])


def box(x0, y0, z0, x1, y1, z1, block, **props):
    bid = state(block, **props)
    for cx in range(x0//16, x1//16+1):
        for cz in range(z0//16, z1//16+1):
            for sy in range(y0//16, y1//16+1):
                a = OVERLAYS.setdefault((cx, cz, sy), np.full((16,16,16), SENTINEL, np.uint16))
                a[max(0,y0-sy*16):min(16,y1-sy*16+1),
                  max(0,z0-cz*16):min(16,z1-cz*16+1),
                  max(0,x0-cx*16):min(16,x1-cx*16+1)] = bid


def local_box(s0, s1, side0, side1, y0, y1, block):
    # Only used on straight course segments; cardinal bounding boxes are exact here.
    corners = np.array([pos(s, side) for s in (s0,s1) for side in (side0,side1)])
    lo, hi = corners.min(axis=0), corners.max(axis=0)
    box(int(lo[0]),y0,int(lo[1]),int(hi[0]),y1,int(hi[1]),block)


def sign(s, side, lines):
    x,z = pos(s,side)
    base.sign(x,65,z,lines)


def gate(s, title, color='cyan', y=64, checkpoint=True):
    p, tangent = at(s)
    yaw = math.degrees(math.atan2(-tangent[0], tangent[1]))
    if checkpoint:
        GATES.append({'s':s,'x':round(float(p[0]),2),'z':round(float(p[1]),2),'y':y,
                      'yaw':round(yaw,2),'title':title,'gorge':y==66})
    span = math.ceil(float(half_width(s)))+3
    for side in (-span,span):
        x,z = pos(s,side)
        box(x-1,61,z-1,x+1,77,z+1,color+'_concrete')
        box(x-1,75,z-1,x+1,75,z+1,'sea_lantern')
    for side in range(-span,span+1):
        x,z = pos(s,side)
        box(x,77,z,x,78,z,color+'_concrete')
        if side%4 == 0: box(x,77,z,x,77,z,'sea_lantern')
    sign(s-6,span+2,[title,'检查点','沿箭头前进',''])


def dock(s, number):
    edge=math.ceil(float(half_width(s)))
    local_box(s-11,s+11,edge,edge+11,61,63,'dark_oak_planks')
    local_box(s-10,s+10,edge+1,edge+10,64,76,'air')
    x,z = pos(s,edge+5)
    box(x,64,z,x,64,z,'chest',facing='north',type='single',waterlogged='false')
    stacks = [item(kind,slot=i*2+j) for i,kind in enumerate(
        ('oak_boat','wooden_shovel','pufferfish_bucket')) for j in range(2)]
    base.be(x,64,z,'chest',Items=list_(10,[]),CustomName=string(label('个人补给 '+str(number),'gold')),
            Lock=string('longboatlab_personal_supply'),components=compound({'minecraft:custom_data':compound({
                'LongboatRaceStation':integer(number-1),'LongboatSupplyVersion':integer(1)})}))
    SUPPLIES.append({'s':s,'x':x,'y':64,'z':z,'items':stacks})
    xx,zz = pos(s+3,edge+5)
    box(xx,64,zz,xx,64,zz,'crafting_table')
    sign(s-6,edge+6,['补给 '+str(number),'空手右键','船 / 桨 / 河豚各 2','每人限领一次'])
    for delta in (-9,9):
        xx,zz = pos(s+delta,edge+10)
        box(xx,64,zz,xx,70,zz,'stripped_spruce_log',axis='y')
        box(xx-1,70,zz-1,xx+1,70,zz+1,'yellow_concrete')
        box(xx,69,zz,xx,69,zz,'sea_lantern')


def height_at(distance, s, x, z):
    distance = distance-half_width(s)+20
    hills = 5+9*(0.5+0.5*np.sin(x*.036)*np.cos(z*.029))
    rise = np.clip((distance-24)/22,0,1)
    fade = np.clip((112-distance)/35,0,1)
    surface = 53+(12+rise*hills)*fade
    surface = np.where(distance<=20.5,60,surface)
    surface = np.where((distance>20.5)&(distance<=24),64,surface)
    return np.floor(surface).astype(np.int32)


def scenery():
    # Deterministic groves, boulders and coastal landmarks outside the navigable river.
    rng = np.random.default_rng(440021)
    for s in np.arange(35,LENGTH,23):
        for side in (-1,1):
            off = side*float(rng.uniform(35,78))
            x,z = pos(s,off)
            y = int(height_at(abs(off),s,x,z))+1
            if 1040<s<1410:
                r = int(rng.integers(3,7))
                for dy in range(r+2):
                    rr = max(1,r-dy//2)
                    box(x-rr,y+dy,z-rr,x+rr,y+dy,z+rr,'stone' if dy%3 else 'andesite')
            elif 2700<s<3300:
                box(x,y,z,x,y+7,z,'spruce_log',axis='y')
                for dy,r in [(3,3),(5,2),(7,1)]:
                    box(x-r,y+dy,z-r,x+r,y+dy+1,z+r,'spruce_leaves',persistent='true',distance='1',waterlogged='false')
                    box(x-r,y+dy+2,z-r,x+r,y+dy+2,z+r,'snow_block')
            else:
                box(x,y,z,x,y+5,z,'oak_log',axis='y')
                box(x-2,y+4,z-2,x+2,y+6,z+2,'oak_leaves',persistent='true',distance='1',waterlogged='false')
                box(x-1,y+7,z-1,x+1,y+7,z+1,'oak_leaves',persistent='true',distance='1',waterlogged='false')
    for s in (470,1830,2520,3870):
        x,z = pos(s,-48)
        y = int(height_at(48,s,x,z))+1
        box(x-6,y-1,z-6,x+6,y,z+6,'stone_bricks')
        for dy in range(1,26):
            box(x-3,y+dy,z-3,x+3,y+dy,z+3,'white_concrete' if (dy//5)%2 else 'red_concrete')
        box(x-4,y+26,z-4,x+4,y+27,z+4,'sea_lantern')
        box(x-5,y+28,z-5,x+5,y+28,z+5,'dark_prismarine')


def geometry(station_scale=1):
    # Keep the shared obstacle sequence while allowing a shorter physical route.
    def local_box(s0,s1,*args): return globals()['local_box'](s0*station_scale,s1*station_scale,*args)
    def pos(s,*args): return globals()['pos'](s*station_scale,*args)
    def sign(s,*args): return globals()['sign'](s*station_scale,*args)
    def gate(s,*args,**kwargs): return globals()['gate'](s*station_scale,*args,**kwargs)
    def dock(s,*args): return globals()['dock'](s*station_scale,*args)
    base.box = box
    base.OUT = OUT
    scenery()
    box(-34,61,-38,34,64,-12,'dark_oak_planks')
    box(-33,65,-37,33,78,-13,'air')
    for x,color in [(-8,'cyan'),(8,'orange')]:
        box(x-2,65,-24,x+2,65,-20,color+'_wool')
        base.sign(x,66,-27,[color.upper()+' READY','站上色块加入','两人共用同一条赛道','单人：右键绿宝石'])
    base.sign(0,65,-32,['河豚共用拉力赛 V2','5.4 公里 / 前窄后宽','起步 2 河豚 + 4 木铲','自带重锤 / 首格空手'])
    gate(0,'起航港',checkpoint=False)
    for s in (180,255):
        edge=math.ceil(float(half_width(s*station_scale)))
        local_box(s,s+1,-edge,edge,61,64,'orange_concrete')
        sign(s-18,edge+4,['弹簧水闸','R 压缩 / 空格起跳','障碍高于水面 1 格','两栏间留有回充距离'])
    for start,end in [(700,765),(2800,3000)]:
        edge=math.ceil(float(half_width(end*station_scale)))
        local_box(start,end,-edge,edge,61,63,'blue_ice')
        sign(start-17,math.ceil(float(half_width(start*station_scale)))+4,['冰面陆路','压缩起跳上台','登台后展开加速','提前准备出弯'])
    # A dry gorge has no drivable lower shortcut. Catch plates fit the stock 11-block hook.
    local_box(1160,1234,-24,24,38,85,'air')
    local_box(1160,1234,-24,24,36,38,'cobbled_deepslate')
    local_box(1158,1159,-24,24,38,63,'stone_bricks')
    local_box(1235,1236,-24,24,38,63,'stone_bricks')
    for lo,hi in [(-25,-24),(24,25)]: local_box(1158,1236,lo,hi,38,72,'stone')
    for s in (1174,1194,1214):
        local_box(s,s+5,-20,20,60,65,'stone_bricks')
        local_box(s,s+5,-20,20,65,65,'moss_block')
    for s in range(1158,1238,7):
        local_box(s,s+1,-21,21,71,71,'gold_block')
        for side in (-22,22):
            x,z = pos(s,side)
            box(x,66,z,x,73,z,'polished_deepslate')
            box(x,72,z,x,72,z,'sea_lantern')
    sign(1135-8/station_scale,24,['钩爪断谷：下方无道路','R 压缩 / G 钩金色横梁','W 收索 / 空格解锁','落下自动救援 +10秒'])
    for s,n in [(140,1),(840,2),(1600,3),(3430,4)]: dock(s,n)
    distances = sorted(set([0,120,320,580,780,970,1135,1196,1250,1430,1650,1880,
                            2050,2270,2420,2630,2770,3040,3250,3400,3690,3900,4120,4380,4650,4920,5160,(LENGTH-28)/station_scale]))
    for i,s in enumerate(distances):
        if s==0:
            p,t = at(0)
            GATES.append({'s':0,'x':0.,'z':4.,'y':64,'yaw':0.,'title':'起航港'})
            continue
        titles={120:'起步补给',320:'弹簧水闸出口',580:'林地大弯',780:'初级冰面出口',970:'岩壁航道',1135:'钩爪断谷入口',
                1196:'钩爪断谷中继',1250:'峡谷出口',1650:'海岸灯塔',2050:'跃浪水闸',
                2630:'雪林回环',2770:'冰川货运',3250:'松林回头弯',3400:'最后补给',3900:'归港高速道',4650:'海岸全速巡航'}
        title = '终点' if i==len(distances)-1 else f'{i:02d} '+titles.get(s,'检查点')
        gate(s,title,'yellow' if s in (400,1430,2420,3690) else 'cyan',66 if s==1196 else 64)
    for s in np.arange(30,(LENGTH-30)/station_scale,28/station_scale):
        if 1135<s<1250: continue
        edge=math.ceil(float(half_width(s*station_scale)))
        for side in (-edge,edge):
            x,z = pos(s,side)
            box(x,63,z,x,63,z,'sea_lantern')
            box(x,64,z,x,64,z,'yellow_concrete' if int(s/28)%2 else 'cyan_concrete')


def prepare_index():
    for i,p in enumerate(POINTS):
        if i%3 and i!=len(POINTS)-1: continue
        cx,cz = np.floor(p/16).astype(int)
        for dx in range(-8,9):
            for dz in range(-8,9):
                if dx*dx+dz*dz<=75: INDEX[cx+dx,cz+dz].append(i)
    for cx,cz,sy in OVERLAYS:
        if (cx,cz) not in INDEX:
            p=np.array([cx*16+8,cz*16+8])
            INDEX[cx,cz]=[int(np.argmin(np.sum((POINTS-p)**2,axis=1)))]


def terrain(cx,cz):
    zz,xx = np.mgrid[cz*16:cz*16+16,cx*16:cx*16+16]
    ids = np.array(INDEX[cx,cz])
    # Project onto short centerline chords instead of quantizing terrain to sampled points.
    ids = np.minimum(ids,len(POINTS)-2)
    a = POINTS[ids]
    b = POINTS[np.minimum(ids+3,len(POINTS)-1)]
    vx,vz = (b-a).T
    dx,dz = xx[None,:,:]-a[:,0,None,None],zz[None,:,:]-a[:,1,None,None]
    den = np.maximum(vx*vx+vz*vz,1e-9)
    t = np.clip((dx*vx[:,None,None]+dz*vz[:,None,None])/den[:,None,None],0,1)
    d2 = (dx-t*vx[:,None,None])**2+(dz-t*vz[:,None,None])**2
    best = np.argmin(d2,axis=0)
    d = np.sqrt(np.take_along_axis(d2,best[None],axis=0)[0])
    tt = np.take_along_axis(t,best[None],axis=0)[0]
    s = DISTANCES[ids[best]]+tt*(DISTANCES[np.minimum(ids[best]+3,len(POINTS)-1)]-DISTANCES[ids[best]])
    top = height_at(d,s,xx,zz)
    yy = np.arange(-64,112)[:,None,None]
    blocks = np.where(yy<=top[None],state('stone'),AIR).astype(np.uint16)
    blocks[(yy>top[None]) & (yy<=63)] = state('water',level='0')
    blocks[0,:,:] = state('bedrock')
    bank_distance=d-half_width(s)+20
    surface = np.where(bank_distance<=20.5,state('gravel'),np.where((bank_distance<29)|(bank_distance>85),state('sand'),
                       np.where((s>2700)&(s<3300),state('snow_block'),state('grass_block',snowy='false'))))
    for dy in (0,1,2):
        yix = top+64-dy
        blocks[yix,np.arange(16)[:,None],np.arange(16)[None,:]] = surface if dy==0 else state('dirt')
    return blocks, d, s


def regions():
    prepare_index()
    grouped=defaultdict(list)
    for cx,cz in INDEX: grouped[cx//32,cz//32].append((cx,cz))
    (OUT/'region').mkdir(exist_ok=True)
    count=0
    for (rx,rz),keys in sorted(grouped.items()):
        chunks={}
        for cx,cz in sorted(keys):
            blocks,d,s=terrain(cx,cz)
            top_section=blocks.shape[0]//16-4
            for sy in range(-4,top_section):
                overlay=OVERLAYS.get((cx,cz,sy))
                if overlay is not None:
                    slab=blocks[(sy+4)*16:(sy+5)*16]
                    np.copyto(slab,overlay,where=overlay!=SENTINEL)
            nonair=blocks!=AIR
            height=blocks.shape[0]-np.argmax(nonair[::-1],axis=0)
            surface_ids=np.take_along_axis(blocks,(height-1)[None],axis=0)[0]
            SURFACE[cx,cz]=(height-65,surface_ids)
            sections=[]
            biome='minecraft:snowy_taiga' if 2700<float(np.mean(s))<3300 else 'minecraft:river'
            for sy in range(-4,20):
                slab=blocks[(sy+4)*16:(sy+5)*16] if sy<top_section else np.zeros((16,16,16),np.uint16)
                palette,inverse=np.unique(slab,return_inverse=True)
                bs={'palette':list_(10,[base.STATES[int(i)] for i in palette])}
                if len(palette)>1: bs['data']=longs(pack_indices(inverse.tolist(),max(4,(len(palette)-1).bit_length())))
                sections.append({'Y':byte(sy),'block_states':compound(bs),'biomes':compound({'palette':list_(8,[biome])})})
            packed=longs(pack_indices(height.reshape(-1).tolist(),9))
            chunks[cx,cz]={'DataVersion':integer(3955),'xPos':integer(cx),'zPos':integer(cz),'yPos':integer(-4),
                'Status':string('minecraft:full'),'LastUpdate':long(0),'InhabitedTime':long(0),'isLightOn':byte(0),
                'sections':list_(10,sections),'block_entities':list_(10,base.BLOCK_ENTITIES[cx,cz]),
                'block_ticks':list_(10,[]),'fluid_ticks':list_(10,[]),
                'PostProcessing':list_(9,[(2,[]) for _ in range(24)]),
                'Heightmaps':compound({'WORLD_SURFACE':packed,'MOTION_BLOCKING':packed}),
                'structures':compound({'starts':compound({}),'References':compound({})})}
            count+=1
        write_region(OUT/'region'/f'r.{rx}.{rz}.mca',chunks)
        print(f'Region {rx},{rz}: {count}/{len(INDEX)} chunks',flush=True)
    return count


def fn(name,lines): base.function(name,lines)
def msg(text_, color='aqua'): return base.msg('@s',text_,color)


TOOLS = [('compass','rescue','救援浮标：保留改装 +10秒'),
         ('recovery_compass','boat','换基础船：返回检查点 +20秒'),
         ('emerald','practice','单人练习：大厅右键开始'),
         ('feather','leave','返回大厅：退出当前比赛'),
         ('clock','reset','重开比赛：全员完赛后使用'),
         ('book','help','赛道指南：右键查看操作')]


def summon(team,x,y,z,yaw):
    return (f'summon minecraft:boat {x} {y} {z} {{Type:"oak",Tags:["lr_raceboat","lr_boat_{team}"],'
            f'Rotation:[{yaw}f,0f],LongboatRig:{{Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0}}}}')


def pack():
    p=OUT/'datapacks/puffer_rally'
    (p/'data/minecraft/tags/function').mkdir(parents=True,exist_ok=True)
    (p/'pack.mcmeta').write_text(json.dumps({'pack':{'pack_format':48,'description':'Shared Puffer Rally V2 / Longboat Lab 0.10.6 / personal supplies'}}))
    for name in ('load','tick'):
        (p/f'data/minecraft/tags/function/{name}.json').write_text(json.dumps({'values':['puffer_rally:'+name]}))
    fn('load',['scoreboard objectives add lr_state dummy','execute unless score #init lr_state matches 1 run function puffer_rally:init',
               *[f'scoreboard objectives add lr_pit{i} dummy' for i in range(4)],'function puffer_rally:display_setup'])
    fn('display_setup',['scoreboard objectives setdisplay sidebar lr_sec',
                        'scoreboard objectives modify lr_sec displayname '+label('用时 · 秒'),
                        'scoreboard players set #twenty lr_tmp 20'])
    fn('display_time',['scoreboard players operation @s lr_sec = @s lr_time',
                      'scoreboard players operation @s lr_sec /= #twenty lr_tmp'])
    fn('init',[f'scoreboard objectives add {n} dummy' for n in ('lr_cp','lr_time','lr_place','lr_sec','lr_tmp','lr_pit','lr_epoch')]+[
        'scoreboard players set #init lr_state 1','scoreboard players set #phase lr_state 0','scoreboard players set #tick lr_state 0',
        'function puffer_rally:display_setup',
        'gamerule keepInventory true','gamerule doImmediateRespawn true','gamerule fallDamage false',
        'gamerule drowningDamage false','gamerule doMobSpawning false','gamerule doDaylightCycle false',
        'gamerule doWeatherCycle false','gamerule doFireTick false','gamerule mobGriefing false',
        'gamerule sendCommandFeedback false','gamerule announceAdvancements false','gamerule spawnRadius 0',
        'scoreboard players set #round lr_epoch 1',
        'time set day','weather clear','setworldspawn 0 65 -32 0','forceload add -16 -32 15 -17'])
    kit=['tag @s add lr_tools']
    for kind,action,title in TOOLS:
        custom='{LongboatRaceTool:"'+action+'"}'
        kit.append('execute unless data entity @s Inventory[{components:{"minecraft:custom_data":'+custom+'}}] run function puffer_rally:give_'+action)
        # Missing tools wait for a free slot instead of repeatedly spilling /give items onto the ground.
        room=['scoreboard players set #room lr_tmp 0']
        room += [f'execute unless data entity @s Inventory[{{Slot:{slot}b}}] run scoreboard players set #room lr_tmp 1' for slot in range(36)]
        room.append('execute if score #room lr_tmp matches 1 run give @s minecraft:'+kind+
                    '[minecraft:custom_data='+custom+',minecraft:custom_name='+json.dumps(label(title),ensure_ascii=False)+'] 1')
        fn('give_'+action,room)
    fn('kit',kit)
    tick=['scoreboard players add #tick lr_state 1',
          'execute as @a[tag=lr_racer] unless score @s lr_epoch = #round lr_epoch run function puffer_rally:reset_player',
          'execute as @a[tag=!lr_tools] run function puffer_rally:kit',
          'execute if score #tick lr_state matches 100.. as @a run function puffer_rally:kit',
          'execute if score #tick lr_state matches 100.. run scoreboard players set #tick lr_state 0']
    for team,x in [('a',-8),('b',8)]:
        tick.append(f'execute if score #phase lr_state matches 0 unless entity @a[tag=lr_{team}] as @a[tag=!lr_racer,x={x-2},y=66,z=-24,dx=4,dy=3,dz=4,limit=1] run function puffer_rally:join_{team}')
        fn('join_'+team,[f'tag @s add lr_{team}','tag @s add lr_racer','scoreboard players operation @s lr_epoch = #round lr_epoch','scoreboard players set @s lr_cp 0',
                         'scoreboard players set @s lr_place 0',msg('已就位，等待对手。绿宝石：单练。')])
    for action in ('rescue','boat','practice','leave','reset','help'):
        tick += [f'execute as @a[tag=lr_tools,tag=lr_use_{action}] at @s run function puffer_rally:use_{action}',
                 f'tag @a[tag=lr_use_{action}] remove lr_use_{action}']
    tick += ['execute if score #phase lr_state matches 0 if entity @a[tag=lr_a] if entity @a[tag=lr_b] run function puffer_rally:countdown',
             'execute if score #phase lr_state matches 1 run function puffer_rally:count_tick',
             'execute if score #phase lr_state matches 2 run function puffer_rally:race_tick',
             'function puffer_rally:scene_tick',
             'execute as @a[tag=lr_racer] run function puffer_rally:display_time']
    fn('tick',tick)
    # Epochs survive chunk unload. Cleanup never force-loads thousands of course chunks.
    lo=np.floor(POINTS.min(axis=0)-160).astype(int)
    hi=np.ceil(POINTS.max(axis=0)+160).astype(int)
    bounds=f'x={lo[0]},y=-64,z={lo[1]},dx={hi[0]-lo[0]},dy=384,dz={hi[1]-lo[1]}'
    fn('scene_tick',[f'execute as @e[type=minecraft:{kind},{bounds}] run function puffer_rally:scene_entity'
                     for kind in ('boat','chest_boat','pufferfish','item')])
    fn('scene_entity',[
        'execute unless score @s lr_epoch matches -2147483648..2147483647 run scoreboard players operation @s lr_epoch = #round lr_epoch',
        'execute unless score @s lr_epoch = #round lr_epoch run return run function puffer_rally:discard',
        'execute if score #phase lr_state matches 0 run function puffer_rally:discard'])
    fn('discard',['scoreboard players reset @s lr_epoch',
        'execute store result score #entitydrops lr_tmp run gamerule doEntityDrops',
        'execute store result score #mobdrops lr_tmp run gamerule doMobLoot',
        'gamerule doEntityDrops false','gamerule doMobLoot false','kill @s',
        'execute if score #entitydrops lr_tmp matches 1 run gamerule doEntityDrops true',
        'execute if score #mobdrops lr_tmp matches 1 run gamerule doMobLoot true'])
    fn('inventory',['clear @s','item replace entity @s hotbar.0 with minecraft:barrier',
                    'item replace entity @s hotbar.1 with minecraft:mace','function puffer_rally:kit',
                    'give @s minecraft:pufferfish_bucket 2','give @s minecraft:wooden_shovel 4',
                    'item replace entity @s hotbar.0 with minecraft:air','tag @s add lr_select_empty'])
    fn('reset_player',['function puffer_rally:leave','function puffer_rally:inventory',
                       'scoreboard players operation @s lr_epoch = #round lr_epoch',
                       'scoreboard players reset @s lr_cp','scoreboard players reset @s lr_time','scoreboard players reset @s lr_sec','scoreboard players reset @s lr_place'])
    fn('use_help',[msg('R 变形 · 空格 跳跃/松钩 · G 钩爪 · W/S 收放 · 方向键 摆荡'),
        msg('蹲下：河豚桶安装，重锤拆卸。Alt 全喷 · 小键盘 单面喷气'),
        msg('右键道具：指南针救援 +10秒 · 追溯指针换船 +20秒 · 羽毛退出 · 时钟重开'),
        msg('补给箱：空手右键，每人船、桨、河豚各2份。')])
    fn('use_practice',['execute unless score #phase lr_state matches 0 run '+msg('请在大厅待命阶段开始练习。','yellow'),
        'execute if score #phase lr_state matches 0 unless entity @s[tag=lr_racer] unless entity @a[tag=lr_a] run function puffer_rally:join_a',
        'execute if score #phase lr_state matches 0 if entity @s[tag=lr_racer] run function puffer_rally:countdown'])
    setup=['scoreboard players set #phase lr_state 1','scoreboard players set #count lr_state 200','scoreboard players set #rank lr_place 0',
           'scoreboard players set @a[tag=lr_racer] lr_time 0','scoreboard players set @a[tag=lr_racer] lr_cp 0',
           'scoreboard players set @a[tag=lr_racer] lr_place 0']
    setup += [f'scoreboard players set #pit{i} lr_pit 0' for i in range(4)]
    for team,x in [('a',-5),('b',5)]:
        setup.append(f'execute as @a[tag=lr_{team}] at @s run function puffer_rally:start_{team}')
        fn('start_'+team,['ride @s dismount',f'kill @e[type=minecraft:boat,tag=lr_boat_{team}]',summon(team,x,START_Y,START_Z,0),
            f'tp @s {x} {START_Y} {START_Z} 0 0',f'ride @s mount @e[type=minecraft:boat,tag=lr_boat_{team},limit=1]',
            'gamemode survival @s','effect give @s minecraft:resistance infinite 4 true','effect give @s minecraft:saturation infinite 0 true',
            'effect give @s minecraft:water_breathing infinite 0 true','spawnpoint @s 0 65 -32',
            'function puffer_rally:inventory','scoreboard players operation @s lr_epoch = #round lr_epoch',
            msg('准备起航！沿黄色箭头前进。')])
    fn('countdown',setup+['title @a[tag=lr_racer] times 0 24 0',
                          'title @a[tag=lr_racer] title '+label('10','yellow')])
    countdown=['scoreboard players remove #count lr_state 1']
    for team,x in [('a',-5),('b',5)]:
        countdown += [f'execute as @a[tag=lr_{team}] on vehicle run tp @s {x} {START_Y} {START_Z} 0 0',
                      f'execute as @a[tag=lr_{team}] on vehicle run data merge entity @s {{Motion:[0d,0d,0d]}}']
    for n in range(9,0,-1):
        t=n*20
        countdown.append(f'execute if score #count lr_state matches {t} run title @a[tag=lr_racer] title '+label(str(n),'yellow'))
    countdown += ['execute if score #count lr_state matches ..0 run title @a[tag=lr_racer] title '+label('起航！','green'),
                  'execute if score #count lr_state matches ..0 run scoreboard players set #phase lr_state 2']
    fn('count_tick',countdown)
    race=['scoreboard players add @a[tag=lr_racer,scores={lr_place=0}] lr_time 1']
    for i,g in enumerate(GATES[1:],1):
        # Oriented gates are converted to enclosing AABBs; progress always requires the previous gate.
        edge=g.get('capture_half_width',math.ceil(float(half_width(g['s'])))+2)
        corners=np.array([at(g['s']+ds,side)[0] for ds in (-7,7) for side in (-edge,edge)])
        lo=np.floor(corners.min(axis=0)).astype(int);hi=np.ceil(corners.max(axis=0)).astype(int)
        y=65 if g.get('gorge') else g['y']-4
        race.append(f'execute as @a[tag=lr_racer,tag=!lr_rescue_pending,scores={{lr_cp={i-1},lr_place=0}},x={lo[0]},y={y},z={lo[1]},dx={hi[0]-lo[0]},dy={GATE_HEIGHT},dz={hi[1]-lo[1]}] at @s run function puffer_rally:cp_{i}')
        # Death respawns at the harbor, where a right-click rescue retains checkpoint progress.
        fn('cp_'+str(i),[f'scoreboard players set @s lr_cp {i}',
                        'title @s actionbar '+label(g['title'],'aqua'),
                        'playsound minecraft:block.note_block.chime master @s ~ ~ ~ 0.8 1.2']+
                        (['function puffer_rally:finish'] if i==len(GATES)-1 else []))
    for i,supply in enumerate(SUPPLIES):
        x,y,z=(supply[k] for k in ('x','y','z'))
        marker='{components:{"minecraft:custom_data":{LongboatRaceStation:'+str(i)+',LongboatSupplyVersion:1}}}'
        # Also migrates existing V2 saves when their station chunks load after a datapack update.
        tick.append(f'execute if loaded {x} {y} {z} if block {x} {y} {z} minecraft:chest unless data block {x} {y} {z} {marker} run function puffer_rally:restock_{i}')
        tick += [f'execute as @a[tag=lr_tools,tag=lr_supply_{i}] at @s run function puffer_rally:supply_{i}',
                 f'tag @a[tag=lr_supply_{i}] remove lr_supply_{i}']
        station_nbt={'Items':list_(10,[]),'Lock':string('longboatlab_personal_supply'),
               'CustomName':string(label('个人补给 '+str(i+1),'gold')),
               'components':compound({'minecraft:custom_data':compound({'LongboatRaceStation':integer(i),'LongboatSupplyVersion':integer(1)})})}
        fn('restock_'+str(i),[f'data merge block {x} {y} {z} '+snbt(compound(station_nbt))])
        sx,sz=pos(supply['s']-6,math.ceil(float(half_width(supply['s'])))+6)
        face=compound({'messages':list_(8,[label(line) for line in [
            '补给 '+str(i+1),'空手右键','船 / 桨 / 河豚各 2','每人限领一次']]),
            'color':string('black'),'has_glowing_text':byte(1)})
        sign_nbt=compound({'front_text':face,'back_text':face,'is_waxed':byte(1),
            'components':compound({'minecraft:custom_data':compound({'LongboatSupplyVersion':integer(1)})})})
        tick.append(f'execute if loaded {sx} 65 {sz} if block {sx} 65 {sz} minecraft:oak_sign unless data block {sx} 65 {sz} {{components:{{"minecraft:custom_data":{{LongboatSupplyVersion:1}}}}}} run data merge block {sx} 65 {sz} '+snbt(sign_nbt))
        fn('supply_'+str(i),[
            'execute unless score #phase lr_state matches 2 run return 0',
            'execute unless entity @s[tag=lr_racer,scores={lr_place=0}] run return 0',
            f'execute positioned {x} {y} {z} unless entity @s[distance=..8] run return 0',
            f'execute if score @s lr_pit{i} = #round lr_epoch run return 0',
            'scoreboard players set #room lr_tmp 0',
            *[f'execute unless data entity @s Inventory[{{Slot:{slot}b}}] run scoreboard players add #room lr_tmp 1' for slot in range(36)],
            'execute unless score #room lr_tmp matches 6.. run '+msg('需要6个背包空位。','yellow'),
            'execute unless score #room lr_tmp matches 6.. run return 0',
            'give @s minecraft:oak_boat 2','give @s minecraft:wooden_shovel 2','give @s minecraft:pufferfish_bucket 2',
            f'scoreboard players operation @s lr_pit{i} = #round lr_epoch',msg('补给已领取。')])
    fn('tick',tick)
    race += ['execute as @a[tag=lr_racer,scores={lr_place=0},y=-64,dy=118] at @s run function puffer_rally:rescue',
             'execute unless entity @a[tag=lr_racer,scores={lr_place=0}] run scoreboard players set #phase lr_state 3']
    fn('race_tick',race)
    for action,target in [('rescue','rescue'),('boat','replace')]:
        fn('use_'+action,[f'execute if score #phase lr_state matches 2 if entity @s[tag=lr_racer,scores={{lr_place=0}}] run function puffer_rally:{target}',
                          'execute unless score #phase lr_state matches 2 run '+msg('救援道具在比赛进行中使用。','yellow')])
    fn('rescue',['execute if entity @s[tag=lr_rescue_pending] run return 0',
                 *[f'execute if score @s lr_cp matches {i} run function puffer_rally:return_{i}' for i in range(len(GATES))]])
    fn('replace',['execute if entity @s[tag=lr_rescue_pending] run return 0',
                  'tag @s add lr_replace_pending','function puffer_rally:rescue'])
    for i,g in enumerate(GATES):
        x,y,z,yaw=(g[k] for k in ('x','y','z','yaw'))
        fn('return_'+str(i),[f'longboatlab race_prepare {x} {y} {z} {i}'])
        lines=['execute unless entity @s[tag=lr_rescue_pending,tag=lr_racer] run return 0',
               'execute if entity @s[tag=lr_replace_pending] run ride @s dismount',
               'scoreboard players add @s lr_time 200',
               'execute if entity @s[tag=lr_replace_pending] run scoreboard players add @s lr_time 200',
               'scoreboard players set #mounted lr_tmp 0',
               'execute on vehicle if entity @s[type=minecraft:boat] run scoreboard players set #mounted lr_tmp 1',
               f'execute if score #mounted lr_tmp matches 1 on vehicle run longboatlab race_move {x} {y} {z} {yaw}',
               f'execute if score #mounted lr_tmp matches 0 run tp @s {x} {y} {z} {yaw} 0']
        for team in ('a','b'):
            condition=f'execute if score #mounted lr_tmp matches 0 if entity @s[tag=lr_{team}] run '
            lines += [condition+f'kill @e[type=minecraft:boat,tag=lr_boat_{team}]',condition+summon(team,x,y,z,yaw),
                      condition+f'ride @s mount @e[type=minecraft:boat,tag=lr_boat_{team},limit=1]']
        lines += ['execute unless entity @s[tag=lr_replace_pending] run '+msg('已救援 · +10秒','yellow'),
                  'execute if entity @s[tag=lr_replace_pending] run '+msg('已换船 · +20秒','yellow')]
        fn('return_ready_'+str(i),lines)
    fn('finish',['scoreboard players add #rank lr_place 1','scoreboard players operation @s lr_place = #rank lr_place',
        'scoreboard players set #twenty lr_tmp 20','scoreboard players operation @s lr_sec = @s lr_time',
        'scoreboard players operation @s lr_sec /= #twenty lr_tmp','title @s title '+label('完赛！','gold'),
        'tellraw @a '+json.dumps([{'selector':'@s'},{'text':' 完赛，名次 '},{'score':{'name':'@s','objective':'lr_place'}},
                                  {'text':'，含罚时 '},{'score':{'name':'@s','objective':'lr_sec'}},{'text':' 秒。'}],ensure_ascii=False),
        msg('全员完赛后右键时钟重开；右键羽毛返回大厅。')])
    fn('use_leave',['function puffer_rally:leave'])
    fn('leave',['tag @s remove lr_rescue_pending','tag @s remove lr_replace_pending',
        'scoreboard players reset @s lr_sec',
        'ride @s dismount','tp @s 0 65 -32 0 0','spawnpoint @s 0 65 -32',
        *['effect clear @s minecraft:'+n for n in ('resistance','saturation','water_breathing')],
        'tag @s remove lr_a','tag @s remove lr_b','tag @s remove lr_racer',
        'execute unless entity @a[tag=lr_racer] unless score #phase lr_state matches 0 run scoreboard players add #round lr_epoch 1',
        'execute unless entity @a[tag=lr_racer] run scoreboard players set #phase lr_state 0'])
    fn('use_reset',['execute unless score #phase lr_state matches 3 run '+msg('全员完赛后才可重开，退出用羽毛。','yellow'),
                    'execute if score #phase lr_state matches 3 if entity @s[tag=lr_racer] run function puffer_rally:reset'])
    fn('reset',['scoreboard players add #round lr_epoch 1',
                'scoreboard players set #phase lr_state 0',
                'execute as @a[tag=lr_racer] run function puffer_rally:reset_player',
                'scoreboard players set #phase lr_state 0','function puffer_rally:scene_tick',
                *[f'scoreboard players set #pit{i} lr_pit 0' for i in range(4)],
                *[f'execute if loaded {p["x"]} {p["y"]} {p["z"]} run function puffer_rally:restock_{i}' for i,p in enumerate(SUPPLIES)]])


def level():
    base.build_level()
    root=decode(gzip.decompress((OUT/'level.dat').read_bytes()))
    d=root['Data'][1]
    d.update(LevelName=string('河豚拉力赛 · 共用大回环 V2'),SpawnY=integer(65),SpawnZ=integer(-32),
             BorderCenterX=double(500),BorderCenterZ=double(750))
    settings=d['WorldGenSettings'][1]['dimensions'][1]['minecraft:overworld'][1]['generator'][1]['settings'][1]
    settings['layers']=list_(10,[{'height':integer(h),'block':string('minecraft:'+b)} for h,b in [(1,'bedrock'),(117,'stone'),(10,'water')]])
    save(OUT/'level.dat',root)


def overview():
    from PIL import Image,ImageDraw,ImageFont
    minx=min(x for x,z in SURFACE)*16;minz=min(z for x,z in SURFACE)*16
    maxx=(max(x for x,z in SURFACE)+1)*16;maxz=(max(z for x,z in SURFACE)+1)*16
    pixels=np.zeros((maxz-minz,maxx-minx,3),np.uint8);pixels[:]=[51,105,145]
    palette=[]
    for entry in base.STATES:
        name=entry['Name'][1]
        c=(116,131,105)
        if 'water' in name:c=(55,132,168)
        elif 'ice' in name:c=(134,209,234)
        elif 'sand' in name:c=(213,205,159)
        elif 'leaves' in name:c=(47,110,65)
        elif 'grass' in name or 'moss' in name:c=(95,151,81)
        elif 'snow' in name or 'white' in name:c=(230,238,240)
        elif 'stone' in name or 'gravel' in name:c=(127,137,140)
        elif 'gold' in name or 'yellow' in name:c=(239,193,69)
        elif 'cyan' in name:c=(44,193,191)
        elif 'planks' in name or 'log' in name:c=(141,106,70)
        elif 'red_' in name or 'orange' in name:c=(208,110,68)
        palette.append(c)
    palette=np.array(palette,np.uint8)
    for (cx,cz),(h,ids) in SURFACE.items():
        shade=np.clip(1+np.gradient(h.astype(float),axis=0)*.035,.65,1.2)
        pixels[cz*16-minz:cz*16-minz+16,cx*16-minx:cx*16-minx+16]=np.clip(palette[ids]*shade[:,:,None],0,255)
    terrain_img=Image.fromarray(pixels)
    scale=0.73
    terrain_img=terrain_img.resize((round(terrain_img.width*scale),round(terrain_img.height*scale)))
    canvas=Image.new('RGB',(terrain_img.width+100,terrain_img.height+230),'#edf2f3')
    canvas.paste(terrain_img,(50,140));draw=ImageDraw.Draw(canvas)
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.text((40,22),'河豚拉力赛 · 共用大回环 V2',font=font(34),fill='#183e48')
    draw.text((40,77),f'{LENGTH/1000:.2f} km / 25 → 41 → 57 格宽 / 7 个宽弯 / 4 个个人补给站',font=font(21),fill='#315862')
    for s,title in [(0,'起点'),(140,'补给 1'),(840,'补给 2'),(1196,'钩爪峡谷'),(1600,'补给 3'),(3430,'补给 4'),(LENGTH-28,'终点')]:
        p,t=at(s);x=(p[0]-minx)*scale+50;y=(p[1]-minz)*scale+140
        draw.ellipse((x-5,y-5,x+5,y+5),fill='#fff2b2',outline='#283d40',width=2)
        draw.text((x+10,y-24),title,font=font(18),fill='#182d34',stroke_width=2,stroke_fill='#f3f4df')
    draw.text((40,canvas.height-62),'右键道具：指南针救援 / 追溯指针换船 / 绿宝石单练 / 羽毛退出 / 时钟重开',font=font(18),fill='#315862')
    canvas.save(OUT.parent/(OUT.name+'_overview.png'))
    icon=terrain_img.resize((64,64));icon.save(OUT/'icon.png')


def artifacts(chunks):
    manifest={'minecraft':'1.21.1','required_mod':'longboatlab >=0.10.6','length_blocks':round(LENGTH,2),
              'water_widths':[25,41,57],'high_speed_start':1300,'corner_radius':90,'chunks':chunks,'gates':GATES,
              'supply_stations':[{k:v for k,v in p.items() if k!='items'} for p in SUPPLIES],
              'items_per_kind_per_station':2,'starter':{'length':1,'width':1,'left_oars':1,'right_oars':1,'puffers':0},
              'starter_inventory':{'pufferfish_bucket':2,'wooden_shovel':4,'mace':1},
              'starter_selected_slot':0,'starter_empty_slot':0,'supply_mode':'per_player_per_round',
              'runtime_tested':False,'waypoints':WAYPOINTS}
    (OUT.parent/(OUT.name+'_manifest.json')).write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    guide=f'''# 河豚拉力赛 · 共用大回环

Minecraft Java 1.21.1 / Fabric / Longboat Lab 0.10.6。先编译并更新两位玩家的模组。

世界目录：Puffer_Rally_Shared_V2。旧存档保留，不覆盖已有游玩进度。
两人分别站出生港青色、橙色 READY 平台，倒计时后在同一条赛道起跑。单人右键绿宝石开始练习。
起步是普通单格橡木船、左右各一桨；背包附带两个河豚桶、四把木铲、一把重锤。快捷栏第一格留空并自动选中，重锤在第二格。开赛与重置会清空整个参赛者背包（包括装备和副手），重新发放这套配件和右键道具。
重赛清理赛道内船、箱子船、掉落物及活河豚；已卸载的旧实体在区块再次加载时按比赛轮次清理，不会把船再变成物品留下。
新一局恢复每位玩家各站的领取资格；救援不会重置领取记录。未在线的参赛者重连时也会重置背包与比赛状态。

## 路线

主线 {LENGTH:.0f} 格，起步水道宽 25 格，中段平滑过渡到 41 格，后段拓宽到 57 格。7 个半径约 90 格的真实转弯，包含回头与反向连续弯。
起步补给提前到 140 米，基础弹跳障碍集中在前 255 米；1300 米后进入连续高速部分，占总长约 76%，末段延长 1000 格，取消后段强制跳栏。
依次经过林地河港、弹跳水闸、冰面陆路、岩石峡谷、海岸灯塔、积雪林地与终点长弯。
按顺序通过 {len(GATES)-1} 个检查门；中途省略检查门不会结算。转弯处沿两岸浮标继续，不要朝下一个远处拱门直接切过陆地。
R 压缩、空格弹跳可以越过高出水面一格的闸栏；冰面需要跳上去，再展开提速。
钩爪峡谷底部无冰道无水路，掉到低处自动救援。金色横梁间距 7 格，单格基础船也在钩爪射程内。
压缩后 G 瞄金梁，W 收索、方向键摆动；空格松钩，接下一条梁。三个高位落脚台可以休息，中间检查门要求经过高处。

## 个人补给

全线仅 4 个补给箱，位于约 140 / 840 / 1600 / 3430 米。空手右键领取，每位参赛者每站每局可获得橡木船、木铲、河豚桶各 2 件；重锤开局自带。
双方使用同一个领取点，但领取资格独立，另一位玩家取走补给不影响自己。背包需要至少 6 个空位，空间不足不扣领取资格。只有比赛进行中且尚未完赛的参赛者可领取，救援不会刷新。
站台配工作台。取船、拆船、合成长船，再下水；多桨和河豚按比赛策略逐步安装，不强制改装才能通关。
每个物品单独占槽，没有超过不可堆叠物品的堆叠上限。赛道不提供大量食物或建筑材料；比赛中自动维持饱食度。

## 右键快捷道具

| 道具 | 操作 |
| --- | --- |
| 指南针 | 保留当前乘坐船和改装返回最近检查点，罚时 10 秒。未乘船时补发基础船。 |
| 追溯指针 | 换成普通单格船并回检查点，总罚时 20 秒。 |
| 绿宝石 | 大厅开始单人练习。 |
| 羽毛 | 退出当前比赛，返回大厅。 |
| 时钟 | 全员完赛后重置，重新站准备平台开下一局。 |
| 书 | 显示控制和路线帮助。 |

道具进入世界自动发放，右键空气、方块、实体均能触发，不要求访客有指令权限。道具使用有 1 秒冷却。
丢失的快捷道具在背包有空间时会补发。模组只接受固定动作标记，不执行物品 NBT 里的任意命令。
比赛道具可以主手或副手使用；功能只对该存档数据包授权的玩家生效。
船的 R/G/空格/喷气/方向键由模组原有自定义绑定控制。小键盘 2 尾喷、5 底喷，Alt 全部喷气。
H 开关船舶 HUD，J 循环切换 75%、100%、125%、150% 大小；均可改键，HUD 设置自动保存。

## 多人和边界

房主打开世界并对局域网开放，另一位加入；双方都安装相同新版模组与 Fabric API。
这是朋友间生存竞速地图，需要约定不拆赛道、不飞行、不携外部装备、不爬岸绕峡谷。检查门和坠落救援防止常见捷径，但不是防作弊服务器。
两人独立计时；同 tick 到达按服务器处理顺序排名。中途断线建议退出重开，暂未实现离线赛事裁判。
救援对船姿态复位，但不撤销玩家对场景方块的修改；要恢复拆坏的地图请重新导入原始副本。

生成：python tools/make_shared_race_save.py。静态检查：python tools/check_shared_race_save.py。
只做离线生成和 NBT/区块/补给/路线尺寸/函数引用检查；未编译、未启动游戏、未进行实机驾驶测试。
'''
    (OUT/'README_游玩指南.md').write_text(guide,encoding='utf-8')
    (OUT.parent/(OUT.name+'_游玩指南.md')).write_text(guide,encoding='utf-8')
    (OUT/'.generated-pristine').write_text('Generated offline. Never overwrite a played save.\n')
    with zipfile.ZipFile(OUT.parent/(OUT.name+'.zip'),'w',zipfile.ZIP_DEFLATED,6) as archive:
        for p in sorted(OUT.rglob('*')):
            if p.is_file(): archive.write(p,p.relative_to(OUT.parent))


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--refresh-pristine',action='store_true');args=parser.parse_args()
    if (OUT/'level.dat').exists() and (not args.refresh_pristine or not (OUT/'.generated-pristine').exists()
            or any((OUT/n).exists() for n in ('session.lock','playerdata','stats','advancements'))):
        raise SystemExit('Refusing to overwrite an existing or played save: '+str(OUT))
    OUT.mkdir(parents=True,exist_ok=True)
    geometry();pack();level();chunks=regions();overview();artifacts(chunks)
    print('Ready:',OUT,flush=True)


if __name__=='__main__': main()
