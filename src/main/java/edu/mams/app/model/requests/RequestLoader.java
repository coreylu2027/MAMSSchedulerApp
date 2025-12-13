package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestLoader {
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    public static LocalDate parseDate(String s) {
        for (DateTimeFormatter f : FORMATTERS) {
            try {
                return LocalDate.parse(s, f);
            } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Unsupported date format: " + s);
    }

    public static LocalTime parseLocalTime(String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm:ss a");
        return LocalTime.parse(s.trim(), formatter);
    }

    public static List<TeacherRequest> loadRequests(LocalDate loadDate) {
        List<TeacherRequest> requests = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("requests.csv"))) {
            String line;
            boolean header = true;

            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }

                String[] fields = line.split(",");

                String teacher = fields[1];
                LocalDate date = parseDate(fields[2]);
                String type = fields[3];
                LocalTime time = parseLocalTime(fields[4]);
                Duration duration = Duration.ofMinutes((long) (Double.parseDouble(fields[5])*60));
                String reason = null;
                try {
                    reason = fields[6];
                } catch (Exception e) {
                }

                if (!date.equals(loadDate)) {
                    continue;
                }

                TeacherRequest request;
                switch (type) {
                    case "All School" -> {
                        request = new AllSchoolRequest(new Teacher(teacher), reason, duration, time);
                    }
                    case "Avoid" -> {
                        request = new AvoidTimeRequest(new Teacher(teacher), reason, duration, time);
                    }
                    default -> throw new IllegalArgumentException("Unknown type: " + type);
                }
                requests.add(request);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return requests;
    }
}