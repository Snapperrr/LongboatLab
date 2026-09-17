"""Render actual saved landmark blocks for offline visual review, without starting Minecraft."""
import json
import math
import struct
import zlib
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw,ImageFont
from race_nbt import decode
import make_mountain_race_save as mountain

SAVE=mountain.OUT
manifest=json.loads((SAVE.parent/(SAVE.name+'_manifest.json')).read_text(encoding='utf-8'))
COLORS={'dark_prismarine':(38,91,88),'prismarine_bricks':(98,171,156),'oxidized_copper':(75,157,128),
 'waxed_weathered_copper':(79,135,108),'waxed_cut_copper':(182,105,68),'sea_lantern':(202,255,233),'end_rod':(242,237,208),
 'quartz_block':(237,228,210),'amethyst_block':(151,110,197),'purple_stained_glass':(137,82,180),
 'cyan_stained_glass':(84,187,204),'black_concrete':(28,28,36),'pearlescent_froglight':(247,217,244),
 'stripped_dark_oak_log':(83,60,43),'stripped_spruce_log':(118,85,50),'spruce_planks':(147,110,62),
 'spruce_log':(73,58,36),'spruce_fence':(107,80,48),'spruce_leaves':(54,102,60),
 'stone_bricks':(113,120,124),'red_terracotta':(150,69,46),'yellow_stained_glass':(255,210,101),
 'shroomlight':(249,178,88),'orange_wool':(245,141,53),'white_wool':(234,234,222),
 'hay_block':(196,164,47),'wheat':(197,176,70),'farmland':(110,74,48),'lantern':(255,200,92),
 'moss_block':(91,124,57),'bricks':(153,87,69),'chain':(81,89,104),'composter':(131,95,57),
 'snow_block':(222,238,245),'water':(64,139,175),'flowering_azalea':(136,158,88),'azalea':(98,138,61)}
COLORS.update({'blue_concrete':(49,66,157),'light_blue_concrete':(71,164,202),
 'cyan_concrete':(30,123,142),'blue_terracotta':(78,65,91),'light_blue_stained_glass':(125,190,240),
 'ochre_froglight':(248,214,137),'gold_block':(247,209,53),'orange_concrete':(225,111,28),
 'yellow_concrete':(240,177,23),'red_concrete':(172,48,42),'orange_stained_glass':(243,160,55),
 'magenta_stained_glass':(198,98,186),'pink_stained_glass':(229,161,194),
 'dark_oak_planks':(79,53,32),'white_stained_glass':(218,236,238),
 'yellow_terracotta':(186,133,36),'smooth_sandstone':(220,205,157),'orange_terracotta':(158,83,35),
 'white_concrete':(217,220,221),'pink_terracotta':(163,79,79),'pink_concrete':(214,102,142),
 'purple_concrete':(110,42,152),'dead_brain_coral_block':(129,122,119),
 'tuff':(107,108,97),'deepslate':(70,70,75),'basalt':(80,80,83),'stone':(124,126,127),
 'gravel':(131,126,126),'waxed_oxidized_copper':(73,148,123),'light_blue_terracotta':(113,109,139),
 'cyan_terracotta':(85,91,91),'black_terracotta':(42,27,23),'bone_block':(225,220,199),
 'brown_terracotta':(79,51,37),'oak_fence':(157,127,78),'light_blue_wool':(61,176,218),
 'cyan_wool':(23,137,145)})


