#!/usr/bin/env python3
"""
Generate simple placeholder WAV sound effects for the 3D prototype.

These files are intentionally synthetic and copyright-free.
They are only for development checks until real sound assets exist.
"""

from __future__ import annotations

import math
import struct
import wave
from dataclasses import dataclass
from pathlib import Path


SAMPLE_RATE = 44100
MASTER_VOLUME = 0.42
FADE_SECONDS = 0.008


@dataclass(frozen=True)
class ToneEvent:
    duration: float
    start_hz: float
    end_hz: float
    waveform: str = "sine"
    amplitude: float = 1.0


SOUNDS: dict[str, list[ToneEvent]] = {
    "player_shoot.wav": [
        ToneEvent(0.035, 820, 620, "square", 0.95),
        ToneEvent(0.025, 620, 540, "sine", 0.55),
    ],
    "enemy_shoot.wav": [
        ToneEvent(0.05, 520, 380, "square", 0.9),
        ToneEvent(0.03, 380, 320, "sine", 0.5),
    ],
    "hit.wav": [
        ToneEvent(0.02, 980, 860, "square", 1.0),
        ToneEvent(0.03, 860, 720, "sine", 0.8),
    ],
    "player_splatted.wav": [
        ToneEvent(0.08, 660, 520, "square", 0.9),
        ToneEvent(0.08, 520, 380, "square", 0.8),
        ToneEvent(0.1, 380, 220, "sine", 0.7),
    ],
    "enemy_splatted.wav": [
        ToneEvent(0.07, 760, 600, "square", 0.9),
        ToneEvent(0.07, 600, 440, "square", 0.8),
        ToneEvent(0.09, 440, 260, "sine", 0.7),
    ],
    "countdown_beep.wav": [
        ToneEvent(0.12, 760, 760, "sine", 0.8),
    ],
    "countdown_go.wav": [
        ToneEvent(0.07, 540, 540, "sine", 0.7),
        ToneEvent(0.07, 720, 720, "sine", 0.8),
        ToneEvent(0.12, 980, 980, "square", 0.85),
    ],
    "game_over.wav": [
        ToneEvent(0.12, 560, 460, "sine", 0.75),
        ToneEvent(0.12, 460, 340, "sine", 0.7),
        ToneEvent(0.18, 340, 180, "sine", 0.65),
    ],
    "weapon_switch.wav": [
        ToneEvent(0.04, 660, 660, "square", 0.75),
        ToneEvent(0.05, 880, 880, "square", 0.75),
    ],
    "ink_empty.wav": [
        ToneEvent(0.06, 280, 240, "square", 0.85),
        ToneEvent(0.06, 240, 200, "square", 0.75),
    ],
    "title_bgm.wav": [
        ToneEvent(0.22, 392, 392, "sine", 0.40),
        ToneEvent(0.22, 523, 523, "sine", 0.38),
        ToneEvent(0.22, 659, 659, "sine", 0.40),
        ToneEvent(0.22, 523, 523, "sine", 0.38),
        ToneEvent(0.22, 440, 440, "sine", 0.40),
        ToneEvent(0.22, 587, 587, "sine", 0.38),
        ToneEvent(0.22, 698, 698, "sine", 0.40),
        ToneEvent(0.22, 587, 587, "sine", 0.38),
    ],
    "battle_bgm.wav": [
        ToneEvent(0.14, 196, 196, "square", 0.32),
        ToneEvent(0.14, 247, 247, "square", 0.30),
        ToneEvent(0.14, 294, 294, "square", 0.32),
        ToneEvent(0.14, 247, 247, "square", 0.30),
        ToneEvent(0.14, 220, 220, "square", 0.32),
        ToneEvent(0.14, 277, 277, "square", 0.30),
        ToneEvent(0.14, 330, 330, "square", 0.32),
        ToneEvent(0.14, 277, 277, "square", 0.30),
        ToneEvent(0.18, 196, 196, "sine", 0.18),
        ToneEvent(0.18, 220, 220, "sine", 0.18),
    ],
    "result_bgm.wav": [
        ToneEvent(0.28, 523, 523, "sine", 0.42),
        ToneEvent(0.28, 659, 659, "sine", 0.40),
        ToneEvent(0.28, 784, 784, "sine", 0.42),
        ToneEvent(0.34, 1046, 1046, "sine", 0.44),
        ToneEvent(0.32, 784, 784, "sine", 0.32),
    ],
}


def waveform_value(phase: float, waveform: str) -> float:
    if waveform == "square":
        return 1.0 if math.sin(phase) >= 0 else -1.0
    if waveform == "triangle":
        return 2.0 * abs((phase / math.pi) % 2.0 - 1.0) - 1.0
    return math.sin(phase)


def envelope(progress: float, duration_seconds: float) -> float:
    fade_ratio = min(0.45, FADE_SECONDS / max(0.001, duration_seconds))
    if progress < fade_ratio:
        return progress / fade_ratio
    if progress > 1.0 - fade_ratio:
        return (1.0 - progress) / fade_ratio
    return 1.0


def build_sound(events: list[ToneEvent]) -> bytes:
    samples: list[int] = []

    for event in events:
        sample_count = max(1, int(event.duration * SAMPLE_RATE))
        for index in range(sample_count):
            progress = index / sample_count
            frequency = event.start_hz + (event.end_hz - event.start_hz) * progress
            time_seconds = index / SAMPLE_RATE
            phase = 2.0 * math.pi * frequency * time_seconds
            value = waveform_value(phase, event.waveform)
            value *= event.amplitude * MASTER_VOLUME * envelope(progress, event.duration)
            samples.append(int(max(-1.0, min(1.0, value)) * 32767))

    return struct.pack("<" + "h" * len(samples), *samples)


def write_wav(path: Path, pcm_data: bytes) -> None:
    with wave.open(str(path), "wb") as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        wav_file.writeframes(pcm_data)


def main() -> None:
    repo_root = Path(__file__).resolve().parents[1]
    output_dir = repo_root / "assets" / "audio"
    output_dir.mkdir(parents=True, exist_ok=True)

    for filename, events in SOUNDS.items():
        write_wav(output_dir / filename, build_sound(events))

    print("Generated placeholder audio:")
    for filename in sorted(SOUNDS):
        print(f" - assets/audio/{filename}")


if __name__ == "__main__":
    main()
