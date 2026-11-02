# Unsere Tage

Eine kleine Android-App, die mitzählt, wie lange ihr euch schon kennt und
wie lange ihr verheiratet seid – und rechtzeitig daran erinnert.

## Was die App kann

- Kennenlerntag und Hochzeitstag mit Tageszähler und "X Jahre, Y Monate, Z Tage"
- Countdown bis zum nächsten Jahrestag
- Meilensteine wie 5.000 gemeinsame Tage
- Benachrichtigung am Tag selbst, optional 3, 7 oder 14 Tage vorher
- Optional Monatsjubiläen
- Uhrzeit der Erinnerung frei wählbar
- Erinnerungen überstehen einen Handy-Neustart
- Eigenes Foto, wahlweise groß oben, als Seitenhintergrund oder als rundes Symbol
- Update-Knopf: prüft auf GitHub nach einer neueren Version, lädt sie und
  startet die Installation

Namen und Daten trägst du direkt in der App ein (Knopf "Einstellungen").
Es muss also nie neu gebaut werden, wenn sich etwas ändert.

## APK bauen lassen (ohne PC)

1. Auf GitHub ein neues, leeres Repository anlegen, zum Beispiel `unsere-tage`.
2. Diesen Ordner hochladen:

   ```
   git init
   git add .
   git commit -m "Unsere Tage"
   git branch -M main
   git remote add origin https://github.com/DEIN-NAME/unsere-tage.git
   git push -u origin main
   ```

3. Auf GitHub unter **Actions** läuft der Build automatisch los (ca. 3–5 Minuten).
4. Danach unter **Releases** den neuesten Eintrag öffnen und `UnsereTage.apk`
   antippen. Das Handy fragt beim Öffnen einmal nach der Erlaubnis,
   Apps aus dieser Quelle zu installieren.

## Falls die Erinnerung zu spät kommt

Android darf Apps im Energiesparmodus ausbremsen. In den Handy-Einstellungen
unter *Apps → Unsere Tage → Akku* auf "Nicht eingeschränkt" stellen.
Auf Android 12 und neuer kann zusätzlich "Wecker und Erinnerungen" erlaubt werden,
dann kommt die Meldung auf die Minute genau.

## Technisch

- Kotlin, Views mit Material 3, kein Compose
- minSdk 26 (Android 8), targetSdk 35
- Keine Internetberechtigung, keine Konten, alles bleibt auf dem Handy
- Build über GitHub Actions, Debug-Signatur
