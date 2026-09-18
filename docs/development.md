# 开发与资源说明

[返回 README](../README.md)

## 构建依赖

普通构建只需要 JDK 21 和工程内的 Gradle Wrapper。当前版本配置：

| 工具 / 依赖 | 版本 | 用途 |
| --- | --- | --- |
| Gradle Wrapper | 9.5.1 | 构建工具，首次运行自动下载 |
| Fabric Loom | 1.17.12 | 开发构建与重映射插件，不是游戏前置模组 |
| Minecraft | 1.21.1 | 目标游戏版本 |
| Yarn mappings | 1.21.1+build.3 | 开发映射，不需放入玩家 `mods` |
| Fabric Loader | 0.19.3 | 开发配置版本；模组元数据声明最低 0.16.0 |
| Fabric API | 0.116.13+1.21.1 | 开发依赖，也是玩家与服务端必需的运行前置 |

版本来源为 [build.gradle](../build.gradle)、[gradle.properties](../gradle.properties) 和 [Wrapper 配置](../gradle/wrapper/gradle-wrapper.properties)。运行依赖完整说明见 README。

在项目根目录执行 `./gradlew.bat build`，构建产物位于 `build/libs`。地图与工具目录不作为额外游戏前置打包；资源文件已在 `src/main/resources` 中，无需为普通构建运行生成脚本。

## Blockbench 模型

实际读取的源文件：

- [continuous_hull.bbmodel](../src/main/resources/assets/longboatlab/models/entity/continuous_hull.bbmodel)
- [oar.bbmodel](../src/main/resources/assets/longboatlab/models/entity/oar.bbmodel)

运行时由模组自己的 [LongboatModels](../src/main/java/com/xc/longboatlab/client/LongboatModels.java) 读取立方体和盒式 UV，不需要 GeckoLib 或 Blockbench 运行环境，也不需要导出 Java 模型。

船体参考模型为两份船长，16 模型单位等于 1 格，+Z 为船头，+X 为左侧，+Y 为上方。保留 `stretch_`、`bow_`、`stern_` 前缀和 `seat_center` 模板：它们分别控制连续延展、首尾移动与舱间横板生成。当前读取器不解析旋转立方体、组变换、网格、Blockbench 动画或单面 UV；船桨与形变动画由代码驱动。

修改模型外形时需同步对应的船体、普通桨或巨大桨几何与碰撞逻辑；修改模型不会自动改变碰撞箱。

加宽时在 `LongboatModels.bake` 中扩展船底、首尾板和横梁的实际尺寸，原版 `ModelPart.Cuboid` 按几何尺寸计算盒式 UV。九种 2048×1024 木纹图集都是 16×16 木板纹理的重复铺图，新增宽度继续采样图集，横向仍为每格 16 像素。模型缓存以长宽共同索引，上限 32 项，左右侧壁只平移；不再使用 X 缩放拉伸宽船。纵向压缩动画和巨大桨缩放不变。

## 资源与地图工具

以下工具仅在重新制作资源或地图时使用，**不属于游玩依赖，也不属于普通 Gradle 构建依赖**。Python 脚本按各自 import 需要 NumPy、Pillow、SoundFile 等；离线 Java 语法检查使用 tree-sitter 与 tree-sitter-java。

| 资源 | 主要位置 / 工具 |
| --- | --- |
| 木纹与模型材质 | `src/main/resources/assets/longboatlab/textures/entity`；`tools/make_textures.ps1` |
| 像素 HUD | `src/main/resources/assets/longboatlab/textures/gui/boat_hud.png`；`tools/make_hud.py` |
| 河豚喷气音效 | `tools/make_jet_sound.py` |
| 羽化喷气与水雾纹理 | `tools/make_jet_texture.py`；低透明度渲染用 `shaders/core/soft_spray.*` |
| 水花、浪尖与泡沫纹理 | `tools/make_water_textures.py`、`tools/make_wake_ribbon_texture.py` |
| 拍水与连续破浪音效 | `tools/make_water_sounds.py` |
| V12 地图生成 / 离线校验 | `tools/make_mountain_race_save.py`、`tools/check_mountain_race_save.py` |
| 独立关卡正交图导出 | `tools/export_stage_orthographs.py`，输出 `Stage_Orthographs_V12` |
| 水花、网格、热调与节点源码检查 | `tools/check_water_workshop.py` |
| 稳定视角、喷气合力、雾滴与透明纹理检查 | `tools/check_spray_camera.py` |
| 角度插值、喷雾方向与连续浪迹检查 | `tools/check_wake_motion.py` |
| 船侧压力波、破碎泡沫与纹理检查 | `tools/check_spilling_wake.py`；`tools/preview_spilling_wake.py` 输出离线几何／材质预览到 `build/visual-review-0.10.19` |
| 宽船材质与几何检查 | `tools/check_hull_material.py`，校验侧壁厚度、横梁衔接、UV 密度与九种图集平铺 |
| 资源批处理辅助脚本 | `tools/update_ability_assets.mjs`；仅此类工具需要 Node.js |

