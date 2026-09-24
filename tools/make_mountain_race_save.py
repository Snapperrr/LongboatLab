"""V12 course: one-block-deep waterfront stations with reachable boat crafting.

Reuses V2's race rules and Anvil writer. Only new/pristine output worlds may be generated.
"""
from pathlib import Path
import argparse
import gzip
import json
import math
import zipfile
import sys
import numpy as np
import mountain_scenery
import sculpted_wonders
import rally_pit_stops
import rally_ground_details
import rally_free_practice
import make_shared_race_save as course
from race_nbt import *

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'maps/Puffer_Rally_Mountain_V12'
FLAT_SCALE=.52
MOUNTAIN_RADIUS=150
ELEVATION_STEP=30
FLAT_WAYPOINTS=[(0,0),(0,600),(440,600),(440,80),(880,80),(880,920),
    (320,920),(320,1420),(960,1420),(960,1800),(1210,1800),(1210,1240),
    (1490,1240),(1490,1760),(1790,1760),(1790,1500),(2180,1500)]
FLAT_WAYPOINTS=[(x*FLAT_SCALE,z*FLAT_SCALE) for x,z in FLAT_WAYPOINTS]
ARROWS=[]
STRUCTURES=[]
SHORTCUTS=[]
SUPPORTS=[]
LIGHTS=[]
FINISH_CELLS=[]
DECOR={}
LAKE_Y=179
SPIRAL_TURNS=1.10
SPIRAL_CENTER=np.array(FLAT_WAYPOINTS[-1])+[0,MOUNTAIN_RADIUS]
ORIGINAL_WIDTH=course.half_width
ORIGINAL_GATE=course.gate
ORIGINAL_SCENERY=course.scenery
ORIGINAL_TERRAIN=course.terrain
ORIGINAL_INDEX=course.prepare_index


def path():
    points=[np.array(FLAT_WAYPOINTS[0],float)]
    def line(end):
        start=points[-1]
        points.extend(start+(end-start)*t for t in np.linspace(0,1,max(2,math.ceil(np.linalg.norm(end-start))+1))[1:])
    for i in range(1,len(FLAT_WAYPOINTS)-1):
        prev,p,nxt=(np.array(v,float) for v in FLAT_WAYPOINTS[i-1:i+2])
        a=(p-prev)/np.linalg.norm(p-prev);b=(nxt-p)/np.linalg.norm(nxt-p)
        radius=(90 if i<=7 else 65)*FLAT_SCALE
        entry=p-a*radius;end=p+b*radius;center=entry+b*radius
        line(entry)
        angle=math.atan2((entry-center)[1],(entry-center)[0])
        turn=np.sign(a[0]*b[1]-a[1]*b[0])
        for theta in np.linspace(0,math.pi/2,math.ceil(radius*math.pi/2)+1)[1:]:
            points.append(center+radius*np.array([math.cos(angle+turn*theta),math.sin(angle+turn*theta)]))
        assert np.linalg.norm(points[-1]-end)<1e-6
    line(np.array(FLAT_WAYPOINTS[-1],float))
    start=len(points)-1
    for t in np.linspace(0,math.tau*SPIRAL_TURNS,2401)[1:]:
        radius=MOUNTAIN_RADIUS+np.sin(t)**2*(8*np.sin(2.3*t)+5*np.sin(4.1*t))
        center=SPIRAL_CENTER+np.array([18*(1-np.cos(t*.5)),8*np.sin(t*.7)**2])
        points.append(center+radius*np.array([np.sin(t),-np.cos(t)]))
    p=np.array(points)
    distances=np.r_[0,np.cumsum(np.linalg.norm(np.diff(p,axis=0),axis=1))]
    return p,distances,float(distances[start])


POINTS,DISTANCES,MOUNTAIN_START=path()
LENGTH=float(DISTANCES[-1])


def water_y(s):
    return 63+np.floor(np.maximum(0,np.minimum(np.asarray(s),LENGTH-50)-MOUNTAIN_START)/ELEVATION_STEP).astype(int)


def half_width(s):
    a=np.asarray(s)
    station=a/FLAT_SCALE
    late=13+7*(.5+.5*np.sin((station-3600)/117))
    blend=np.clip((station-3550)/180,0,1)
    flat=ORIGINAL_WIDTH(station)*(1-blend)+late*blend
    return np.where(a>=MOUNTAIN_START,12+2*np.sin((a-MOUNTAIN_START)/165)**2,flat)


def nearest_s(x,z):
    return float(DISTANCES[np.argmin(np.sum((POINTS[:np.searchsorted(DISTANCES,MOUNTAIN_START)]-[x,z])**2,axis=1))])


def shortcuts():
    for start,end,title in [((960,1590),(1210,1590),'峡湾近道'),((1490,1570),(1790,1570),'松林近道')]:
        start=np.array(start)*FLAT_SCALE;end=np.array(end)*FLAT_SCALE
        lo,hi=nearest_s(*start),nearest_s(*end)
        p,t0=course.at(lo);q,t1=course.at(hi)
        u=np.linspace(0,1,math.ceil(np.linalg.norm(q-p)*1.2)+1)[:,None]
        pts=(1-u)**3*p+3*(1-u)**2*u*(p+t0*45)+3*(1-u)*u*u*(q-t1*45)+u**3*q
        SHORTCUTS.append({'title':title,'entry_s':lo,'exit_s':hi,'points':pts,'half_width':5,
                          'length':float(np.linalg.norm(np.diff(pts,axis=0),axis=1).sum())})


def frame_point(p,t,along,side):
    return tuple(int(v) for v in np.rint(p+t*along+np.array([t[1],-t[0]])*side))


