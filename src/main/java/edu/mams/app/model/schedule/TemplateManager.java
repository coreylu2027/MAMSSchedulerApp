package edu.mams.app.model.schedule;

import java.util.*;

public class TemplateManager {
    private static Map<String, DayTemplate> templates = new HashMap<>();

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
}