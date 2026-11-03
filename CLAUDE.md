# Projektgedächtnis – Unsere Tage

Diese Datei ist für Claude gedacht und wird beim Öffnen des Projekts
automatisch gelesen. Sie beantwortet die Frage: *Worum geht es hier, wie
wird gebaut, was ist absichtlich so, und wo standen wir zuletzt?*

**Bitte beim Weiterarbeiten pflegen** – vor allem den Abschnitt
„Stand" und „Offene Punkte". Der ausführliche Verlauf steht in
`CHANGELOG.md`, hier nur das Nötige zum Wiedereinsteigen.

---

## Worum es geht

Eine Android-App, die Kennenlerntag und Hochzeitstag mitzählt, bis zum
nächsten Jahrestag herunterzählt und rechtzeitig erinnert.

- Repository: https://github.com/ProTech1982/unsere-tage (öffentlich)
- Paket: `com.unseretage.app`
- Lizenz: MIT, Rechteinhaber ProTech1982

## Technik

| | |
|---|---|
| Sprache | Kotlin |
| Oberfläche | **klassische Views mit XML und ViewBinding – kein Compose** |
| Einstellungen | `SharedPreferences` über `Prefs.kt`, keine Datenbank |
| minSdk / targetSdk | 26 / 35 |

Das ist der wichtigste Unterschied zum Schwesterprojekt
*Meine Termine*, das Compose benutzt. Nicht verwechseln.

Aufbau unter `app/src/main/java/com/unseretage/app/`:

- `MainActivity.kt` – Startseite: Zähler, Countdown, Meilenstein-Karte
- `SettingsActivity.kt` – alle Einstellungen, inklusive Update-Bereich
- `LegalActivity.kt` – Über & Rechtliches
- `Prefs.kt` – sämtliche gespeicherten Einstellungen an einem Ort
- `DateMath.kt`, `Milestones.kt` – Rechnerei für Tage, Jahrestage, Meilensteine
- `Notifier.kt`, `AlarmScheduler.kt`, `AlarmReceiver.kt`, `BootReceiver.kt`,
  `Reminders.kt` – Benachrichtigungen, überstehen den Neustart
- `Theming.kt`, `PhotoStyle.kt`, `PhotoLoader.kt` – Aussehen und Fotos
- `Updater.kt` – Versionsabfrage gegen die GitHub-API

Layouts: `app/src/main/res/layout/` – `activity_main.xml`,
`activity_settings.xml`, `activity_legal.xml`, `item_counter_card.xml`.

Das Theme setzt `windowTranslucentStatus`, der Inhalt liegt also unter dem
Statusbalken. Wer oben etwas platziert, muss den Abstand selbst setzen – so
macht es `keepSettingsBelowStatusBar()` in `MainActivity.kt` für das Zahnrad.

## Bauen und veröffentlichen

**Es wird nicht lokal gebaut.** Jeder Push auf `main` löst
`.github/workflows/android.yml` aus.

- `versionName` steht **von Hand** in `gradle.properties`
  (`appVersionName=1.4`) – bei einer neuen Version dort hochsetzen.
- `versionCode` = Lauf-Nummer von GitHub Actions (`BUILD_NUMBER`).
  Daran erkennt die App ein neues Release.
- Release-Tag: `build-<Nummer>`, Datei immer `UnsereTage.apk`
- Änderungstext im Release = Betreffzeile des letzten Commits
  (`git log -1 --pretty=%s`), damit keine Commit-Anhänge wie
  `Co-Authored-By` im Update-Fenster der App landen
- Signierung: fester Keystore, Alias `unseretage`, aus den Secrets
  `KEYSTORE_B64` und `KEYSTORE_PASSWORD`. **Liegt nicht im Repo.**

## Absichtliche Entscheidungen (nicht versehentlich so)

- **Kein `REQUEST_INSTALL_PACKAGES`, kein eingebauter Download, kein
  FileProvider.** Das gab es bis 1.1 und Play Protect hat die App daraufhin
  als möglicherweise gefährlich eingestuft. Seit 1.2 prüft die App nur noch,
  ob es etwas Neues gibt, und öffnet die Seite im Browser. **Bitte nicht
  rückgängig machen** – der Grund steht ausführlich in `CHANGELOG.md` zu 1.2.
- Es bleiben vier Berechtigungen: Internet (nur Versionsabfrage),
  Benachrichtigungen, Start nach dem Hochfahren, exakte Weckzeiten.
- Versionsanzeige und Update-Suche stehen seit 1.3 in den Einstellungen,
  nicht mehr auf der Startseite.
- Die App ist offen als KI-generiert gekennzeichnet – in README und unter
  „Über & Rechtliches".

## Arbeitsweise mit ProTech1982

- Alles auf Deutsch: Oberfläche, Commit-Nachrichten, Code-Kommentare.
- Er arbeitet überwiegend vom Handy aus und baut nichts selbst.
  Claude übernimmt Git, Push und das Beobachten des Builds und meldet erst
  zurück, wenn das Release fertig ist.
- Beides soll sich gleich anfühlen wie *Meine Termine*
  (https://github.com/ProTech1982/meine-termine): gleiche Gestaltungsideen,
  gleiche Texte unter „Über & Rechtliches", gleiches Verhalten beim Update.
  Bei Unsicherheit dort nachsehen.
- Die Play-Protect-Warnung beim Installieren („kennt von diesem Entwickler
  noch keine anderen Apps") ist normal für selbst signierte APKs und lässt
  sich nicht aus der App heraus abstellen.

## Stand

**3. November 2026, Version 1.4 (Build 15)**

Zuletzt gemacht:

- Build 14: Einstellungen sind **vom Textknopf unten in ein Zahnrad oben
  rechts im Kopfbereich** gewandert (`activity_main.xml`, Knopf heißt
  weiterhin `btnSettings`, neues Icon `res/drawable/ic_settings.xml`).
  Der Abstand zum Statusbalken kommt aus `keepSettingsBelowStatusBar()`.
- Build 15: Die Update-Suche zeigt jetzt **ein Fenster statt einer
  Textzeile** – wie bei *Meine Termine*. Drei Fälle in `SettingsActivity.kt`:
  neue Version (Titel mit Versionsnummer, installierte Version,
  Download-Größe, Änderungstext, dann „Im Browser laden" / „Später"),
  „Alles aktuell", „Nachsehen ging nicht".
  `Updater.CheckResult.Available` trägt dafür zusätzlich `apkUrl`,
  `sizeBytes` und `notes`.
  Im selben Zug trägt der Workflow den Commit-Betreff als Änderungstext
  ins Release ein – vorher stand dort nur ein fester Satz.

## Offene Punkte

- `appVersionName` steht noch auf `1.4`, obwohl seither zwei Builds dazukamen.
  Bei der nächsten inhaltlichen Änderung auf `1.5` setzen und einen
  Abschnitt in `CHANGELOG.md` ergänzen.
- Klarname statt „ProTech1982" in Lizenz und Rechtlichem? Noch nicht entschieden.
- Soll Claude im KI-Hinweis namentlich genannt bleiben? Noch offen.
