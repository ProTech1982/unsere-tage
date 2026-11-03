package com.unseretage.app

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.unseretage.app.databinding.ActivitySettingsBinding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: Prefs

    private val dateFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.GERMANY)

    private var meetDate: LocalDate? = null
    private var weddingDate: LocalDate? = null
    private var hour = 9
    private var minute = 0
    private var photoUri: String? = null
    private var cornerDp = 24
    private var opacity = 100
    private var accent = Theming.DEFAULT_ACCENT
    private var themeMode = "system"
    private var pendingPageUrl: String? = null
    private val swatches = mutableListOf<View>()

    private var headerPattern = Pattern.NONE
    private var headerPatternColor = PatternColors.DEFAULT_ON_HEADER
    private var headerPatternStrength = 20
    private var bgPattern = Pattern.NONE
    private var bgPatternColor = Theming.DEFAULT_ACCENT
    private var bgPatternStrength = 14

    private val headerPatternTiles = mutableListOf<View>()
    private val bgPatternTiles = mutableListOf<View>()
    private val headerPatternSwatches = mutableListOf<View>()
    private val bgPatternSwatches = mutableListOf<View>()

    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            // Dauerhafte Leseerlaubnis, damit das Bild auch nach einem Neustart da ist.
            runCatching {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            photoUri = uri.toString()
            updatePhotoViews()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = Prefs(this)
        loadIntoForm()

        binding.btnMeetDate.setOnClickListener {
            pickDate("Kennenlerntag", meetDate) { meetDate = it; updateDateButtons() }
        }
        binding.btnWeddingDate.setOnClickListener {
            pickDate("Hochzeitstag", weddingDate) { weddingDate = it; updateDateButtons() }
        }
        binding.btnTime.setOnClickListener { pickTime() }

        binding.btnLegal.setOnClickListener {
            startActivity(Intent(this, LegalActivity::class.java))
        }

        binding.btnPickPhoto.setOnClickListener {
            pickPhoto.launch(arrayOf("image/*"))
        }
        binding.btnRemovePhoto.setOnClickListener {
            photoUri = null
            updatePhotoViews()
        }

        binding.groupShape.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            cornerDp = when (checkedId) {
                R.id.btnShapeSharp -> Theming.SHAPES[0].second
                R.id.btnShapeRound -> Theming.SHAPES[2].second
                else -> Theming.SHAPES[1].second
            }
            updatePreview()
        }

        binding.groupTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            themeMode = when (checkedId) {
                R.id.btnThemeLight -> "light"
                R.id.btnThemeDark -> "dark"
                else -> "system"
            }
        }

        binding.sliderOpacity.addOnChangeListener { _, value, _ ->
            opacity = value.toInt()
            updatePreview()
        }

        buildColorRow()
        buildPatternRows()

        binding.sliderHeaderPattern.addOnChangeListener { _, value, _ ->
            headerPatternStrength = value.toInt()
            paintPatternRows()
        }
        binding.sliderBgPattern.addOnChangeListener { _, value, _ ->
            bgPatternStrength = value.toInt()
            paintPatternRows()
        }

        binding.swPreWarn.setOnCheckedChangeListener { _, checked ->
            binding.groupPreWarn.visibility = if (checked) View.VISIBLE else View.GONE
        }

        binding.tvVersion.text =
            "Version ${Updater.installedName(this)} installiert (Build ${Updater.installedBuild(this)})"
        binding.btnCheckUpdate.setOnClickListener { checkForUpdate() }
        binding.btnInstallUpdate.setOnClickListener {
            Updater.openReleasesPage(this, pendingPageUrl)
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnTest.setOnClickListener {
            Notifier.showTest(this)
            Toast.makeText(this, "Testbenachrichtigung gesendet", Toast.LENGTH_SHORT).show()
        }

        showExactAlarmHintIfNeeded()
    }

    private fun loadIntoForm() {
        binding.etNameSelf.setText(prefs.nameSelf)
        binding.etNamePartner.setText(prefs.namePartner)
        meetDate = prefs.meetDate
        weddingDate = prefs.weddingDate
        hour = prefs.notifyHour
        minute = prefs.notifyMinute
        photoUri = prefs.photoUri
        cornerDp = prefs.cardCornerDp
        opacity = prefs.cardOpacity
        accent = prefs.accentColor

        binding.groupShape.check(
            when (cornerDp) {
                Theming.SHAPES[0].second -> R.id.btnShapeSharp
                Theming.SHAPES[2].second -> R.id.btnShapeRound
                else -> R.id.btnShapeSoft
            }
        )
        binding.sliderOpacity.value = opacity.toFloat().coerceIn(30f, 100f)

        binding.groupPhotoStyle.check(
            when (prefs.photoStyle) {
                PhotoStyle.BACKGROUND -> R.id.btnStyleBackground
                PhotoStyle.AVATAR -> R.id.btnStyleAvatar
                else -> R.id.btnStyleBanner
            }
        )

        val warn = prefs.preWarnDays
        binding.swPreWarn.isChecked = warn > 0
        binding.groupPreWarn.visibility = if (warn > 0) View.VISIBLE else View.GONE
        binding.groupPreWarn.check(
            when (warn) {
                3 -> R.id.btnWarn3
                14 -> R.id.btnWarn14
                else -> R.id.btnWarn7
            }
        )

        binding.swMsRepdigits.isChecked = prefs.milestoneRepdigits
        binding.swMsThousands.isChecked = prefs.milestoneThousands
        binding.swMsHundreds.isChecked = prefs.milestoneHundreds

        themeMode = prefs.themeMode
        binding.groupTheme.check(
            when (themeMode) {
                "light" -> R.id.btnThemeLight
                "dark" -> R.id.btnThemeDark
                else -> R.id.btnThemeSystem
            }
        )
        binding.swMonthly.isChecked = prefs.monthlyEnabled

        headerPattern = prefs.headerPattern
        headerPatternColor = prefs.headerPatternColor
        headerPatternStrength = prefs.headerPatternStrength
        bgPattern = prefs.backgroundPattern
        bgPatternColor = prefs.backgroundPatternColor
        bgPatternStrength = prefs.backgroundPatternStrength
        binding.sliderHeaderPattern.value = headerPatternStrength.toFloat().coerceIn(5f, 100f)
        binding.sliderBgPattern.value = bgPatternStrength.toFloat().coerceIn(5f, 100f)

        updateDateButtons()
        updateTimeButton()
        updatePhotoViews()
        updatePreview()
    }

    private fun updateDateButtons() {
        binding.btnMeetDate.text = meetDate?.let { "Kennenlerntag: ${it.format(dateFormat)}" }
            ?: "Kennenlerntag wählen"
        binding.btnWeddingDate.text = weddingDate?.let { "Hochzeitstag: ${it.format(dateFormat)}" }
            ?: "Hochzeitstag wählen"
    }

    private fun updateTimeButton() {
        binding.btnTime.text = String.format(Locale.GERMANY, "Erinnerung um %02d:%02d Uhr", hour, minute)
    }

    private fun updatePhotoViews() {
        val bitmap = PhotoLoader.load(this, photoUri, 256)
        if (bitmap != null) {
            binding.ivPhotoPreview.setImageBitmap(bitmap)
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.VISIBLE
            binding.btnPickPhoto.text = "Anderes Foto wählen"
        } else {
            binding.ivPhotoPreview.visibility = View.GONE
            binding.btnRemovePhoto.visibility = View.GONE
            binding.btnPickPhoto.text = "Foto wählen"
        }
    }

    private fun pickDate(title: String, current: LocalDate?, onPicked: (LocalDate) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection((current ?: LocalDate.now()).toEpochDay() * MILLIS_PER_DAY)
            .build()
        picker.addOnPositiveButtonClickListener { utcMillis ->
            onPicked(Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate())
        }
        picker.show(supportFragmentManager, "date")
    }

    private fun pickTime() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Wann sollen wir erinnern?")
            .build()
        picker.addOnPositiveButtonClickListener {
            hour = picker.hour
            minute = picker.minute
            updateTimeButton()
        }
        picker.show(supportFragmentManager, "time")
    }

    private fun showExactAlarmHintIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!am.canScheduleExactAlarms()) {
            binding.tvExactHint.visibility = View.VISIBLE
            binding.tvExactHint.text =
                "Hinweis: Ohne die Berechtigung für exakte Alarme kann die Erinnerung ein " +
                    "Stück später als zur eingestellten Uhrzeit kommen. Am selben Tag kommt sie trotzdem."
        }
    }

    private fun save() {
        prefs.nameSelf = binding.etNameSelf.text?.toString()?.trim().orEmpty()
        prefs.namePartner = binding.etNamePartner.text?.toString()?.trim().orEmpty()
        prefs.meetDate = meetDate
        prefs.weddingDate = weddingDate
        prefs.notifyHour = hour
        prefs.notifyMinute = minute
        prefs.photoUri = photoUri
        prefs.photoStyle = when (binding.groupPhotoStyle.checkedButtonId) {
            R.id.btnStyleBackground -> PhotoStyle.BACKGROUND
            R.id.btnStyleAvatar -> PhotoStyle.AVATAR
            else -> PhotoStyle.BANNER
        }
        prefs.preWarnDays = if (binding.swPreWarn.isChecked) {
            when (binding.groupPreWarn.checkedButtonId) {
                R.id.btnWarn3 -> 3
                R.id.btnWarn14 -> 14
                else -> 7
            }
        } else {
            0
        }
        prefs.cardCornerDp = cornerDp
        prefs.cardOpacity = opacity
        prefs.accentColor = accent
        prefs.milestoneRepdigits = binding.swMsRepdigits.isChecked
        prefs.milestoneThousands = binding.swMsThousands.isChecked
        prefs.milestoneHundreds = binding.swMsHundreds.isChecked
        prefs.headerPattern = headerPattern
        prefs.headerPatternColor = headerPatternColor
        prefs.headerPatternStrength = headerPatternStrength
        prefs.backgroundPattern = bgPattern
        prefs.backgroundPatternColor = bgPatternColor
        prefs.backgroundPatternStrength = bgPatternStrength
        prefs.themeMode = themeMode
        App.applyThemeMode(themeMode)
        prefs.monthlyEnabled = binding.swMonthly.isChecked

        AlarmScheduler.scheduleNext(this)
        Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show()
        finish()
    }

    // ---------- Aussehen ----------

    private fun buildColorRow() {
        val row: LinearLayout = binding.colorRow
        row.removeAllViews()
        swatches.clear()

        val size = (46 * resources.displayMetrics.density).toInt()
        val gap = (10 * resources.displayMetrics.density).toInt()

        Theming.ACCENTS.forEach { (name, color) ->
            val swatch = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.marginEnd = gap
                }
                contentDescription = name
                tag = color
                setOnClickListener {
                    accent = color
                    paintSwatches()
                    updatePreview()
                    // Die Vorschau der Kopf-Muster liegt auf der Akzentfarbe.
                    paintPatternRows()
                }
            }
            row.addView(swatch)
            swatches += swatch
        }
        paintSwatches()
    }

    private fun paintSwatches() {
        val ring = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorOnSurface, 0xFF2A1F23.toInt()
        )
        val strokeWidth = (3 * resources.displayMetrics.density).toInt()

        swatches.forEach { swatch ->
            val color = swatch.tag as Int
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                if (color == accent) setStroke(strokeWidth, ring)
            }
            swatch.background = drawable
        }
    }

    // ---------- Muster ----------

    private fun buildPatternRows() {
        buildPatternTiles(binding.headerPatternRow, headerPatternTiles) { picked ->
            headerPattern = picked
            paintPatternRows()
        }
        buildPatternTiles(binding.bgPatternRow, bgPatternTiles) { picked ->
            bgPattern = picked
            paintPatternRows()
        }
        buildPatternSwatches(binding.headerPatternColorRow, headerPatternSwatches) { picked ->
            headerPatternColor = picked
            paintPatternRows()
        }
        buildPatternSwatches(binding.bgPatternColorRow, bgPatternSwatches) { picked ->
            bgPatternColor = picked
            paintPatternRows()
        }
        paintPatternRows()
    }

    /** Eine Reihe kleiner Vorschauen, eine je Muster. */
    private fun buildPatternTiles(
        row: LinearLayout,
        tiles: MutableList<View>,
        onPick: (Pattern) -> Unit
    ) {
        row.removeAllViews()
        tiles.clear()

        val density = resources.displayMetrics.density
        val width = (74 * density).toInt()
        val height = (54 * density).toInt()
        val gap = (10 * density).toInt()

        Pattern.entries.forEach { pattern ->
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = gap }
                setOnClickListener { onPick(pattern) }
            }

            val preview = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(width, height)
                tag = pattern
                contentDescription = pattern.label
            }

            val label = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .also { it.topMargin = (4 * density).toInt() }
                text = pattern.label
                textSize = 11f
                alpha = 0.75f
            }

            column.addView(preview)
            column.addView(label)
            row.addView(column)
            tiles += preview
        }
    }

    /** Farbkreise fuer die Musterfarbe. */
    private fun buildPatternSwatches(
        row: LinearLayout,
        swatchList: MutableList<View>,
        onPick: (Int) -> Unit
    ) {
        row.removeAllViews()
        swatchList.clear()

        val density = resources.displayMetrics.density
        val size = (40 * density).toInt()
        val gap = (10 * density).toInt()

        PatternColors.CHOICES.forEach { (name, color) ->
            val swatch = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also { it.marginEnd = gap }
                contentDescription = name
                tag = color
                setOnClickListener { onPick(color) }
            }
            row.addView(swatch)
            swatchList += swatch
        }
    }

    private fun paintPatternRows() {
        val density = resources.displayMetrics.density
        val ring = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorOnSurface, 0xFF2A1F23.toInt()
        )
        val surface = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorSurfaceVariant, 0xFFF8EFF2.toInt()
        )
        val strokeWidth = (3 * density).toInt()
        val corner = 12 * density

        // In der kleinen Vorschau muss das Muster sichtbar sein, auch wenn es
        // auf der Startseite bewusst sehr zart eingestellt ist.
        fun previewColor(color: Int, strength: Int) =
            PatternColors.withStrength(color, strength.coerceAtLeast(45))

        headerPatternTiles.forEach { tile ->
            val pattern = tile.tag as Pattern
            val base = GradientDrawable(
                GradientDrawable.Orientation.BR_TL,
                intArrayOf(accent, Theming.shade(accent, 0.55f))
            ).apply {
                cornerRadius = corner
                if (pattern == headerPattern) setStroke(strokeWidth, ring)
            }
            tile.background = LayerDrawable(
                arrayOf(
                    base,
                    PatternDrawable(
                        pattern,
                        previewColor(headerPatternColor, headerPatternStrength),
                        density,
                        scale = 0.5f
                    )
                )
            )
        }

        bgPatternTiles.forEach { tile ->
            val pattern = tile.tag as Pattern
            val base = GradientDrawable().apply {
                setColor(surface)
                cornerRadius = corner
                if (pattern == bgPattern) setStroke(strokeWidth, ring)
            }
            tile.background = LayerDrawable(
                arrayOf(
                    base,
                    PatternDrawable(
                        pattern,
                        previewColor(bgPatternColor, bgPatternStrength),
                        density,
                        scale = 0.5f
                    )
                )
            )
        }

        paintPatternSwatches(headerPatternSwatches, headerPatternColor, ring, strokeWidth)
        paintPatternSwatches(bgPatternSwatches, bgPatternColor, ring, strokeWidth)

        binding.tvHeaderPatternValue.text = "$headerPatternStrength %"
        binding.tvBgPatternValue.text = "$bgPatternStrength %"
    }

    private fun paintPatternSwatches(
        swatchList: List<View>,
        selected: Int,
        ring: Int,
        strokeWidth: Int
    ) {
        swatchList.forEach { swatch ->
            val color = swatch.tag as Int
            swatch.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
                if (color == selected) {
                    setStroke(strokeWidth, ring)
                } else {
                    // Weiss waere auf hellem Grund sonst unsichtbar.
                    setStroke((1 * resources.displayMetrics.density).toInt(), 0x33000000)
                }
            }
        }
    }

    private fun updatePreview() {
        val surface = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorSurface, 0xFFFFFFFF.toInt()
        )
        val readable = Theming.readableOn(accent, surface)

        binding.tvPreviewNumber.setTextColor(readable)
        binding.progressPreview.setIndicatorColor(readable)
        binding.progressPreview.trackColor = Theming.resolveColor(
            this, com.google.android.material.R.attr.colorSurfaceVariant, 0xFFF8EFF2.toInt()
        )
        binding.tvOpacityValue.text = "$opacity %"
        Theming.applyCard(binding.cardPreview, cornerDp, opacity)
    }

    // ---------- Update ----------

    private fun checkForUpdate() {
        binding.btnCheckUpdate.isEnabled = false
        binding.btnInstallUpdate.visibility = View.GONE
        showUpdateStatus("Suche nach einer neuen Version \u2026")

        Updater.check(this) { result ->
            binding.btnCheckUpdate.isEnabled = true
            when (result) {
                is Updater.CheckResult.Available -> {
                    pendingPageUrl = result.pageUrl
                    showUpdateStatus("${result.name} ist verfügbar.")
                    binding.btnInstallUpdate.text = "Download öffnen"
                    binding.btnInstallUpdate.visibility = View.VISIBLE
                    showUpdateDialog(result)
                }
                Updater.CheckResult.UpToDate -> {
                    showUpdateStatus("Du hast bereits die neueste Version.")
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Alles aktuell")
                        .setMessage(
                            "Version ${Updater.installedName(this)} " +
                                "(Build ${Updater.installedBuild(this)}) ist die neueste."
                        )
                        .setPositiveButton("Alles klar", null)
                        .show()
                }
                is Updater.CheckResult.NeedsBrowser -> {
                    pendingPageUrl = null
                    showUpdateStatus(result.reason)
                    binding.btnInstallUpdate.text = "Im Browser öffnen"
                    binding.btnInstallUpdate.visibility = View.VISIBLE
                    showUpdateProblem(result.reason)
                }
                is Updater.CheckResult.Failed -> {
                    showUpdateStatus(result.reason)
                    showUpdateProblem(result.reason)
                }
            }
        }
    }

    /** Kleines Fenster mit Versionsnummer und Änderungstext des Releases. */
    private fun showUpdateDialog(result: Updater.CheckResult.Available) {
        val lines = StringBuilder()
        lines.append("Installiert ist ${Updater.installedName(this)}")
        lines.append(" (Build ${Updater.installedBuild(this)}).")
        if (result.sizeBytes > 0) {
            lines.append(" Der Download ist etwa ${result.sizeBytes / 1024 / 1024} MB groß.")
        }

        val notes = result.notes.lineSequence()
            .filter { it.isNotBlank() }
            .take(6)
            .joinToString("\n")
        if (notes.isNotBlank()) {
            lines.append("\n\n")
            lines.append(notes)
        }

        lines.append("\n\nDer Browser öffnet sich und lädt die Datei. ")
        lines.append("Danach in den Downloads antippen, um sie einzuspielen.")

        MaterialAlertDialogBuilder(this)
            .setTitle("${result.name} ist da")
            .setMessage(lines.toString())
            .setPositiveButton("Im Browser laden") { _, _ ->
                Updater.openReleasesPage(this, result.apkUrl)
            }
            .setNegativeButton("Später", null)
            .show()
    }

    private fun showUpdateProblem(reason: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Nachsehen ging nicht")
            .setMessage(reason)
            .setPositiveButton("Im Browser nachsehen") { _, _ ->
                Updater.openReleasesPage(this, pendingPageUrl)
            }
            .setNegativeButton("Schließen", null)
            .show()
    }

    private fun showUpdateStatus(text: String) {
        binding.tvUpdateStatus.text = text
        binding.tvUpdateStatus.visibility = View.VISIBLE
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
