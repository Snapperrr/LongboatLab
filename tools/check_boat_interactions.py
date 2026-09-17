"""Offline geometric regression cases for boat picking and seat changes; no Java compilation.

The geometry calculations check pose invariants and reproduce the old yaw-only envelope miss.
They do not execute Minecraft's network handlers, damage, menus or passenger synchronization.
"""
from pathlib import Path
from itertools import product
import json
import math
import sys
import numpy as np

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/xc/longboatlab'
sys.path.insert(0,str(ROOT/'tools/.audio-deps'))
from tree_sitter import Language,Parser
import tree_sitter_java
parser=Parser(Language(tree_sitter_java.language()))
names=('BoatGeometry.java','BoatSeats.java','BoatBoardPayload.java','LongboatLab.java',
       'client/LongboatClient.java','mixin/client/MinecraftClientMixin.java')
for name in names:assert not parser.parse((JAVA/name).read_bytes()).root_node.has_error,name


def rotation(yaw,pitch,roll):
    cy,sy=math.cos(yaw),math.sin(yaw)
    cp,sp=math.cos(pitch),math.sin(pitch)
    cr,sr=math.cos(roll),math.sin(roll)
    return np.array([[cy,0,-sy],[0,1,0],[sy,0,cy]]) @ np.array([[cr,-sr,0],[sr,cr,0],[0,0,1]]) @ np.array([[1,0,0],[0,cp,sp],[0,-sp,cp]])


def ray_box(start,end,lo,hi):
    enter,leave=0.,1.
    for axis in range(3):
        step=end[axis]-start[axis]
        if abs(step)<1e-10:
            if start[axis]<lo[axis] or start[axis]>hi[axis]:return None
        else:
            near,far=sorted(((lo[axis]-start[axis])/step,(hi[axis]-start[axis])/step))
            enter=max(enter,near);leave=min(leave,far)
            if enter>leave:return None
    return start+(end-start)*enter


def shell(width,length,height):
    return [((-width,0,-length),(width,.12,length)),
            ((-width,0,-length),(-width+.15,height,length)),
            ((width-.15,0,-length),(width,height,length)),
            ((-width,0,-length),(width,height,-length+.15)),
            ((-width,0,length-.15),(width,height,length))]


poses=0;old_envelope_misses=0;rays=0
for length,width,stack,yaw,pitch,roll in product((1,2,16,128),(1,3,8),(0,.35),
                                               (0,37,90,175),(0,.4,math.pi/2,math.pi),(0,math.pi/4,math.pi)):
    w=width*11/16;h=9/16+stack;center=np.array([0,h/2,0]);matrix=rotation(math.radians(yaw),pitch,roll)
    corners=np.array(list(product((-w,w),(0,h),(-length,length))))
    world=(corners-center)@matrix.T+center
    extent=np.abs(matrix)@np.array([w,h/2,length])
    assert np.all(world>=center-extent-1e-9) and np.all(world<=center+extent+1e-9)
    assert np.allclose((world-center)@matrix+center,corners)
    y=math.radians(yaw)
    old_size=np.array([(w-.0875)*abs(math.cos(y))+(length-.1)*abs(math.sin(y)),h/2,
                       (w-.0875)*abs(math.sin(y))+(length-.1)*abs(math.cos(y))])
    # Near a tilted bow or stern, vanilla's old Y envelope fails the server's reach check.
    local=np.array([0,.28,length-.2]);tip=(local-center)@matrix.T+center
    if np.any(np.abs(tip-center)>old_size+1e-8):old_envelope_misses+=1
    # Reachable side, bow and floor rays must hit the same local point in every orientation.
    for start,end,expected in (([w+1.2,.28,length-.2],[w-.2,.28,length-.2],[w,.28,length-.2]),
                               ([0,.28,length+1.2],[0,.28,length-.2],[0,.28,length]),
                               ([0,.4,0],[0,-.1,0],[0,.12,0])):
        start=np.array(start);end=np.array(end)
        a=((start-center)@matrix.T)@matrix+center
        b=((end-center)@matrix.T)@matrix+center
        hits=[hit for lo,hi in shell(w,length,h) if (hit:=ray_box(a,b,lo,hi)) is not None]
        hit=min(hits,key=lambda p:np.sum((p-a)**2))
        assert np.allclose(hit,expected), (length,width,yaw,pitch,roll,hit,expected)
        rays+=1
    poses+=1
assert old_envelope_misses>0

# A vertical ray through a rotated discovery-box corner must miss the actual hull.
matrix=rotation(math.pi/4,0,0);center=np.array([0,9/32,0])
start=np.array([9,2,9]);end=np.array([9,-1,9])
a=(start-center)@matrix+center;b=(end-center)@matrix+center
assert all(ray_box(a,b,lo,hi) is None for lo,hi in shell(11/16,16,9/16))

client=(JAVA/'client/LongboatClient.java').read_text(encoding='utf-8')
server=(JAVA/'LongboatLab.java').read_text(encoding='utf-8')
seats=(JAVA/'BoatSeats.java').read_text(encoding='utf-8')
geometry=(JAVA/'BoatGeometry.java').read_text(encoding='utf-8')
assert client.index('useMountedBlock(client)')<client.index('BoatGeometry.raycastHull')
assert 'if(player.getVehicle()==target.boat())return;' not in server
assert 'hit.boat() == client.player.getVehicle()' not in client
assert 'BoatGeometry.canUseHull(player,boat,payload.localHit())' in server
assert 'client.interactionManager.attackEntity(client.player,hit.boat())' in client
assert 'client.attackCooldown>0' in client
assert 'Double.isFinite(local.x)' in geometry and 'unobstructedDistance(player,start,end)' in geometry
branch=seats[seats.index('if (changingSeat)'):seats.index('boat.interact(player,hand)')]
assert 'updatePassengerPosition(player)' in branch and 'return' in branch
assert 'stopRiding(' not in branch and 'startRiding(' not in branch
assert 'clearPhantomHullTarget(client,delta)' in client
for lang in ('zh_cn','en_us'):
    data=json.loads((ROOT/f'src/main/resources/assets/longboatlab/lang/{lang}.json').read_text(encoding='utf-8'))
    assert data['message.longboatlab.seat_full'] and data['message.longboatlab.board_unavailable']

report={'version':'0.10.15','java_files_parsed':len(names),'pose_cases':poses,'shell_ray_cases':rays,
        'old_envelope_misses_reproduced':old_envelope_misses,'empty_corner_miss_checked':True,
        'mounted_block_priority_guard_checked':True,'seat_change_without_remount_guard_checked':True,
        'finite_hit_reach_occlusion_source_guards_checked':True,'vanilla_damage_path_guard_checked':True,
        'java_compiled':False,'minecraft_launched':False,'network_and_multiplayer_tested':False}
(ROOT/'docs/boat-interactions-0.10.15.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
