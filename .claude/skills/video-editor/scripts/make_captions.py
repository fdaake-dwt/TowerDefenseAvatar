#!/usr/bin/env python3
"""Erzeugt Social-Media-Captions (.ass) aus words.json: 2-4 Wörter pro Zeile, aktives Wort in Markenfarbe.

Usage: make_captions.py <words.json> <out.ass> --w 1080 --h 1920 --font "Inter" --primary "#FF5A1F" --accent "#1E1E2E"
Einbrennen: ffmpeg -i in.mp4 -vf "ass=out.ass" ...
"""
import argparse, json

ap = argparse.ArgumentParser()
ap.add_argument("words"); ap.add_argument("out")
ap.add_argument("--w", type=int, default=1080); ap.add_argument("--h", type=int, default=1920)
ap.add_argument("--font", default="Inter")
ap.add_argument("--primary", default="#FFD400", help="Farbe des aktiven Worts")
ap.add_argument("--accent", default="#000000", help="Kontur/Box-Farbe")
ap.add_argument("--max-words", type=int, default=3)
ap.add_argument("--upper", action="store_true", help="Großbuchstaben")
a = ap.parse_args()

def ass_col(hexc):  # #RRGGBB -> &H00BBGGRR
    h = hexc.lstrip("#"); return f"&H00{h[4:6]}{h[2:4]}{h[0:2]}".upper()

def ts(t):
    h, r = divmod(max(t, 0), 3600); m, s = divmod(r, 60)
    return f"{int(h)}:{int(m):02d}:{s:05.2f}"

vertical = a.h > a.w
size = round(a.h * (0.058 if vertical else 0.06))
marginv = round(a.h * (0.28 if vertical else 0.08))
hdr = f"""[Script Info]
ScriptType: v4.00+
PlayResX: {a.w}
PlayResY: {a.h}
WrapStyle: 2
ScaledBorderAndShadow: yes

[V4+ Styles]
Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
Style: Cap,{a.font},{size},&H00FFFFFF,&H00FFFFFF,{ass_col(a.accent)},&H80000000,1,0,0,0,100,100,0,0,1,{max(3, size // 12)},2,2,60,60,{marginv},1

[Events]
Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
"""
words = json.load(open(a.words))["words"]
# Gruppen bilden: max N Wörter, neue Gruppe bei Pause > 0.4 s oder Satzende
groups, cur = [], []
for w in words:
    if cur and (len(cur) >= a.max_words or w["start"] - cur[-1]["end"] > 0.4 or cur[-1]["word"][-1:] in ".?!"):
        groups.append(cur); cur = []
    cur.append(w)
if cur: groups.append(cur)

lines = []
hi = ass_col(a.primary)
for gi, g in enumerate(groups):
    g_end = groups[gi + 1][0]["start"] if gi + 1 < len(groups) and groups[gi + 1][0]["start"] - g[-1]["end"] < 0.4 else g[-1]["end"] + 0.15
    for i, w in enumerate(g):
        st = w["start"]; en = g[i + 1]["start"] if i + 1 < len(g) else g_end
        txt = []
        for j, x in enumerate(g):
            t = x["word"].upper() if a.upper else x["word"]
            txt.append(f"{{\\c{hi}}}{t}{{\\c&H00FFFFFF&}}" if j == i else t)
        pop = r"{\fscx108\fscy108\t(0,80,\fscx100\fscy100)}" if i == 0 else ""
        lines.append(f"Dialogue: 0,{ts(st)},{ts(en)},Cap,,0,0,0,,{pop}{' '.join(txt)}")
open(a.out, "w").write(hdr + "\n".join(lines) + "\n")
print(f"{len(groups)} Caption-Zeilen -> {a.out}")
