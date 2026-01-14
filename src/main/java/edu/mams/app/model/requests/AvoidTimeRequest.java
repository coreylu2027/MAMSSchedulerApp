package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;

import java.time.Duration;
import java.time.LocalTime;

public class AvoidTimeRequest extends TeacherTimeRequest {
    public AvoidTimeRequest(Teacher teacher, Assignment assignment, String reason, Duration length, LocalTime startTime) {
        super(teacher, assignment, reason, length, startTime);
    }

    public AvoidTimeRequest(Teacher teacher, String reason, Duration length, LocalTime startTime) {
        super(teacher, reason, length, startTime);
    }

    public AvoidTimeRequest() {
    }
}