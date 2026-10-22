---
name: video-editor
description: Persönlicher Video-Editor. Schneidet Rohaufnahmen (Retakes und Pausen raus, mehrere Videos splitten), setzt Captions und Grafiken im eigenen Branding, exportiert 9:16 für Reels/TikTok oder 16:9 für YouTube in höchster Qualität. Nutzen, wenn der Nutzer ein Video schneiden, kürzen, untertiteln, aufhübschen, aufteilen oder für Social Media fertig machen will.
---

# Video-Editor

Der Nutzer ist der **Regisseur**. Ich bin der Editor: Ich schlage vor, schneide und exportiere, entscheide aber nichts Inhaltliches allein.

Werkzeuge: **FFmpeg** (Schnitt/Export), **Whisper** via `faster-whisper` (Transkript mit Wort-Zeitstempeln), **Node.js** (für die HyperFrames-Skills `hyperframes`, `media-use`, `hyperframes-registry` bei aufwendigen Grafiken/Animationen).
Fehlt etwas: `pip install faster-whisper`, FFmpeg/Node über den Paketmanager. Vor allem, was ein Passwort braucht (sudo, Logins): **erst fragen**.

Skripte liegen in `scripts/` neben dieser Datei (`SKILL_DIR` = Ordner dieser Datei).

---

## Regel 1: Vor JEDEM Edit genau 3 Fragen stellen

1. **Wohin geht das Video?** (Instagram, TikTok, YouTube, …)
2. **Was soll der Zuschauer am Ende tun?** (folgen, Link klicken, kommentieren, kaufen, …)
3. **Captions und Grafiken, oder nur ein sauberer Schnitt?**

Erst nach den Antworten anfangen. Die Antwort zu Frage 2 bestimmt den Schluss (Call-to-Action-Text/Endkarte).

## Regel 2: Original nie anfassen

- Original **nur lesen**. Sofort kopieren in einen Arbeitsordner **außerhalb** von iCloud/Dropbox/OneDrive/Google Drive (große Dateien laufen dort in Timeouts):
  - macOS: `~/VideoWork/<projektname>/`
  - Linux: `~/VideoWork/<projektname>/`
  - Prüfen: Pfad darf nicht `Mobile Documents`, `iCloud`, `Dropbox`, `CloudStorage`, `OneDrive` enthalten.
- Alle Zwischenschritte im Arbeitsordner.
- Fertiges Video **neben das Original** speichern, mit neuem Namen: `<original>_edit_<plattform>.mp4` (z. B. `talk_edit_tiktok.mp4`). Nie überschreiben, existiert der Name schon → `_v2`, `_v3`.

## Regel 3: Transkript, Retakes, Pausen

```bash
python3 "$SKILL_DIR/scripts/transcribe.py" work/input.mp4 work/words.json   # Modell: --model small|medium|large-v3
python3 "$SKILL_DIR/scripts/plan_cuts.py"  work/words.json work/keep.json    # Vorschlag: Pausen >0,5 s raus, Retakes markiert
python3 "$SKILL_DIR/scripts/render_cuts.py" work/input.mp4 work/keep.json work/cut.mp4
```

- Whisper schreibt jedes Wort mit Start/Ende. Sprache automatisch, bei Bedarf `--language de`.
- **Retakes**: Wo ich einen Satz neu anfange oder sage „let's redo that“, „nochmal“, „noch mal von vorne“, „sorry, nochmal“ → nur den **besten (meist letzten vollständigen) Take** behalten. `plan_cuts.py` markiert Kandidaten; ich lese das Transkript selbst gegen und korrigiere `keep.json` von Hand, bevor gerendert wird.
- **Pausen > 0,5 s** raus (zwischen Wörtern; ca. 0,1 s Luft am Rand lassen, damit nichts abgehackt klingt).
- Dem Nutzer kurz zeigen, was rausgeflogen ist (Liste der entfernten Retakes mit Text).

## Regel 4: Mehrere Videos in einer Aufnahme → splitten

Erkennen an: langen Pausen (> 3 s), Sätzen wie „nächstes Video“, „Video zwei“, „okay, next one“, Themenwechsel im Transkript. Vorschlag mit Zeitstempeln + Arbeitstitel zeigen, nach Bestätigung je eine Datei: `<original>_teil1_<titel>.mp4`, … Danach jede Datei einzeln durch Regel 3.

## Regel 5: Sieht aus wie ich (Branding)

Reihenfolge:
1. Gibt es einen **eigenen** Branding-Skill oder Brand-Dateien (`brand*.md`, `brand*.json`, Styleguide-PDF) → Farben, Fonts, Tonalität daraus. (Achtung: der Skill `brand-guidelines` ist Anthropics Marke, **nicht** die des Nutzers.)
2. Sonst: Block **„Gespeicherte Marke“** unten nutzen.
3. Ist der leer → **einmalig** nach 2 Markenfarben + 1 Font fragen und **sofort unten eintragen**. Danach nie wieder fragen.

**B-Roll kit**: Gibt es einen Ordner `B-roll kit` / `B-Roll Kit` / `broll-kit` (suchen in Projektordner, neben dem Original, `~/Desktop`, `~/Documents`, `~/Movies`) → Logo, Fotos, Grafiken daraus ziehen. Nichts erfinden, kein Logo nachzeichnen.

