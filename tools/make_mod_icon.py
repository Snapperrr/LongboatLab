"""Orthographically render the shipped Blockbench hull and oars for the mod icon.

Uses Pillow and NumPy, plus the local Minecraft client jar for pufferfish skin.
No game launch, shader pack, Blender, network call, or image-generation API.
"""
from __future__ import annotations

import io
import json
import math
import zipfile
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/longboatlab'
OUT = ROOT / 'branding'
WORK = ROOT / 'build/icon-work'
RES = 1536
FACES = []
RNG = np.random.default_rng(311)


def unit(v):
    v = np.asarray(v, dtype=float)
    return v / np.linalg.norm(v)


def basis(direction):
    view = unit(direction)
    right = unit(np.cross([0, 1, 0], view))
    up = np.cross(view, right)
    return np.array([right, up, view])


def face(vertices, texture=None, uv=None, color=(1, 1, 1), material='wood'):
    p = np.asarray(vertices, dtype=float)
    normal = unit(np.cross(p[1]-p[0], p[2]-p[0]))
    FACES.append(dict(p=p, n=normal, tex=texture,
                      uv=np.array(uv if uv is not None else [(0, 0)]*len(p)),
                      color=np.array(color), material=material))


def box(lo, hi, texture=None, uv_faces=None, color=(1, 1, 1), matrix=None,
        offset=(0, 0, 0), material='wood'):
    x0, y0, z0 = lo
    x1, y1, z1 = hi
    vertices = {
        'north': [(x0,y1,z0),(x1,y1,z0),(x1,y0,z0),(x0,y0,z0)],
        'south': [(x1,y1,z1),(x0,y1,z1),(x0,y0,z1),(x1,y0,z1)],
        'west': [(x0,y1,z1),(x0,y1,z0),(x0,y0,z0),(x0,y0,z1)],
        'east': [(x1,y1,z0),(x1,y1,z1),(x1,y0,z1),(x1,y0,z0)],
        'up': [(x0,y1,z1),(x1,y1,z1),(x1,y1,z0),(x0,y1,z0)],
        'down': [(x0,y0,z0),(x1,y0,z0),(x1,y0,z1),(x0,y0,z1)],
    }
    for name, p in vertices.items():
        p = np.array(p, dtype=float)
        if matrix is not None:
            p = p @ matrix.T
        p += offset
        if uv_faces:
            u0, v0, u1, v1 = uv_faces[name]['uv']
            uv = [(u0,v0),(u1,v0),(u1,v1),(u0,v1)]
        else:
            uv = [(0,0),(8,0),(8,8),(0,8)]
        face(p, texture, uv, color, material)


def load_model(name, texture, matrix=None, offset=(0, 0, 0)):
    model = json.loads((ASSETS/f'models/entity/{name}.bbmodel').read_text(encoding='utf-8'))
    for element in model['elements']:
        box(element['from'], element['to'], texture, element['faces'],
            matrix=matrix, offset=offset)


def spike(center, normal, span=1.1, height=2.1):
    center = np.array(center, dtype=float)
    normal = np.array(normal, dtype=float)
    u = unit(np.cross(normal, [0,1,0] if abs(normal[1])<.8 else [1,0,0]))*span/2
    v = np.cross(normal,u)
    base = [center-u-v, center+u-v, center+u+v, center-u+v]
    tip = center+normal*height
    for i in range(4):
        face([base[i],base[(i+1)%4],tip],color=(.95,.62,.16),material='fish')


