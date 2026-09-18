"""Inspect V11 Anvil data, mounted supply access, clearance, scenery and lighting offline."""
from pathlib import Path
import gzip
import json
import re
import struct
import zlib
from collections import defaultdict
from itertools import permutations
import numpy as np
from race_nbt import decode
import make_mountain_race_save as mountain

mountain.configure();mountain.shortcuts()
course=mountain.course
SAVE=mountain.OUT
manifest=json.loads((SAVE.parent/(SAVE.name+'_manifest.json')).read_text(encoding='utf-8'))
assert 4000<manifest['length_blocks']<5000
assert len(manifest['shortcuts'])==2 and len(manifest['structures'])==4
assert manifest['mountain_end_y']==int(mountain.water_y(mountain.LENGTH)) and len(manifest['arrows'])>150
assert manifest['countdown_seconds']==10 and manifest['lake_y']==179
assert len(manifest['lights'])>200
assert manifest['spiral_turns']==1.10
assert len(manifest['supports'])>30
level=decode(gzip.decompress((SAVE/'level.dat').read_bytes()))['Data'][1]
assert level['DataVersion'][1]==3955 and level['SpawnY'][1]==65
assert level['DragonFight'][0]==10 and level['DragonFight'][1]['NeedsStateScanning'][1]==1
checks=[];glyph_failures=[]


def ray_voxels(start,end):
    """DDA through every grid cell traversed by the seated player's block ray."""
    start=np.asarray(start,float);end=np.asarray(end,float);delta=end-start
    cell=np.floor(start).astype(int);last=np.floor(end).astype(int);step=np.sign(delta).astype(int)
    stride=np.divide(1.,np.abs(delta),out=np.full(3,np.inf),where=delta!=0)
    boundary=cell+(step>0)
    when=np.divide(boundary-start,delta,out=np.full(3,np.inf),where=delta!=0)
    result=[]
    for _ in range(64):
        result.append(tuple(int(v) for v in cell))
        if np.array_equal(cell,last):return result
        axes=when<=when.min()+1e-10;cell[axes]+=step[axes];when[axes]+=stride[axes]
    raise AssertionError('ray did not reach service block')


def hull_cells(center,tangent,half_length):
    """Conservative full rectangular hull vs saved voxel columns, using separating axes."""
    normal=np.array([tangent[1],-tangent[0]]);half_width=11/16
    extent=np.abs(tangent)*half_length+np.abs(normal)*half_width
    for x in range(int(np.floor(center[0]-extent[0])),int(np.ceil(center[0]+extent[0]))):
        for z in range(int(np.floor(center[1]-extent[1])),int(np.ceil(center[1]+extent[1]))):
            d=np.array([x+.5,z+.5])-center
            if abs(d@tangent)>=half_length+.5*np.abs(tangent).sum()-1e-6:continue
            if abs(d@normal)>=half_width+.5*np.abs(normal).sum()-1e-6:continue
            if np.any(np.abs(d)>=extent+.5-1e-6):continue
            yield x,z


service_reach=[];hull_approach=set();service_rays=0
for x in range(int(mountain.SPIRAL_CENTER[0])-60,int(mountain.SPIRAL_CENTER[0])+61,12):
    for z in range(int(mountain.SPIRAL_CENTER[1])-60,int(mountain.SPIRAL_CENTER[1])+61,12):
        rho=float(mountain.lake_radius(x,z))
        if rho<=1:
            checks.append((x,mountain.LAKE_Y,z,'water','summit lake'))
            checks.append((x,mountain.LAKE_Y+1,z,'air','lake surface'))
            checks.append((x,mountain.lake_bed(rho),z,'gravel','crater lake bed'))
        elif rho<2.8:
            shore=int(mountain.mountain_height(x,z))
            checks.append((x,shore,z,'snow_block' if rho>1.23 else 'basalt','crater rim'))
            checks.append((x,shore+1,z,'air','crater skyline'))
            if 1.43<rho<1.78:assert shore>=200,('low crater rim',x,z,shore)
        else:checks.append((x,int(mountain.mountain_height(x,z)),z,'solid','mountain summit'))
