package edu.mams.app.model.util;

import edu.mams.app.model.people.Section;
import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.requests.AllSchoolRequest;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.schedule.Assignment;
import edu.mams.app.model.schedule.Day;
import edu.mams.app.model.schedule.ScheduleEntry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleBuilderTest {

    @Test
    void getScheduleEntriesDoesNotMutateDayClassesWhenApplyingAllSchoolRequest() {
        Day day = buildDayWithAllSchoolRequest();
        List<String> originalNames = day.getClasses().stream().map(Assignment::getName).toList();

        List<ScheduleEntry> entries = ScheduleBuilder.getScheduleEntries("Homeroom Day", day);

        assertEquals(8, entries.size());
        assertEquals(originalNames, day.getClasses().stream().map(Assignment::getName).toList());
    }

    @Test
    void buildNewNoSplitScheduleDoesNotMutateDayClassesWhenApplyingAllSchoolRequest() {
        Day day = buildDayWithAllSchoolRequest();
        List<String> originalNames = day.getClasses().stream().map(Assignment::getName).toList();

        ScheduleBuilder.buildNewNoSplitSchedule("Homeroom Day", day);

        assertEquals(originalNames, day.getClasses().stream().map(Assignment::getName).toList());
    }

    private static Day buildDayWithAllSchoolRequest() {
        List<Assignment> classes = new ArrayList<>(List.of(
                new edu.mams.app.model.schedule.Course("Math", new Teacher("Durost")),
                new edu.mams.app.model.schedule.Course("Physics", new Teacher("Chase")),
                new edu.mams.app.model.schedule.Course("CS", new Teacher("Taricco")),
                new edu.mams.app.model.schedule.Course("STEM", new Teacher("Crowthers")),
                new edu.mams.app.model.schedule.Course("Hum", new Teacher("Small")),
                new edu.mams.app.model.schedule.Course("Lang", new Teacher("Wildfong"))
        ));

        Day day = new Day(
                LocalDate.of(2026, 2, 3),
                12,
                new ArrayList<>(List.of(new Section("R"), new Section("G"), new Section("B"))),
                new ArrayList<>(classes)
        );

        TeacherRequest request = new AllSchoolRequest(
                new Teacher("Small"),
                classes.get(4),
                "Assembly",
                Duration.ofMinutes(60),
                LocalTime.of(8, 45)
        );
        day.setRequests(new ArrayList<>(List.of(request)));
        return day;
    }
}
