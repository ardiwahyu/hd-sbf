package com.bm.hdsbf.utils

import com.kizitonwose.calendar.core.yearMonth
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object CalendarUtil {
    val daysOfWeek = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    fun YearMonth.displayName(): String {
        return this.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ID")))
    }

    fun getMonthNowAndAfter(): List<String> {
        val dateNow = LocalDate.now()
        val listMonth = mutableListOf<String>()
        listMonth.add(dateNow.yearMonth.displayName())
        listMonth.add(dateNow.plusMonths(1).yearMonth.displayName())
        return listMonth
    }
}