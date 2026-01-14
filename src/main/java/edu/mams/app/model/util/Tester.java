package edu.mams.app.model.util;

import edu.mams.app.model.people.Section;
import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.requests.RequestLoader;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.schedule.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Tester {
    public static void main(String[] args) {
        testTemplate();
    }

    public static <T> List<T> removeIndex(List<T> list, int index) {
        List<T> newList = new ArrayList<>(list);
        newList.remove(index);
        return newList;
    }

    public static Week testTemplate() {
        List<Assignment> classes = new ArrayList<>();
        classes.add(new Course("Math", new Teacher("Durost")));
        classes.add(new Course("Physics", new Teacher("Chase")));
        classes.add(new Course("CS", new Teacher("Taricco")));
        classes.add(new Course("STEM", new Teacher("Crowthers")));
        classes.add(new Course("Hum", new Teacher("Small")));
        classes.add(new Course("Lang", new Teacher("Wildfong")));

        List<Section> sections = new ArrayList<>(
                List.of(
                        new Section("R"),
                        new Section("G"),
                        new Section("B")
                )
        );

        List<Section> XYZ = new ArrayList<>(
                List.of(
                        new Section("X"),
                        new Section("Y"),
                        new Section("Z")
                )
        );

        ScheduleBuilder.setSplitClass((Course) classes.get(5));
        ScheduleBuilder.setSplitSection(sections.get(1));

        // Homeroom Day
        TemplateManager.addTemplate(new DayTemplate(
                "Class Meeting Day",
                List.of(
                        new BlockDefinition("ClassBlock", LocalTime.of(7,45), Duration.ofMinutes(60), "Block 1"),
                        new BlockDefinition("ClassBlock", LocalTime.of(8,45), Duration.ofMinutes(60), "Block 2"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(9,45), Duration.ofMinutes(30), "Class Meeting"),
                        new BlockDefinition("ClassBlock", LocalTime.of(10,15), Duration.ofMinutes(60), "Block 3"),
                        new BlockDefinition("ClassBlock", LocalTime.of(11,15), Duration.ofMinutes(60), "Block 4"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(12,15), Duration.ofMinutes(30), "Lunch"),
                        new BlockDefinition("ClassBlock", LocalTime.of(12,45), Duration.ofMinutes(60), "Block 5"),
                        new BlockDefinition("ClassBlock", LocalTime.of(13,45), Duration.ofMinutes(60), "Block 6")
                )
        ));

        // Homeroom Day
        TemplateManager.addTemplate(new DayTemplate(
                "Homeroom Day",
                List.of(
                        new BlockDefinition("ClassBlock", LocalTime.of(7,45), Duration.ofMinutes(60), "Block 1"),
                        new BlockDefinition("ClassBlock", LocalTime.of(8,45), Duration.ofMinutes(60), "Block 2"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(9,45), Duration.ofMinutes(30), "Homeroom"),
                        new BlockDefinition("ClassBlock", LocalTime.of(10,15), Duration.ofMinutes(60), "Block 3"),
                        new BlockDefinition("ClassBlock", LocalTime.of(11,15), Duration.ofMinutes(60), "Block 4"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(12,15), Duration.ofMinutes(30), "Lunch"),
                        new BlockDefinition("ClassBlock", LocalTime.of(12,45), Duration.ofMinutes(60), "Block 5"),
                        new BlockDefinition("ClassBlock", LocalTime.of(13,45), Duration.ofMinutes(60), "Block 6")
                        )
        ));

        // Flex Day
        TemplateManager.addTemplate(new DayTemplate(
                "Flex Day",
                List.of(
                        new BlockDefinition("ClassBlock", LocalTime.of(7,45), Duration.ofMinutes(60), "Block 1"),
                        new BlockDefinition("ClassBlock", LocalTime.of(8,45), Duration.ofMinutes(60), "Block 2"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(9,45), Duration.ofMinutes(15), "15 min break"),
                        new BlockDefinition("ClassBlock", LocalTime.of(10,0), Duration.ofMinutes(60), "Block 3"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(11,0), Duration.ofMinutes(75), "Flex"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(12,15), Duration.ofMinutes(30), "Lunch"),
                        new BlockDefinition("ClassBlock", LocalTime.of(12,45), Duration.ofMinutes(60), "Block 4"),
                        new BlockDefinition("ClassBlock", LocalTime.of(13,45), Duration.ofMinutes(60), "Block 5")
                )
        ));

        TemplateManager.addTemplate(new DayTemplate(
                "PE Day",
                List.of(
                        new BlockDefinition("ClassBlock", LocalTime.of(7,45), Duration.ofMinutes(60), "Block 1"),
                        new BlockDefinition("ClassBlock", LocalTime.of(8,45), Duration.ofMinutes(60), "Block 2"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(9,45), Duration.ofMinutes(15), "15 min break"),
                        new BlockDefinition("ClassBlock", LocalTime.of(10,0), Duration.ofMinutes(60), "Block 3"),
                        new BlockDefinition("ClassBlock", LocalTime.of(11,0), Duration.ofMinutes(60), "Block 4"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(12,0), Duration.ofMinutes(30), "Lunch"),
                        new BlockDefinition("ClassBlock", LocalTime.of(12,30), Duration.ofMinutes(60), "Block 5"),
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(13,30), Duration.ofMinutes(75), "PE")
                )
        ));


        Day monday = new Day(LocalDate.of(2025, 12, 1), 71, sections, classes);
        Day tuesday = new Day(LocalDate.of(2025, 12, 2), 72, sections, classes);
        Day wednesday = new Day(LocalDate.of(2025, 12, 3), 73, sections, removeIndex(classes, 0));
        Day thursday = new Day(LocalDate.of(2025, 12, 4), 74, XYZ, removeIndex(classes, 5));
        Day friday = new Day(LocalDate.of(2025, 12, 5), 75, sections, classes);

        tuesday.setSplit(true, (Course) classes.get(4));


        Week week = new Week(new ArrayList<>(List.of(monday, tuesday, wednesday, thursday, friday)));
        week.loadRequests();
        week.generateBlocks();
        return week;
    }
}
