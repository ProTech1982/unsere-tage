package com.unseretage.app

import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.time.temporal.ChronoUnit
import java.text.NumberFormat
import java.util.Locale

object DateMath {

    private val numbers: NumberFormat = NumberFormat.getIntegerInstance(Locale.GERMANY)

    /** Volle Jahre, Monate und Tage zwischen zwei Daten. */
    fun elapsed(from: LocalDate, to: LocalDate): Period = Period.between(from, to)

    fun totalDays(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    /**
     * Der Jahrestag in einem bestimmten Jahr. Ein 29. Februar fällt
     * in Nicht-Schaltjahren auf den 1. März.
     */
    fun anniversaryIn(from: LocalDate, year: Int): LocalDate =
        if (from.monthValue == 2 && from.dayOfMonth == 29 && !Year.isLeap(year.toLong())) {
            LocalDate.of(year, 3, 1)
        } else {
            LocalDate.of(year, from.monthValue, from.dayOfMonth)
        }

    /** Der nächste Jahrestag ab heute; heute selbst zählt noch dazu. */
    fun nextAnniversary(from: LocalDate, today: LocalDate): LocalDate {
        val thisYear = anniversaryIn(from, today.year)
        return if (thisYear.isBefore(today)) anniversaryIn(from, today.year + 1) else thisYear
    }

    fun daysUntilNextAnniversary(from: LocalDate, today: LocalDate): Long =
        ChronoUnit.DAYS.between(today, nextAnniversary(from, today))

    /** Wie viele Jahre der nächste Jahrestag markiert. */
    fun yearsAtNextAnniversary(from: LocalDate, today: LocalDate): Int =
        nextAnniversary(from, today).year - from.year

    /** Wie weit das laufende Jahr bis zum nächsten Jahrestag fortgeschritten ist, 0..100. */
    fun anniversaryProgress(from: LocalDate, today: LocalDate): Int {
        val next = nextAnniversary(from, today)
        val previous = anniversaryIn(from, next.year - 1)
        val span = ChronoUnit.DAYS.between(previous, next).coerceAtLeast(1)
        val done = ChronoUnit.DAYS.between(previous, today).coerceIn(0, span)
        return ((done * 100) / span).toInt()
    }

    /** Nächster Tausender-Meilenstein, z. B. 5000 Tage. */
    fun nextMilestone(totalDays: Long): Long = (totalDays / 1000 + 1) * 1000

    fun daysUntilMilestone(from: LocalDate, today: LocalDate): Long {
        val done = totalDays(from, today)
        return nextMilestone(done) - done
    }

    /** "12 Jahre, 3 Monate und 5 Tage" */
    fun formatPeriod(period: Period): String {
        val parts = mutableListOf<String>()
        if (period.years > 0) parts += "${period.years} ${if (period.years == 1) "Jahr" else "Jahre"}"
        if (period.months > 0) parts += "${period.months} ${if (period.months == 1) "Monat" else "Monate"}"
        if (period.days > 0) parts += "${period.days} ${if (period.days == 1) "Tag" else "Tage"}"
        return when (parts.size) {
            0 -> "Heute ist der Tag"
            1 -> parts[0]
            else -> parts.dropLast(1).joinToString(", ") + " und " + parts.last()
        }
    }

    fun formatNumber(value: Long): String = numbers.format(value)

    fun formatDays(value: Long): String = when (value) {
        0L -> "heute"
        1L -> "morgen"
        else -> "in ${formatNumber(value)} Tagen"
    }
}
