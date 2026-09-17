"""Reproducible continuous-alpha water masks; Pillow only, no external artwork."""
from pathlib import Path
import math
import random
from PIL import Image, ImageFilter

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/longboatlab/textures/water'
ROOT.mkdir(parents=True, exist_ok=True)
rng = random.Random(63041)


def noise(size, coarse):
    im = Image.new('L', (coarse, coarse))
    im.putdata([rng.randrange(256) for _ in range(coarse * coarse)])
    return im.resize((size, size), Image.Resampling.BICUBIC).filter(ImageFilter.GaussianBlur(0.6))


def save(name, paint, size=128):
    broad, fine = noise(size, 12), noise(size, 48)
    im = Image.new('RGBA', (size, size))
    for y in range(size):
        for x in range(size):
            u, v = (x + .5) / size, (y + .5) / size
            n, f = broad.getpixel((x, y)) / 255, fine.getpixel((x, y)) / 255
            rgb, alpha = paint(u, v, n, f)
            im.putpixel((x, y), (*rgb, round(max(0, min(255, alpha)))))
    im.save(ROOT / f'{name}.png')


def sheet(u, v, n, f):
    streak = (math.sin(u * 56 + math.sin(v * 8) * 1.5) * .5 + .5) ** 7
    edge = min(1, u * 16, (1 - u) * 16)
    crest = max(0, 1 - v * 6)
    # Keep a continuous water body. Breakup belongs to the moving mesh, not large invisible texture holes.
    alpha = (200 + 25 * n + 20 * streak + 10 * crest) * edge
    value = round(225 + 30 * max(streak, crest))
    return (value, min(255, value + 12), 255), alpha


def foam(u, v, n, f):
    band = math.sin(math.pi * v) ** 0.7
    bubbles = max(0, 1 - abs(f - .5) * 6)
    patch = .80 + .20 * n
    return (245, 252, 255), 255 * band * patch * (.78 + .22 * bubbles)


def mist(u, v, n, f):
    r = math.hypot((u - .5) * 2, (v - .5) * 2)
    return (248, 253, 255), 180 * max(0, 1 - r * r) ** 2 * (.45 + .55 * n)


def drop(u, v, n, f):
    r = math.hypot((u - .5) * 2.2, (v - .5) * 2)
    edge = min(1, max(0, (1 - r) * 10))
    shine = math.exp(-((u - .36) ** 2 * 130 + (v - .33) ** 2 * 65))
    return (round(170 + 85 * shine), round(215 + 40 * shine), 255), edge * (165 + 90 * shine)


for name, painter in [('sheet', sheet), ('foam', foam), ('mist', mist), ('drop', drop)]:
    save(name, painter)
