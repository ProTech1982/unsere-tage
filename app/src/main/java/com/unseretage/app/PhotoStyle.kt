package com.unseretage.app

/** Wie das gewaehlte Foto auf dem Hauptbildschirm erscheint. */
enum class PhotoStyle(val key: String) {
    /** Grosses Bild oben, ueber die volle Breite. */
    BANNER("banner"),

    /** Fuellt den Hintergrund der ganzen Seite, abgedunkelt. */
    BACKGROUND("background"),

    /** Kleines rundes Symbol ueber den Namen. */
    AVATAR("avatar"),

    /** Kein Foto anzeigen. */
    NONE("none");

    companion object {
        fun fromKey(key: String?): PhotoStyle =
            entries.firstOrNull { it.key == key } ?: BANNER
    }
}
