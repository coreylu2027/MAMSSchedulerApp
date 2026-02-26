package edu.mams.app.model.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Week {
    private List<Day> days;

    public Week(List<Day> days) {
        this.days = days;
    }

    public Week(LocalDate startingDate) {
        this(startingDate, 0);
    }

    public Week(LocalDate startingDate, int startingDayNumber) {
        days = new ArrayList<>();
        for (int d = 0; d < 5; d++) {
            days.add(new Day(startingDate.plusDays(d), startingDayNumber + d, new ArrayList<>(), new ArrayList<>()));
        }
    }

    public Week() {
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    @JsonIgnore
    public LocalDate getStartingDate() {
        return days.get(0).getDate();
    }

    @JsonIgnore
    public LocalDate getEndingDate() {
        return days.get(days.size() - 1).getDate();
    }
    @JsonIgnore
    public int getStartingDayNumber () {
        return days.get(0).getDayNumber();
    }

    @JsonIgnore
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
        for (Day day : days) {
            day.loadRequests();
        }
    }

    public void loadRequests(File requestFile) {
        for (Day day : days) {
            day.loadRequests(requestFile);
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
