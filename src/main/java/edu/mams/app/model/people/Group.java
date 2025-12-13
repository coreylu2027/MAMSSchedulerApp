package edu.mams.app.model.people;

import java.util.Objects;

public abstract class Group {
    protected String name;

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String toString();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Group group)) return false;
        return Objects.equals(name, group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

