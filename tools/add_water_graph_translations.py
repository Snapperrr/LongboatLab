"""Keep both shipped locales complete for the in-game water inspector and operator controls."""
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/longboatlab/lang'
UI={
 'title':('水面节点','Water Nodes'),
 'fit':('查看全部','Frame All'), 'selected':('查看所选','Frame Selected'),
 'properties':('侧栏','Sidebar'), 'node':('节点','Node'),
 'breadcrumb':('水面节点  >  Longboat Water','Water Nodes  >  Longboat Water'),
 'active':('模拟：活动','Simulation: Active'), 'waiting':('模拟：待机','Simulation: Idle'),
 'fallback':('表面：原版','Surface: Vanilla'),
 'controls':('中键 平移    滚轮 缩放    拖动 移动节点    Home 查看全部    N 侧栏    Esc 关闭',
             'MMB Pan    Wheel Zoom    Drag Move    Home Frame All    N Sidebar    Esc Close'),
 'controls_short':('中键平移 · 滚轮缩放 · Home 全部 · N 侧栏','MMB Pan · Wheel Zoom · Home All · N Sidebar'),
 'source':('来源\n%s','Source\n%s'),
 'metric.contact':('船体 %s   采样 %s','Hulls %s   Samples %s'),
 'metric.pressure':('船体   %s / 8','Hulls   %s / 8'),
 'metric.field':('分辨率   97 × 97','Resolution   97 × 97'),
 'metric.ribbon':('侧浪 %s   尾迹 %s','Hull %s   Wake %s'),
 'metric.splash':('数量   %s','Count   %s'),
 'metric.spray':('水滴 %s   雾 %s','Drops %s   Mist %s'),
 'metric.foam':('涟漪   %s','Ripples   %s'),
 'metric.jets':('密度 %s   力 %s','Density %s   Force %s'),
}
SOCKETS={'water':('水体','Water'),'material':('材质','Material'),'motion':('运动','Motion'),
 'impact':('碰撞','Impact'),'pressure':('压力','Pressure'),'height':('高度','Height'),
 'surface':('表面','Surface'),'geometry':('几何体','Geometry'),'foam':('泡沫','Foam'),
 'spray':('喷雾','Spray'),'gas':('气体','Gas')}
for name,pair in SOCKETS.items():UI['socket.'+name]=pair
NODES={
 'water':('水体输入','Water Input','静水 / 流向','Level / Flow',
          '输出水位、流向与岸线遮罩。水色和光照取自当前世界。','Water level, flow and shoreline mask. Tint and lighting from the current world.'),
 'contact':('船体输入','Hull Input','','',
            '范围 80 m。上限 24 艘，2304 次表面查询 / tick。输出运动与入水事件。','Range 80 m. Limit 24 hulls, 2304 queries per tick. Outputs motion and entry events.'),
 'pressure':('船体压力','Hull Pressure','','',
             '沿运动轨迹写入龙骨低谷与尾部肩波。同水位的压力相加。上限 8 艘。','Swept keel depression and wake shoulders. Pressure adds at the same water level. Limit 8 hulls.'),
 'impact':('入水冲量','Entry Impulse','速度 × 面积','Speed × Area',
           '根据入水方向、速度与面积生成下凹和外扩压力。压力在世界坐标保留，随后衰减。','Entry direction, speed and area determine depression and outward pressure. Retained in world space, then decayed.'),
 'field':('波动求解','Wave Simulation','','',
          '尺寸 48 × 48 m。间距 0.5 m。子步 3。阻尼 0.13。多船共用高度场；岸线吸收、深度限幅。','Size 48 × 48 m. Spacing 0.5 m. Substeps 3. Damping 0.13. Shared wave field; shore absorption and depth limits.'),
 'material':('水面材质','Water Material','原版水图集','Vanilla Water Atlas',
             '高度位移与表面法线。原版图集、水色、光照。Sodium / Iris 环境使用原版表面。','Height displacement and surface normals. Vanilla atlas, tint and light. Vanilla surface with Sodium / Iris.'),
 'ribbon':('尾迹几何','Wake Geometry','','',
           '船头压力波沿船身衰减；转弯按各船段向外推水的速度增强。共享端点连接船侧与尾迹。装饰几何独立于高度场。','Bow pressure decays along the hull. Turning waves follow each segment’s outward motion. Shared endpoints join hull and wake. Geometry is separate from the height field.'),
 'foam':('泡沫','Foam','','',
         '泡沫强度取决于浪高与宽度之比。破碎泡沫簇沿浪尖和外侧回落，间隙透出水色。落水涟漪上限 48。','Breaking intensity follows wave steepness. Separate foam islands spill along the crest and outer slope, with clear water between them. Limit 48 landing ripples.'),
 'splash':('飞溅几何','Splash Geometry','','',
           '不规则折叠水冠。上缘先破碎，下缘外扩。重力 0.042 m/tick²。主体约 0.5 s 内消散。上限 16 组。','Irregular folded crown. Rim breakup and spreading base. Gravity 0.042 m/tick². Membrane dissipates within about 0.5 s. Limit 16 groups.'),
 'spray':('水滴与雾','Droplets and Mist','','',
          '水滴重力 0.045 m/tick²。薄雾低速扩散、缓慢沉降。上限 384 水滴、192 雾片。','Droplet gravity 0.045 m/tick². Vapor diffuses at low speed and settles slowly. Limit 384 drops, 192 vapor patches.'),
 'output':('渲染输出','Render Output','深度测试 / 颜色写入','Depth Test / Color Write',
           '水面写入深度。透明特效在水面合成之后绘制，只写颜色。侧浪、泡沫、喷气与雾保留低透明度边缘。','Surface writes depth. Transparent effects render after water composition, color writes only. Hull waves, foam, gas and mist retain low-alpha edges.'),
 'jets':('气体','Gas','','',
         '发射位置与方向取自河豚嘴。粒子减速、扩散、消散。管理员：/longboatlab jets density 或 force。','Emission from puffer mouths. Deceleration, diffusion and dissipation. Operator settings: /longboatlab jets density or force.'),
}

for locale,col in [('zh_cn',0),('en_us',1)]:
    path=ROOT/(locale+'.json');data=json.loads(path.read_text(encoding='utf-8'))
    data['key.longboatlab.water_graph']=('水面节点','Water Nodes')[col]
    data['key.longboatlab.rear_camera']=('稳定后视 / Shift 重新对齐','Stable rear view / Shift to recenter')[col]
    data['message.longboatlab.rear_camera']=('稳定后视 · 滚轮调距 · Shift + 视角键重新对齐','Stable rear view · Wheel to zoom · Shift + camera key to recenter')[col]
    data['command.longboatlab.jets']=('河豚喷气：粒子密度 %s 倍 / 推力 %s 倍（已即时生效；默认均为 1）',
                                    'Puffer jets: particle density %s× / force %s× (live; defaults are 1)')[col]
    for key,pair in UI.items():data['screen.longboatlab.water.'+key]=pair[col]
    for key,row in NODES.items():
        for field,start in [('title',0),('metric',2),('description',4)]:
            data['screen.longboatlab.water.'+key+'.'+field]=row[start+col]
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
