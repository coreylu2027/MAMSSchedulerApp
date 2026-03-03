package edu.mams.app.model.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.Assignment;
import edu.mams.app.model.schedule.Course;

import java.util.List;
import java.util.Objects;

/**
 * Base type for teacher-submitted scheduling requests.
 */
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

    /**
     * Creates an empty request for serialization frameworks.
     */
    public TeacherRequest() {
    }

    /**
     * Creates a request with teacher, assignment and reason.
     *
     * @param teacher requesting teacher
     * @param assignment assignment involved in the request
     * @param reason reason text
     */
    public TeacherRequest(Teacher teacher, Assignment assignment, String reason) {
        this.teacher = teacher;
        this.assignment = assignment;
        this.reason = reason;
    }

    /**
     * Creates a request without a pre-linked assignment.
     *
     * @param teacher requesting teacher
     * @param reason reason text
     */
    public TeacherRequest(Teacher teacher, String reason) {
        this.teacher = teacher;
        this.reason = reason;
    }

    /**
     * Returns the requesting teacher.
     *
     * @return teacher
     */
    public Teacher getTeacher() {
        return teacher;
    }

    /**
     * Sets the requesting teacher.
     *
     * @param teacher teacher to assign
     */
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    /**
     * Returns the associated assignment.
     *
     * @return assignment, possibly {@code null}
     */
    public Assignment getAssignment() {
        return assignment;
    }

    /**
     * Sets the associated assignment.
     *
     * @param assignment assignment to assign
     */
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    /**
     * Returns the reason text.
     *
     * @return reason text, possibly {@code null}
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason text.
     *
     * @param reason reason text
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Links this request to a matching teacher-owned course from a list.
     *
     * @param assignmentList candidate assignments
     */
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
        if (o == null || getClass() != o.getClass()) return false;
        TeacherRequest that = (TeacherRequest) o;
        return Objects.equals(teacher, that.teacher)
                && Objects.equals(assignment, that.assignment)
                && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), teacher, assignment, reason);
    }
}
