package edu.mams.app.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import edu.mams.app.model.people.*;
import edu.mams.app.model.schedule.*;
import edu.mams.app.model.schedule.Event;
import edu.mams.app.model.util.*;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.sql.Time;
import java.util.List;

public class WeekEdit extends JFrame {
    private static Schedule schedule;
    private final Week week;
    private static final File file = new File("schedule.json");

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
    private JCheckBox splitButton;
    private JComboBox<String> splitClassSelector;
    private JButton generateTemplateButton;
    private JButton quickGenerate;

    private static List<Assignment> classes = new ArrayList<>();

    private List<BlockRow> currentRows = new ArrayList<>();

    private final Map<String, List<Section>> sectionGroups = new LinkedHashMap<>();
    private static List<HalfSection> halfSections = new ArrayList<>();

    public WeekEdit(Week week, Schedule schedule) {
        WeekEdit.schedule = schedule;
        classes.add(new Course("Math", new Teacher("Durost")));
        classes.add(new Course("Physics", new Teacher("Chase")));
        classes.add(new Course("CS", new Teacher("Taricco")));
        classes.add(new Course("STEM", new Teacher("Crowthers")));
        classes.add(new Course("Hum", new Teacher("Small")));
        classes.add(new Course("Lang", new Teacher("Wildfong")));

        ScheduleBuilder.setSplitClass((Course) classes.get(5));

        halfSections = ScheduleBuilder.getHalfSections();

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
        generateTemplateButton.addActionListener(e -> generateTemplate());
        sectionSelect.addActionListener(e -> changeSection());
        editClasses.addActionListener(e -> editClasses());
        quickGenerate.addActionListener(e -> openQuickGenerateDialog());
        splitButton.addActionListener(e -> changeSplit());
        splitClassSelector.addActionListener(e -> selectPartnerSplit());
    }

    private void selectPartnerSplit() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        String selectedClass = (String) splitClassSelector.getSelectedItem();
        Assignment selectedAssignment = classes.stream()
                .filter(a -> a.getName().equals(selectedClass))
                .findFirst()
                .orElse(null);

