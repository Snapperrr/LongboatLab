"""Offline datapack guard scenarios. This does not start Minecraft or parse Brigadier.

Evaluate the actual generated entry/supply commands with a small scoreboard/tag
interpreter, checking lobby contention, duplicate entry, repeat rewards and exit.
"""
from pathlib import Path
import argparse
import json
import re


class Returned(Exception):
    pass


class Session:
    def __init__(self, functions, phase=0, free=False, other=False):
        self.functions=functions
        self.players={'self':{'tags':{'lr_tools'},'scores':{'lr_place':0,'lr_epoch':5}}}
        if free:self.players['self']['tags'].update(('lr_free','lr_racer'))
        if other:self.players['other']={'tags':{'lr_racer','lr_free'} if phase==4 else {'lr_racer','lr_a'},'scores':{}}
        self.fake={'#phase':{'lr_state':phase},'#round':{'lr_epoch':5},'#granted':{'lr_tmp':1}}
        self.trace=[]
        self.grant=1
        self.closed=False

    def selection(self,selector):
        m=re.fullmatch(r'(@[sae])(?:\[(.*)\])?',selector)
        assert m,selector
        base,terms=m.groups()
        if base=='@e':return ['boat'] if self.closed else []
        result=['self'] if base=='@s' else list(self.players)
        for sign,tag in re.findall(r'tag=(!?)([^,\]]+)',terms or ''):
            result=[p for p in result if ((tag in self.players[p]['tags']) != bool(sign))]
        for key,span in re.findall(r'(lr_\w+)=(-?[\d.]+)',terms or ''):
            result=[p for p in result if self.matches(self.players[p]['scores'].get(key),span)]
        return result

    @staticmethod
    def matches(value,span):
        if value is None:return False
        if '..' not in span:return value==int(span)
        lo,hi=span.split('..')
        return (not lo or value>=int(lo)) and (not hi or value<=int(hi))

    def scores(self,target):
        return self.players['self']['scores'] if target=='@s' else self.fake.setdefault(target,{})

    def function(self,name):
        self.trace.append(name)
        # Only these functions implement the guards being tested. Rendering,
        # spawn, inventory and rescue operations are terminal observations here.
        if name not in {'use_free','free_busy','use_rescue','use_boat','free_rescue','free_replace',
                        'supply_0_oars','use_leave','leave'}:return
        try:
            for line in self.functions[name]:self.command(line)
        except Returned:pass

    def command(self,line):
        if line.startswith('execute '):
            conditions,action=line[8:].split(' run ',1)
            tokens=conditions.split();i=0;valid=True
            while i<len(tokens):
                token=tokens[i]
                if token in ('if','unless'):
                    invert=token=='unless';kind=tokens[i+1]
                    if kind=='entity':
                        result=bool(self.selection(tokens[i+2]));i+=3
                    elif kind=='score':
                        a=self.scores(tokens[i+2]).get(tokens[i+3]);op=tokens[i+4]
                        if op=='matches':result=self.matches(a,tokens[i+5]);i+=6
                        else:
                            b=self.scores(tokens[i+5]).get(tokens[i+6]);result=a is not None and b is not None and a==b;i+=7
                    else:raise AssertionError(conditions)
                    valid &= result != invert
                elif token=='positioned':i+=4
                elif token=='store':
                    assert tokens[i+1:i+3]==['result','score']
                    target,objective=tokens[i+3:i+5];i+=5
                    if valid:self.scores(target)[objective]=self.grant
                else:raise AssertionError(conditions)
            if valid:self.command(action)
        elif line.startswith('return'):
            if line.startswith('return run '):self.command(line[11:])
            raise Returned()
        elif line.startswith('function puffer_rally:'):
            self.function(line.split(':',1)[1])
        elif line.startswith('tag '):
            _,selector,action,tag=line.split()
            for player in self.selection(selector):
                if action=='add':self.players[player]['tags'].add(tag)
                else:self.players[player]['tags'].discard(tag)
        elif line.startswith('scoreboard players '):
            words=line.split();action,target,key=words[2:5];scores=self.scores(target)
            if action=='set':scores[key]=int(words[5])
            elif action=='add':scores[key]=scores.get(key,0)+int(words[5])
            elif action=='reset':scores.pop(key,None)
            elif action=='operation':scores[key]=self.scores(words[6]).get(words[7],0)
            else:raise AssertionError(line)
        else:self.trace.append(line)


