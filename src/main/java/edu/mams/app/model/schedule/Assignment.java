package edu.mams.app.model.schedule;

import java.util.Objects;

public abstract class Assignment {
    protected String name;

    protected Assignment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Assignment that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}