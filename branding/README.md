# Longboat Lab icon

Use `longboatlab-icon-512.png` for the Modrinth project icon, or
`longboatlab-icon-1024.png` when a larger source is useful. Smaller 256, 128,
and 64 pixel versions are included, together with a transparent foreground.
The project root's `longboatlab-icon.png` is the same 1024 pixel artwork.

The composition uses the project's `continuous_hull.bbmodel`, `oar.bbmodel`,
and oak texture, with two inflated pufferfish, oversized oars, and a blue wake.
The fish body uses Minecraft 1.21.1's pufferfish texture; eyes and spikes are
rendered as separate geometry. These are orthographic asset renders rather
than in-game screenshots. No generative image service was used.

The 256 pixel version is also installed at
`src/main/resources/assets/longboatlab/icon.png` and referenced by
`fabric.mod.json`.

To regenerate from the project directory:

```powershell
python tools/make_mod_icon.py
```

The renderer requires Python, NumPy, Pillow, and the local Fabric Loom
Minecraft 1.21.1 client jar. Its preview sheet is written to
`build/icon-work/preview.png`. It does not compile or launch the mod.
