package edu.mams.app.model.people;

import java.util.Objects;

/**
 * Base type for named grouping concepts used in scheduling.
 */
public abstract class Group {
    protected String name;

    /**
     * Creates a named group.
     *
     * @param name group name
     */
    public Group(String name) {
        this.name = name;
    }

    /**
     * Creates an empty group for serialization frameworks.
     */
    public Group() {
    }

    /**
     * Returns the group name.
     *
     * @return group name
     */
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
