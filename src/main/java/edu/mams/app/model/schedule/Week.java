package edu.mams.app.model.schedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Week {
    private List<Day> days;

    public Week(List<Day> days) {
        this.days = days;
    }

    public Week() {
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public LocalDate getStartingDate() {
        return days.get(0).getDate();
    }

    public LocalDate getEndingDate() {
        return days.get(days.size() - 1).getDate();
    }

    public int getStartingDayNumber () {
        return days.get(0).getDayNumber();
    }

    public int getEndingDayNumber () {
        return days.get(days.size() - 1).getDayNumber();
    }

    public Day getDay(LocalDate date) {
        for (Day day : days) {
            if (day.getDate().equals(date)) {
                return day;
            }
        }
        throw new IllegalArgumentException("Day not found for date: " + date);
    }

    public void loadRequests() {
        for (int d = 0; d < days.size(); d++) {
            days.get(d).loadRequests();
        }
    }

    public void generateBlocks() {
        generateBlocks(new ArrayList<String>(Arrays.asList("Class Meeting Day", "Homeroom Day", "Flex Day", "PE Day", "Homeroom Day")));
    }

    public void generateBlocks(List<String> templates) {
        for (int d = 0; d < days.size(); d++) {
            days.get(d).generateBlocks(templates.get(d));
        }
}
}