"""Continuous water tint and separated spilling-crest foam islands.

U is distance travelled along the wake, V crosses its shoulder. All assets wrap in U.
"""
from pathlib import Path
import math
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/longboatlab/textures/water'


def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data))


def save(name, painter):
    size = 128
    rows = bytearray()
    for y in range(size):
        rows.append(0)
        for x in range(size):
            values = painter((x + .5) / size, (y + .5) / size)
            rows.extend(bytes(round(max(0, min(255, value))) for value in values))
    (ROOT / (name + '.png')).write_bytes(b'\x89PNG\r\n\x1a\n'
        + chunk(b'IHDR', struct.pack('>IIBBBBB', size, size, 8, 6, 0, 0, 0))
        + chunk(b'IDAT', zlib.compress(rows)) + chunk(b'IEND', b''))


def ribbon(u, v):
    # Colour is multiplied by the local water tint. No continuous white highlight is
    # baked into the water layer; the separate aerated islands supply the white detail.
    grain = noise(u*48, v*26, 48)
    ripples = noise(u*12, v*7, 12)
    ridge = math.exp(-((v-.64)/.17)**2)
    edge = smooth(v/.18) * smooth((1-v)/.22)
    return 216+grain*23, 237+grain*13, 249+grain*6, (58+44*ridge+24*ripples)*edge


def smooth(t):
    t=max(0,min(1,t))
    return t*t*(3-2*t)


def noise(x, y, period=16):
    def h(a,b):
        n=((a%period)*374761393+b*668265263)&0xffffffff
        n=((n^(n>>13))*1274126177)&0xffffffff
        return ((n^(n>>16))&0xffff)/65535
    ix,iy=math.floor(x),math.floor(y);fx=x-ix;fy=y-iy
    fx=fx*fx*(3-2*fx);fy=fy*fy*(3-2*fy)
    return (h(ix,iy)*(1-fx)+h(ix+1,iy)*fx)*(1-fy)+(h(ix,iy+1)*(1-fx)+h(ix+1,iy+1)*fx)*fy


def cellular(u,v):
    # Periodic irregular cells: thin bubble walls with open transparent interiors.
    x=u*16;y=v*10;ix=math.floor(x);iy=math.floor(y);nearest=[100.,100.]
    for a in range(ix-1,ix+2):
        for b in range(iy-1,iy+2):
            cx=a+.15+.7*noise(a,b)
            cy=b+.15+.7*noise(a+5,b+13)
            d=(x-cx)**2+(y-cy)**2
            if d<nearest[0]:nearest=[d,nearest[0]]
            elif d<nearest[1]:nearest[1]=d
    rim=math.exp(-((math.sqrt(nearest[1])-math.sqrt(nearest[0]))/.085)**2)
    grain=noise(u*64,v*48,64)
    clusters=noise(u*8,v*5,8)
    return rim,grain,clusters


def froth(u, v):
    # Unequal whitecaps have completely clear water between them. Smooth multiscale
    # density forms ragged islands; bubble walls sit inside them, never across the gaps.
    warp=(noise(u*8,v*4,8)-.5)*.28
    field=.67*noise(u*8,(v+warp)*5,8)+.33*noise(u*24,v*13,24)
    islands=smooth((field-.44)/.19)
    center=.49+.13*math.sin(u*math.tau*3)+.06*math.sin(u*math.tau*7)
    band=smooth((.43-abs(v-center))/.18)
    bubbles,grain,_=cellular(u,v)
    aerated=smooth((grain-.34)/.34)
    body=min(1,.47+.32*aerated+.38*bubbles)
    edge=smooth(v/.16)*smooth((1-v)/.16)
    return 247+grain*8,251+grain*4,255,250*islands*band*body*edge


def wake_lace(u,v):
    bubbles,grain,clusters=cellular(u,v)
    edge=math.sin(math.pi*v)**1.4
    patches=max(0,min(1,(clusters-.31)*3.2))
    flecks=max(0,(grain-.69)*3.2)
    return 236+grain*19,245+grain*10,255,215*edge*patches*min(1,bubbles*.85+flecks)


def impact_sheet(u, v):
    angle = u * math.tau
    fingers = (.5 + .5 * math.sin(angle * 4 + v * 5)) ** 4
    thin = .5 + .5 * math.sin(angle * 11 - v * 7)
    # Thin ejecta lamella near the top separates into translucent ligaments.
    rim = max(0, 1 - v * 2)
    breakup = 1 - rim * (.25 + .55 * thin)
    foot = min(1, (1 - v) * 10)
    return 219 + fingers * 25, 239 + fingers * 13, 255, (135 + fingers * 90) * breakup * foot


if __name__ == '__main__':
    save('ribbon', ribbon)
    save('froth', froth)
    save('wake_lace',wake_lace)
    save('impact_sheet', impact_sheet)
