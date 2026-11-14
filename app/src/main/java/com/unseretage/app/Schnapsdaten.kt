package com.unseretage.app

import java.time.LocalDate
import java.time.Period

/**
 * Ein Schnapsdatum: Jahre, Monate und Tage sind dieselbe Zahl,
 * also zum Beispiel 7 Jahre, 7 Monate und 7 Tage zusammen.
 *
 * Das ist etwas anderes als eine Schnapszahl in [Milestones]. Dort geht es um
 * die Gesamtzahl gemeinsamer Tage (777, 7777), hier um das Muster in der
 * Aufteilung nach Jahren, Monaten und Tagen. Ein Schnapsdatum trifft darum
 * so gut wie nie mit einer runden Tageszahl zusammen.
 */
data class Schnapsdatum(val count: Int, val date: LocalDate) {

    val label: String get() = "Schnapsdatum"

    /** "7 Jahre, 7 Monate und 7 Tage", bzw. im Singular "1 Jahr, 1 Monat und 1 Tag". */
    val text: String
        get() {
            val jahre = if (count == 1) "Jahr" else "Jahre"
            val monate = if (count == 1) "Monat" else "Monate"
            val tage = if (count == 1) "Tag" else "Tage"
            return "$count $jahre, $count $monate und $count $tage"
        }

    /** "7/7/7" – kurz fuer Titelzeilen. */
    val short: String get() = "$count/$count/$count"
}

object Schnapsdaten {

    /**
     * Nur 1 bis 11 kommen vor: ab 12 Monaten wird daraus ein weiteres Jahr,
     * das Muster kann in der Aufteilung also gar nicht mehr auftauchen.
     */
    private val COUNTS = 1..11

    /**
     * Das Datum, an dem die Aufteilung genau [count] Jahre, [count] Monate und
     * [count] Tage ergibt, oder null wenn es so einen Tag nicht gibt.
     *
     * Der erste Versuch ist Startdatum + n Jahre + n Monate + n Tage. Bei einem
     * 29. Februar als Startdatum kann das Addieren auf das Monatsende gestaucht
     * werden, dann weicht die Aufteilung um einen Tag ab. Darum wird in einem
     * kleinen Fenster nachgesehen: Erinnerung und Anzeige auf der Startseite
     * sollen dasselbe sagen, und die Startseite rechnet mit [Period].
     */
    fun dateFor(base: LocalDate, count: Int): LocalDate? {
        val wanted = Period.of(count, count, count)
        val guess = base
            .plusYears(count.toLong())
            .plusMonths(count.toLong())
            .plusDays(count.toLong())
        for (shift in 0L..3L) {
            if (Period.between(base, guess.plusDays(shift)) == wanted) return guess.plusDays(shift)
            if (Period.between(base, guess.minusDays(shift)) == wanted) return guess.minusDays(shift)
        }
        return null
    }

    private fun all(base: LocalDate): List<Schnapsdatum> =
        COUNTS.mapNotNull { count -> dateFor(base, count)?.let { Schnapsdatum(count, it) } }

    /** Das naechste Schnapsdatum nach heute, oder null wenn keins mehr kommt. */
    fun next(prefs: Prefs, base: LocalDate, today: LocalDate): Schnapsdatum? {
        if (!prefs.milestoneTriples) return null
        return all(base).firstOrNull { it.date.isAfter(today) }
    }

    /** Ist heute selbst ein Schnapsdatum? */
    fun onDay(prefs: Prefs, base: LocalDate, today: LocalDate): Schnapsdatum? {
        if (!prefs.milestoneTriples) return null
        return all(base).firstOrNull { it.date == today }
    }
}
