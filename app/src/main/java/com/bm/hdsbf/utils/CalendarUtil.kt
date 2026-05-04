package com.bm.hdsbf.utils

import androidx.appcompat.app.AppCompatActivity
import com.bm.hdsbf.R
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.kizitonwose.calendar.core.yearMonth
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CalendarUtil {
    val daysOfWeek = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    fun YearMonth.displayName(): String {
        return this.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ID")))
    }

    fun String.formatDate(): String {
        val inputFormat = arrayOf("dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy", "dd/MM", "dd-MM")
        val outputFormat = arrayOf("dd MMMM yyyy", "dd MMMM")
        var output = this
        for (f in inputFormat) {
            try {
                val dfInput = SimpleDateFormat(f, Locale("ID"))
                val dateOutput = dfInput.parse(this)!!
                val dfOutput = SimpleDateFormat(if (f.contains("y")) outputFormat[0] else outputFormat[1], Locale("ID"))
                output = dfOutput.format(dateOutput)
                break
            } catch (_: Exception) {
                continue
            }
        }
        return output
    }

    fun getMonthNowAndAfter(): List<String> {
        val dateNow = LocalDate.now()
        val listMonth = mutableListOf<String>()
        listMonth.add(dateNow.yearMonth.displayName())
        listMonth.add(dateNow.plusMonths(1).yearMonth.displayName())
        return listMonth
    }

    fun AppCompatActivity.showDatePicker(onSelect: ((LocalDate) -> Unit)) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(today))
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setTheme(R.style.CustomDatePickerLight)
            .setSelection(today)
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
        datePicker.show(supportFragmentManager, null)
        datePicker.addOnPositiveButtonClickListener {
            val selectedDate = Instant
                .ofEpochMilli(it)
                .atZone(ZoneId.of("Asia/Jakarta"))
                .toLocalDate()
            onSelect.invoke(selectedDate)
        }
    }

    fun LocalDate.formatLocalDate(): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        return format(formatter)
    }
}