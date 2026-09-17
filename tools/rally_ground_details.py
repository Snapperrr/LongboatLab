"""Deterministic terrain materials, riverbank understory and grounded small landmarks."""
import math
import numpy as np
from mountain_scenery import Scenery
import rally_pit_stops


def surface(m,blocks,d,s,cx,cz):
    c=m.course
    zz,xx=np.mgrid[cz*16:cz*16+16,cx*16:cx*16+16]
    top=blocks.shape[0]-1-np.argmax((blocks!=c.AIR)[::-1],axis=0)
    iz,ix=np.indices((16,16));ids=blocks[top,iz,ix]
    wave=np.sin(xx*.18+np.sin(zz*.13))*np.cos(zz*.22)+.45*np.sin((xx+zz)*.49)
    speck=((xx.astype(np.int64)*73856093)^(zz.astype(np.int64)*19349663))&65535
    distance=d-m.half_width(s)
    earth=np.isin(ids,[c.state('grass_block',snowy='false'),c.state('moss_block')])&(distance>5)&(m.lake_radius(xx,zz)>2.8)
    # Blend coherent patches with isolated grains; never change navigable water heights.
    patches=np.where(wave>.65,c.state('moss_block'),np.where(wave<-.80,c.state('coarse_dirt'),
             np.where((wave<-.25)&(speck%7==0),c.state('rooted_dirt'),ids)))
    blocks[top[earth],iz[earth],ix[earth]]=patches[earth]
    shore=(ids==c.state('sand'))&(distance>3)&(m.lake_radius(xx,zz)>2.8)
    material=np.where(wave>.50,c.state('gravel'),np.where(wave<-.8,c.state('smooth_sandstone'),ids))
    blocks[top[shore],iz[shore],ix[shore]]=material[shore]
    dry=earth&(top>128)&(top<240)
    species=np.mod(np.floor((np.sin(xx*.034)+np.cos(zz*.041))*3),5).astype(int)
    for n,name in ((0,'short_grass'),(1,'fern'),(2,'dandelion'),(3,'cornflower'),(4,'oxeye_daisy')):
        grow=dry&(speck%19==0)&(species==n)&(wave>-.3)
        blocks[top[grow]+1,iz[grow],ix[grow]]=c.state(name)
    # Gravel bars below the waterline provide a layered bank without adding collision obstacles.
    return blocks


def build(m):
    scene=Scenery(m);c=m.course;rng=np.random.default_rng(1001211)
    records=[];checks=[]
    def safe(x,z,r):
        if not scene.safe(x,z,r) or rally_pit_stops.protect(m,x,z,r+3):return False
        if any(np.min(np.sum((v['points']-[x,z])**2,axis=1))<(v['half_width']+r+7)**2 for v in m.SHORTCUTS):return False
        for entries in c.base.BLOCK_ENTITIES.values():
            if any((be['x'][1]-x)**2+(be['z'][1]-z)**2<(r+6)**2 for be in entries):return False
        return True
    for i,s in enumerate(np.arange(110,m.MOUNTAIN_START,24)):
        for sign in (-1,1):
            side=sign*(float(m.half_width(s))+rng.uniform(19,38));x,z=c.pos(s,side)
            y=scene.ground(x,z)
            if y is None or y<64 or not safe(x,z,4):continue
            kind=(i+(sign>0))%5
            if kind==0:
                # Rounded, partly buried river boulders with a moss cap and adjacent pebbles.
                scene.orb([x,y,z],(3.2,2.3,2.6),'andesite')
                scene.orb([x-.5,y+1.4,z],(2.3,.8,1.8),'moss_block')
                scene.orb([x+3,y,z+2],(1.5,1.0,1.3),'stone')
                records.append(dict(kind='mossy_boulder',center=[x,y,z]))
            elif kind==1:
                # Fallen timber follows the ground; branches and foliage break the straight silhouette.
                for dx in range(-3,4):
                    ground=scene.ground(x+dx,z)
                    if ground is not None:scene.box(x+dx,ground+1,z,x+dx,ground+1,z,'oak_log',axis='x')
                scene.box(x+1,y+2,z-1,x+1,y+2,z+1,'oak_log',axis='z')
                scene.orb([x-2,y+2,z+1],(2.0,1.3,1.8),'moss_block')
                records.append(dict(kind='fallen_tree',center=[x,y,z]))
            elif kind==2:
                scene.box(x,y+1,z,x,y+5,z,'birch_log',axis='y')
                for dx,dy,dz,r in ((-1,5,0,2.5),(1,6,1,2.4),(0,7,-1,2.1)):
                    for xx in range(x-4,x+5):
                        for zz in range(z-4,z+5):
                            if (xx-x-dx)**2+(zz-z-dz)**2<=r*r:
                                scene.box(xx,y+dy,zz,xx,y+dy+1,zz,'birch_leaves',persistent='true',distance='1',waterlogged='false')
                records.append(dict(kind='birch_clump',center=[x,y,z]))
            elif kind==3:
                for dx,dz in ((-2,-1),(1,1),(2,-2)):
                    yy=scene.ground(x+dx,z+dz)
                    if yy is None:continue
                    scene.orb([x+dx,yy+1,z+dz],(1.5,1.2,1.5),'moss_block')
                    scene.box(x+dx,yy+3,z+dz,x+dx,yy+3,z+dz,'flowering_azalea')
                records.append(dict(kind='flower_thicket',center=[x,y,z]))
            else:
                # Small stone cairns and a warm path lamp make the coast feel inhabited.
                scene.orb([x,y,z],(2.0,.8,2.0),'gravel')
                scene.box(x,y+1,z,x,y+2,z,'cobblestone')
                scene.box(x,y+3,z,x,y+3,z,'lantern',hanging='false',waterlogged='false')
                checks.append([x,y+3,z,'lantern'])
                records.append(dict(kind='trail_cairn',center=[x,y,z]))
    return {'ground_details':records,'ground_checks':checks,'surface_material_patches':True}
