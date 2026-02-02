package edu.mams.app.model.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.AllSchoolBlock;
import edu.mams.app.model.schedule.Assignment;
import edu.mams.app.model.schedule.ClassBlock;
import edu.mams.app.model.schedule.Course;

import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AllSchoolRequest.class, name = "allSchoolRequest"),
        @JsonSubTypes.Type(value = AvoidTimeRequest.class, name = "avoidTimeRequest"),
})
public abstract class TeacherRequest {
    private Teacher teacher;
    private Assignment assignment;
    private String reason;

    public TeacherRequest() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeacherRequest that)) return false;
        return Objects.equals(teacher, that.teacher)
                && Objects.equals(assignment, that.assignment)
                && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacher, assignment, reason);
    }
}