        week.getDay(date).setSplitCourse((Course) selectedAssignment);
    }

    private void changeSplit() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        week.getDay(date).setSplit(splitButton.isSelected());
    }

    private void generateTemplate() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        Day day = week.getDay(date);
        if (day.getSections() == null) day.setSections(sectionGroups.get("RGB"));
        if (day.getClasses() == null || day.getClasses().isEmpty()) day.setClasses(new ArrayList<>(classes));
        day.loadRequests();
        String templateName = (String) template.getSelectedItem();
        if (templateName == null) return;

        day.setTemplate(templateName);
        day.generateBlocks();
        updateModelFromUI();
        generateDay(day);
    }

    private void updateModelFromUI() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        Day updatedDay = getUpdatedDay(date);

        for (int i = 0; i < week.getDays().size(); i++) {
            if (week.getDays().get(i).getDate().equals(date)) {
                week.getDays().set(i, updatedDay);
                break;
            }
        }
    }

    private void editClasses() {
        LocalDate date = (LocalDate) daySelector.getSelectedItem();
        if (date == null) return;

        Day day = week.getDay(date);
        if (day == null) return;

        List<Assignment> picked = pickClassesForDate(date, day.getClasses());
        if (picked == null) return; // should not happen, but keep safe

        // Save onto the Day model
        day.setClasses(picked);
        updateModelFromUI();
        generateDay();
    }

    /**
     * Shows a checkbox picker for classes for a specific date.
     * @param date date being edited (for dialog title)
     * @param existing existing class list for that day (may be null)
     * @return selected classes (never null). If user cancels, returns the existing list (or empty list if existing was null).
     */
    private List<Assignment> pickClassesForDate(LocalDate date, List<Assignment> existing) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Select classes for " + date + ":");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(8));

        // Pre-select boxes based on what's already on the day
        Set<String> existingNames = new HashSet<>();
        if (existing != null) {
            for (Assignment a : existing) existingNames.add(a.getName());
        }

        List<JCheckBox> boxes = new ArrayList<>();
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
                "Edit Classes – " + date,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        }

        List<Assignment> selected = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) {
            if (boxes.get(i).isSelected()) {
                selected.add(classes.get(i));
            }
        }
        return selected;
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

        ScheduleBuilder.setSplitSection(RGB.get(1));

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

        updateModelFromUI();
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
        updateModelFromUI();
        generateDay(day);
    }

    private void generateDay(Day day) {
        splitClassSelector.removeAllItems();
        for (Assignment assignment : classes) {
            if (!((Course) assignment).equals(ScheduleBuilder.getSplitClass()))
                splitClassSelector.addItem(assignment.getName());
        }

        if (day.getSplitCourse() != null) {
            String name = day.getSplitCourse().getName();
            splitClassSelector.setSelectedItem(name);
        }

        dynamicPanel.removeAll();
        dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
        dynamicPanel.setBackground(new Color(245, 246, 248));
        dynamicPanel.setOpaque(true);
        currentRows.clear();

        template.setSelectedItem(day.getTemplate());

        // Ensure we always have sections; prevents empty SectionClassPanel / AIOOBE during updates
        if (day.getSections() == null || day.getSections().isEmpty()) {
            List<Section> fallback = sectionGroups.get("RGB");
            day.setSections(fallback == null ? new ArrayList<>() : new ArrayList<>(fallback));
        }

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

            JPanel wrapper = new CardPanel();
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
//            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
            dynamicPanel.add(wrapper);
        }

        // Push everything to the top; extra space stays at the bottom.
        dynamicPanel.add(Box.createVerticalGlue());
        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    private void onSave() {
        updateModelFromUI();
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
        SwingUtilities.invokeLater(() -> {
            new WeekSelector(schedule).setVisible(true);
        });
        dispose();
    }

    private void onOpenHTML() {
        HtmlOutput.output(week);
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

        // Update the main model before re-generating
        for (int i = 0; i < week.getDays().size(); i++) {
            if (week.getDays().get(i).getDate().equals(date)) {
                week.getDays().set(i, newDay);
                break;
            }
        }
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

        // Update the main model before re-generating
        for (int i = 0; i < week.getDays().size(); i++) {
            if (week.getDays().get(i).getDate().equals(date)) {
                week.getDays().set(i, newDay);
                break;
            }
        }
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
                out.setSectionCourses(new HashMap<>(cb.getSectionCourses()));
            } else {
                out.setSectionCourses(new HashMap<>());
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        titleLabel = new JLabel();
        titleLabel.setText("Label");
        contentPane.add(titleLabel, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setText("Save");
        contentPane.add(saveButton, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openHTML = new JButton();
        openHTML.setText("Open HTML");
        contentPane.add(openHTML, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sectionSelect = new JComboBox();
        sectionSelect.setToolTipText("Section");
        contentPane.add(sectionSelect, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editClasses = new JButton();
        editClasses.setText("Edit Classes");
        contentPane.add(editClasses, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        dynamicPanel = new JPanel();
        dynamicPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane1.setViewportView(dynamicPanel);
        dynamicPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        splitButton = new JCheckBox();
        splitButton.setText("Split");
        contentPane.add(splitButton, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        splitClassSelector = new JComboBox();
        splitClassSelector.setToolTipText("Select Split Class");
        contentPane.add(splitClassSelector, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        insertTemplateButton = new JButton();
        insertTemplateButton.setText("Insert Blank Template");
        contentPane.add(insertTemplateButton, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateTemplateButton = new JButton();
        generateTemplateButton.setText("Generate Template");
        contentPane.add(generateTemplateButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        template = new JComboBox();
        contentPane.add(template, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        quickGenerate = new JButton();
        quickGenerate.setText("Quick Generate");
        contentPane.add(quickGenerate, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        daySelector = new JComboBox();
        contentPane.add(daySelector, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generate = new JButton();
        generate.setText("Generate");
        contentPane.add(generate, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class CardPanel extends JPanel {
        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
    }

    private class BlockRow extends JPanel {
        JSpinner timeSpinner;
        JButton insertButton;
        JButton deleteButton;
        JComboBox<String> entryType;
        SectionClassPanel sectionPanel;
        AllSchoolPanel allSchoolPanel;
        SplitSectionPanel splitSectionPanel;
        ScheduleEntry entry;

        private final int blockIndex;
        private final List<Section> sections;

        BlockRow(int blockIndex, LocalTime startTime, List<Section> sections, ScheduleEntry preselected) {
            this.sections = sections;
            this.entry = preselected;
            this.blockIndex = blockIndex;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.setMaximumSize(new Dimension(Integer.MAX_VALUE, top.getPreferredSize().height));
            entryType = new JComboBox<>(new String[]{"All School", "Sections", "Split Section"});
            if (preselected instanceof AllSchoolBlock) {
                entryType.setSelectedItem("All School");
            } else if (preselected instanceof ClassBlock cb) {
                if (cb.isSplit()) {
                    entryType.setSelectedItem("Split Section");
                } else {
                    entryType.setSelectedItem("Sections");
                }
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

            timeSpinner.addChangeListener(e -> updateModelFromUI());

            // ----- Initial panel based on preselected type -----
            if (preselected instanceof ClassBlock classBlock) {
                classBlock.setSplit();
                if (classBlock.isSplit()) {
                    splitSectionPanel = new SplitSectionPanel(sections, classBlock);
                    add(splitSectionPanel);
                } else {
                    sectionPanel = new SectionClassPanel(sections, classBlock);
                    add(sectionPanel);
                }

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
                if (splitSectionPanel != null) {
                    remove(splitSectionPanel);
                }
                if (allSchoolPanel != null) {
                    remove(allSchoolPanel);
                }

                if ("Sections".equals(selected)) {
                    if (sectionPanel == null) {
                        sectionPanel = new SectionClassPanel(sections, (preselected instanceof ClassBlock) ? (ClassBlock) preselected : null);
                    }
                    add(sectionPanel);
                } else if ("Split Section".equals(selected)) {
                    if (splitSectionPanel == null) {
                        splitSectionPanel = new SplitSectionPanel(sections, (preselected instanceof ClassBlock) ? (ClassBlock) preselected : null);
                    }
                    add(splitSectionPanel);
                } else { // "All School"
                    if (allSchoolPanel == null) {
                        AllSchoolBlock ab = (preselected instanceof AllSchoolBlock) ? (AllSchoolBlock) preselected : null;
                        allSchoolPanel = new AllSchoolPanel(ab);
                    }
                    add(allSchoolPanel);
                }

                // Ensure consistency
                if (sectionPanel != null) sectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                if (splitSectionPanel != null) splitSectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                if (allSchoolPanel != null) allSchoolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

                updateModelFromUI();
                Container card = getParent();          // wrapper
                if (card != null) {
                    card.revalidate();
                    card.repaint();
                }
                WeekEdit.this.dynamicPanel.revalidate();
                WeekEdit.this.dynamicPanel.repaint();
            });

            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }

        private static void styleMiniButton(JButton b) {
            b.setFocusPainted(false);
            b.setMargin(new Insets(2, 10, 2, 10));
        }

        private Day applyToModel(Day baseDay) {
            Date d = (Date) timeSpinner.getValue();
            LocalTime newStart = d.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .withSecond(0)
                    .withNano(0);

            String type = (String) entryType.getSelectedItem();

            ScheduleEntry newEntry;
            if ("Sections".equals(type)) {
                ClassBlock cb = new ClassBlock(newStart);

                // ensure we have a map to write into
                if (cb.getSectionCourses() == null) {
                    cb.setSectionCourses(new HashMap<>());
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
                    Object selectedItem = allSchoolPanel.getCombo().getSelectedItem();
                    String name = selectedItem != null ? selectedItem.toString() : "(Open)";
                    String reason = allSchoolPanel.reason.getText();
                    ab.setReason(reason);
                    if ("(Open)".equals(name) || name.trim().isEmpty()) {
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
            } else if ("Split Section".equals(type)) {
                ClassBlock cb = new ClassBlock(newStart);
                // Mark as split (your ClassBlock appears to track this via isSplit())
                cb.setSplit();

                if (cb.getSectionCourses() == null) {
                    cb.setSectionCourses(new HashMap<>());
                }

                // If split UI isn't available or we can't actually split, fall back to treating this as normal Sections
                if (splitSectionPanel == null || sections == null || sections.isEmpty() || halfSections == null || halfSections.size() < 2) {
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
                } else {
                    int splitIndex = 1; // split the middle section (G/Y) by default

                    for (int i = 0; i < sections.size(); i++) {
                        Section section = sections.get(i);

                        if (i == splitIndex) {
                            String firstSelect = splitSectionPanel.getSplitA();
                            String secondSelect = splitSectionPanel.getSplitB();

                            Assignment firstCourse = (firstSelect == null || "(Open)".equals(firstSelect))
                                    ? null
                                    : classes.stream().filter(a -> a.getName().equals(firstSelect)).findFirst().orElse(null);

                            Assignment secondCourse = (secondSelect == null || "(Open)".equals(secondSelect))
                                    ? null
                                    : classes.stream().filter(a -> a.getName().equals(secondSelect)).findFirst().orElse(null);

                            Map<HalfSection, Assignment> splitMap = new HashMap<>();
                            splitMap.put(halfSections.get(0), firstCourse);
                            splitMap.put(halfSections.get(1), secondCourse);

                            SplitCourse sc = new SplitCourse(splitMap);
                            cb.setSectionCourse(section, sc);

                        } else {
                            String selectedClassName = splitSectionPanel.getClassForSection(i);

                            if (selectedClassName == null || "(Open)".equals(selectedClassName)) {
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
                }
            } else {
                throw new IllegalStateException("Unknown entry type: " + type);
            }

            // Return a new Day instance rather than mutating the existing one
            return baseDay.withUpdatedEntry(blockIndex, newEntry);
        }
    }

    private static class SplitSectionPanel extends JPanel {
        private JComboBox<String>[] sectionCombos;
        private JComboBox<String> splitA;
        private JComboBox<String> splitB;

        // Which section index is split into half-sections (default: middle section)
        private static final int SPLIT_INDEX = 1;

        @SuppressWarnings("unchecked")
        SplitSectionPanel(List<Section> sections, ClassBlock preselected) {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            sectionCombos = new JComboBox[sections == null ? 0 : sections.size()];

            for (int i = 0; i < sectionCombos.length; i++) {
                Section sec = sections.get(i);
                add(new JLabel(sec.getName()));

                // For the split section, show TWO combos (halfSections[0] and halfSections[1])
                if (i == SPLIT_INDEX && halfSections != null && halfSections.size() >= 2) {
                    splitA = buildCombo();
                    splitB = buildCombo();

                    // Preselect if the day already has a SplitCourse stored for this section
                    if (preselected != null &&
                            preselected.getSectionCourses() != null &&
                            preselected.getSectionCourses().get(sec) instanceof SplitCourse sc &&
                            sc.getHalfSectionCourses() != null) {

                        Assignment a = sc.getHalfSectionCourses().get(halfSections.get(0));
                        Assignment b = sc.getHalfSectionCourses().get(halfSections.get(1));
                        if (a != null) splitA.setSelectedItem(a.getName());
                        if (b != null) splitB.setSelectedItem(b.getName());
                    }

                    splitA.addActionListener(e -> triggerUpdate());
                    splitB.addActionListener(e -> triggerUpdate());

                    add(splitA);
                    add(splitB);
                    sectionCombos[i] = null; // unused for the split section
                } else {
                    JComboBox<String> combo = buildCombo();
                    sectionCombos[i] = combo;

                    // Preselect existing assignment if present (and not a SplitCourse)
                    if (preselected != null &&
                            preselected.getSectionCourses() != null &&
                            preselected.getSectionCourses().get(sec) != null &&
                            !(preselected.getSectionCourses().get(sec) instanceof SplitCourse)) {
                        combo.setSelectedItem(preselected.getSectionCourses().get(sec).getName());
                    }

                    combo.addActionListener(e -> triggerUpdate());
                    add(combo);
                }
            }

            // If sections are too small / split not possible, ensure split combos exist so callers don't NPE
            if (splitA == null) splitA = buildCombo();
            if (splitB == null) splitB = buildCombo();
        }

        private JComboBox<String> buildCombo() {
            JComboBox<String> cb = new JComboBox<>();
            cb.addItem("(Open)");
            for (Assignment assignment : classes) {
                cb.addItem(assignment.getName());
            }
            return cb;
        }

        private void triggerUpdate() {
            WeekEdit we = (WeekEdit) SwingUtilities.getAncestorOfClass(WeekEdit.class, this);
            if (we != null) {
                we.updateModelFromUI();
            }
        }

        String getClassForSection(int sectionIndex) {
            if (sectionCombos == null || sectionIndex < 0 || sectionIndex >= sectionCombos.length) return "(Open)";
            JComboBox<String> cb = sectionCombos[sectionIndex];
            if (cb == null) return "(Open)"; // split section
            Object v = cb.getSelectedItem();
            return v == null ? "(Open)" : v.toString();
        }

        String getSplitA() {
            Object v = splitA.getSelectedItem();
            return v == null ? "(Open)" : v.toString();
        }

        String getSplitB() {
            Object v = splitB.getSelectedItem();
            return v == null ? "(Open)" : v.toString();
        }
    }

    private static class SectionClassPanel extends JPanel {
        JComboBox<String>[] combos;

        @SuppressWarnings("unchecked")
        SectionClassPanel(List<Section> sections,
                          ClassBlock preselected) {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setAlignmentX(Component.LEFT_ALIGNMENT);
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

                combos[i].addActionListener(e -> {
                    // This finds the WeekEdit instance and triggers the update
                    SwingUtilities.getAncestorOfClass(WeekEdit.class, this);
                    Container parent = getParent();
                    while (parent != null && !(parent instanceof WeekEdit)) {
                        parent = parent.getParent();
                    }
                    if (parent != null) {
                        ((WeekEdit) parent).updateModelFromUI();
                    }
                });

                add(new JLabel(sections.get(i).getName()));
                add(combos[i]);
            }
        }

        String getClassForSection(int sectionIndex) {
            if (combos == null || sectionIndex < 0 || sectionIndex >= combos.length) return "(Open)";
            Object v = combos[sectionIndex].getSelectedItem();
            return v == null ? "(Open)" : v.toString();
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
            super(new FlowLayout(FlowLayout.LEFT)); // Reduced vertical gap
            setAlignmentX(Component.LEFT_ALIGNMENT);

            combo = new JComboBox<>();
            combo.setEditable(true);

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

            combo.addActionListener(e -> {
                // Update model when selection or custom text changes
                triggerUpdate();
            });

            // Also update when the editor loses focus (for custom typed text)
            Component editor = combo.getEditor().getEditorComponent();
            editor.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    triggerUpdate();
                }
            });

            add(combo);

            reason = new JTextField();
            reason.setColumns(15); // Slightly smaller to prevent wrapping
            reason.setToolTipText("Reason (optional)");
            if (preselected != null && preselected.getReason() != null) {
                reason.setText(preselected.getReason());
            }

            reason.addActionListener(e -> {
                triggerUpdate();
            });

            add(reason);
        }

        private void triggerUpdate() {
            Container parent = getParent();
            while (parent != null && !(parent instanceof WeekEdit)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                ((WeekEdit) parent).updateModelFromUI();
            }
        }

        public JComboBox<String> getCombo() {
            return combo;
        }
    }

    private void openQuickGenerateDialog() {
        Window owner = SwingUtilities.getWindowAncestor(contentPane);
        QuickGenerateDialog dialog = new QuickGenerateDialog(owner, week, getTemplateNames());
        dialog.setLocationRelativeTo(contentPane);
        dialog.setVisible(true);

        if (!dialog.wasGenerated()) return;

        // Apply selected template + per-day classes for each day
        Map<LocalDate, String> selections = dialog.getSelections();
        Map<LocalDate, List<Assignment>> classSelections = dialog.getClassSelections();

        Map<LocalDate, Boolean> splitSelections = dialog.getSplitSelections();
        Map<LocalDate, String> splitCourseSelections = dialog.getSplitCourseSelections();

        for (Map.Entry<LocalDate, String> entry : selections.entrySet()) {
            LocalDate date = entry.getKey();
            String templateName = entry.getValue();
            if (templateName == null || templateName.isBlank()) continue;

            Day day = week.getDay(date);
            if (day == null) continue;

            // Make sure day has required context for the builder
            day.setSections(sectionGroups.get("RGB"));

            List<Assignment> picked = classSelections.get(date);
            if (picked == null) {
                // fall back to whatever is already on the day, else the global list
                picked = (day.getClasses() != null) ? new ArrayList<>(day.getClasses()) : new ArrayList<>(classes);
            }
            day.setClasses(new ArrayList<>(picked));

            day.loadRequests();
            day.setTemplate(templateName);

            Boolean split = splitSelections.get(date);
            day.setSplit(split != null && split);

            String splitCourseName = splitCourseSelections.get(date);
            if (splitCourseName == null || splitCourseName.isBlank() || "(None)".equals(splitCourseName)) {
                day.setSplitCourse(null);
            } else {
                Assignment a = classes.stream()
                        .filter(x -> x.getName().equals(splitCourseName))
                        .findFirst()
                        .orElse(null);
                day.setSplitCourse(a instanceof Course ? (Course) a : null);
            }

            day.generateBlocks();

        }

        // Optional: refresh UI to whichever day is currently selected
        LocalDate current = (LocalDate) daySelector.getSelectedItem();
        if (current != null) generateDay(week.getDay(current));
    }

    /**
     * Return template names that should appear in the dropdowns.
     * Replace this with your real source (TemplateManager, files, etc.)
     */
    private List<String> getTemplateNames() {
        // Example: if you already have a JComboBox<String> template; use its model:
        List<String> names = new ArrayList<>();
        ComboBoxModel<String> model = template.getModel();
        for (int i = 0; i < model.getSize(); i++) names.add(model.getElementAt(i));
        return names;
    }


    /* ===================== Dialog implementation ===================== */

    private final class QuickGenerateDialog extends JDialog {
        private final Map<LocalDate, JComboBox<String>> comboByDate = new LinkedHashMap<>();
        private final Map<LocalDate, List<Assignment>> classesByDate = new LinkedHashMap<>();
        private boolean generated = false;
        private final Map<LocalDate, JCheckBox> splitByDate = new LinkedHashMap<>();
        private final Map<LocalDate, JComboBox<String>> splitCourseComboByDate = new LinkedHashMap<>();

        QuickGenerateDialog(Window owner, Week week, List<String> templateNames) {
            super(owner, "Quick Generate", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JLabel info = new JLabel("Choose a template for each day, then click Generate.");
            root.add(info, BorderLayout.NORTH);

            // Center: scrollable list of days + comboboxes
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

            // Get days from your Week model. Replace week.getDays() accordingly.
            // Assumption: you can iterate dates in the week.
            List<LocalDate> dates = getWeekDates(week);
            // Initialize per-day classes (use current day classes or global)
            for (LocalDate d : dates) {
                Day day = week.getDay(d);
                classesByDate.put(d, new ArrayList<>(classes));
            }

            for (LocalDate date : dates) {
                listPanel.add(makeRow(date, templateNames));
                listPanel.add(Box.createVerticalStrut(6));
            }

            JScrollPane scroll = new JScrollPane(listPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            root.add(scroll, BorderLayout.CENTER);

            // Bottom buttons
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(e -> dispose());

            JButton generate = new JButton("Generate");
            generate.addActionListener(e -> {
                generated = true;
//                for (int i = 0; i < week.getDays().size(); i++) {
//                    week.getDays().set(i, getUpdatedDay(week.getDays().get(i).getDate()));
//                }
                dispose();
            });

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.add(cancel);
            buttons.add(generate);
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);
            setPreferredSize(new Dimension(520, 420));
            pack();
        }

        private JPanel makeRow(LocalDate date, List<String> templateNames) {
            JPanel row = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 8);

            Day day = week.getDay(date);

            // ---- line 0: date label + template combo ----
            JLabel dayLabel = new JLabel(date.toString());
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            row.add(dayLabel, gbc);

            JComboBox<String> combo = new JComboBox<>(templateNames.toArray(new String[0]));
            combo.setEditable(false);
            if (combo.getItemCount() > 0) combo.setSelectedIndex(0);

            // preselect existing template if present
            if (day != null && day.getTemplate() != null) {
                combo.setSelectedItem(day.getTemplate());
            }

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            row.add(combo, gbc);

            // ---- line 1: classes picker ----
            JPanel classesLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            classesLine.setOpaque(false);

            JLabel classesLabel = new JLabel();
            List<Assignment> current = classesByDate.get(date);
            int count = current == null ? 0 : current.size();
            classesLabel.setText("Classes: " + count);

            JButton pickClasses = new JButton("Select Classes");
            pickClasses.addActionListener(e -> {
                List<Assignment> existing = classesByDate.get(date);
                List<Assignment> picked = WeekEdit.this.pickClassesForDate(date, existing);
                classesByDate.put(date, new ArrayList<>(picked));
                classesLabel.setText("Classes: " + picked.size());
            });

            classesLine.add(pickClasses);
            classesLine.add(classesLabel);

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            row.add(classesLine, gbc);

            // ---- line 2: split checkbox + partner split class selector ----
            JPanel splitLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            splitLine.setOpaque(false);

            JCheckBox splitBox = new JCheckBox("Split");
            splitBox.setOpaque(false);

            JComboBox<String> splitCourseCombo = new JComboBox<>();
            splitCourseCombo.setToolTipText("Partner split class");
            splitCourseCombo.addItem("(None)");

            // match your main UI behavior: exclude the primary split class
            Course primarySplit = ScheduleBuilder.getSplitClass();
            for (Assignment a : classes) {
                if (!(a instanceof Course c)) continue;
                if (primarySplit != null && c.equals(primarySplit)) continue;
                splitCourseCombo.addItem(c.getName());
            }

            // preselect from existing day model
            boolean preSplit = day != null && day.isSplit();
            splitBox.setSelected(preSplit);

            if (day != null && day.getSplitCourse() != null) {
                splitCourseCombo.setSelectedItem(day.getSplitCourse().getName());
            } else {
                splitCourseCombo.setSelectedItem("(None)");
            }

            // enable/disable partner selector based on split flag
            splitCourseCombo.setEnabled(splitBox.isSelected());

            splitBox.addActionListener(e -> {
                splitCourseCombo.setEnabled(splitBox.isSelected());
            });

            splitLine.add(splitBox);
            splitLine.add(new JLabel("Partner:"));
            splitLine.add(splitCourseCombo);

            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            row.add(splitLine, gbc);

            // store per-date components
            comboByDate.put(date, combo);
            splitByDate.put(date, splitBox);
            splitCourseComboByDate.put(date, splitCourseCombo);

            return row;
        }


        boolean wasGenerated() {
            return generated;
        }

        Map<LocalDate, Boolean> getSplitSelections() {
            Map<LocalDate, Boolean> out = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, JCheckBox> e : splitByDate.entrySet()) {
                out.put(e.getKey(), e.getValue() != null && e.getValue().isSelected());
            }
            return out;
        }

        Map<LocalDate, String> getSplitCourseSelections() {
            Map<LocalDate, String> out = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, JComboBox<String>> e : splitCourseComboByDate.entrySet()) {
                Object sel = e.getValue() == null ? null : e.getValue().getSelectedItem();
                out.put(e.getKey(), sel == null ? null : sel.toString());
            }
            return out;
        }


        Map<LocalDate, String> getSelections() {
            Map<LocalDate, String> out = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, JComboBox<String>> e : comboByDate.entrySet()) {
                Object sel = e.getValue().getSelectedItem();
                out.put(e.getKey(), sel == null ? null : sel.toString());
            }
            return out;
        }

        Map<LocalDate, List<Assignment>> getClassSelections() {
            Map<LocalDate, List<Assignment>> out = new LinkedHashMap<>();
            for (Map.Entry<LocalDate, List<Assignment>> e : classesByDate.entrySet()) {
                out.put(e.getKey(), e.getValue() == null ? null : new ArrayList<>(e.getValue()));
            }
            return out;
        }

        private static List<LocalDate> getWeekDates(Week week) {
            List<LocalDate> dates = new ArrayList<>();
            for (Day day : week.getDays()) {
                dates.add(day.getDate());
            }
            dates.sort(Comparator.naturalOrder());
            return dates;
        }
    }

}