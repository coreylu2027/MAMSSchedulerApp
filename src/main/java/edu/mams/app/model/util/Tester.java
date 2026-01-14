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


        Day monday = new Day(LocalDate.of(2025, 12, 1), 71, sections, classes);
        Day tuesday = new Day(LocalDate.of(2025, 12, 2), 72, sections, classes);
        Day wednesday = new Day(LocalDate.of(2025, 12, 3), 73, sections, removeIndex(classes, 0));
        Day thursday = new Day(LocalDate.of(2025, 12, 4), 74, XYZ, removeIndex(classes, 5));
        Day friday = new Day(LocalDate.of(2025, 12, 5), 75, sections, classes);

        tuesday.setSplitCourse((Course) classes.get(4));


        Week week = new Week(new ArrayList<>(List.of(monday, tuesday, wednesday, thursday, friday)));
        week.loadRequests();
        week.generateBlocks();
        return week;
    }
}
