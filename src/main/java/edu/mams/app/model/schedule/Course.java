package edu.mams.app.model.schedule;

import edu.mams.app.model.people.Teacher;

/**
 * Represents a course assignment that may be associated with a teacher.
 */
public class Course extends Assignment{
    private Teacher teacher;

    /**
     * Creates an empty course for serialization frameworks.
     */
    public Course() {
        super();
    }

    /**
     * Creates a course with a name and teacher.
     *
     * @param name course name
     * @param teacher assigned teacher
     */
    public Course(String name, Teacher teacher) {
        super(name);
        this.teacher = teacher;
    }

    /**
     * Creates a course with only a name.
     *
     * @param name course name
     */
    public Course(String name) {
        super(name);
    }

    /**
     * Returns the course name.
     *
     * @return course name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the assigned teacher.
     *
     * @return teacher, possibly {@code null}
     */
    public Teacher getTeacher() {
        return teacher;
    }

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                ", teacher=" + teacher +
                '}';
    }
}
