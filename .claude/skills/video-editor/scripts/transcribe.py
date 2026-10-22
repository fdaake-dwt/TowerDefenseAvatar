#!/usr/bin/env python3
"""Transkribiert ein Video/Audio mit faster-whisper und speichert jedes Wort mit Zeitstempel.

Usage: transcribe.py <input> <out.json> [--model small] [--language de]
"""
import argparse, json
from faster_whisper import WhisperModel

ap = argparse.ArgumentParser()
ap.add_argument("input")
ap.add_argument("out")
ap.add_argument("--model", default="small")
ap.add_argument("--language", default=None)
a = ap.parse_args()

model = WhisperModel(a.model, device="auto", compute_type="int8")
segments, info = model.transcribe(a.input, language=a.language, word_timestamps=True, vad_filter=False)

words, segs = [], []
for s in segments:
    segs.append({"start": round(s.start, 3), "end": round(s.end, 3), "text": s.text.strip()})
    for w in s.words or []:
        words.append({"start": round(w.start, 3), "end": round(w.end, 3), "word": w.word.strip()})

json.dump({"language": info.language, "duration": round(info.duration, 3), "segments": segs, "words": words},
          open(a.out, "w"), ensure_ascii=False, indent=1)
for s in segs:
    print(f"[{s['start']:7.2f} - {s['end']:7.2f}] {s['text']}")
print(f"\n{len(words)} Wörter, Sprache: {info.language}, Dauer: {info.duration:.1f}s -> {a.out}")
