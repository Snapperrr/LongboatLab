"""Export actual V12 Anvil blocks through an orthographic camera, without launching Minecraft.

Only exposed voxel faces are rasterized. Chunk decoding is cached, bounded and vectorized;
the images have no perspective, labels or route overlays, for reuse outside this project.
"""
import argparse
import functools
import io
import json
import math
import re
import struct
import zipfile
import zlib
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont
import make_mountain_race_save as mountain
from race_nbt import decode
from preview_rally_scenery import COLORS

ROOT=Path(__file__).resolve().parents[1]
SAVE=mountain.OUT
OUT=ROOT/'Stage_Orthographs_V12'
MANIFEST=json.loads((SAVE.parent/(SAVE.name+'_manifest.json')).read_text(encoding='utf-8'))
mountain.configure() # Use V12's shortened route, not the imported V2 course's original path.
for gate in MANIFEST['gates']:
    assert np.linalg.norm(mountain.course.at(gate['s'])[0]-[gate['x'],gate['z']])<5
NAMES=['air'];IDS={'air':0}
RES=2304,1536
TEXTURES=None


@functools.lru_cache(maxsize=5)
def region(rx,rz):
    p=SAVE/'region'/f'r.{rx}.{rz}.mca'
    return p.read_bytes() if p.exists() else b''


