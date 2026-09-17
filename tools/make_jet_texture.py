"""Generate a soft translucent plume without an opaque core or saturated rim."""
from pathlib import Path
from PIL import Image

target = Path(__file__).resolve().parents[1] / "src/main/resources/assets/longboatlab/textures/particle/jet.png"
texture = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
for y in range(32):
    for x in range(32):
        radius = (((x - 15.5) / 15.5) ** 2 + ((y - 15.5) / 15.5) ** 2) ** 0.5
        opacity = round(225 * max(0, 1 - radius * radius) ** 1.25)
        texture.putpixel((x, y), (205, 238, 248, opacity))
texture.save(target)
