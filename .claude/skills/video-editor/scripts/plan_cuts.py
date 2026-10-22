#!/usr/bin/env python3
"""Schlägt Behalten-Bereiche vor: Pausen > max_gap raus, Retakes markiert/entfernt.

Usage: plan_cuts.py <words.json> <keep.json> [--max-gap 0.5] [--pad 0.1]
Das Ergebnis ist ein VORSCHLAG. Transkript gegenlesen und keep.json bei Bedarf anpassen.
"""
import argparse, json, re

REDO = [r"let'?s redo( that)?", r"redo that", r"one more time", r"start (that )?over", r"take two",
        r"noch ?mal( von vorne)?", r"von vorne", r"sorry,? noch ?mal", r"neuer versuch"]

ap = argparse.ArgumentParser()
ap.add_argument("words"); ap.add_argument("out")
ap.add_argument("--max-gap", type=float, default=0.5)
ap.add_argument("--pad", type=float, default=0.1)
a = ap.parse_args()
d = json.load(open(a.words))
segs = d["segments"]

def norm(t): return re.sub(r"[^\wäöüß ]", "", t.lower()).split()

# 1) Satz-Retakes: Segment mit Redo-Phrase raus + das Segment davor (der verworfene Take).
#    Beginnt ein Segment mit denselben ersten 3 Wörtern wie das nächste -> früheres Segment raus.
drop = set()
for i, s in enumerate(segs):
    low = s["text"].lower()
    if any(re.search(p, low) for p in REDO):
        drop.add(i)
        if i > 0: drop.add(i - 1)
    if i + 1 < len(segs):
        a3, b3 = norm(s["text"])[:3], norm(segs[i + 1]["text"])[:3]
        if len(a3) >= 2 and a3 == b3:
            drop.add(i)
removed = [segs[i] for i in sorted(drop)]
dropped_ranges = [(segs[i]["start"], segs[i]["end"]) for i in drop]

def dropped(w):
    mid = (w["start"] + w["end"]) / 2
    return any(s <= mid <= e for s, e in dropped_ranges)

# 2) Wörter behalten, Lücken > max_gap schließen
words = [w for w in d["words"] if not dropped(w)]
keep = []
for w in words:
    st, en = max(0, w["start"] - a.pad), w["end"] + a.pad
    if keep and st - keep[-1][1] <= a.max_gap - 2 * a.pad:
        keep[-1][1] = max(keep[-1][1], en)
    elif keep and st < keep[-1][1]:
        keep[-1][1] = en
    else:
        keep.append([st, en])
keep = [[round(s, 3), round(min(e, d["duration"]), 3)] for s, e in keep]

json.dump({"keep": keep, "removed_retakes": removed}, open(a.out, "w"), ensure_ascii=False, indent=1)
total = sum(e - s for s, e in keep)
print(f"{len(keep)} Bereiche behalten, {total:.1f}s von {d['duration']:.1f}s")
for r in removed:
    print(f"  Retake raus [{r['start']:.2f}-{r['end']:.2f}]: {r['text']}")