def puffer(center, texture, size=10.5):
    # The inflated vanilla puffer has an 8-pixel box UV layout.
    uv = {
        'north': {'uv':[8,8,16,16]}, 'south':{'uv':[24,8,32,16]},
        'west': {'uv':[16,8,24,16]}, 'east':{'uv':[0,8,8,16]},
        'up': {'uv':[8,0,16,8]}, 'down':{'uv':[16,0,24,8]},
    }
    c=np.array(center)
    r=size/2
    box([-r,-r,-r],[r,r,r],texture,uv,offset=center,material='fish')
    # The vanilla fish's eyes are separate model parts rather than details in
    # its body UV.  Add those as cuboids so the expression survives downscaling.
    for s in (-1,1):
        e=c+[s*r*.62,r*.05,-r-.25]
        box(e+[-1.16,-1.05,-.22],e+[1.16,1.3,.22],
            color=(1,.98,.85),material='fish')
        box(e+[s*.12-.46,-.50,-.30],e+[s*.12+.46,.56,-.23],
            color=(.04,.10,.11),material='fish')
    mouth=c+[0,-r*.44,-r-.12]
    box(mouth+[-.67,-.45,-.15],mouth+[.67,.36,.05],
        color=(.26,.16,.08),material='fish')
    for normal in ([0,1,0],[-1,0,0],[1,0,0],[0,0,-1]):
        n=np.array(normal)
        u=unit(np.cross(n,[0,1,0] if abs(n[1])<.8 else [1,0,0]))
        v=np.cross(n,u)
        for a,b in [(-.66,-.65),(.66,-.65),(-.66,.64),(.66,.64)]:
            spike(c+n*r+u*r*a+v*r*b,n,span=1.4,height=2.3)
    # Small blue fins match the vanilla fish's contrasting flippers.
    for s in (-1,1):
        box(c+[s*(r+1)-1.2,-1,-.5],c+[s*(r+1)+1.2,1.0,1.8],
            color=(.31,.72,.79),material='fish')


def ribbon(side, start, end, outer, color, ripple=0.0):
    for i in range(58):
        t0,t1=i/58,(i+1)/58
        def point(t,u):
            z=start+(end-start)*t
            width=math.sin(t*math.pi)**.65
            x=side*(11.5+2*math.sin(t*math.pi)+max(0,-z-15)*.18+
                    u*outer*width+math.sin(t*18)*.45)
            crest=math.sin(u*math.pi)**1.6
            y=-2+crest*(.9+ripple)*width+.16*math.sin(t*32)
            return (x,y,z)
        for j in range(5):
            u0,u1=j/5,(j+1)/5
            p=[point(t0,u0),point(t0,u1),point(t1,u1),point(t1,u0)]
            cross=np.cross(np.array(p[1])-p[0],np.array(p[2])-p[0])
            if np.linalg.norm(cross)<1e-8:
                continue
            if cross[1]<0:
                p=p[::-1]
            tint=np.array(color)*(.73+.28*math.sin((u0+u1)*math.pi/2))
            face(p,color=tint,material='water')


def scene():
    wood=np.asarray(Image.open(ASSETS/'textures/entity/oak.png').convert('RGBA'))/255
    jar=Path.home()/'.gradle/caches/fabric-loom/1.21.1/minecraft-client.jar'
    with zipfile.ZipFile(jar) as archive:
        fish=np.asarray(Image.open(io.BytesIO(archive.read(
            'assets/minecraft/textures/entity/fish/pufferfish.png'))).convert('RGBA'))/255
    load_model('continuous_hull',wood)
    # Two matching oversized pairs, seated directly on the actual hull rails.
    for z in (-13,13):
        for side in (-1,1):
            axis=unit([side,-.24,-.32])
            across=unit(np.cross(axis,[0,1,0]))
            up=np.cross(across,axis)
            transform=np.array([axis,up,across]).T*1.12
            load_model('oar',wood,matrix=transform,offset=(side*10,8.5,z))
    puffer((-6.3,7.2,-37.8),fish,11)
    puffer((6.3,7.2,-37.8),fish,10)
    # A sculpted wake with separated bright foam flecks; no lettering or border.
    for side in (-1,1):
        ribbon(side,36,-55,7.0,(.055,.69,.78),1.6)
        for i in range(30):
            z=30-i*2.7+RNG.uniform(-.6,.6)
            t=(36-z)/91
            x=side*(11.5+2*math.sin(t*math.pi)+max(0,-z-15)*.18+
                    3.5*math.sin(t*math.pi)**.65+math.sin(t*18)*.45)
            y=-2+2.5*math.sin(t*math.pi)**.65
            width=RNG.uniform(.25,.8)*math.sin(t*math.pi)**.4
            box([x-width,y,z-.35],[x+width,y+.24,z+RNG.uniform(.2,1.1)],
                color=(.74,.98,.94),material='foam')
        for _ in range(13):
            z=RNG.uniform(-42,25)
            x=side*RNG.uniform(16,23)
            y=RNG.uniform(.4,6.0)
            r=RNG.uniform(.19,.43)
            box([x-r,y-r,z-r],[x+r,y+r,z+r],color=(.7,.98,1),material='foam')


