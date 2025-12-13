package edu.mams.app.model.schedule;

import java.time.Duration;
import java.time.LocalTime;

public class BlockDefinition {
    private String type;  // "ClassBlock", "AllSchoolEvent", etc.
    private LocalTime start;
    private Duration length;
    private String label; // Optional, like event name

    public BlockDefinition(String type, LocalTime start, Duration length, String label) {
        this.type = type;
        this.start = start;
        this.length = length;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public LocalTime getStart() {
        return start;
    }

    public Duration getLength() {
        return length;
    }

    public String getLabel() {
        return label;
    }
}