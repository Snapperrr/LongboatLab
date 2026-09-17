"""One resource per drive-through station, with reachable waterfront service counters."""
import math
import json
import numpy as np
from race_nbt import compound, integer, string, list_, byte

KINDS=(('fish','pufferfish_bucket',4,'河豚 ×4','yellow'),
       ('oars','wooden_shovel',6,'木铲 ×6','lime'),
       ('boats','oak_boat',2,'木船 ×2','light_blue'))
STATION_KINDS=('oars','boats','fish','fish')


def build(m,s,number):
    c=m.course;p,t=c.at(s);normal=np.array([t[1],-t[0]])
    edge=math.ceil(float(m.half_width(s)));half=17 if number==1 else 25
    kind,item,count,title,color=next(v for v in KINDS if v[0]==STATION_KINDS[number-1])
    def at(a,b):return tuple(int(v) for v in np.rint(p+t*a+normal*b))
    # One inverse raster pass gives diagonal docks sealed banks and a continuous water inlet.
    corners=np.array([p+t*a+normal*b for a in (-half-3,half+3) for b in (edge-4,edge+20)])
    lo=np.floor(corners.min(axis=0)).astype(int);hi=np.ceil(corners.max(axis=0)).astype(int)
    water=[];clearance=[]
    for x in range(lo[0],hi[0]+1):
        for z in range(lo[1],hi[1]+1):
            d=np.array([x,z])-p;a=float(d@t);b=float(d@normal)
            if abs(a)>half+2 or b<edge-3 or b>edge+19:continue
            flare=max(0,min(1,(half+2-abs(a))/7));flare=flare*flare*(3-2*flare)
            wall=edge+2+12*flare
            if b<wall:
                c.box(x,58,z,x,59,z,'dark_prismarine')
                c.box(x,60,z,x,63,z,'water',level='0')
                c.box(x,64,z,x,77,z,'air')
                water.append([x,63,z]);clearance.append([x,65,z])
            elif b<wall+4:
                c.box(x,58,z,x,63,z,'stone_bricks')
                c.box(x,64,z,x,64,z,'spruce_planks')
                c.box(x,65,z,x,77,z,'air')
                if b>wall+2.5:c.box(x,65,z,x,65,z,'spruce_fence')
    # The service blocks occupy the first bank row, flush with the water. A full deck block
    # in front of a low chest used to intercept the seated player's downward ray.
    a=-1 if kind=='boats' else 0;x,z=at(a,edge+14)
    facing=('east' if normal[0]<0 else 'west') if abs(normal[0])>=abs(normal[1]) else ('south' if normal[1]<0 else 'north')
    c.box(x,60,z,x,63,z,'polished_andesite')
    c.box(x,64,z,x,64,z,'chest',facing=facing,type='single',waterlogged='false')
    entries=[c.item(item,count if kind!='boats' else 1,slot=0)]
    if kind=='boats':entries.append(c.item(item,1,slot=1))
    meta={'LongboatRaceStation':integer(number-1),'LongboatRaceSupply':string(kind),'LongboatSupplyVersion':integer(3)}
    c.base.be(x,64,z,'chest',Items=list_(10,entries),Lock=string('longboatlab_personal_supply'),
              CustomName=string(c.label(title,'gold')),components=compound({'minecraft:custom_data':compound(meta)}))
    crates=[dict(kind=kind,item=item,count=count,x=x,y=64,z=z)]
    service=[dict(kind='chest',x=x,y=64,z=z)]
    if kind=='boats':
        # Share an edge with the boat chest even on a diagonal bank.
        tx,tz=(x+int(np.sign(t[0])),z) if abs(t[0])>=abs(t[1]) else (x,z+int(np.sign(t[1])))
        c.box(tx,60,tz,tx,63,tz,'polished_andesite')
        c.box(tx,64,tz,tx,64,tz,'crafting_table')
        service.append(dict(kind='crafting_table',x=tx,y=64,z=tz))
    occupied={(v['x'],v['z']) for v in service}
    water=[v for v in water if (v[0],v[2]) not in occupied]
    clearance=[v for v in clearance if (v[0],v[2]) not in occupied]
    sx,sz=at(0,edge+17)
    c.base.sign(sx,66,sz,[title,'乘船靠岸 · 空手右键','每人每局一次',
                         '旁边工作台可合成长船' if kind=='boats' else '补给 '+str(number)])
    c.box(sx-1,70,sz-1,sx+1,70,sz+1,color+'_concrete')
    c.box(sx,69,sz,sx,69,sz,'sea_lantern')
    # Tall striped landmark and a broad canopy over the bank, leaving 13 blocks above the bay.
    for a in (-half+3,half-3):
        x,z=at(a,edge+17)
        c.box(x,65,z,x,77,z,'stripped_spruce_log',axis='y')
        c.box(x-1,77,z-1,x+1,78,z+1,color+'_concrete')
        c.box(x,76,z,x,76,z,'sea_lantern')
        for dy in range(3,10):c.box(x,65+dy,z,x,65+dy,z,'white_concrete' if dy%3 else color+'_concrete')
    for a in np.arange(-half+1,half,0.5):
        for b in np.arange(edge+14,edge+19,.5):
            x,z=at(a,b);c.box(x,74,z,x,74,z,color+'_wool' if int(a//3)%2 else 'white_wool')
    # Mark the water entrance from a racer's eye level, with luminous floor chevrons.
    for a in (-half+5,half-5):
        for k in range(6):
            for sign in (-1,1):
                x,z=at(a+sign*(5-k)*.6,edge+k)
                c.box(x,59,z,x,59,z,'sea_lantern')
    x,z=at(-half+3,edge+17)
    c.base.sign(x,71,z,[f'补给港 {number}',title,'驶入侧方水道','无需下船'])
    x,z=at(0,edge+8)
    # Exact world positions let offline checks verify the full ray and hull approach, including
    # diagonal stations, instead of merely checking that the basin has air above it.
    berth=p+t*(-.5 if kind=='boats' else 0)+normal*(edge+11.5)
    c.SUPPLIES.append(dict(s=s,x=x,y=64,z=z,kind=kind,items=[],crates=crates,service=service,
                          berth=[float(berth[0]),63.5,float(berth[1])],forward=t.tolist(),
                          half_length=half,edge=edge,water=water,clearance=clearance))


def protect(m,x,z,radius=0):
    for station in m.course.SUPPLIES:
        p,t=m.course.at(station['s']);d=np.array([x,z])-p
        a=float(d@t);b=float(d@np.array([t[1],-t[0]]))
        if abs(a)<station['half_length']+4+radius and station['edge']-4-radius<b<station['edge']+21+radius:return True
    return False


def clear_markers(m):
    # The base course places low bollards after docks; remove only those inside the new water lane.
    c=m.course
    for station in c.SUPPLIES:
        cleared={(x,z) for x,y,z in station['water']}
        for entities in c.base.BLOCK_ENTITIES.values():
            entities[:]=[be for be in entities if (be['x'][1],be['z'][1]) not in cleared or be['y'][1]>73]
        for x,y,z in station['water']:
            c.box(x,60,z,x,63,z,'water',level='0')
            c.box(x,64,z,x,73,z,'air')


def pack(m):
    c=m.course;p=m.OUT/'datapacks/puffer_rally/data/puffer_rally/function'
    read=lambda name:(p/(name+'.mcfunction')).read_text(encoding='utf-8').splitlines()
    # Replace legacy mixed-crate polling. Chest NBT is already generated; no per-tick NBT migration.
    tick=[line for line in read('tick') if not any(v in line for v in ('lr_supply_','restock_','LongboatSupplyVersion'))]
    load=read('load')
    for i,station in enumerate(c.SUPPLIES):
        for crate in station['crates']:
            kind=crate['kind'];suffix=f'{i}_{kind}';objective=f'lr_p{i}_{kind}';x,y,z=(crate[k] for k in ('x','y','z'))
            load.append(f'scoreboard objectives add {objective} dummy')
            tick.extend([f'execute as @a[tag=lr_tools,tag=lr_supply_{suffix}] at @s run function puffer_rally:supply_{suffix}',
                         f'tag @a[tag=lr_supply_{suffix}] remove lr_supply_{suffix}'])
            c.fn('supply_'+suffix,[
                'execute unless score #phase lr_state matches 2 run return 0',
                'execute unless entity @s[tag=lr_racer,scores={lr_place=0}] run return 0',
                f'execute positioned {x} {y} {z} unless entity @s[distance=..8] run return 0',
                f'execute if score @s {objective} = #round lr_epoch run return 0',
                'scoreboard players set #granted lr_tmp 0',
                f'execute store result score #granted lr_tmp run longboatlab race_supply {kind}',
                'execute unless score #granted lr_tmp matches 1 run return 0',
                f'scoreboard players operation @s {objective} = #round lr_epoch',
                c.msg(next(title for k,_,_,title,_ in KINDS if k==kind)+' · 已领取')])
        # Preserve legacy function references without a mixed reward or global refill.
        c.fn('supply_'+str(i),['return 0']);c.fn('restock_'+str(i),['return 0'])
    # Refresh time and clean stale loaded entities every five ticks.
    tick=[line.replace('function puffer_rally:scene_tick',
          'execute if score #housekeeping lr_tmp matches 1 run function puffer_rally:scene_tick')
          if line=='function puffer_rally:scene_tick' else line for line in tick]
    tick=[line.replace('execute as @a[tag=lr_racer] run function puffer_rally:display_time',
          'execute if score #housekeeping lr_tmp matches 1 as @a[tag=lr_racer] run function puffer_rally:display_time') for line in tick]
    tick[:0]=['scoreboard players add #housekeeping lr_tmp 1',
              'execute if score #housekeeping lr_tmp matches 6.. run scoreboard players set #housekeeping lr_tmp 1']
    c.fn('tick',tick);c.fn('load',load)
    help_=[line for line in read('use_help') if '补给箱' not in line]
    c.fn('use_help',help_+[c.msg('每站仅一种资源：绿港木铲6、蓝港木船2、黄港河豚4。'),
                         c.msg('乘船靠岸，空手右键箱子；蓝港工作台紧挨船箱。'),
                         c.msg('蹲下持木铲右键船侧：安装这一叠，背包里的不动。')])