模型、声音、贴图、水面 shader 与地图均已保存。不要为了安装模组重新运行地图生成器；需要导入地图时使用根目录的 `Puffer_Rally_Final` 或 ZIP。

模组元数据的许可标识为 MIT。九种木纹由 Minecraft 原版木板贴图派生，纹理内容归 Minecraft 原权利人所有，不能将源码许可视为对原版素材的重新授权。项目中的喷气 / 水声为程序生成的资源；水面实现的设计与参考记录见 [water-surface-design.md](water-surface-design.md)，该文件包含历史阶段设计，当前实现以源码为准。

## 验证范围

`docs` 下保留相关离线检查记录，例如 [交互几何](boat-interactions-0.10.15.json)、[水雾与角度插值](wake-motion-0.10.16.json) 和 [最终地图复制校验](final-map-copy.json)。这些检查涵盖部分语法、几何 / 数值约束、文件和数据结构，不会替代 Minecraft 内的编译加载、驾驶手感、多人同步、画面或帧率验证。

0.10.17 未运行 Gradle 构建或启动游戏；V12 地图和正交图离线生成，最终地图复制核对全部文件内容与 ZIP 完整性。开发环境新增独立 V12，旧版已游玩存档保留。

0.10.18 核对了 1.21.1 的 `Camera.update` / `clipToSpace`、鼠标滚轮回调和节点界面 API。离线检查覆盖 2,500 组喷气姿态与质量组合、单面行为和水平推力保持、多面升力预算、雾滴回落、曲面破碎时序、17 条节点连线及软透明 Shader 的资源契约；并复查现有转圈插值、喷雾方向与浪迹接缝的数值约束。报告为 [spray-camera-0.10.18.json](spray-camera-0.10.18.json)、[water-workshop-0.10.18.json](water-workshop-0.10.18.json)、[wake-motion-0.10.18.json](wake-motion-0.10.18.json)。只做源码、离线数值及布局／纹理预览，未运行 Java 编译、OpenGL 或 Minecraft，不能视作实机效果验收。

稳定相机由 `RearCamera` 保存世界朝向和缩放距离，`RiderCameraMixin` 修改原版后视镜头的角度、焦点与避墙距离；只影响本地乘船视角。`MouseMixin` 仅在此模式且无菜单时接管滚轮。F8 界面在 `WaterGraphScreen`，端口和连线描述实际渲染流程，拖动节点只改变布局。

`JetPhysics` 统一分配各面的垂直合力，`BoatBody` 用同一结果施加平移与力矩。`soft_spray` 用于喷气、水雾以及 0.10.19 的侧浪水色／白沫，保留低 alpha 边缘、场景深度测试、颜色写入和水面之后的绘制顺序。拍水曲面在每帧缓存并由水膜／泡沫复用；喷气方向投影按年龄段和船面共用。粒子池、世界采样和高度场预算保持原值。

0.10.19 的 `WakeProfile` 提供船头压力衰减、局部向外推水、浪高宽度约束和破碎强度的视觉模型，属于基于现象的近似，不是三维流体求解器。`WakeNode` 插值并缓存共享的 12 段水色曲面，白沫采样同一网格。连续的节点相位同时控制形态与纹理流动；几何只使用比采样间距更长的波长，细泡纹理负责更小的细节，避免长船自转时空间混叠。

侧浪检查见 [spilling-wake-0.10.19.json](spilling-wake-0.10.19.json)，方向与角度回归见 [wake-motion-0.10.19.json](wake-motion-0.10.19.json)。离线 CPU 预览只比较纹理透明度和几何形态，不包含实际游戏的动态水面、光照与粒子，不能当作实机截图。未编译或启动游戏，本轮没有改动地图。

宽船材质检查见 [hull-material-0.10.19.json](hull-material-0.10.19.json)，包含 96 组宽度／部件检查，确认原宽形状、加宽后的侧壁厚度与横梁接触不变；九种图集均可继续平铺。通过本地 1.21.1 字节码确认盒式 UV 的跨度按传入 cuboid 尺寸计算，不依赖编译或游戏启动。
