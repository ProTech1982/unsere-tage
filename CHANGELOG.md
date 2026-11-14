# Änderungsverlauf

## 1.6 — 14. November 2026

- Neue Meilenstein-Art **Schnapsdatum**: Jahre, Monate und Tage sind dieselbe
  Zahl, also 7 Jahre, 7 Monate und 7 Tage zusammen. Das sind 1/1/1 bis
  11/11/11 — ab zwölf Monaten wird daraus ein weiteres Jahr, das Muster kann
  in der Aufteilung also nicht mehr auftauchen.
- Das ist **etwas anderes als die Schnapszahlen**, die es schon gab: die
  zählen die Gesamtzahl gemeinsamer Tage (777, 7777). Ein Schnapsdatum trifft
  mit einer runden Tageszahl so gut wie nie zusammen, darum kam dafür bisher
  keine Erinnerung. Der Schalter in den Einstellungen heißt jetzt
  „Schnapszahlen in Tagen", damit die beiden nicht zu verwechseln sind.
- Eigener Schalter in den Einstellungen, standardmäßig eingeschaltet.
- Die Karte „Nächster Meilenstein" auf der Startseite zeigt, was zuerst
  kommt — runde Tageszahl oder Schnapsdatum.

## 1.5 — 3. November 2026

- **Muster** für den Kopfbereich und den Seitenhintergrund, wie in der
  Schwester-App *Meine Termine*. Zehn gerechnete Muster in drei Gruppen:
  *Dezent* (Punkte, Raster, Karo, Höhenlinien), *Geometrisch* (Schräge,
  Zickzack, Waben, Kreuze) und *Lebhaft* (Wellen, Konfetti).
- Für jeden der beiden Bereiche getrennt einstellbar: Muster, Musterfarbe
  (weiß, schwarz oder eine der acht Akzentfarben) und Stärke.
- Der Seitenhintergrund bekommt sein Muster auch dann, wenn gar kein Foto
  hinterlegt ist. Liegt ein Foto darüber, deckt es das Muster ab.
- Die Einstellungen sind von einem Textknopf unten in ein Zahnrad oben
  rechts im Kopfbereich gewandert.
- Die Update-Suche meldet sich jetzt mit einem Fenster, das Versionsnummer,
  Download-Größe und den Änderungstext des Releases zeigt — vorher stand
  dort nur eine Textzeile.

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
