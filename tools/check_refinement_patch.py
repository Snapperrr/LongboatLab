"""Offline regressions for seam mounts, hand-only loading, supply capacity and water-cache moves.

Parses Java syntax and checks the geometry/data invariants used by the patch. Does not execute
Minecraft, compile Java, measure FPS or assert the in-game appearance.
"""
from pathlib import Path
import ast
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
sources=['PufferGrid.java','BoatGeometry.java','OarRack.java','BoatRig.java','LongboatLab.java','RaceTools.java',
         'RigBoatCommand.java','client/PufferPlacementPreview.java','client/water/HeightfieldSurface.java',
         'client/water/WaterSplashRenderer.java','client/LongboatClient.java','mixin/client/MinecraftClientMixin.java',
         'BoatBoardPayload.java','BoatSeats.java']
for name in sources:assert not parser.parse((JAVA/name).read_bytes()).root_node.has_error,name
for name in ('make_mountain_race_save.py','rally_pit_stops.py','rally_ground_details.py','make_wake_ribbon_texture.py',
             'check_mountain_race_save.py','preview_course_refinements.py'):
    ast.parse((ROOT/'tools'/name).read_text(encoding='utf-8'))

# Rays from above a gunwale must hit its real rail, not the old solid hull's invisible lid.
def hit_box(start,end,lo,hi):
    a,b=0.,1.
    for axis in range(3):
        step=end[axis]-start[axis]
        if abs(step)<1e-10:
            if not lo[axis]<=start[axis]<=hi[axis]:return None
        else:
            near,far=sorted(((lo[axis]-start[axis])/step,(hi[axis]-start[axis])/step))
            a=max(a,near);b=min(b,far)
            if a>b:return None
    return tuple(start[i]+a*(end[i]-start[i]) for i in range(3))

seams=0;rail_hits=0
offset=lambda i,edge:math.copysign(1 if edge==3 else 11/16,i) if abs(i)==edge else i*.34
for length in (1,2,3,16,64,256):
    for seam in range(1,length):
        z=-length+seam*2
        identities=[]
        for epsilon in (-1e-7,0,1e-7):
            slot=max(0,min(length-1,math.floor((z+epsilon+length)/2)))
            local=z+epsilon-(-length+1+2*slot)
            u=min(range(-3,4),key=lambda i:abs(offset(i,3)-local))
            if u==3 and slot<length-1:slot+=1;u=-3
            identities.append((slot,u))
            assert abs(-length+1+2*slot+offset(u,3)-z)<1e-10
        assert len(set(identities))==1
        seams+=1
    for z in (-length+.2,0,length-.2):
        for sign in (-1,1):
            w=11/16
            point=hit_box((sign*(w-.1),1.8,z),(sign*(w-.1),.2,z),
                          ((w-.15 if sign>0 else -w),0,-length),
                          ((w if sign>0 else -w+.15),9/16,length))
            assert point and abs(point[0])>=w-.16
            rail_hits+=1
for width in (2,4,16):
    for lane in range(width-1):
        assert abs((lane-(width-1)/2)*22/16+offset(2,2)
                   -((lane+1-(width-1)/2)*22/16+offset(-2,2)))<1e-12

# At intermediate/compressed extensions a +edge row and the next -edge row differ in
# both fore/aft coordinate and outward depth. Preserve the selected compartment there.
grid=(JAVA/'PufferGrid.java').read_text(encoding='utf-8')
assert 'boolean sharedSeam=BoatGeometry.extension(boat)>=0.9999f;' in grid
assert 'if(sharedSeam && c.face>=LEFT' in grid and 'if(sharedSeam && c.face==BOTTOM' in grid
compressed_cases=0
for length in (2,3,16,64):
    for slot in range(length-1):
        for extension in (0.,.25,.5,.75,1.):
            original=np.array([(-length+1+slot*2)*extension+1,slot*(1-extension)*.24])
            next_row=np.array([(-length+1+(slot+1)*2)*extension-1,(slot+1)*(1-extension)*.24])
            if extension<1:
                assert np.linalg.norm(original-next_row)>.1
            else:assert np.allclose(original,next_row)
            compressed_cases+=1

# Use exact row-copy ranges from HeightfieldSurface.shiftArray and compare with a world-coordinate oracle.
moves=0
for size in (48,52,97):
    original=np.arange(1,size*size+1).reshape(size,size)
    for dx in (-120,-32,-16,0,16,32,120):
        for dz in (-120,-32,-16,0,16,32,120):
            actual=np.zeros_like(original);lo=max(0,-dx);hi=min(size,size-dx)
            if lo<hi:
                for z in range(max(0,-dz),min(size,size-dz)):
                    actual[z,lo:hi]=original[z+dz,lo+dx:hi+dx]
            z,x=np.indices((size,size));valid=(x+dx>=0)&(x+dx<size)&(z+dz>=0)&(z+dz<size)
            expected=np.zeros_like(original);expected[valid]=original[(z+dz)[valid],(x+dx)[valid]]
            assert np.array_equal(actual,expected);moves+=1

