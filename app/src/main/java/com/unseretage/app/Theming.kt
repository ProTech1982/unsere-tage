package com.unseretage.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView

/** Alles, was das Aussehen zur Laufzeit steuert: Farbe, Kachelform, Transparenz. */
object Theming {

    const val DEFAULT_ACCENT = 0xFFC14D6B.toInt()

    /** Auswahl der Akzentfarben in den Einstellungen. */
    val ACCENTS: List<Pair<String, Int>> = listOf(
        "Rosé" to 0xFFC14D6B.toInt(),
        "Mohn" to 0xFFC0392B.toInt(),
        "Kupfer" to 0xFFC2662C.toInt(),
        "Gold" to 0xFFBE912F.toInt(),
        "Salbei" to 0xFF4F8B63.toInt(),
        "Petrol" to 0xFF2E7D8A.toInt(),
        "Enzian" to 0xFF3D6DB5.toInt(),
        "Flieder" to 0xFF7B52AB.toInt()
    )

    /** Mögliche Kachelformen: Beschriftung und Eckenradius in dp. */
    val SHAPES: List<Pair<String, Int>> = listOf(
        "Kantig" to 4,
        "Weich" to 24,
        "Rund" to 40
    )

    /** Farbverlauf für den Kopfbereich, abgeleitet aus der Akzentfarbe. */
    fun headerBackground(context: Context, accent: Int): GradientDrawable =
        GradientDrawable(
            GradientDrawable.Orientation.BR_TL,
            intArrayOf(accent, shade(accent, 0.55f), shade(accent, 0.78f))
        ).apply {
            val radius = dp(context, 32f)
            cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, radius, radius, radius, radius)
        }

    /** Wendet Form und Transparenz auf eine Kachel an. */
    fun applyCard(card: MaterialCardView, cornerDp: Int, opacityPercent: Int) {
        card.radius = dp(card.context, cornerDp.toFloat())

        val alpha = (opacityPercent.coerceIn(30, 100) * 255) / 100
        val base = card.cardBackgroundColor.defaultColor
        card.setCardBackgroundColor(ColorUtils.setAlphaComponent(base, alpha))

        // Ein Schatten unter einer durchsichtigen Flaeche sieht falsch aus.
        if (opacityPercent < 100) {
            card.cardElevation = 0f
            card.strokeWidth = dp(card.context, 1f).toInt()
            card.strokeColor = ColorUtils.setAlphaComponent(base, 90)
        } else {
            card.cardElevation = dp(card.context, 2f)
            card.strokeWidth = 0
        }
    }

    /** Mischt eine Farbe in Richtung Schwarz. 0 = unveraendert, 1 = schwarz. */
    fun shade(color: Int, amount: Float): Int =
        ColorUtils.blendARGB(color, Color.BLACK, amount.coerceIn(0f, 1f))

    /** Hellere Variante, fuer Text auf dunklem Grund. */
    fun tint(color: Int, amount: Float): Int =
        ColorUtils.blendARGB(color, Color.WHITE, amount.coerceIn(0f, 1f))

    /**
     * Sorgt dafuer, dass die Akzentfarbe auf dem jeweiligen Untergrund lesbar bleibt.
     * Im dunklen Modus werden kraeftige Toene sonst schnell zu dunkel.
     */
    fun readableOn(accent: Int, background: Int): Int {
        var candidate = accent
        var guard = 0
        while (ColorUtils.calculateContrast(candidate, background) < 3.0 && guard < 12) {
            candidate = if (isDark(background)) tint(candidate, 0.12f) else shade(candidate, 0.12f)
            guard++
        }
        return candidate
    }

    fun isDark(color: Int): Boolean = ColorUtils.calculateLuminance(color) < 0.5

    fun resolveColor(context: Context, attr: Int, fallback: Int): Int {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(attr, value, true)) {
            if (value.resourceId != 0) {
                androidx.core.content.ContextCompat.getColor(context, value.resourceId)
            } else {
                value.data
            }
        } else {
            fallback
        }
    }

    private fun dp(context: Context, value: Float): Float =
        value * context.resources.displayMetrics.density
}
