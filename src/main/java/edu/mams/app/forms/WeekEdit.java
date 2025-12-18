package edu.mams.app.forms;

import edu.mams.app.model.people.Section;
import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.schedule.*;
import edu.mams.app.model.schedule.Event;
import edu.mams.app.model.util.HtmlOutput;
import edu.mams.app.model.util.ScheduleBuilder;
import edu.mams.app.model.util.Tester;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.sql.Time;

public class WeekEdit extends JFrame {
    private Week week;

    private JPanel contentPane;
    private JLabel titleLabel;
    private JComboBox<LocalDate> daySelector;
    private JButton saveButton;
    private JButton cancelButton;

    private JPanel dynamicPanel;
    private JButton openHTML;
    private JButton generate;

    private static List<Assignment> classes;

    private java.util.List<BlockRow> currentRows = new java.util.ArrayList<>();

    public WeekEdit(Week week) {
        classes = new ArrayList<>();
        classes.add(new Course("Math", new Teacher("Durost")));
        classes.add(new Course("Physics", new Teacher("Chase")));
        classes.add(new Course("CS", new Teacher("Taricco")));
        classes.add(new Course("STEM", new Teacher("Crowthers")));
        classes.add(new Course("Hum", new Teacher("Small")));
        classes.add(new Course("Lang", new Teacher("Wildfong")));

        this.week = week;
        setContentPane(contentPane);
        setTitle("Week Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(800, 1200);
        setLocationRelativeTo(null);

        loadWeekIntoForm();

        generateDay();

        titleLabel.setText("Week of " + week.getStartingDate());
        saveButton.setText("Save");
        cancelButton.setText("Cancel");
        openHTML.setText("Open HTML");
        generate.setText("Generate");

        daySelector.addActionListener(e -> generateDay());
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> onCancel());
        openHTML.addActionListener(e -> onOpenHTML());
        generate.addActionListener(e -> generate());
    }

    private void loadWeekIntoForm() {
        daySelector.removeAllItems();
        for (int i = 0; i < week.getDays().size(); i++) {
            daySelector.addItem(week.getDays().get(i).getDate());
        }
    }