def check(save):
    folder=save/'datapacks/puffer_rally/data/puffer_rally/function'
    functions={p.stem:p.read_text(encoding='utf-8').splitlines() for p in folder.glob('*.mcfunction')}
    lines=0
    for name,body in functions.items():
        for line in body:
            lines+=1
            assert '\ufffd' not in line,(name,line)
            for ref in re.findall(r'\bfunction puffer_rally:([a-z0-9_/]+)',line):assert ref in functions,(name,ref)
            for a,b in [('(',')'),('[',']'),('{','}')]:assert line.count(a)==line.count(b),(name,line)
            match=re.search(r'(?:tellraw @\S+|title @\S+ (?:title|subtitle|actionbar)) (\{.*|\[.*)$',line)
            if match:json.loads(match[1])
    scenarios=0
    for phase,free,other,expected in [
        (0,False,False,True),(0,False,True,False),
        (1,False,False,False),(2,False,False,False),(3,False,False,False),
        (4,False,True,True),(4,True,True,False)]:
        s=Session(functions,phase,free,other);s.function('use_free')
        assert ('free_start' in s.trace)==expected,(phase,free,other,s.trace)
        assert 'lr_free_joining' not in s.players['self']['tags'];scenarios+=1
    s=Session(functions);s.closed=True;s.function('use_free')
    assert 'free_start' not in s.trace;scenarios+=1
    # One player may change from waiting on a READY pad to free practice.
    s=Session(functions);s.players['self']['tags'].update(('lr_racer','lr_a'));s.function('use_free')
    assert 'free_start' in s.trace;scenarios+=1
    for phase,free,used,cooldown,room,expected in [
        (2,False,False,0,1,True),(2,False,True,0,1,False),
        (4,True,True,0,1,True),(4,True,True,20,1,False),
        (4,True,True,0,0,False),(0,False,False,0,1,False)]:
        s=Session(functions,phase,free);s.players['self']['tags'].add('lr_racer')
        scores=s.players['self']['scores'];scores['lr_p0_oars']=5 if used else 4;scores['lr_fstock']=cooldown
        s.grant=room;s.function('supply_0_oars')
        granted=any('tellraw' in line for line in s.trace)
        assert granted==expected,(phase,free,used,cooldown,room,s.trace)
        if granted and free:assert scores['lr_fstock']==20
        if not room:assert scores['lr_fstock']==cooldown
        scenarios+=1
    for other in (False,True):
        s=Session(functions,4,True,other);s.function('leave')
        assert 'lr_free' not in s.players['self']['tags'] and 'lr_racer' not in s.players['self']['tags']
        assert s.fake['#phase']['lr_state']==(4 if other else 0)
        assert s.fake['#round']['lr_epoch']==(5 if other else 6)
        scenarios+=1
    for action,expected in [('use_rescue','free_rescue'),('use_boat','free_replace')]:
        s=Session(functions,4,True);s.function(action)
        assert expected in s.trace
        assert not any('add @s lr_time' in line for line in s.trace)
        scenarios+=1
    for name in ('race_tick','navigation_tick','off_course'):
        assert functions[name][0]=='execute if score #phase lr_state matches 4 run return 0'
    assert not any('lr_time' in line or 'lr_place' in line for line in functions['free_return_ready'])
    assert any('tag=lr_free_entity' in line for line in functions['scene_tick'])
    result={'functions':len(functions),'command_lines':lines,'guard_scenarios':scenarios,
            'java_compiled':False,'minecraft_started':False,'commands_parsed_by_minecraft':False}
    print(json.dumps(result,indent=2))
    return result


if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('save',type=Path);args=parser.parse_args()
    check(args.save)
