# 调试命令参考（0.10.19）

[返回 README](../README.md)

所有 `/longboatlab` 管理命令需要权限等级 2。单人开启作弊即可使用；以下 `give` / `oar` 示例由玩家执行。`spawn` 生成的船仍须通过空间检查，命令不会强行加载远处区块。

## 河豚喷气热调

```mcfunction
/longboatlab jets
/longboatlab jets density 2
/longboatlab jets force 1.5
/longboatlab jets reset
```

| 参数 | 范围 | 默认 | 作用 |
| --- | --- | --- | --- |
| `density <倍率>` | 0～8 | 1 | 河豚喷气的移动粒子采样密度；0 关闭喷气图像，不改变推力、声音或水浪 |
| `force <倍率>` | 0～16 | 1 | 五个面的实际喷射加速度和对应力矩一起缩放；重力、船重和原有水平／垂直计算仍保留 |
| `reset` | — | — | 同时恢复密度和推力为 1 |
| 不带子命令 | — | — | 查看当前倍率 |

仅权限等级 **2** 及以上可修改，控制台也可执行。无需重进游戏：推力从后续物理刻生效，粒子设置同步所有在线玩家，并在新玩家加入时同步。参数保存在当前世界的 `data/longboatlab_jets.dat`（随世界保存），所有维度共用；切换服务器时客户端恢复默认再接收新服务器参数。没有供普通客户端写入参数的网络接口。

默认倍率仍为 1；0.10.18 的喷气采用羽化气体纹理，多面喷射共享垂直合力预算，水平喷射力度保留。更高密度增加喷气顶点绘制开销，不增加水面格点或水浪粒子预算。该参数只调河豚喷气，不调拍水浪花；按 F8 的节点图可以查看收到的倍率。

## 快速生成普通桨船

```text
/longboatlab give <船长> <左桨数> <右桨数> <船尾河豚数> [木材]
/longboatlab spawn <船长> <左桨数> <右桨数> <船尾河豚数> [木材]
```

`give` 给予船物品，背包满则掉在身边；`spawn` 在执行者水平朝向前方 2 格生成压缩船。船长至少 1，其他数量至少 0，数值可到 2,147,483,647；生成的是宽度为 1 的船。

木材默认为 `oak`，支持 `oak / spruce / birch / jungle / acacia / dark_oak / mangrove / cherry / bamboo`，可用 Tab 补全。

```mcfunction
/longboatlab give 4 16 16 4 oak
/longboatlab spawn 16 64 64 8 spruce
```

## 指定大小的巨大桨

```text
/longboatlab oar <尺寸倍数> [数量]
```

尺寸支持小数，范围为 1.001～约 1290.159；数量默认 1，单次 1～64 根。尺寸保存到物品组件中。

```mcfunction
/longboatlab oar 2.5 2
```

## 完整改装船

```text
/longboatlab rig give <SNBT配置>
/longboatlab rig spawn <SNBT配置>
```

`rig` **默认生成巨大桨**；要使用普通桨，需设置 `NormalOars:1b`。省略不需要修改的字段即可。布尔值可写 `1b/0b`，小数示例中的 `d` 表示 double。

| 字段 | 默认 | 含义 |
| --- | --- | --- |
| `Length`、`Width` | 各 1 | 长度、宽度；1～2,147,483,647 |
| `Wood` | `"oak"` | 上述九种木材之一 |
| `Chest` | `0b` | 是否箱子船 |
| `NormalOars` | `0b` | `1b` 使用普通桨；不能同时提供巨大桨列表或 `LeftSize/RightSize` |
| `Compressed` | `0b` | `spawn` 的初始形态；巨大桨强制展开。`give` 的加长普通桨船物品默认压缩，不能用此字段强制物品展开 |
| `LeftCount`、`RightCount` | 各 1 | 普通桨模式下为每侧数量，0～2,147,483,647；巨大桨模式下为 0～min(船长, 512)，沿船舱分布 |
| `LeftSize`、`RightSize` | 各 4 | 巨大桨模式下对应侧的统一倍数，范围同 `oar` |
| `Left`、`Right` | 未设置 | 巨大桨列表 `[{Slot:1,Size:3d},…]`，覆盖该侧 Count/Size；每侧最多 512 根，同侧不能重复船舱 |
| `SternPuffers` | 0 | 兼容旧布局的船尾河豚数 |
| `Bottom` | `[]` | 兼容旧布局的船底河豚，如 `[{Slot:4,Count:6}]`；每个船舱只能出现一次 |
| `Grid` | `[]` | 指定五个面的河豚安装网格，见下表 |
| `Yaw` | 执行者朝向 | 船体水平角度，−360～360 度 |
| `Pitch`、`Roll` | 各 0 | 抬头、侧翻角度，−360～360 度；只有 `spawn` 支持非零值 |
| `Distance` | 4 | `spawn` 沿执行者水平朝向前移 0～256 格；与 `Yaw` 独立 |
| `Height` | 0 | `spawn` 在执行位置基础上竖直偏移 −64～256 格 |

