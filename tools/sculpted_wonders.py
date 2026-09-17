"""V9 authored voxel sculptures: local anatomical frames, tapered surfaces and layered detail.

All geometry becomes ordinary blocks. No display entities or per-tick sculpture animation.
"""
import json
import math
import numpy as np
from rally_wonders import Wonders

UP=np.array([0.,1.,0.])


def unit(v):
    v=np.asarray(v,dtype=float)
    return v/max(1e-9,np.linalg.norm(v))


def basis(forward):
    f=unit(forward);s=unit(np.cross(f,UP));u=unit(np.cross(s,f))
    return np.array([s,u,f])


def bezier(points,n=48):
    p=np.asarray(points,float);t=np.linspace(0,1,n)[:,None]
    return sum(math.comb(len(p)-1,i)*(1-t)**(len(p)-1-i)*t**i*v for i,v in enumerate(p))


def spline(points,spacing=1.4):
    p=np.asarray(points,float);out=[]
    padded=np.vstack((2*p[0]-p[1],p,2*p[-1]-p[-2]))
    for a,b,c,d in zip(padded,padded[1:],padded[2:],padded[3:]):
        for t in np.linspace(0,1,max(3,math.ceil(np.linalg.norm(c-b)/spacing)),endpoint=False):
            out.append(.5*((2*b)+(-a+c)*t+(2*a-5*b+4*c-d)*t*t+(-a+3*b-3*c+d)*t*t*t))
    return np.array(out+[p[-1]])


