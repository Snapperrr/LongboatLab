"""Static voxel landmarks and sparse, nearby-only magic accents for the rally world."""
import math
import json
import numpy as np


class Scenery:
    def __init__(self, mountain):
        self.m=mountain;self.c=mountain.course
        self.landmarks=[];self.effects=[];self.checks=[]
        self.route=mountain.POINTS[::8]
        self.widths=mountain.half_width(mountain.DISTANCES[::8])

    def box(self,x0,y0,z0,x1,y1,z1,material,**props):
        self.c.box(int(x0),int(y0),int(z0),int(x1),int(y1),int(z1),material,**props)

    def voxel(self,p,material,**props):
        x,y,z=np.rint(p).astype(int);self.box(x,y,z,x,y,z,material,**props)

    def orb(self,p,r,material):
        p=np.asarray(p);r=np.broadcast_to(r,(3,))
        lo=np.floor(p-r).astype(int);hi=np.ceil(p+r).astype(int)
        # One contiguous horizontal run per cross-section keeps generation inexpensive.
        for y in range(lo[1],hi[1]+1):
            for z in range(lo[2],hi[2]+1):
                q=((y-p[1])/r[1])**2+((z-p[2])/r[2])**2
                if q>1:continue
                dx=r[0]*math.sqrt(1-q)
                self.box(math.ceil(p[0]-dx),y,z,math.floor(p[0]+dx),y,z,material)

    def tube(self,points,r,material):
        for a,b in zip(points,points[1:]):
            a=np.asarray(a);b=np.asarray(b)
            for t in np.linspace(0,1,max(2,math.ceil(np.linalg.norm(b-a)/1.1))):
                self.orb(a+(b-a)*t,r,material)

    def position(self,s,side,y):
        p,t=self.c.at(s,side)
        return np.array([p[0],y,p[1]]),np.array([t[0],0,t[1]])

    def dragon(self,start,end,phase,name):
        spine=[]
        for s in np.arange(start,end+.1,2):
            u=(s-start)/(end-start)
            side=12*math.sin(u*math.tau*1.8+phase)
            p,t=self.position(s,side,113+10*math.sin(u*math.tau*1.2+phase))
            spine.append((p,t,u))
            radius=min(5.2,.8+u*22)*(1-.12*math.sin(u*30))
            self.orb(p,(radius,radius*.78,radius),'dark_prismarine')
            self.orb(p+[0,-radius*.27,0],(radius*.81,radius*.48,radius*.81),'prismarine_bricks')
            if int((s-start)/2)%5==0:
                # Raised jade scales alternate with luminous joints along the spine.
                self.orb(p+[0,radius*.56,0],(radius*.9,1.4,radius*.9),'oxidized_copper')
                self.tube([p+[0,radius*.65,0],p-t*3+[0,radius+4,0]],.8,'amethyst_block')
                self.voxel(p+[0,radius+3,0],'sea_lantern')
                # Alternating lit belly scales remain visible to racers looking up from beneath.
                self.orb(p+[0,-radius*.68,0],(radius*.7,.65,radius*.7),'waxed_cut_copper')
                self.orb(p+[0,-radius*.78,0],(1.1,.7,1.1),'sea_lantern')
        head,forward,_=spine[-1];side=np.cross(forward,[0,1,0])
        # Separate muzzle, cheeks, brow, eyes, horns and whiskers make a readable dragon silhouette.
        self.orb(head,(7,5.5,7),'oxidized_copper')
        self.tube([head,head+forward*9+[0,-1,0]],(3.8,2.8,3.8),'prismarine_bricks')
        self.orb(head+forward*10+[0,-1,0],(3.5,1.8,3.5),'dark_prismarine')
        self.tube([head+forward*3+[0,-4,0],head+forward*10+[0,-4,0]],(2.6,1.1,2.6),'quartz_block')
        for sign in (-1,1):
            eye=head+forward*3+side*sign*5+[0,2,0]
            self.orb(eye,1.6,'black_concrete');self.orb(eye+forward*.6+side*sign*.9,1,'sea_lantern')
            self.tube([head+side*sign*4+[0,4,0],head-forward*5+side*sign*7+[0,12,0],
                       head-forward*10+side*sign*10+[0,15,0]],1.1,'quartz_block')
            mouth=head+forward*8+side*sign*3
            self.tube([mouth,mouth+forward*5+side*sign*7+[0,-2,0],
                       mouth+side*sign*16-forward*2+[0,2,0]],.6,'end_rod')
            for tooth in (4,7):self.tube([head+forward*tooth+side*sign*2.8+[0,-1,0],
                                         head+forward*tooth+side*sign*2.8+[0,-3,0]],.7,'quartz_block')
        # Two pairs of hooked legs and three-toed claws, all well above sailing clearance.
        for index in (int(len(spine)*.62),int(len(spine)*.86)):
            p,t,u=spine[index];normal=np.cross(t,[0,1,0])
            for sign in (-1,1):
                elbow=p+normal*sign*8+[0,-5,0];foot=p+normal*sign*12+t*4+[0,-10,0]
                self.tube([p,elbow,foot],1.8,'oxidized_copper')
                for toe in (-1,0,1):
                    self.tube([foot,foot+t*5+normal*toe*2+[0,-2,0]],.7,'quartz_block')
        pearl=head+forward*19+[0,1,0]
        self.orb(pearl,4.3,'cyan_stained_glass');self.orb(pearl,2.3,'sea_lantern')
        self.ring(pearl,8,forward,'amethyst_block','pearlescent_froglight')
        self.effects.append((*np.rint(pearl).astype(int),'end_rod'))
        self.landmarks.append({'kind':'dragon','name':name,'start':start,'end':end,'head':head.tolist()})
        self.checks.append([*np.rint(pearl).astype(int),'sea_lantern'])

    def ring(self,center,radius,normal,material,accent):
        normal=np.asarray(normal);normal=normal/np.linalg.norm(normal)
        u=np.cross(normal,[0,1,0]);u/=np.linalg.norm(u)
        v=np.cross(normal,u)
        for i,angle in enumerate(np.linspace(0,math.tau,math.ceil(radius*9))):
            p=center+radius*(u*math.cos(angle)+v*math.sin(angle))
            self.orb(p,.85,accent if i%19<3 else material)

    def magic_oar(self,s,phase):
        center,t=self.position(s,0,108+5*math.sin(phase))
        axis=np.array([t[0]*.6,.8,t[2]*.6]);side=np.cross(t,[0,1,0])
        self.tube([center-axis*14,center+axis*8],1.2,'stripped_dark_oak_log')
        for a in np.arange(7,17,.7):
            width=3.8*math.sin((a-6)/12*math.pi)
            self.tube([center+axis*a-side*width,center+axis*a+side*width],1.1,'waxed_weathered_copper')
        self.ring(center,18,t,'purple_stained_glass','sea_lantern')
        self.ring(center,13,t+side*.55,'amethyst_block','pearlescent_froglight')
        for angle in np.arange(0,math.tau,.55):
            p=center+side*math.cos(angle)*23+np.array([0,math.sin(angle)*12,0])
            self.orb(p,1.2,'sea_lantern')
        self.landmarks.append({'kind':'enchanted_oar','s':s,'center':center.tolist()})
        self.effects.append((*np.rint(center).astype(int),'enchant'))

    def ground(self,x,z):
        m=self.m;rho=float(m.lake_radius(x,z))
        if rho<1.5:return None
        r=math.sqrt(((x-m.SPIRAL_CENTER[0])/1.05)**2+((z-m.SPIRAL_CENTER[1])/.92)**2)
        if r<145:return int(m.mountain_height(x,z))
        i=int(np.argmin(np.sum((m.POINTS-[x,z])**2,axis=1)))
        d=float(np.linalg.norm(m.POINTS[i]-[x,z]))
        return int(self.c.height_at(d,m.DISTANCES[i],x,z))

    def safe(self,x,z,radius):
        return not np.any(np.sum((self.route-[x,z])**2,axis=1)<(self.widths+radius+12)**2)

    def house(self,x,z,y,index):
        b=self.box
        # Terraces have solid foundations to the actual hillside, never floating at a guessed height.
        for xx in range(x-8,x+9):
            for zz in range(z-8,z+9):
                base=self.ground(xx,zz)
                if base is None:continue
                b(xx,min(base,y)-1,zz,xx,y,zz,'stone_bricks')
                b(xx,y+1,zz,xx,y+13,zz,'air')
        b(x-5,y+1,z-4,x+5,y+6,z+4,'stripped_spruce_log',axis='y')
        b(x-4,y+1,z-3,x+4,y+5,z+3,'air')
        b(x-4,y,z-3,x+4,y,z+3,'spruce_planks')
        b(x-1,y+1,z-4,x+1,y+3,z-4,'air')
        roof='dark_prismarine' if index%2 else 'red_terracotta'
        for level in range(5):
            b(x-7+level,y+7+level,z-6,x+7-level,y+7+level,z+6,roof)
        for sign in (-1,1):
            b(x+sign*5,y+3,z-1,x+sign*5,y+4,z+1,'yellow_stained_glass')
            b(x+sign*4,y+3,z,x+sign*4,y+3,z,'shroomlight')
        b(x+2,y+7,z+2,x+3,y+14,z+3,'bricks')
        for xx in range(x-5,x+6):
            b(xx,y+4,z-7,xx,y+4,z-5,'orange_wool' if (xx-x)%2 else 'white_wool')
        for xx in (x-5,x+5):
            b(xx,y+1,z-7,xx,y+3,z-7,'spruce_fence')
            b(xx,y+1,z-6,xx,y+1,z-6,'lantern',hanging='false',waterlogged='false')
        for xx in (x-7,x+7):
            b(xx,y+1,z+5,xx,y+1,z+5,'composter',level='7')
            b(xx,y+1,z+6,xx,y+2,z+7,'hay_block',axis='y')
        # A lit porch, drying rack, market canopy and small allotment give the houses daily-life detail.
        for xx in (x-7,x+7):b(xx,y+1,z+7,xx,y+4,z+7,'spruce_fence')
        b(x-6,y+4,z+7,x+6,y+4,z+7,'chain',axis='x',waterlogged='false')
        for xx in range(x-4,x+5,3):b(xx,y+2,z+7,xx+1,y+3,z+7,'white_wool')
        b(x-3,y,z+5,x+3,y,z+6,'farmland',moisture='7')
        b(x-3,y+1,z+5,x+3,y+1,z+6,'wheat',age='7')
        self.effects.append((x+2,y+15,z+2,'campfire_cosy_smoke'))
        self.landmarks.append({'kind':'hillside_house','center':[x,y,z]})
        self.checks.append([x,y,z,'spruce_planks'])

    def village(self):
        m=self.m;rng=np.random.default_rng(71009);occupied=[]
        for i,theta in enumerate(np.arange(.1,math.tau,.40)):
            radius=103 if i%2 else 114
            x,z=np.rint(m.SPIRAL_CENTER+[radius*math.cos(theta),radius*.88*math.sin(theta)]).astype(int)
            y=self.ground(x,z)
            if y is None or not self.safe(x,z,12):continue
            if any((x-a)**2+(z-b)**2<28**2 for a,b in occupied):continue
            self.house(x,z,y,i);occupied.append((x,z))
        trees=bushes=0
        for _ in range(700):
            theta=rng.uniform(0,math.tau);radius=rng.uniform(72,220)
            x,z=np.rint(m.SPIRAL_CENTER+[radius*math.cos(theta),radius*.88*math.sin(theta)]).astype(int)
            y=self.ground(x,z)
            if y is None or y<64 or not self.safe(x,z,5):continue
            if any((x-a)**2+(z-b)**2<16**2 for a,b in occupied):continue
            if rng.random()<.4:
                height=int(rng.integers(5,10));self.box(x,y,z,x,y+height,z,'spruce_log',axis='y')
                for dy,r in ((height-3,3),(height-1,2),(height+1,1)):
                    for xx in range(x-r,x+r+1):
                        for zz in range(z-r,z+r+1):
                            if abs(xx-x)+abs(zz-z)>r+1:continue
                            self.box(xx,y+dy,zz,xx,y+dy+1,zz,'spruce_leaves',persistent='true',distance='1',waterlogged='false')
                trees+=1
            else:
                self.orb([x,y+1,z],(2,1.5,2),'moss_block')
                self.voxel([x,y+3,z],('azalea','flowering_azalea','fern')[int(rng.integers(3))])
                bushes+=1
        self.landmarks.append({'kind':'vegetation','trees':trees,'bushes':bushes})

    def build(self):
        for args in ((240,680,0,'听潮青龙'),(1350,1850,1.3,'云海游龙'),(2550,3090,3.2,'星河长吟')):
            self.dragon(*args)
        for i,s in enumerate((850,1150,2120,2380,3280)):
            self.magic_oar(s,i)
        self.village()
        return {'landmarks':self.landmarks,'effects':self.effects,'checks':self.checks,
                'minimum_sky_decoration_y':87,'static_blocks':True}


def build(mountain):
    return json.loads(json.dumps(Scenery(mountain).build(),default=lambda value:value.item()))


def effects_pack(course,decor):
    # No global particle storm and no ticking display entities: only nearby racers see sparse accents.
    course.fn('scenery_tick',['scoreboard players add #decor lr_tmp 1',
        'execute unless score #decor lr_tmp matches 10.. run return 0',
        'scoreboard players set #decor lr_tmp 0',
        *[f'execute positioned {x} {y} {z} if entity @a[distance=..72] run particle minecraft:{kind} ~ ~ ~ '
          +('0.5 0.5 0.5 0.01 1 normal @a[distance=..72]' if kind=='campfire_cosy_smoke'
            else '5 3 5 0.015 4 normal @a[distance=..72]') for x,y,z,kind in decor['effects']]])