for x,y,z,kind in manifest['finish_cells']:checks.append((x,y,z,kind,'finish harbor'))
for x,y,z,kind in manifest['lights']:checks.append((x,y,z,kind,'lighting fixture'))
for x,y,z,kind in manifest['decor']['checks']:checks.append((x,y,z,kind,'scenery landmark'))
for x,y,z,kind in manifest['decor']['ground_checks']:checks.append((x,y,z,kind,'riverbank detail'))
assert len(manifest['decor']['ground_details'])>100
landmarks=manifest['decor']['landmarks']
assert manifest['decor']['sculpture_revision']==9 and manifest['decor']['static_blocks']
for kind in ('twin_dragons','star_whale','phoenix','jellyfish','sky_ship','puffer_moon','enchanted_oar'):
    assert sum(v['kind']==kind for v in landmarks)==1,kind
corals=[v for v in landmarks if v['kind']=='giant_coral']
assert len(corals)>=6 and len({v['variant'] for v in corals})==3
assert sum(v['kind']=='hillside_house' for v in landmarks)>=6
# Dense radial checks ensure a continuous, substantial wall encloses the water on every side.
for theta in np.linspace(0,np.pi*2,96,endpoint=False):
    irregular=1+.06*np.sin(theta*3)+.04*np.cos(theta*5)
    for rho in (1.04,1.2,1.44,1.60,1.78):
        x=round(mountain.SPIRAL_CENTER[0]+27*irregular*rho*np.cos(theta))
        z=round(mountain.SPIRAL_CENTER[1]+22*irregular*rho*np.sin(theta))
        checks.append((x,mountain.LAKE_Y,z,'solid','sealed crater wall'))
        if rho>=1.44:
            checks.append((x,199,z,'solid','thick crater crest'))
end_p,end_t=course.at(mountain.LENGTH-28);end_y=int(mountain.water_y(mountain.LENGTH))
for along in range(-14,80,2):
    for side in (-10,0,10):
        x,z=mountain.frame_point(end_p,end_t,along,side)
        checks.append((x,end_y,z,'water','finish runout'))
        checks.append((x,end_y+2,z,'air','finish runout clearance'))
for side in range(-25,26,2):
    x,z=mountain.frame_point(end_p,end_t,90,side)
    checks.append((x,end_y,z,'solid','closed end promenade'))
for x in (-5,5):
    checks.append((x,64,-17,'solid','starting deck'))
    checks.append((x,65,-17,'air','starting boat'))
    checks.append((x,63,-8,'water','launch water'))
for station in (180,255):
    s=station*mountain.FLAT_SCALE
    for side in (-8,0,8):
        x,z=course.pos(s,side);checks.append((x,64,z,'orange_concrete','spring obstacle'))
for start,end in ((700,765),(2800,3000)):
    for station in np.linspace(start+4,end-4,8):
        x,z=course.pos(station*mountain.FLAT_SCALE)
        checks.append((x,63,z,'blue_ice','land section'))
for station in (1174,1194,1214):
    x,z=course.pos((station+2)*mountain.FLAT_SCALE)
    checks.append((x,65,z,'moss_block','grapple landing'))
