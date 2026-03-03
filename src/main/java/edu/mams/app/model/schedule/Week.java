package edu.mams.app.model.schedule;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents one school week and its five scheduled days.
 */
public class Week {
    private List<Day> days;

    /**
     * Creates a week from an existing day list.
     *
     * @param days day objects in week order
     */
    public Week(List<Day> days) {
        this.days = days;
    }

    /**
     * Creates a default five-day week starting at the given date with day number 0.
     *
     * @param startingDate date for the first day
     */
    public Week(LocalDate startingDate) {
        this(startingDate, 0);
    }

    /**
     * Creates a default five-day week.
     *
     * @param startingDate date for the first day
     * @param startingDayNumber day number for the first day
     */
    public Week(LocalDate startingDate, int startingDayNumber) {
        days = new ArrayList<>();
        for (int d = 0; d < 5; d++) {
            days.add(new Day(startingDate.plusDays(d), startingDayNumber + d, new ArrayList<>(), new ArrayList<>()));
        }
    }

    /**
     * Creates an empty week for serialization frameworks.
     */
    public Week() {
    }

    /**
     * Returns all days in this week.
     *
     * @return mutable day list
     */
    public List<Day> getDays() {
        return days;
    }

    /**
     * Replaces the week day list.
     *
     * @param days new day list
     */
    public void setDays(List<Day> days) {
        this.days = days;
    }

    /**
     * Returns the first day date.
     *
     * @return starting date
     */
    @JsonIgnore
    public LocalDate getStartingDate() {
        return days.get(0).getDate();
    }

    /**
     * Returns the last day date.
     *
     * @return ending date
     */
    @JsonIgnore
    public LocalDate getEndingDate() {
        return days.get(days.size() - 1).getDate();
    }

    /**
     * Returns the first day number.
     *
     * @return starting day number
     */
    @JsonIgnore
    public int getStartingDayNumber () {
        return days.get(0).getDayNumber();
    }

    /**
     * Returns the last day number.
     *
     * @return ending day number
     */
    @JsonIgnore
    public int getEndingDayNumber () {
        return days.get(days.size() - 1).getDayNumber();
    }

    /**
     * Finds the day matching a date.
     *
     * @param date target date
     * @return matching day
     * @throws IllegalArgumentException when no day matches the date
     */
    public Day getDay(LocalDate date) {
        for (Day day : days) {
            if (day.getDate().equals(date)) {
                return day;
            }
        }
        throw new IllegalArgumentException("Day not found for date: " + date);
    }

    /**
     * Loads requests for every day from the default request file.
     */
    public void loadRequests() {
        for (Day day : days) {
            day.loadRequests();
        }
    }

    /**
     * Loads requests for every day from the provided request file.
     *
     * @param requestFile source CSV file
     */
    public void loadRequests(File requestFile) {
        for (Day day : days) {
            day.loadRequests(requestFile);
        }
    }

    /**
     * Generates day blocks using the default weekly template sequence.
     */
    public void generateBlocks() {
        generateBlocks(new ArrayList<String>(Arrays.asList("Class Meeting Day", "Homeroom Day", "Flex Day", "PE Day", "Homeroom Day")));
    }

    /**
     * Generates day blocks using one template name per day.
     *
     * @param templates day template names in week order
     */
    public void generateBlocks(List<String> templates) {
        for (int d = 0; d < days.size(); d++) {
            days.get(d).generateBlocks(templates.get(d));
        }
}
}
