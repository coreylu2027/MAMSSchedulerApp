package edu.mams.app.model.schedule;

import edu.mams.app.model.people.Group;
import edu.mams.app.model.people.HalfSection;
import edu.mams.app.model.people.Section;
import edu.mams.app.model.requests.AllSchoolRequest;
import edu.mams.app.model.requests.RequestLoader;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.util.ScheduleBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single day that includes various scheduling information such as date, day number,
 * schedule entries, teacher requests, notes, clubs, sections, and class assignments. Provides
 * functionality to manage and interact with these elements, including updating schedules,
 * generating blocks, and retrieving section-specific schedules.
 */
public class Day {
    private LocalDate date;
    private int dayNumber;

    public Day() {
    }

    private List<ScheduleEntry> entries;
    private List<TeacherRequest> requests;

    private List<String> notes;
    private List<String> clubs;

    private List<Section> sections;
    private List<Assignment> classes;

    private boolean split = false;
    private Assignment splitCourse = null;

    private String template;

    public Day(LocalDate date, int dayNumber, List<Section> sections, List<Assignment> classes) {
        this.date = date;
        this.dayNumber = dayNumber;
        this.sections = sections;
        this.classes = new ArrayList<>(classes);
    }

    public List<ScheduleEntry> getEntries() {
        return entries;
    }

    public ScheduleEntry getEntry(int i) {
        return entries.get(i);
    }

    public List<TeacherRequest> getRequests() {
        return requests;
    }

    public void setEntries(List<ScheduleEntry> entries) {
        this.entries = entries;
    }

    /**
     * Returns a copy of this Day with a deep-copied entries list.
     * (Other fields are copied in the "safe enough" way for this app:
     *  - date/dayNumber are immutable
     *  - sections/classes are reused or shallow-copied as appropriate
     *  - requests/notes/clubs are reused as-is)
     */
    public Day copy() {
        Day copy = new Day(this.date, this.dayNumber, this.sections, this.classes);
        copy.entries = deepCopyEntries(this.entries);
        copy.requests = this.requests;
        copy.notes = this.notes;
        copy.clubs = this.clubs;
        return copy;
    }