    private void generateDay() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        Day day = week.getDay(date);
        generateDay(day);
    }

    private void generateDay(Day day) {
        dynamicPanel.removeAll();
        dynamicPanel.setLayout(new GridLayout(0, 1));
        currentRows.clear();

        int blocks = day.getEntries().size();

        for (int i = 0; i < blocks; i++) {
            LocalTime startTime = day.getEntry(i).getStart();
            BlockRow row = new BlockRow(
                    i,
                    startTime,
                    day.getSections(),
                    day.getEntry(i)
            );
            currentRows.add(row);

            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.add(row);
            wrapper.add(Box.createVerticalStrut(5));
            wrapper.add(new JSeparator());
            dynamicPanel.add(wrapper);
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    private void onSave() {
        System.out.println("Saving for day: " + daySelector.getSelectedItem());

        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        Day updatedDay = getUpdatedDay(date);

        // replace the Day inside the Week (without mutating the old Day instance)
        for (int i = 0; i < week.getDays().size(); i++) {
            if (week.getDays().get(i).getDate().equals(date)) {
                week.getDays().set(i, updatedDay);
                break;
            }
        }

        HtmlOutput.output(week);
    }

    private Day getUpdatedDay(LocalDate date) {
        Day baseDay = week.getDay(date);

        Day updatedDay = baseDay;
        for (BlockRow row : currentRows) {
            updatedDay = row.applyToModel(updatedDay);
        }
        updatedDay = updatedDay.withUpdatedDurations();
        return updatedDay;
    }

    private void onCancel() {
        dispose();
    }

    private void onOpenHTML() {
        try {
            File file = new File("output_java.html");
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void generate() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();

        // 1) Build "what the user currently has in the UI" as a Day (still not saved to week)
        Day updatedDay = getUpdatedDay(date);

        // 2) Generate a preview Day WITHOUT mutating week
        Day previewDay = updatedDay.copy();
        previewDay.setEntries(ScheduleBuilder.buildAroundSchedule(previewDay));
        previewDay.updateDurations();

        // 3) Show previewDay in the UI (still not saved anywhere)
        generateDay(previewDay);
    }

    public static void main(String[] args) {
        Week week = Tester.testTemplate();
        HtmlOutput.output(week);
        SwingUtilities.invokeLater(() -> new WeekEdit(week).setVisible(true));
    }

    private static class BlockRow extends JPanel {
        JComboBox classSelect;
        JSpinner timeSpinner;
        JButton insertButton;
        JComboBox<String> entryType;
        SectionClassPanel sectionPanel;
        AllSchoolPanel allSchoolPanel;
        ScheduleEntry entry;

        private final int blockIndex;
        private final List<Section> sections;

        BlockRow(int blockIndex,
                 LocalTime startTime,
                 List<Section> sections,
                 ScheduleEntry preselected) {

            this.sections = sections;
            this.entry = preselected;
            this.blockIndex = blockIndex;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            entryType = new JComboBox<>(new String[]{"All School", "Sections"});
            if (preselected instanceof AllSchoolBlock) {
                entryType.setSelectedItem("All School");
            } else {
                entryType.setSelectedItem("Sections");
            }

            // Convert LocalTime -> java.util.Date (via java.sql.Time)
            Date initialTime = Time.valueOf(startTime);

            // 15-minute step model
            SpinnerDateModel model = new SpinnerDateModel(initialTime, null, null, Calendar.MINUTE) {
                @Override
                public Object getNextValue() {
                    Date value = (Date) getValue();
                    Calendar c = Calendar.getInstance();
                    c.setTime(value);
                    c.add(Calendar.MINUTE, 15);
                    return c.getTime();
                }

                @Override
                public Object getPreviousValue() {
                    Date value = (Date) getValue();
                    Calendar c = Calendar.getInstance();
                    c.setTime(value);
                    c.add(Calendar.MINUTE, -15);
                    return c.getTime();
                }
            };

            timeSpinner = new JSpinner(model);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
            timeSpinner.setEditor(editor);

            insertButton = new JButton("+");

            top.add(entryType);
            top.add(timeSpinner);
            top.add(insertButton);
            add(top);

            // ----- Initial panel based on preselected type -----
            if (preselected instanceof ClassBlock classBlock) {
                sectionPanel = new SectionClassPanel(sections, classBlock);
                add(sectionPanel);
            } else if (preselected instanceof AllSchoolBlock allSchoolBlock) {
                allSchoolPanel = new AllSchoolPanel(allSchoolBlock);
                add(allSchoolPanel);
            }

            // ----- Swap panels when entryType changes -----
            entryType.addActionListener(e -> {
                String selected = (String) entryType.getSelectedItem();

                // Remove whichever detail panel is currently showing
                if (sectionPanel != null) {
                    remove(sectionPanel);
                }
                if (allSchoolPanel != null) {
                    remove(allSchoolPanel);
                }

                if ("Sections".equals(selected)) {
                    // Lazily create if needed; preselected may or may not be a ClassBlock
                    if (sectionPanel == null) {
                        ClassBlock cb = (preselected instanceof ClassBlock)
                                ? (ClassBlock) preselected
                                : null;
                        sectionPanel = new SectionClassPanel(sections, cb);
                    }
                    add(sectionPanel);
                } else { // "All School"
                    if (allSchoolPanel == null) {
                        AllSchoolBlock ab = (preselected instanceof AllSchoolBlock)
                                ? (AllSchoolBlock) preselected
                                : null;
                        allSchoolPanel = new AllSchoolPanel(ab);
                    }
                    add(allSchoolPanel);
                }

                revalidate();
                repaint();
            });
        }

        Day applyToModel(Day baseDay) {
            Date d = (Date) timeSpinner.getValue();
            LocalTime newStart = d.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime()
                    .withSecond(0)
                    .withNano(0);

            String type = (String) entryType.getSelectedItem();

            ScheduleEntry newEntry;
            if ("Sections".equals(type)) {
                ClassBlock cb = new ClassBlock(newStart);

                // ensure we have a map to write into
                if (cb.getSectionCourses() == null) {
                    cb.setSectionCourses(new java.util.HashMap<>());
                }

                if (sectionPanel != null) {
                    for (int i = 0; i < sections.size(); i++) {
                        String selectedClassName = sectionPanel.getClassForSection(i);
                        Section section = sections.get(i);

                        if ("(Open)".equals(selectedClassName)) {
                            cb.setSectionCourse(section, null);
                        } else {
                            Assignment selectedAssignment = classes.stream()
                                    .filter(a -> a.getName().equals(selectedClassName))
                                    .findFirst()
                                    .orElse(null);
                            cb.setSectionCourse(section, selectedAssignment);
                        }
                    }
                }

                newEntry = cb;
            } else if ("All School".equals(type)) {
                AllSchoolBlock ab = new AllSchoolBlock(newStart);

                if (allSchoolPanel != null) {
                    String name = (String) allSchoolPanel.getCombo().getSelectedItem();
                    if ("(Open)".equals(name) || name == null) {
                        ab.setAssignment(null);
                    } else {
                        Assignment selectedAssignment = classes.stream()
                                .filter(a -> a.getName().equals(name))
                                .findFirst()
                                .orElse(new Event(name));
                        ab.setAssignment(selectedAssignment); // can be null safely
                    }
                }

                newEntry = ab;
            } else {
                throw new IllegalStateException("Unknown entry type: " + type);
            }

            // Return a new Day instance rather than mutating the existing one
            return baseDay.withUpdatedEntry(blockIndex, newEntry);
        }
    }

    private static class SectionClassPanel extends JPanel {
        JComboBox<String>[] combos;

        @SuppressWarnings("unchecked")
        SectionClassPanel(List<Section> sections,
                          ClassBlock preselected) {
            setLayout(new FlowLayout(FlowLayout.LEFT));

            combos = new JComboBox[sections.size()];

            for (int i = 0; i < sections.size(); i++) {
                combos[i] = new JComboBox<>();
                for (Assignment assignment : classes) {
                    combos[i].addItem(assignment.getName());
                }
                combos[i].addItem("(Open)");

                // Only try to preselect if we actually have a preselected ClassBlock
                if (preselected != null &&
                        preselected.getSectionCourses() != null &&
                        preselected.getSectionCourses().get(sections.get(i)) != null) {

                    String name = preselected
                            .getSectionCourses()
                            .get(sections.get(i))
                            .getName();
                    combos[i].setSelectedItem(name);
                }

                add(new JLabel(sections.get(i).getName()));
                add(combos[i]);
            }
        }

        String getClassForSection(int sectionIndex) {
            return (String) combos[sectionIndex].getSelectedItem();
        }

        void setClassForSection(int sectionIndex, String className) {
            combos[sectionIndex].setSelectedItem(className);
        }
    }

    private static class AllSchoolPanel extends JPanel {
        private final JComboBox<String> combo;

        AllSchoolPanel(AllSchoolBlock preselected) {
            super(new FlowLayout(FlowLayout.LEFT));

            combo = new JComboBox<>();

            // Populate choices
            for (Assignment a : classes) {
                combo.addItem(a.getName());
            }
            combo.addItem("(Open)");
            combo.addItem("Flex");
            combo.addItem("Lunch");
            combo.addItem("Class Meeting");
            combo.addItem("15 min break");


            // Preselect existing value (or Open)
            if (preselected != null && preselected.getAssignment() != null) {
                combo.setSelectedItem(preselected.getAssignment().getName());
            } else {
                combo.setSelectedItem("(Open)");
            }

            add(combo);
        }

        public JComboBox<String> getCombo() {
            return combo;
        }
    }

}