package edu.mams.app.model.schedule;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class TemplateManager {
    private static Map<String, DayTemplate> templates = new HashMap<>();

    static {
        loadDefaultTemplates();
    }

    public static void addTemplate(DayTemplate template) {
        templates.put(template.getName(), template);
    }

    public static DayTemplate getTemplate(String name) {
        return templates.get(name);
    }

    public static void removeTemplate(String name) {
        templates.remove(name);
    }

    public static List<String> listTemplateNames() {
        return new ArrayList<>(templates.keySet());
    }

    private static void loadDefaultTemplates() {
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
                        new BlockDefinition("PEBlock", LocalTime.of(13,30), Duration.ofMinutes(75), "PE")
                )
        ));

        TemplateManager.addTemplate(new DayTemplate(
                "No School",
                List.of(
                        new BlockDefinition("AllSchoolEvent", LocalTime.of(7, 45), Duration.ofHours(7), "No School")
                )
        ));
    }
}
