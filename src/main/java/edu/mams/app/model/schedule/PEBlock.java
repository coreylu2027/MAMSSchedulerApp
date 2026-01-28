package edu.mams.app.model.schedule;

import java.time.Duration;
import java.time.LocalTime;

public class PEBlock extends ScheduleEntry {
    private String group1;
    private String group2;

    public PEBlock(String group1, String group2) {
        super(LocalTime.of(13,30), Duration.ofMinutes(75));
        this.group1 = group1;
        this.group2 = group2;
    }

    public String getGroup1() {
        return group1;
    }

    public String getGroup2() {
        return group2;
    }

    @Override
    public String toString() {
        return "PE{" +
                "group1='" + group1 + '\'' +
                ", group2='" + group2 + '\'' +
                ", start=" + start +
                ", length=" + length +
                '}';
    }
}
