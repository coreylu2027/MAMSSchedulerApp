package edu.mams.app.model.util;

import edu.mams.app.model.people.HalfSection;
import edu.mams.app.model.people.Section;
import edu.mams.app.model.requests.AllSchoolRequest;
import edu.mams.app.model.requests.AvoidTimeRequest;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.schedule.*;

import java.util.*;

public class ScheduleBuilder {
    private static Course splitClass = null;
    private static Section splitSection = null;

    public static void setSplitClass(Course splitClass) {
        ScheduleBuilder.splitClass = splitClass;
    }

    public static void setSplitSection(Section splitSection) {
        ScheduleBuilder.splitSection = splitSection;
    }

    public static Section getSplitSection() {
        return splitSection;
    }

    public static Course getSplitClass() {
        return splitClass;
    }

    public static List<ScheduleEntry> buildNewSplitSchedule(String templateName, Day day) {
        List<TeacherRequest> requests = day.getRequests();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<ScheduleEntry> entries = getScheduleEntries(templateName, requests, classes);
        Course partnerSplit = day.getSplitCourse();

        boolean[][] forbidden = getForbidden(entries, classes, requests);

        int splitIndex = classes.indexOf(partnerSplit);
        int langIndex = classes.indexOf(splitClass);


        int[][] partial = getEmptyPartial(entries);
        partial[partial.length - 1][1] = langIndex;
        partial[partial.length - 2][1] = splitIndex;
        partial[partial.length - 1][3] = splitIndex;
        partial[partial.length - 2][3] = langIndex;

//        int freeBlocks = partial.length - 4;
//        for (int i = 0; i < freeBlocks; i++) {
//            partial[i][4+i] = langIndex;
//        }

        int[][] grid = LatinFill.generateFromPartial(partial, forbidden);
        fillEntries(entries, sections, classes, grid);

        int block = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock classBlock) {
                Map<Section, Assignment> sectionCourses = classBlock.getSectionCourses();
                for (int i = 0; i < sections.size(); i++) {
                    if (block == partial.length - 1 && i == 1) {
                        SplitCourse splitCourse = new SplitCourse(Map.of(
                                new HalfSection("Intermediate", sections.get(i)),
                                (Course) classes.get(splitIndex),
                                new HalfSection("Advanced", sections.get(i)),
                                (Course) classes.get(langIndex))
                        );
                        sectionCourses.put(sections.get(i), splitCourse);
                    } else if (block == partial.length - 2 && i == 1) {
                        SplitCourse splitCourse = new SplitCourse(Map.of(
                                new HalfSection("Intermediate", sections.get(i)),
                                (Course) classes.get(langIndex),
                                new HalfSection("Advanced", sections.get(i)),
                                (Course) classes.get(splitIndex))
                        );
                        sectionCourses.put(sections.get(i), splitCourse);
                    }
                }
                classBlock.setSectionCourses(sectionCourses);
                block++;
            }
        }
        return entries;
    }

    public static List<ScheduleEntry> buildNewNoSplitSchedule(String templateName, Day day) {
        List<TeacherRequest> requests = day.getRequests();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<ScheduleEntry> entries = getScheduleEntries(templateName, requests, classes);

        boolean[][] forbidden = getForbidden(entries, classes, requests);

        int[][] grid = LatinFill.generate(classes.size(), sections.size(), forbidden);

        fillEntries(entries, sections, classes, grid);
        return entries;
    }

    private static List<ScheduleEntry> getScheduleEntries(String templateName, List<TeacherRequest> requests, List<Assignment> classes) {
        List<ScheduleEntry> entries = new ArrayList<>();

        DayTemplate template = TemplateManager.getTemplate(templateName);

        for (BlockDefinition def : template.getBlocks()) {
            switch (def.getType()) {
                case "ClassBlock" -> entries.add(new ClassBlock(def.getStart(), def.getLength(), new HashMap<>()));
                case "AllSchoolEvent" ->
                        entries.add(new AllSchoolBlock(def.getStart(), def.getLength(), new Event(def.getLabel())));
            }
        }

        for (TeacherRequest request : requests) {
            if (request instanceof AllSchoolRequest allSchoolRequest) {
                for (int i = 0; i < entries.size(); i++) {
                    ScheduleEntry entry = entries.get(i);
                    if (entry.getStart().equals(allSchoolRequest.getStartTime())) {
                        entries.set(i, new AllSchoolBlock(allSchoolRequest));
                        classes.remove(allSchoolRequest.getAssignment());
                    }
                }
            }
        }
        return entries;
    }

    private static boolean[][] getForbidden(List<ScheduleEntry> entries, List<Assignment> classes, List<TeacherRequest> requests) {
        int numClassBlocks = getNumClassBlocks(entries);

        boolean[][] forbidden = new boolean[numClassBlocks][numClassBlocks];

        if (classes.size() != numClassBlocks) {
            throw new IllegalStateException("classes does not match available class blocks");
        }

        for (TeacherRequest request : requests) {
            if (request instanceof AvoidTimeRequest avoidTimeRequest) {
                int a = classes.indexOf(avoidTimeRequest.getAssignment());
                int block = 0;
                for (ScheduleEntry entry : entries) {
                    if (entry instanceof ClassBlock classBlock) {
                        if (classBlock.intersects(avoidTimeRequest)) {
                            forbidden[block][a] = true;
                        }
                        block++;
                    }
                }
            }
        }
        return forbidden;
    }

    private static int getNumClassBlocks(List<ScheduleEntry> entries) {
        int numClassBlocks = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock) {
                numClassBlocks++;
            }
        }
        return numClassBlocks;
    }

    public static List<ScheduleEntry> buildAroundSchedule(Day day) {
        List<ScheduleEntry> entries = day.getEntries();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<TeacherRequest> requests = day.getRequests();

        boolean[][] forbidden = getForbidden(entries, classes, requests);

        int[][] partial = getEmptyPartial(entries);

        int block = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock classBlock) {
                int sectionCounter = 0;
                for (Section section : sections) {
                    Assignment assignment = classBlock.getSectionCourses().get(section);
                    if (assignment != null) {
                        if (assignment instanceof Course) {
                            int classIndex = classes.indexOf(assignment);
                            partial[block][sectionCounter] = classIndex;
                        }
                    }
                    sectionCounter++;
                }
                block++;
            }
        }

        int[][] grid = LatinFill.generateFromPartial(partial, forbidden);
        fillEntries(entries, sections, classes, grid);
        return entries;
    }

    private static void fillEntries(List<ScheduleEntry> entries, List<Section> sections, List<Assignment> classes, int[][] grid) {
        int block = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock classBlock) {
                Map<Section, Assignment> sectionCourses = new HashMap<>();
                for (int i = 0; i < sections.size(); i++) {
                    sectionCourses.put(sections.get(i), classes.get(grid[block][i]));
                }
                classBlock.setSectionCourses(sectionCourses);
                block++;
            }
        }
    }

    private static int[][] getEmptyPartial(List<ScheduleEntry> entries) {
        int numClassBlocks = getNumClassBlocks(entries);
        int[][] partial = new int[numClassBlocks][numClassBlocks];
        for (int i = 0; i < numClassBlocks; i++) {
            for (int j = 0; j < numClassBlocks; j++) {
                partial[i][j] = -1;
            }
        }
        return partial;
    }
}