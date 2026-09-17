"""V8: individually composed giant landmarks; every major scene has its own silhouette."""
import json
import math
import numpy as np
from mountain_scenery import Scenery


class Wonders(Scenery):
    def safe(self,x,z,radius):
        if not super().safe(x,z,radius):return False
        return not any(np.any(np.sum((b['points']-[x,z])**2,axis=1)<(b['half_width']+radius+12)**2)
                       for b in self.m.SHORTCUTS)

    def mark(self,kind,name,p,radius,ymin,ymax,**extra):
        self.landmarks.append(dict(kind=kind,name=name,center=np.asarray(p).tolist(),
                                   preview_radius=radius,ymin=ymin,ymax=ymax,**extra))

    def frame(self,s,y):
        p,f=self.position(s,0,y);side=np.cross(f,[0,1,0])
        return p,f,side

    def dragon_pose(self,points,palette,pearl,pose):
        skin,belly,crest=palette
        spine=[]
        for i,p in enumerate(points):
            u=i/(len(points)-1);r=min(9.5,1.2+u*33)
            forward=points[min(i+1,len(points)-1)]-points[max(0,i-1)]
            forward/=np.linalg.norm(forward)
            spine.append((p,forward,r))
            self.orb(p,(r,r*.82,r),skin)
            self.orb(p+[0,-r*.34,0],(r*.8,r*.55,r*.8),belly)
        for i in range(5,len(spine)-3,6):
            p,f,r=spine[i]
            self.orb(p+[0,r*.57,0],(r*.91,1.9,r*.91),crest)
            self.tube([p+[0,r*.7,0],p-f*6+[0,r+9,0]],1.2,'quartz_block')
            self.orb(p+[0,-r*.82,0],(2.2,1.0,2.2),'sea_lantern')
        head=points[-1];f=pearl-head;f/=np.linalg.norm(f)
        side=np.cross(f,[0,1,0]);side/=np.linalg.norm(side)
        self.orb(head,(13,10,13),crest)
        self.tube([head,head+f*18+[0,-2,0]],(6,4.7,6),belly)
        self.tube([head+f*6+[0,-7,0],head+f*18+[0,-7,0]],(5,1.8,5),'quartz_block')
        for sign in (-1,1):
            eye=head+f*5+side*sign*10+[0,3,0]
            self.orb(eye,3.2,'black_concrete')
            self.orb(eye+side*sign*1.7+f,1.8,'ochre_froglight')
            self.tube([head+side*sign*7+[0,7,0],head-f*7+side*sign*14+[0,21,0],
                       head-f*17+side*sign*19+[0,28,0]],1.8,'quartz_block')
            self.tube([head+f*15+side*sign*5,head+f*22+side*sign*17+[0,-4,0],
                       head-f*3+side*sign*30+[0,5,0]],1,'end_rod')
            for tooth in (8,13):self.tube([head+f*tooth+side*sign*5+[0,-3,0],
                                          head+f*tooth+side*sign*5+[0,-6,0]],1,'quartz_block')
        for fraction in (.57,.82):
            p,f,r=spine[int(len(spine)*fraction)];side=np.cross(f,[0,1,0]);side/=np.linalg.norm(side)
            for sign in (-1,1):
                # One dragon reaches toward the pearl; the other banks with its claws swept back.
                reach=9 if pose=='rising' else -8
                elbow=p+side*sign*16+[0,-7,0];foot=p+side*sign*23+f*reach+[0,-16,0]
                self.tube([p,elbow,foot],3,crest)
                for toe in (-1,0,1):self.tube([foot,foot+f*9+side*toe*3+[0,-3,0]],1.2,'quartz_block')

    def twin_dragons(self):
        center,f,side=self.frame(385,158);pearl=center+[0,7,0]
        for sign,palette,pose in ((1,('dark_prismarine','prismarine_bricks','oxidized_copper'),'rising'),
                                   (-1,('blue_terracotta','quartz_block','light_blue_concrete'),'diving')):
            points=[]
            # Opposed sweeping curves: jade dragon climbs from below, pale dragon dives from above.
            for u in np.linspace(0,1,190):
                angle=math.pi*(1.6-1.6*u)+(.15 if sign==1 else -.15)
                radial=92-45*u
                horizontal=side*(sign*radial*math.cos(angle))+f*(radial*math.sin(angle))
                height=(-30+43*u+7*math.sin(u*math.tau)) if sign==1 else (38-27*u+9*math.sin(u*math.pi))
                points.append(center+horizontal+[0,height,0])
            self.dragon_pose(np.array(points),palette,pearl,pose)
        self.orb(pearl,11,'cyan_stained_glass');self.orb(pearl,6,'sea_lantern')
        self.ring(pearl,18,f,'amethyst_block','pearlescent_froglight')
        self.ring(pearl,23,f+side*.7,'light_blue_stained_glass','sea_lantern')
        self.mark('twin_dragons','双龙戏珠 · 一升一俯',center,128,95,240,body_diameter=19)
        self.checks.append([*np.rint(pearl).astype(int),'sea_lantern'])
        self.effects.append((*np.rint(pearl).astype(int),'end_rod'))

    def whale(self):
        p,f,side=self.frame(970,157)
        for along in np.arange(-50,52,2):
            r=20*math.sqrt(max(.01,1-(along/55)**2))
            self.orb(p+f*along,(r,r*.72,r),'blue_concrete')
            self.orb(p+f*along+[0,-r*.42,0],(r*.82,r*.36,r*.82),'light_blue_concrete')
        for sign in (-1,1):
            self.tube([p-f*12+side*sign*13,p-f*29+side*sign*40+[0,-12,0],
                       p-f*8+side*sign*59+[0,-5,0]],(4,2.5,4),'cyan_concrete')
            self.tube([p-f*47,p-f*73+side*sign*29+[0,9,0],p-f*63+side*sign*40+[0,14,0]],(6,2.5,6),'blue_concrete')
            eye=p+f*36+side*sign*12+[0,1,0]
            self.orb(eye,3.2,'black_concrete');self.orb(eye+side*sign*1.8,1.3,'sea_lantern')
        for along in range(-30,39,11):
            for sign in (-1,1):self.orb(p+f*along+side*sign*10+[0,12,0],1.5,'sea_lantern')
        self.tube([p+f*12+[0,14,0],p+f*8+[0,29,0],p-f*1+[0,43,0]],2,'light_blue_stained_glass')
        self.ring(p+f*6+[0,35,0],12,f,'cyan_stained_glass','sea_lantern')
        self.mark('star_whale','星海鲸 · 展鳍巡游',p,91,110,215,span=118)

    def phoenix(self):
        p,f,side=self.frame(1490,158)
        self.tube([p-f*19,p,p+f*20+[0,10,0]],(8,10,8),'red_terracotta')
        neck=p+f*23+[0,17,0];head=p+f*33+[0,25,0]
        self.tube([p+f*13,neck,head],5,'orange_concrete')
        self.orb(head,7,'yellow_concrete')
        self.tube([head+f*5,head+f*17+[0,-3,0]],2.6,'gold_block')
        for sign in (-1,1):
            self.orb(head+side*sign*5+f*2,1.9,'black_concrete')
            self.tube([p, p+side*sign*32+f*6+[0,14,0],p+side*sign*65+[0,30,0]],5,'orange_concrete')
            for i in range(10):
                root=p+side*sign*(12+i*5)+[0,5+i*2,0]
                tip=root+side*sign*(14+i*.7)-f*(34-i*1.2)+[0,7+3*math.sin(i),0]
                self.tube([root,(root+tip)/2+[0,5,0],tip],3.3,('red_concrete','orange_concrete','yellow_concrete')[i%3])
                self.orb(tip,2,'shroomlight')
            self.tube([head-f*3+side*sign*3+[0,5,0],head-f*13+side*sign*6+[0,18,0]],1.5,'red_concrete')
        for i in range(-3,4):
            self.tube([p-f*14,p-f*45+side*i*6+[0,-4,0],p-f*(79-abs(i)*4)+side*i*9+[0,8,0]],3,
                      ('orange_stained_glass','yellow_concrete','red_terracotta')[(i+3)%3])
        self.mark('phoenix','曜日金凰 · 振翅掠空',p,104,125,210,span=160)

    def jellyfish(self):
        p,f,side=self.frame(1990,184)
        for y in np.arange(0,31,1.5):
            r=38*math.sqrt(max(0,1-(y/32)**2))
            self.orb(p+[0,y,0],(r,1,r),'magenta_stained_glass')
        for a in np.linspace(0,math.tau,13)[:-1]:
            n=side*math.cos(a)+f*math.sin(a)
            rib=[p+n*(38*math.cos(t))+[0,32*math.sin(t),0] for t in np.linspace(0,math.pi/2,20)]
            self.tube(rib,1,'amethyst_block')
            self.orb(p+n*36,2.6,'pearlescent_froglight')
            tentacle=[p+n*(25+6*math.sin(u*7+a))+f*(10*u*u)+[0,-u*(58+12*math.sin(a)**2),0] for u in np.linspace(0,1,42)]
            self.tube(tentacle,1.7,'pink_stained_glass' if a<math.pi else 'purple_stained_glass')
            self.orb(tentacle[-1],2.2,'sea_lantern')
        self.orb(p+[0,4,0],8,'pearlescent_froglight')
        self.mark('jellyfish','极光水母 · 十二条流苏',p,57,105,220,diameter=76)

    def sky_ship(self):
        p,f,side=self.frame(2440,135)
        for along in range(-53,54,2):
            w=16*max(.14,1-(abs(along)/57)**2)
            self.orb(p+f*along,(w,7,w),'dark_oak_planks')
            self.tube([p+f*along-side*w+[0,4,0],p+f*along+side*w+[0,4,0]],1.2,'waxed_cut_copper')
        for along,height in ((-24,45),(15,60)):
            mast=p+f*along
            self.tube([mast,mast+[0,height,0]],2,'stripped_spruce_log')
            for sign in (-1,1):
                self.tube([mast+[0,height-4,0],mast+side*sign*27+[0,height-4,0]],1,'gold_block')
                for width in np.arange(2,28,1.3):
                    top=mast+side*sign*width+[0,height-6,0]
                    low=mast+side*sign*width+f*(7*math.sin(width/27*math.pi))+[0,12+width*.3,0]
                    self.tube([top,low],.9,'cyan_stained_glass' if along<0 else 'white_stained_glass')
        self.tube([p+f*48,p+f*73+[0,18,0]],2,'gold_block')
        for along in (-40,0,40):
            for sign in (-1,1):
                hub=p+f*along+side*sign*22
                self.tube([hub-side*sign*10,hub+side*sign*28+[0,-14,0]],1.8,'waxed_weathered_copper')
                self.orb(hub+side*sign*29+[0,-15,0],(6,2,6),'sea_lantern')
        self.mark('sky_ship','逐星舟 · 六桨云帆',p,87,109,201,length=137)

    def puffer_moon(self):
        p,f,side=self.frame(2950,177)
        self.orb(p,32,'yellow_terracotta')
        self.orb(p+[0,-12,0],(29,20,29),'smooth_sandstone')
        for i in range(58):
            y=1-2*(i+.5)/58;angle=i*math.pi*(3-math.sqrt(5));r=math.sqrt(1-y*y)
            n=side*(r*math.cos(angle))+f*(r*math.sin(angle))+[0,y,0]
            if n.dot(f)>.72:continue
            self.tube([p+n*29,p+n*(45+3*math.sin(i))],2.8,'orange_terracotta')
            self.orb(p+n*(45+3*math.sin(i)),1.4,'ochre_froglight')
        for sign in (-1,1):
            eye=p+f*27+side*sign*14+[0,9,0]
            self.orb(eye,7,'white_concrete');self.orb(eye+f*4.5,4,'black_concrete')
            self.orb(eye+f*6+side*sign+[0,1,0],1.6,'sea_lantern')
        self.ring(p+f*33+[0,-7,0],5,f,'orange_terracotta','pink_terracotta')
        self.tube([p-f*29,p-f*49+[0,14,0],p-f*49+[0,-14,0],p-f*29],3,'orange_stained_glass')
        self.mark('puffer_moon','河豚月球 · 鼓气迎宾',p,59,123,230,diameter=96)

    def great_oar(self):
        p,f,side=self.frame(3390,171);axis=f*.5+[0,.866,0]
        self.tube([p-axis*43,p+axis*24],2.7,'stripped_dark_oak_log')
        for d in range(22,54):
            width=12*math.sin((d-19)/39*math.pi)
            self.tube([p+axis*d-side*width,p+axis*d+side*width],2,'waxed_weathered_copper')
        self.ring(p,57,f,'purple_stained_glass','sea_lantern')
        self.ring(p,45,f+side*.65,'amethyst_block','pearlescent_froglight')
        for a in np.arange(0,math.tau,.42):self.orb(p+side*math.cos(a)*64+[0,math.sin(a)*49,0],2,'sea_lantern')
        self.mark('enchanted_oar','潮汐权杖 · 百格星轮',p,75,105,234,length=97,ring_diameter=114)

    def corals(self):
        rng=np.random.default_rng(81026);used=[]
        for i,s in enumerate(np.arange(210,self.m.MOUNTAIN_START-200,155)):
            sign=1 if i%2 else -1;point,_=self.c.at(s,sign*(float(self.m.half_width(s))+100))
            x,z=map(int,np.rint(point));height=32+(i*7)%29
            if not self.safe(x,z,27) or any(np.linalg.norm(point-p)<58 for p in used):continue
            # Keep shoreline forests separate from the existing mountain and its villages.
            if np.linalg.norm(point-self.m.SPIRAL_CENTER)<255:continue
            k=int(np.argmin(np.sum((self.m.POINTS-[x,z])**2,axis=1)))
            ground=int(self.c.height_at(float(np.linalg.norm(self.m.POINTS[k]-[x,z])),self.m.DISTANCES[k],x,z))
            base=np.array([x,ground,z],float);used.append(point)
            self.orb(base+[0,1,0],(12,3,12),'dead_brain_coral_block')
            color=('pink_concrete','orange_terracotta','purple_concrete','cyan_concrete')[i%4]
            accent=('pink_stained_glass','yellow_stained_glass','magenta_stained_glass','light_blue_stained_glass')[i%4]
            if i%3==0:
                # A broad sea fan, with radial ribs and a web woven between neighboring branches.
                ribs=[]
                for a in np.linspace(-1.15,1.15,10):
                    tip=base+[math.sin(a)*25,height*math.cos(a)+10,5*math.sin(a*2)]
                    mid=base+[math.sin(a)*12,height*.48,2]
                    self.tube([base,mid,tip],2.1,color);self.orb(tip,3,accent);ribs.append(tip)
                for fraction in (.5,.7,.9):self.tube([base+(v-base)*fraction for v in ribs],1,accent)
                variant='扇珊瑚'
            elif i%3==1:
                # Antler coral, with different branch count, angle and height at every site.
                self.tube([base,base+[0,height*.55,0],base+[4,height,2]],4,color)
                for branch in range(7):
                    a=branch*2.4+i;root=base+[0,10+branch*3,0]
                    elbow=root+[math.cos(a)*14,7,math.sin(a)*14]
                    tip=elbow+[math.cos(a+.25)*8,10+rng.uniform(0,7),math.sin(a+.25)*8]
                    self.tube([root,elbow,tip],2.5,color);self.orb(tip,3,accent)
                variant='鹿角珊瑚'
            else:
                # Tiered plate coral, carried by a central stone-like organic stem.
                self.tube([base,base+[0,height,0]],4,color)
                for tier in range(5):
                    p=base+[math.sin(tier+i)*5,8+tier*(height-8)/5,math.cos(tier+i)*5]
                    r=18-tier*1.9
                    self.orb(p,(r,2.3,r*.8),color);self.orb(p+[0,1.8,0],(r*.85,.8,r*.65),accent)
                variant='层台珊瑚'
            self.orb(base+[0,3,0],3,'sea_lantern')
            self.checks.append([x,ground+3,z,'sea_lantern'])
            self.mark('giant_coral',variant+f' {i+1}',base,31,ground-3,ground+height+24,variant=variant,height=height)

    def build(self):
        self.twin_dragons();self.whale();self.phoenix();self.jellyfish()
        self.sky_ship();self.puffer_moon();self.great_oar();self.corals();self.village()
        return dict(landmarks=self.landmarks,effects=self.effects,checks=self.checks,
                    minimum_sky_decoration_y=95,static_blocks=True)


def build(mountain):
    return json.loads(json.dumps(Wonders(mountain).build(),default=lambda value:value.item()))
