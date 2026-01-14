package edu.mams.app.model.schedule;

import edu.mams.app.model.people.HalfSection;

import java.util.Map;

/**
 * Represents a specialized type of assignment where a course is divided into multiple half-sections, usually two,
 * each associated with a specific course. This class organizes and manages the relationship between
 * half-sections and their corresponding courses.
 */
public class SplitCourse extends Assignment {
    private Map<HalfSection, Course> halfSectionCourses;

    public SplitCourse(Map<HalfSection, Course> halfSectionCourses) {
        super("Split Block");
        this.halfSectionCourses = halfSectionCourses;
    }

    public SplitCourse() {
    }

    public Map<HalfSection, Course> getHalfSectionCourses() {
        return halfSectionCourses;
    }

    /**
     * Retrieves the name of the split block that combines the names of all associated half-sections
     * and their corresponding courses in the format: "Split Block: [HalfSectionName - CourseName, ...]".
     *
     * @return A string representing the names of half-sections and their associated courses in the split block.
     */
    @Override
    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append("Split Block: ");
        boolean first = true;
        for (Map.Entry<HalfSection, Course> entry : halfSectionCourses.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey().getName())
                    .append(" - ")
                    .append(entry.getValue().getName());
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getName();
    }
}