class Camera:
    def __init__(self, direction, resolution, padding=.09):
        self.b=basis(direction)
        all_points=np.concatenate([f['p'] for f in FACES])@self.b.T
        lo=all_points.min(axis=0)
        hi=all_points.max(axis=0)
        self.center=(lo+hi)/2
        self.scale=resolution*(1-padding*2)/max((hi-lo)[:2])
        self.res=resolution

    def project(self,p):
        q=np.asarray(p)@self.b.T-self.center
        return np.stack((self.res/2+q[...,0]*self.scale,
                         self.res/2-q[...,1]*self.scale,q[...,2]),axis=-1)


def raster(camera, full=False):
    res=camera.res
    depth=np.full((res,res),-np.inf)
    if full:
        rgb=np.zeros((res,res,3),np.float32)
        positions=np.zeros((res,res,3),np.float32)
        normals=np.zeros((res,res,3),np.float32)
        kinds=np.zeros((res,res),np.uint8)
    for f in FACES:
        if np.dot(f['n'],camera.b[2])<1e-7:
            continue
        screen=camera.project(f['p'])
        triangles=[(0,i,i+1) for i in range(1,len(screen)-1)]
        for ids in triangles:
            p=screen[list(ids)]
            x0=max(0,int(p[:,0].min())); x1=min(res,int(p[:,0].max())+1)
            y0=max(0,int(p[:,1].min())); y1=min(res,int(p[:,1].max())+1)
            if x0>=x1 or y0>=y1:
                continue
            y,x=np.mgrid[y0:y1,x0:x1]; x=x+.5; y=y+.5
            den=(p[1,1]-p[2,1])*(p[0,0]-p[2,0])+(p[2,0]-p[1,0])*(p[0,1]-p[2,1])
            if abs(den)<1e-8:
                continue
            a=((p[1,1]-p[2,1])*(x-p[2,0])+(p[2,0]-p[1,0])*(y-p[2,1]))/den
            b=((p[2,1]-p[0,1])*(x-p[2,0])+(p[0,0]-p[2,0])*(y-p[2,1]))/den
            c=1-a-b
            z=a*p[0,2]+b*p[1,2]+c*p[2,2]
            valid=(a>=-1e-7)&(b>=-1e-7)&(c>=-1e-7)&(z>depth[y0:y1,x0:x1])
            if not valid.any():
                continue
            if f['tex'] is not None:
                uv=f['uv'][list(ids)]
                u=a*uv[0,0]+b*uv[1,0]+c*uv[2,0]
                v=a*uv[0,1]+b*uv[1,1]+c*uv[2,1]
                th,tw=f['tex'].shape[:2]
                sample=f['tex'][np.clip(v.astype(int),0,th-1),np.clip(u.astype(int),0,tw-1)]
                valid&=sample[:,:,3]>.5
            depth[y0:y1,x0:x1][valid]=z[valid]
            if full:
                wp=f['p'][list(ids)]
                xyz=a[:,:,None]*wp[0]+b[:,:,None]*wp[1]+c[:,:,None]*wp[2]
                positions[y0:y1,x0:x1][valid]=xyz[valid]
                normals[y0:y1,x0:x1][valid]=f['n']
                if f['tex'] is not None:
                    albedo=sample[:,:,:3]*f['color']
                else:
                    albedo=np.broadcast_to(f['color'],(*valid.shape,3))
                rgb[y0:y1,x0:x1][valid]=albedo[valid]
                kinds[y0:y1,x0:x1][valid]={'wood':1,'fish':2,'water':3,'foam':4}[f['material']]
    return (depth,rgb,positions,normals,kinds) if full else depth


def background():
    y,x=np.mgrid[:RES,:RES]/RES
    glow=np.exp(-(((x-.51)/.56)**2+((y-.40)/.53)**2)*2.2)
    out=np.zeros((RES,RES,3),dtype=float)
    for i,(base,light) in enumerate(zip([5,20,33],[12,76,96])):
        out[:,:,i]=(base+glow*light)/255
    return Image.fromarray(np.uint8(np.clip(out,0,1)*255)).convert('RGBA')


