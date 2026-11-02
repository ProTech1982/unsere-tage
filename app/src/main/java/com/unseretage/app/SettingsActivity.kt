package com.unseretage.app

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        binding.swPreWarn.setOnCheckedChangeListener { _, checked ->
            binding.groupPreWarn.visibility = if (checked) View.VISIBLE else View.GONE
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

        binding.swMilestones.isChecked = prefs.milestonesEnabled
        binding.swMonthly.isChecked = prefs.monthlyEnabled

        updateDateButtons()
        updateTimeButton()
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

    private fun pickDate(title: String, current: LocalDate?, onPicked: (LocalDate) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(
                (current ?: LocalDate.now()).toEpochDay() * MILLIS_PER_DAY
            )
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
        prefs.preWarnDays = if (binding.swPreWarn.isChecked) {
            when (binding.groupPreWarn.checkedButtonId) {
                R.id.btnWarn3 -> 3
                R.id.btnWarn14 -> 14
                else -> 7
            }
        } else {
            0
        }
        prefs.milestonesEnabled = binding.swMilestones.isChecked
        prefs.monthlyEnabled = binding.swMonthly.isChecked

        AlarmScheduler.scheduleNext(this)
        Toast.makeText(this, "Gespeichert", Toast.LENGTH_SHORT).show()
        finish()
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
