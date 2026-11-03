# Unsere Tage

> **Hinweis:** Diese App wurde mit Hilfe von künstlicher Intelligenz
> entwickelt. Konzept und Vorgaben stammen von ProTech1982, den Programmcode
> hat überwiegend Claude (Anthropic) geschrieben. Der Code ist nicht Zeile für
> Zeile von einem Menschen geprüft. Einzelheiten unter
> [Mit künstlicher Intelligenz entwickelt](#mit-künstlicher-intelligenz-entwickelt).

Aktuelle Version: **1.4**. Der Verlauf steht in [CHANGELOG.md](CHANGELOG.md).

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
- Helligkeit wählbar: hell, dunkel oder systemabhängig
- Eigenes Foto, wahlweise groß oben, als Seitenhintergrund oder als rundes Symbol
- Update-Knopf: prüft auf GitHub nach einer neueren Version und öffnet
  deren Download-Seite im Browser
- Anpassbares Aussehen: Kachelform, Deckkraft der Kacheln und Akzentfarbe,
  mit Live-Vorschau in den Einstellungen

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
- Internetzugriff nur für die Versionsabfrage bei GitHub, keine Konten,
  alle Daten bleiben auf dem Handy
- Keine Berechtigung zum Installieren anderer Apps — sonst warnt
  Play Protect zu Recht
- Build über GitHub Actions, Debug-Signatur

## Mit künstlicher Intelligenz entwickelt

Diese App ist ein privates Projekt. Konzept, Anforderungen und alle
Entscheidungen stammen von ProTech1982; den Programmcode hat überwiegend
Claude, ein KI-Assistent von Anthropic, nach diesen Vorgaben geschrieben.

**Was das bedeutet, wenn du die App benutzt oder den Code weiterverwendest:**

- Der Code ist nicht von einem Menschen Zeile für Zeile geprüft worden.
  Er wird automatisch kompiliert, aber nicht systematisch getestet.
- Die App ist kein geprüftes Produkt, sondern eine kleine Erinnerungshilfe.
- Sie kann Fehler enthalten. Verlass dich bei wichtigen Tagen nicht allein
  auf sie.

Dieser Hinweis steht hier aus Transparenz, nicht weil eine Vorschrift ihn
verlangt. Wer ein öffentlich herunterladbares Programm anbietet, sollte sagen,
wie es entstanden ist.

## Haftung

Die Nutzung erfolgt auf eigene Gefahr. Die App wird ohne Gewähr
bereitgestellt; eine Haftung für Schäden aus ihrer Nutzung ist, soweit
gesetzlich zulässig, ausgeschlossen. Unberührt bleibt die Haftung für Vorsatz
und grobe Fahrlässigkeit sowie für Schäden aus der Verletzung von Leben,
Körper oder Gesundheit.

## Datenschutz

Alle Eingaben – Daten, Uhrzeit, Einstellungen und das gewählte Foto – bleiben
auf dem Gerät. Es gibt keine Konten, keine Cloud, keine Werbung und keine
Analyse-Werkzeuge. Das Foto wird nicht kopiert, die App merkt sich nur die
Leseerlaubnis dafür.

Daten verlassen das Gerät nur, wenn du auf **Nach Updates suchen** tippst:
dann ruft die App die Angaben zum neuesten Release von `api.github.com` ab.
GitHub sieht dabei – wie beim Aufruf jeder Webseite – die IP-Adresse des
Geräts. Den Download selbst übernimmt der Browser.

## Lizenz

Der eigene Programmcode steht unter der MIT-Lizenz, siehe [LICENSE](LICENSE).
Die verwendeten Bibliotheken stehen unter der Apache License 2.0, siehe
[THIRD-PARTY-LICENSES.md](THIRD-PARTY-LICENSES.md).
