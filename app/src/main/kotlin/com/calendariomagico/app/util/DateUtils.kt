package com.calendariomagico.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    val PT_LOCALE: Locale = Locale.forLanguageTag("pt-PT")
    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun millisToLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun millisToLocalDateTime(millis: Long): LocalDateTime =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()

    fun localDateTimeToMillis(dateTime: LocalDateTime): Long =
        dateTime.atZone(zone).toInstant().toEpochMilli()

    fun combine(date: LocalDate, time: LocalTime): Long =
        localDateTimeToMillis(LocalDateTime.of(date, time))

    fun isSameDay(millisA: Long, millisB: Long): Boolean =
        millisToLocalDate(millisA) == millisToLocalDate(millisB)

    /**
     * Full 6x7 grid for the given month, including the trailing days of the
     * previous month and the leading days of the next month, starting on Monday.
     */
    fun monthGrid(month: YearMonth): List<LocalDate> {
        val firstOfMonth = month.atDay(1)
        val firstDayOffset = (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
        val gridStart = firstOfMonth.minusDays(firstDayOffset.toLong())
        return (0 until 42).map { gridStart.plusDays(it.toLong()) }
    }

    fun weekDayLabels(): List<String> {
        val monday = DayOfWeek.MONDAY
        return (0 until 7).map { offset ->
            DayOfWeek.of(((monday.value - 1 + offset) % 7) + 1)
                .getDisplayName(TextStyle.SHORT, PT_LOCALE)
                .replaceFirstChar { it.uppercase(PT_LOCALE) }
        }
    }

    fun monthYearLabel(month: YearMonth): String =
        month.atDay(1)
            .format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT_LOCALE))
            .replaceFirstChar { it.uppercase(PT_LOCALE) }

    fun dayHeaderLabel(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_LOCALE))
            .replaceFirstChar { it.uppercase(PT_LOCALE) }

    fun timeLabel(millis: Long): String =
        millisToLocalDateTime(millis).format(DateTimeFormatter.ofPattern("HH:mm"))

    fun shortDateLabel(millis: Long): String =
        millisToLocalDate(millis).format(DateTimeFormatter.ofPattern("d MMM", PT_LOCALE))
}