船舱 `Slot` 从 1 开始：**1 为船尾，Length 为船头**。左右均以面向船头为准。所有河豚布局的总数合计不能超过 2,147,483,647。

### 五面河豚网格

| Grid 字段 | 默认 | 含义 |
| --- | --- | --- |
| `Face` | 必填 | `bottom / stern / bow / left / right`：底、尾、头、左、右 |
| `Slot` | 1 | 船舱 1～Length；前后端面必须为 1 |
| `Lane` | 1 | 横向船位 1～Width；左右船舷必须为 1 |
| `U` | 0 | 底 / 前 / 后的横向网格 −2～2；左右侧面纵向网格 −3～3；正值分别朝左 / 朝船头 |
| `V` | 0 | 底面纵向网格 −3～3，正值朝船头；其他面为非负高度网格，可向上延伸 |
| `Depth` | 0 | 向外分支深度，非负整数 |
| `Outward` | `1b` | 沿外法线堆叠；`0b` 使用兼容旧版的列布局 |
| `Count` | 1 | 该列河豚数量，1～2,147,483,647 |

同一个完整网格坐标不能重复。`Grid` 可与 `SternPuffers`、`Bottom` 同时使用。表中范围为当前命令校验范围；正常右键安装以游戏预览显示的有效格子为准。

### 可复制示例

四节两列的普通桨船，默认压缩放置：

```mcfunction
/longboatlab rig give {Length:4,Width:2,NormalOars:1b,LeftCount:32,RightCount:32,Grid:[{Face:"stern",Lane:1,U:0,V:0,Count:4},{Face:"stern",Lane:2,U:0,V:0,Count:4}]}
```

一条船同时安装不同大小的巨大桨，并在船头舱底部与船尾安装河豚：

```mcfunction
/longboatlab rig give {Length:4,Width:2,Left:[{Slot:1,Size:3d},{Slot:4,Size:5d}],Right:[{Slot:1,Size:3d},{Slot:4,Size:5d}],Grid:[{Face:"bottom",Slot:4,Lane:1,U:0,V:0,Count:6},{Face:"stern",U:0,V:0,Count:4}]}
```

在前方 8 格、上方 2 格直接生成带巨大桨的船：

```mcfunction
/longboatlab rig spawn {Length:4,LeftCount:4,RightCount:4,LeftSize:4d,RightSize:4d,SternPuffers:4,Distance:8d,Height:2d}
```

在开阔水域生成无桨船，测试船头舱底喷引起的抬头：

```mcfunction
/longboatlab rig spawn {Length:4,LeftCount:0,RightCount:0,Grid:[{Face:"bottom",Slot:4,U:0,V:0,Count:9}],Distance:8d,Height:1d}
```

## 诊断与边界

坐在船上执行 `/longboatlab physics` 可查看碰撞诊断。命令范围不代表极端船长或数十亿配件都适合实际游玩；实体生成、巨大桨安装和移动仍受可用空间、已加载区块与有限碰撞预算约束。

`race_supply`、救援准备 / 移动命令由地图数据包使用，普通选手使用发放的右键道具即可。不要把地图内部指令当成通用成品船生成指令。

本页根据当前 [BoatDebugCommand](../src/main/java/com/xc/longboatlab/BoatDebugCommand.java) 和 [RigBoatCommand](../src/main/java/com/xc/longboatlab/RigBoatCommand.java) 核对，示例未在此次文档整理中实际执行。
