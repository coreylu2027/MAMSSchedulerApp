package edu.mams.app.model.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void copyCreatesDetachedDays() {
        LocalDate start = LocalDate.of(2026, 2, 2);
        Week original = new Week(start, 1);
        Day originalDay = original.getDay(start);
        originalDay.setNotes(new ArrayList<>(List.of("Original note")));
        originalDay.setClasses(new ArrayList<>(List.of(new Course("Math"))));
        originalDay.setEntries(new ArrayList<>(List.of(new AllSchoolBlock(LocalTime.of(7, 45)))));

        Week copy = original.copy();
        Day copyDay = copy.getDay(start);
        copyDay.setNotes(new ArrayList<>(List.of("Changed note")));
        copyDay.getClasses().clear();
        copyDay.getEntries().getFirst().setStart(LocalTime.of(8, 0));

        assertEquals(List.of("Original note"), originalDay.getNotes());
        assertEquals(1, originalDay.getClasses().size());
        assertEquals(LocalTime.of(7, 45), originalDay.getEntries().getFirst().getStart());
    }
}