@functools.lru_cache(maxsize=128)
def chunk(cx,cz):
    raw=region(cx//32,cz//32);result={}
    if not raw:return result
    loc=struct.unpack_from('>I',raw,((cz%32)*32+cx%32)*4)[0]
    if not loc:return result
    start=(loc>>8)*4096;n=struct.unpack_from('>I',raw,start)[0]
    tag=decode(zlib.decompress(raw[start+5:start+4+n]))
    for section in tag['sections'][1][1]:
        sy=section['Y'][1];states=section['block_states'][1];palette=[]
        for p in states['palette'][1][1]:
            name=p['Name'][1].split(':')[-1]
            if name in ('air','cave_air','void_air','light'):name='air'
            if name not in IDS:IDS[name]=len(NAMES);NAMES.append(name)
            palette.append(IDS[name])
        if len(palette)==1:indices=np.zeros(4096,dtype=np.int32)
        else:
            bits=max(4,(len(palette)-1).bit_length());per=64//bits
            words=np.array([v&((1<<64)-1) for v in states['data'][1]],np.uint64)
            i=np.arange(4096,dtype=np.uint64)
            indices=((words[i//per]>>((i%per)*bits))&((1<<bits)-1)).astype(np.int32)
        result[sy]=np.array(palette,dtype=np.uint16)[indices].reshape((16,16,16))
    return result


def volume(bounds,ymin,ymax):
    x0,z0,x1,z1=bounds
    data=np.zeros((ymax-ymin+1,z1-z0+1,x1-x0+1),np.uint16)
    for cx in range(x0//16,x1//16+1):
        for cz in range(z0//16,z1//16+1):
            ax,bx=max(x0,cx*16),min(x1+1,cx*16+16)
            az,bz=max(z0,cz*16),min(z1+1,cz*16+16)
            for sy,section in chunk(cx,cz).items():
                ay,by=max(ymin,sy*16),min(ymax+1,sy*16+16)
                if ay>=by:continue
                data[ay-ymin:by-ymin,az-z0:bz-z0,ax-x0:bx-x0]=section[ay-sy*16:by-sy*16,az-cz*16:bz-cz*16,ax-cx*16:bx-cx*16]
    return data


@functools.lru_cache(maxsize=2048)
def color(name):
    if name=='water':return 58,126,169
    if name.endswith('_leaves'):return (68,113,62) if 'spruce' in name else (91,144,63)
    if name in ('grass_block','grass','short_grass','tall_grass','fern','large_fern'):return 96,143,61
    if name in COLORS and any(s in name for s in ('glass','light','lantern','coral')):return COLORS[name]
    candidates=[name,name+'_top',name.replace('_stairs','').replace('_slab',''),
                name.replace('_fence','_planks'),name.replace('_wall',''),name.replace('potted_','')]
    for candidate in candidates:
        try:
            with Image.open(io.BytesIO(TEXTURES.read('assets/minecraft/textures/block/'+candidate+'.png'))) as im:
                pixels=np.asarray(im.convert('RGBA').crop((0,0,im.width,im.width)))
                visible=pixels[:,:,3]>90
                if visible.any():return tuple(np.mean(pixels[:,:,:3][visible],axis=0).astype(int))
        except KeyError:pass
    return COLORS.get(name,(125,128,119))


def render(data,path,turns=0):
    data=np.rot90(data,turns,axes=(1,2))
    # Merge water interiors like solid voxels; this is an offline matte render, not a shader capture.
    ysize,zsize,xsize=data.shape
    valid=data!=0;faces=[]
    for axis in (0,1,2):
        exposed=valid.copy()
        a=[slice(None)]*3;b=a.copy();a[axis]=slice(0,-1);b[axis]=slice(1,None)
        exposed[tuple(a)] &= ~valid[tuple(b)]
        yy,zz,xx=np.nonzero(exposed)
        face_ids=data[yy,zz,xx]
        faces.append(np.column_stack((xx,yy,zz,np.full(xx.shape,axis),face_ids)))
    faces=np.concatenate(faces);assert len(faces), 'Export bounds do not intersect saved terrain'
    faces=faces[np.argsort(faces[:,:3].sum(axis=1),kind='stable')]
    x,y,z=faces[:,0],faces[:,1],faces[:,2]
    px=(x-z)*math.sqrt(3)/2;py=(x+z)*.5-y
    lo=np.array([px.min()-1,py.min()-1]);hi=np.array([px.max()+1,py.max()+2])
    margin=50;scale=min((RES[0]-margin*2)/(hi[0]-lo[0]),(RES[1]-margin*2)/(hi[1]-lo[1]))
    offset=(np.array(RES)-(hi-lo)*scale)/2-lo*scale
    image=Image.new('RGBA',RES,(0,0,0,0));draw=ImageDraw.Draw(image)
    # Camera sees +X,+Z and +Y faces; orthographic oblique projection is constant at every depth.
    corners=[[(0,1,0),(1,1,0),(1,1,1),(0,1,1)],
             [(0,0,1),(0,1,1),(1,1,1),(1,0,1)],
             [(1,0,0),(1,0,1),(1,1,1),(1,1,0)]]
    projected=[[( (a-c)*math.sqrt(3)/2*scale,(a+c)*.5*scale-b*scale) for a,b,c in v] for v in corners]
    shades=[1.11,.70,.86]
    colors={}
    for pid in np.unique(faces[:,4]):
        name=NAMES[pid];rgb=color(name)
        for axis,shade in enumerate(shades):
            colors[int(pid),axis]=tuple(min(255,int(c*shade)) for c in rgb)+(255,)
    for x,y,z,axis,pid in faces:
        u=(x-z)*.866025403784*scale+offset[0];v=((x+z)*.5-y)*scale+offset[1]
        draw.polygon([(u+a,v+b) for a,b in projected[axis]],fill=colors[int(pid),int(axis)])
    image.save(path,optimize=True)
    return dict(exposed_faces=len(faces),pixels=list(RES),blocks_per_pixel=round(1/scale,4),camera_quarter_turns=turns)


def frames():
    gates=MANIFEST['gates'];result=[]
    for i,a in enumerate(gates):
        end=gates[i+1]['s'] if i+1<len(gates) else mountain.LENGTH
        title=a['title']+'—'+(gates[i+1]['title'] if i+1<len(gates) else '终点港池')
        result.append(dict(title=title,start=a['s'],end=end,type='stage'))
    for i,s in enumerate(MANIFEST['structures']):
        result.append(dict(title=('岩壁隧道' if s['kind']=='tunnel' else '涵洞')+f'_{i+1}_剖视',
                           start=s['start']-10,end=s['end']+10,type='cutaway'))
    result.append(dict(title='雪冠火山口湖',start=mountain.LENGTH-28,end=mountain.LENGTH,type='crater'))
    return result


def main():
    global TEXTURES
    parser=argparse.ArgumentParser();parser.add_argument('--only',type=int);args=parser.parse_args()
    jar=next((ROOT/'.gradle/loom-cache/minecraftMaven').rglob('minecraft-merged-*.jar'))
    # Windows' ordinary API path limit is exceeded by Loom's mapped artifact names.
    TEXTURES=zipfile.ZipFile('\\\\?\\'+str(jar.resolve()))
    OUT.mkdir(exist_ok=True);index=[]
    for number,frame in enumerate(frames(),1):
        if args.only and number!=args.only:continue
        s0,s1=frame['start'],frame['end'];padding=36
        stations=np.linspace(s0,s1,max(2,int((s1-s0)/3)+1))
        points=np.array([mountain.course.at(s)[0] for s in stations])
        ymin,ymax=48,172
        if s1>=mountain.MOUNTAIN_START:ymax=218
        if frame['type']=='cutaway':padding=28;ymin=int(mountain.water_y(s0))-6;ymax=int(mountain.water_y(s1))+3
        if frame['type']=='crater':
            points=np.array([mountain.SPIRAL_CENTER]);padding=78;ymin=154;ymax=218
        if number==1:points=np.vstack((points,[-36,-38],[36,-38]))
        if number==len(MANIFEST['gates']):
            p,t=mountain.course.at(mountain.LENGTH);points=np.vstack((points,p+t*84))
        lo=np.floor(points.min(axis=0)-padding).astype(int);hi=np.ceil(points.max(axis=0)+padding).astype(int)
        bounds=int(lo[0]),int(lo[1]),int(hi[0]),int(hi[1])
        turns=0
        if s1>=mountain.MOUNTAIN_START:
            d=points.mean(axis=0)-mountain.SPIRAL_CENTER
            # np.rot90 rotates the (Z,X) plane: k=1 is (x',z')=(z,-x).
            # Face the outside of the mountain, so the cut boundary cannot hide the raceway.
            scores=[d[0]+d[1],-d[0]+d[1],-d[0]-d[1],d[0]-d[1]]
            turns=int(np.argmax(scores))
        filename=f'{number:02d}_'+re.sub(r'[<>:"/\\|?*]','_',frame['title'])+'.png'
        data=volume(bounds,ymin,ymax)
        meta=render(data,OUT/filename,turns)
        index.append(dict(number=number,file=filename,**frame,bounds_xz=list(bounds),y_range=[ymin,ymax],**meta))
        print(f'Rendered {number:02d} / {len(frames())}: {meta["exposed_faces"]} faces',flush=True)
        del data
    if args.only:return
    report=dict(source=str(SAVE.relative_to(ROOT)),projection='orthographic isometric',transparent_background=True,
                renderer='Actual saved voxel faces, vanilla texture average colors; no Minecraft/shader execution.',
                images=index)
    (OUT/'index.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    (OUT/'README.md').write_text('# 关卡正交图 · V12\n\n'
        '每个检查点至下个检查点各一张，共 37 张赛段图；另有 4 张隧道／涵洞剖视和 1 张火山口湖图。'
        '每张 2304×1536 PNG，正交投影，无文字或路线覆盖，透明背景；可直接复用。\n\n'
        '由实际 Anvil 存档方块离线渲染，采用原版贴图平均色和固定面光照。不是游戏截图；'
        '栅栏等采用体素近似，玻璃、水体为哑光表现，不含游戏内 Shader、粒子和动画。'
        '剖视图仅在导出时裁掉高处遮挡，不改变存档。index.json 记录各图里程、坐标范围与摄像机方向。\n',encoding='utf-8')
    # A separate labeled contact sheet keeps every individual PNG clean for reuse.
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',16)
    cols=4;tw,th=576,410;sheet=Image.new('RGB',(cols*tw,math.ceil(len(index)/cols)*th),'#172633');draw=ImageDraw.Draw(sheet)
    for i,row in enumerate(index):
        x=i%cols*tw;y=i//cols*th
        with Image.open(OUT/row['file']) as image:
            thumb=image.resize((tw,384),Image.Resampling.LANCZOS);sheet.paste(thumb,(x,y),thumb)
        text=f'{row["number"]:02d}  '+row['title']
        while draw.textlength(text,font=font)>tw-20:text=text[:-2]+'…'
        draw.text((x+10,y+385),text,font=font,fill='#d6e3ec')
    sheet.save(OUT/'00_关卡总览.jpg',quality=91)
    print('Exported',len(index),'orthographic images to',OUT,flush=True)


if __name__=='__main__':main()
