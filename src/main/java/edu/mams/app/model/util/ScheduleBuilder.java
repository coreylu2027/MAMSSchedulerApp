package edu.mams.app.model.util;

import edu.mams.app.model.people.HalfSection;
import edu.mams.app.model.people.Section;
import edu.mams.app.model.requests.AllSchoolRequest;
import edu.mams.app.model.requests.AvoidTimeRequest;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.schedule.*;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ScheduleBuilder {
    private static Course splitClass = null;
    private static Section splitSection = null;
    private static List<HalfSection> halfSections = new ArrayList<>(List.of(new HalfSection("Intermediate", new Section("G")), new HalfSection("Advanced", new Section("G"))));
    private static String peGroupAName = "Group A";
    private static String peGroupBName = "Group B";
    private static String peActivityOne = "Gym";
    private static String peActivityTwo = "Zumba";

    public static List<HalfSection> getHalfSections() {
        return halfSections;
    }

    public static void setHalfSections(List<HalfSection> halfSections) {
        ScheduleBuilder.halfSections = halfSections;
    }

    public static Section getSplitSection() {
        return splitSection;
    }

    public static void setSplitSection(Section splitSection) {
        ScheduleBuilder.splitSection = splitSection;
    }

    public static Course getSplitClass() {
        return splitClass;
    }

    public static void setSplitClass(Course splitClass) {
        ScheduleBuilder.splitClass = splitClass;
    }

    public static String getPeGroupAName() {
        return peGroupAName;
    }

    public static String getPeGroupBName() {
        return peGroupBName;
    }

    public static String getPeActivityOne() {
        return peActivityOne;
    }

    public static String getPeActivityTwo() {
        return peActivityTwo;
    }

    public static void setPeDefaults(String groupAName, String groupBName, String activityOne, String activityTwo) {
        if (groupAName != null && !groupAName.isBlank()) {
            peGroupAName = groupAName.trim();
        }
        if (groupBName != null && !groupBName.isBlank()) {
            peGroupBName = groupBName.trim();
        }
        if (activityOne != null && !activityOne.isBlank()) {
            peActivityOne = activityOne.trim();
        }
        if (activityTwo != null && !activityTwo.isBlank()) {
            peActivityTwo = activityTwo.trim();
        }
    }

    public static List<ScheduleEntry> buildNewSplitSchedule(String templateName, Day day) {
        List<TeacherRequest> requests = day.getRequests();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<ScheduleEntry> entries = getScheduleEntries(templateName, day);
        Assignment partnerAssignment = day.getSplitCourse();
        if (!(partnerAssignment instanceof Course partnerSplit)) {
            throw new IllegalStateException("Split mode requires a valid partner split course.");
        }

        boolean[][] forbidden = getForbidden(entries, classes, requests);

        int splitIndex = classes.indexOf(partnerSplit);
        int langIndex = classes.indexOf(splitClass);
        if (splitIndex < 0) {
            throw new IllegalStateException("Partner split course must be included in the selected classes for the day.");
        }
        if (langIndex < 0) {
            throw new IllegalStateException("Primary split course must be included in the selected classes for the day.");
        }


        int[][] partial = getEmptyPartial(entries);
        int splitConfig = pickSplitConfig(partial.length);
        int splitBlock1, splitBlock2;
        switch (splitConfig) {
            case (0) -> {
                splitBlock1 = partial.length - 1;
                splitBlock2 = partial.length - 2;
            }
            case (1) -> {
                splitBlock1 = partial.length - 2;
                splitBlock2 = partial.length - 1;
            }
            case (2) -> {
                splitBlock1 = partial.length - 2;
                splitBlock2 = partial.length - 3;
            }
            case (3) -> {
                splitBlock1 = partial.length - 3;
                splitBlock2 = partial.length - 2;
            }
            case (4) -> {
                splitBlock1 = partial.length - 3;
                splitBlock2 = partial.length - 4;
            }
            case (5) -> {
                splitBlock1 = partial.length - 4;
                splitBlock2 = partial.length - 3;
            }
            default -> {
                throw new IllegalArgumentException("Unknown split configuration: " + splitConfig);
            }
        }
        setSplitConfig(partial, splitBlock1, langIndex, splitBlock2, splitIndex);


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
                    if (block == splitBlock1 && i == 1) {
                        SplitCourse splitCourse = new SplitCourse(Map.of(new HalfSection("Intermediate", sections.get(i)), classes.get(splitIndex), new HalfSection("Advanced", sections.get(i)), classes.get(langIndex)));
                        sectionCourses.put(sections.get(i), splitCourse);
                    } else if (block == splitBlock2 && i == 1) {
                        SplitCourse splitCourse = new SplitCourse(Map.of(halfSections.get(0), classes.get(langIndex), halfSections.get(1), classes.get(splitIndex)));
                        sectionCourses.put(sections.get(i), splitCourse);
                    }
                }
                classBlock.setSectionCourses(sectionCourses);
                block++;
            }
        }
        return entries;
    }

    public static List<ScheduleEntry> getScheduleEntries(String templateName, List<TeacherRequest> requests, List<Assignment> classes) {
        return getScheduleEntries(templateName, requests, classes, null);
    }

    public static List<ScheduleEntry> getScheduleEntries(String templateName, Day day) {
        return getScheduleEntries(templateName, day.getRequests(), day.getClasses(), day.getDate());
    }

    public static List<ScheduleEntry> getScheduleEntries(String templateName, List<TeacherRequest> requests, List<Assignment> classes, LocalDate date) {
        List<ScheduleEntry> entries = new ArrayList<>();

        DayTemplate template = TemplateManager.getTemplate(templateName);
        if (template == null) {
            return entries;
        }

        for (BlockDefinition def : template.getBlocks()) {
            switch (def.getType()) {
                case "ClassBlock" -> entries.add(new ClassBlock(def.getStart(), def.getLength(), new HashMap<>()));
                case "PEBlock" -> entries.add(buildDefaultPEBlock(def, date));
                case "AllSchoolEvent" -> {
                    if (isPEDefinition(def)) {
                        entries.add(buildDefaultPEBlock(def, date));
                    } else {
                        entries.add(new AllSchoolBlock(def.getStart(), def.getLength(), new Event(def.getLabel())));
                    }
                }
            }
        }

        if (requests == null) {
            return entries;
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

        if (requests == null) {
            return forbidden;
        }

        for (TeacherRequest request : requests) {
            if (request instanceof AvoidTimeRequest avoidTimeRequest) {
                int a = classes.indexOf(avoidTimeRequest.getAssignment());
                if (a < 0) {
                    continue;
                }
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

    private static int pickSplitConfig(int numClassBlocks) {
        List<Integer> configs = new ArrayList<>();
        configs.add(0);
        configs.add(1);
        if (numClassBlocks > 2) {
            configs.add(2);
            configs.add(3);
        }
        if (numClassBlocks > 3) {
            configs.add(4);
            configs.add(5);
        }
        return configs.get(ThreadLocalRandom.current().nextInt(configs.size()));
    }

    private static void setSplitConfig(int[][] partial, int splitBlock1, int langIndex, int splitBlock2, int splitIndex) {
        partial[splitBlock1][1] = langIndex;
        partial[splitBlock2][1] = splitIndex;
        partial[splitBlock1][3] = splitIndex;
        partial[splitBlock2][3] = langIndex;
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

    private static int getNumClassBlocks(List<ScheduleEntry> entries) {
        int numClassBlocks = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock) {
                numClassBlocks++;
            }
        }
        return numClassBlocks;
    }

    public static List<ScheduleEntry> buildNewNoSplitSchedule(String templateName, Day day) {
        List<TeacherRequest> requests = day.getRequests();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<ScheduleEntry> entries = getScheduleEntries(templateName, day);

        boolean[][] forbidden = getForbidden(entries, classes, requests);

        int[][] grid = LatinFill.generate(classes.size(), sections.size(), forbidden);

        fillEntries(entries, sections, classes, grid);
        return entries;
    }

    public static List<ScheduleEntry> buildAroundSchedule(Day day) {
        List<ScheduleEntry> entries = day.getEntries();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<TeacherRequest> requests = day.getRequests();

        boolean[][] forbidden = getForbidden(entries, classes, requests);

        int[][] partial = getEmptyPartial(entries);
        List<int[]> splitSlots = new ArrayList<>();
        List<SplitCourse> splitCourses = new ArrayList<>();

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
                        } else if (assignment instanceof SplitCourse splitCourse) {
                            splitSlots.add(new int[]{block, sectionCounter});
                            splitCourses.add(splitCourse);
                            Map<HalfSection, Assignment> halves = splitCourse.getHalfSectionCourses();
                            if (halves != null) {
                                Assignment firstCourse = halves.get(halfSections.get(0));
                                Assignment secondCourse = halves.get(halfSections.get(1));
                                int firstIndex = classes.indexOf(firstCourse);
                                int secondIndex = classes.indexOf(secondCourse);
                                partial[block][sectionCounter] = secondIndex;
                                partial[block][sectionCounter + 2] = firstIndex;
                            }
                        }
                    }
                    sectionCounter++;
                }
                block++;
            }
        }

        int[][] grid = LatinFill.generateFromPartial(partial, forbidden);
        fillEntries(entries, sections, classes, grid);
        for (int i = 0; i < splitSlots.size(); i++) {
            int[] slot = splitSlots.get(i);
            applySplitCourse(entries, sections, slot[0], slot[1], splitCourses.get(i));
        }
        return entries;
    }

    private static void applySplitCourse(List<ScheduleEntry> entries, List<Section> sections, int blockIndex, int sectionIndex, SplitCourse splitCourse) {
        int block = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock classBlock) {
                if (block == blockIndex) {
                    Map<Section, Assignment> sectionCourses = classBlock.getSectionCourses();
                    sectionCourses.put(sections.get(sectionIndex), splitCourse);
                    classBlock.setSectionCourses(sectionCourses);
                    return;
                }
                block++;
            }
        }
    }

    private static PEBlock buildDefaultPEBlock(BlockDefinition def, LocalDate date) {
        boolean swap = isSwapWeek(date);
        String groupAActivity = swap ? peActivityTwo : peActivityOne;
        String groupBActivity = swap ? peActivityOne : peActivityTwo;
        return new PEBlock(
                def.getStart(),
                def.getLength(),
                peGroupAName,
                groupAActivity,
                peGroupBName,
                groupBActivity
        );
    }

    private static boolean isPEDefinition(BlockDefinition def) {
        return def.getLabel() != null && "PE".equalsIgnoreCase(def.getLabel().trim());
    }

    private static boolean isSwapWeek(LocalDate date) {
        if (date == null) {
            return false;
        }
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        return Math.floorMod((year * 53) + week, 2) == 1;
    }
}
