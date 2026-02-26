package edu.mams.app.model.requests;

import edu.mams.app.model.people.Teacher;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RequestLoader {
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE,          // yyyy-MM-dd
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
    };

    public static final String FILE_NAME = "Teacher Request Form.csv";

    public static LocalDate parseDate(String s) {
        if (s == null) throw new IllegalArgumentException("Date is null");

        String cleaned = getCleaned(s);

        for (DateTimeFormatter f : FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, f);
            } catch (DateTimeParseException ignored) {}
        }
        throw new IllegalArgumentException("Unsupported date format: " + s + " (cleaned=" + cleaned + ")");
    }

    private static String getCleaned(String s) {
        // trim, remove surrounding quotes, normalize weird dashes
        String cleaned = s.trim();
        if (cleaned.length() >= 2 && cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        cleaned = cleaned
                .replace('\u2013', '-')  // en-dash
                .replace('\u2014', '-')  // em-dash
                .replace('\u2212', '-'); // minus sign
        return cleaned;
    }


    public static LocalTime parseLocalTime(String s) {
        String cleaned = getCleaned(s);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        return LocalTime.parse(cleaned.trim(), formatter);
    }

    public static List<TeacherRequest> loadRequests(LocalDate loadDate) {
        return loadRequest(new File(FILE_NAME), loadDate);
    }

    public static List<TeacherRequest> loadRequest(File requestFile, LocalDate loadDate) {
        List<TeacherRequest> requests = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(requestFile))) {
            String line;
            boolean header = true;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {
                lineNum++;
                if (header) {
                    header = false;
                    continue;
                }

                String[] fields = parseCsvLine(line);
                if (fields.length < 6) {
                    throw new IllegalArgumentException("CSV line " + lineNum + " has too few fields: " + fields.length);
                }

                String teacher = getCleaned(fields[1]);
                LocalDate date = parseDate(fields[2]);
                String type = fields[3];
                LocalTime time = parseLocalTime(fields[4]);
                Duration duration = Duration.ofMinutes((long) (Double.parseDouble(getCleaned(fields[5]))*60));
                String reason = (fields.length > 6) ? getCleaned(fields[6]) : null;

                if (!date.equals(loadDate)) {
                    continue;
                }

                TeacherRequest request;
                switch (getCleaned(type)) {
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
            throw new RuntimeException("Request file not found: " + requestFile.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return requests;
    }

    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
