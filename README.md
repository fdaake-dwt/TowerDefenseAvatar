# Elemental Defense

Ein Tower-Defense-Spiel für Android im Stil von Bloons TD, mit den vier
Elementen (Feuer, Wasser, Erde, Luft) als Turm-Kategorien. Räuberbanden
ziehen über feste Pfade durchs Dorf; du baust und upgradest Elementartürme,
um sie aufzuhalten. Wie bei Mario Kart sind zu Beginn nicht alle Level
verfügbar – ein neues Level wird erst freigeschaltet, wenn das vorherige
gewonnen wurde.

## Projektstruktur

Ein LibGDX-Multi-Modul-Projekt:

- `core` – die eigentliche Spiellogik und alle Screens (plattformunabhängig,
  reines Kotlin + LibGDX). Hier steckt praktisch das ganze Spiel.
- `android` – der Android-Launcher (Activity, Manifest, Gradle-Modul).
- `desktop` – ein Desktop-Launcher (LWJGL3), nützlich zum schnellen Testen
  am Rechner, ohne Emulator/Gerät.

## Spielinhalt (aktueller Stand)

- 1 spielbares Level ("Dorf am Fluss", 6 Wellen), ein zweites Level
  ("Verschneites Bergdorf") ist angelegt, aber gesperrt bis Level 1
  gewonnen wurde.
- 4 Türme: Feuerbändiger (Brandschaden über Zeit), Wasserbändiger
  (verlangsamt + Flächenschaden), Erdbändiger (hoher Einzelschaden,
  Flächenschaden), Luftbändiger (schnelle Angriffe, trifft mehrere Gegner).
  Jeder Turm hat eine Ausbaustufe.
- 4 Gegnertypen (Räuber, Eisiger Räuber, Pflanzen-Avatar, Kundschafter) mit
  Schwächen/Resistenzen gegenüber bestimmten Elementen.
- Wirtschaft (Gold durch Kills), Leben, manuell startbare Wellen (mit
  Auto-Start nach kurzer Pause, falls man nicht selbst startet),
  Turm-Verkauf mit Teilerstattung, Sieg/Niederlage-Bildschirm.

## Am Rechner testen (Desktop-Version)

```
./gradlew :desktop:run
```

(Falls der Gradle-Wrapper noch fehlt: einmalig `gradle wrapper` in einem
Terminal mit installiertem Gradle ausführen, oder das Projekt einfach in
Android Studio öffnen – das richtet den Wrapper automatisch ein.)

## Als Android-App bauen

1. Projekt in Android Studio öffnen (Ordner `TowerDefenseAvatar` auswählen).
2. Android Studio synchronisiert Gradle automatisch und lädt dabei die
   Android-Toolchain/Bibliotheken von Googles Maven-Repository – dafür wird
   eine normale Internetverbindung benötigt.
3. `android`-Modul auswählen und auf einem Gerät/Emulator ausführen, oder
   über `Build > Build Bundle(s) / APK(s) > Build APK(s)` eine APK erzeugen.

> Hinweis: Diese Cloud-Sandbox hier hat keinen Zugriff auf Googles
> Maven-Repository (nötig für die Android-Gradle-Plugin/AndroidX-Abhängigkeiten),
> daher konnte das `android`-Modul hier nicht kompiliert werden. Die
> Spiellogik selbst (`core`-Modul) wurde ausführlich getestet: kompiliert
> sauber und wurde über den Desktop-Launcher tatsächlich gespielt
> (Türme bauen, Wellen starten, Gegner sterben, Gold/Leben, Sieg/Niederlage).
> Der Android-Teil ist Standard-LibGDX-Boilerplate und sollte in Android
> Studio ohne Änderungen funktionieren.

## Nächste mögliche Schritte

- Weitere Level/Tracks.
- Echte Grafiken/Sprites statt einfacher Farbflächen.
- Sound/Musik.
- Zweite Ausbaustufe für mehr Tower-Vielfalt, weitere Gegnertypen.
