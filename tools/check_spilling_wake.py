"""Offline wake-shape, breaking and shared-edge regressions. No Java/OpenGL execution."""
from pathlib import Path
import json
import math
import re
import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/xc/longboatlab/client/water'


def smooth(t):
    t = np.clip(t, 0, 1)
    return t*t*(3-2*t)


def pressure(distance, outward):
    return max(.24+.76*math.exp(-max(0, distance)/2.6), smooth(max(0, outward)/.55))


def amplitude(power, entry, forcing, width):
    return max(0, min(width*.34, .54, (.11+power*.48)*entry*forcing))


def breaking(height, width, power):
    return smooth((height/max(.02, width)-.08)/.18)*(.35+.65*smooth(power/.90))


def crest(phase):
    p = phase*math.tau
    return .62+.022*np.sin(p*.11+.8)+.010*np.sin(p*.23)


def profile(across, peak):
    q = np.where(across <= peak, across/peak, (1-across)/(1-peak))
    return smooth(q)**2


def modulation(phase):
    p = phase*math.tau
    return 1+.045*np.sin(p*.11)+.020*np.sin(p*.23+1.2)


def run():
    source = (JAVA/'WaterSplashRenderer.java').read_text(encoding='utf-8')
    shape = (JAVA/'WakeProfile.java').read_text(encoding='utf-8')
    assert 'WakeProfile.pressure(Math.abs(station - leading), contact.velocity().dotProduct(out))' in source
    assert 'breaking = prior.breaking; previousBreaking = prior.previousBreaking' in source
    assert 'releasedBreaking = breaking' in source
    assert 'previousBreaking = breaking' in source
    assert 'alphaA * MathHelper.lerp(delta,a.previousBreaking,a.breaking)' in source
    assert 'alphaB * MathHelper.lerp(delta,b.previousBreaking,b.breaking)' in source
    assert 'if (flatReleaseAge' not in source  # Live hull whitecaps must not be disabled.
    assert 'float offset = foam ? i * 0.41f : 0' in source  # Water UVs cannot jump between rows.
    assert 'BoatEffectRenderPass.softLayer(FROTH)' in source
    assert 'BoatEffectRenderPass.softLayer(RIBBON)' in source
    assert 'return frameCurve[lower].lerp(frameCurve[lower + 1], fraction)' in source
    assert 'head == prior' in source and 'addSpan(new WakeStrip(head, node)' in source
    assert 'frameCurve=new Vec3d[WakeProfile.SAMPLES+1]' in source
    assert 'SAMPLES = 12' in shape

    # Both feet must return to the surface with no upright edge. An ordinary wave
    # cannot fold backwards or have a second high peak from an overlapping shoulder.
    phases = np.linspace(-120, 120, 161)
    sections = 0
    for p in phases:
        peak = float(crest(p))
        assert .58 < peak < .66
        assert profile(0., peak) == profile(1., peak) == 0
        assert profile(peak, peak) == 1
        assert np.all(np.diff(profile(np.linspace(0, peak, 80), peak)) >= -1e-12)
        assert np.all(np.diff(profile(np.linspace(peak, 1, 80), peak)) <= 1e-12)
        epsilon = 1e-5
        assert profile(epsilon, peak)/epsilon < .001
        assert profile(1-epsilon, peak)/epsilon < .001
        sections += 1

    # Longer straight midships shed the bow's pressure, but the outside of a turn
    # still raises a wave at the actual moving segment, including well behind the bow.
    for d in np.linspace(0, 40, 81):
        assert pressure(d+.1, 0) < pressure(d, 0)
        assert pressure(d, .55) == 1
        assert pressure(d, -.55) == pressure(d, 0)
    assert pressure(20, 0) < pressure(1, 0)*.4
    pose_cases = 0
    for yaw in np.linspace(-math.tau, math.tau, 73):
        c, s = math.cos(yaw), math.sin(yaw)
        matrix = np.array([[c, -s], [s, c]])
        for side in (-1, 1):
            for z in (-16, -1, 0, 1, 16):
                # yaw angular velocity at the station, plus forward translation.
                velocity = np.array([-.07*z, .4])
                normal = np.array([side, 0])
                expected = pressure(16-z, float(velocity@normal))
                actual = pressure(16-z, float((matrix@velocity)@(matrix@normal)))
                assert math.isclose(actual, expected, abs_tol=1e-12)
                pose_cases += 1

    heights = 0
    for power in (.1, .4, .8, 1.7):
        for entry in np.linspace(0, 1, 11):
            for d in (0, .75, 2, 8, 16, 64):
                width = (.60+power*.40)*entry
                h = amplitude(power, entry, pressure(d, 0), width)
                assert 0 <= h <= min(.54, width*.34)+1e-12
                assert 0 <= breaking(h, width, power) <= 1
                if entry == 0:
                    assert h == breaking(h, width, power) == 0
                heights += 1
    assert breaking(amplitude(.6, 1, 1, .588), .588, .6) > .6

    # A foam point must lie on the rendered polygon, not on a different analytic arc
    # inside it. Endpoints sampled from two neighbours must remain exactly identical.
    samples = np.linspace(0, 1, 13)
    seams = 0
    for p in phases:
        wave = profile(samples, crest(p))*.18
        for across in np.linspace(0, 1, 101):
            lo = min(11, int(across*12)); fraction = across*12-lo
            y = wave[lo]*(1-fraction)+wave[lo+1]*fraction
            assert math.isclose(y, np.interp(across, samples, wave), abs_tol=1e-12)
            seams += 1

    from make_wake_ribbon_texture import ribbon, froth
    for func in (ribbon, froth):
        for v in np.linspace(0, 1, 21):
            assert np.allclose(func(0., v), func(1., v), atol=1e-8)
            assert func(.17, 0)[3] == func(.17, 1)[3] == 0
    a = np.asarray(Image.open(ROOT/'src/main/resources/assets/longboatlab/textures/water/froth.png'))[:, :, 3]
    # Reproduce the old 97% nonzero blanket as a failing coverage pattern. Keep dense
    # white islands but require genuinely open water in the new shipped texture.
    assert np.mean(a < 8) > .55 and np.mean(a >= 150) > .03
    assert np.max(np.mean(a > 25, axis=1)) < .85
    assert 'MAX_SHEETS = 16, MAX_DROPS = 384, MAX_MIST = 192, MAX_RIPPLES = 48' in source
    assert 'liveStripsLeft = 80' in source and 'raysLeft = 384; waterQueriesLeft = 384' in source

    version = re.search(r'^mod_version=(.+)$', (ROOT/'gradle.properties').read_text(), re.M)[1].strip()
    report = dict(version=version,cross_section_cases=sections,pressure_rotation_cases=pose_cases,
                  height_and_breaking_cases=heights,foam_surface_cases=seams,
                  open_froth_fraction=round(float(np.mean(a < 8)), 4),
                  dense_froth_fraction=round(float(np.mean(a >= 150)), 4),
                  continuous_uv_and_shared_release_source_guards=True,particle_and_query_budgets_unchanged=True,
                  java_compiled=False,minecraft_launched=False,in_game_visuals_verified=False,fps_measured=False)
    (ROOT/f'docs/spilling-wake-{version}.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,indent=2))


if __name__ == '__main__':
    run()
