package edu.mams.app.model.schedule;

import java.time.Duration;
import java.time.LocalTime;

public class PEBlock extends ScheduleEntry {
    private String groupAName;
    private String groupAActivity;
    private String groupBName;
    private String groupBActivity;

    public PEBlock() {
    }

    public PEBlock(String group1, String group2) {
        this(LocalTime.of(13, 30), Duration.ofMinutes(75), "Group A", group1, "Group B", group2);
    }

    public PEBlock(LocalTime start, Duration length, String groupAName, String groupAActivity, String groupBName, String groupBActivity) {
        super(start, length);
        this.groupAName = groupAName;
        this.groupAActivity = groupAActivity;
        this.groupBName = groupBName;
        this.groupBActivity = groupBActivity;
    }

    public PEBlock(LocalTime start, String groupAName, String groupAActivity, String groupBName, String groupBActivity) {
        this(start, Duration.ofMinutes(75), groupAName, groupAActivity, groupBName, groupBActivity);
    }

    public String getGroup1() {
        return groupAActivity;
    }

    public String getGroup2() {
        return groupBActivity;
    }

    public void setGroup1(String group1) {
        this.groupAActivity = group1;
    }

    public void setGroup2(String group2) {
        this.groupBActivity = group2;
    }

    public String getGroupAName() {
        return groupAName;
    }

    public void setGroupAName(String groupAName) {
        this.groupAName = groupAName;
    }

    public String getGroupAActivity() {
        return groupAActivity;
    }

    public void setGroupAActivity(String groupAActivity) {
        this.groupAActivity = groupAActivity;
    }

    public String getGroupBName() {
        return groupBName;
    }

    public void setGroupBName(String groupBName) {
        this.groupBName = groupBName;
    }

    public String getGroupBActivity() {
        return groupBActivity;
    }

    public void setGroupBActivity(String groupBActivity) {
        this.groupBActivity = groupBActivity;
    }

    @Override
    public String toString() {
        return "PE{" +
                "groupAName='" + groupAName + '\'' +
                ", groupAActivity='" + groupAActivity + '\'' +
                ", groupBName='" + groupBName + '\'' +
                ", groupBActivity='" + groupBActivity + '\'' +
                ", start=" + start +
                ", length=" + length +
                '}';
    }
}
