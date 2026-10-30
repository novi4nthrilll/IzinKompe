package com.izinkompe.ui.schedule

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.izinkompe.IzinKompeApp
import com.izinkompe.data.model.Schedule
import com.izinkompe.databinding.ActivityScheduleBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Activity for configuring the study schedule.
 * Allows setting start/end times, active days, and enabling/disabling the schedule.
 */
class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private val app by lazy { application as IzinKompeApp }

    private var startHour = 8
    private var startMinute = 0
    private var endHour = 12
    private var endMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTimePickers()
        setupSaveButton()
        loadExistingSchedule()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupTimePickers() {
        binding.btnStartTime.setOnClickListener {
            showTimePicker("Jam Mulai", startHour, startMinute) { hour, minute ->
                startHour = hour
                startMinute = minute
                binding.btnStartTime.text = String.format("%02d:%02d", hour, minute)
            }
        }

        binding.btnEndTime.setOnClickListener {
            showTimePicker("Jam Selesai", endHour, endMinute) { hour, minute ->
                endHour = hour
                endMinute = minute
                binding.btnEndTime.text = String.format("%02d:%02d", hour, minute)
            }
        }
    }

    private fun showTimePicker(
        title: String,
        initialHour: Int,
        initialMinute: Int,
        onTimeSelected: (Int, Int) -> Unit
    ) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialHour)
            .setMinute(initialMinute)
            .setTitleText(title)
            .build()

        picker.addOnPositiveButtonClickListener {
            onTimeSelected(picker.hour, picker.minute)
        }

        picker.show(supportFragmentManager, "time_picker")
    }

    private fun setupSaveButton() {
        binding.btnSaveSchedule.setOnClickListener {
            val schedule = Schedule(
                id = 1, // Always use ID 1 for single schedule
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                monday = binding.chipMonday.isChecked,
                tuesday = binding.chipTuesday.isChecked,
                wednesday = binding.chipWednesday.isChecked,
                thursday = binding.chipThursday.isChecked,
                friday = binding.chipFriday.isChecked,
                saturday = binding.chipSaturday.isChecked,
                sunday = binding.chipSunday.isChecked,
                isEnabled = binding.switchSchedule.isChecked
            )

            lifecycleScope.launch {
                app.database.scheduleDao().insert(schedule)
                Toast.makeText(this@ScheduleActivity, "Jadwal tersimpan ✓", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadExistingSchedule() {
        lifecycleScope.launch {
            val schedule = app.database.scheduleDao().getSchedule().first()
            if (schedule != null) {
                startHour = schedule.startHour
                startMinute = schedule.startMinute
                endHour = schedule.endHour
                endMinute = schedule.endMinute

                binding.btnStartTime.text = String.format("%02d:%02d", startHour, startMinute)
                binding.btnEndTime.text = String.format("%02d:%02d", endHour, endMinute)

                binding.chipMonday.isChecked = schedule.monday
                binding.chipTuesday.isChecked = schedule.tuesday
                binding.chipWednesday.isChecked = schedule.wednesday
                binding.chipThursday.isChecked = schedule.thursday
                binding.chipFriday.isChecked = schedule.friday
                binding.chipSaturday.isChecked = schedule.saturday
                binding.chipSunday.isChecked = schedule.sunday

                binding.switchSchedule.isChecked = schedule.isEnabled
            }
        }
    }
}
