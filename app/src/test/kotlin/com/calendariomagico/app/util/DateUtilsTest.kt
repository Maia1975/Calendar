package com.calendariomagico.app.util

import com.google.common.truth.Truth.assertThat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `monthGrid always has 42 days`() {
        val grid = DateUtils.monthGrid(YearMonth.of(2026, 2))
        assertThat(grid).hasSize(42)
    }

    @Test
    fun `monthGrid starts on a Monday`() {
        val grid = DateUtils.monthGrid(YearMonth.of(2026, 8))
        assertThat(grid.first().dayOfWeek).isEqualTo(DayOfWeek.MONDAY)
    }

    @Test
    fun `monthGrid contains every day of the requested month`() {
        val month = YearMonth.of(2026, 8)
        val grid = DateUtils.monthGrid(month)
        val daysInMonth = grid.filter { it.month == month.month && it.year == month.year }
        assertThat(daysInMonth).hasSize(month.lengthOfMonth())
    }

    @Test
    fun `weekDayLabels has seven entries starting with segunda`() {
        val labels = DateUtils.weekDayLabels()
        assertThat(labels).hasSize(7)
        assertThat(labels.first().lowercase()).startsWith("seg")
    }

    @Test
    fun `combine and millisToLocalDate round trip`() {
        val date = LocalDate.of(2026, 8, 12)
        val time = LocalTime.of(14, 30)
        val millis = DateUtils.combine(date, time)
        assertThat(DateUtils.millisToLocalDate(millis)).isEqualTo(date)
        assertThat(DateUtils.millisToLocalDateTime(millis).toLocalTime()).isEqualTo(time)
    }

    @Test
    fun `isSameDay is true for different times on the same date`() {
        val morning = DateUtils.combine(LocalDate.of(2026, 8, 12), LocalTime.of(8, 0))
        val night = DateUtils.combine(LocalDate.of(2026, 8, 12), LocalTime.of(23, 0))
        assertThat(DateUtils.isSameDay(morning, night)).isTrue()
    }

    @Test
    fun `isSameDay is false across midnight`() {
        val day1 = DateUtils.combine(LocalDate.of(2026, 8, 12), LocalTime.of(23, 59))
        val day2 = DateUtils.combine(LocalDate.of(2026, 8, 13), LocalTime.of(0, 1))
        assertThat(DateUtils.isSameDay(day1, day2)).isFalse()
    }
}
