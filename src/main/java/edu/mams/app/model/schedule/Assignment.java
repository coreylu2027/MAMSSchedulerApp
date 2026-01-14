package edu.mams.app.model.schedule;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = Course.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Course.class, name = "course"),
        @JsonSubTypes.Type(value = SplitCourse.class, name = "splitCourse"),
        @JsonSubTypes.Type(value = Event.class, name = "event"),
})
public abstract class Assignment {
    protected String name;

    protected Assignment(String name) {
        this.name = name;
    }

    public Assignment() {
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