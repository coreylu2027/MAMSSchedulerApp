package edu.mams.app.model.util;

import edu.mams.app.model.people.HalfSection;
import edu.mams.app.model.people.Section;
import edu.mams.app.model.schedule.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class HtmlOutput {
    private static PrintWriter out;
    private static Week week;

    public static void output(Week setWeek) {
        week = setWeek;
        try {
            out = new PrintWriter("output_java.html");
            header();
            weekHeader();
            dayHeader();
            sectionHeader();
            entries();
            close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static void header() {
        out.print("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Title</title>
                  <link rel="stylesheet" href="style.css">
                </head>
                <body>
                <table class="schedule">
                """);
    }

    private static void weekHeader() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        out.println("  <tr>");
        out.print("    <th class=\"week-header\" colspan=\"30\">");
        out.print(week.getStartingDate().format(formatter));
        out.print(" (day ");
        out.print(week.getStartingDayNumber());
        out.print(") – ");
        out.print(week.getEndingDate().format(formatter));
        out.print(" (day ");
        out.print(week.getEndingDayNumber());
        out.println(")</th>");
        out.println("  </tr>");
    }

    private static void dayHeader() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        out.println("  <tr>");
        for (Day day : week.getDays()) {
            out.print("    <th class=\"day-header\" colspan=\"6\"><em>");
            out.print(day.getDate().format(formatter));
            out.print(" (day ");
            out.print(day.getDayNumber());
            out.println(")</em></th>");
        }
        out.println("  </tr>");
        out.print("""
                  <tr>
                    <th class="day-header" colspan="6">Monday</th>
                    <th class="day-header" colspan="6">Tuesday</th>
                    <th class="day-header" colspan="6">Wednesday</th>
                    <th class="day-header" colspan="6">Thursday</th>
                    <th class="day-header" colspan="6">Friday</th>
                  </tr>
                """);
    }

    private static void sectionHeader() {
        out.println("  <tr>");
        for (Day day : week.getDays()) {
            for (Section section : day.getSections()) {
                out.print("    <th class=\"section-header\" colspan=\"2\">");
                out.print(section.getName());
                out.println("</th>");
            }
        }
        out.println("  </tr>");
    }

    private static void entries() {
        int[] currentEntry = new int[week.getDays().size()];
        ArrayList<String> rows = new ArrayList<>();
        boolean running = true;
        LocalTime currentTime = LocalTime.of(7, 45);
        while (currentTime.isBefore(LocalTime.of(14, 45))) {
            out.print("  <tr>\n");
            for (Day day : week.getDays()) {
                for (ScheduleEntry entry : day.getEntries()) {
                    if (entry.getStart().equals(currentTime)) {
                        if (entry instanceof AllSchoolBlock allSchoolBlock) {
                            if (allSchoolBlock.getAssignment() instanceof Course course) {
                                out.print("    <td class=\"slot span " + course.getName() +
                                        "\" colspan=\"6\" rowspan=\"");
                                out.print(allSchoolBlock.getLength().toMinutes() / 15);
                                out.print("\">");
                                out.print("      <div class=\"time\">" + allSchoolBlock.getStart() + "</div>\n");
                                out.print("      <div class=\"name\">" + course.getName() + "</div>\n");
                                out.print("    </td>\n");
                            } else {
                                out.print("    <td class=\"slot span event\" colspan=\"6\" rowspan=\"");
                                out.print(allSchoolBlock.getLength().toMinutes() / 15);
                                out.print("\">");
                                if (allSchoolBlock.getLength().toMinutes() <= 15) {
                                    out.print(allSchoolBlock.getStart());
                                    out.print(" (");
                                    out.print(allSchoolBlock.getAssignment().getName());
                                    out.print(")");
                                } else {
                                    out.print("      <div class=\"time\">" + allSchoolBlock.getStart() + "</div>\n");
                                    out.print("      <div class=\"name\">" + allSchoolBlock.getAssignment().getName() + "</div>\n");
                                }
                                out.print("    </td>\n");
                            }

                        } else if (entry instanceof ClassBlock classBlock) {
                            for (Section section : day.getSections()) {
                                Assignment assignment = classBlock.getSectionCourses().get(section);
                                if (assignment instanceof Course) {
                                    out.print("    <td class=\"slot class ");
                                    out.print(assignment.getName());
                                    out.print("\" colspan=\"2\" rowspan=\"");
                                    out.print(classBlock.getLength().toMinutes() / 15);
                                    out.print("\">\n");

                                    out.print("      <div class=\"time\">" + classBlock.getStart() + "</div>\n");
                                    out.print("      <div class=\"name\">" + assignment.getName() + "</div>\n");
                                    out.print("    </td>\n");
                                } else if (assignment instanceof SplitCourse splitCourse) {
                                    String firstSplit = splitCourse.getHalfSectionCourses().get(new HalfSection("Intermediate")).getName();
                                    String secondSplit = splitCourse.getHalfSectionCourses().get(new HalfSection("Advanced")).getName();

                                    out.print("    <td class=\"slot class ");
                                    out.print(firstSplit);
                                    out.print("\" colspan=\"1\" rowspan=\"");
                                    out.print(classBlock.getLength().toMinutes() / 15);
                                    out.print("\">\n");

                                    out.print("      <div class=\"time\">" + classBlock.getStart() + "</div>\n");
                                    out.print("      <div class=\"name\">" + firstSplit + "</div>\n");
                                    out.print("    </td>\n");

                                    out.print("    <td class=\"slot class ");
                                    out.print(secondSplit);
                                    out.print("\" colspan=\"1\" rowspan=\"");
                                    out.print(classBlock.getLength().toMinutes() / 15);
                                    out.print("\">\n");

                                    out.print("      <div class=\"time\">" + classBlock.getStart() + "</div>\n");
                                    out.print("      <div class=\"name\">" + secondSplit + "</div>\n");
                                    out.print("    </td>\n");
                                } else {
                                    out.print("    <td class=\"slot class OPEN\" colspan=\"2\" rowspan=\"");
                                    out.print(classBlock.getLength().toMinutes() / 15);
                                    out.print("\">\n");

                                    out.print("      <div class=\"time\">" + classBlock.getStart() + "</div>\n");
                                    out.print("      <div class=\"name\"> OPEN </div>\n");
                                    out.print("    </td>\n");
                                }
                            }
                        }
                    }
                }
            }

            out.print("  </tr>\n");
            currentTime = currentTime.plusMinutes(15);
        }
    }

    private static void close() {
        out.print("</table>");
        out.print("</body>");
    }
}
