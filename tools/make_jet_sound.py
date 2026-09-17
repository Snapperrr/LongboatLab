"""Generate the bundled seamless mono jet loop. Requires numpy and soundfile; no build dependency."""
from pathlib import Path
import sys
local_deps = Path(__file__).resolve().parent / ".audio-deps"
if local_deps.is_dir():
    sys.path.insert(0, str(local_deps))
import numpy as np
import soundfile as sf

rate = 22050
frames = rate * 4
rng = np.random.default_rng(74219)
frequency = np.fft.rfftfreq(frames, 1 / rate)
spectrum = np.fft.rfft(rng.normal(size=frames))
# Filtering in the Fourier domain keeps both ends of the loop periodic.
# Broad warm air rush: attenuate the shrill 2-6 kHz band and remove the old tonal whistle.
shape = (frequency / (frequency + 65)) / (1 + (frequency / 1050) ** 4)
noise = np.fft.irfft(spectrum * shape, n=frames)
time = np.arange(frames) / rate
noise *= 0.91 + 0.055 * np.sin(2 * np.pi * 0.75 * time) + 0.035 * np.sin(2 * np.pi * 1.25 * time)
noise *= 0.58 / np.max(np.abs(noise))
target = Path(__file__).resolve().parents[1] / "src/main/resources/assets/longboatlab/sounds/puffer_jet.ogg"
target.parent.mkdir(parents=True, exist_ok=True)
sf.write(target, noise, rate, format="OGG", subtype="VORBIS")
print(target)
