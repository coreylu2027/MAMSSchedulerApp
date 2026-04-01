package edu.mams.app.model.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void loadFromFileReturnsEmptyScheduleWhenFileIsMissing() {
        Path missing = tempDir.resolve("missing-schedule.json");

        Schedule schedule = Schedule.loadFromFile(missing.toFile());

        assertNotNull(schedule);
        assertTrue(schedule.getWeeks().isEmpty());
    }

    @Test
    void loadFromFileReturnsEmptyScheduleWhenFileIsBlank() throws Exception {
        Path blank = tempDir.resolve("blank-schedule.json");
        Files.writeString(blank, "");

        Schedule schedule = Schedule.loadFromFile(blank.toFile());

        assertNotNull(schedule);
        assertTrue(schedule.getWeeks().isEmpty());
    }
}
