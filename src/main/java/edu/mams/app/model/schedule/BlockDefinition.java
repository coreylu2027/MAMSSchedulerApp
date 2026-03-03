package edu.mams.app.model.schedule;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Immutable description of one block instance inside a {@link DayTemplate}.
 */
public class BlockDefinition {
    private String type;  // "ClassBlock", "AllSchoolEvent", etc.
    private LocalTime start;
    private Duration length;
    private String label; // Optional, like event name

    /**
     * Creates a template block definition.
     *
     * @param type block type key
     * @param start block start time
     * @param length block length
     * @param label optional label shown to users
     */
    public BlockDefinition(String type, LocalTime start, Duration length, String label) {
        this.type = type;
        this.start = start;
        this.length = length;
        this.label = label;
    }

    /**
     * Returns the block type key.
     *
     * @return block type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the block start time.
     *
     * @return start time
     */
    public LocalTime getStart() {
        return start;
    }

    /**
     * Returns the block duration.
     *
     * @return block length
     */
    public Duration getLength() {
        return length;
    }

    /**
     * Returns the optional label.
     *
     * @return label text, possibly {@code null}
     */
    public String getLabel() {
        return label;
    }
}
