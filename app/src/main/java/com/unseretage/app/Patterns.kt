package com.unseretage.app

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Gruppen, damit die Auswahl uebersichtlich bleibt. */
enum class PatternGroup(val label: String) {
    PLAIN("Ohne"),
    SUBTLE("Dezent"),
    GEOMETRIC("Geometrisch"),
    LIVELY("Lebhaft")
}

/**
 * Dieselben Muster wie in der Schwester-App "Meine Termine", hier aber als
 * Android-Drawable statt in Compose. Alle Muster werden gerechnet, nicht aus
 * Bilddateien geladen: so passen sie sich jeder Farbe und jeder
 * Bildschirmgroesse an und kosten kein Gewicht in der APK.
 */
enum class Pattern(val key: String, val label: String, val group: PatternGroup) {
    NONE("none", "Kein Muster", PatternGroup.PLAIN),

    DOTS("dots", "Punkte", PatternGroup.SUBTLE),
    GRID("grid", "Raster", PatternGroup.SUBTLE),
    CROSSHATCH("crosshatch", "Karo", PatternGroup.SUBTLE),
    CONTOUR("contour", "Höhenlinien", PatternGroup.SUBTLE),

    DIAGONAL("diagonal", "Schräge", PatternGroup.GEOMETRIC),
    CHEVRON("chevron", "Zickzack", PatternGroup.GEOMETRIC),
    HONEYCOMB("honeycomb", "Waben", PatternGroup.GEOMETRIC),
    PLUS("plus", "Kreuze", PatternGroup.GEOMETRIC),

    WAVES("waves", "Wellen", PatternGroup.LIVELY),
    CONFETTI("confetti", "Konfetti", PatternGroup.LIVELY);

    companion object {
        fun fromKey(key: String?): Pattern =
            entries.firstOrNull { it.key == key } ?: NONE
    }
}

/**
 * Zeichnet ein Muster ueber die gesamte Flaeche.
 *
 * @param density Bildschirmdichte, damit die Strichstaerken auf jedem Geraet gleich wirken.
 * @param bottomCornerRadius Wird der Kopfbereich unten abgerundet, muss das Muster
 *        dort mit abgeschnitten werden, sonst ragt es ueber die Ecke hinaus.
 */
