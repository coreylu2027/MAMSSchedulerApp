package edu.mams.app.model.util;

import edu.mams.app.model.people.HalfSection;
import edu.mams.app.model.people.Section;
import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.requests.AllSchoolRequest;
import edu.mams.app.model.requests.AvoidTimeRequest;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.schedule.*;

import java.util.*;

public class ScheduleBuilder {
    private static TemplateManager templateManager;
    private static Course splitClass = null;
    private static Section splitSection = null;

    public static void setTemplateManager(TemplateManager templateManager) {
        ScheduleBuilder.templateManager = templateManager;
    }

    public static void setSplitClass(Course splitClass) {
        ScheduleBuilder.splitClass = splitClass;
    }

    public static void setSplitSection(Section splitSection) {
        ScheduleBuilder.splitSection = splitSection;
    }

    public static List<ScheduleEntry> buildNewNoSplitSchedule(String templateName, Day day) {
        List<TeacherRequest> requests = day.getRequests();
        List<Assignment> classes = day.getClasses();
        List<Section> sections = day.getSections();
        List<ScheduleEntry> entries = new ArrayList<>();

        DayTemplate template = templateManager.getTemplate(templateName);

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

        int numClassBlocks = 0;
        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock) {
                numClassBlocks++;
            }
        }

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

        int[][] grid = LatinFill.generate(classes.size(), sections.size(), forbidden);
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
        return entries;
    }

    public static List<ScheduleEntry> buildNewSchedule(String templateName, Day day) {
        return buildNewNoSplitSchedule(templateName, day);
    }

}