package edu.mams.app.model.schedule;

import edu.mams.app.model.requests.AllSchoolRequest;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Schedule entry representing a whole-school block that applies to all sections.
 */
public class AllSchoolBlock extends ScheduleEntry {
    private Assignment assignment;
    private String reason;

    /**
     * Creates a block with a start time and default length.
     *
     * @param start block start time
     */
    public AllSchoolBlock(LocalTime start) {
        super(start);
    }

    /**
     * Creates a block with an assignment.
     *
     * @param start block start time
     * @param assignment assigned event/course
     */
    public AllSchoolBlock(LocalTime start, Assignment assignment) {
        super(start);
        this.assignment = assignment;
    }

    /**
     * Creates a block with explicit length and assignment.
     *
     * @param start block start time
     * @param length block duration
     * @param assignment assigned event/course
     */
    public AllSchoolBlock(LocalTime start, Duration length, Assignment assignment) {
        super(start, length);
        this.assignment = assignment;
    }

    /**
     * Creates a block with length, assignment, and note.
     *
     * @param start block start time
     * @param length block duration
     * @param assignment assigned event/course
     * @param reason optional reason text
     */
    public AllSchoolBlock(LocalTime start, Duration length, Assignment assignment, String reason) {
        super(start, length);
        this.assignment = assignment;
        this.reason = reason;
    }

    /**
     * Creates a block from an all-school request.
     *
     * @param request request definition
     */
    public AllSchoolBlock(AllSchoolRequest request) {
        super(request.getStartTime(), request.getLength());
        this.assignment = request.getAssignment();
        this.reason = request.getReason();
    }

    /**
     * Creates an empty block for serialization frameworks.
     */
    public AllSchoolBlock() {
    }

    /**
     * Returns the assignment placed in this block.
     *
     * @return assignment, possibly {@code null}
     */
    public Assignment getAssignment() {
        return assignment;
    }

    /**
     * Updates the assignment for this block.
     *
     * @param assignment assignment to use
     */
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    /**
     * Returns the reason or note associated with this block.
     *
     * @return reason text, possibly {@code null}
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason or note for this block.
     *
     * @param reason reason text
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "AllSchoolBlock{" +
                "assignment=" + assignment +
                ", start=" + start +
                ", length=" + length +
                '}';
    }
}
