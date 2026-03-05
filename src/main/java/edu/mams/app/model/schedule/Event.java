package edu.mams.app.model.schedule;

/**
 * Represents an all-school event assignment such as lunch or flex.
 */
public class Event extends Assignment {
    /**
     * Creates an event with a name.
     *
     * @param name event name
     */
    public Event(String name) {
        super(name);
    }

    /**
     * Creates an empty event for serialization frameworks.
     */
    public Event() {
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                '}';
    }
}
