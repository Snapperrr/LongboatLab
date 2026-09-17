"""Review V11 waterfront chests and the boat workbench from real saved blocks."""
from PIL import Image,ImageDraw,ImageFont
import preview_rally_scenery as view

view.COLORS.update({'grass_block':(101,151,72),'short_grass':(107,165,68),'fern':(75,134,63),
    'dirt':(133,93,65),'coarse_dirt':(121,91,66),'rooted_dirt':(140,105,75),'sand':(219,210,157),
    'spruce_planks':(129,101,65),'andesite':(129,131,130),'polished_andesite':(146,147,145),
    'oak_log':(121,98,66),'birch_log':(219,215,194),'birch_leaves':(92,143,57),
    'oak_leaves':(83,137,60),'yellow_wool':(241,188,44),'lime_concrete':(114,172,30),
    'oak_sign':(166,135,82),'chest':(176,121,47),'crafting_table':(141,105,57),
    'dandelion':(236,208,53),'cornflower':(70,113,216),'oxeye_daisy':(239,237,208),
    'cobblestone':(124,129,126),'lime_wool':(114,190,45)})

station=view.manifest['supply_stations'][0]
view.render(view.voxels((station['x']-5,station['z']),35,60,79,True),
    'V11 木铲水港 · 每站仅一种资源','实际存档方块 / 绿色港只供应木铲6 · 箱子在水边第一排',
    view.SAVE.parent/(view.SAVE.name+'_supply_harbor.png'),turns=2)
station=view.manifest['supply_stations'][1]
bench=next(v for v in station['service'] if v['kind']=='crafting_table')
view.render(view.voxels((bench['x'],bench['z']),12,62,69,True),
    'V11 木船补给 · 箱子旁的工作台','实际存档方块 / 木船2 · 箱子与工作台相邻，可乘船靠泊使用',
    view.SAVE.parent/(view.SAVE.name+'_boat_workbench.png'),turns=0)
landmark=next(v for v in view.manifest['decor']['ground_details'] if v['kind']=='mossy_boulder' and v['center'][2]>160)
x,y,z=landmark['center']
view.render(view.voxels((x,z),31,max(63,y-4),y+15,True),
    'V11 沿岸地表细节','实际存档方块 / 苔石、花草、碎石与林下灌丛',
    view.SAVE.parent/(view.SAVE.name+'_riverbank.png'))
root=view.SAVE.parents[1];assets=root/'src/main/resources/assets/longboatlab/textures/water'
sheet=Image.new('RGB',(1100,360),(28,55,66));draw=ImageDraw.Draw(sheet)
font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc',20)
for i,(name,label) in enumerate((('ribbon','薄浪面'),('froth','浪尖气泡'),('wake_lace','尾迹碎沫'))):
    texture=Image.open(assets/(name+'.png')).convert('RGBA').resize((256,256))
    tile=Image.new('RGBA',(320,256),(65,120,148,255))
    for start in (0,256):tile.alpha_composite(texture,(start,0))
    sheet.paste(tile.convert('RGB'),(25+i*365,55));draw.text((25+i*365,18),label,font=font,fill='#edf6fa')
draw.text((25,323),'程序生成纹理预览；游戏内另有连续曲面、插值、细飞滴与光照',font=font,fill='#d2edf6')
sheet.save(root/'docs/water-refinement-0.10.13.png')
print('Saved supply harbor, riverbank and water texture previews.')
