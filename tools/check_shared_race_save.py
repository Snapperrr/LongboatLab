"""Offline NBT, route geometry, supply and command-reference inspection; never starts Minecraft."""
from pathlib import Path
import gzip
import json
import re
import struct
import zlib
import numpy as np
from race_nbt import decode
import make_shared_race_save as course

SAVE=course.OUT
manifest=json.loads((SAVE.parent/(SAVE.name+'_manifest.json')).read_text(encoding='utf-8'))
level=decode(gzip.decompress((SAVE/'level.dat').read_bytes()))['Data'][1]
assert level['DataVersion'][1]==3955
assert level['SpawnY'][1]==65 and level['SpawnZ'][1]==-32
assert manifest['length_blocks']>5300 and manifest['water_widths']==[25,41,57]
assert manifest['starter_inventory']=={'pufferfish_bucket':2,'wooden_shovel':4,'mace':1}
assert manifest['starter_selected_slot']==manifest['starter_empty_slot']==0
assert manifest['supply_mode']=='per_player_per_round'
assert len(manifest['supply_stations'])==4
selected={}
checks=[]
for g in manifest['gates']:
    checks.append((round(g['x']),g['y'],round(g['z']),'gate'))
for s in range(1162,1234,2):
    x,z=course.pos(s);checks.append((x,54,z,'gorge_floor'))
for s in range(1158,1238,7):
    x,z=course.pos(s);checks.append((x,71,z,'anchor'))
for s in (90,460,680,1020,1330,1690,2290,2580,3100,3350,4030):
    half=float(course.half_width(s))-2
    for side in (-half,0,half):
        x,z=course.pos(s,side);checks.append((x,63,z,'water'))
for x in (-8,8): checks.append((x,65,-22,'ready'))
for s in (90,460,2290,3350,4650):
    width=float(course.half_width(s))
    for side in (-1,1):
        x,z=course.pos(s,side*(width+2));checks.append((x,64,z,'bank'))
