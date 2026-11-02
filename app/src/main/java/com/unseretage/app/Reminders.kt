package com.unseretage.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Reminder(val title: String, val text: String)

/** Entscheidet, welche Erinnerungen an einem bestimmten Tag fällig sind. */
object Reminders {

    fun forDay(prefs: Prefs, today: LocalDate): List<Reminder> {
        val out = mutableListOf<Reminder>()
        val couple = coupleName(prefs)

        addAnniversaryReminders(out, prefs, today, prefs.meetDate, "Jahrestag", "zusammen", couple)
        addAnniversaryReminders(out, prefs, today, prefs.weddingDate, "Hochzeitstag", "verheiratet", couple)

        if (prefs.milestonesEnabled) {
            val base = prefs.meetDate ?: prefs.weddingDate
            if (base != null) {
                val days = DateMath.totalDays(base, today)
                if (days > 0 && days % 1000L == 0L) {
                    out += Reminder(
                        "${DateMath.formatNumber(days)} gemeinsame Tage",
                        "${couple}Heute sind es ${DateMath.formatNumber(days)} gemeinsame Tage."
                    )
                }
            }
        }

        if (prefs.monthlyEnabled) {
            val base = prefs.meetDate
            if (base != null && isMonthlyDay(base, today) && !isYearlyDay(base, today)) {
                val months = ChronoUnit.MONTHS.between(base, today)
                if (months > 0) {
                    out += Reminder(
                        "$months Monate",
                        "${coupleName(prefs)}Heute seid ihr $months Monate zusammen."
                    )
                }
            }
        }

        return out
    }

    private fun addAnniversaryReminders(
        out: MutableList<Reminder>,
        prefs: Prefs,
        today: LocalDate,
        date: LocalDate?,
        word: String,
        verb: String,
        couple: String
    ) {
        if (date == null) return
        val until = DateMath.daysUntilNextAnniversary(date, today)
        val years = DateMath.yearsAtNextAnniversary(date, today)

        if (until == 0L) {
            out += Reminder(
                "Heute: $years. $word",
                "${couple}Seit $years ${if (years == 1) "Jahr" else "Jahren"} $verb."
            )
            return
        }

        val warn = prefs.preWarnDays
        if (warn > 0 && until.toInt() == warn) {
            val on = DateMath.nextAnniversary(date, today)
            out += Reminder(
                "$word in $until Tagen",
                "${couple}Am ${on.dayOfMonth}.${on.monthValue}. ist euer $years. $word. Zeit, etwas zu planen."
            )
        }
    }

    /** Monatstag, der auch in kurzen Monaten funktioniert (31. -> letzter Tag). */
    private fun isMonthlyDay(base: LocalDate, today: LocalDate): Boolean {
        val target = minOf(base.dayOfMonth, today.lengthOfMonth())
        return today.dayOfMonth == target
    }

    private fun isYearlyDay(base: LocalDate, today: LocalDate): Boolean =
        DateMath.daysUntilNextAnniversary(base, today) == 0L

    /** "Marcel & Jenny - " oder leer, wenn keine Namen gesetzt sind. */
    private fun coupleName(prefs: Prefs): String {
        val self = prefs.nameSelf.trim()
        val partner = prefs.namePartner.trim()
        return when {
            self.isNotEmpty() && partner.isNotEmpty() -> "$self & $partner \u2013 "
            else -> ""
        }
    }
}
