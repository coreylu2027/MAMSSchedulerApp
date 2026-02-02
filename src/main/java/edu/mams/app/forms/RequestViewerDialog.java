package edu.mams.app.forms;

import edu.mams.app.model.people.Teacher;
import edu.mams.app.model.requests.AllSchoolRequest;
import edu.mams.app.model.requests.AvoidTimeRequest;
import edu.mams.app.model.requests.TeacherRequest;
import edu.mams.app.model.requests.TeacherTimeRequest;
import edu.mams.app.model.schedule.Assignment;
import edu.mams.app.model.schedule.Day;
import edu.mams.app.model.schedule.Week; // adjust if your Week is elsewhere

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestViewerDialog extends JDialog {

    private final Week week;
    private final List<Teacher> teachers;         // used for pickers (optional but recommended)
    private final List<Assignment> assignments;   // used for pickers (optional but recommended)

    private final RequestTableModel tableModel;
    private final JTable table;

    public RequestViewerDialog(Window owner, Week week, List<Teacher> teachers, List<Assignment> assignments) {
        super(owner, "Requests", ModalityType.APPLICATION_MODAL);
        this.week = Objects.requireNonNull(week, "week");
        this.teachers = teachers != null ? teachers : List.of();
        this.assignments = assignments != null ? assignments : List.of();

        this.tableModel = new RequestTableModel(week);
        this.table = new JTable(tableModel);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        buildUi();

        setMinimumSize(new Dimension(900, 420));
        setLocationRelativeTo(owner);
    }

    // Convenience constructor if you don’t have teacher/assignment lists handy
    public RequestViewerDialog(Window owner, Week week) {
        this(owner, week, null, null);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        // Buttons
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton closeBtn = new JButton("Close");

        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = table.getSelectedRow() >= 0;
            editBtn.setEnabled(selected);
            deleteBtn.setEnabled(selected);
        });

        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        closeBtn.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(addBtn);
        btns.add(editBtn);
        btns.add(deleteBtn);
        btns.add(closeBtn);

        root.add(btns, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void onAdd() {
        RequestEditDialog dlg = new RequestEditDialog(this, weekDays(week), teachers, assignments, null, null);
        dlg.setVisible(true);

        if (!dlg.isSaved()) return;

        Day targetDay = dlg.getSelectedDay();
        TeacherRequest newReq = dlg.buildRequest();

        if (targetDay.getRequests() == null) {
            // If your Day constructor always initializes requests, you can remove this.
            throw new IllegalStateException("Day.getRequests() returned null; initialize requests list in Day.");
        }

        targetDay.getRequests().add(newReq);
        tableModel.reload();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        RequestRow rr = tableModel.getRow(row);

        RequestEditDialog dlg = new RequestEditDialog(
                this,
                weekDays(week),
                teachers,
                assignments,
                rr.day,
                rr.request
        );
        dlg.setVisible(true);

        if (!dlg.isSaved()) return;

        Day newDay = dlg.getSelectedDay();
        TeacherRequest replacement = dlg.buildRequest();

        // If day changed: remove from old day, add to new day.
        if (rr.day != newDay) {
            rr.day.getRequests().remove(rr.request);
            newDay.getRequests().add(replacement);
        } else {
            // same day: replace in-place (preserves order)
            List<TeacherRequest> list = rr.day.getRequests();
            int idx = list.indexOf(rr.request);
            if (idx >= 0) list.set(idx, replacement);
            else list.add(replacement);
        }

        tableModel.reload();
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        RequestRow rr = tableModel.getRow(row);

        int ok = JOptionPane.showConfirmDialog(
                this,
                "Delete this request?\n\n" + tableModel.describe(rr),
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (ok != JOptionPane.OK_OPTION) return;

        rr.day.getRequests().remove(rr.request);
        tableModel.reload();
    }

    private static List<Day> weekDays(Week week) {
        // Adjust depending on your Week API.
        // Common options:
        // - week.getDays()
        // - week.getDay(i) for i=0..n-1
        if (week.getDays() != null) return week.getDays();

        // Fallback if you only have getDay(i)
        List<Day> out = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Day d = week.getDays().get(i);
            if (d != null) out.add(d);
        }
        return out;
    }

    // -------- table model + row wrapper --------

    private static class RequestRow {
        final Day day;
        final TeacherRequest request;
        RequestRow(Day day, TeacherRequest request) {
            this.day = day;
            this.request = request;
        }
    }

    private static class RequestTableModel extends AbstractTableModel {
        private final Week week;
        private final List<RequestRow> rows = new ArrayList<>();

        private static final String[] COLS = {
                "Day",
                "Type",
                "Teacher",
                "Assignment",
                "Reason",
                "Start",
                "Length"
        };

        private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

        RequestTableModel(Week week) {
            this.week = week;
            reload();
        }

        void reload() {
            rows.clear();
            for (Day d : weekDays(week)) {
                if (d == null || d.getRequests() == null) continue;
                for (TeacherRequest r : d.getRequests()) {
                    rows.add(new RequestRow(d, r));
                }
            }
            fireTableDataChanged();
        }

        RequestRow getRow(int row) {
            return rows.get(row);
        }

        String describe(RequestRow rr) {
            return rr.getClass().getSimpleName() + ": " + rr.request;
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int column) { return COLS[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RequestRow rr = rows.get(rowIndex);
            Day d = rr.day;
            TeacherRequest r = rr.request;

            String dayLabel = safeDayLabel(d);
            String type = r.getClass().getSimpleName();
            String teacher = r.getTeacher() != null ? r.getTeacher().toString() : "";
            String assignment = r.getAssignment() != null ? r.getAssignment().toString() : "";
            String reason = r.getReason() != null ? r.getReason() : "";

            String start = "";
            String length = "";
            if (r instanceof TeacherTimeRequest ttr) {
                if (ttr.getStartTime() != null) start = TIME_FMT.format(ttr.getStartTime());
                if (ttr.getLength() != null) length = prettyDuration(ttr.getLength());
            }

            return switch (columnIndex) {
                case 0 -> dayLabel;
                case 1 -> type;
                case 2 -> teacher;
                case 3 -> assignment;
                case 4 -> reason;
                case 5 -> start;
                case 6 -> length;
                default -> "";
            };
        }

        private static String safeDayLabel(Day d) {
            // Adjust to your Day API (date/dayNumber/etc.)
            try {
                if (d.getDate() != null) return d.getDate().toString();
            } catch (Exception ignored) {}
            try {
                return "Day " + d.getDayNumber();
            } catch (Exception ignored) {}
            return d.toString();
        }

        private static String prettyDuration(Duration dur) {
            long mins = dur.toMinutes();
            if (mins % 60 == 0) return (mins / 60) + " hr";
            if (mins > 60) return (mins / 60) + " hr " + (mins % 60) + " min";
            return mins + " min";
        }
    }

    // -------- editor dialog --------

    private static class RequestEditDialog extends JDialog {
        private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

        private boolean saved = false;

        private final JComboBox<Day> dayBox;
        private final JComboBox<String> typeBox;

        private final JComboBox<Teacher> teacherBox;
        private final JComboBox<Assignment> assignmentBox;

        private final JTextField reasonField = new JTextField();
        private final JTextField startTimeField = new JTextField("8:00");
        private final JTextField lengthMinField = new JTextField("30");

        RequestEditDialog(
                Window owner,
                List<Day> days,
                List<Teacher> teachers,
                List<Assignment> assignments,
                Day initialDay,
                TeacherRequest initialRequest
        ) {
            super(owner, initialRequest == null ? "Add Request" : "Edit Request", ModalityType.APPLICATION_MODAL);

            dayBox = new JComboBox<>(days.toArray(new Day[0]));
            typeBox = new JComboBox<>(new String[] { "AvoidTimeRequest", "AllSchoolRequest" });

            teacherBox = new JComboBox<>(teachers.toArray(new Teacher[0]));
            assignmentBox = new JComboBox<>(assignments.toArray(new Assignment[0]));

            // Renderers so combo boxes look nice even if toString() is meh
            dayBox.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Day d) setText(RequestTableModel.safeDayLabel(d));
                    return this;
                }
            });

            initFromExisting(initialDay, initialRequest);

            buildUi();

            setMinimumSize(new Dimension(520, 320));
            setLocationRelativeTo(owner);
        }

        private void initFromExisting(Day initialDay, TeacherRequest initialRequest) {
            if (initialDay != null) dayBox.setSelectedItem(initialDay);

            if (initialRequest == null) return;

            if (initialRequest instanceof AllSchoolRequest) typeBox.setSelectedItem("AllSchoolRequest");
            else typeBox.setSelectedItem("AvoidTimeRequest");

            if (initialRequest.getTeacher() != null) teacherBox.setSelectedItem(initialRequest.getTeacher());
            if (initialRequest.getAssignment() != null) assignmentBox.setSelectedItem(initialRequest.getAssignment());
            if (initialRequest.getReason() != null) reasonField.setText(initialRequest.getReason());

            if (initialRequest instanceof TeacherTimeRequest ttr) {
                if (ttr.getStartTime() != null) startTimeField.setText(TIME_FMT.format(ttr.getStartTime()));
                if (ttr.getLength() != null) lengthMinField.setText(Long.toString(ttr.getLength().toMinutes()));
            }
        }

        private void buildUi() {
            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(6, 6, 6, 6);
            c.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;

            addRow(form, c, y++, "Day:", dayBox);
            addRow(form, c, y++, "Type:", typeBox);
            addRow(form, c, y++, "Teacher:", teacherBox);
            addRow(form, c, y++, "Assignment:", assignmentBox);
            addRow(form, c, y++, "Reason:", reasonField);
            addRow(form, c, y++, "Start time (H:mm):", startTimeField);
            addRow(form, c, y++, "Length (minutes):", lengthMinField);

            JButton saveBtn = new JButton("Save");
            JButton cancelBtn = new JButton("Cancel");

            saveBtn.addActionListener(e -> {
                try {
                    validateInputs();
                    saved = true;
                    dispose();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelBtn.addActionListener(e -> dispose());

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btns.add(saveBtn);
            btns.add(cancelBtn);

            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            root.add(form, BorderLayout.CENTER);
            root.add(btns, BorderLayout.SOUTH);

            setContentPane(root);
        }

        private static void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
            c.gridx = 0; c.gridy = row; c.weightx = 0;
            form.add(new JLabel(label), c);
            c.gridx = 1; c.gridy = row; c.weightx = 1;
            form.add(field, c);
        }

        private void validateInputs() {
            if (dayBox.getSelectedItem() == null) throw new IllegalArgumentException("Pick a day.");
            if (typeBox.getSelectedItem() == null) throw new IllegalArgumentException("Pick a request type.");
            if (teacherBox.getSelectedItem() == null) {
                throw new IllegalArgumentException("Pick a teacher.");
            }

            // start time parse
            parseTime(startTimeField.getText());

            // length parse
            long mins = parseMinutes(lengthMinField.getText());
            if (mins <= 0) throw new IllegalArgumentException("Length must be a positive number of minutes.");
        }

        private static LocalTime parseTime(String s) {
            try {
                return LocalTime.parse(s.trim(), TIME_FMT);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Start time must be like 8:00 or 13:30 (H:mm).");
            }
        }

        private static long parseMinutes(String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Length must be an integer number of minutes.");
            }
        }

        boolean isSaved() { return saved; }

        Day getSelectedDay() {
            return (Day) dayBox.getSelectedItem();
        }

        TeacherRequest buildRequest() {
            Day day = getSelectedDay();
            Teacher teacher = (Teacher) teacherBox.getSelectedItem();
            Assignment assignment = (Assignment) assignmentBox.getSelectedItem();
            String reason = reasonField.getText() != null ? reasonField.getText().trim() : "";

            LocalTime start = parseTime(startTimeField.getText());
            long mins = parseMinutes(lengthMinField.getText());
            Duration len = Duration.ofMinutes(mins);

            String type = (String) typeBox.getSelectedItem();

            // Build your existing model types (both extend TeacherTimeRequest)
            if ("AllSchoolRequest".equals(type)) {
                AllSchoolRequest r = new AllSchoolRequest();
                r.setTeacher(teacher);
                r.setAssignment(assignment);
                r.setReason(reason);
                r.setStartTime(start);
                r.setLength(len);
                return r;
            } else {
                AvoidTimeRequest r = new AvoidTimeRequest();
                r.setTeacher(teacher);
                r.setAssignment(assignment);
                r.setReason(reason);
                r.setStartTime(start);
                r.setLength(len);
                return r;
            }
        }
    }
}
