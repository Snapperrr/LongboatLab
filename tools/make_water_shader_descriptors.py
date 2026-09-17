"""Generate core shader descriptors; shader sources are maintained alongside these JSON files."""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/longboatlab/shaders/core'
identity4 = [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1]


def uniform(name, values, kind='float'):
    return dict(name=name, type=kind, count=len(values), values=values)


common = [uniform('ModelViewMat', identity4, 'matrix4x4'), uniform('ProjMat', identity4, 'matrix4x4'),
          uniform('PatchOrigin', [0, 0, 0]), uniform('FogStart', [0]), uniform('FogEnd', [1]),
          uniform('FogColor', [0, 0, 0, 0]), uniform('FogShape', [0], 'int'),
          uniform('WaterStill', [0, 0, 1, 1])]
mask = common + [uniform('ChunkOffset', [0, 0, 0]), uniform('ColorModulator', [1, 1, 1, 1]),
                 uniform('WaterFlow', [0, 0, 1, 1])]
surface = common + [uniform('TickDelta', [0]), uniform('WaterUvInset', [0]),
                    uniform('UpShade', [1]), uniform('ColorModulator', [1, 1, 1, 1])]
for name, uniforms, samplers in [
    ('water_mask', mask, ['Sampler0', 'Sampler2', 'PatchMask']),
    ('water_surface', surface, ['HeightField', 'PatchMask', 'PatchLight', 'Sampler2', 'WaterAtlas']),
]:
    data = dict(vertex='longboatlab:' + name, fragment='longboatlab:' + name,
                samplers=[dict(name=s) for s in samplers], uniforms=uniforms)
    (root / (name + '.json')).write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')
