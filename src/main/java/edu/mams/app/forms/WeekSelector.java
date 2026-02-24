package edu.mams.app.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import edu.mams.app.model.schedule.Schedule;
import edu.mams.app.model.schedule.Week;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeekSelector extends JFrame {
    private static final File FILE = new File("schedule.json");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
    private final Schedule schedule;
    private JButton cancelButton;
    private JPanel contentPane;
    private JButton deleteButton;
    private JButton newWeekButton;
    private JButton openButton;
    private JList<LocalDate> weekList;

    public WeekSelector(Schedule schedule) {
        this.schedule = schedule;

        // GUI setup
        setContentPane(contentPane);
        setTitle("Week Selector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);

        initWeekList();
        initActions();
    }

    private void initWeekList() {
        DefaultListModel<LocalDate> model = new DefaultListModel<>();

        schedule.getWeekStartDates().stream().sorted().forEach(model::addElement);

        weekList.setModel(model);
        weekList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        weekList.setFixedCellHeight(34);

        weekList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.format(DISPLAY_FMT));
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            lbl.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            lbl.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return lbl;
        });

        if (!model.isEmpty()) {
            weekList.setSelectedIndex(0);
        }
    }

    private void initActions() {

        openButton.addActionListener(e -> openSelectedWeek());
        deleteButton.addActionListener(e -> deleteSelectedWeek());

        newWeekButton.addActionListener(e -> {
            LocalDate lastStart = schedule.getWeekStartDates().stream().max(LocalDate::compareTo).orElse(null);
            LocalDate newStart = (lastStart == null ? LocalDate.of(2025, 12, 1).minusWeeks(1) : lastStart).plusWeeks(1);

            int startingDayNumber = 0;
            if (lastStart != null) {
                Week previousWeek = schedule.getWeek(lastStart);
                if (previousWeek != null && previousWeek.getDays() != null && !previousWeek.getDays().isEmpty()) {
                    startingDayNumber = previousWeek.getEndingDayNumber() + 1;
                }
            }

            Week newWeek = new Week(newStart, startingDayNumber);
            schedule.addWeek(newWeek);
            schedule.saveToFile(FILE);

            refreshList();
            weekList.setSelectedValue(newStart, true);
        });

        cancelButton.addActionListener(e -> dispose());

        // Double-click to open
        weekList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedWeek();
                }
            }
        });
    }

    private void openSelectedWeek() {
        LocalDate start = weekList.getSelectedValue();
        if (start == null) {
            JOptionPane.showMessageDialog(this, "Select a week first.");
            return;
        }

        Week week = schedule.getWeek(start);
        new WeekEdit(week, schedule).setVisible(true);
        dispose();
    }

    private void deleteSelectedWeek() {
        LocalDate start = weekList.getSelectedValue();
        if (start == null) {
            JOptionPane.showMessageDialog(this, "Select a week first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete the week starting " + start.format(DISPLAY_FMT) + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        schedule.getWeeks().remove(start);
        schedule.saveToFile(FILE);

        int priorIndex = weekList.getSelectedIndex();
        refreshList();

        DefaultListModel<LocalDate> model = (DefaultListModel<LocalDate>) weekList.getModel();
        if (!model.isEmpty()) {
            int nextIndex = Math.min(priorIndex, model.size() - 1);
            weekList.setSelectedIndex(nextIndex);
        }
    }

    private void refreshList() {
        DefaultListModel<LocalDate> model = (DefaultListModel<LocalDate>) weekList.getModel();

        model.clear();
        schedule.getWeekStartDates().stream().sorted().forEach(model::addElement);
    }

    public static void main(String[] args) {
        Schedule schedule = Schedule.loadFromFile(FILE);
        SwingUtilities.invokeLater(() -> new WeekSelector(schedule).setVisible(true));
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
        contentPane.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        weekList = new JList();
        contentPane.add(weekList, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        contentPane.add(cancelButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        newWeekButton = new JButton();
        newWeekButton.setText("New");
        contentPane.add(newWeekButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Open");
        contentPane.add(openButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deleteButton = new JButton();
        deleteButton.setText("Delete");
        contentPane.add(deleteButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
