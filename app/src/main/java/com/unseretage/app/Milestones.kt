package com.unseretage.app

/** Ein erreichbarer Meilenstein in gemeinsamen Tagen. */
data class Milestone(val days: Long, val kind: Kind) {

    enum class Kind(val label: String) {
        REPDIGIT("Schnapszahl"),
        THOUSAND("Tausender"),
        HUNDRED("Hunderter")
    }

    val label: String get() = kind.label
}

/**
 * Welche runden Zahlen gefeiert werden. Mehrere Arten koennen gleichzeitig
 * aktiv sein; trifft eine Zahl auf mehrere zu, gewinnt die auffaelligste.
 */
object Milestones {

    /** 11, 22, ... 111, 222, ... bis in den Bereich von rund 274 Jahren. */
    private val REPDIGITS: List<Long> = buildList {
        for (length in 2..7) {
            val repunit = "1".repeat(length).toLong()
            for (digit in 1..9) add(repunit * digit)
        }
    }.sorted()

    /** Der naechste faellige Meilenstein nach [days], oder null wenn keine Art aktiv ist. */
    fun next(prefs: Prefs, days: Long): Milestone? {
        val candidates = mutableListOf<Milestone>()

        if (prefs.milestoneRepdigits) {
            REPDIGITS.firstOrNull { it > days }?.let { candidates += Milestone(it, Milestone.Kind.REPDIGIT) }
        }
        if (prefs.milestoneThousands) {
            candidates += Milestone((days / 1000 + 1) * 1000, Milestone.Kind.THOUSAND)
        }
        if (prefs.milestoneHundreds) {
            candidates += Milestone((days / 100 + 1) * 100, Milestone.Kind.HUNDRED)
        }

        val soonest = candidates.minByOrNull { it.days } ?: return null
        // Faellt dasselbe Datum unter mehrere Arten, die auffaelligste benennen.
        return Milestone(soonest.days, kindOf(prefs, soonest.days) ?: soonest.kind)
    }

    /** Ist [days] selbst ein Meilenstein? Dann mit passender Bezeichnung zurueckgeben. */
    fun onDay(prefs: Prefs, days: Long): Milestone? {
        if (days <= 0) return null
        val kind = kindOf(prefs, days) ?: return null
        return Milestone(days, kind)
    }

    private fun kindOf(prefs: Prefs, days: Long): Milestone.Kind? = when {
        prefs.milestoneRepdigits && REPDIGITS.contains(days) -> Milestone.Kind.REPDIGIT
        prefs.milestoneThousands && days % 1000L == 0L -> Milestone.Kind.THOUSAND
        prefs.milestoneHundreds && days % 100L == 0L -> Milestone.Kind.HUNDRED
        else -> null
    }

    fun anyEnabled(prefs: Prefs): Boolean =
        prefs.milestoneRepdigits || prefs.milestoneThousands || prefs.milestoneHundreds
}
