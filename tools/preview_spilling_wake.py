"""Orthographic CPU material/shape study, NOT a Minecraft screenshot.

Shows shipped texture alpha on a model of the wake mesh; no scene shader, depth-field
displacement, water lighting or particle system is emulated. Output stays under build/.
"""
from pathlib import Path
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from check_spilling_wake import pressure, amplitude, breaking, crest, profile, modulation, smooth
from make_wake_ribbon_texture import cellular

ROOT=Path(__file__).resolve().parents[1]
W,H=820,580
view=np.array([.7,.65,-1.]);view/=np.linalg.norm(view)
right=np.cross([0,1,0],view);right/=np.linalg.norm(right)
up=np.cross(view,right)
water=np.array([63,118,228])/255
SCALE=58


def old_froth(u,v):
    a=u*math.tau;c=.5+.12*math.sin(a*3)+.04*math.sin(a*7)
    band=math.exp(-((v-c)/.34)**2);b,g,cl=cellular(u,v)
    pa=max(0,min(1,(cl-.08)*3.5));ae=max(0,min(1,(g-.35)*3))
    ed=smooth(min(1,v*10,(1-v)*10))
    return [245+g*10,250+g*5,255,255*band*pa*min(1,.53+.47*b+.42*ae)*ed]


def old_ribbon(u,v):
    a=u*math.tau;st=(.5+.5*math.sin(a*7+v*7+math.sin(a*3)))**8
    ri=math.exp(-((v-.73-.03*math.sin(a*3))/.12)**2)
    mi=(.5+.5*math.sin(a*19+v*23+math.sin(a*5)))**12
    return [210+st*28,234+st*16,251,(62+28*st+48*ri+30*mi)*min(1,v*12,(1-v)*9)]


def texture(func):
    return np.array([[func((x+.5)/128,(y+.5)/128) for x in range(128)] for y in range(128)])/255


NEW={name:np.asarray(Image.open(ROOT/f'src/main/resources/assets/longboatlab/textures/water/{name}.png'))/255 for name in ['ribbon','froth']}
OLD={'ribbon':texture(old_ribbon),'froth':texture(old_froth)}


def project(points):
    points=np.asarray(points)
    return np.column_stack((W/2+points@right*SCALE,H*.52-points@up*SCALE,points@view))