def arrow(p,t,y,side=0,color='yellow_concrete',floor=False):
    # Inverse rasterization fills every destination voxel, including diagonal glyphs and their backing.
    ax,az=frame_point(p,t,0,side)
    if not floor and rally_pit_stops.protect(sys.modules[__name__],ax,az,9):return
    if any((a['x']-ax)**2+(a['z']-az)**2<19**2 and abs(a['y']-y)<=5 for a in ARROWS):return
    if not floor and color=='yellow_concrete' and any(np.min(np.sum((b['points']-[ax,az])**2,axis=1))<16**2 for b in SHORTCUTS):return
    if color=='lime_concrete':
        index=int(np.argmin(np.sum((POINTS[:np.searchsorted(DISTANCES,MOUNTAIN_START)]-[ax,az])**2,axis=1)))
        if np.linalg.norm(POINTS[index]-[ax,az])<float(half_width(DISTANCES[index]))+10:return
    for entries in course.base.BLOCK_ENTITIES.values():
        if any(abs(be['y'][1]-y)<12 and (be['x'][1]-ax)**2+(be['z'][1]-az)**2<13**2 for be in entries):return
    glyph=[];backing=[];pixels={}
    for x in range(ax-9,ax+10):
        for z in range(az-9,az+10):
            a=(x-ax)*t[0]+(z-az)*t[1];b=(x-ax)*t[1]-(z-az)*t[0]
            if abs(a)>7 or abs(b)>5:continue
            head=-.5<=a<=5.5 and abs(b)<=max(.7,(5.5-a)*.8)
            marked=(-5.5<=a<=1 and abs(b)<=1.65) or head
            pixels[x,z]=marked
    # Give diagonal-only contacts a shared edge, so arrow tips never become detached pixels.
    for x,z in list(pixels):
        if not pixels[x,z]:continue
        for dx,dz in ((1,1),(1,-1),(-1,1),(-1,-1)):
            if not pixels.get((x+dx,z+dz),False):continue
            if pixels.get((x+dx,z),False) or pixels.get((x,z+dz),False):continue
            candidates=[v for v in ((x+dx,z),(x,z+dz)) if v in pixels]
            if candidates:
                join=min(candidates,key=lambda v:abs((v[0]-ax)*t[1]-(v[1]-az)*t[0]))
                pixels[join]=True
    for (x,z),marked in pixels.items():
        course.box(x,y,z,x,y,z,color if marked else 'polished_deepslate')
        if not floor:course.box(x,y+1,z,x,y+5,z,'air')
        (glyph if marked else backing).append([x,y,z])
    ARROWS.append({'x':ax,'y':y,'z':az,'dx':float(t[0]),'dz':float(t[1]),
                   'color':color,'floor':floor,'glyph':glyph,'backing':backing})


def gate(s,title,color='cyan',y=64,checkpoint=True):
    # V2 asks for later gates too; V3 replaces those after the shared opening section.
    if s>3500*FLAT_SCALE:return
    harbor=next((p for p in course.SUPPLIES if abs(p['s']-s)<p['half_length']+3),None)
    if harbor is None:
        ORIGINAL_GATE(s,title,color,y,checkpoint);return
    p,t=course.at(s);span=math.ceil(float(half_width(s)))+3;outer=harbor['edge']+20
    if checkpoint:
        course.GATES.append({'s':s,'x':round(float(p[0]),2),'z':round(float(p[1]),2),'y':y,
            'yaw':round(math.degrees(math.atan2(-t[0],t[1])),2),'title':title,'gorge':y==66,
            'capture_half_width':outer+2})
    # Keep the checkpoint arch supported on the far bank, outside the widened harbor water.
    for side in (-span,outer):
        x,z=course.pos(s,side);course.box(x-1,60,z-1,x+1,77,z+1,color+'_concrete')
        course.box(x-1,75,z-1,x+1,75,z+1,'sea_lantern')
    for side in np.arange(-span,outer+.5,.5):
        x,z=course.pos(s,side);course.box(x,77,z,x,78,z,color+'_concrete')
        if int(side)%4==0:course.box(x,77,z,x,77,z,'sea_lantern')
    course.sign(s-6,outer+2,[title,'检查点','补给可直接驶入',''])


def elevated_gate(s,title):
    p,t=course.at(s);y=int(water_y(s))+1
    course.GATES.append({'s':s,'x':round(float(p[0]),2),'z':round(float(p[1]),2),'y':y,
        'yaw':round(math.degrees(math.atan2(-t[0],t[1])),2),'title':title})
    edge=math.ceil(float(half_width(s)))+3
    if s==LENGTH-28:edge=math.ceil(float(finish_width(0)))+3
    for side in (-edge,edge):
        x,z=course.pos(s,side)
        course.box(x-1,y-4,z-1,x+1,y+12,z+1,'stone_bricks')
        course.box(x,y+10,z,x,y+11,z,'sea_lantern')
    for side in range(-edge,edge+1):
        x,z=course.pos(s,side)
        course.box(x,y+12,z,x,y+13,z,'white_concrete' if int(side/3)%2 else 'cyan_concrete')


def strip(points,y,half,material,dy=0):
    for i in range(len(points)-1):
        p=points[i];q=points[i+1];v=q-p
        lo=np.floor(np.minimum(p,q)-half).astype(int);hi=np.ceil(np.maximum(p,q)+half).astype(int)
        zz,xx=np.mgrid[lo[1]:hi[1]+1,lo[0]:hi[0]+1]
        t=np.clip(((xx-p[0])*v[0]+(zz-p[1])*v[1])/np.dot(v,v),0,1)
        inside=(xx-p[0]-t*v[0])**2+(zz-p[1]-t*v[1])**2<=half*half
        for x,z in zip(xx[inside],zz[inside]):course.box(int(x),y,int(z),int(x),y+dy,int(z),material)


def shortcut_geometry():
    for branch in SHORTCUTS:
        pts=branch['points']
        strip(pts,59,8,'stone_bricks',4)
        strip(pts,64,7,'air',19)
        strip(pts,60,5,'water',3)
        middle=len(pts)//2
        strip(pts[middle:middle+3],60,5,'orange_concrete',4)
        for j in range(15,len(pts)-15,25):
            t=pts[j+1]-pts[j];t/=np.linalg.norm(t)
            arrow(pts[j],t,64,10,'lime_concrete')
        for label,s in [('近道入口：窄槽跳栏',branch['entry_s']-24),('近道并线',branch['exit_s']+20)]:
            x,z=course.pos(s,float(half_width(s))+8)
            course.base.sign(x,65,z,[branch['title'],label,'绿色箭头 / 可选路线','主线黄色箭头'])
    # Branch embankments must merge into, rather than cross, the full-width main channel.
    for branch in SHORTCUTS:
        for join in (branch['entry_s'],branch['exit_s']):
            for s in np.arange(join-35,join+45,.75):
                for side in np.arange(-float(half_width(s)),float(half_width(s))+.5,.7):
                    x,z=course.pos(s,side)
                    course.box(x,60,z,x,63,z,'water')
                    course.box(x,64,z,x,73,z,'air')


