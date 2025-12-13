package edu.mams.app.model.schedule;

import edu.mams.app.model.people.HalfSection;

import java.util.Map;

public class SplitCourse extends Assignment {
    private Map<HalfSection, Course> halfSectionCourses;

    public SplitCourse(Map<HalfSection, Course> halfSectionCourses) {
        super("Split Block");
        this.halfSectionCourses = halfSectionCourses;
    }

    public Map<HalfSection, Course> getHalfSectionCourses() {
        return halfSectionCourses;
    }

    @Override
    public String getName() {
        // Build a readable name from all subgroups
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