class PatternDrawable(
    private val pattern: Pattern,
    color: Int,
    private val density: Float,
    private val scale: Float = 1f,
    private val bottomCornerRadius: Float = 0f
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        if (pattern == Pattern.NONE) return

        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        if (w <= 0f || h <= 0f) return

        val saved = canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())

        if (bottomCornerRadius > 0f) {
            val clip = Path().apply {
                addRoundRect(
                    RectF(0f, 0f, w, h),
                    floatArrayOf(
                        0f, 0f, 0f, 0f,
                        bottomCornerRadius, bottomCornerRadius,
                        bottomCornerRadius, bottomCornerRadius
                    ),
                    Path.Direction.CW
                )
            }
            canvas.clipPath(clip)
        }

        drawPattern(canvas, w, h)
        canvas.restoreToCount(saved)
    }

    private fun drawPattern(canvas: Canvas, w: Float, h: Float) {
        val unit = 34f * scale * density

        when (pattern) {
            Pattern.NONE -> Unit

            Pattern.DOTS -> {
                val radius = 1.9f * scale * density
                var y = unit / 2
                var row = 0
                while (y < h + unit) {
                    val offset = if (row % 2 == 0) 0f else unit / 2
                    var x = offset
                    while (x < w + unit) {
                        canvas.drawCircle(x, y, radius, fill)
                        x += unit
                    }
                    y += unit
                    row++
                }
            }

            Pattern.GRID -> {
                paint.strokeWidth = 1f * density
                var x = 0f
                while (x < w + unit) {
                    canvas.drawLine(x, 0f, x, h, paint)
                    x += unit
                }
                var y = 0f
                while (y < h + unit) {
                    canvas.drawLine(0f, y, w, y, paint)
                    y += unit
                }
            }

            Pattern.CROSSHATCH -> {
                paint.strokeWidth = 1f * density
                val step = unit * 1.1f
                var d = -h
                while (d < w + h) {
                    canvas.drawLine(d, 0f, d + h, h, paint)
                    canvas.drawLine(d, h, d + h, 0f, paint)
                    d += step
                }
            }

            Pattern.CONTOUR -> {
                paint.strokeWidth = 1.3f * density
                val cx = w * 0.82f
                val cy = h * 0.18f
                var radius = unit * 0.8f
                while (radius < (w + h)) {
                    canvas.drawCircle(cx, cy, radius, paint)
                    radius += unit * 1.15f
                }
            }

            Pattern.DIAGONAL -> {
                paint.strokeWidth = 3f * scale * density
                val step = unit * 0.9f
                var d = -h
                while (d < w + h) {
                    canvas.drawLine(d, 0f, d + h, h, paint)
                    d += step
                }
            }

            Pattern.CHEVRON -> {
                paint.strokeWidth = 2f * scale * density
                val step = unit * 0.95f
                val width = unit * 0.8f
                var y = -step
                while (y < h + step * 2) {
                    val path = Path()
                    var x = 0f
                    var up = true
                    path.moveTo(0f, y)
                    while (x < w + width) {
                        x += width
                        path.lineTo(x, if (up) y - width * 0.55f else y)
                        up = !up
                    }
                    canvas.drawPath(path, paint)
                    y += step
                }
            }

            Pattern.HONEYCOMB -> {
                paint.strokeWidth = 1.2f * density
                val radius = unit * 0.62f
                val horizontal = radius * 1.732f
                val vertical = radius * 1.5f
                var row = 0
                var y = 0f
                while (y < h + vertical) {
                    val shift = if (row % 2 == 0) 0f else horizontal / 2
                    var x = shift
                    while (x < w + horizontal) {
                        canvas.drawPath(hexagon(x, y, radius), paint)
                        x += horizontal
                    }
                    y += vertical
                    row++
                }
            }

            Pattern.PLUS -> {
                paint.strokeWidth = 1.8f * density
                val arm = unit * 0.22f
                var row = 0
                var y = unit / 2
                while (y < h + unit) {
                    val shift = if (row % 2 == 0) 0f else unit / 2
                    var x = shift
                    while (x < w + unit) {
                        canvas.drawLine(x - arm, y, x + arm, y, paint)
                        canvas.drawLine(x, y - arm, x, y + arm, paint)
                        x += unit
                    }
                    y += unit
                    row++
                }
            }

            Pattern.WAVES -> {
                paint.strokeWidth = 1.8f * density
                val amplitude = unit * 0.28f
                val wavelength = unit * 1.7f
                val stepX = 4f * density
                var y = 0f
                while (y < h + unit) {
                    val path = Path()
                    var x = 0f
                    path.moveTo(0f, y)
                    while (x < w) {
                        x += stepX
                        path.lineTo(x, y + sin(x / wavelength * 6.2832f) * amplitude)
                    }
                    canvas.drawPath(path, paint)
                    y += unit * 0.85f
                }
            }

            Pattern.CONFETTI -> {
                // Fester Startwert: das Muster soll bei jedem Zeichnen gleich aussehen.
                paint.strokeWidth = 3f * scale * density
                val random = Random(42)
                val count = ((w * h) / (unit * unit * 1.6f)).toInt().coerceIn(30, 900)
                repeat(count) {
                    val x = random.nextFloat() * w
                    val y = random.nextFloat() * h
                    val length = unit * (0.16f + random.nextFloat() * 0.2f)
                    val angle = random.nextFloat() * 6.2832f
                    canvas.drawLine(
                        x, y,
                        x + cos(angle) * length,
                        y + sin(angle) * length,
                        paint
                    )
                }
            }
        }
    }

    private fun hexagon(cx: Float, cy: Float, radius: Float): Path {
        val path = Path()
        for (i in 0 until 6) {
            val angle = Math.toRadians((60.0 * i) - 30.0)
            val x = cx + radius * cos(angle).toFloat()
            val y = cy + radius * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        fill.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        fill.colorFilter = colorFilter
    }

    @Deprecated("Von Drawable vorgegeben", ReplaceWith("PixelFormat.TRANSLUCENT"))
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

/** Farben, die als Muster zur Auswahl stehen. */
object PatternColors {

    const val DEFAULT_ON_HEADER = 0xFFFFFFFF.toInt()

    /** Beschriftung und Grundfarbe. Die Staerke regelt spaeter die Deckkraft. */
    val CHOICES: List<Pair<String, Int>> = listOf(
        "Weiß" to 0xFFFFFFFF.toInt(),
        "Schwarz" to 0xFF000000.toInt()
    ) + Theming.ACCENTS

    /** Farbe mit der eingestellten Staerke (0..100 Prozent) als Deckkraft. */
    fun withStrength(color: Int, strengthPercent: Int): Int =
        ColorUtils.setAlphaComponent(
            color,
            (strengthPercent.coerceIn(0, 100) * 255 / 100)
        )
}
