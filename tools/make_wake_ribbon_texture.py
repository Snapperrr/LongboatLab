"""Periodic water striations and broken crest foam, generated without external artwork."""
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
    angle = u * math.tau
    streak = (.5 + .5 * math.sin(angle * 7 + v * 7 + math.sin(angle * 3))) ** 8
    rim = math.exp(-((v - .73 - .03 * math.sin(angle * 3)) / .12) ** 2)
    # Keep the body translucent and the foot/outer edge soft; highlight only the moving crest.
    edge = min(1, v * 12, (1 - v) * 9)
    micro = (.5+.5*math.sin(angle*19+v*23+math.sin(angle*5)))**12
    return 210 + streak * 28, 234 + streak * 16, 251, (62 + 28 * streak + 48 * rim + 30*micro) * edge


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
    angle = u * math.tau
    center = .5 + .12 * math.sin(angle * 3) + .04 * math.sin(angle * 7)
    band = math.exp(-((v - center) / .21) ** 2)
    bubbles,grain,clusters=cellular(u,v)
    patches=max(0,min(1,(clusters-.23)*2.4))
    return 241+grain*14,247+grain*8,255,255*band*patches*(.18+.82*bubbles)


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


save('ribbon', ribbon)
save('froth', froth)
save('wake_lace',wake_lace)
save('impact_sheet', impact_sheet)
