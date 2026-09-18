"""Feathered gas and water-vapor masks, consumed by the low-alpha soft-spray shader."""
from pathlib import Path
import math
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/longboatlab/textures'

def noise(x,y):
    def value(a,b):
        n=(a*374761393+b*668265263+98317)&0xffffffff
        n=((n^(n>>13))*1274126177)&0xffffffff
        return ((n^(n>>16))&0xffff)/65535
    a,b=math.floor(x),math.floor(y);u,v=x-a,y-b
    u=u*u*(3-2*u);v=v*v*(3-2*v)
    return (value(a,b)*(1-u)+value(a+1,b)*u)*(1-v)+(value(a,b+1)*(1-u)+value(a+1,b+1)*u)*v


def mask(path,vapor=False):
    size=128;texture=Image.new('RGBA',(size,size),(236,245,249,0))
    for y in range(size):
        for x in range(size):
            u=(x+.5)/size*2-1;v=(y+.5)/size*2-1
            # Aperiodic smooth density avoids regular checker/pearl patterns when many
            # sprites overlap. A broad soft envelope removes the circular cutout edge.
            envelope=max(0,1-u*u-v*v)**2.4
            density=.24+.54*noise(u*2.8+7,v*3.1+11)+.22*noise(u*7.3+19,v*5.7+3)
            bend=.18*(noise(u*2.1+5,8)-.5)+.09*math.sin(u*3.2+.7)
            streak=sum(weight*math.exp(-((v-center-bend)/width)**2)
                       for center,width,weight in ((-.33,.19,.65),(.05,.24,.9),(.39,.14,.48)))
            density*=.80+.20*noise(u*4+21,v*4+16) if vapor else .38+.62*min(1,streak)
            alpha=round((186 if vapor else 215)*envelope*density)
            texture.putpixel((x,y),((248,251,255) if vapor else (224,241,248))+(alpha,))
    texture.save(path)

mask(ROOT/'particle/jet.png')
mask(ROOT/'water/vapor.png',True)
