package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;


/**
 * Teacher request with explicit time and duration constraints.
 */
public class TeacherTimeRequest extends TeacherRequest {
    private Duration length;
    private LocalTime startTime;

    /**
     * Creates a timed request with a linked assignment.
     *
     * @param teacher requesting teacher
     * @param assignment assignment involved in the request
     * @param reason reason text
     * @param length request duration
     * @param startTime requested start time
     */
    public TeacherTimeRequest(Teacher teacher, Assignment assignment, String reason, Duration length, LocalTime startTime) {
        super(teacher, assignment, reason);
        this.length = length;
        this.startTime = startTime;
    }

    /**
     * Creates an empty timed request for serialization frameworks.
     */
    public TeacherTimeRequest() {
    }

    /**
     * Creates a timed request without a pre-linked assignment.
     *
     * @param teacher requesting teacher
     * @param reason reason text
     * @param length request duration
     * @param startTime requested start time
     */
    public TeacherTimeRequest(Teacher teacher, String reason, Duration length, LocalTime startTime) {
        super(teacher, reason);
        this.length = length;
        this.startTime = startTime;
    }

    /**
     * Returns the requested duration.
     *
     * @return request length
     */
    public Duration getLength() {
        return length;
    }

    /**
     * Sets the requested duration.
     *
     * @param length request length
     */
    public void setLength(Duration length) {
        this.length = length;
    }

    /**
     * Returns the requested start time.
     *
     * @return start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the requested start time.
     *
     * @param startTime start time
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeacherTimeRequest that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(length, that.length) && Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), length, startTime);
    }
}
