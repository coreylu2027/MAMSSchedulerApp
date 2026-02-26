package edu.mams.app.model.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void parseDateSupportsMultipleFormatsAndQuotedValues() {
        assertEquals(LocalDate.of(2026, 2, 3), RequestLoader.parseDate("2026-02-03"));
        assertEquals(LocalDate.of(2026, 2, 3), RequestLoader.parseDate("2/3/2026"));
        assertEquals(LocalDate.of(2026, 2, 3), RequestLoader.parseDate("\"02/03/2026\""));
    }

    @Test
    void parseDateRejectsUnsupportedFormat() {
        assertThrows(IllegalArgumentException.class, () -> RequestLoader.parseDate("2026/02/03"));
    }

    @Test
    void parseLocalTimeSupportsQuotedHourMinute() {
        assertEquals(LocalTime.of(7, 45), RequestLoader.parseLocalTime("\"7:45\""));
    }

    @Test
    void loadRequestParsesCsvRowsAndFiltersByDate() throws Exception {
        Path csvPath = tempDir.resolve("requests.csv");
        Files.writeString(csvPath, String.join("\n",
                "Timestamp,Teacher,Date,Type,Time,Duration,Reason",
                "2026-02-01,\"Smith, Jane\",2/3/2026,All School,7:45,1.5,\"Assembly, prep\"",
                "2026-02-01,Brown,2/3/2026,Avoid,10:00,0.5,\"Planning\"",
                "2026-02-01,Other,2/4/2026,Avoid,11:00,1.0,\"Other day\""
        ));

        List<TeacherRequest> requests = RequestLoader.loadRequest(new File(csvPath.toString()), LocalDate.of(2026, 2, 3));

        assertEquals(2, requests.size());

        TeacherTimeRequest first = assertInstanceOf(TeacherTimeRequest.class, requests.get(0));
        assertInstanceOf(AllSchoolRequest.class, requests.get(0));
        assertEquals("Smith, Jane", first.getTeacher().getName());
        assertEquals(LocalTime.of(7, 45), first.getStartTime());
        assertEquals(Duration.ofMinutes(90), first.getLength());
        assertEquals("Assembly, prep", first.getReason());

        TeacherTimeRequest second = assertInstanceOf(TeacherTimeRequest.class, requests.get(1));
        assertInstanceOf(AvoidTimeRequest.class, requests.get(1));
        assertEquals("Brown", second.getTeacher().getName());
        assertEquals(LocalTime.of(10, 0), second.getStartTime());
        assertEquals(Duration.ofMinutes(30), second.getLength());
    }
}
