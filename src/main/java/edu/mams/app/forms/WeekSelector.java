package edu.mams.app.forms;

import edu.mams.app.model.schedule.Schedule;
import edu.mams.app.model.schedule.Week;

import javax.swing.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeekSelector extends JFrame {
    private JPanel contentPane;
    private JList<LocalDate> weekList;
    private JButton openButton;
    private JButton newWeekButton;
    private JButton cancelButton;

    // ===== App state =====
    private final Schedule schedule;
    private static final File FILE = new File("schedule.json");

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");

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

    // ===== Initialize list =====
    private void initWeekList() {
        DefaultListModel<LocalDate> model = new DefaultListModel<>();

        schedule.getWeekStartDates().stream()
                .sorted()
                .forEach(model::addElement);

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

    // ===== Button actions =====
    private void initActions() {

        openButton.addActionListener(e -> openSelectedWeek());

        newWeekButton.addActionListener(e -> {
            LocalDate newStart = schedule.getWeekStartDates().stream()
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.of(2025, 12, 1).minusWeeks(1))
                    .plusWeeks(1);

            Week newWeek = new Week(newStart);
            schedule.addWeek(newWeek);
            schedule.saveToFile(FILE);

            refreshList();
            weekList.setSelectedValue(newStart, true);
        });

        cancelButton.addActionListener(e -> dispose());

        // Double-click to open
        weekList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedWeek();
                }
            }
        });
    }

    // ===== Helpers =====
    private void refreshList() {
        DefaultListModel<LocalDate> model =
                (DefaultListModel<LocalDate>) weekList.getModel();

        model.clear();
        schedule.getWeekStartDates().stream()
                .sorted()
                .forEach(model::addElement);
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

    public static void main(String[] args) {
        Schedule schedule = Schedule.loadFromFile(FILE);
        SwingUtilities.invokeLater(() -> new WeekSelector(schedule).setVisible(true));
    }
}
