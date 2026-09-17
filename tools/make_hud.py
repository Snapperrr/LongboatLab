"""Draw the HUD atlas as deliberately aligned pixel art; stdlib only, no external images."""
from pathlib import Path
import struct
import zlib

W = H = 256
pixels = bytearray(W * H * 4)

def rect(x, y, w, h, color):
    rgba = bytes.fromhex(color)
    for py in range(y, y + h):
        for px in range(x, x + w):
            offset = (py * W + px) * 4
            pixels[offset:offset + 4] = rgba

# Stepped wooden frame, teal enamel inset, rivets and engraved separations.
rect(2, 0, 204, 118, '362b26f4')
rect(0, 2, 208, 114, '362b26f4')
rect(2, 2, 204, 114, 'ab8050ff')
rect(4, 4, 200, 110, '526365ff')
rect(5, 5, 198, 108, '182932f4')
rect(6, 6, 196, 17, '30424bff')
for y in (24, 51, 71, 90):
    rect(6, y, 196, 1, '40514aff')
for x in (2, 202):
    for y in (2, 112):
        rect(x, y, 3, 3, 'efd3a0ff')
        rect(x + 1, y + 1, 1, 1, '684a36ff')
for x in range(11, 195, 19):
    rect(x, 1, 11, 1, 'caa16aff')
    rect(x, 116, 8, 1, '684a36ff')

palette = {
    '.': None, '#': '17242eff', 'w': 'c99457ff', 'W': 'f1cb82ff',
    'b': '61b7cdff', 'B': 'ade9e5ff', 'y': 'eebf4fff', 'Y': 'ffe7a1ff',
    'g': '82bb7dff', 'G': 'c2ef9bff', 'r': 'e98365ff', 's': '899b9dff',
}
icons = [
    [ # Long hull in three quarters view.
        '................','................','...WWWWWWWWWW...',
        '..WwwwwwwwwwwW..','.Ww##########wW.','#wwWWWWWWWWWWww#',
        '.#wwwwwwwwwwww#.','..#wwwwwwwwww#..','...##########...',
        '..b..b..b..b....','.bBBbBBbBBbBBb..','................',
    ],
    [ # Folded layers.
        '................','.....WWWWWW.....','....WwwwwwwW....',
        '...#wwwwwwww#...','....########....','....WWWWWWWW....',
        '...#wwwwwwww#...','....########....','....WWWWWWWW....',
        '...#wwwwwwww#...','....########....','...bBBbBBbBBb...',
    ],
    [ # Port oar.
        '............WW..','...........Ww...','..........Ww....',
        '.........Ww.....','........Ww......','.......Ww.......',
        '......Ww........','...WWWw.........','..WwwwW.........',
        '.WwwwwW.........','..WwwW..........','...WW...........',
    ],
    [ # Starboard oar.
        '..WW............','...wW...........','....wW..........',
        '.....wW.........','......wW........','.......wW.......',
        '........wW......','.........wWWW...','.........WwwwW..',
        '.........WwwwwW.','..........WwwW..','...........WW...',
    ],
    [
        '......y..y......','....yYYYYYYy....','...yYYYYYYYYy...',
        '..yYY#YYYY#YYy..','.yYYY#YYYY#YYYy.','..yYYYYYYYYYYy..',
        'byYYyYYYYYYyYYyb','..yYYYY##YYYYy..','..yYYYYYYYYYYy..',
        '...yYYYYYYYYy...','....yYYYYYYy....','......y..y......',
    ],
    [ # Wooden grappling claw.
        '.......WW.......','.......ww.......','.......ww.......',
        '.......ww.......','...W...ww...W...','..Ww...ww...wW..',
        '..Ww...ww...wW..','..Ww...ww...wW..','...Ww..ww..wW...',
        '....WwwwwwwW....','.....WWwwWW.....','.......WW.......',
    ],
    [
        '.......YY.......','......YY........','.....YY.........',
        '....YY..........','...YYYYYYYYYY...','..YYYYYYYYYY....',
        '.......YYY......','......YYY.......','.....YYY........',
        '....YYY.........','...YY...........','..Y.............',
    ],
    [ # Spring.
        '.....GGGGGG.....','....GggggggG....','.....GGGGGG.....',
        '.......ss.......','....ssssssss....','.......ss.......',
        '....ssssssss....','.......ss.......','....ssssssss....',
        '.......ss.......','...WWWWWWWWWW...','....wwwwwwww....',
    ],
]
for i, art in enumerate(icons):
    for y, line in enumerate(art):
        for x, symbol in enumerate(line):
            if palette[symbol]: rect(i * 16 + x, 130 + y, 1, 1, palette[symbol])

def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind + data))

scanlines = b''.join(b'\0' + pixels[y * W * 4:(y + 1) * W * 4] for y in range(H))
png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>2I5B', W, H, 8, 6, 0, 0, 0))
png += chunk(b'IDAT', zlib.compress(scanlines, 9)) + chunk(b'IEND', b'')
output = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/longboatlab/textures/gui/boat_hud.png'
output.parent.mkdir(parents=True, exist_ok=True)
output.write_bytes(png)
