package edu.mams.app.model.schedule;

import edu.mams.app.model.people.Section;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

/**
 * Schedule entry for section-specific class assignments.
 */
public class ClassBlock extends ScheduleEntry {
    private Map<Section, Assignment> sectionCourses; // Each section’s assigned course
    private boolean split;

    /**
     * Creates a class block with default duration.
     *
     * @param start block start time
     * @param sectionCourses section-to-assignment mapping
     */
    public ClassBlock(LocalTime start, Map<Section, Assignment> sectionCourses) {
        super(start);
        this.sectionCourses = sectionCourses;
    }

    /**
     * Creates a class block with default duration and no assignments.
     *
     * @param start block start time
     */
    public ClassBlock(LocalTime start) {
        super(start);
    }

    /**
     * Creates a class block with explicit duration and assignments.
     *
     * @param start block start time
     * @param length block duration
     * @param sectionCourses section-to-assignment mapping
     */
    public ClassBlock(LocalTime start, Duration length, Map<Section, Assignment> sectionCourses) {
        super(start, length);
        this.sectionCourses = sectionCourses;
    }

    /**
     * Creates an empty class block for serialization frameworks.
     */
    public ClassBlock() {
    }

    /**
     * Returns the section-to-assignment mapping.
     *
     * @return section course assignments
     */
    public Map<Section, Assignment> getSectionCourses() {
        return sectionCourses;
    }

    /**
     * Replaces the section-to-assignment mapping.
     *
     * @param sectionCourses section course assignments
     */
    public void setSectionCourses(Map<Section, Assignment> sectionCourses) {
        this.sectionCourses = sectionCourses;
    }

    /**
     * Sets the assignment for one section.
     *
     * @param section section to update
     * @param assignment assignment to set
     */
    public void setSectionCourse(Section section, Assignment assignment) {
        sectionCourses.put(section, assignment);
    }

    /**
     * Returns whether this block currently includes split-course content.
     *
     * @return {@code true} when split content is present
     */
    public boolean isSplit() {
        return split;
    }

    /**
     * Explicitly sets the split flag.
     *
     * @param split split state
     */
    public void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * Recomputes split state from the current section assignments.
     */
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
