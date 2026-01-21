package edu.mams.app.model.schedule;

import edu.mams.app.model.people.HalfSection;

import java.util.Map;

/**
 * Represents a specialized type of assignment where a course is divided into multiple half-sections, usually two,
 * each associated with a specific course. This class organizes and manages the relationship between
 * half-sections and their corresponding courses.
 */
public class SplitCourse extends Assignment {
    private Map<HalfSection, Assignment> halfSectionCourses;

    public SplitCourse(Map<HalfSection, Assignment> halfSectionCourses) {
        super("Split Block");
        this.halfSectionCourses = halfSectionCourses;
    }

    public SplitCourse() {
    }

    public Map<HalfSection, Assignment> getHalfSectionCourses() {
        return halfSectionCourses;
    }

    public void setHalfSectionCourses(Map<HalfSection, Assignment> halfSectionCourses) {
        this.halfSectionCourses = halfSectionCourses;
    }

    @Override
    public String toString() {
        return getName();
    }
}