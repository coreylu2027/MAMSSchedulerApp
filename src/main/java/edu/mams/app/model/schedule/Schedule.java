package edu.mams.app.model.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Root schedule model keyed by week start date.
 */
public class Schedule {
    private Map<LocalDate, Week> weeks = new LinkedHashMap<>();

    /**
     * Creates an empty schedule.
     */
    public Schedule() {
    }

    /**
     * Adds or replaces a week by its starting date.
     *
     * @param week week to store
     */
    public void addWeek(Week week) {
        weeks.put(week.getStartingDate(), week);
    }

    /**
     * Returns a week by start date.
     *
     * @param startDate week start date
     * @return matching week or {@code null}
     */
    public Week getWeek(LocalDate startDate) {
        return weeks.get(startDate);
    }

    /**
     * Returns the week map.
     *
     * @return mutable week map
     */
    public Map<LocalDate, Week> getWeeks() {
        return weeks;
    }

    /**
     * Replaces the week map.
     *
     * @param weeks new week map
     */
    public void setWeeks(Map<LocalDate, Week> weeks) {
        this.weeks = weeks;
    }

    /**
     * Saves this schedule as JSON.
     *
     * @param file destination file
     */
    public void saveToFile(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(file, this);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save schedule", e);
        }
    }

    /**
     * Loads a schedule from JSON.
     *
     * @param file source file
     * @return parsed schedule
     */
    public static Schedule loadFromFile(File file) {
        try {
            ObjectMapper mapper = createMapper();
            return mapper.readValue(file, Schedule.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schedule", e);
        }
    }

    /**
     * Returns week start dates in insertion order.
     *
     * @return ordered start dates
     */
    public List<LocalDate> getWeekStartDates() {
        return new java.util.ArrayList<>(weeks.keySet());
    }


    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDate, LocalTime
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
