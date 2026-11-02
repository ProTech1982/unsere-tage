package com.unseretage.app

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
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

    private var pendingApkUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Notifier.createChannel(this)

        binding.btnSettings.setOnClickListener { openSettings() }
        binding.btnSetup.setOnClickListener { openSettings() }
        binding.btnCheckUpdate.setOnClickListener { checkForUpdate() }
        binding.btnInstallUpdate.setOnClickListener { installUpdate() }

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

    // ---------- Darstellung ----------

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

        applyTheming(prefs)
        applyPhoto(prefs)

        bindCounterCard(binding.cardMeet, "Kennengelernt", prefs.meetDate, today, "Jahrestag")
        bindCounterCard(binding.cardWedding, "Hochzeitstag", prefs.weddingDate, today, "Hochzeitstag")

        renderMilestone(prefs, today)

        binding.tvVersion.text =
            "Version ${Updater.installedName(this)} installiert (Build ${Updater.installedBuild(this)})"

        binding.tvNotifyInfo.text = if (prefs.isConfigured) {
            String.format(Locale.GERMANY, "Erinnerungen um %02d:%02d Uhr", prefs.notifyHour, prefs.notifyMinute)
        } else {
            ""
        }
    }

    /** Farbe, Kachelform und Transparenz aus den Einstellungen anwenden. */
    private fun applyTheming(prefs: Prefs) {
        val accent = prefs.accentColor
        val corner = prefs.cardCornerDp
        val opacity = prefs.cardOpacity

        binding.headerBox.background = Theming.headerBackground(this, accent)

        val surface = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorSurface, 0xFFFFFFFF.toInt()
        )
        val track = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorSurfaceVariant, 0xFFF8EFF2.toInt()
        )
        val readable = Theming.readableOn(accent, surface)
        val accentTint = ColorStateList.valueOf(accent)

        listOf(
            binding.cardSetup,
            binding.cardMeet.root,
            binding.cardWedding.root,
            binding.cardMilestone,
            binding.cardUpdate
        ).forEach { Theming.applyCard(it, corner, opacity) }

        listOf(binding.cardMeet, binding.cardWedding).forEach { card ->
            card.tvCardBig.setTextColor(readable)
            card.progressYear.setIndicatorColor(readable)
            card.progressYear.trackColor = track
        }

        binding.progressUpdate.setIndicatorColor(readable)
        binding.progressUpdate.trackColor = track
        binding.btnInstallUpdate.backgroundTintList = accentTint
        binding.btnSetup.backgroundTintList = accentTint
    }

    private fun applyPhoto(prefs: Prefs) {
        val style = prefs.photoStyle
        val uri = prefs.photoUri

        binding.ivBackgroundPhoto.visibility = View.GONE
        binding.vBackgroundScrim.visibility = View.GONE
        binding.ivBanner.visibility = View.GONE
        binding.vBannerScrim.visibility = View.GONE
        binding.ivAvatar.visibility = View.GONE

        if (uri == null || style == PhotoStyle.NONE) return

        val screenWidth = resources.displayMetrics.widthPixels.coerceAtLeast(480)
        val target = if (style == PhotoStyle.AVATAR) 320 else screenWidth
        val bitmap: Bitmap = PhotoLoader.load(this, uri, target) ?: return

        when (style) {
            PhotoStyle.BANNER -> {
                binding.ivBanner.setImageBitmap(bitmap)
                binding.ivBanner.visibility = View.VISIBLE
                binding.vBannerScrim.visibility = View.VISIBLE
            }
            PhotoStyle.BACKGROUND -> {
                binding.ivBackgroundPhoto.setImageBitmap(bitmap)
                binding.ivBackgroundPhoto.visibility = View.VISIBLE
                binding.vBackgroundScrim.visibility = View.VISIBLE
            }
            PhotoStyle.AVATAR -> {
                binding.ivAvatar.setImageBitmap(bitmap)
                binding.ivAvatar.visibility = View.VISIBLE
            }
            PhotoStyle.NONE -> Unit
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
        countUp(card.tvCardBig, days)
        card.tvCardUnit.text = if (days == 1L) "Tag" else "Tage"
        card.tvCardPeriod.text = DateMath.formatPeriod(DateMath.elapsed(date, today))

        card.progressYear.setProgressCompat(DateMath.anniversaryProgress(date, today), animationsOn())

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
        val milestone = base?.let { Milestones.next(prefs, DateMath.totalDays(it, today)) }

        if (base == null || milestone == null) {
            binding.cardMilestone.visibility = View.GONE
            return
        }

        val done = DateMath.totalDays(base, today)
        val until = milestone.days - done
        val reachedOn = base.plusDays(milestone.days)
        binding.cardMilestone.visibility = View.VISIBLE
        binding.tvMilestone.text = "${milestone.label}: ${DateMath.formatNumber(milestone.days)} " +
            "gemeinsame Tage am ${reachedOn.format(dateFormat)} \u2013 ${DateMath.formatDays(until)}."
    }

    /** Zaehlt die grosse Zahl kurz hoch, sofern Animationen eingeschaltet sind. */
    private fun countUp(view: TextView, value: Long) {
        if (!animationsOn() || value <= 0) {
            view.text = DateMath.formatNumber(value)
            return
        }
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900
            addUpdateListener {
                val shown = (value * (it.animatedValue as Float)).toLong()
                view.text = DateMath.formatNumber(shown)
            }
            start()
        }
    }

    private fun animationsOn(): Boolean = runCatching {
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }.getOrDefault(true)

    // ---------- Update ----------

    private fun checkForUpdate() {
        binding.btnCheckUpdate.isEnabled = false
        binding.btnInstallUpdate.visibility = View.GONE
        showStatus("Suche nach einer neuen Version …")

        Updater.check(this) { result ->
            binding.btnCheckUpdate.isEnabled = true
            when (result) {
                is Updater.CheckResult.Available -> {
                    pendingApkUrl = result.apkUrl
                    showStatus("${result.name} ist verfügbar.")
                    binding.btnInstallUpdate.text = "Jetzt installieren"
                    binding.btnInstallUpdate.visibility = View.VISIBLE
                }
                Updater.CheckResult.UpToDate ->
                    showStatus("Du hast bereits die neueste Version.")
                is Updater.CheckResult.NeedsBrowser -> {
                    showStatus(result.reason)
                    binding.btnInstallUpdate.text = "Im Browser öffnen"
                    binding.btnInstallUpdate.visibility = View.VISIBLE
                    pendingApkUrl = null
                }
                is Updater.CheckResult.Failed ->
                    showStatus(result.reason)
            }
        }
    }

    private fun installUpdate() {
        val url = pendingApkUrl
        if (url == null) {
            Updater.openReleasesPage(this)
            return
        }

        binding.btnInstallUpdate.isEnabled = false
        binding.progressUpdate.visibility = View.VISIBLE
        binding.progressUpdate.isIndeterminate = true
        showStatus("Lade herunter …")

        Updater.downloadAndInstall(
            context = this,
            apkUrl = url,
            onProgress = { percent ->
                if (percent >= 0) {
                    binding.progressUpdate.isIndeterminate = false
                    binding.progressUpdate.setProgressCompat(percent, true)
                    if (percent >= 100) showStatus("Installation wird geöffnet …")
                }
            },
            onError = { message ->
                binding.btnInstallUpdate.isEnabled = true
                binding.progressUpdate.visibility = View.GONE
                showStatus(message)
            }
        )
    }

    private fun showStatus(text: String) {
        binding.tvUpdateStatus.text = text
        binding.tvUpdateStatus.visibility = View.VISIBLE
    }
}
