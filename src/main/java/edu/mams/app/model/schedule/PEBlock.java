package edu.mams.app.model.schedule;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Schedule entry for PE where two groups run parallel activities.
 */
public class PEBlock extends ScheduleEntry {
    private String groupAName;
    private String groupAActivity;
    private String groupBName;
    private String groupBActivity;

    /**
     * Creates an empty PE block for serialization frameworks.
     */
    public PEBlock() {
    }

    /**
     * Creates a PE block with default labels and start time.
     *
     * @param group1 activity for group A
     * @param group2 activity for group B
     */
    public PEBlock(String group1, String group2) {
        this(LocalTime.of(13, 30), Duration.ofMinutes(75), "Group A", group1, "Group B", group2);
    }

    /**
     * Creates a PE block with explicit timing and labels.
     *
     * @param start block start time
     * @param length block duration
     * @param groupAName display name for group A
     * @param groupAActivity activity for group A
     * @param groupBName display name for group B
     * @param groupBActivity activity for group B
     */
    public PEBlock(LocalTime start, Duration length, String groupAName, String groupAActivity, String groupBName, String groupBActivity) {
        super(start, length);
        this.groupAName = groupAName;
        this.groupAActivity = groupAActivity;
        this.groupBName = groupBName;
        this.groupBActivity = groupBActivity;
    }

    /**
     * Creates a PE block with the default PE duration.
     *
     * @param start block start time
     * @param groupAName display name for group A
     * @param groupAActivity activity for group A
     * @param groupBName display name for group B
     * @param groupBActivity activity for group B
     */
    public PEBlock(LocalTime start, String groupAName, String groupAActivity, String groupBName, String groupBActivity) {
        this(start, Duration.ofMinutes(75), groupAName, groupAActivity, groupBName, groupBActivity);
    }

    /**
     * Legacy accessor for group A activity.
     *
     * @return group A activity
     */
    public String getGroup1() {
        return groupAActivity;
    }

    /**
     * Legacy accessor for group B activity.
     *
     * @return group B activity
     */
    public String getGroup2() {
        return groupBActivity;
    }

    /**
     * Legacy mutator for group A activity.
     *
     * @param group1 activity text
     */
    public void setGroup1(String group1) {
        this.groupAActivity = group1;
    }

    /**
     * Legacy mutator for group B activity.
     *
     * @param group2 activity text
     */
    public void setGroup2(String group2) {
        this.groupBActivity = group2;
    }

    /**
     * Returns the display name for group A.
     *
     * @return group A name
     */
    public String getGroupAName() {
        return groupAName;
    }

    /**
     * Updates the display name for group A.
     *
     * @param groupAName group A name
     */
    public void setGroupAName(String groupAName) {
        this.groupAName = groupAName;
    }

    /**
     * Returns the activity for group A.
     *
     * @return group A activity
     */
    public String getGroupAActivity() {
        return groupAActivity;
    }

    /**
     * Updates the activity for group A.
     *
     * @param groupAActivity group A activity
     */
    public void setGroupAActivity(String groupAActivity) {
        this.groupAActivity = groupAActivity;
    }

    /**
     * Returns the display name for group B.
     *
     * @return group B name
     */
    public String getGroupBName() {
        return groupBName;
    }

    /**
     * Updates the display name for group B.
     *
     * @param groupBName group B name
     */
    public void setGroupBName(String groupBName) {
        this.groupBName = groupBName;
    }

    /**
     * Returns the activity for group B.
     *
     * @return group B activity
     */
    public String getGroupBActivity() {
        return groupBActivity;
    }

    /**
     * Updates the activity for group B.
     *
     * @param groupBActivity group B activity
     */
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
