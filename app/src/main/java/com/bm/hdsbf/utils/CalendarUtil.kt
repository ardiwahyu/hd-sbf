package com.bm.hdsbf.utils

import com.kizitonwose.calendar.core.yearMonth
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
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
}