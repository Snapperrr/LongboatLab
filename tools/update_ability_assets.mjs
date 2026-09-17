import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const assets = path.join(root, 'src/main/resources/assets/longboatlab');
const translations = {
  zh_cn: {
    'category.longboatlab': '长船实验室',
    'key.longboatlab.mode': '切换长船 / 压缩形态',
    'key.longboatlab.hook': '发射 / 收回船桨钩爪',
    'key.longboatlab.boost': '河豚氮气加速',
    'key.longboatlab.orbit_up': '锁钩：向上沿球面移动',
    'key.longboatlab.orbit_down': '锁钩：向下沿球面移动',
    'key.longboatlab.orbit_left': '锁钩：向左沿球面移动',
    'key.longboatlab.orbit_right': '锁钩：向右沿球面移动',
    'hud.longboatlab.compressed': '压缩',
    'hud.longboatlab.extended': '长船',
    'hud.longboatlab.status': '%s · %s 份船体 | 河豚 %s | 加速就绪剩余 %s 秒',
    'hud.longboatlab.hook': '船桨链 %s / %s 格 | 按 %s 解锁',
    'tooltip.longboatlab.upgrades': '形态：%s | 船尾河豚：%s',
    'message.longboatlab.no_space': '周围空间不足，无法展开船体。',
    'message.longboatlab.compressed': '已切换为压缩形态：可以弹簧跳跃和使用船桨钩爪。',
    'message.longboatlab.extended': '已切换为长船形态。',
    'message.longboatlab.hook_needs_oars': '至少装一根船桨才能发射钩爪。',
    'message.longboatlab.stern_only': '请蹲下对准船尾安装河豚。',
    'message.longboatlab.puffer_limit': '船尾最多挂 %s 只河豚。',
    'message.longboatlab.puffers': '已挂上河豚：%s / %s'
  },
  en_us: {
    'category.longboatlab': 'Longboat Lab',
    'key.longboatlab.mode': 'Toggle long / compressed hull',
    'key.longboatlab.hook': 'Fire / retract oar grapple',
    'key.longboatlab.boost': 'Pufferfish nitro boost',
    'key.longboatlab.orbit_up': 'Grapple: orbit upward',
    'key.longboatlab.orbit_down': 'Grapple: orbit downward',
    'key.longboatlab.orbit_left': 'Grapple: orbit left',
    'key.longboatlab.orbit_right': 'Grapple: orbit right',
    'hud.longboatlab.compressed': 'Compressed',
    'hud.longboatlab.extended': 'Longboat',
    'hud.longboatlab.status': '%s · %s hull units | Puffers %s | Boost ready in %ss',
    'hud.longboatlab.hook': 'Oar chain %s / %s blocks | %s to release',
    'tooltip.longboatlab.upgrades': 'Mode: %s | Stern puffers: %s',
    'message.longboatlab.no_space': 'Not enough room to extend the hull.',
    'message.longboatlab.compressed': 'Compressed mode: spring jump and oar grapple available.',
    'message.longboatlab.extended': 'Long hull mode activated.',
    'message.longboatlab.hook_needs_oars': 'Attach at least one oar to fire the grapple.',
    'message.longboatlab.stern_only': 'Sneak and aim at the stern to attach a pufferfish.',
    'message.longboatlab.puffer_limit': 'The stern holds at most %s pufferfish.',
    'message.longboatlab.puffers': 'Pufferfish attached: %s / %s'
  }
};
for (const [locale, additions] of Object.entries(translations)) {
  const file = path.join(assets, 'lang', locale + '.json');
  const data = JSON.parse(fs.readFileSync(file, 'utf8'));
  fs.writeFileSync(file, JSON.stringify({...data, ...additions}, null, 2) + '\n');
}
const file = path.join(assets, 'models/entity/oar.bbmodel');
const model = JSON.parse(fs.readFileSync(file, 'utf8'));
const shaft = model.elements.find(cube => cube.name === 'shaft');
shaft.from[0] = -4;
const [dx, dy, dz] = shaft.to.map((v, i) => v - shaft.from[i]);
const [u, v] = shaft.uv_offset;
const faces = {
  north: [u + dz, v + dz, u + dz + dx, v + dz + dy],
  east: [u, v + dz, u + dz, v + dz + dy],
  south: [u + 2 * dz + dx, v + dz, u + 2 * dz + 2 * dx, v + dz + dy],
  west: [u + dz + dx, v + dz, u + 2 * dz + dx, v + dz + dy],
  up: [u + dz, v, u + dz + dx, v + dz],
  down: [u + dz + dx, v, u + dz + 2 * dx, v + dz]
};
for (const [side, uv] of Object.entries(faces)) shaft.faces[side].uv = uv;
fs.writeFileSync(file, JSON.stringify(model, null, 2) + '\n');
