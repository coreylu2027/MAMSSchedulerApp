package edu.mams.app.model.schedule;

import java.util.*;

public class TemplateManager {
    private Map<String, DayTemplate> templates = new HashMap<>();

    public void addTemplate(DayTemplate template) {
        templates.put(template.getName(), template);
    }

    public DayTemplate getTemplate(String name) {
        return templates.get(name);
    }

    public void removeTemplate(String name) {
        templates.remove(name);
    }

    public List<String> listTemplateNames() {
        return new ArrayList<>(templates.keySet());
    }
}