package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Teacher request indicating a time slot that should be avoided.
 */
public class AvoidTimeRequest extends TeacherTimeRequest {
    /**
     * Creates an avoid-time request with a linked assignment.
     *
     * @param teacher requesting teacher
     * @param assignment assignment to avoid scheduling
     * @param reason reason text
     * @param length avoided duration
     * @param startTime avoided start time
     */
    public AvoidTimeRequest(Teacher teacher, Assignment assignment, String reason, Duration length, LocalTime startTime) {
        super(teacher, assignment, reason, length, startTime);
    }

    /**
     * Creates an avoid-time request without a pre-linked assignment.
     *
     * @param teacher requesting teacher
     * @param reason reason text
     * @param length avoided duration
     * @param startTime avoided start time
     */
    public AvoidTimeRequest(Teacher teacher, String reason, Duration length, LocalTime startTime) {
        super(teacher, reason, length, startTime);
    }

    /**
     * Creates an empty avoid-time request for serialization frameworks.
     */
    public AvoidTimeRequest() {
    }
}
