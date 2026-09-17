"""Original procedural water recordings: filtered turbulence, slap, bubbles and wash."""
from pathlib import Path
import sys
sys.path.insert(0, str(Path(__file__).resolve().parent / '.audio-deps'))
import numpy as np
import soundfile as sf

OUT = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/longboatlab/sounds'
RATE = 22050
rng = np.random.default_rng(924118)

def noise(seconds, low, high):
    n = int(seconds * RATE)
    f = np.fft.rfftfreq(n, 1 / RATE)
    spectrum = np.fft.rfft(rng.normal(size=n))
    shape = (f / (f + low)) / (1 + (f / high) ** 4)
    signal = np.fft.irfft(spectrum * shape, n=n)
    return signal / max(1e-9, np.std(signal))

def save(name, signal):
    signal *= 0.87 / np.max(np.abs(signal))
    sf.write(OUT / (name + '.ogg'), signal, RATE, format='OGG', subtype='VORBIS')

t = np.arange(RATE * 6) / RATE
# Integer cycles make the steady wash seamless; keep it broad rather than whistling.
wash = noise(6, 90, 2200) * (0.72 + 0.17 * np.sin(2*np.pi*t/2) + 0.11*np.sin(2*np.pi*t/3))
wash += noise(6, 15, 340) * 0.42
save('water_wake', wash)
t = np.arange(int(RATE * 2.4)) / RATE
attack = 1 - np.exp(-t * 180)
slap = noise(2.4, 25, 480) * attack * np.exp(-t*9) * 2.2
slap += noise(2.4, 120, 3000) * attack * np.exp(-t*3.0) * 0.65
slap += noise(2.4, 50, 1400) * (1-np.exp(-t*7)) * np.exp(-t*2.5) * 0.65
for _ in range(28):
    delay = rng.uniform(0.12, 1.4)
    u = np.maximum(0, t-delay)
    f = rng.uniform(260, 850)
    slap += 0.08*np.sin(2*np.pi*(f*u-70*u*u))*np.exp(-u*35)*(t>=delay)
slap *= np.minimum(1, (2.4-t)*5)
save('water_impact', slap)
print('Generated water_wake.ogg (6s) and water_impact.ogg (2.4s)')
