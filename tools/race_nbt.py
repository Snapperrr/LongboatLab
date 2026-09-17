"""Small big-endian NBT/Anvil writer for the bundled 1.21.1 race save; no game launch required."""
import struct
import gzip
import zlib
from pathlib import Path

def byte(v): return (1, v)
def short(v): return (2, v)
def integer(v): return (3, v)
def long(v): return (4, v)
def float_(v): return (5, v)
def double(v): return (6, v)
def bytes_(v): return (7, bytes(v))
def string(v): return (8, str(v))
def list_(kind, values): return (9, (kind, list(values)))
def compound(v): return (10, v)
def ints(v): return (11, list(v))
def longs(v): return (12, list(v))

def text(s):
    # All generated strings are BMP; Minecraft NBT uses Java modified UTF-8.
    raw = s.encode('utf-8').replace(b'\0', b'\xc0\x80')
    return struct.pack('>H', len(raw)) + raw

def payload(kind, value):
    if kind in (1,2,3,4,5,6): return struct.pack({1:'>b',2:'>h',3:'>i',4:'>q',5:'>f',6:'>d'}[kind], value)
    if kind == 7: return struct.pack('>i',len(value))+value
    if kind == 8: return text(value)
    if kind == 9:
        child, values = value
        return bytes([child])+struct.pack('>i',len(values))+b''.join(payload(child,v) for v in values)
    if kind == 10:
        return b''.join(bytes([v[0]])+text(k)+payload(*v) for k,v in value.items())+b'\0'
    if kind in (11,12):
        fmt='>I' if kind==11 else '>Q';mask=(1<<(32 if kind==11 else 64))-1
        return struct.pack('>i',len(value))+b''.join(struct.pack(fmt,v&mask) for v in value)
    raise ValueError(kind)

def encode(root): return b'\x0a\x00\x00'+payload(10,root)
def save(path, root): Path(path).write_bytes(gzip.compress(encode(root),mtime=0))

class Reader:
    def __init__(self, raw): self.raw=raw;self.pos=0
    def take(self,n):
        value=self.raw[self.pos:self.pos+n];self.pos+=n
        if len(value)!=n:raise ValueError('Truncated NBT')
        return value
    def number(self,fmt): return struct.unpack(fmt,self.take(struct.calcsize(fmt)))[0]
    def text(self): return self.take(self.number('>H')).replace(b'\xc0\x80',b'\0').decode('utf-8','surrogatepass')
    def read(self,kind):
        if kind in (1,2,3,4,5,6):return self.number({1:'>b',2:'>h',3:'>i',4:'>q',5:'>f',6:'>d'}[kind])
        if kind==7:return self.take(self.number('>i'))
        if kind==8:return self.text()
        if kind==9:
            child=self.number('>B');n=self.number('>i');return (child,[self.read(child) for _ in range(n)])
        if kind==10:
            result={}
            while True:
                child=self.number('>B')
                if child==0:return result
                name=self.text();result[name]=(child,self.read(child))
        if kind in (11,12):return [self.number('>i' if kind==11 else '>q') for _ in range(self.number('>i'))]
        raise ValueError(kind)

def decode(raw):
    r=Reader(raw);kind=r.number('>B');r.text();return r.read(kind)

def pack_indices(values,bits):
    per=64//bits;result=[]
    for i in range(0,len(values),per):
        word=0
        for j,v in enumerate(values[i:i+per]):word|=int(v)<<(j*bits)
        result.append(word)
    return result

def write_region(path,chunks):
    header=bytearray(8192);body=bytearray();sector=2
    for (cx,cz),root in sorted(chunks.items()):
        raw=zlib.compress(encode(root),6)
        record=struct.pack('>I',len(raw)+1)+b'\x02'+raw
        count=(len(record)+4095)//4096
        if count>255:raise ValueError('Chunk exceeds Anvil sector limit')
        i=(cx&31)+(cz&31)*32
        header[i*4:i*4+4]=struct.pack('>I',(sector<<8)|count)
        body+=record+b'\0'*(count*4096-len(record));sector+=count
    Path(path).write_bytes(header+body)
