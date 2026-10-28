package com.acadiancrypto.loadcounter

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Pure formatting helpers, kept free of Android classes so they can be unit tested. */
object LoadFormat {

    private val csvDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    /** CSV with a header and one row per load, oldest first, numbered from 1. */
    fun toCsv(loads: List<Long>, zone: ZoneId = ZoneId.systemDefault()): String {
        val sb = StringBuilder("Load Number,Date and Time\n")
        loads.forEachIndexed { index, millis ->
            val time = Instant.ofEpochMilli(millis).atZone(zone).format(csvDateFormat)
            sb.append(index + 1).append(',').append(time).append('\n')
        }
        return sb.toString()
    }

    /** Default export file name, e.g. "loads_2026-10-28.csv". */
    fun defaultCsvName(today: LocalDate = LocalDate.now()): String = "loads_$today.csv"

    /** How many loads fall on [today] in the given time zone. */
    fun countOnDate(loads: List<Long>, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Int =
        loads.count { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today }

    /** Locale-friendly date and time for the "Recent loads" list, e.g. "Oct 28, 2026, 2:14 PM". */
    fun displayDateTime(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone)
            .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))
}
