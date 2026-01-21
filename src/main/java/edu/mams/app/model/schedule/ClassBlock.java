package edu.mams.app.model.schedule;

import edu.mams.app.model.people.Section;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

public class ClassBlock extends ScheduleEntry {
    private Map<Section, Assignment> sectionCourses; // Each section’s assigned course
    private boolean split;

    public ClassBlock(LocalTime start, Map<Section, Assignment> sectionCourses) {
        super(start);
        this.sectionCourses = sectionCourses;
    }

    public ClassBlock(LocalTime start) {
        super(start);
    }

    public ClassBlock(LocalTime start, Duration length, Map<Section, Assignment> sectionCourses) {
        super(start, length);
        this.sectionCourses = sectionCourses;
    }

    public ClassBlock() {
    }

    public Map<Section, Assignment> getSectionCourses() {
        return sectionCourses;
    }

    public void setSectionCourses(Map<Section, Assignment> sectionCourses) {
        this.sectionCourses = sectionCourses;
    }

    public void setSectionCourse(Section section, Assignment assignment) {
        sectionCourses.put(section, assignment);
    }

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public void setSplit() {
        if (sectionCourses == null) return;
        for (Section section : sectionCourses.keySet()) {
            if (sectionCourses.get(section) instanceof SplitCourse) {
                split = true;
                return;
            }
        }
        split = false;
    }

    @Override
    public String toString() {
        return "ClassBlock{" +
                "sectionCourses=" + sectionCourses +
                ", start=" + start +
                ", length=" + length +
                '}';
    }
}