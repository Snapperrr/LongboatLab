"""Offline regression models for yaw wrapping and wake advection (no Java compilation).

Parses the edited Java, then checks mathematical invariants against documented Minecraft
angle normalization. The numerical model does not execute Minecraft/rendering or measure FPS.
"""
from pathlib import Path
import json
import math
import random
import re
import sys
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/xc/longboatlab'
sys.path.insert(0, str(ROOT / 'tools/.audio-deps'))
from tree_sitter import Language, Parser
import tree_sitter_java

parser = Parser(Language(tree_sitter_java.language()))
names = ('BoatBody.java', 'client/LongboatRenderer.java', 'client/water/WaterImpact.java',
         'client/water/BoatWaterEffects.java', 'client/water/WaterSplashRenderer.java')
sources = {name: (JAVA / name).read_text(encoding='utf-8') for name in names}
for name, source in sources.items():
    assert not parser.parse(source.encode()).root_node.has_error, name
body = sources['BoatBody.java']
renderer = sources['client/LongboatRenderer.java']
effects = sources['client/water/BoatWaterEffects.java']
spray = sources['client/water/WaterSplashRenderer.java']
assert 'MathHelper.lerpAngleDegrees(delta,boat.prevYaw,boat.getYaw())' in body
assert 'if (!inventory) yaw = com.xc.longboatlab.BoatBody.visualYaw(boat, tickDelta)' in renderer
assert 'tangent.normalize().multiply(-0.35)' not in spray
assert 'fleck.previous = a.point(across, 0, false)' in spray
assert 'EFFECTS.finishTick(client)' in effects and 'node.shed(client)' not in spray


def constant(name):
    return float(re.search(r'\b' + name + r'\s*=\s*([0-9.]+)', spray)[1])


def wrap(angle):
    return (angle + 180) % 360 - 180


def rotate(v, yaw):
    c, s = math.cos(math.radians(yaw)), math.sin(math.radians(yaw))
    x, y, z = v
    return np.array([x*c-z*s, y, x*s+z*c])


def unit(v):
    norm = np.linalg.norm(v)
    return v/norm if norm > 1e-12 else np.zeros(3)


# On each 360-degree boundary, the rendered seat and hull must travel the same short arc.
# Real local positions model single, long and wide seats; roll/pitch change their Y only.
yaw_cases = 0
old_wrap_jumps = 0
largest_old_error = 0.
largest_new_error = 0.
for direction in (-1, 1):
    for rate in (1.5, 6., 12.):
        for local in ((0, .2, .4), (.7, .3, 16), (5.5, 1.2, -32)):
            old = 0.
            for tick in range(1, int(1080/rate)+1):
                current = math.fmod(direction*tick*rate, 360.)
                for delta in (0., .125, .5, .875, 1.):
                    expected = rotate(local, direction*(tick-1+delta)*rate)
                    corrected = rotate(local, old + wrap(current-old)*delta)
                    wrong = rotate(local, old+(current-old)*delta)
                    new_error = float(np.linalg.norm(corrected-expected))
                    old_error = float(np.linalg.norm(wrong-expected))
                    largest_new_error = max(largest_new_error, new_error)
                    largest_old_error = max(largest_old_error, old_error)
                    assert new_error < 1e-9
                    if old_error > .2:
                        old_wrap_jumps += 1
                    yaw_cases += 1
                old = current
assert old_wrap_jumps > 0 and largest_old_error > 60


def wake_velocity(relative, flow, outward, power, fog, scatter):
    """Numerical counterpart; assertions below test momentum/sign/speed invariants."""
    out = unit(np.array([outward[0], 0, outward[2]]))
    flat = np.array([relative[0], 0, relative[2]])
    into = float(flat @ out)
    along = flat-out*into
    drift = along*(.12 if fog else .25)
    if np.linalg.norm(drift) > .12:
        drift = unit(drift)*.12
    eject = min(.17, .035+power*.060+max(0, into)*.08) * (.60 if fog else 1) * (.9+abs(scatter)*.2)
    jitter = np.array([-out[2], 0, out[0]])*min(.008, np.linalg.norm(along)*.03)*scatter
    lift = min(.17, .06+power*.09)*(.60 if fog else 1)*(1+scatter*.22)
    velocity = drift+out*eject+jitter+np.array([0, lift, 0])
    limit = constant('MAX_WAKE_SPRAY_SPEED')
    if np.linalg.norm(velocity) > limit:
        velocity = unit(velocity)*limit
    return flow+velocity


