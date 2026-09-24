"""Publish the generated V12 save and verify every copied/archive byte, preserving played saves."""
import gzip
import hashlib
import json
import shutil
import zipfile
from pathlib import Path
import make_mountain_race_save as mountain
from race_nbt import decode

ROOT=Path(__file__).resolve().parents[1]
SOURCE=mountain.OUT.resolve()
FINAL=(ROOT/'Puffer_Rally_Final').resolve()
REPORT=ROOT/'docs/final-map-copy.json'


def files(path):return {p.relative_to(path).as_posix():p for p in path.rglob('*') if p.is_file()}
def hashes(path):return {name:hashlib.sha256(p.read_bytes()).hexdigest() for name,p in files(path).items()}
def pristine(path):
    assert path.is_relative_to(ROOT),path
    assert (path/'.generated-pristine').is_file(),path
    assert not any((path/n).exists() for n in ('session.lock','playerdata','stats','advancements')),path


def archive(path,folder):
    assert path.resolve().is_relative_to(ROOT)
    temp=path.with_suffix('.zip.tmp')
    entries=files(folder)
    with zipfile.ZipFile(temp,'w',zipfile.ZIP_DEFLATED,6) as z:
        for name,p in sorted(entries.items()):z.write(p,folder.name+'/'+name)
    with zipfile.ZipFile(temp) as z:
        assert z.testzip() is None
        assert len(z.namelist())==len(entries)
        for name,p in entries.items():assert z.read(folder.name+'/'+name)==p.read_bytes(),name
    temp.replace(path)


def main():
    pristine(SOURCE)
    wanted=hashes(SOURCE)
    if FINAL.exists():
        pristine(FINAL)
        old=ROOT/json.loads(REPORT.read_text(encoding='utf-8'))['source']
        assert hashes(FINAL) in (hashes(old),wanted), 'Final copy has local changes; refusing to overwrite'
        assert files(FINAL).keys()==files(SOURCE).keys(), 'Final copy has additional files'
    shutil.copytree(SOURCE,FINAL,dirs_exist_ok=True)
    assert hashes(FINAL)==wanted
    # Import a new version only. Never update a world that has been opened in Minecraft.
    free_practice=(SOURCE/'datapacks/puffer_rally/data/puffer_rally/function/free_start.mcfunction').is_file()
    run_name=SOURCE.name+('_Free_Practice' if free_practice else '')
    run=(ROOT/'run/saves'/run_name).resolve()
    suffix=2
    while run.exists() and (any((run/n).exists() for n in ('session.lock','playerdata','stats','advancements'))
                            or not (run/'.generated-pristine').is_file()):
        run=(ROOT/'run/saves'/(run_name+'_'+str(suffix))).resolve();suffix+=1
    if run.exists():
        pristine(run);assert hashes(run)==wanted,'Existing V12 differs; refusing to overwrite'
    else:shutil.copytree(SOURCE,run)
    assert hashes(run)==wanted
    archive(SOURCE.parent/(SOURCE.name+'.zip'),SOURCE)
    archive(ROOT/'Puffer_Rally_Final.zip',FINAL)
    workspace_zip=ROOT.parent/'Puffer_Rally_Final.zip'
    shutil.copyfile(ROOT/'Puffer_Rally_Final.zip',workspace_zip)
    assert workspace_zip.read_bytes()==(ROOT/'Puffer_Rally_Final.zip').read_bytes()
    data=decode(gzip.decompress((SOURCE/'level.dat').read_bytes()))['Data'][1]
    report=dict(source=SOURCE.relative_to(ROOT).as_posix(),copy='Puffer_Rally_Final',archive='Puffer_Rally_Final.zip',
                map_revision='V12 + free practice' if free_practice else 'V12',
                recommended_mod_version='0.10.20' if free_practice else '0.10.17',minecraft='1.21.1',
                free_practice=free_practice,
                level_name=data['LevelName'][1],data_version=data['DataVersion'][1],files=len(wanted),
                regions=len(list((SOURCE/'region').glob('*.mca'))),save_bytes=sum(p.stat().st_size for p in files(SOURCE).values()),
                archive_bytes=(ROOT/'Puffer_Rally_Final.zip').stat().st_size,
                archive_sha256=hashlib.sha256(workspace_zip.read_bytes()).hexdigest(),
                every_file_matches_source=True,every_zip_entry_matches_copy=True,zip_crc_checked=True,
                bundled_race_datapack=True,contains_playerdata=False,runtime_started=False,
                development_save=run.relative_to(ROOT).as_posix(),workspace_archive=str(workspace_zip))
    REPORT.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(report,ensure_ascii=True,indent=2))


if __name__=='__main__':main()