    public void setSplitCourse(Course course) {
        this.split = true;
        this.splitCourse = course;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<String> getClubs() {
        return clubs;
    }

    public boolean isSplit() {
        return split;
    }

    public Assignment getSplitCourse() {
        return splitCourse;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    /**
     * Returns a copy of this Day with one entry replaced (and the list deep-copied).
     */
    public Day withUpdatedEntry(int index, ScheduleEntry newEntry) {
        Day copy = this.copy();

        if (index < copy.entries.size()) {
            // normal edit of an existing block
            copy.entries.set(index, newEntry);
        } else if (index == copy.entries.size()) {
            // newly inserted block at the end
            copy.entries.add(newEntry);
        } else {
            // defensive: should never happen
            throw new IllegalStateException(
                    "Block index " + index + " out of bounds for entries size " + copy.entries.size()
            );
        }

        return copy;
    }


    /**
     * Returns a copy of this Day where entry durations have been recalculated.
     * Does not mutate the original Day.
     */
    public Day withUpdatedDurations() {
        Day copy = this.copy();
        copy.updateDurations(); // safe: we're mutating the copy's entries
        return copy;
    }

    public String getSectionSchedule(Group group) {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule for Section ").append(group.getName()).append(":\n");

        Section masterSection;
        HalfSection halfSection = null;

        if (group instanceof Section section) {
            masterSection = section;
        } else if (group instanceof HalfSection half) {
            masterSection = half.getParentSection();
            halfSection = half;
        } else {
            throw new IllegalArgumentException("Unknown group type: " + group);
        }

        for (ScheduleEntry entry : entries) {
            if (entry instanceof ClassBlock classBlock) {
                Assignment assignment = classBlock.getSectionCourses().get(masterSection);

                if (assignment instanceof SplitCourse splitCourse && halfSection != null) {
                    // If it’s a split course and the group is a half section
                    Assignment halfAssignment = splitCourse.getHalfSectionCourses().get(halfSection);
                    if (halfAssignment != null) {
                        sb.append(" - ").append(halfAssignment.getName())
                                .append(" at ").append(entry.getStart())
                                .append(" for ").append(entry.getLength())
                                .append(" (Half Section)\n");
                    }
                } else if (assignment != null) {
                    // Normal course
                    sb.append(" - ").append(assignment.getName())
                            .append(" at ").append(entry.getStart())
                            .append(" for ").append(entry.getLength())
                            .append("\n");
                }
            }

            else if (entry instanceof AllSchoolBlock allSchoolBlock) {
                Assignment assignment = allSchoolBlock.getAssignment();
                sb.append(" - ").append(assignment.getName())
                        .append(" at ").append(entry.getStart())
                        .append(" for ").append(entry.getLength())
                        .append(" (All School Block)\n");
            }        }

        return sb.toString();
    }

    /**
     * Loads teacher requests for a specific date and processes them according to their type.
     *
     * This method performs the following logic:
     * 1. Calls {@link RequestLoader#loadRequests(LocalDate)} to load teacher requests
     *    matching the current instance's date.
     * 2. Iterates through the loaded requests, invoking the {@link TeacherRequest#setAssignmentFromList(List)}
     *    method for each request to assign corresponding assignments from the `classes` list.
     * 3. Processes all instances of {@link AllSchoolRequest} within the list of loaded requests:
     *    - The assignment associated with the {@link AllSchoolRequest} is removed from the `classes` list.
     *
     * The method updates the `requests` field with the list of loaded requests and modifies the
     * `classes` list by removing assignments referenced by `AllSchoolRequest` objects.
     *
     * Preconditions:
     * - The `date` field must be initialized to define the load criteria for requests.
     * - The `classes` field must be populated with a list of assignments prior to method invocation.
     *
     * Postconditions:
     * - The `requests` list is populated with teacher requests that match the current instance's date.
     * - Assignments for the loaded requests are set using the `classes` list.
     * - The `classes` list is updated to exclude assignments referenced by any `AllSchoolRequest`.
     */
    public void loadRequests() {
        requests = RequestLoader.loadRequests(date);
        for (int i = 0; i < requests.size(); i++) {
            requests.get(i).setAssignmentFromList(classes);
        }
        for (TeacherRequest request : requests) {
            if (request instanceof AllSchoolRequest allSchoolRequest) {
                classes.remove(allSchoolRequest.getAssignment());
            }
        }
    }

    public List<Assignment> getClasses() {
        return classes;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Generates a schedule consisting of blocks for the day based on the specified template.
     *
     * This method uses the provided template name to build a schedule, which includes
     * different types of blocks such as class blocks or all-school events. The generated
     * blocks are stored in the `entries` field of the current instance.
     *
     * @param templateName the name of the schedule template to use for generating blocks.
     */
    public void generateBlocks(String templateName) {
        this.template = templateName;
        if (split) {
            entries = ScheduleBuilder.buildNewSplitSchedule(templateName, this);
        } else {
            entries = ScheduleBuilder.buildNewNoSplitSchedule(templateName, this);
        }
    }

    /**
     * Updates the duration of each schedule entry in the list based on their start times.
     *
     * This method iterates over the list of schedule entries and calculates the duration
     * for each entry by finding the time difference between its start time and the start
     * time of the next entry. For the last entry in the list, its duration is calculated
     * as the time difference between its start time and a fixed end time of 14:45.
     *
     * Preconditions:
     * - The `entries` list contains at least one schedule entry with initialized start times.
     *
     * Postconditions:
     * - Each schedule entry in the list has its duration (`length`) updated. The last entry's
     *   duration is set relative to the fixed end time of 14:45.
     */
    public void updateDurations() {
        for (int i = 0; i < entries.size() - 1; i++) {
            entries.get(i).setLength(Duration.between(entries.get(i).getStart(), entries.get(i + 1).getStart()));
        }
        entries.get(entries.size() - 1).setLength(Duration.between(entries.get(entries.size() - 1).getStart(), LocalTime.of(14, 45)));
    }

    private static List<ScheduleEntry> deepCopyEntries(List<ScheduleEntry> source) {
        if (source == null) return null;
        List<ScheduleEntry> out = new ArrayList<>(source.size());
        for (ScheduleEntry e : source) {
            out.add(copyEntry(e));
        }
        return out;
    }

    private static ScheduleEntry copyEntry(ScheduleEntry e) {
        if (e == null) return null;

        if (e instanceof ClassBlock cb) {
            Map<Section, Assignment> original = cb.getSectionCourses();
            Map<Section, Assignment> copied = (original == null) ? null : new HashMap<>(original);
            return new ClassBlock(cb.getStart(), cb.getLength(), copied);
        }

        if (e instanceof AllSchoolBlock ab) {
            return new AllSchoolBlock(ab.getStart(), ab.getLength(), ab.getAssignment(), ab.getReason());
        }

        // If you add more ScheduleEntry subclasses later, extend this copier.
        throw new IllegalArgumentException("Unsupported ScheduleEntry type for copy: " + e.getClass().getName());
    }

    @Override
    public String toString() {
        return "Day{" +
                "date=" + date +
                ", dayNumber=" + dayNumber +
                ", blocks=" + entries +
                ", requests=" + requests +
                ", notes=" + notes +
                ", clubs=" + clubs +
                '}';
    }
}