### Gespeicherte Marke
<!-- BRAND:START -->
- Farbe 1 (Primär): `#FF5A1F` (Orange), aktives Caption-Wort, Akzente, CTA
- Farbe 2 (Akzent): `#14213D` (Dunkelblau), Caption-Kontur, Flächen, Endkarte
- Font: **Inter** (Bold/ExtraBold für Captions)
- Caption-Stil: weiße Schrift, dunkelblaue Kontur, aktives Wort orange, 3 Wörter pro Zeile
- Befehl: `make_captions.py ... --font "Inter" --primary "#FF5A1F" --accent "#14213D"`
- Gesetzt am 2026-10-22, nicht erneut fragen.
<!-- BRAND:END -->

Captions: `python3 "$SKILL_DIR/scripts/make_captions.py" work/words_cut.json work/captions.ass --w 1080 --h 1920 --font "<Font>" --primary "<Farbe1>" --accent "<Farbe2>"` und dann mit `-vf "ass=work/captions.ass"` einbrennen. **Wichtig:** Nach dem Schnitt das geschnittene Video neu transkribieren (`words_cut.json`), damit die Zeiten passen. Für aufwendigere Grafiken (Titel, Lower Thirds, Endkarte) → Skills `hyperframes` / `hyperframes-registry`.

## Regel 6: Format & Qualität

| Ziel | Format | Auflösung |
|---|---|---|
| Reels, TikTok, Shorts | 9:16 | 1080×1920 (2160×3840 wenn Quelle 4K) |
| YouTube | 16:9 | 1920×1080 oder Quellauflösung (bis 4K) |

- Hochkant aus Querformat: Gesicht mittig zuschneiden (`crop=ih*9/16:ih:<x>:0,scale=1080:1920:flags=lanczos,setsar=1`, x nach Person anpassen), nie verzerren. **Immer `setsar=1`** nach Crop/Scale, sonst scheitert späteres Zusammenfügen (concat).
- **Immer höchste Qualität**: `-c:v libx264 -preset slow -crf 16 -profile:v high -pix_fmt yuv420p -c:a aac -b:a 320k -ar 48000 -movflags +faststart`. Framerate der Quelle behalten. Nie hochskalieren über Quellauflösung hinaus, ohne es zu sagen.

## Regel 7: KI-Stimme + echte Stimme → getrennt pegeln

- Liegen die Stimmen auf **getrennten Spuren/Dateien**: jede einzeln auf **-16 LUFS** (Social) normalisieren: `loudnorm=I=-16:TP=-1.5:LRA=11` (zwei Durchläufe für Genauigkeit), dann mischen.
- Sind sie **in einer Spur**: Abschnitte über das Transkript/Zeitstempel trennen, jeden Abschnitt einzeln messen (`ebur128`) und angleichen, dann wieder zusammensetzen. Ziel: echte Stimme nie leiser als die KI-Stimme.
- Am Ende Gesamtpegel prüfen und Ergebnis nennen (LUFS beider Stimmen).

## Regel 8: Zu dunkel / verrauscht → fixen und ehrlich sein

- Messen: `ffmpeg -i in.mp4 -vf signalstats,metadata=print:key=lavfi.signalstats.YAVG -f null -` (YAVG < ~60 von 255 = dunkel).
- Fix: `eq=brightness=0.04:contrast=1.08:gamma=1.15:saturation=1.05`, Rauschen `hqdn3d=3:2:4:3` (stärker: `nlmeans`). Für Looks/LUTs → Skill `media-use` (`media-treatment`).
- **Ehrlich sagen**: was verbessert wurde und was nicht geht. Wenn Gesicht kaum erkennbar, starkes Rauschen nach Fix bleibt oder Hauttöne kippen → klar empfehlen: **„Das würde ich neu drehen“** (mit Tipp: Licht von vorne/Fenster, Kamera-ISO runter).

## Regel 9: Abschluss

Immer melden:
- **Wo die Datei liegt** (voller Pfad).
- Was gemacht wurde (Kurzliste: Schnitte, Länge vorher/nachher, Format).
- Erinnerung: **„Schau es dir einmal komplett an, bevor du es postest. Du bist der Regisseur.“**

---

## Ablauf in Kurzform
1. 3 Fragen (Regel 1) → 2. Kopie in Arbeitsordner (Regel 2) → 3. Qualität prüfen (Regel 8) → 4. Transkript + Split (Regel 3/4) → 5. Schnitt → 6. Audio pegeln (Regel 7) → 7. Format + Branding + Captions (Regel 5/6) → 8. Export neben Original → 9. Abschlussmeldung (Regel 9)

## Bekannte Stolperfallen
- `drawbox`: Bildhöhe ist `ih`, nicht `h` (`h` = Boxhöhe). Bei `drawtext` ist `h` die Bildhöhe.
- Nach Crop/Scale immer `setsar=1`, sonst scheitert `concat`.
- Cloud-Container: dort kann `huggingface.co` gesperrt sein, dann lädt Whisper keine Modelle. Lokal (Mac) kein Problem. Falls gesperrt: Pausen per `silencedetect=noise=-40dB:d=0.5` finden, Whisper-Schritt dem Nutzer offen als „nicht gelaufen“ melden.
- Endkarte für CTA (Antwort auf Frage 2): 2-3 s, Hintergrund Farbe 2, CTA-Wort in Farbe 1, Font der Marke.
