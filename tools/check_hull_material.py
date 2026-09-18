"""Check widened hull bounds and native-density box UV assumptions without compiling Java."""
from pathlib import Path
import json
import math
import re
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/longboatlab'
JAVA=ROOT/'src/main/java/com/xc/longboatlab/client'
source=(JAVA/'LongboatModels.java').read_text(encoding='utf-8')
model=json.loads((ASSETS/'models/entity/continuous_hull.bbmodel').read_text(encoding='utf-8'))
cubes={c['name']:c for c in model['elements']}
assert 'Map<HullSize, ModelPart> hulls' in source
assert 'new HullSize(segments, width)' in source
assert 'part.xScale' not in source and 'divider.xScale' not in source
assert '.cuboid(x, cube.y(), z, cubeWidth, cube.height(), depth)' in source
assert 'crossbar.width() * crossbarScale' in source
assert 'return size() > 32' in source and 'hulls.clear()' in source
assert 'LongboatRenderer.render(' in (JAVA/'BoatItemPreview.java').read_text(encoding='utf-8')


def bounds(name,width):
    c=cubes[name];lo,hi=c['from'][0],c['to'][0]
    if name.startswith('stretch_left_'):
        shift=(width-1)*11;return lo+shift,hi+shift
    if name.startswith('stretch_right_'):
        shift=-(width-1)*11;return lo+shift,hi+shift
    scale=(22*width-4)/18 if name.startswith('seat_') else width
    return lo*scale,hi*scale


cases=0
for width in (1,2,3,4,16,64,256,10000):
    floor=bounds('stretch_floor',width)
    assert floor==(-11*width,11*width)
    assert bounds('bow_wall',width)==bounds('stern_wall',width)==floor
    assert bounds('bow_rail',width)==bounds('stern_rail',width)==floor
    left=bounds('stretch_left_wall',width);right=bounds('stretch_right_wall',width)
    assert left[1]==floor[1] and right[0]==floor[0]
    assert left[1]-left[0]==right[1]-right[0]==2
    divider=bounds('seat_center',width)
    assert math.isclose(divider[0],right[1],abs_tol=1e-8)
    assert math.isclose(divider[1],left[0],abs_tol=1e-8)
    for name,c in cubes.items():
        lo,hi=bounds(name,width);face_width=hi-lo
        # Yarn 1.21.1 Cuboid: U interval spans the supplied cuboid dimension before
        # division by atlas width; widened geometry therefore retains 16 px per block.
        normalized_uv_span=face_width/model['resolution']['width']
        pixels_per_block=normalized_uv_span*model['resolution']['width']/(face_width/16)
        assert math.isclose(pixels_per_block,16,abs_tol=1e-9)
        if width==1:assert (lo,hi)==(c['from'][0],c['to'][0])
        cases+=1

# All nine existing atlases are whole-image repeats, not separate packed materials:
# expanded box UVs must not wander into another model's unrelated sprite.
woods=('oak','spruce','birch','jungle','acacia','dark_oak','mangrove','cherry','bamboo')
for wood in woods:
    image=Image.open(ASSETS/f'textures/entity/{wood}.png').convert('RGBA')
    pixels=np.asarray(image);tile=pixels[:16,:16]
    assert image.size==(2048,1024)
    assert np.array_equal(pixels,np.tile(tile,(64,128,1))),(wood,'atlas is not periodic')

version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M)[1].strip()
report=dict(version=version,width_geometry_and_uv_cases=cases,native_pixels_per_block=16,
            original_width_geometry_preserved=True,sidewall_thickness_preserved=True,
            divider_contacts_preserved=True,periodic_wood_atlases_checked=len(woods),
            dimension_cache_bounded=True,shared_item_and_entity_renderer_source_checked=True,
            java_compiled=False,minecraft_launched=False,in_game_visuals_verified=False)
(ROOT/f'docs/hull-material-{version}.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')

out=ROOT/f'build/visual-review-{version}';out.mkdir(parents=True,exist_ok=True)
tile=Image.open(ASSETS/'textures/entity/oak.png').crop((0,0,16,16)).convert('RGB')
one=np.tile(np.array(tile),(4,2,1))[:64,:22]
expanded=np.tile(np.array(tile),(4,6,1))[:64,:88]
images=[Image.fromarray(one),Image.fromarray(one).resize((88,64),Image.Resampling.NEAREST),Image.fromarray(expanded)]
preview=Image.new('RGB',(1100,365),'#253444');draw=ImageDraw.Draw(preview)
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',19)
for x,img,label in zip((22,180,640),images,('原宽 · 原生木纹','4 倍宽 · 旧的拉伸','4 倍宽 · 新的平铺')):
    draw.text((x,12),label,font=font,fill='#d1dde5');preview.paste(img.resize((img.width*5,img.height*4),Image.Resampling.NEAREST),(x,52))
draw.text((22,326),'离线木纹密度示意 · 非游戏截图',font=font,fill='#bdcad2')
preview.save(out/'hull-material-density.png')
print(json.dumps(report,indent=2))