def render(old=False,turn=0):
    image=np.empty((H,W,3),float);image[:]=np.array([46,94,139])/255
    depth=np.full((H,W),-np.inf)

    def quad(points,uv,tex,tint,opacity,solid=False):
        positions=project(points);uv=np.asarray(uv,float)
        for ids in ((0,1,2),(0,2,3)):
            p=positions[list(ids)];coords=uv[list(ids)]
            x0=max(0,int(p[:,0].min()));x1=min(W,int(p[:,0].max())+1)
            y0=max(0,int(p[:,1].min()));y1=min(H,int(p[:,1].max())+1)
            if x1<=x0 or y1<=y0:continue
            x,y=np.meshgrid(np.arange(x0,x1)+.5,np.arange(y0,y1)+.5)
            den=(p[1,1]-p[2,1])*(p[0,0]-p[2,0])+(p[2,0]-p[1,0])*(p[0,1]-p[2,1])
            if abs(den)<1e-8:continue
            a=((p[1,1]-p[2,1])*(x-p[2,0])+(p[2,0]-p[1,0])*(y-p[2,1]))/den
            b=((p[2,1]-p[0,1])*(x-p[2,0])+(p[0,0]-p[2,0])*(y-p[2,1]))/den;c=1-a-b
            z=a*p[0,2]+b*p[1,2]+c*p[2,2]
            valid=(a>=0)&(b>=0)&(c>=0)&(z>=depth[y0:y1,x0:x1]-1e-5)
            if not valid.any():continue
            u=a*coords[0,0]+b*coords[1,0]+c*coords[2,0]
            v=a*coords[0,1]+b*coords[1,1]+c*coords[2,1]
            th,tw=tex.shape[:2];sample=tex[(np.floor(v*th).astype(int))%th,(np.floor(u*tw).astype(int))%tw]
            alpha=sample[:,:,3]*opacity
            patch=image[y0:y1,x0:x1]
            blended=sample[:,:,:3]*tint*alpha[:,:,None]+patch*(1-alpha[:,:,None])
            patch[valid]=blended[valid]
            if solid:depth[y0:y1,x0:x1][valid]=z[valid]

    white=np.ones((1,1,4));uv=[(0,0),(1,0),(1,1),(0,1)]
    # Plain hull proxy makes relative wave height easy to compare. It is not a game model.
    for side in (-1,1):
        quad([(side*.69,0,-4),(side*.69,0,4),(side*.69,.48,4),(side*.69,.48,-4)],uv,white,[.52,.34,.17],1,True)
        quad([(side*.69,.48,-4),(side*.69,.48,4),(side*.59,.48,4),(side*.59,.48,-4)],uv,white,[.69,.47,.24],1,True)
    for end in (-4,4):
        quad([(-.69,0,end),(.69,0,end),(.69,.48,end),(-.69,.48,end)],uv,white,[.63,.42,.21],1,True)
    quad([(-.69,.04,-4),(.69,.04,-4),(.69,.04,4),(-.69,.04,4)],uv,white,[.32,.23,.14],1,True)
    for z in (-2,0,2):
        quad([(-.66,.35,z-.08),(.66,.35,z-.08),(.66,.35,z+.08),(-.66,.35,z+.08)],uv,white,[.65,.44,.23],1,True)
    tex=OLD if old else NEW
    passes=[[],[]]
    for side in (-1,1):
        stations=sorted(set(np.linspace(-3.7,3.7,3).tolist()+[3.7-.30,3.7-.75,3.7-1.35]))
        nodes=[]
        for z in stations:
            d=3.7-z;outward=-turn*z*side
            speed=math.hypot(.70,max(0,outward));power=min(1.7,.1+max(0,speed-.09)*1.12)
            entry=float(smooth(d/2.5));width=((.42+power*.28) if old else (.60+power*.40))*entry
            h=(.10+power*.56)*entry if old else amplitude(power,entry,pressure(d,outward),width)
            act=1 if old else breaking(h,width,power)*entry
            nodes.append(dict(z=z,width=width,h=h,alpha=175*min(1,.25+power*.8)*entry/255,act=act,p=z*.35+1.1))
        def point(n,v):
            if old:
                prof=smooth(v/.68) if v<.68 else max(0,1-(v-.68)/.32)**1.65
                h=n['h']*prof
                curl=math.sin(math.pi*v)*smooth((v-.45)/.28)*min(.14,n['h']*.26)*.8
            else:
                samples=np.linspace(0,1,13)
                heights=profile(samples,crest(n['p']))*min(n['width']*.34,n['h']*modulation(n['p']))
                h=np.interp(v,samples,heights);curl=0
            return (side*(.71+v*n['width']+curl),.025+.015*v+h,n['z'])
        def edge(n,band,high):
            if old:
                center=.675 if band==0 else .87;size=.18 if band==0 else .105
            else:
                center=crest(n['p']) if band==0 else .865;size=.070+.045*n['act'] if band==0 else .115
            return max(.01,min(.99,center+(size if high else -size)))
        for a,b in zip(nodes,nodes[1:]):
            for row in range(12):
                lo,hi=row/12,(row+1)/12;points=[point(a,lo),point(b,lo),point(b,hi),point(a,hi)]
                offset=row*.31 if old else 0
                passes[0].append((points,[(a['p']+offset,lo),(b['p']+offset,lo),(b['p']+offset,hi),(a['p']+offset,hi)],tex['ribbon'],water*(.8 if old else .955)+( .20 if old else .045),(a['alpha']+b['alpha'])/2*(.7 if old else .85)))
            for band in range(2):
                for part in range(4):
                    t0,t1=part/4,(part+1)/4
                    al,ah=edge(a,band,False),edge(a,band,True);bl,bh=edge(b,band,False),edge(b,band,True)
                    points=[point(a,al*(1-t0)+ah*t0),point(b,bl*(1-t0)+bh*t0),point(b,bl*(1-t1)+bh*t1),point(a,al*(1-t1)+ah*t1)]
                    points=[(x,y+.004,z)for x,y,z in points]
                    offset=band*(.31 if old else .41)
                    alpha=(a['alpha']*a['act']+b['alpha']*b['act'])/2*((1.65 if band==0 else 1.1) if old else (1.8 if band==0 else 1.1))
                    passes[1].append((points,[(a['p']+offset,1-t0),(b['p']+offset,1-t0),(b['p']+offset,1-t1),(a['p']+offset,1-t1)],tex['froth'],water*.06+.94,min(1,alpha)))
    for items in passes:
        for args in sorted(items,key=lambda item:np.mean(np.asarray(item[0])@view)):
            quad(*args)
    return Image.fromarray(np.uint8(np.clip(image,0,1)*255))


if __name__=='__main__':
    out=ROOT/'build/visual-review-0.10.19';out.mkdir(parents=True,exist_ok=True)
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',22)
    board=Image.new('RGB',(W*3,H+68),'#243a4b')
    for i,(old,turn,title) in enumerate([(True,0,'0.10.18 · 原白沫覆盖'),(False,0,'0.10.19 · 直行压力波'),(False,.10,'0.10.19 · 转弯局部推水')]):
        frame=render(old,turn);board.paste(frame,(i*W,50));d=ImageDraw.Draw(board);d.text((i*W+25,12),title,font=font,fill='#d7e3eb')
    ImageDraw.Draw(board).text((20,H+21),'离线形态与透明材质预览 · 非游戏截图 · 不含水面 Shader、光照和雾滴',font=font,fill='#bacbd6')
    board.save(out/'wake-comparison.png')
    print(out/'wake-comparison.png')
