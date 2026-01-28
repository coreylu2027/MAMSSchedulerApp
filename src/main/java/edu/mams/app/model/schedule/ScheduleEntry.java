package edu.mams.app.model.schedule;

import edu.mams.app.model.requests.TeacherTimeRequest;

import java.time.Duration;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base abstract class for all schedule entries.
 * Provides start and length getters for JSON serialization.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClassBlock.class, name = "class"),
        @JsonSubTypes.Type(value = AllSchoolBlock.class, name = "allSchool"),
})
public abstract class ScheduleEntry {
    protected LocalTime start;
    protected Duration length;

    protected ScheduleEntry(LocalTime start, Duration length) {
        this.start = start;
        this.length = length;
    }

    protected ScheduleEntry(LocalTime start) {
        this.start = start;
        this.length = Duration.ofHours(1);
    }

    protected ScheduleEntry() {
    }

    public boolean intersects(TeacherTimeRequest request) {
        return (start.isBefore(request.getStartTime().plus(request.getLength()))
                && request.getStartTime().isBefore(start.plus(length)));
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public Duration getLength() {
        return length;
    }

    public void setLength(Duration length) {
        this.length = length;
    }
}