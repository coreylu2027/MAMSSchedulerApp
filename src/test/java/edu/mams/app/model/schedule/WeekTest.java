package edu.mams.app.model.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeekTest {

    @Test
    void constructorBuildsFiveSequentialDaysWithNumbers() {
        LocalDate start = LocalDate.of(2026, 2, 2);
        Week week = new Week(start, 10);

        assertEquals(5, week.getDays().size());
        assertEquals(start, week.getStartingDate());
        assertEquals(start.plusDays(4), week.getEndingDate());
        assertEquals(10, week.getStartingDayNumber());
        assertEquals(14, week.getEndingDayNumber());
    }

    @Test
    void getDayReturnsDayForDateAndThrowsWhenMissing() {
        LocalDate start = LocalDate.of(2026, 2, 2);
        Week week = new Week(start, 1);

        assertEquals(start.plusDays(3), week.getDay(start.plusDays(3)).getDate());
        assertThrows(IllegalArgumentException.class, () -> week.getDay(start.plusDays(7)));
    }
}
