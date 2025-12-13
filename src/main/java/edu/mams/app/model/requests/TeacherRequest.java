package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;
import edu.mams.app.model.schedule.Course;

import java.util.List;

public abstract class TeacherRequest {
    private Teacher teacher;
    private Assignment assignment;
    private String reason;

    public TeacherRequest(Teacher teacher, Assignment assignment, String reason) {
        this.teacher = teacher;
        this.assignment = assignment;
        this.reason = reason;
    }

    public TeacherRequest(Teacher teacher, String reason) {
        this.teacher = teacher;
        this.reason = reason;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setAssignmentFromList(List<Assignment> assignmentList) {
        for (Assignment otherAssignment : assignmentList) {
            if (otherAssignment instanceof Course otherCourse) {
                if (otherCourse.getTeacher().equals(teacher)) {
                    assignment = otherCourse;
                    return;
                }
            }
        }
    }
}

