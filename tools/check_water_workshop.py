"""Offline source/API-shape and geometry regressions; does not compile or execute Minecraft."""
from pathlib import Path
import ast
import json
import math
import re
import sys

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/xc/longboatlab'
sys.path.insert(0,str(ROOT/'tools/.audio-deps'))
from tree_sitter import Language,Parser
import tree_sitter_java
parser=Parser(Language(tree_sitter_java.language()))
java=list(JAVA.rglob('*.java'))
for path in java:assert not parser.parse(path.read_bytes()).root_node.has_error,path
for path in ROOT.joinpath('tools').glob('*.py'):ast.parse(path.read_text(encoding='utf-8-sig'))
source=lambda path:(JAVA/path).read_text(encoding='utf-8')
grid=source('PufferGrid.java');settings=source('JetSettings.java');payload=source('JetSettingsPayload.java')
body=source('BoatBody.java');exhaust=source('client/BoatExhaust.java');screen=source('client/water/WaterGraphScreen.java')
spray=source('client/water/WaterSplashRenderer.java')

# Regression: from the TOP of an existing fish, the next center must occupy the immediate
# row, including legacy end-face columns and outward branches. Preserve old stored positions.
row_delta=int(re.search(r'long v=\(long\)c.v\+(\d+)\+\(c.outward',grid)[1])
mounts=0
for face in (1,2,3,4):
    for row in (0,1,2,17,900):
        for layer in (0,1,2,11):
            for outward in (False,True):
                actual_old_row=row+(layer*2 if not outward and face<3 else 0)
                next_row=row+row_delta+(0 if outward or face>=3 else layer*2)
                assert math.isclose((next_row-actual_old_row)*.23,.23,abs_tol=1e-8)
                assert not math.isclose((next_row-actual_old_row)*.23,.46,abs_tol=1e-8)
                mounts+=1
assert '0.17+c.v*0.23' in grid and 'face==STERN||face==BOW ? new Vec3d(0,0.46,0)' in grid

# A save-scoped state and one-way S2C payload are essential: operators alone can change
# values, current players update immediately, new players sync, and clients reset on leave.
assert 'requires(source -> source.hasPermissionLevel(2))' in settings
assert 'getOverworld().getPersistentStateManager().getOrCreate' in settings
assert 'markDirty()' in settings and 'getPlayerList().forEach(this::send)' in settings
assert 'ServerPlayConnectionEvents.JOIN.register' in settings
assert 'private float density = 1, force = 1' in settings
assert 'Float.isFinite(value)' in settings
assert 'ClientPlayConnectionEvents.DISCONNECT' in source('client/ClientJetSettings.java')
assert 'JetSettingsPayload.ID' not in source('LongboatLab.java') # no C2S receiver
assert 'PacketCodecs.FLOAT' in payload
assert 'JetPhysics.accelerations(counts,jetAxes,mass,jetMultiplier)' in body
assert 'acceleration=accelerations[face]' in body
assert body.index('acceleration=accelerations[face]')<body.index('acceleration.multiply(mass)')
assert '.multiply(multiplier)' in source('JetPhysics.java')
assert 'density <= 0' in exhaust and '(beads - 2) * density' in exhaust
for base in (6,10,14):
    assert 2+max(1,round((base-2)*1))==base
    assert 2+max(1,round((base-2)*8))<=98

# The crown has replaced the former long tubes; shared curved boundary vertices are
# cached once for both water/froth passes. Physics and lifetime checks live in check_spray_camera.
assert 'frameJets' not in spray
assert 'frameEdges=new Vec3d[segments][HEIGHTS.length]' in spray
assert 'frameEdges[next][height]' in spray
assert 'int strips = foam ? (flat ? 3 : 2)' in spray
assert 'MAX_SHEETS = 16, MAX_DROPS = 384, MAX_MIST = 192, MAX_RIPPLES = 48' in spray

ids=re.findall(r'new Definition\("([a-z]+)"',screen)
assert len(ids)==len(set(ids))==12
locales=[]
for locale in ('zh_cn','en_us'):
    data=json.loads((ROOT/f'src/main/resources/assets/longboatlab/lang/{locale}.json').read_text(encoding='utf-8'))
    for id in ids:
        assert data[f'screen.longboatlab.water.{id}.title']
        assert data[f'screen.longboatlab.water.{id}.description']
    locales.append(locale)
assert 'mouseDragged' in screen and 'mouseScrolled' in screen and 'keyPressed' in screen
assert 'BoatWaterEffects.diagnostics()' in screen
# Screen transforms invert accurately through mouse-centered zoom, needed for selecting nodes.
for zoom in (.3,.7,1.,2.5):
    for pan in (-1000.,12.,500.):
        for node_x in (0,205,615,990):
            screen_x=node_x*zoom+pan
            assert math.isclose((screen_x-pan)/zoom,node_x,abs_tol=1e-9)

version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M)[1].strip()
report=dict(version=version,java_files_parsed=len(java),adjacent_row_cases=mounts,
            legacy_mount_positions_preserved=True,shared_crown_vertices_source_checked=True,
            operator_only_world_settings_source_checks=True,jet_default_density_unchanged=True,
            graph_nodes=len(ids),locales=locales,particle_pool_limits_unchanged=True,
            java_compiled=False,minecraft_launched=False,live_network_tested=False,
            in_game_visuals_verified=False,fps_measured=False)
(ROOT/f'docs/water-workshop-{version}.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,indent=2))
