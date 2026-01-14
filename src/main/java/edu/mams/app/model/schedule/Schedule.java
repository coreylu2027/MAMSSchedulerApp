package edu.mams.app.model.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Schedule {
    private Map<LocalDate, Week> weeks = new LinkedHashMap<>();

    public Schedule() {
    }

    public void addWeek(Week week) {
        weeks.put(week.getStartingDate(), week);
    }

    public Week getWeek(LocalDate startDate) {
        return weeks.get(startDate);
    }

    public Map<LocalDate, Week> getWeeks() {
        return weeks;
    }

    public void setWeeks(Map<LocalDate, Week> weeks) {
        this.weeks = weeks;
    }

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

    public static Schedule loadFromFile(File file) {
        try {
            ObjectMapper mapper = createMapper();
            return mapper.readValue(file, Schedule.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schedule", e);
        }
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDate, LocalTime
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