def mountain_geometry():
    rng=np.random.default_rng(390171)
    for theta in np.arange(0,math.tau,.12):
        radius=float(rng.uniform(75,116))
        x=int(SPIRAL_CENTER[0]+radius*math.cos(theta));z=int(SPIRAL_CENTER[1]+radius*.88*math.sin(theta))
        y=int(mountain_height(x,z))+1
        course.box(x,y,z,x,y+8,z,'spruce_log',axis='y')
        for dy,r in [(4,3),(6,2),(8,1)]:
            course.box(x-r,y+dy,z-r,x+r,y+dy+1,z+r,'spruce_leaves',persistent='true',distance='1',waterlogged='false')
            if y>150:course.box(x-r,y+dy+2,z-r,x+r,y+dy+2,z+r,'snow_block')
    for theta in (1.0,3.8):
        x=int(SPIRAL_CENTER[0]+178*math.cos(theta));z=int(SPIRAL_CENTER[1]+160*math.sin(theta))
        y=int(mountain_height(x,z))
        course.box(x-2,62,z-2,x+2,y,z+2,'mossy_cobblestone')
        course.box(x-1,63,z-1,x+1,y+2,z+1,'water',level='8')
        course.box(x-1,y+3,z-1,x+1,y+3,z+1,'water',level='0')
    for lo,hi,kind in [(150,225,'tunnel'),(350,395,'culvert'),(580,665,'tunnel'),(830,875,'culvert')]:
        STRUCTURES.append({'start':MOUNTAIN_START+lo,'end':MOUNTAIN_START+hi,'kind':kind,
                           'clearance':8 if kind=='tunnel' else 5})
    for s in np.arange(MOUNTAIN_START-450,MOUNTAIN_START,1):
        for side in np.arange(-float(half_width(s)),float(half_width(s))+.5,.8):
            x,z=course.pos(s,side)
            course.box(x,60,z,x,63,z,'water')
            course.box(x,64,z,x,81,z,'air')
    # Rasterize entire voxel columns. Separate elevation bands preserve vertically overlapping decks.
    columns={}
    for s in np.arange(MOUNTAIN_START,LENGTH+.5,.8):
        p,t=course.at(s);edge=math.ceil(float(half_width(s)));r=edge+7
        zz,xx=np.mgrid[math.floor(p[1]-r):math.ceil(p[1]+r)+1,math.floor(p[0]-r):math.ceil(p[0]+r)+1]
        dx,dz=xx-p[0],zz-p[1];along=dx*t[0]+dz*t[1];side=dx*t[1]-dz*t[0]
        keep=(np.abs(along)<=1)&(np.abs(side)<=edge+6)
        for x,z,a,b in zip(xx[keep],zz[keep],along[keep],side[keep]):
            station=float(np.clip(s+a,MOUNTAIN_START,LENGTH));y=int(water_y(station))
            key=(int(x),int(z),y//24);distance=float(a*a+b*b)
            if key not in columns or distance<columns[key][0]:columns[key]=(distance,station,y,abs(float(b)),edge)
    for (x,z,band),(distance,s,y,side,edge) in columns.items():
        section=next((v for v in STRUCTURES if v['start']<=s<=v['end']),None)
        if section:
            roof=y+section['clearance']+1
            course.box(x,y-4,z,x,roof+5,z,'stone' if section['kind']=='tunnel' else 'mossy_stone_bricks')
            course.box(x,roof+6,z,x,roof+6,z,'moss_block')
        if side<=edge+3:
            course.box(x,y-5,z,x,y-4,z,'stone_bricks')
            if side>edge:
                course.box(x,y-3,z,x,y+1,z,'deepslate_bricks')
                course.box(x,y+2,z,x,y+2,z,'polished_andesite')
    # Excavate after every rock shell and pier exists, so later slices cannot refill the waterway.
    for (x,z,band),(distance,s,y,side,edge) in columns.items():
        if side>edge:continue
        section=next((v for v in STRUCTURES if v['start']<=s<=v['end']),None)
        course.box(x,y-3,z,x,y,z,'water')
        course.box(x,y+1,z,x,y+(section['clearance'] if section else 17),z,'air')
    for section in STRUCTURES:
        for s in np.arange(section['start']+5,section['end'],12):
            y=int(water_y(s));edge=math.ceil(float(half_width(s)))
            for side in (-edge-1,edge+1):
                x,z=course.pos(s,side)
                course.box(x,y+3,z,x,y+3,z,'sea_lantern')
        for s in (section['start']-15,section['end']+12):
            x,z=course.pos(s,float(half_width(s))+4)
            course.base.sign(x,int(water_y(s))+3,z,['岩壁隧道' if section['kind']=='tunnel' else '高架涵洞',
                '净空 '+str(section['clearance'])+' 格','建议压缩 / 控制速度','沿灯带行驶'])


def finish_width(along):
    return 14+12*np.clip((along+12)/35,0,1)


def finish_harbor():
    p,t=course.at(LENGTH-28);y=int(water_y(LENGTH))
    for x in range(int(p[0])-140,int(p[0])+141):
        for z in range(int(p[1])-140,int(p[1])+141):
            a=(x-p[0])*t[0]+(z-p[1])*t[1];b=(x-p[0])*t[1]-(z-p[1])*t[0]
            if not -22<=a<=94:continue
            width=float(finish_width(a))
            if abs(b)>width+10:continue
            course.box(x,y-6,z,x,y-4,z,'stone_bricks')
            wet=abs(b)<=width and a<82
            if wet:
                course.box(x,y-3,z,x,y,z,'water')
                course.box(x,y+1,z,x,y+16,z,'air')
                if abs(a)<1.5:course.box(x,y-4,z,x,y-4,z,'black_concrete' if int(math.floor(b/2))%2 else 'white_concrete')
            else:
                course.box(x,y-3,z,x,y,z,'stone_bricks')
                course.box(x,y+1,z,x,y+15,z,'air')
                course.box(x,y,z,x,y,z,'polished_andesite' if int(a+b)%5 else 'smooth_stone')
                if abs(b)>width+8 or a>92:course.box(x,y+1,z,x,y+2,z,'stone_brick_wall',up='true',north='none',south='none',east='none',west='none',waterlogged='false')
            if int(round(a))%4==0 and int(round(b))%4==0:
                FINISH_CELLS.append([x,y,z,'water' if wet else 'solid'])
    # A closed end promenade and deck-level finish stripe flank the open racing channel.
    for side in (-1,1):
        x,z=frame_point(p,t,87,side*20)
        course.base.sign(x,y+1,z,['盘山终点港','恭喜完赛','右键羽毛返回大厅','全员完赛后时钟重开'])
    for a in (35,65):
        x,z=frame_point(p,t,a,float(finish_width(a))+5)
        course.box(x,y+1,z,x,y+9,z,'stripped_spruce_log',axis='y')
        course.box(x-1,y+8,z-1,x+1,y+9,z+1,'sea_lantern')


def lake_radius(x,z):
    dx=(np.asarray(x)-SPIRAL_CENTER[0])/27;dz=(np.asarray(z)-SPIRAL_CENTER[1])/22
    theta=np.arctan2(dz,dx)
    return np.sqrt(dx*dx+dz*dz)/(1+.06*np.sin(theta*3)+.04*np.cos(theta*5))


def mountain_height(x,z):
    x=np.asarray(x);z=np.asarray(z)
    r=np.sqrt(((x-SPIRAL_CENTER[0])/1.05)**2+((z-SPIRAL_CENTER[1])/.92)**2)
    rugged=5*np.sin(x*.071)*np.cos(z*.054)+3*np.sin((x+z)*.12)
    original=61+139*np.clip(1-r/145,0,1)**.65+np.where(r<145,rugged,0)
    rho=lake_radius(x,z)
    theta=np.arctan2(z-SPIRAL_CENTER[1],x-SPIRAL_CENTER[0])
    ridge=207+4*np.sin(theta*3)+2*np.cos(theta*7)
    # Steep enclosed inner cliffs, a broad rounded rim, then a gentle outer scree slope.
    climb=np.clip((rho-1)/.43,0,1)
    inner=LAKE_Y+2+(ridge-LAKE_Y-2)*np.sin(climb*np.pi/2)
    blend=np.clip((rho-1.78)/1.02,0,1);blend=blend*blend*(3-2*blend)
    crater=inner*(1-blend)+original*blend
    return np.floor(np.where((rho>1)&(rho<2.8),crater,original)).astype(int)


def lake_bed(rho):
    return LAKE_Y-3-int(10*(1-rho*rho))


def terrain(cx,cz):
    low,d,s=ORIGINAL_TERRAIN(cx,cz)
    blocks=np.zeros((320,16,16),np.uint16)
    blocks[:len(low)]=low
    zz,xx=np.mgrid[cz*16:cz*16+16,cx*16:cx*16+16]
    r=np.sqrt(((xx-SPIRAL_CENTER[0])/1.05)**2+((zz-SPIRAL_CENTER[1])/.92)**2)
    summit=mountain_height(xx,zz)
    yy=np.arange(-64,256)[:,None,None]
    mask=(r<145)[None]&(yy<=summit[None])&(yy>59)
    blocks[mask]=course.state('stone')
    for iy,iz,ix in zip(*(np.where((yy==summit[None])&(r<145)[None]))):
        blocks[iy,iz,ix]=course.state('snow_block' if summit[iz,ix]>161 else 'moss_block')
    lake=lake_radius(xx,zz)
    for iz,ix in zip(*np.where(lake<2.8)):
        rho=lake[iz,ix]
        if rho<=1:
            bed=lake_bed(rho)
            blocks[1:bed+65,iz,ix]=course.state('stone')
            blocks[bed+62:bed+65,iz,ix]=course.state('gravel')
            blocks[bed+65:LAKE_Y+65,iz,ix]=course.state('water',level='0')
            blocks[LAKE_Y+65:,iz,ix]=course.AIR
        else:
            top=int(summit[iz,ix])
            # Geological bands show through the snow on the inner caldera wall.
            for y in range(LAKE_Y-4,top+1):
                material=('tuff','deepslate','basalt','stone')[(y//4)%4]
                blocks[y+64,iz,ix]=course.state(material)
            blocks[top+64,iz,ix]=course.state('snow_block' if rho>1.23 else 'basalt')
            blocks[top+65:,iz,ix]=course.AIR
    blocks=rally_ground_details.surface(sys.modules[__name__],blocks,d,s,cx,cz)
    return blocks,d,s


def scenery():
    # V2 scenery stays on the opening sector; high-level decorations are placed at their own altitude.
    saved=course.LENGTH;course.LENGTH=MOUNTAIN_START
    try:ORIGINAL_SCENERY()
    finally:course.LENGTH=saved


def prepare_index():
    ORIGINAL_INDEX()
    # The route's normal generation radius leaves a hole at the center of a wide spiral.
    for cx in range(int((SPIRAL_CENTER[0]-250)//16),int((SPIRAL_CENTER[0]+250)//16)+1):
        for cz in range(int((SPIRAL_CENTER[1]-250)//16),int((SPIRAL_CENTER[1]+250)//16)+1):
            if (cx,cz) in course.INDEX:continue
            p=np.array([cx*16+8,cz*16+8])
            if np.linalg.norm(p-SPIRAL_CENTER)<235:
                course.INDEX[cx,cz]=[int(np.argmin(np.sum((POINTS-p)**2,axis=1)))]


def dock_approaches():
    rally_pit_stops.clear_markers(sys.modules[__name__])


def bridge_supports():
    samples=np.arange(max(0,MOUNTAIN_START-500),LENGTH,3.)
    centers=np.array([course.at(s)[0] for s in samples]);levels=water_y(samples)
    widths=half_width(samples)+8
    frames=[(float(s),*course.at(s),int(water_y(s)),float(half_width(s))) for s in np.arange(MOUNTAIN_START+35,LENGTH-10,55)]
    end_p,end_t=course.at(LENGTH-28)
    frames += [(LENGTH+a,end_p+end_t*a,end_t,int(water_y(LENGTH)),float(finish_width(a))+10) for a in (12,45,85)]
    for s,p,t,y,half in frames:
        top=y-5
        # Portal piers stand outside every lower waterway; the cap beam carries the offset deck.
        protect=levels-5<=top
        placed=[]
        for sign in (-1,1):
            found=None
            for offset in np.arange(half+9,320,2):
                x,z=frame_point(p,t,0,sign*offset)
                if np.any(np.sum((centers[protect]-[x,z])**2,axis=1)<widths[protect]**2):continue
                if any(47<=a['y']<=top+3 and (a['x']-x)**2+(a['z']-z)**2<12**2 for a in ARROWS):continue
                found=(x,z,sign*offset);break
            if found is None:raise RuntimeError(f'No clear pier position at {s}')
            x,z,offset=found
            course.box(x-2,47,z-2,x+2,50,z+2,'polished_andesite')
            course.box(x-1,48,z-1,x+1,top,z+1,'stone_bricks')
            SUPPORTS.append({'x':x,'z':z,'bottom':48,'top':top,'s':float(s)})
            placed.append(offset)
        beam=[]
        for offset in np.arange(placed[0],placed[1]+.5,.5):
            x,z=frame_point(p,t,0,offset)
            course.box(x-1,top-1,z-1,x+1,top,z+1,'polished_andesite')
            beam.append([x,top,z])
        SUPPORTS[-1]['beam']=beam


def railing_lights():
    for s in np.arange(MOUNTAIN_START+12,LENGTH-58,12):
        y=int(water_y(s));edge=math.ceil(float(half_width(s)))
        for side in (-edge-1.5,edge+1.5):
            x,z=course.pos(s,side)
            if any((a['x']-x)**2+(a['z']-z)**2<10**2 and abs(a['y']-y)<7 for a in ARROWS):continue
            course.box(x,y+2,z,x,y+2,z,'polished_andesite')
            course.box(x,y+3,z,x,y+4,z,'stone_brick_wall',up='true',north='none',south='none',east='none',west='none',waterlogged='false')
            course.box(x,y+5,z,x,y+5,z,'lantern',hanging='false',waterlogged='false')
            LIGHTS.append([x,y+5,z,'lantern'])
        x,z=course.pos(s)
        if not any(a['floor'] and (a['x']-x)**2+(a['z']-z)**2<12**2 and abs(a['y']-(y-4))<2 for a in ARROWS):
            course.box(x,y-4,z,x,y-4,z,'sea_lantern')
            LIGHTS.append([x,y-4,z,'sea_lantern'])
    # Expose fixtures on the inner wall; lamps outside a tunnel shell cannot illuminate the lane.
    for s in np.arange(MOUNTAIN_START+2,LENGTH-50,6):
        y=int(water_y(s));edge=math.ceil(float(half_width(s)))
        for side in (-edge-.5,edge+.5):
            x,z=course.pos(s,side)
            course.box(x,y+1,z,x,y+1,z,'sea_lantern')
            LIGHTS.append([x,y+1,z,'sea_lantern'])
        section=next((v for v in STRUCTURES if v['start']<=s<=v['end']),None)
        if section:
            for side in (-6,0,6):
                x,z=course.pos(s,side);roof=y+section['clearance']+1
                course.box(x,roof,z,x,roof,z,'sea_lantern')
                LIGHTS.append([x,roof,z,'sea_lantern'])
    # Invisible vanilla light blocks fill wide lanes without adding collision or changing water depth.
    for s in np.arange(MOUNTAIN_START+2,LENGTH-50,4):
        y=int(water_y(s));edge=float(half_width(s))
        for side in np.linspace(-edge+2,edge-2,5):
            x,z=course.pos(s,side)
            course.box(x,y+3,z,x,y+3,z,'light',level='15',waterlogged='false')
            LIGHTS.append([x,y+3,z,'light'])
    p,t=course.at(LENGTH-28);y=int(water_y(LENGTH))
    for along in range(-6,91,12):
        for side in (-1,1):
            x,z=frame_point(p,t,along,side*(float(finish_width(along))+7))
            course.box(x,y+1,z,x,y+3,z,'stone_brick_wall',up='true',north='none',south='none',east='none',west='none',waterlogged='false')
            course.box(x,y+4,z,x,y+4,z,'lantern',hanging='false',waterlogged='false')
            LIGHTS.append([x,y+4,z,'lantern'])
    # The harbor also needs coverage between decorative promenade lanterns.
    for along in range(-10,81,6):
        for side in np.arange(-float(finish_width(along))+3,float(finish_width(along))-2,6):
            x,z=frame_point(p,t,along,side)
            course.box(x,y+3,z,x,y+3,z,'light',level='15',waterlogged='false')
            LIGHTS.append([x,y+3,z,'light'])


def geometry():
    global DECOR
    course.geometry(FLAT_SCALE)
    course.GATES[:]=[g for g in course.GATES if g['s']<=3500*FLAT_SCALE]
    distances=set(np.arange(3650*FLAT_SCALE,MOUNTAIN_START,130))
    for branch in SHORTCUTS:
        distances={s for s in distances if not branch['entry_s']-30<s<branch['exit_s']+30}
        distances.update([branch['entry_s']-35,branch['exit_s']+35])
    distances.update(np.arange(MOUNTAIN_START+45,LENGTH-100,140))
    distances.add(LENGTH-28)
    mountain_geometry()
    finish_harbor()
    shortcut_geometry()
    dock_approaches()
    for i,s in enumerate(sorted(distances)):
        section=next((v for v in STRUCTURES if v['start']-15<=s<=v['end']+15),None)
        if section:continue
        title='山巅终点' if s==LENGTH-28 else ('盘山水路' if s>=MOUNTAIN_START else '曲水连弯')+f' {i+1}'
        elevated_gate(float(s),title)
    course.GATES.sort(key=lambda g:g['s'])
    for s in np.arange(35,LENGTH-30,32):
        if 1135*FLAT_SCALE<s<1250*FLAT_SCALE or s>LENGTH-60:continue
        if any(abs(s-g['s'])<13 for g in course.GATES):continue
        p,t=course.at(s);y=int(water_y(s))
        section=next((v for v in STRUCTURES if v['start']-10<s<v['end']+10),None)
        if section:
            # Center the floor glyph on one terrace; no raised pixels protrude into the water.
            marker=MOUNTAIN_START+math.floor((s-MOUNTAIN_START)/ELEVATION_STEP)*ELEVATION_STEP+ELEVATION_STEP/2
            p,t=course.at(marker);y=int(water_y(marker))
            if not any(np.linalg.norm(np.array([a['x'],a['z']])-p)<16 and abs(a['y']-(y-4))<2 for a in ARROWS):
                arrow(p,t,y-4,color='sea_lantern',floor=True)
        else:
            for side in (-float(half_width(s))-9,float(half_width(s))+9):arrow(p,t,y+2,side)
    bridge_supports()
    railing_lights()
    entities=course.base.BLOCK_ENTITIES[0,-2]
    entities[:]=[be for be in entities if (be['x'][1],be['y'][1],be['z'][1])!=(0,65,-32)]
    course.base.sign(0,65,-32,['河豚盘山拉力赛 V12',f'{LENGTH/1000:.1f} 公里','黄色主线 / 绿色近道','岸上起航'])
    DECOR=sculpted_wonders.build(sys.modules[__name__])
    DECOR.update(rally_ground_details.build(sys.modules[__name__]))


def navigation():
    fn=course.fn;p=OUT/'datapacks/puffer_rally/data/puffer_rally/function'
    (OUT/'datapacks/puffer_rally/pack.mcmeta').write_text(json.dumps({'pack':{'pack_format':48,
        'description':'Puffer Mountain Rally V12 / one-block-deep waterfront stations'}}),encoding='utf-8')
    load=(p/'load.mcfunction').read_text(encoding='utf-8').splitlines()
    load += [f'scoreboard objectives add {name} dummy' for name in ('lr_navclock','lr_off','lr_warn')]
    fn('load',load)
    tick=(p/'tick.mcfunction').read_text(encoding='utf-8').splitlines()
    tick += ['execute if score #phase lr_state matches 2 run function puffer_rally:navigation_tick',
             'function puffer_rally:scenery_tick']
    mountain_scenery.effects_pack(course,DECOR)
    fn('tick',tick)
    nav=['scoreboard players add #nav lr_navclock 1',
         'execute unless score #nav lr_navclock matches 10.. run return 0','scoreboard players set #nav lr_navclock 0',
         'scoreboard players remove @a[scores={lr_warn=1..}] lr_warn 1']
    for i,g in enumerate(course.GATES[:-1]):
        end=course.GATES[i+1]['s'];lo=max(0,g['s']-45);hi=min(LENGTH,end+35)
        samples=list(np.arange(lo,hi+1,16))+[hi]
        lines=['scoreboard players set #inside lr_tmp 0']
        for s in samples:
            x,z=course.at(s)[0];y=int(water_y(s))+1
            # Box height distinguishes stacked decks; full width permits legitimate sliding and jumping.
            radius=float(half_width(s))+15
            lines.append(f'execute positioned {x-radius:.2f} {y-6} {z-radius:.2f} if entity @s[dx={radius*2:.1f},dy=20,dz={radius*2:.1f}] run scoreboard players set #inside lr_tmp 1')
        for branch in SHORTCUTS:
            if lo<=branch['entry_s']<=hi or (branch['entry_s']<lo<branch['exit_s']):
                for x,z in branch['points'][::12]:
                    lines.append(f'execute positioned {x:.2f} 64 {z:.2f} if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1')
        for station in course.SUPPLIES:
            if lo-30<=station['s']<=hi+30:
                lines.append(f'execute positioned {station["x"]} 65 {station["z"]} if entity @s[distance=..23] run scoreboard players set #inside lr_tmp 1')
        lines += ['execute if score #inside lr_tmp matches 1 run scoreboard players set @s lr_off 0',
                  'execute if score #inside lr_tmp matches 0 run scoreboard players add @s lr_off 1',
                  'execute if score @s lr_off matches 3.. unless score @s lr_warn matches 1.. run function puffer_rally:off_course']
        fn(f'nav_{i}',lines)
        nav.append(f'execute as @a[tag=lr_racer,scores={{lr_cp={i},lr_place=0}}] at @s run function puffer_rally:nav_{i}')
    fn('navigation_tick',nav)
    fn('off_course',['scoreboard players set @s lr_warn 6','title @s actionbar '+course.label('偏航 · 沿黄色箭头返回','yellow'),
                     'playsound minecraft:block.note_block.pling master @s ~ ~ ~ 0.7 0.8'])
    # Reset the warning timer with each new start, without using camera yaw to judge navigation.
    inventory=(p/'inventory.mcfunction').read_text(encoding='utf-8').splitlines()
    fn('inventory',inventory+['scoreboard players set @s lr_off 0','scoreboard players set @s lr_warn 0'])


def configure():
    course.OUT=OUT;course.base.OUT=OUT
    course.START_Y,course.START_Z=65,-17
    course.GATE_HEIGHT=14
    course.POINTS=POINTS;course.DISTANCES=DISTANCES;course.LENGTH=LENGTH
    course.half_width=half_width;course.gate=gate;course.scenery=scenery;course.terrain=terrain
    course.prepare_index=prepare_index
    course.dock=lambda s,number:rally_pit_stops.build(sys.modules[__name__],s,number)


def artifacts(chunks):
    gates=course.GATES
    manifest={'minecraft':'1.21.1','required_mod':'longboatlab >=0.10.20','length_blocks':round(LENGTH,2),
        'free_practice':{'tool':'amethyst_shard','trigger':'lr_free','phase':4,'navigation':False,'timing':False,'repeat_supplies':True},
        'chunks':chunks,'gates':gates,'mountain_start':MOUNTAIN_START,'mountain_end_y':int(water_y(LENGTH)),
        'spiral_turns':SPIRAL_TURNS,'main_corner_count':len(FLAT_WAYPOINTS)-2,'arrows':ARROWS,'structures':STRUCTURES,
        'supports':SUPPORTS,
        'lights':LIGHTS,'finish_cells':FINISH_CELLS,'lake_y':LAKE_Y,'countdown_seconds':10,
        'shortcuts':[{k:(v.tolist() if isinstance(v,np.ndarray) else v) for k,v in b.items()} for b in SHORTCUTS],
        'supply_stations':[{k:v for k,v in p.items() if k!='items'} for p in course.SUPPLIES],
        'supply_counts':{'fish':4,'oars':6,'boats':2},'supply_mode':'one_kind_per_station_per_player_per_round',
        'supply_order':list(rally_pit_stops.STATION_KINDS),
        'runtime_tested':False,'navigation_interval_ticks':10,'navigation_warning_cooldown_ticks':60,
        'start_surface':'harbor_deck','start_y':65,'start_z':-17,'decor':DECOR}
    (OUT.parent/(OUT.name+'_manifest.json')).write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
    guide=f'''# 河豚盘山拉力赛 V12

Minecraft Java 1.21.1 / Fabric / Longboat Lab 0.10.20 及以上。
地图目录：{OUT.name}。这是独立新存档，旧版已游玩进度保留。

## 路线

全长约 {LENGTH:.0f} 格。共用赛道、双人竞速，出生港站上青色/橙色平台开始，绿宝石右键可单练。两船在岸上码头起跑，10 秒倒计时后自行从前方宽口下水；救援仍返回水上检查点。
保留开局弹跳水闸、冰道、钩爪峡谷及 4 个个人补给站。删减重复折返与长直线，水道约 27~41 格宽；盘山段约 25~29 格宽。补给码头入口已清除沙土阻挡。
全程 {len(ARROWS)} 组连续实心方向箭头，黄色为主线、绿色为近道，隧道与涵洞用嵌入水底的发光箭头。偏离规定走廊约 1.5 秒后文字与声音提醒，每 3 秒最多一次；不会按镜头朝向误判。
两条 U 弯近道分别缩短约 {SHORTCUTS[0]['exit_s']-SHORTCUTS[0]['entry_s']-SHORTCUTS[0]['length']:.0f} / {SHORTCUTS[1]['exit_s']-SHORTCUTS[1]['entry_s']-SHORTCUTS[1]['length']:.0f} 格。近道宽 11 格，途中有 1 格高跳栏；可选择较宽主线，两者均可正常通过检查点。
最终盘山水槽约 {LENGTH-MOUNTAIN_START:.0f} 格，环山 {SPIRAL_TURNS} 圈，从水面 Y=63 上升至 Y={int(water_y(LENGTH))}；约每 {ELEVATION_STEP} 格升一格，水深 4 格，池底低于过渡面，不设露出水面的台阶。错位门式桥墩避开下层水道，以横梁承托桥面，墩脚连续落入地下地基。
桥墩、护岸、岩山、观景台、沿岸林地与灯带组成沿途景观。两个岩壁隧道净空 8 格、两个涵洞净空 5 格；压缩船较容易通过。上下层检查门分别按高度判定。
终点为闭合港池，终点线后约 80 格水面缓冲区，两侧及尽端有环绕步道和护栏，下方有落地支撑。山顶改为死火山口湖：Y={LAKE_Y} 水面，约 54×44 格不规则湖口，深处约 13 格；厚实积雪环形山脊包围玄武岩、凝灰岩内壁，无熔岩。盘山护栏、隧道、水底与终点步道均有照明。
七组空中雕塑全面重做：双龙使用顺颈朝向的长吻、眉骨眼窝、分叉角、鬃须和鱼鳞腹甲；金凰采用弯颈钩喙、三层收尖飞羽与七根长尾羽；星海鲸补充喉褶、鳍面与星图；水母改为薄伞膜、细触须与宽褶带；云帆舟增加真实船舷、三桅鼓帆、索具与舷楼；河豚使用短棘、鳍纹与气泡；星轮巨桨改为双轨刻度、宝石与铜金镶边。沿海珊瑚、村居和火山口湖保留。主体均为静态方块造型，烟气与魔法光点仅向附近玩家播放。

## 比赛与操作

开局一格船、2 个河豚桶、4 把木铲、1 把重锤；首格留空并选中。补给港水面保持 Y=63，底部抬到 Y=62，只有一格水深；彩色灯标指向可直接驶入的补给水港，乘船靠近水边服务台，空手对准箱子右键领取。
每处补给点仅有一种资源，沿途依次为：①绿色木铲港 6 把；②蓝色木船港 2 艘；③黄色河豚港 4 桶；④黄色河豚港 4 桶。每人每局每站各领一次，不抢占其他选手补给；检查背包和可合并的副手堆叠容量，装不下则不扣领取资格。
木船港的工作台紧邻船箱，两者都放在水边第一排，靠泊后可在船内直接使用。使用原版方块交互距离，当前乘坐的船不会拦截箱子或工作台的右键。
R 切换形态，空格跳跃/松钩，G 发射钩爪，W/S 收放，方向键球面摆动；Alt 全喷，数字小键盘控制单面；H 隐藏 HUD、J 缩放。按键可自定义。
指南针右键救援并保留当前船改装，罚时 10 秒；追溯指针换基础船，罚时 20 秒；羽毛退出，时钟在全员完赛后重开。
侧栏直接显示秒数（含罚时）。安装消耗河豚桶，重锤拆卸返还河豚桶；创造模式不消耗、不返还。
河豚桶和木铲最多堆叠 16 个，仅相同数据可合并。木铲作为工具使用时拆出一把独立消耗耐久，其余放回背包，背包满则掉到身边。
蹲下持普通木铲右键一侧船舷，一次装上当前交互手内的那一叠，上限不足时只安装可容纳的数量，余下仍留在手上。巨大桨仍逐个按安装点放置。
沿岸新增花草、苔地、碎石滩、灌丛、倒木与小路灯；装饰避开主航道、补给入口和近道。旧有七组精雕巨构与盘山村居保留。
重置清理旧船、掉落物与河豚，清空参赛者背包并重新发放。必须顺序通过检查点，近道内部不放必须经过的门。

## 验证范围

离线生成存档并检查 NBT、区块、路径连通取样、垂直净空、近道检查点与数据包引用；另按实际存档方块核对靠泊船体空间、箱子及工作台的视线和交互距离。未编译或启动游戏，船内交互、实际浮力和转弯仍需实机驾驶验收。
生成：python tools/make_mountain_race_save.py；校验：python tools/check_mountain_race_save.py。
'''
    guide += rally_free_practice.GUIDE
    (OUT/'README_游玩指南.md').write_text(guide,encoding='utf-8')
    (OUT.parent/(OUT.name+'_游玩指南.md')).write_text(guide,encoding='utf-8')
    (OUT/'.generated-pristine').write_text('Generated offline. Never overwrite a played save.\n',encoding='utf-8')
    with zipfile.ZipFile(OUT.parent/(OUT.name+'.zip'),'w',zipfile.ZIP_DEFLATED,6) as archive:
        for p in sorted(OUT.rglob('*')):
            if p.is_file():archive.write(p,p.relative_to(OUT.parent))


def main():
    assert LENGTH<5000, 'The complete main course must stay below 5 km'
    parser=argparse.ArgumentParser();parser.add_argument('--refresh-pristine',action='store_true');args=parser.parse_args()
    if (OUT/'level.dat').exists() and (not args.refresh_pristine or not (OUT/'.generated-pristine').exists()
            or any((OUT/n).exists() for n in ('session.lock','playerdata','stats','advancements'))):
        raise SystemExit('Refusing to overwrite an existing or played save: '+str(OUT))
    configure();shortcuts();OUT.mkdir(parents=True,exist_ok=True)
    geometry();course.pack();navigation();rally_pit_stops.pack(sys.modules[__name__]);rally_free_practice.install(OUT);course.level()
    root=decode(gzip.decompress((OUT/'level.dat').read_bytes()))
    root['Data'][1]['LevelName']=string('河豚盘山拉力赛 V12 · 自由练习')
    root['Data'][1]['BorderCenterX']=double(1400)
    # WorldSaveProperties expects this compound even for an overworld-only adventure map.
    root['Data'][1]['DragonFight']=compound({'NeedsStateScanning':byte(1),
        'DragonKilled':byte(0),'PreviouslyKilled':byte(0)})
    save(OUT/'level.dat',root)
    chunks=course.regions();overview();artifacts(chunks)
    print('Ready:',OUT,flush=True)


def overview():
    from PIL import Image,ImageDraw,ImageFont
    scale=.70;offset=np.array([140.,100.])
    image=Image.new('RGB',(1400,1150),'#17333c');draw=ImageDraw.Draw(image)
    font=lambda n:ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',n)
    xy=lambda p:tuple(np.asarray(p)*scale+offset)
    draw.text((35,18),'河豚盘山拉力赛 V12',font=font(30),fill='#f3eac4')
    draw.text((35,60),f'{LENGTH/1000:.1f} km  /  {len(FLAT_WAYPOINTS)-2} 个主线转弯 + {SPIRAL_TURNS} 圈盘山水路  /  {len(ARROWS)} 组箭头',font=font(18),fill='#a8d9d9')
    for i in range(0,len(POINTS)-4,4):
        y=int(water_y(DISTANCES[i]));color=(65+int((y-63)*1.4),165,191) if y>63 else (62,145,171)
        draw.line([xy(POINTS[i]),xy(POINTS[i+4])],fill=color,width=9)
    for b in SHORTCUTS:draw.line([xy(p) for p in b['points']],fill='#b9e16b',width=5)
    labels=[(0,'起航'),(1196*FLAT_SCALE,'钩爪峡谷'),(MOUNTAIN_START,'盘山入口'),(LENGTH-28,'山巅终点')]
    for b in SHORTCUTS:labels.append((b['entry_s'],b['title']))
    for j,(s,name) in enumerate(labels):
        p=xy(course.at(s)[0]);draw.ellipse((p[0]-4,p[1]-4,p[0]+4,p[1]+4),fill='#ffe68a')
        draw.text((p[0]+8,p[1]-26 if j%2 else p[1]+8),name,font=font(17),fill='#ffffff',stroke_width=2,stroke_fill='#17333c')
    # Elevation profile makes the stacked route readable where a plan view overlaps.
    draw.text((35,955),'盘山纵断面：深水槽逐级升高，跨越下层水路',font=font(20),fill='#f3eac4')
    profile=[]
    for s in np.linspace(MOUNTAIN_START,LENGTH,400):profile.append((50+(s-MOUNTAIN_START)/(LENGTH-MOUNTAIN_START)*1240,1100-(int(water_y(s))-63)*1.12))
    draw.line(profile,fill='#89dce3',width=4)
    for section in STRUCTURES:
        x=50+(section['start']-MOUNTAIN_START)/(LENGTH-MOUNTAIN_START)*1240
        draw.text((x,1020 if section['kind']=='culvert' else 990),'涵洞' if section['kind']=='culvert' else '隧道',font=font(15),fill='#f0d18b')
    image.save(OUT.parent/(OUT.name+'_overview.png'))
    image.crop((20,90,1350,930)).resize((64,64)).save(OUT/'icon.png')
    terrain_preview()


def terrain_preview():
    from PIL import Image,ImageDraw,ImageFont
    x0,z0,size=int((SPIRAL_CENTER[0]-304)//16)*16,int((SPIRAL_CENTER[1]-286)//16)*16,608
    colors=np.zeros((size,size,3),np.uint8);colors[:]=[40,91,119]
    heights=np.full((size,size),63.,float)
    palette=[]
    for state in course.base.STATES:
        name=state['Name'][1]
        color=(112,120,124)
        if 'water' in name:color=(61,142,180)
        elif 'leaves' in name:color=(51,100,69)
        elif 'moss' in name or 'grass' in name:color=(102,141,87)
        elif 'snow' in name:color=(225,234,239)
        elif 'deepslate' in name:color=(64,72,76)
        elif 'yellow' in name:color=(234,197,66)
        elif 'sea_lantern' in name:color=(193,242,223)
        elif 'cyan' in name:color=(39,185,189)
        elif 'log' in name or 'planks' in name or 'fence' in name:color=(150,112,73)
        palette.append(color)
    palette=np.array(palette,np.uint8)
    for (cx,cz),(h,ids) in course.SURFACE.items():
        x,z=cx*16-x0,cz*16-z0
        if 0<=x<size and 0<=z<size:
            colors[z:z+16,x:x+16]=palette[ids]
            heights[z:z+16,x:x+16]=h
    shade=np.clip(1+np.gradient(heights,axis=0)*.028-np.gradient(heights,axis=1)*.016,.55,1.25)
    pixels=np.clip(colors*shade[:,:,None],0,255).astype(np.uint8)
    terrain=Image.fromarray(pixels).resize((1216,1216),Image.Resampling.NEAREST)
    canvas=Image.new('RGB',(1256,1320),'#17333c');canvas.paste(terrain,(20,84));draw=ImageDraw.Draw(canvas)
    font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',24)
    draw.text((24,16),'盘山段实际方块俯视图（离线生成，非游戏截图）',font=font,fill='#f3eac4')
    canvas.save(OUT.parent/(OUT.name+'_mountain.png'))


if __name__=='__main__':main()
