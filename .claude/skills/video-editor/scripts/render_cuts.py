#!/usr/bin/env python3
"""Schneidet die Bereiche aus keep.json aus dem Video und fügt sie nahtlos zusammen (höchste Qualität).

Usage: render_cuts.py <input> <keep.json> <out.mp4> [--vf "<extra video filter>"]
"""
import argparse, json, subprocess

ap = argparse.ArgumentParser()
ap.add_argument("input"); ap.add_argument("keep"); ap.add_argument("out")
ap.add_argument("--vf", default=None, help="z. B. Crop/Scale/Grading nach dem Concat")
a = ap.parse_args()
keep = json.load(open(a.keep))["keep"]

parts, labels = [], ""
for i, (s, e) in enumerate(keep):
    parts.append(f"[0:v]trim={s}:{e},setpts=PTS-STARTPTS[v{i}];"
                 f"[0:a]atrim={s}:{e},asetpts=PTS-STARTPTS,afade=t=in:d=0.01,afade=t=out:st={max(0, e - s - 0.01):.3f}:d=0.01[a{i}];")
    labels += f"[v{i}][a{i}]"
fc = "".join(parts) + f"{labels}concat=n={len(keep)}:v=1:a=1[vc][a]"
fc += f";[vc]{a.vf}[v]" if a.vf else ";[vc]null[v]"

cmd = ["ffmpeg", "-y", "-i", a.input, "-filter_complex", fc, "-map", "[v]", "-map", "[a]",
       "-c:v", "libx264", "-preset", "slow", "-crf", "16", "-profile:v", "high", "-pix_fmt", "yuv420p",
       "-c:a", "aac", "-b:a", "320k", "-ar", "48000", "-movflags", "+faststart", a.out]
subprocess.run(cmd, check=True)
print("->", a.out)
