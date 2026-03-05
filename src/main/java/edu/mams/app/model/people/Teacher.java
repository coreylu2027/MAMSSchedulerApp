package edu.mams.app.model.people;

import java.util.Objects;

/**
 * Represents a teacher identified by name.
 */
public class Teacher {
    private String name;

    /**
     * Creates a teacher with a name.
     *
     * @param name teacher name
     */
    public Teacher(String name) {
        this.name = name;
    }

    /**
     * Returns the teacher name.
     *
     * @return teacher name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                '}';
    }

    /**
     * Creates an empty teacher for serialization frameworks.
     */
    public Teacher() {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Teacher teacher)) return false;
        return Objects.equals(name, teacher.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