needed={(x//16,z//16) for x,y,z,kind in checks}
regions=chunks=signs=containers=0
block_entities=[]
station_ids=set()
for region in sorted((SAVE/'region').glob('*.mca')):
    regions+=1;raw=region.read_bytes();occupied=set()
    rx,rz=map(int,region.stem.split('.')[1:])
    for i in range(1024):
        entry=struct.unpack_from('>I',raw,i*4)[0]
        if not entry:continue
        offset,count=entry>>8,entry&255
        assert offset>=2 and offset+count<=len(raw)//4096
        sectors=set(range(offset,offset+count));assert not occupied&sectors;occupied|=sectors
        n=struct.unpack_from('>I',raw,offset*4096)[0]
        assert n+4<=count*4096 and raw[offset*4096+4]==2
        c=decode(zlib.decompress(raw[offset*4096+5:offset*4096+4+n]))
        cx,cz=c['xPos'][1],c['zPos'][1]
        assert (cx,cz)==(rx*32+i%32,rz*32+i//32)
        assert c['Status'][1]=='minecraft:full' and len(c['sections'][1][1])==24
        chunks+=1
        for sec in c['sections'][1][1]:
            states=sec['block_states'][1];palette=states['palette'][1][1]
            if len(palette)>1:
                bits=max(4,(len(palette)-1).bit_length());per=64//bits;mask=(1<<bits)-1
                words=np.array([v&((1<<64)-1) for v in states['data'][1]],np.uint64)
                assert len(words)==(4096+per-1)//per
                indexes=np.arange(4096,dtype=np.uint64)
                values=(words[indexes//per]>>((indexes%per)*bits))&mask
                assert int(values.max())<len(palette)
        entities=c['block_entities'][1][1]
        if (cx,cz) in needed or entities: selected[cx,cz]=c
        block_entities.extend(entities)
        for be in entities:
            kind=be['id'][1]
            if kind=='minecraft:chest':
                containers+=1;stacks=be['Items'][1][1]
                assert not stacks
                assert be['Lock'][1]=='longboatlab_personal_supply'
                data=be['components'][1]['minecraft:custom_data'][1]
                station=data['LongboatRaceStation'][1]
                assert station not in station_ids and 0<=station<4
                station_ids.add(station)
                assert data['LongboatSupplyVersion'][1]==1
            elif kind=='minecraft:sign':signs+=1
    print('Inspected region',region.name,flush=True)


def block(x,y,z):
    c=selected[x//16,z//16]
    sec=next(s for s in c['sections'][1][1] if s['Y'][1]==y//16)
    states=sec['block_states'][1];palette=states['palette'][1][1]
    if len(palette)==1:return palette[0]['Name'][1]
    bits=max(4,(len(palette)-1).bit_length());per=64//bits;i=(y%16)*256+(z%16)*16+x%16
    ix=(states['data'][1][i//per]>>((i%per)*bits))&((1<<bits)-1)
    return palette[ix]['Name'][1]


for x,y,z,kind in checks:
    value=block(x,y,z)
    if kind in ('gate','gorge_floor'):assert value=='minecraft:air',(kind,x,y,z,value)
    elif kind=='anchor':assert value=='minecraft:gold_block',(kind,x,y,z,value)
    elif kind=='water':assert value=='minecraft:water',(kind,x,y,z,value)
    elif kind=='ready':assert value.endswith('_wool')
    elif kind=='bank':assert value not in ('minecraft:water','minecraft:air'),(kind,x,y,z,value)
for be in block_entities:
    x,y,z=(be[k][1] for k in ('x','y','z'))
    if be['id'][1]=='minecraft:chest':assert block(x,y,z)=='minecraft:chest'
    else:
        assert block(x,y,z)=='minecraft:oak_sign'
        assert block(x,y-1,z) not in ('minecraft:air','minecraft:water')
assert chunks==manifest['chunks'] and containers==4
assert station_ids==set(range(4))
# A normal one-segment boat has an 11-block hook. Horizontal plate spacing and vertical
# offset fit that range; this is a geometry bound, not a claim of gameplay validation.
assert (7**2+(71-64.5)**2)**.5<11
assert all(a['s']<b['s'] for a,b in zip(manifest['gates'],manifest['gates'][1:]))
pack=SAVE/'datapacks/puffer_rally'
functions={p.stem:p for p in (pack/'data/puffer_rally/function').glob('*.mcfunction')}
lines=0
for name,path in functions.items():
    for line in path.read_text(encoding='utf-8').splitlines():
        lines+=1
        for ref in re.findall(r'\bfunction puffer_rally:([a-z0-9_/]+)',line):assert ref in functions,(name,ref)
        assert len(line)<32768
        for a,b in [('{','}'),('[',']')]: assert line.count(a)==line.count(b),(name,line)
for p in pack.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
json.loads((pack/'pack.mcmeta').read_text())
tick_text=functions['tick'].read_text(encoding='utf-8')
kit_text=functions['kit'].read_text(encoding='utf-8')
for action in ('rescue','boat','practice','leave','reset','help'):
    assert 'lr_use_'+action in tick_text
    assert 'LongboatRaceTool:"'+action+'"' in kit_text
assert 'y=65' in functions['race_tick'].read_text(encoding='utf-8')
for team in ('a','b'):
    starter=functions['start_'+team].read_text(encoding='utf-8')
    assert 'Segments:1,Width:1,LeftCount:1,RightCount:1,Compressed:0b,Puffers:0' in starter
    assert 'function puffer_rally:inventory' in starter
inventory=functions['inventory'].read_text()
assert 'clear @s' in inventory and 'give @s minecraft:pufferfish_bucket 2' in inventory
assert 'give @s minecraft:wooden_shovel 4' in inventory
assert 'item replace entity @s hotbar.1 with minecraft:mace' in inventory
assert inventory.index('hotbar.0 with minecraft:barrier')<inventory.index('function puffer_rally:kit')
assert inventory.index('give @s minecraft:wooden_shovel 4')<inventory.index('hotbar.0 with minecraft:air')
assert inventory.rstrip().endswith('tag @s add lr_select_empty')
for i in range(4):
    supply=functions['supply_'+str(i)].read_text(encoding='utf-8')
    assert 'unless score #phase lr_state matches 2' in supply
    assert 'unless entity @s[tag=lr_racer,scores={lr_place=0}]' in supply
    assert 'unless entity @s[distance=..8]' in supply
    assert f'if score @s lr_pit{i} = #round lr_epoch' in supply
    assert supply.count('Inventory[{Slot:')==36
    assert 'unless score #room lr_tmp matches 6.. run return 0' in supply
    for kind in ('oak_boat','wooden_shovel','pufferfish_bucket'):
        assert supply.count(f'give @s minecraft:{kind} 2')==1
    assert 'minecraft:mace' not in supply
    assert f'scoreboard players operation @s lr_pit{i} = #round lr_epoch' in supply
reset=functions['reset'].read_text()
assert 'scoreboard players add #round lr_epoch 1' in reset and 'function puffer_rally:scene_tick' in reset
assert 'function puffer_rally:inventory' in functions['reset_player'].read_text()
assert 'unless score @s lr_epoch = #round lr_epoch' in functions['scene_entity'].read_text()
report={'chunks':chunks,'regions':regions,'containers':containers,'signs':signs,'functions':len(functions),
        'command_lines':lines,'geometry_points_checked':len(checks),'length_blocks':manifest['length_blocks'],
        'nbt_and_reference_checks':'passed','minecraft_runtime_tested':False,'commands_parsed_by_minecraft':False}
(SAVE.parent/(SAVE.name+'_static_check.json')).write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
