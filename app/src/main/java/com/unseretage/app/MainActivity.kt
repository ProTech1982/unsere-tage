package com.unseretage.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.unseretage.app.databinding.ActivityMainBinding
import com.unseretage.app.databinding.ItemCounterCardBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val dateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMANY)

    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Notifier.createChannel(this)

        binding.btnSettings.setOnClickListener { openSettings() }
        binding.btnSetup.setOnClickListener { openSettings() }

        requestNotificationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        render()
        AlarmScheduler.scheduleNext(this)
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun render() {
        val prefs = Prefs(this)
        val today = LocalDate.now()

        val self = prefs.nameSelf.trim()
        val partner = prefs.namePartner.trim()
        binding.tvNames.text = when {
            self.isNotEmpty() && partner.isNotEmpty() -> "$self & $partner"
            self.isNotEmpty() -> self
            partner.isNotEmpty() -> partner
            else -> getString(R.string.app_name)
        }
        binding.tvSubtitle.text =
            if (prefs.isConfigured) "Eure gemeinsame Zeit" else "Noch nicht eingerichtet"

        binding.cardSetup.visibility = if (prefs.isConfigured) View.GONE else View.VISIBLE

        bindCounterCard(binding.cardMeet, "Kennengelernt", prefs.meetDate, today, "Jahrestag")
        bindCounterCard(binding.cardWedding, "Hochzeitstag", prefs.weddingDate, today, "Hochzeitstag")

        renderMilestone(prefs, today)

        binding.tvNotifyInfo.text = if (prefs.isConfigured) {
            String.format(Locale.GERMANY, "Erinnerungen um %02d:%02d Uhr", prefs.notifyHour, prefs.notifyMinute)
        } else {
            ""
        }
    }

    private fun bindCounterCard(
        card: ItemCounterCardBinding,
        label: String,
        date: LocalDate?,
        today: LocalDate,
        anniversaryWord: String
    ) {
        if (date == null) {
            card.root.visibility = View.GONE
            return
        }
        card.root.visibility = View.VISIBLE
        card.tvCardLabel.text = label
        card.tvCardDate.text = date.format(dateFormat)

        val days = DateMath.totalDays(date, today)
        card.tvCardBig.text = "${DateMath.formatNumber(days)} Tage"
        card.tvCardPeriod.text = DateMath.formatPeriod(DateMath.elapsed(date, today))

        val until = DateMath.daysUntilNextAnniversary(date, today)
        val years = DateMath.yearsAtNextAnniversary(date, today)
        val next = DateMath.nextAnniversary(date, today)
        card.tvCardNext.text = if (until == 0L) {
            "Heute ist euer $years. $anniversaryWord."
        } else {
            "$years. $anniversaryWord am ${next.format(dateFormat)} – ${DateMath.formatDays(until)}."
        }
    }

    private fun renderMilestone(prefs: Prefs, today: LocalDate) {
        val base = prefs.meetDate ?: prefs.weddingDate
        if (base == null || !prefs.milestonesEnabled) {
            binding.cardMilestone.visibility = View.GONE
            return
        }
        val done = DateMath.totalDays(base, today)
        val target = DateMath.nextMilestone(done)
        val until = target - done
        val reachedOn = base.plusDays(target)
        binding.cardMilestone.visibility = View.VISIBLE
        binding.tvMilestone.text =
            "${DateMath.formatNumber(target)} gemeinsame Tage am ${reachedOn.format(dateFormat)} – ${DateMath.formatDays(until)}."
    }
}