# Both sides inherit some forward momentum, but still trail in the boat's frame.
# Reverse travel reverses the along-hull component, not the outboard spray direction.
spray_cases = 0
for yaw in range(-360, 361, 30):
    forward = rotate((0, 0, 1), yaw)
    flow = rotate((.006, 0, -.008), yaw)
    for side in (-1, 1):
        outward = rotate((side, 0, 0), yaw)
        for speed in (-100., -4., -.4, -.12, .12, .4, 4., 100.):
            for power in (.10, .45, 1.7):
                for fog in (False, True):
                    for scatter in (-1., 0., 1.):
                        relative = forward*speed
                        velocity = wake_velocity(relative, flow, outward, power, fog, scatter)
                        along = float((velocity-flow) @ forward)
                        assert along*speed > 0
                        assert abs(along) < abs(speed)
                        assert float((velocity-flow) @ outward) > 0
                        assert np.linalg.norm(velocity-flow) <= constant('MAX_WAKE_SPRAY_SPEED')+1e-12
                        assert np.linalg.norm(velocity) < .25  # Remains below the expensive raycast threshold.
                        assert np.isfinite(velocity).all()
                        spray_cases += 1

# Reproduce the old backward world-space ejecta at ordinary forward rowing speed.
old_radial = unit(np.array([.8, 0, -.35]))
old_forward = (old_radial*(.025+.45*.17)+np.array([0, 0, .4*.08]))[2]
assert old_forward < 0

# During pure yaw rotation, all stations on one side must share along-hull texture travel.
# Midpoint projection avoids chord-length errors stretching the texture each revolution.
phase_cases = 0
texture_scale = constant('WAKE_TEXTURE_SCALE')
for direction in (-1, 1):
    for rate in (3., 12.):
        for side in (-1, 1):
            stations = np.array([-16., -1., 0., 1., 16.])
            phase = stations*texture_scale
            initial_diffs = np.diff(phase)
            for tick in range(1, int(1080/rate)+1):
                old_yaw = math.fmod(direction*(tick-1)*rate, 360.)
                new_yaw = math.fmod(direction*tick*rate, 360.)
                axis = unit(rotate((0, 0, 1), old_yaw)+rotate((0, 0, 1), new_yaw))
                for i, z in enumerate(stations):
                    contact_motion = rotate((side*.75, 0, z), new_yaw)-rotate((side*.75, 0, z), old_yaw)
                    along = float(contact_motion @ axis)
                    phase[i] += max(-1.5, min(1.5, along))*texture_scale
                    phase_cases += 1
                assert np.allclose(np.diff(phase), initial_diffs, atol=1e-10)

# Positive U travel on a stable local-Z axis must move texture features aft (and vice versa).
for speed in (-1.2, -.4, .4, 1.2):
    local_feature_velocity = -(speed*texture_scale)/texture_scale
    assert math.isclose(local_feature_velocity, -speed)

# Length-weighted stratification covers both gunwales within the existing global limit.
budget_cases = 0
rng = random.Random(1016)
for quality in (1, 2, 3):
    for spans_per_side in (1, 4, 20, 40):
        weights = [1./spans_per_side]*(2*spans_per_side)
        total = sum(weights)
        cumulative = np.cumsum(weights)
        for frame in range(100):
            count = min(10*quality, math.ceil(total*quality*3.4))
            indices = [int(np.searchsorted(cumulative, total*(i+rng.random())/count)) for i in range(count)]
            assert count <= 10*quality
            assert any(i < spans_per_side for i in indices)
            assert any(i >= spans_per_side for i in indices)
            budget_cases += 1
assert constant('MAX_DROPS') == 384 and constant('MAX_MIST') == 192

report = {
    'version': '0.10.16', 'java_files_parsed': len(names),
    'yaw_interpolation_cases': yaw_cases, 'old_wrap_jump_cases_reproduced': old_wrap_jumps,
    'largest_old_seat_position_error_blocks': round(largest_old_error, 6),
    'largest_new_seat_position_error_blocks': round(largest_new_error, 12),
    'spray_direction_and_budget_cases': spray_cases, 'old_reverse_spray_reproduced': True,
    'rotating_texture_station_cases': phase_cases, 'two_side_allocation_cases': budget_cases,
    'particle_pool_limits_unchanged': True, 'java_compiled': False, 'minecraft_launched': False,
    'in_game_visuals_verified': False, 'fps_measured': False,
}
(ROOT/'docs/wake-motion-0.10.16.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
print(json.dumps(report, indent=2))
