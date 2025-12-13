package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;

import java.time.Duration;
import java.time.LocalTime;


public class TeacherTimeRequest extends TeacherRequest {
    private Duration length;
    private LocalTime startTime;

    public TeacherTimeRequest(Teacher teacher, Assignment assignment, String reason, Duration length, LocalTime startTime) {
        super(teacher, assignment, reason);
        this.length = length;
        this.startTime = startTime;
    }

    public TeacherTimeRequest(Teacher teacher, String reason, Duration length, LocalTime startTime) {
        super(teacher, reason);
        this.length = length;
        this.startTime = startTime;
    }

    public Duration getLength() {
        return length;
    }

    public void setLength(Duration length) {
        this.length = length;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
