package com.acadiancrypto.loadcounter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LoadFormatTest {
    private val zone = ZoneId.of("America/Halifax")

    private fun millis(y: Int, mo: Int, d: Int, h: Int, mi: Int, s: Int = 0) =
        LocalDateTime.of(y, mo, d, h, mi, s).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun csvHasHeaderOnlyWhenEmpty() {
        assertEquals("Load Number,Date and Time\n", LoadFormat.toCsv(emptyList(), zone))
    }

    @Test
    fun csvRowsAreNumberedAndFormatted() {
        val csv = LoadFormat.toCsv(listOf(millis(2026, 10, 28, 7, 5, 9), millis(2026, 10, 28, 14, 14, 0)), zone)
        assertEquals(
            "Load Number,Date and Time\n" +
                "1,2026-10-28 07:05:09\n" +
                "2,2026-10-28 14:14:00\n",
            csv,
        )
    }

    @Test
    fun defaultCsvNameUsesDate() {
        assertEquals("loads_2026-10-28.csv", LoadFormat.defaultCsvName(LocalDate.of(2026, 10, 28)))
    }

    @Test
    fun countOnDateUsesLocalMidnight() {
        val loads = listOf(
            millis(2026, 10, 27, 23, 59),
            millis(2026, 10, 28, 0, 1),
            millis(2026, 10, 28, 18, 0),
        )
        assertEquals(2, LoadFormat.countOnDate(loads, LocalDate.of(2026, 10, 28), zone))
    }
}
