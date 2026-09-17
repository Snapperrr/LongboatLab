"""Migrate race display functions without replacing played worlds or resetting scores."""
from pathlib import Path
import argparse
import shutil
import make_mountain_race_save as mountain


def upgrade(save):
    folder=save/'datapacks/puffer_rally/data/puffer_rally/function'
    if not folder.is_dir():return
    source=mountain.OUT/'datapacks/puffer_rally/data/puffer_rally/function'
    changed={}
    for name in ('display_setup','display_time','use_help'):
        changed[name+'.mcfunction']=(source/(name+'.mcfunction')).read_text(encoding='utf-8')
    for name,command in (('load','function puffer_rally:display_setup'),
                         ('tick','execute as @a[tag=lr_racer] run function puffer_rally:display_time')):
        text=(folder/(name+'.mcfunction')).read_text(encoding='utf-8')
        changed[name+'.mcfunction']=text if command in text else text.rstrip()+'\n'+command+'\n'
    pairs={
      '已就位，另一位玩家就位后自动倒计时；右键绿宝石可单人练习。':'已就位，等待对手。绿宝石：单练。',
      '单格船起航：2个河豚桶、4把木铲、1把重锤；首格空手。补给每人独立领取。':'准备起航！沿黄色箭头前进。',
      '请先腾出6个背包空位，再领取整份补给。':'需要6个背包空位。',
      '已领取本站个人补给。其他玩家可独立领取。':'补给已领取。',
      '已回到检查点，保留船只改装；罚时10秒。':'已救援 · +10秒',
      '已更换基础船并返回检查点；罚时20秒。':'已换船 · +20秒',
      '偏离赛道：沿黄色箭头返回，或右键指南针救援':'偏航 · 沿黄色箭头返回'}
    for path in folder.glob('*.mcfunction'):
        text=changed.get(path.name,path.read_text(encoding='utf-8'));old=text
        for before,after in pairs.items():text=text.replace(before,after)
        if path.stem=='init':
            lines=[line for line in text.splitlines() if 'setdisplay sidebar lr_time' not in line and 'displayname' not in line]
            text='\n'.join(lines)+'\nfunction puffer_rally:display_setup\n'
        if path.stem=='leave' and 'scoreboard players reset @s lr_sec' not in text:
            text+='scoreboard players reset @s lr_sec\n'
        if text!=old:changed[path.name]=text
    backup=save/'longboat_captions_backup_before_0.10.9';backup.mkdir(exist_ok=True)
    for name,text in changed.items():
        path=folder/name
        if path.exists() and path.read_text(encoding='utf-8')==text:continue
        if path.exists() and not (backup/name).exists():shutil.copy2(path,backup/name)
        path.write_text(text,encoding='utf-8')
    print('Updated race captions:',save.name)


if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('saves',type=Path,nargs='+');args=p.parse_args()
    for save in args.saves:upgrade(save.resolve())