x,z=course.pos(1187*mountain.FLAT_SCALE)
checks.extend([(x,50,z,'air','dry grapple gorge'),(x,38,z,'cobbled_deepslate','gorge bottom')])
assert manifest['supply_order']==['oars','boats','fish','fish']
assert len(manifest['supply_stations'])==4
for index,station in enumerate(manifest['supply_stations']):
    kind=manifest['supply_order'][index]
    assert station['kind']==kind and len(station['crates'])==1
    assert station['crates'][0]['kind']==kind
    service=station['service'];berth=np.array(station['berth']);t=np.array(station['forward'])
    assert [v['kind'] for v in service]==(['chest','crafting_table'] if kind=='boats' else ['chest'])
    if kind=='boats':
        assert sum(abs(service[0][v]-service[1][v]) for v in ('x','y','z'))==1,'workbench must touch boat chest'
    for target in service:
        x,y,z=(target[k] for k in ('x','y','z'))
        checks.extend([(x,y,z,target['kind'],'waterfront service'),(x,y+1,z,'air','service headroom')])
        # Check both eye positions within a one-compartment seat, including an offset rider.
        for seat in (-.3,.3):
            eye=berth+np.array([t[0]*seat,1.2,t[1]*seat]);aim=np.array([x+.5,y+.4375,z+.5])
            distance=float(np.linalg.norm(aim-eye));assert distance<=4.5,('service beyond block reach',index,distance)
            service_reach.append(distance);service_rays+=1
            for point in ray_voxels(eye,aim):
                if point!=(x,y,z):checks.append((*point,'passable','seated service line of sight'))
    normal=np.array([t[1],-t[0]])
    # Sweep 1- and 3-compartment hulls laterally from the main water lane to the berth.
    for half_length in (1,3):
        for offset in np.arange(0,14.01,.25):
            for x,z in hull_cells(berth[[0,2]]-normal*offset,t,half_length):
                for y in (63,64,65):hull_approach.add((x,y,z,'passable','berthing hull clearance'))
    for x,y,z in station['water']:
        checks.append((x,y,z,'water','supply basin'))
        checks.append((x,62,z,'solid','one-block supply basin floor'))
        checks.append((x,61,z,'solid','sealed supply basin foundation'))
    for x,y,z in station['clearance']:
        for dy in (0,2,5):checks.append((x,y+dy,z,'air','drive-through clearance'))
    edge=int(np.ceil(mountain.half_width(station['s'])))
    for along in (-6,-3,0,3,6):
        for side in (edge-2,edge-1,edge,edge+1):
            x,z=course.pos(station['s']+along,side)
            for y in (64,65,66):checks.append((x,y,z,'air','supply approach'))
            if side==edge-2:checks.append((x,63,z,'water','supply water approach'))
checks.extend(sorted(hull_approach))
for support in manifest['supports']:
    for x in range(support['x']-1,support['x']+2):
        for z in range(support['z']-1,support['z']+2):
            for y in range(support['bottom']-1,support['top']+1):checks.append((x,y,z,'solid','continuous pier'))
    for x,y,z in support.get('beam',[]):checks.append((x,y,z,'solid','pier cap beam'))
for arrow in manifest['arrows']:
    glyph={tuple(p) for p in arrow['glyph']}
    assert len(glyph)>25
    seen_glyph={next(iter(glyph))};pending=list(seen_glyph)
    while pending:
        x,y,z=pending.pop()
        for next_point in ((x-1,y,z),(x+1,y,z),(x,y,z-1),(x,y,z+1)):
            if next_point in glyph and next_point not in seen_glyph:
                seen_glyph.add(next_point);pending.append(next_point)
    if seen_glyph!=glyph:glyph_failures.append(('disconnected arrow',arrow['x'],arrow['z']))
    for x,y,z in glyph:checks.append((x,y,z,arrow['color'],'arrow glyph'))
    for x,y,z in arrow['backing']:checks.append((x,y,z,'polished_deepslate','arrow backing'))
for g in manifest['gates']:
    checks.append((round(g['x']),g['y']+1,round(g['z']),'air','gate '+g['title']))
for s in np.arange(mountain.MOUNTAIN_START+3,mountain.LENGTH-2,3):
    y=int(mountain.water_y(s));width=float(mountain.half_width(s))
    for side in (-width+3,0,width-3):
        x,z=course.pos(s,side)
        # Neighboring rising cross-sections may already raise the surface one block.
        checks.append((x,y,z,'surface','spiral water '+str(round(s,1))))
        for h in (2,3,4):checks.append((x,y+h,z,'air','spiral clearance '+str(round(s,1))))
        checks.append((x,y,z,'bed','aqueduct bed '+str(round(s,1))))
