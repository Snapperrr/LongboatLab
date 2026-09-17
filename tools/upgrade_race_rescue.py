"""Upgrade checkpoint functions only; preserve played terrain, inventory and race progress."""
from pathlib import Path
import argparse
import re
import shutil


def upgrade(save):
    directory=save/'datapacks/puffer_rally/data/puffer_rally/function'
    if not directory.is_dir():return
    changed={}
    for path in directory.glob('return_*.mcfunction'):
        if not re.fullmatch(r'return_\d+',path.stem):continue
        old=path.read_text(encoding='utf-8')
        if 'longboatlab race_prepare' in old:continue
        match=re.search(r'^execute if score #mounted lr_tmp matches 1 on vehicle run tp @s (\S+) (\S+) (\S+) (\S+) 0$',old,re.M)
        if not match:raise ValueError('Unrecognized checkpoint function: '+str(path))
        x,y,z,yaw=match.groups();index=path.stem.split('_')[1]
        lines=['execute unless entity @s[tag=lr_rescue_pending,tag=lr_racer] run return 0',
               'execute if entity @s[tag=lr_replace_pending] run ride @s dismount',
               'scoreboard players add @s lr_time 200',
               'execute if entity @s[tag=lr_replace_pending] run scoreboard players add @s lr_time 200']
        for line in old.splitlines():
            if 'on vehicle run data merge entity' in line:continue
            if line==match.group(0):line=f'execute if score #mounted lr_tmp matches 1 on vehicle run longboatlab race_move {x} {y} {z} {yaw}'
            lines.append(line)
        changed[f'return_ready_{index}.mcfunction']='\n'.join(lines)+'\n'
        changed[path.name]=f'longboatlab race_prepare {x} {y} {z} {index}\n'
    if not changed:
        print('Already current:',save.name);return
    rescue=(directory/'rescue.mcfunction').read_text(encoding='utf-8').splitlines()
    changed['rescue.mcfunction']='\n'.join(['execute if entity @s[tag=lr_rescue_pending] run return 0']+
        [line for line in rescue if 'run function puffer_rally:return_' in line])+'\n'
    changed['replace.mcfunction']='\n'.join(['execute if entity @s[tag=lr_rescue_pending] run return 0',
        'tag @s add lr_replace_pending','function puffer_rally:rescue'])+'\n'
    changed['leave.mcfunction']='tag @s remove lr_rescue_pending\ntag @s remove lr_replace_pending\n'+(directory/'leave.mcfunction').read_text(encoding='utf-8')
    race=(directory/'race_tick.mcfunction').read_text(encoding='utf-8')
    changed['race_tick.mcfunction']=race.replace('@a[tag=lr_racer,scores={lr_cp=',
                                                               '@a[tag=lr_racer,tag=!lr_rescue_pending,scores={lr_cp=')
    backup=save/'longboat_rescue_backup_before_0.10.7'
    backup.mkdir(exist_ok=True)
    for name,text in changed.items():
        path=directory/name
        if path.exists() and not (backup/name).exists():shutil.copy2(path,backup/name)
        path.write_text(text,encoding='utf-8')
    print('Updated rescue functions:',save.name,len(changed))


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('saves',type=Path,nargs='+');args=parser.parse_args()
    for save in args.saves:upgrade(save.resolve())
