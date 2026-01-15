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
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.sql.Time;
import java.util.List;

public class WeekEdit extends JFrame {
    private static Schedule schedule;
    private Week week;
    private static File file = new File("schedule.json");

    private JPanel contentPane;
    private JLabel titleLabel;
    private JComboBox<LocalDate> daySelector;
    private JButton saveButton;
    private JButton cancelButton;

    private JPanel dynamicPanel;
    private JButton openHTML;
    private JButton generate;
    private JComboBox<String> template;
    private JButton insertTemplateButton;
    private JComboBox<String> sectionSelect;
    private JButton editClasses;

    private static List<Assignment> classes;

    private java.util.List<BlockRow> currentRows = new java.util.ArrayList<>();

    private final Map<String, List<Section>> sectionGroups = new LinkedHashMap<>();

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

        for (String templateName : TemplateManager.listTemplateNames()) {
            template.addItem(templateName);
        }

        populateSections();
        generateDay();

        titleLabel.setText("Week of " + week.getStartingDate());

        daySelector.addActionListener(e -> generateDay());
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> onCancel());
        openHTML.addActionListener(e -> onOpenHTML());
        generate.addActionListener(e -> generate());
        insertTemplateButton.addActionListener(e -> insertTemplate());
        sectionSelect.addActionListener(e -> changeSection());
        editClasses.addActionListener(e -> editClasses());
    }

    private void editClasses() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        Day day = week.getDay(date);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Select classes for " + date + ":");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));

        // Pre-select boxes based on what's already on the day
        java.util.List<Assignment> existing = day.getClasses(); // <-- assumes you have this
        java.util.Set<String> existingNames = new java.util.HashSet<>();
        if (existing != null) {
            for (Assignment a : existing) existingNames.add(a.getName());
        }

        java.util.List<JCheckBox> boxes = new ArrayList<>();
        for (Assignment a : classes) {
            JCheckBox cb = new JCheckBox(a.getName());
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            cb.setSelected(existingNames.contains(a.getName()));
            boxes.add(cb);
            panel.add(cb);
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Edit Classes",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        // --- Gather selections ---
        java.util.List<Assignment> selected = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) {
            if (boxes.get(i).isSelected()) {
                selected.add(classes.get(i)); // same index as checkbox list
            }
        }

        // Save onto the Day model
        day.setClasses(selected); // <-- assumes you have this setter
    }

    private void populateSections() {
        List<Section> RGB = new ArrayList<>(List.of(
                new Section("R"),
                new Section("G"),
                new Section("B")
        ));

        List<Section> XYZ = new ArrayList<>(List.of(
                new Section("X"),
                new Section("Y"),
                new Section("Z")
        ));

        sectionGroups.put("RGB", RGB);
        sectionGroups.put("XYZ", XYZ);

        sectionSelect.removeAllItems();
        for (String key : sectionGroups.keySet()) {
            sectionSelect.addItem(key);
        }
    }

    private void changeSection() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        String selectedGroup = (String) sectionSelect.getSelectedItem();
        if (selectedGroup == null) return;

        Day day = week.getDay(date);
        if (day == null) return;

        List<Section> chosen = sectionGroups.get(selectedGroup);
        if (chosen == null) return;

        // Use a copy so you don't accidentally share/mutate the list stored in the map
        day.setSections(new ArrayList<>(chosen));

        // Refresh UI to reflect new sections (headers, column structure, etc.)
        generateDay();
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

    private void insertTemplate() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        Day day = week.getDay(date);
        day.setSections(sectionGroups.get("RGB"));
        day.setClasses(new ArrayList<>(classes));
        day.loadRequests();
        String templateName = (String) template.getSelectedItem();
        if (templateName == null) return;

        day.setTemplate(templateName);
        day.setEntries(ScheduleBuilder.getScheduleEntries(templateName, day.getRequests(), day.getClasses()));
        generateDay(day);
    }

    private void generateDay(Day day) {
        dynamicPanel.removeAll();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
        dynamicPanel.setBackground(new Color(245, 246, 248));
        dynamicPanel.setOpaque(true);
        currentRows.clear();

        template.setSelectedItem(day.getTemplate());

        if (day.getEntries() == null) {
            day.setEntries(new ArrayList<>(List.of(new AllSchoolBlock(LocalTime.of(7, 45)))));
        }
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
            wrapper.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            wrapper.setBackground(Color.WHITE);
            wrapper.setOpaque(true);

            row.setOpaque(false); // let wrapper show the white card background

            wrapper.add(row);
            wrapper.add(Box.createVerticalStrut(6));
            // Ensure row does not stretch vertically
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
            dynamicPanel.add(wrapper);
        }

        // Push everything to the top; extra space stays at the bottom.
        dynamicPanel.add(Box.createVerticalGlue());
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
        schedule.addWeek(week);
        schedule.saveToFile(file);
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

    private void insertAfter(BlockRow row) {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        int idx = currentRows.indexOf(row);
        if (idx < 0) return;

        Day updatedDay = getUpdatedDay(date);
        List<ScheduleEntry> entries = new ArrayList<>(updatedDay.getEntries());
        if (idx >= entries.size()) return;

        LocalTime currentStart = entries.get(idx).getStart();
        LocalTime insertStart = currentStart.plusMinutes(15);

        // Default new entry type matches the row you clicked (+) on
        String type = (String) row.entryType.getSelectedItem();
        ScheduleEntry inserted = "All School".equals(type)
                ? new AllSchoolBlock(insertStart)
                : new ClassBlock(insertStart);

        // Decide whether we need to shift future blocks.
        // We only shift if the next block starts BEFORE insertStart + 15 (no 15-min slot available).
        boolean needShift = false;
        int nextIndex = idx + 1;

        if (nextIndex < entries.size()) {
            LocalTime nextStart = entries.get(nextIndex).getStart();
            LocalTime requiredGapEnd = insertStart.plusMinutes(15);
            needShift = nextStart.isBefore(requiredGapEnd);
        }

        if (needShift) {
            for (int j = nextIndex; j < entries.size(); j++) {
                entries.set(j, shiftedCopy(entries.get(j), +15));
            }
        }

        entries.add(idx + 1, inserted);

        Day newDay = updatedDay.copy();
        newDay.setEntries(entries);
        newDay.updateDurations();
        generateDay(newDay);
    }

    private void deleteRow(BlockRow row) {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        int idx = currentRows.indexOf(row);
        if (idx < 0) return;

        Day updatedDay = getUpdatedDay(date);
        List<ScheduleEntry> entries = new ArrayList<>(updatedDay.getEntries());

        // Don't allow deleting the last remaining block (optional safeguard)
        if (entries.size() <= 1) return;

        if (idx >= entries.size()) return;

        // Remove the entry at idx
        entries.remove(idx);

        // Shift all later entries backward by 15 minutes
        for (int j = idx; j < entries.size(); j++) {
            entries.set(j, shiftedCopy(entries.get(j), -15));
        }

        Day newDay = updatedDay.copy();
        newDay.setEntries(entries);
        newDay.updateDurations();
        generateDay(newDay);
    }

    /**
     * Returns a deep-ish copy of the ScheduleEntry with its start time shifted by minutesDelta,
     * preserving section maps / assignment / reason.
     */
    private static ScheduleEntry shiftedCopy(ScheduleEntry entry, int minutesDelta) {
        LocalTime newStart = entry.getStart().plusMinutes(minutesDelta);

        if (entry instanceof ClassBlock cb) {
            ClassBlock out = new ClassBlock(newStart);

            if (cb.getSectionCourses() != null) {
                // copy the map so edits don't alias the old object
                out.setSectionCourses(new java.util.HashMap<>(cb.getSectionCourses()));
            } else {
                out.setSectionCourses(new java.util.HashMap<>());
            }
            return out;
        }

        if (entry instanceof AllSchoolBlock ab) {
            AllSchoolBlock out = new AllSchoolBlock(newStart);
            out.setAssignment(ab.getAssignment());
            out.setReason(ab.getReason());
            return out;
        }

        // If you add more ScheduleEntry subtypes later, handle them here.
        throw new IllegalStateException("Unsupported ScheduleEntry type for shift: " + entry.getClass());
    }

    public static void main(String[] args) {
        Week week = new Week();
        if (true) {
            schedule = new Schedule();
            week = Tester.testTemplate();
            schedule.addWeek(week);
            schedule.saveToFile(file);

        } else {
            schedule = Schedule.loadFromFile(file);
            week = schedule.getWeek(LocalDate.of(2025, 12, 1));
        }

        HtmlOutput.output(week);
        Week finalWeek = week;
        SwingUtilities.invokeLater(() -> new WeekEdit(finalWeek).setVisible(true));
    }

    private class BlockRow extends JPanel {
        JSpinner timeSpinner;
        JButton insertButton;
        JButton deleteButton;
        JComboBox<String> entryType;
        SectionClassPanel sectionPanel;
        AllSchoolPanel allSchoolPanel;
        ScheduleEntry entry;

        private final int blockIndex;
        private final List<Section> sections;

        BlockRow(int blockIndex, LocalTime startTime, List<Section> sections, ScheduleEntry preselected) {
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
            deleteButton = new JButton("-");

            insertButton.addActionListener(e -> WeekEdit.this.insertAfter(this));
            deleteButton.addActionListener(e -> WeekEdit.this.deleteRow(this));

            styleMiniButton(insertButton);
            styleMiniButton(deleteButton);

            top.add(entryType);
            top.add(timeSpinner);
            top.add(insertButton);
            top.add(deleteButton);
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
                // Ensure swapped panels are aligned left for BoxLayout
                if (sectionPanel != null) sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                if (allSchoolPanel != null) allSchoolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                revalidate();
                repaint();
            });
            // Keep rows from stretching vertically when the window grows.
            // Do this AFTER children are added so preferred height is correct.
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        }

        private static void styleMiniButton(JButton b) {
            b.setFocusPainted(false);
            b.setMargin(new Insets(2, 10, 2, 10));
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
                    String reason = allSchoolPanel.reason.getText();
                    ab.setReason(reason);
                    if ("(Open)".equals(name) || name == null) {
                        ab.setAssignment(null);
                    } else {
                        Assignment selectedAssignment = classes.stream()
                                .filter(a -> a.getName().equals(name))
                                .findFirst()
                                .orElse(new Event(name));
                        ab.setAssignment(selectedAssignment);
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
                combos[i].addItem("(Open)");

                for (Assignment assignment : classes) {
                    combos[i].addItem(assignment.getName());
                }

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
        private final JTextField reason;
        private static final String[] DEFAULT_OPTIONS = {
                "(Open)", "Flex", "Lunch", "Class Meeting", "15 min break", "Homeroom", "PE"
        };

        AllSchoolPanel(AllSchoolBlock preselected) {
            super(new FlowLayout(FlowLayout.LEFT));

            combo = new JComboBox<>();

            // Populate choices
            for (Assignment a : classes) {
                combo.addItem(a.getName());
            }
            for (String option : DEFAULT_OPTIONS) {
                combo.addItem(option);
            }

            // Preselect existing value (or Open)
            if (preselected != null && preselected.getAssignment() != null) {
                combo.setSelectedItem(preselected.getAssignment().getName());
            } else {
                combo.setSelectedItem("(Open)");
            }

            add(combo);


            reason = new JTextField();
            reason.setColumns(20);
            reason.setPreferredSize(new Dimension(250, 28));
            reason.setToolTipText("Reason (optional)");
            if (preselected != null && preselected.getReason() != null) {
                reason.setText(preselected.getReason());
            }
            add(reason);

        }

        public JComboBox<String> getCombo() {
            return combo;
        }
    }

}