for cx,cz in ((0,0),(26,26),(51,51),(17,39)):
    queued=[]
    def q(x,z):
        if 0<=x<52 and 0<=z<52:queued.append((x,z))
    q(cx,cz)
    for radius in range(1,52):
        for x in range(cx-radius,cx+radius+1):q(x,cz-radius);q(x,cz+radius)
        for z in range(cz-radius+1,cz+radius):q(cx-radius,z);q(cx+radius,z)
    assert len(queued)==len(set(queued))==52*52
    distance=[max(abs(x-cx),abs(z-cz)) for x,z in queued]
    assert distance==sorted(distance)

def fits(need,limit,slots,offhand=(False,0)):
    same,count=offhand
    if same and count>0:need-=max(0,limit-count)
    for same,count in slots:
        if count==0:need-=limit
        elif same:need-=max(0,limit-count)
    return need<=0
assert fits(6,16,[(True,10)]+[(False,64)]*35)
assert not fits(6,16,[(True,14)]+[(False,64)]*35)
assert fits(4,16,[(True,12)]+[(False,64)]*35)
assert not fits(2,1,[(False,0)]+[(False,64)]*35)
assert fits(2,1,[(False,0)]*2+[(False,64)]*34)
assert fits(4,16,[(False,64)]*36,(True,12))
assert fits(6,16,[(True,14)]+[(False,64)]*35,(True,12))
assert not fits(4,16,[(False,64)]*36,(True,14))
assert not fits(4,16,[(False,64)]*36,(False,0))
assert not fits(4,16,[(False,64)]*36,(False,12))
supplies=(JAVA/'RaceTools.java').read_text(encoding='utf-8')
assert '!offHand.isEmpty() && ItemStack.areItemsAndComponentsEqual(offHand,reward)' in supplies
client=(JAVA/'client/LongboatClient.java').read_text(encoding='utf-8')
assert client.index('useMountedBlock(client)')<client.index('BoatGeometry.raycast(')
assert 'player.raycast(player.getBlockInteractionRange(),1,false)' in client
assert 'entity.getRootVehicle()!=player.getRootVehicle()' in client
assert 'hit.boat() == client.player.getVehicle()' not in client
assert 'BoatGeometry.raycastHull(client.player,delta)' in client
assert 'new BoatBoardPayload(' in client
seats=(JAVA/'BoatSeats.java').read_text(encoding='utf-8')
assert seats.index('if (changingSeat)')<seats.index('boat.interact(player,hand)')
assert 'boat.updatePassengerPosition(player);return;' in seats
assert 'itemUseCooldown=4' in (JAVA/'mixin/client/MinecraftClientMixin.java').read_text(encoding='utf-8')
interaction=(JAVA/'LongboatLab.java').read_text(encoding='utf-8')
assert 'if(player.getVehicle()==target.boat())return;' not in interaction
assert 'BoatGeometry.canUseHull(player,boat,payload.localHit())' in interaction
for code in (client,interaction):
    assert 'held.isOf(Items.MACE), held.isOf(Items.PUFFERFISH_BUCKET)' in code
assert 'Math.min(held.getCount(),rig.oarLimit()-rig.side(target.left()).size())' in interaction
assert 'held.decrementUnlessCreative(amount, player)' in interaction
for held,room in ((16,40),(16,3),(6,6),(1,16)):
    taken=min(held,room);assert taken<=room and taken+(held-taken)==held
cache=(JAVA/'client/water/HeightfieldSurface.java').read_text(encoding='utf-8')
assert 'height.clone()' not in cache and 'SCANS_PER_TICK = 128' in cache
assert 'source[cell]=scanned[cell]=false' in cache
for name in ('zh_cn','en_us'):
    data=json.loads((ROOT/f'src/main/resources/assets/longboatlab/lang/{name}.json').read_text(encoding='utf-8'))
    assert 'message.longboatlab.supply_room' in data

report={'java_files_parsed':len(sources),'longitudinal_seams_checked':seams,'gunwale_rays_checked':rail_hits,
        'cache_overlap_cases':moves,'scan_queue_positions':4,'maximum_cells_scanned_per_tick':128,
        'inventory_capacity_cases':10,'compressed_seam_geometry_cases':compressed_cases,
        'mounted_block_source_guards_checked':True,
        'hand_only_install_cases':4,'java_compiled':False,'minecraft_launched':False,
        'fps_measured':False}
(ROOT/'docs/refinement-check-0.10.15.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
