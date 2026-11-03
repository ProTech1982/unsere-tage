# Änderungsverlauf

## 1.4 — 3. November 2026

- Neue Seite **Über & Rechtliches** in den Einstellungen: offener Hinweis,
  dass die App mit Hilfe von KI entwickelt wurde, dazu Haftungsausschluss,
  Datenschutzhinweis und die Lizenzen der verwendeten Bibliotheken.
- `LICENSE` (MIT) und `THIRD-PARTY-LICENSES.md` im Projekt. Die Apache
  License 2.0 der verwendeten Bibliotheken verlangt, dass ihre Lizenz- und
  Copyright-Vermerke bei der Weitergabe mitgeliefert werden.

## 1.3 — 2. November 2026

- Versionsanzeige und Update-Suche sind von der Startseite in die
  Einstellungen gewandert, ganz nach unten. Die Startseite zeigt jetzt
  nur noch eure Tage.

## 1.2 — 2. November 2026

Play Protect hatte die App als möglicherweise gefährlich eingestuft.
Auslöser war die Berechtigung, andere Apps zu installieren, gemeinsam
mit dem Internetzugriff — dasselbe Muster, das Schadsoftware benutzt,
um im Hintergrund etwas nachzuladen.

- Die Berechtigung `REQUEST_INSTALL_PACKAGES` ist entfernt, ebenso der
  eingebaute Download und der FileProvider.
- Der Update-Knopf prüft weiterhin, ob eine neue Version vorliegt, und
  öffnet deren Seite im Browser. Herunterladen und Installieren
  übernimmt der Browser.
- Es bleiben vier Berechtigungen: Internet (nur für die
  Versionsabfrage), Benachrichtigungen, Start nach dem Hochfahren und
  exakte Weckzeiten.

## 1.1 — 2. November 2026

- Helligkeit frei wählbar: hell, dunkel oder der Systemeinstellung
  folgend. Bisher richtete sich die App immer nach dem System.
- Meilensteine in drei Arten, einzeln an- und abschaltbar:
  - **Schnapszahlen** wie 111, 666, 777 oder 1111
  - **Tausender** wie 1000 oder 2000
  - **Hunderter** wie 100 oder 200
  Die Karte auf der Startseite zeigt immer den nächsten fälligen
  Meilenstein und benennt, um welche Art es sich handelt.

## 1.0 — 2. November 2026

Erste vollwertige Version. Ab hier tragen alle Builds denselben
Signaturschlüssel, sodass sich jede neue Version über die vorherige
legen lässt.

- Kennenlerntag und Hochzeitstag mit Tageszähler und Angabe in
  Jahren, Monaten und Tagen
- Countdown bis zum nächsten Jahrestag, mit Fortschrittsbalken
- Meilensteine alle 1000 gemeinsame Tage
- Benachrichtigung am Tag selbst, wahlweise 3, 7 oder 14 Tage vorher,
  zur frei wählbaren Uhrzeit; übersteht einen Neustart
- Optionale Monatsjubiläen
- Eigenes Foto, wahlweise groß oben, als Seitenhintergrund oder als
  rundes Symbol
- Anpassbares Aussehen: Kachelform, Deckkraft und Akzentfarbe mit
  Live-Vorschau
- Update-Knopf: prüft auf GitHub, lädt die neue Version und startet
  die Installation

## Beta-Builds 1 bis 6 — Vorgeschichte

Diese Builds liegen weiterhin unter *Releases*, sind aber als Beta
gekennzeichnet. Jeder trug eine eigene, zufällig erzeugte Signatur,
weshalb sie sich nicht gegenseitig aktualisieren konnten. Wer einen
davon installiert hat, muss die App einmal deinstallieren, bevor 1.0
installiert werden kann.

- Build 5: Aussehen anpassbar — Kachelform, Deckkraft, Akzentfarbe
- Build 4: neues Design, Foto-Funktion, Update-Knopf
- Build 2: erste lauffähige Fassung mit Zählern und Benachrichtigungen
- Builds 1 und 3: fehlgeschlagene Läufe, kein Ergebnis
