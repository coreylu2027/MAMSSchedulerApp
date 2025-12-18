package edu.mams.app.model.util;

import edu.mams.app.model.people.HalfSection;
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
import java.util.Map;

public class Tester {
    public static void main(String[] args) {
//        testDataStructure();
        testTemplate();
    }

    public static <T> List<T> removeIndex(List<T> list, int index) {
        List<T> newList = new ArrayList<>(list);
        newList.remove(index);
        return newList;
    }

    public static Week testTemplate() {
        TemplateManager manager = new TemplateManager();

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

        ScheduleBuilder.setTemplateManager(manager);
        ScheduleBuilder.setSplitClass((Course) classes.get(5));
        ScheduleBuilder.setSplitSection(sections.get(1));

        // Homeroom Day
        manager.addTemplate(new DayTemplate(
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
        manager.addTemplate(new DayTemplate(
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
        manager.addTemplate(new DayTemplate(
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

        manager.addTemplate(new DayTemplate(
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

        List<TeacherRequest> requests = RequestLoader.loadRequests(LocalDate.of(2025, 10, 5));
        for (int i = 0; i < requests.size(); i++) {
            requests.get(i).setAssignmentFromList(classes);
        }
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
    private static void testDataStructure() {
        List<Course> classes = new ArrayList<>();
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

        Day monday = new Day(LocalDate.of(2025, 10, 6));

        List<ScheduleEntry> blocks = new ArrayList<>();

        blocks.add(new AllSchoolBlock(
                LocalTime.of(7,45),
                Duration.ofMinutes(105),
                classes.get(3)
        ));

        blocks.add(new AllSchoolBlock(
                LocalTime.of(9,30),
                Duration.ofMinutes(45),
                new Event("HR Advisory")
        ));

        HalfSection G1 = new HalfSection("G1");
        HalfSection G2 = new HalfSection("G2");
        sections.get(1).addHalfSection(G1);
        sections.get(1).addHalfSection(G2);

        blocks.add(new ClassBlock(
                LocalTime.of(10,15),
                Duration.ofMinutes(60),
                Map.of(
                        sections.get(0), classes.get(0),
                        sections.get(1), new SplitCourse(Map.of(G1, classes.get(5), G2, classes.get(1))),
                        sections.get(2), classes.get(2)
                )
        ));

        blocks.add(new ClassBlock(
                LocalTime.of(11,15),
                Duration.ofMinutes(60),
                Map.of(
                        sections.get(0), classes.get(2),
                        sections.get(1), new SplitCourse(Map.of(G1, classes.get(1), G2, classes.get(5))),
                        sections.get(2), classes.get(0)
                )
        ));

        blocks.add(
                new AllSchoolBlock(LocalTime.of(12, 15),
                        Duration.ofMinutes(30),
                        new Event("Lunch")))
        ;

        blocks.add(new ClassBlock(
                LocalTime.of(12, 45),
                Duration.ofMinutes(60),
                Map.of(
                        sections.get(0), classes.get(1),
                        sections.get(1), classes.get(0),
                        sections.get(2), classes.get(5)
                ))
        );

        blocks.add(new ClassBlock(
                LocalTime.of(1, 45),
                Duration.ofMinutes(60),
                Map.of(
                        sections.get(0), classes.get(5),
                        sections.get(1), classes.get(2),
                        sections.get(2), classes.get(1)
                ))
        );

        monday.getEntries().addAll(blocks);

        System.out.println(monday);

        System.out.println(monday.getSectionSchedule(sections.get(0)));
        System.out.println(monday.getSectionSchedule(sections.get(1)));
        System.out.println(monday.getSectionSchedule(sections.get(2)));
        System.out.println(monday.getSectionSchedule(G1));
    }
}
