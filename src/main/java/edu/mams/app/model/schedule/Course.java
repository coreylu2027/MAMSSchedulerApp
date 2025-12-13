package edu.mams.app.model.schedule;

import edu.mams.app.model.people.Teacher;

public class Course extends Assignment{
    private Teacher teacher;

    public Course(String name, Teacher teacher) {
        super(name);
        this.teacher = teacher;
    }

    public Course(String name) {
        super(name);
    }

    public String getName() {
        return name;
    }

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