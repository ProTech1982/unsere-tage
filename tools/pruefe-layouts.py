#!/usr/bin/env python3
"""
Prueft die Layout-Dateien auf Werte, die erst zur Laufzeit krachen.

Hintergrund: Ein Material-Slider wirft beim Aufbauen des Fensters eine
Ausnahme, wenn sein Startwert nicht genau auf einem Schritt liegt
(value = valueFrom + n * stepSize). Das faellt beim Bauen nicht auf -
die App stuerzt erst ab, wenn jemand das Fenster oeffnet. Genau das ist
in Version 1.5 passiert und hat die Einstellungen unbenutzbar gemacht.

Laeuft in der GitHub-Action vor dem Bauen. Rueckgabewert ungleich 0
bricht den Build ab.
"""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID = "{http://schemas.android.com/apk/res/android}"

LAYOUT_DIR = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res" / "layout"


def attr(element, name):
    value = element.get(ANDROID + name)
    if value is None:
        return None
    try:
        return float(value)
    except ValueError:
        # Verweis auf eine Ressource - koennen wir hier nicht aufloesen.
        return None


def pruefe_slider(datei, element, fehler):
    name = element.get(ANDROID + "id") or "(ohne id)"
    value = attr(element, "value")
    start = attr(element, "valueFrom")
    ende = attr(element, "valueTo")
    schritt = attr(element, "stepSize")

    if value is None:
        return

    if start is not None and value < start:
        fehler.append(f"{datei.name}: {name} startet mit {value}, kleiner als valueFrom {start}")
    if ende is not None and value > ende:
        fehler.append(f"{datei.name}: {name} startet mit {value}, groesser als valueTo {ende}")

    if schritt and schritt > 0 and start is not None:
        schritte = (value - start) / schritt
        if abs(schritte - round(schritte)) > 1e-6:
            gueltig = start + round(schritte) * schritt
            fehler.append(
                f"{datei.name}: {name} startet mit {value:g}, liegt aber nicht auf einem "
                f"Schritt (valueFrom {start:g}, stepSize {schritt:g}). "
                f"Naechster gueltiger Wert: {gueltig:g}"
            )


def main():
    if not LAYOUT_DIR.is_dir():
        print(f"Kein Layout-Ordner unter {LAYOUT_DIR}")
        return 1

    fehler = []
    geprueft = 0

    for datei in sorted(LAYOUT_DIR.glob("*.xml")):
        baum = ET.parse(datei)
        for element in baum.iter():
            if element.tag.endswith("slider.Slider") or element.tag.endswith("RangeSlider"):
                geprueft += 1
                pruefe_slider(datei, element, fehler)

    if fehler:
        print("Layout-Pruefung fehlgeschlagen:")
        for eintrag in fehler:
            print(f"  - {eintrag}")
        return 1

    print(f"Layout-Pruefung in Ordnung ({geprueft} Regler geprueft).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
