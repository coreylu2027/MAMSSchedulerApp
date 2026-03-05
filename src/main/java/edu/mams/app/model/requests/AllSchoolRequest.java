package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Teacher request that converts a time slot into an all-school block.
 */
public class AllSchoolRequest extends TeacherTimeRequest {
    /**
     * Creates an all-school request with a linked assignment.
     *
     * @param teacher requesting teacher
     * @param assignment assignment to schedule
     * @param reason reason text
     * @param length block duration
     * @param startTime block start time
     */
    public AllSchoolRequest(Teacher teacher, Assignment assignment, String reason, Duration length, LocalTime startTime) {
        super(teacher, assignment, reason, length, startTime);
    }

    /**
     * Creates an all-school request without a pre-linked assignment.
     *
     * @param teacher requesting teacher
     * @param reason reason text
     * @param length block duration
     * @param startTime block start time
     */
    public AllSchoolRequest(Teacher teacher, String reason, Duration length, LocalTime startTime) {
        super(teacher, reason, length, startTime);
    }

    /**
     * Creates an empty all-school request for serialization frameworks.
     */
    public AllSchoolRequest() {
    }
}
