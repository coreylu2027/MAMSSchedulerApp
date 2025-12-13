package edu.mams.app.model.schedule;

public class Event extends Assignment {
    public Event(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                '}';
    }
}