def voxels(center,radius,ymin,ymax,include_water=False):
    x,z=center;result={};regions={}
    for cx in range(math.floor((x-radius)/16),math.ceil((x+radius)/16)):
        for cz in range(math.floor((z-radius)/16),math.ceil((z+radius)/16)):
            file=SAVE/'region'/f'r.{cx//32}.{cz//32}.mca'
            if file not in regions:regions[file]=file.read_bytes() if file.exists() else b''
            raw=regions[file]
            if not raw:continue
            location=struct.unpack_from('>I',raw,((cz%32)*32+cx%32)*4)[0]
            if not location:continue
            start=(location>>8)*4096;n=struct.unpack_from('>I',raw,start)[0]
            chunk=decode(zlib.decompress(raw[start+5:start+4+n]))
            for sec in chunk['sections'][1][1]:
                sy=sec['Y'][1]
                if sy*16>ymax or sy*16+15<ymin:continue
                data=sec['block_states'][1];palette=[p['Name'][1].split(':')[1] for p in data['palette'][1][1]]
                if len(palette)==1:indices=np.zeros(4096,dtype=int)
                else:
                    bits=max(4,(len(palette)-1).bit_length());per=64//bits
                    words=np.array([v&((1<<64)-1) for v in data['data'][1]],np.uint64);i=np.arange(4096,dtype=np.uint64)
                    indices=((words[i//per]>>((i%per)*bits))&((1<<bits)-1)).astype(int)
                for i,pid in enumerate(indices):
                    name=palette[pid]
                    if name in ('air','light') or (name=='water' and not include_water):continue
                    yy=sy*16+i//256;zz=cz*16+(i//16)%16;xx=cx*16+i%16
                    if ymin<=yy<=ymax and abs(xx-x)<=radius and abs(zz-z)<=radius:
                        result[xx,yy,zz]=name
    return result


def connected(data,seed):
    """Isolate one sculpture for detail plates; neighboring coastal props stay in the save."""
    from collections import deque
    seed=min(data,key=lambda p:sum((a-b)**2 for a,b in zip(p,seed)))
    todo=deque([seed]);seen={seed}
    offsets=[(x,y,z) for x in (-1,0,1) for y in (-1,0,1) for z in (-1,0,1) if (x,y,z)!=(0,0,0)]
    while todo:
        x,y,z=todo.popleft()
        for a,b,c in offsets:
            p=x+a,y+b,z+c
            if p in data and p not in seen:seen.add(p);todo.append(p)
    return {p:data[p] for p in seen}


def render(data,title,subtitle,path,turns=0,underside=False):
    for _ in range(turns%4):data={(z,y,-x):name for (x,y,z),name in data.items()}
    if underside:data={(x,-y,z):name for (x,y,z),name in data.items()}
    project=lambda p:((p[0]-p[2])*.866,(p[0]+p[2])*.5-p[1])
    points=np.array([project(p) for p in data]);lo=points.min(axis=0);hi=points.max(axis=0)
    scale=min(1250/(hi[0]-lo[0]+3),650/(hi[1]-lo[1]+3));offset=np.array([60.,100.])-lo*scale
    im=Image.new('RGB',(1380,830),'#172c38');draw=ImageDraw.Draw(im)
    faces=[((0,1,0),[(0,1,0),(1,1,0),(1,1,1),(0,1,1)],1.12),
           ((1,0,0),[(1,0,0),(1,0,1),(1,1,1),(1,1,0)],.83),
           ((0,0,1),[(0,0,1),(0,1,1),(1,1,1),(1,0,1)],.65)]
    for (x,y,z),name in sorted(data.items(),key=lambda item:sum(item[0])):
        color=COLORS.get(name,(121,130,121))
        for (dx,dy,dz),vertices,shade in faces:
            if (x+dx,y+dy,z+dz) in data:continue
            polygon=[tuple(np.asarray(project((x+a,y+b,z+c)))*scale+offset) for a,b,c in vertices]
            draw.polygon(polygon,fill=tuple(min(255,int(v*shade)) for v in color))
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    draw.rectangle((0,0,1380,85),fill='#172c38')
    draw.text((35,18),title,font=font(28),fill='#f3e7c2')
    draw.text((35,59),subtitle,font=font(15),fill='#b6d8dd')
    draw.text((35,790),'存档方块离线预览 · 非游戏截图；游戏内含照明、半透明玻璃与局部粒子',font=font(17),fill='#b6d8dd')
    im.save(path)


if __name__=='__main__':
    landmarks=manifest['decor']['landmarks'];tiles=[]
    for kind in ('twin_dragons','star_whale','phoenix','jellyfish','sky_ship','puffer_moon','enchanted_oar','giant_coral'):
        v=next(v for v in landmarks if v['kind']==kind);x,y,z=v['center']
        path=SAVE.parent/(SAVE.name+'_'+kind+'.png')
        data=voxels((x,z),v['preview_radius'],v['ymin'],v['ymax'])
        if kind=='phoenix':data=connected(data,v['center'])
        render(data,v['name'],
               'V9 重塑 / 曲面、层次、镶边与独立轮廓 / 实际存档方块',path)
        tiles.append(path);print('Rendered',kind,flush=True)
        if kind=='twin_dragons':
            head=v['heads'][0][0];hx,hy,hz=head
            render(voxels((hx,hz),42,hy-26,hy+32),'玉龙首部 · 眉骨、口鼻与角须',
                   '实际存档近景 / 头部沿颈部与龙珠方向构建',SAVE.parent/(SAVE.name+'_dragon_head.png'))
        elif kind=='phoenix':
            render(data,'金凰下方视角 · 腹部、爪与飞羽','从赛道下方仰视的体素结构 / 非游戏截图',
                   SAVE.parent/(SAVE.name+'_phoenix_under.png'),turns=1,underside=True)
    path=SAVE.parent/(SAVE.name+'_crater.png')
    render(voxels(mountain.SPIRAL_CENTER,75,168,218,include_water=True),'雪冠死火山 · 收紧的深蓝湖口',
           '约 54×44 格湖面 / 宽厚雪脊、分层岩壁与静水深潭',path)
    tiles.append(path)
    contact=Image.new('RGB',(1380,830),'#172c38')
    for i,path in enumerate(tiles):
        with Image.open(path) as im:contact.paste(im.resize((460,276),Image.Resampling.LANCZOS),(i%3*460,i//3*276))
    contact.save(SAVE.parent/(SAVE.name+'_wonders.png'))
    print('Rendered 9 saved-voxel scenery previews and contact sheet.')