class Sculptures(Wonders):
    def volume(self,p,extent,axes,shape):
        """Rasterize a local implicit solid directly into world voxels (no rotation pinholes)."""
        p=np.asarray(p,float);extent=np.asarray(extent,float);axes=np.asarray(axes,float)
        reach=np.abs(axes).T@extent
        lo=np.floor(p-reach-1).astype(int);hi=np.ceil(p+reach+1).astype(int)
        xx,zz=np.meshgrid(np.arange(lo[0],hi[0]+1),np.arange(lo[2],hi[2]+1))
        for y in range(lo[1],hi[1]+1):
            delta=np.stack((xx-p[0],np.full_like(xx,y,dtype=float)-p[1],zz-p[2]),axis=-1)
            local=delta@axes.T;ids=shape(local[...,0],local[...,1],local[...,2])
            for row in np.flatnonzero(np.any(ids>=0,axis=1)):
                values=ids[row];edges=np.r_[0,np.flatnonzero(values[1:]!=values[:-1])+1,len(values)]
                for a,b in zip(edges,edges[1:]):
                    if values[a]>=0:
                        material=self.c.base.STATES[int(values[a])]['Name'][1].split(':')[1]
                        self.c.box(int(lo[0]+a),y,int(lo[2]+row),int(lo[0]+b-1),y,int(lo[2]+row),material)

    def ell(self,p,r,material,axes=None,shell=0):
        axes=np.eye(3) if axes is None else np.asarray(axes)
        r=np.broadcast_to(r,(3,));sid=self.c.state(material)
        def shape(x,y,z):
            q=(x/r[0])**2+(y/r[1])**2+(z/r[2])**2
            return np.where((q<=1)&((q>shell*shell) if shell else True),sid,-1)
        self.volume(p,r,axes,shape)

    def stroke(self,points,r,material,end=.5):
        pts=spline(points,.65);total=max(1,len(pts)-1)
        # A sub-voxel tip must still cover the nearest integer voxel, including diagonal curves.
        for i,p in enumerate(pts):self.orb(p,max(.88,r+(end-r)*i/total),material)

    def feather(self,points,width,normal,material,inner,shaft='gold_block',thickness=1.05):
        pts=bezier(points,max(24,int(sum(np.linalg.norm(np.asarray(b)-a) for a,b in zip(points,points[1:])))))
        n=unit(normal)
        for i,p in enumerate(pts):
            t=i/(len(pts)-1);tangent=unit(pts[min(i+1,len(pts)-1)]-pts[max(0,i-1)])
            cross=unit(np.cross(n,tangent));face=unit(np.cross(tangent,cross))
            w=max(.55,width*math.sin(math.pi*t)**.72)
            axes=np.array([cross,face,tangent])
            self.ell(p,(w,thickness,1.3),material,axes)
            if .12<t<.88:
                self.ell(p+face*.65,(w*.67,thickness*.66,1.1),inner,axes)
                self.ell(p-face*.85,(w*.60,thickness*.68,1.1),inner,axes)
        if shaft:
            self.stroke([p+n*(thickness+.2) for p in pts[::max(1,len(pts)//12)]],.72,shaft,.45)
            self.stroke([p-n*(thickness+.2) for p in pts[::max(1,len(pts)//12)]],.65,shaft,.45)

    def dragon_head(self,head,f,skin,light,trim,ivory):
        axes=basis(f);s,u,f=axes
        q=lambda x,y,z:head+s*x+u*y+f*z
        # Cheek, brow and elongated muzzle are aligned with the neck, not world axes.
        self.ell(q(0,0,-2),(9.2,7.8,12),skin,axes)
        self.ell(q(0,-1.6,9),(5.8,4.2,10),light,axes)
        self.ell(q(0,-2.4,17),(6,3,4.6),skin,axes)
        self.ell(q(0,-5.6,8),(5.6,1.8,11),ivory,axes)
        self.ell(q(0,4,2),(3.2,3,8),trim,axes)
        for sign in (-1,1):
            # Recessed almond eyes; a heavy tilted brow avoids a googly-eyed face.
            self.ell(q(sign*8,2.5,4),(1.2,2.6,3.5),'black_concrete',axes)
            self.ell(q(sign*8.85,2.4,5),(1.0,1.55,2.1),'ochre_froglight',axes)
            self.ell(q(sign*9.55,2.4,5.3),(.55,1.35,.65),'black_concrete',axes)
            self.stroke([q(sign*7,4.6,8),q(sign*9.7,5.5,3),q(sign*9.4,4.8,-2)],2.0,trim,1.2)
            self.ell(q(sign*3.8,-.3,19),(.9,1.1,1.5),'black_terracotta',axes)
            self.stroke([q(sign*5.7,-4.1,18),q(sign*5.9,-4,10),q(sign*7.4,-3.3,2)],.72,'black_terracotta',.55)
            for z in (7,14):self.stroke([q(sign*5.7,-2.8,z),q(sign*5.5,-5.5,z+.4)],1.15,ivory,.55)
            # Deer-like branching horns sweep backward, separate from ears and mane.
            self.stroke([q(sign*5.4,6,-6),q(sign*8,15,-10),q(sign*13,23,-19),q(sign*12,25,-28)],2.7,ivory,.55)
            self.stroke([q(sign*8,15,-10),q(sign*16,18,-13),q(sign*19,24,-18)],1.7,ivory,.5)
            self.feather([q(sign*8,1,-7),q(sign*18,8,-14),q(sign*21,4,-22)],3.7,u,skin,trim,None)
            for i in range(7):
                self.feather([q(sign*(6+i*.55),5-i*1.6,-8),q(sign*(14+i*.8),9-i*1.7,-16),
                              q(sign*(15+i),7-i*2,-28-i*.7)],2.0,u,trim,light,None,.75)
            # Long flowing whiskers taper to a point instead of kinked white rods.
            self.stroke([q(sign*5,-.7,15),q(sign*13,-1,25),q(sign*25,3,21),q(sign*29,11,9)],1.25,ivory,.55)
        for i in (-1,0,1):
            self.feather([q(i*2,-6,8),q(i*5,-17,1),q(i*6,-20,-10)],2.0,-u,trim,ivory,None,.8)
        return [head.tolist(),f.tolist()]

    def dragon(self,controls,pearl,palette,pose):
        skin,light,trim,ivory=palette;pts=spline(controls)
        frames=[]
        for i,p in enumerate(pts):
            t=i/(len(pts)-1);axes=basis(pts[min(i+1,len(pts)-1)]-pts[max(0,i-1)])
            r=min(9.1,1.0+t*31)*(1-.10*t);frames.append((p,axes,r))
            self.ell(p,(r,r*.84,1.8),skin,axes)
        for i in range(8,len(frames)-5,4):
            p,axes,r=frames[i];s,u,f=axes
            self.ell(p-u*r*.74,(r*.76,.95,2.4),ivory,axes)
            for j,angle in enumerate((-.95,-.43,.15,.70,1.23)):
                angle+=.16*((i//4)%2);normal=s*math.sin(angle)+u*math.cos(angle)
                tangent=unit(s*math.cos(angle)-u*math.sin(angle))
                at=p+s*(math.sin(angle)*r)+u*(math.cos(angle)*r*.82)
                self.ell(at,(2.7,.85,2.7),light if j%2 else trim,np.array([tangent,normal,f]))
            if i%8==0:
                self.stroke([p+u*r*.8,p+u*(r+5)-f*4,p+u*(r+6)-f*7],1.4,trim,.55)
        for fraction in (.53,.80):
            p,axes,r=frames[int(len(frames)*fraction)];s,u,f=axes
            for sign in (-1,1):
                elbow=p+s*sign*15-u*5-f*4;wrist=p+s*sign*20-u*12+f*(7 if pose=='rise' else -7)
                self.stroke([p+s*sign*r*.6,elbow,wrist],3.5,skin,2)
                self.ell(wrist,(3.6,1.7,4),trim,axes)
                for toe in (-1,0,1):
                    a=wrist+s*toe*2;b=a+f*(8-abs(toe)*2)-u*2+s*toe*2
                    self.stroke([a,b,b+f*1.5-u*3],1.3,ivory,.55)
        head=pts[-1];f=unit(pearl-head)
        return self.dragon_head(head,f,skin,light,trim,ivory)

    def twin_dragons(self):
        p,f,s=self.frame(385,158);q=lambda a:p+s*a[0]+UP*a[1]+f*a[2]
        pearl=q((0,7,8))
        jade=[(-109,-27,-63),(-62,-36,-76),(-30,-24,-45),(-64,-10,-5),
              (-104,3,37),(-91,-1,78),(-49,-22,82),(-63,-19,31),(-48,-3,6)]
        snow=[(112,24,87),(99,49,48),(63,55,29),(39,39,-12),(74,28,-53),
              (110,31,-20),(105,24,33),(76,18,48),(48,12,11)]
        heads=[self.dragon([q(a) for a in jade],pearl,('dark_prismarine','prismarine_bricks','waxed_oxidized_copper','smooth_sandstone'),'rise'),
               self.dragon([q(a) for a in snow],pearl,('light_blue_terracotta','quartz_block','light_blue_concrete','bone_block'),'dive')]
        self.ell(pearl,10,'cyan_stained_glass',shell=.84);self.ell(pearl,5,'sea_lantern')
        self.ring(pearl,14,f,'gold_block','ochre_froglight')
        for angle in (0,2.1,4.2):
            line=[pearl+s*(math.cos(t+angle)*(17+5*t))+UP*(math.sin(t+angle)*15)+f*(t*6-8) for t in np.linspace(0,2.8,35)]
            self.stroke(line,1.1,'light_blue_stained_glass',.55)
        self.mark('twin_dragons','双龙戏珠 · 玉鳞与霜角',p,146,100,251,body_diameter=18,heads=heads)
        self.checks.append([*np.rint(pearl).astype(int),'sea_lantern'])
        self.effects.append((*np.rint(pearl).astype(int),'end_rod'))

    def whale(self):
        p,f,s=self.frame(970,157);axes=np.array([s,UP,f]);q=lambda x,y,z:p+s*x+UP*y+f*z
        self.ell(q(0,0,5),(24,20,55),'blue_terracotta',axes)
        self.ell(q(0,-7,9),(22,13,49),'cyan_terracotta',axes)
        self.ell(q(0,-12,16),(18,8.5,40),'light_blue_terracotta',axes)
        self.stroke([q(0,0,-30),q(0,1,-49),q(0,5,-66)],12,'blue_terracotta',4)
        for sign in (-1,1):
            self.feather([q(sign*4,5,-65),q(sign*20,10,-83),q(sign*43,14,-78)],12,UP,'blue_terracotta','cyan_terracotta','light_blue_terracotta',2)
            self.feather([q(sign*17,-3,14),q(sign*41,-9,-4),q(sign*69,-15,-26)],10,UP,'blue_terracotta','cyan_terracotta','light_blue_terracotta',2)
            self.stroke([q(sign*5,-2,59),q(sign*14,-4,52),q(sign*23,-4,32),q(sign*22,-3,17)],.9,'black_terracotta',.6)
            self.ell(q(sign*22.5,-1,25),(1.3,2.5,3),'black_concrete',axes)
            self.ell(q(sign*23.4,0,26),(.7,.8,1),'sea_lantern',axes)
            # Constellations follow the flank, rather than a row of equally spaced dots.
            stars=[q(sign*23.3,6,12),q(sign*23,9,-1),q(sign*20,10,-18),q(sign*15,10,-31)]
            self.stroke(stars,.6,'light_blue_concrete',.5)
            for j,star in enumerate(stars):self.ell(star,1.2 if j%2 else 1.7,'sea_lantern')
        self.feather([q(0,17,-11),q(0,35,-21),q(0,22,-35)],6,s,'blue_terracotta','cyan_terracotta',None,1.4)
        for x in (-12,-8,-4,0,4,8,12):
            path=[]
            for z in np.linspace(13,51,28):
                v=max(.03,1-(x/22)**2-((z-9)/49)**2)
                path.append(q(x,-7-13*math.sqrt(v)-.3,z))
            self.stroke(path,.62,'cyan_terracotta',.5)
        self.ell(q(0,18.7,20),(3,.8,3.5),'black_terracotta',axes)
        for sign in (-1,1):
            for i in range(3):
                self.stroke([q(sign,20,20),q(sign*(3+i),34+i*4,20),q(sign*(14+i*5),43+i*3,17),q(sign*(23+i*3),36,13)],1.1,'light_blue_stained_glass',.5)
        self.mark('star_whale','星海鲸 · 喉褶与星图',p,107,126,220,span=140)

    def phoenix(self):
        p,f,s=self.frame(1490,150);axes=np.array([s,UP,f]);q=lambda x,y,z:p+s*x+UP*y+f*z
        self.ell(q(0,0,0),(11,12,24),'red_terracotta',axes)
        self.ell(q(0,-4,7),(8,9,17),'orange_terracotta',axes)
        neck=spline([q(0,1,17),q(0,13,28),q(0,26,28),q(0,34,37)])
        self.stroke(neck,6.8,'orange_terracotta',4.8)
        for i in range(4,len(neck)-3,3):
            t=i/(len(neck)-1);at=neck[i]+f*(5.7-1.3*t)
            self.ell(at,(4.6-1.1*t,1.4,.8),'yellow_terracotta',axes)
        for sign in (-1,1):
            for i in range(5):
                self.feather([q(sign*2,28+i*2,25+i*1.5),q(sign*(4+i),31+i*2,18),q(sign*(6+i),26+i*2,9-i*1.4)],
                             2.5,UP,'red_terracotta','orange_terracotta','yellow_terracotta',.85)
        self.ell(q(0,34,38),(5.8,6.4,8),'yellow_terracotta',axes)
        for sign in (-1,1):
            self.ell(q(sign*5,35.8,41),(1,1.9,2.5),'black_terracotta',axes)
            self.ell(q(sign*5.8,35.6,42),(.7,1,1.1),'ochre_froglight',axes)
            self.stroke([q(sign*4,38,43),q(sign*5.5,38.8,39),q(sign*5,38,35)],1.2,'red_terracotta',.6)
        self.stroke([q(0,34,44),q(0,33,50),q(0,30,53),q(0,28,51)],2.6,'gold_block',.55)
        for i in range(-2,3):
            self.feather([q(i*.8,39,35),q(i*2.6,52+abs(i),28),q(i*4.6,51-abs(i)*2,17)],2.3,s if i==0 else UP,
                         'red_terracotta','orange_concrete','yellow_terracotta',.9)
        # Two continuous wing masses carry overlapping secondaries and splayed pointed primaries.
        for sign in (-1,1):
            shoulder=q(sign*8,5,7);elbow=q(sign*34,19,16);wrist=q(sign*66,29,8)
            self.feather([shoulder,elbow,wrist],15,UP,'red_terracotta','orange_terracotta',None,3)
            for i in range(12):
                t=i/11;root=q(sign*(17+49*t),8+22*t,9+6*math.sin(t*math.pi))
                tip=q(sign*(27+73*t),6+48*t,-48+25*t)
                self.feather([root,(root+tip)*.5+UP*(7+5*t),tip],5.8-1.5*t,UP,
                             'red_terracotta','orange_terracotta','yellow_terracotta',1.6)
            for i in range(10):
                t=i/9;root=q(sign*(14+49*t),11+22*t,12)
                tip=q(sign*(25+53*t),12+29*t,-18+8*t)
                self.feather([root,(root+tip)*.5+UP*3,tip],4,UP,
                             'orange_terracotta','yellow_terracotta','gold_block',1.3)
            for i in range(9):
                t=i/8;root=q(sign*(11+50*t),13+20*t,15)
                tip=q(sign*(18+50*t),14+22*t,0)
                self.feather([root,(root+tip)/2+UP*2,tip],2.8,UP,
                             'yellow_terracotta','orange_concrete',None,1)
            # Folded bird feet with three hooked toes, visible from the racing channel.
            self.stroke([q(sign*5,-7,-10),q(sign*7,-17,-15),q(sign*8,-18,-4)],2.0,'yellow_terracotta',1.2)
            for toe in (-1,0,1):self.stroke([q(sign*8,-18,-4),q(sign*8+toe*3,-19,2),q(sign*8+toe*3,-22,3)],1,'gold_block',.5)
        for i in range(-3,4):
            root=q(i*1.8,-1,-19);tip=q(i*12,-8+abs(i)*8,-119+abs(i)*9)
            plume=[root,q(i*7,-20,-58),q(i*16,8,-87),tip]
            self.feather(plume,5.2-abs(i)*.35,UP,
                         'red_terracotta','orange_terracotta','gold_block',1.15)
            # Small gold eye-shaped inlays on the long trailing plumes.
            self.ell(bezier(plume,40)[31]+UP*1.0,(1.4,.65,2.8),'yellow_terracotta',axes)
        for i in range(6):
            self.feather([q(0,10,8-i*4),q(0,16,2-i*4),q(0,12,-8-i*4)],4,UP,'orange_terracotta','yellow_terracotta',None)
        self.mark('phoenix','曜日金凰 · 三重飞羽与流焰长尾',p,139,121,216,span=202)

    def jellyfish(self):
        p,f,s=self.frame(1990,189);axes=np.array([s,UP,f]);q=lambda x,y,z:p+s*x+UP*y+f*z
        glass=self.c.state('light_blue_stained_glass');pink=self.c.state('pink_stained_glass')
        def bell(x,y,z):
            angle=np.arctan2(z,x);rho=np.sqrt(x*x+z*z)/(40+1.2*np.cos(angle*16))
            height=y/(31+1.4*np.cos(angle*8));norm=rho*rho+height*height
            return np.where((y>=0)&(norm<=1)&(norm>=.91**2),np.where(y>21,pink,glass),-1)
        self.volume(p,(42,34,42),axes,bell)
        for j in range(16):
            a=j*math.tau/16;n=s*math.cos(a)+f*math.sin(a)
            pts=[p+n*(40*math.cos(t))+UP*(31*math.sin(t)) for t in np.linspace(0,math.pi/2,35)]
            self.stroke(pts,.8,'white_stained_glass',.55)
        rim=[p+(s*math.cos(a)+f*math.sin(a))*(39+1.6*math.sin(a*16))+UP*(-1.8+2*math.cos(a*16)) for a in np.linspace(0,math.tau,260)]
        self.stroke(rim,1.5,'pink_stained_glass',1.5)
        for j in range(24):
            a=j*math.tau/24;n=s*math.cos(a)+f*math.sin(a);cross=s*math.sin(a)-f*math.cos(a)
            pts=[p+n*(31-13*t+5*math.sin(t*5+a))+cross*(9*math.sin(t*6+a)*t)+UP*(-t*(58+15*(j%4)/3)) for t in np.linspace(0,1,60)]
            self.stroke(pts,.85,'purple_stained_glass' if j%3 else 'pink_stained_glass',.55)
        for j in range(4):
            a=j*math.pi/2;n=s*math.cos(a)+f*math.sin(a)
            self.feather([p+UP*14,p+n*19+UP*6,p+n*7-UP*12],6,UP,'magenta_stained_glass','pink_stained_glass',None,1.5)
            # Twisting oral arms are wide ruffled ribbons, distinct from the fine tentacles.
            for t in np.linspace(0,1,90):
                center=p+n*(7+8*t)+s*(7*math.sin(t*5+a))+f*(5*math.cos(t*6+a))-UP*(5+71*t)
                side=unit(s*math.cos(t*9+a)+f*math.sin(t*9+a))
                width=(4.5-3*t)*(1+.35*math.sin(t*43))
                self.tube([center-side*width,center+side*width],.8,'pink_stained_glass' if j%2 else 'magenta_stained_glass')
        self.ell(p+UP*7,5,'pearlescent_froglight')
        for j in range(8):
            a=j*math.tau/8;v=p+(s*math.cos(a)+f*math.sin(a))*38+UP*3
            self.ell(v,1.2,'sea_lantern')
        self.mark('jellyfish','极光水母 · 透光伞膜与褶带',p,55,106,228,diameter=84)

    def sky_ship(self):
        p,f,s=self.frame(2440,137);axes=np.array([s,UP,f]);q=lambda x,y,z:p+s*x+UP*y+f*z
        wood=self.c.state('dark_oak_planks');trim=self.c.state('waxed_cut_copper');deck=self.c.state('spruce_planks')
        def hull(x,y,z):
            t=np.clip((z+60)/125,0,1);beam=22*np.sin(t*np.pi)**.65
            bottom=-13+9*(np.abs(z)/65)**2;top=6+8*(np.abs(z)/65)**4
            outer=beam*np.sqrt(np.clip((y-bottom)/(top-bottom),0,1))
            inside=(z>-60)&(z<65)&(y>=bottom)&(y<=top)&(np.abs(x)<=outer)
            shell=(np.abs(x)>outer-2)|(y<bottom+2)|(np.abs(y-5)<.75)
            return np.where(inside&shell,np.where(np.abs(y-5)<.75,deck,np.where((np.floor(y)%6)==0,trim,wood)),-1)
        self.volume(p,(24,17,66),axes,hull)
        for sign in (-1,1):
            rail=[]
            for z in np.linspace(-58,63,100):
                w=22*math.sin((z+60)/125*math.pi)**.65;y=8+8*(abs(z)/65)**4
                rail.append(q(sign*w,y,z))
            self.stroke(rail,1.1,'stripped_dark_oak_log',1.1)
            for z in range(-48,57,8):
                w=22*math.sin((z+60)/125*math.pi)**.65;y=8+8*(abs(z)/65)**4
                self.tube([q(sign*w,5,z),q(sign*w,y,z)],.7,'oak_fence')
            for z in range(-35,45,13):self.ell(q(sign*18,-1,z),1.6,'ochre_froglight')
        # Aft cabin, framed glowing windows, stepped quarterdeck and a navigation wheel.
        for z in range(-47,-25):
            self.tube([q(-12,7,z),q(-12,17,z)],1,'stripped_spruce_log')
            self.tube([q(12,7,z),q(12,17,z)],1,'stripped_spruce_log')
            self.tube([q(-13,18,z),q(13,18,z)],1,'dark_oak_planks')
        for sign in (-1,1):
            for z in (-42,-34):self.ell(q(sign*12.5,12,z),(.7,2.5,2),'yellow_stained_glass',axes)
        self.ring(q(0,12,-22),4,f,'stripped_dark_oak_log','gold_block')
        for a in np.linspace(0,math.tau,8,endpoint=False):self.tube([q(0,12,-22),q(5*math.cos(a),12+5*math.sin(a),-22)],.6,'gold_block')
        for index,(z,h,w) in enumerate(((-32,46,23),(0,65,29),(34,51,24))):
            self.tube([q(0,5,z),q(0,h,z)],1.65,'stripped_spruce_log')
            for level in (0,1):
                top=h-5-level*23;bottom=top-20;width=w*(1-.17*level)
                self.tube([q(-width-2,top,z),q(width+2,top,z)],.9,'stripped_dark_oak_log')
                # Convex cloth with a curved hem and longitudinal sewn panels.
                for yy in np.arange(bottom,top,.8):
                    v=(yy-bottom)/(top-bottom)
                    points=[]
                    for x in np.arange(-width,width+.5,.8):
                        hem=bottom+3*(1-(x/width)**2)
                        if yy<hem:continue
                        at=q(x,yy,z+5*math.sin(v*math.pi)*math.cos(x/width*math.pi/2))
                        material='white_wool' if index!=0 else 'light_blue_wool'
                        if abs(x)<1 or abs(x)>width-1.2:material='yellow_terracotta'
                        self.orb(at,.68,material);points.append(at)
                for sign in (-1,1):
                    self.stroke([q(0,h-2,z),q(sign*18,8,z-17)],.6,'brown_terracotta',.6)
                # Flag forks stream with the same wind as the billowed cloth.
            self.feather([q(0,h,z),q(6,h+5,z-9),q(3,h+2,z-24)],3.5,UP,'cyan_wool','light_blue_wool',None,.7)
        self.stroke([q(0,10,60),q(0,15,78),q(0,25,84),q(0,28,81)],3,'waxed_cut_copper',1)
        for sign in (-1,1):
            for z in (-35,0,34):
                root=q(sign*19,4,z);tip=q(sign*58,-14,z-17)
                self.stroke([root,(root+tip)/2,tip],1.3,'stripped_dark_oak_log',1.3)
                self.feather([tip-(tip-root)*.27,tip,tip+(tip-root)*.20],4.3,UP,
                             'waxed_weathered_copper','waxed_oxidized_copper','gold_block',1)
        self.mark('sky_ship','逐星舟 · 三桅鼓帆与铜肋舷楼',p,105,112,214,length=150)

    def puffer_moon(self):
        p,f,s=self.frame(2950,174);axes=np.array([s,UP,f]);q=lambda x,y,z:p+s*x+UP*y+f*z
        gold=self.c.state('yellow_terracotta');cream=self.c.state('smooth_sandstone');orange=self.c.state('orange_terracotta')
        def body(x,y,z):
            inside=(np.abs(x/32)**2.8+np.abs(y/29)**2.8+np.abs(z/34)**2.8)<=1
            freckles=(np.sin(x*.53+z*.23)*np.cos(z*.48-y*.12)>.80)&(y>2)
            return np.where(inside,np.where(y<-7+2*np.cos(x*.13),cream,np.where(freckles,orange,gold)),-1)
        self.volume(p,(33,30,35),axes,body)
        for i in range(112):
            y=1-2*(i+.5)/112;a=i*math.pi*(3-math.sqrt(5));r=math.sqrt(1-y*y)
            n=np.array([r*math.cos(a),y,r*math.sin(a)])
            if n[2]>.72:continue
            distance=1/(abs(n[0]/32)**2.8+abs(n[1]/29)**2.8+abs(n[2]/34)**2.8)**(1/2.8)
            start=q(*(n*(distance-.8)));tip=q(*(n*(distance+6+2*math.sin(i))))
            self.stroke([start,tip],2.0,'orange_terracotta',.5)
        for sign in (-1,1):
            self.ell(q(sign*13,8,31),(5.5,6.5,2.3),'smooth_sandstone',axes)
            self.ell(q(sign*13,8,33),(3.4,4.6,1.5),'black_concrete',axes)
            self.ell(q(sign*12,9.8,34),(1.1,1.5,.7),'sea_lantern',axes)
            self.stroke([q(sign*8,16,31),q(sign*13,17,30),q(sign*18,15,29)],1,'orange_terracotta',.65)
            self.feather([q(sign*29,-4,-2),q(sign*50,4,-10),q(sign*46,-12,-21)],10,UP,
                         'orange_terracotta','yellow_terracotta','smooth_sandstone',1.3)
            self.stroke([q(sign*26,2,19),q(sign*29,-4,13),q(sign*27,-10,16)],.9,'brown_terracotta',.5)
        self.ell(q(0,-7,34),(4.7,3.6,1.5),'orange_terracotta',axes)
        self.ell(q(0,-7,35.3),(2.9,1.7,.8),'black_terracotta',axes)
        self.stroke([q(0,-1,-30),q(0,0,-39)],6,'yellow_terracotta',4)
        for sign in (-1,1):
            self.feather([q(0,0,-36),q(sign*12,0,-46),q(sign*19,0,-53)],8,UP,
                         'orange_terracotta','yellow_terracotta','smooth_sandstone',1.4)
        self.feather([q(0,26,-5),q(0,43,-12),q(0,30,-23)],7,s,'orange_terracotta','yellow_terracotta',None,1.2)
        for i,(x,y,z,r) in enumerate(((-48,20,12,5),(-54,35,7,7),(-47,51,0,4),(46,28,13,4),(53,44,0,6))):
            self.ell(q(x,y,z),r,'light_blue_stained_glass',shell=.78)
            self.ell(q(x-1,y+2,z+1),.85,'sea_lantern')
        self.mark('puffer_moon','河豚月球 · 短棘、鳍纹与气泡',p,68,136,231,diameter=105)

    def great_oar(self):
        p,f,s=self.frame(3390,172);axis=unit(f*.35+UP*.937);face=unit(np.cross(s,axis))
        self.stroke([p-axis*52,p+axis*20],2.6,'stripped_dark_oak_log',2.2)
        self.feather([p+axis*13,p+axis*37,p+axis*64],14,face,
                     'waxed_weathered_copper','waxed_oxidized_copper','gold_block',2.3)
        for d in range(-46,17,7):
            center=p+axis*d
            self.ring(center,2.9,axis,'gold_block','waxed_cut_copper')
        for sign in (-1,1):
            self.stroke([p+axis*23+s*sign*5+face*2,p+axis*37+s*sign*9+face*2,p+axis*53+face*2],.85,'gold_block',.5)
        # Armillary rings have paired rails, an engraved dial and eight distinct compass jewels.
        for radius,normal,material in ((57,f,'waxed_cut_copper'),(44,unit(f+s*.7+UP*.25),'amethyst_block')):
            a,b,_=basis(normal)
            self.ring(p,radius,normal,material,'gold_block');self.ring(p,radius-3,normal,'gold_block','ochre_froglight')
            for i in range(48):
                theta=i*math.tau/48;n=a*math.cos(theta)+b*math.sin(theta)
                self.tube([p+n*(radius-2),p+n*(radius+(.9 if i%6 else 4))],.65,'gold_block')
            for i in range(8):
                theta=i*math.pi/4;n=a*math.cos(theta)+b*math.sin(theta);cross=unit(np.cross(normal,n))
                gem=p+n*(radius+2)
                self.ell(gem,(3.2,1.8,5.4),'gold_block',np.array([cross,normal,n]))
                self.ell(gem+normal*1.5,(1.8,1.2,3.5),'sea_lantern' if i%2 else 'amethyst_block',np.array([cross,normal,n]))
        self.ring(p,9,f,'gold_block','sea_lantern')
        for sign in (-1,1):
            for up in (-1,1):
                points=[p+s*sign*9,p+s*sign*22+UP*up*12,p+s*sign*26+UP*up*27,p+s*sign*15+UP*up*38]
                self.stroke(points,1,'waxed_cut_copper',.6)
        self.mark('enchanted_oar','潮汐星仪 · 双轨刻度与镶金巨桨',p,75,104,247,length=116,ring_diameter=126)

    def build(self):
        for method in (self.twin_dragons,self.whale,self.phoenix,self.jellyfish,self.sky_ship,self.puffer_moon,self.great_oar):
            method();print('Sculpted',self.landmarks[-1]['kind'],flush=True)
            if method!=self.twin_dragons:
                p=np.asarray(self.landmarks[-1]['center'])
                if method==self.jellyfish:p=p+UP*7
                if method==self.sky_ship:p=p+UP*5
                self.checks.append([*np.rint(p).astype(int),'solid'])
        self.corals();self.village()
        return dict(landmarks=self.landmarks,effects=self.effects,checks=self.checks,
                    minimum_sky_decoration_y=100,static_blocks=True,sculpture_revision=9)


def build(mountain):
    return json.loads(json.dumps(Sculptures(mountain).build(),default=lambda value:value.item()))