for section in manifest['structures']:
    s=(section['start']+section['end'])/2;y=int(mountain.water_y(s));x,z=course.pos(s)
    checks.append((x,y+section['clearance']+2,z,'solid',section['kind']+' roof'))
for branch in mountain.SHORTCUTS:
    assert not any(branch['entry_s']<g['s']<branch['exit_s'] for g in manifest['gates'])
    assert branch['exit_s']-branch['entry_s']-branch['length']>90
    for i,p in enumerate(branch['points'][::4]):
        if abs(i*4-len(branch['points'])//2)<10:continue
        x,z=np.rint(p).astype(int)
        checks.append((int(x),63,int(z),'water','shortcut'))
        checks.append((int(x),65,int(z),'air','shortcut clearance'))
for s in np.arange(3700*mountain.FLAT_SCALE,mountain.MOUNTAIN_START-10,9):
    x,z=course.pos(s)
    checks.append((x,63,z,'water','flat late channel '+str(round(s,1))))
    checks.append((x,66,z,'air','flat late clearance '+str(round(s,1))))
lighting_samples=[]
for s in np.arange(mountain.MOUNTAIN_START+4,mountain.LENGTH-54,4):
    y=int(mountain.water_y(s))
    for side in np.linspace(-float(mountain.half_width(s))+3,float(mountain.half_width(s))-3,9):
        x,z=course.pos(s,side)
        lighting_samples.append((x,y+1,z))
needed={(x//16,z//16) for x,y,z,kind,label in checks}
for x,y,z in lighting_samples:
    needed.update(((x+dx)//16,(z+dz)//16) for dx in (-16,0,16) for dz in (-16,0,16))
selected={};entities=[];chunks=0;regions=0
for region in sorted((SAVE/'region').glob('*.mca')):
    regions+=1;raw=region.read_bytes();occupied=set();rx,rz=map(int,region.stem.split('.')[1:])
    for i in range(1024):
        entry=struct.unpack_from('>I',raw,i*4)[0]
        if not entry:continue
        offset,count=entry>>8,entry&255
        assert offset>=2 and offset+count<=len(raw)//4096
        sectors=set(range(offset,offset+count));assert not occupied&sectors;occupied|=sectors
        n=struct.unpack_from('>I',raw,offset*4096)[0]
        assert n+4<=count*4096 and raw[offset*4096+4]==2
        chunk=decode(zlib.decompress(raw[offset*4096+5:offset*4096+4+n]))
        cx,cz=chunk['xPos'][1],chunk['zPos'][1]
        assert (cx,cz)==(rx*32+i%32,rz*32+i//32)
        assert chunk['Status'][1]=='minecraft:full' and len(chunk['sections'][1][1])==24
        for sec in chunk['sections'][1][1]:
            states=sec['block_states'][1];palette=states['palette'][1][1]
            if len(palette)>1:
                bits=max(4,(len(palette)-1).bit_length());per=64//bits
                words=np.array([v&((1<<64)-1) for v in states['data'][1]],np.uint64)
                assert len(words)==(4096+per-1)//per
                indexes=np.arange(4096,dtype=np.uint64)
                values=(words[indexes//per]>>((indexes%per)*bits))&((1<<bits)-1)
                assert int(values.max())<len(palette)
        be=chunk['block_entities'][1][1];entities.extend(be)
        if (cx,cz) in needed or be:selected[cx,cz]=chunk
        chunks+=1
    print('Inspected',region.name,flush=True)
assert chunks==manifest['chunks']


def block(x,y,z):
    c=selected[x//16,z//16]
    sec=next(v for v in c['sections'][1][1] if v['Y'][1]==y//16)
    states=sec['block_states'][1];palette=states['palette'][1][1]
    if len(palette)==1:return palette[0]['Name'][1]
    bits=max(4,(len(palette)-1).bit_length());per=64//bits;i=(y%16)*256+(z%16)*16+x%16
    return palette[(states['data'][1][i//per]>>((i%per)*bits))&((1<<bits)-1)]['Name'][1]


failures=list(glyph_failures)
for x,y,z,kind,label in checks:
    actual=block(x,y,z)
    if kind in ('surface','bed'):
        surfaces=[y+dy for dy in (-1,0,1) if block(x,y+dy,z)=='minecraft:water']
        good=bool(surfaces) and (kind=='surface' or block(x,max(surfaces)-4,z) not in ('minecraft:air','minecraft:water'))
    elif kind=='air':good=actual in ('minecraft:air','minecraft:light')
    elif kind=='passable':good=actual in ('minecraft:air','minecraft:water','minecraft:light')
    else:good=actual not in ('minecraft:air','minecraft:water','minecraft:light') if kind=='solid' else actual=='minecraft:'+kind
    if not good:failures.append((x,y,z,kind,actual,label))
# A clear axis-aligned air path is a conservative lower bound on vanilla block-light propagation.
# Check actual saved voxels; counting lamps alone misses fixtures buried inside the tunnel shell.
light_cells=defaultdict(list)
for x,y,z,kind in manifest['lights']:
    if kind=='light':light_cells[x//16,z//16].append((x,y,z))
light_levels=[]
for point in lighting_samples:
    # Rasterized terrace joins can raise the local water surface by one block.
    while block(*point)=='minecraft:water':point=(point[0],point[1]+1,point[2])
    x,y,z=point;best=0
    candidates=[source for dx in (-1,0,1) for dz in (-1,0,1)
                for source in light_cells[x//16+dx,z//16+dz]]
    candidates.sort(key=lambda source:sum(abs(a-b) for a,b in zip(source,point)))
    for source in candidates:
        level=15-sum(abs(a-b) for a,b in zip(source,point))
        if level<=best:break
        for axes in permutations(range(3)):
            current=list(source);clear=True
            for axis in axes:
                while current[axis]!=point[axis]:
                    current[axis]+=1 if point[axis]>current[axis] else -1
                    if block(*current) not in ('minecraft:air','minecraft:light'):
                        clear=False;break
                if not clear:break
            if clear:best=level;break
    light_levels.append(best)
    if best<8:failures.append((*point,'block light >=8',best,'dark navigation lane'))
seen=set();stations=set();crate_counts={}
if failures:print('Geometry failures:',len(failures),failures[:15],flush=True)
for be in entities:
    p=tuple(be[k][1] for k in ('x','y','z'))
    assert p not in seen,('duplicate block entity',p)
    seen.add(p)
    kind=be['id'][1]
    if block(*p)!=('minecraft:oak_sign' if kind=='minecraft:sign' else kind):
        failures.append((*p,kind,block(*p),'block entity mismatch'))
    if kind=='minecraft:chest':
        meta=be['components'][1]['minecraft:custom_data'][1]
        station=meta['LongboatRaceStation'][1];supply=meta['LongboatRaceSupply'][1]
        assert meta['LongboatSupplyVersion'][1]==3
        assert supply==manifest['supply_order'][station]
        assert station not in stations,('multiple resources at one station',station)
        entries=be['Items'][1][1]
        expected={'fish':('minecraft:pufferfish_bucket',4),'oars':('minecraft:wooden_shovel',6),'boats':('minecraft:oak_boat',2)}[supply]
        assert {row['id'][1] for row in entries}=={expected[0]},('mixed crate',p)
        assert sum(row['count'][1] for row in entries)==expected[1],('supply amount',p)
        crate_counts[station,supply]=expected[1]
        stations.add(station)
assert stations=={0,1,2,3}
assert len(crate_counts)==4
pack=SAVE/'datapacks/puffer_rally'
functions={p.stem:p.read_text(encoding='utf-8') for p in (pack/'data/puffer_rally/function').glob('*.mcfunction')}
for name,body in functions.items():
    for ref in re.findall(r'\bfunction puffer_rally:([a-z0-9_/]+)',body):assert ref in functions,(name,ref)
    for line in body.splitlines():
        assert len(line)<32768
        for a,b in [('{','}'),('[',']')]:assert line.count(a)==line.count(b),(name,line)
for p in pack.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
assert '10.. run return 0' in functions['navigation_tick']
assert 'scoreboard players set @s lr_warn 6' in functions['off_course']
assert 'hotbar.0 with minecraft:air' in functions['inventory']
assert 'hotbar.1 with minecraft:mace' in functions['inventory']
assert 'sidebar lr_sec' in functions['display_setup']
assert 'lr_sec /= #twenty lr_tmp' in functions['display_time']
assert 'as @a[tag=lr_racer] run function puffer_rally:display_time' in functions['tick']
assert 'LongboatSupplyVersion' not in functions['tick']
assert 'if score #housekeeping' in functions['tick']
assert '20 ticks' not in ''.join(functions.values())
assert 'normal @a[distance=..72]' in functions['scenery_tick']
assert 'scoreboard players set #count lr_state 200' in functions['countdown']
assert '"text":"10"' in functions['countdown']
for number in range(1,10):
    assert f'if score #count lr_state matches {number*20} run title' in functions['count_tick']
    assert f'"text":"{number}"' in functions['count_tick']
for team,x in [('a',-5),('b',5)]:
    assert f'summon minecraft:boat {x} 65 -17 ' in functions['start_'+team]
    assert f'on vehicle run tp @s {x} 65 -17 ' in functions['count_tick']
assert all(f'nav_{i}' in functions for i in range(len(manifest['gates'])-1))
assert 'tag=lr_rescue_pending] run return 0' in functions['rescue']
assert 'scoreboard players add @s lr_time' not in functions['rescue']
assert 'tag @s remove lr_rescue_pending' in functions['leave']
for i in range(len(manifest['gates'])):
    assert 'longboatlab race_prepare' in functions[f'return_{i}']
    assert 'longboatlab race_move' in functions[f'return_ready_{i}']
    assert 'on vehicle run tp' not in functions[f'return_ready_{i}']
for i,kind in enumerate(manifest['supply_order']):
    body=functions[f'supply_{i}_{kind}']
    assert f'lr_p{i}_{kind} = #round lr_epoch' in body
    assert f'longboatlab race_supply {kind}' in body
    assert body.index('unless score #granted')<body.index(f'scoreboard players operation @s lr_p{i}_{kind}')
    assert {k for k in ('fish','oars','boats') if f'supply_{i}_{k}' in functions}=={kind}
# Projected intersections must have enough vertical separation for the lower boat and bridge deck.
pts=mountain.POINTS[::10];ds=mountain.DISTANCES[::10];ys=mountain.water_y(ds)
near=np.sum((pts[:,None]-pts[None,:])**2,axis=2)<30**2
nonlocal_pair=np.abs(ds[:,None]-ds[None,:])>350
crossings=near&nonlocal_pair
if np.any(crossings) and np.min(np.abs(ys[:,None]-ys[None,:])[crossings])<26:
    failures.append(('crossing clearance',int(np.min(np.abs(ys[:,None]-ys[None,:])[crossings]))))
report={'chunks':chunks,'regions':regions,'block_entities':len(entities),'arrows':len(manifest['arrows']),
    'continuous_piers':len(manifest['supports']),'length_blocks':manifest['length_blocks'],
    'light_sources':len(manifest['lights']),'countdown_seconds':10,'finish_harbor_checked':True,
    'night_lighting_samples':len(light_levels),'minimum_block_light_lower_bound':min(light_levels),
    'functions':len(functions),'single_kind_crates':len(crate_counts),'ground_details':len(manifest['decor']['ground_details']),
    'station_resource_order':manifest['supply_order'],'seated_service_rays':service_rays,
    'maximum_service_reach':round(max(service_reach),3),'hull_approach_voxels':len(hull_approach),
    'boat_workbench_adjacent':True,'dragon_fight_data_present':True,
    'geometry_checks':len(checks),'failures':failures,
    'failure_count':len(failures),'minecraft_runtime_tested':False,'commands_parsed_by_minecraft':False}
(SAVE.parent/(SAVE.name+'_static_check.json')).write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
assert not failures, f'{len(failures)} obstructed or discontinuous geometry samples'
