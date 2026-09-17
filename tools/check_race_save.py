"""Offline artifact inspection only: NBT/Anvil integrity, palettes, containers and function links."""
from pathlib import Path
import gzip
import json
import re
import struct
import zlib
from race_nbt import decode

ROOT=Path(__file__).resolve().parents[1]
SAVE=ROOT/'maps/Puffer_Rally_2100'
level=decode(gzip.decompress((SAVE/'level.dat').read_bytes()))['Data'][1]
assert level['DataVersion'][1]==3955
chunks={};containers=0;signs=0
for region in (SAVE/'region').glob('*.mca'):
    raw=region.read_bytes();occupied=set()
    rx,rz=map(int,region.stem.split('.')[1:])
    for i in range(1024):
        entry=struct.unpack_from('>I',raw,i*4)[0]
        if not entry:continue
        offset,count=entry>>8,entry&255
        assert offset>=2 and offset+count<=len(raw)//4096
        assert not occupied.intersection(range(offset,offset+count));occupied.update(range(offset,offset+count))
        n=struct.unpack_from('>I',raw,offset*4096)[0]
        assert n+4<=count*4096 and raw[offset*4096+4]==2
        c=decode(zlib.decompress(raw[offset*4096+5:offset*4096+4+n]))
        cx,cz=c['xPos'][1],c['zPos'][1]
        assert cx==rx*32+i%32 and cz==rz*32+i//32
        assert c['Status'][1]=='minecraft:full'
        chunks[cx,cz]=c
        for sec in c['sections'][1][1]:
            states=sec['block_states'][1];palette=states['palette'][1][1]
            if len(palette)>1:
                bits=max(4,(len(palette)-1).bit_length());per=64//bits;mask=(1<<bits)-1
                words=states['data'][1];assert len(words)==(4096+per-1)//per
                for index in range(4096):assert ((words[index//per]>>((index%per)*bits))&mask)<len(palette)
        for be in c['block_entities'][1][1]:
            if be['id'][1]=='minecraft:chest':
                containers+=1;items=be['Items'][1][1];slots=[s['Slot'][1] for s in items]
                assert len(set(slots))==len(slots) and all(0<=v<27 for v in slots)
                assert all(s['count'][1]>=1 for s in items)
            if be['id'][1]=='minecraft:sign':signs+=1

def block(x,y,z):
    chunk=chunks[x//16,z//16]
    sec=next(s for s in chunk['sections'][1][1] if s['Y'][1]==y//16)
    states=sec['block_states'][1];palette=states['palette'][1][1]
    if len(palette)==1:return palette[0]['Name'][1]
    bits=max(4,(len(palette)-1).bit_length());per=64//bits;i=(y%16)*256+(z%16)*16+x%16
    ix=(states['data'][1][i//per]>>((i%per)*bits))&((1<<bits)-1)
    return palette[ix]['Name'][1]

for c in chunks.values():
    for be in c['block_entities'][1][1]:
        x,y,z=(be[n][1] for n in ('x','y','z'));kind=be['id'][1]
        if kind=='minecraft:chest':assert block(x,y,z)=='minecraft:chest'
        if kind=='minecraft:sign':
            assert block(x,y,z)=='minecraft:oak_sign'
            assert block(x,y-1,z) not in ['minecraft:air','minecraft:water']
for cx in [-24,24]:
    for z in [-20,8,216,408,600,792,984,1176,1368,1560,1752,1944]:
        assert block(cx,63,z)=='minecraft:water',(cx,z,block(cx,63,z))
        assert block(cx,64,z)=='minecraft:air',(cx,z,block(cx,64,z))
    assert block(cx,65,-70).endswith('_wool')
    assert block(cx,55,1050)=='minecraft:blue_ice'
    assert block(cx,63,450)=='minecraft:blue_ice'
    for side in [-1,1]:assert block(cx+side*11,65,804)=='minecraft:air'
pack=SAVE/'datapacks/puffer_rally'
functions={p.stem:p for p in (pack/'data/puffer_rally/function').glob('*.mcfunction')}
lines=0
for name,path in functions.items():
    for line in path.read_text(encoding='utf-8').splitlines():
        lines+=1
        for ref in re.findall(r'\bfunction puffer_rally:([a-z0-9_/]+)',line):assert ref in functions,(name,ref)
        assert len(line)<32768
        for ch in ('{','['):assert line.count(ch)==line.count('}' if ch=='{' else ']'),(name,line)
for p in pack.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'))
report={'chunks':len(chunks),'containers':containers,'signs':signs,'functions':len(functions),'command_lines':lines,
        'nbt_and_reference_checks':'passed','minecraft_runtime_tested':False,'commands_parsed_by_minecraft':False}
(SAVE.parent/'Puffer_Rally_2100_static_check.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report,indent=2))