def render():
    scene()
    camera=Camera([-1.0,1.15,-1.38],RES,padding=.075)
    light=Camera([-1.4,3.1,-1.0],1024,padding=.04)
    shadow=raster(light)
    depth,albedo,xyz,normals,material=raster(camera,full=True)
    mask=np.isfinite(depth)
    lp=light.project(xyz[mask]+normals[mask]*.14)
    sx=np.floor(lp[:,0]).astype(int); sy=np.floor(lp[:,1]).astype(int)
    light_amount=np.zeros(mask.sum())
    for dx,dy in [(0,0),(-1,-1),(-1,1),(1,-1),(1,1),(2,0),(-2,0),(0,2),(0,-2)]:
        ix=np.clip(sx+dx,0,light.res-1); iy=np.clip(sy+dy,0,light.res-1)
        light_amount+=(lp[:,2]>=shadow[iy,ix]-.12)/9
    diffuse=np.maximum(0,normals[mask]@light.b[2])
    ambient=np.maximum(0,normals[mask,1])*.12+.67
    shade=ambient+diffuse*.31*(.32+light_amount*.68)
    colors=albedo[mask]*shade[:,None]
    # Cool indirect fill and warm key lighting give volume without erasing pixels.
    colors*=np.array([1.09,1.01,.91])
    colors+=np.array([.00,.008,.012])*(1-diffuse[:,None])
    result=np.zeros((RES,RES,4),np.uint8)
    result[mask,:3]=np.uint8(np.clip(colors,0,1)*255)
    result[mask,3]=255
    hero=Image.fromarray(result)
    alpha=hero.getchannel('A')
    back=background()
    # A restrained outline/shadow keeps the silhouette clear on a dark page.
    boat_mask=Image.fromarray(np.uint8((material>0)&(material<=2))*255)
    stroke=boat_mask.filter(ImageFilter.MaxFilter(5)).filter(ImageFilter.GaussianBlur(2))
    outline=Image.new('RGBA',(RES,RES),(1,10,17,255))
    outline.putalpha(stroke)
    back=Image.alpha_composite(back,outline)
    # Exhaust is behind the fish, tapering out from their outward-facing mouths.
    gas=Image.new('RGBA',(RES,RES),(0,0,0,0))
    gd=ImageDraw.Draw(gas)
    for xx in (-6.3,6.3):
        for i in reversed(range(18)):
            t=i/17
            position=(xx+math.sin(i*.9)*.45,6.7,-44-t*15)
            px,py,_=camera.project(position)
            radius=(.25+t*1.55)*camera.scale
            alpha_g=int(20+55*(1-t))
            gd.ellipse((px-radius,py-radius*.55,px+radius,py+radius*.55),fill=(162,246,233,alpha_g))
    gas=gas.filter(ImageFilter.GaussianBlur(3.0))
    back=Image.alpha_composite(back,gas)
    icon=Image.alpha_composite(back,hero).convert('RGB')
    return icon,hero


def main():
    OUT.mkdir(parents=True,exist_ok=True)
    WORK.mkdir(parents=True,exist_ok=True)
    icon,cutout=render()
    for size in (1024,512,256,128,64):
        image=icon.resize((size,size),Image.Resampling.LANCZOS)
        image.save(OUT/f'longboatlab-icon-{size}.png',optimize=True)
    cutout.resize((1024,1024),Image.Resampling.LANCZOS).save(
        OUT/'longboatlab-mark-transparent.png',optimize=True)
    icon.resize((1024,1024),Image.Resampling.LANCZOS).save(ROOT/'longboatlab-icon.png',optimize=True)
    icon.resize((256,256),Image.Resampling.LANCZOS).save(ASSETS/'icon.png',optimize=True)
    # Enlarge thumbnails with nearest sampling only in this private QA sheet.
    sheet=Image.new('RGB',(960,520),'#101d2b')
    sheet.paste(icon.resize((480,480),Image.Resampling.LANCZOS),(20,20))
    for size,y in [(128,40),(64,250)]:
        small=Image.open(OUT/f'longboatlab-icon-{size}.png')
        sheet.paste(small,(550,y))
    sheet.save(WORK/'preview.png')
    print(f'Icon family exported to {OUT}')


if __name__=='__main__':
    main()
