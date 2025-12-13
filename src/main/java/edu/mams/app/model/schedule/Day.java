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
import java.util.List;

/**
 * Represents a day in a schedule system.
 * This class encapsulates data related to a specific day, including its date,
 * day number, schedule blocks, teacher requests, notes, and associated clubs.
 */
public class Day {
    private LocalDate date;
    private int dayNumber;
    private List<ScheduleEntry> entries;
    private List<TeacherRequest> requests;
    private List<String> notes;
    private List<String> clubs;
    private List<Section> sections;
    private List<Assignment> classes;

    public Day(LocalDate date) {
        this.date = date;
        this.entries = new ArrayList<>();
    }

    public Day(LocalDate date, List<ScheduleEntry> entries) {
        this.date = date;
        this.entries = entries;
    }

    public Day(LocalDate date, int dayNumber, List<Section> sections, List<Assignment> classes) {
        this.date = date;
        this.dayNumber = dayNumber;
        this.sections = sections;
        this.classes = new ArrayList<>(classes);
    }

    public List<ScheduleEntry> getEntries() {
        return entries;
    }

    public List<TeacherRequest> getRequests() {
        return requests;
    }

    public void setEntries(List<ScheduleEntry> entries) {
        this.entries = entries;
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

    public LocalDate getDate() {
        return date;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public List<Section> getSections() {
        return sections;
    }

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

    public void generateBlocks(String templateName) {
        entries = ScheduleBuilder.buildSchedule(templateName, this);
    }

    public void updateDurations() {
        for (int i = 0; i < entries.size() - 1; i++) {
            entries.get(i).setLength(Duration.between(entries.get(i).getStart(), entries.get(i+1).getStart()));
        }
        entries.get(entries.size() - 1).setLength(Duration.between(entries.get(entries.size() - 1).getStart(), LocalTime.of(14, 45)));
    }
}
