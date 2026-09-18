"""Offline numerical invariants and integration guards for 0.10.18.

These models do not execute Java, OpenGL or mixins. They check recoil/gravity constraints,
spray lifetimes, asset contracts and graph sockets, not in-game appearance or performance.
"""
from pathlib import Path
import json
import math
import re
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/xc/longboatlab'
ASSETS = ROOT / 'src/main/resources/assets/longboatlab'
read = lambda name: (JAVA / name).read_text(encoding='utf-8')
body = read('BoatBody.java')
physics = read('JetPhysics.java')
spray = read('client/water/WaterSplashRenderer.java')


def recoil(counts, axes, mass, multiplier=1):
    """Reference model of the deliberately anisotropic gameplay thrust law."""
    old = np.zeros((5, 3))
    for face, count in enumerate(counts):
        if count <= 0:
            continue
        root = math.sqrt(count)
        horizontal = min(.075, .018*root/math.sqrt(mass)) if face == 0 else min(.45, (.075+.035*root)/math.sqrt(mass))
        old[face] = axes[face] * (horizontal, min(.075, .018*root/mass), horizontal)
    new = old.copy()
    for sign in (-1, 1):
        chosen = sign*old[:, 1] > 0
        amount = float(np.sum(sign*old[chosen, 1]))
        projected = float(np.sum(counts[chosen]*axes[chosen, 1]**2))
        if amount:
            new[chosen, 1] *= min(1, min(.075, .018*math.sqrt(projected)/mass)/amount)
    return old*multiplier, new*multiplier


assert 'counts[f]*axis.y*axis.y' in physics
assert 'JetPhysics.accelerations(counts,jetAxes,mass,jetMultiplier)' in body
assert 'acceleration.multiply(mass)' in body
assert '- GRAVITY + submerged*0.075' in body
assert 'GRAVITY = 0.04' in body
rng = np.random.default_rng(1018)
normals = np.array([(0, -1, 0), (0, 0, -1), (0, 0, 1), (1, 0, 0), (-1, 0, 0)], dtype=float)
jet_cases = 0
for _ in range(500):
    # All five directions belong to one rigid hull, not five independently rotated jets.
    rotation, _r = np.linalg.qr(rng.normal(size=(3, 3)))
    if np.linalg.det(rotation) < 0:
        rotation[:, 0] *= -1
    axes = -normals @ rotation.T
    for mass in (1, 1.45, 3.55, 20, 200):
        counts = rng.choice([0, 1, 2, 4, 16, 64, 1000, 2_147_483_647], 5)
        old, new = recoil(counts, axes, mass)
        assert np.isfinite(new).all()
        assert np.array_equal(new[:, [0, 2]], old[:, [0, 2]])  # Strong horizontal jets unchanged.
        assert np.all(np.abs(new[:, 1]) <= np.abs(old[:, 1])+1e-12)
        assert np.all(new[:, 1]*old[:, 1] >= 0)  # No reaction direction flips.
        assert np.sum(np.maximum(new[:, 1], 0)) <= .075+1e-12
        assert np.sum(np.maximum(-new[:, 1], 0)) <= .075+1e-12
        for face in range(5):
            solo = np.zeros(5); solo[face] = counts[face]
            a, b = recoil(solo, axes, mass)
            assert np.allclose(a, b, atol=1e-12)  # Single-face behavior retained at all angles/caps.
        _, heavier = recoil(counts, axes, mass*2)
        # The cap can redistribute each face's share; total lift (not every share) must
        # decrease with mass. Check downward thrust separately so cancellation cannot hide it.
        for sign in (-1, 1):
            assert np.maximum(sign*heavier[:, 1], 0).sum() <= np.maximum(sign*new[:, 1], 0).sum()+1e-12
        _, boosted = recoil(counts, axes, mass, 16)
        assert np.allclose(boosted, new*16)
        jet_cases += 1
    # Six fish shared between two upward components cannot cancel occupied hull gravity.
    small = np.array([0, 3, 0, 3, 0])
    _, force = recoil(small, axes, 1.45)
    assert force[:, 1].sum() < .04

# A 45-degree pitch used to reward splitting 3+3 fish across bottom/stern.
axes = -normals.copy()
c = math.sqrt(.5)
rotation = np.array([(1, 0, 0), (0, c, c), (0, -c, c)])
axes = axes @ rotation.T
old, new = recoil(np.array([3, 3, 0, 0, 0]), axes, 1.0)
assert old[:, 1].sum() > .04 > new[:, 1].sum()
assert math.isclose(new[:, 1].sum(), .018*math.sqrt(3), abs_tol=1e-12)
# Quantity can still overcome weight: this is not an arbitrary prohibition on flight.
axes = -normals.copy()
assert recoil(np.array([2, 0, 0, 0, 0]), axes, 1.45)[1][:, 1].sum() < .04
assert recoil(np.array([16, 0, 0, 0, 0]), axes, 1.45)[1][:, 1].sum() > .04

drop_g = float(re.search(r'DROP_GRAVITY\s*=\s*([.\d]+)', spray)[1])
mist_g = float(re.search(r'MIST_SETTLING\s*=\s*([.\d]+)', spray)[1])
max_drop_landing = 0
for height in (.08, .35, .9, 1.6):
    for upward in (.06, .17, .22):
        y, v = height, upward
        for tick in range(1, 33):
            y += v; v = v*.985-drop_g
            if y <= 0:
                max_drop_landing = max(max_drop_landing, tick)
                break
        else:
            raise AssertionError('A crest droplet never returned to the surface')
