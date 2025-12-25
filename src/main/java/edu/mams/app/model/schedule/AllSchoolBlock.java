package edu.mams.app.model.schedule;

import edu.mams.app.model.requests.AllSchoolRequest;

import java.time.Duration;
import java.time.LocalTime;

public class AllSchoolBlock extends ScheduleEntry {
    private Assignment assignment;
    private String reason;

    public AllSchoolBlock(LocalTime start) {
        super(start);
    }

    public AllSchoolBlock(LocalTime start, Assignment assignment) {
        super(start);
        this.assignment = assignment;
    }

    public AllSchoolBlock(LocalTime start, Duration length, Assignment assignment) {
        super(start, length);
        this.assignment = assignment;
    }

    public AllSchoolBlock(AllSchoolRequest request) {
        super(request.getStartTime(), request.getLength());
        this.assignment = request.getAssignment();
        this.reason = request.getReason();
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getReason() {
        return reason;
    }

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