y, v, mist_rise = 0, .17*.16*1.22, 0
for _ in range(38):
    y += v; v = v*.92-mist_g; mist_rise = max(mist_rise, y)
assert mist_rise < .3 and v < 0
assert 'if(clock==born)return true' in spray


def smooth(x):
    x = max(0, min(1, x))
    return x*x*(3-2*x)


def film_alpha(height, noise, time):
    breakup = 1-smooth((time-(2+noise*2.5+(1-height)*2.2))/2.4)
    return smooth(time/.8)*(1-smooth((time-5)/5))*breakup


# Upper film must break before the foot, not remain as long tethered radial arms.
for time in np.linspace(1, 15, 57):
    for noise in np.linspace(0, 1, 17):
        assert 0 <= film_alpha(1, noise, time) <= film_alpha(0, noise, time) <= 1
        if time >= 10:
            assert film_alpha(0, noise, time) == 0
assert film_alpha(1, 0, 4) < film_alpha(1, 1, 4)
assert 'frameJets' not in spray and 'frameEdges[next][height]' in spray
assert 'MathHelper.clamp(alpha,0,255)' in spray

camera = read('client/RearCamera.java')
camera_mixin = read('mixin/client/RiderCameraMixin.java')
mouse = read('mixin/client/MouseMixin.java')
keys = read('client/BoatKeys.java')
tick = camera[camera.index('public static void tick('):camera.index('public static void toggle(')]
assert 'getYaw(' not in tick and 'getPitch(' not in tick
assert len(re.findall(r'\byaw\s*=\s*client.player.getYaw\(\)', camera)) == 2
assert 'if (RearCamera.active()) return;' in camera_mixin  # Do not tilt the stable camera.
assert 'BoatBody.visualPosition(boat,lastTickDelta).add(0,1.35,0)' in camera_mixin
assert 'Camera;clipToSpace(F)F' in camera_mixin  # Keep vanilla collision, change its input distance.
assert 'client.currentScreen != null' in camera and '!client.isWindowFocused()' in camera
assert 'longboat$rearCameraWheel(long window, double horizontal, double vertical' in mouse
assert 'GLFW.GLFW_KEY_F7' in keys and 'RearCamera.recenter(client)' in keys
assert 'if(RearCamera.active())return 0' in keys
mixins = json.loads((ROOT/'src/main/resources/longboatlab.mixins.json').read_text())
assert 'client.MouseMixin' in mixins['client']

screen = read('client/water/WaterGraphScreen.java')
nodes = re.findall(r'new Definition\("(\w+)",\d+,\d+,0x[0-9a-f]+,"[^"]+",sockets\(([^)]*)\),sockets\(([^)]*)\)\)', screen)
links = re.findall(r'new Link\((\d+),(\d+),(\d+),(\d+)\)', screen)
assert len(nodes) == 12 and len(links) == 17
for source, output, target, input in links:
    assert int(output) < len(re.findall(r'"(\w+)"', nodes[int(source)][2]))
    assert int(input) < len(re.findall(r'"(\w+)"', nodes[int(target)][1]))
for locale in ('zh_cn', 'en_us'):
    data = json.loads((ASSETS/f'lang/{locale}.json').read_text(encoding='utf-8'))
    assert data['key.longboatlab.rear_camera']
    for _id, inputs, outputs in nodes:
        for socket in re.findall(r'"(\w+)"', inputs+outputs):
            assert data[f'screen.longboatlab.water.socket.{socket}']

texture_results = {}
for name in ('particle/jet', 'water/vapor'):
    alpha = np.array(Image.open(ASSETS/f'textures/{name}.png'))[:, :, 3]
    assert max(alpha[0].max(), alpha[-1].max(), alpha[:, 0].max(), alpha[:, -1].max()) == 0
    assert np.count_nonzero((alpha > 0) & (alpha < 25)) > alpha.size*.2
    texture_results[name] = {'max_alpha': int(alpha.max()), 'mean_alpha': round(float(alpha.mean()), 2), 'transparent_edges': True}
shader = (ASSETS/'shaders/core/soft_spray.fsh').read_text()
assert 'color.a < 0.001' in shader and 'linear_fog(' in shader
config = json.loads((ASSETS/'shaders/core/soft_spray.json').read_text())
vs = (ASSETS/'shaders/core/soft_spray.vsh').read_text()
uniforms = set(re.findall(r'uniform\s+\w+\s+(\w+);', shader+vs))
assert uniforms == {p['name'] for p in config['samplers']+config['uniforms']}
render_pass = read('client/BoatEffectRenderPass.java')
assert '.writeMaskState(COLOR_MASK)' in render_pass and 'WorldRenderEvents.LAST' in render_pass

report = dict(version='0.10.18',jet_pose_mass_cases=jet_cases,
              horizontal_thrust_unchanged=True,single_face_thrust_unchanged=True,
              combined_small_jet_lift_regression_reproduced=True,more_mass_reduces_lift=True,
              maximum_crest_drop_landing_ticks=max_drop_landing,maximum_mist_rise=round(mist_rise,4),
              crown_rim_breaks_before_foot=True,crown_film_gone_by_tick=10,
              camera_lifecycle_and_mixin_source_guards=True,graph_links_checked=len(links),
              soft_texture_alpha=texture_results,soft_shader_resource_contract_checked=True,
              java_compiled=False,minecraft_launched=False,live_camera_tested=False,
              in_game_visuals_verified=False,fps_measured=False)
(ROOT/'docs/spray-camera-0.10.18.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,indent=2))
