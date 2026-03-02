package edu.mams.app.forms;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

final class AppTheme {
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);
    private static final Insets CONTENT_PADDING = new Insets(14, 14, 14, 14);

    private AppTheme() {
    }

    static void install() {
        if (!INSTALLED.compareAndSet(false, true)) {
            return;
        }

        configureMacSystemProperties();

        try {
            if (isMac()) {
                FlatMacLightLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (RuntimeException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep default LookAndFeel fallback.
            }
        }

        configureDefaults();
    }

    static void styleWindow(Window window, JComponent root) {
        if (window != null) {
            if (SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.updateComponentTreeUI(window);
            } else {
                SwingUtilities.invokeLater(() -> SwingUtilities.updateComponentTreeUI(window));
            }
        }

        if (window instanceof RootPaneContainer rootPaneContainer) {
            JRootPane rootPane = rootPaneContainer.getRootPane();
            rootPane.putClientProperty("JRootPane.titleBarShowIcon", Boolean.FALSE);
            rootPane.putClientProperty("JRootPane.titleBarShowTitle", Boolean.TRUE);
        }

        if (root != null) {
            Border padding = BorderFactory.createEmptyBorder(
                    CONTENT_PADDING.top,
                    CONTENT_PADDING.left,
                    CONTENT_PADDING.bottom,
                    CONTENT_PADDING.right
            );
            Border existing = root.getBorder();
            root.setBorder(existing == null ? padding : BorderFactory.createCompoundBorder(existing, padding));
        }
    }

    static void styleButtons(AbstractButton... buttons) {
        for (AbstractButton button : buttons) {
            if (button == null) continue;
            button.putClientProperty("JButton.buttonType", "roundRect");
            button.putClientProperty("JComponent.minimumHeight", 34);
        }
    }

    static void styleMiniButton(AbstractButton button) {
        if (button == null) return;
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JComponent.minimumWidth", 32);
        button.putClientProperty("JComponent.minimumHeight", 28);
        button.setFocusable(false);
    }

    static void styleTable(JTable table) {
        if (table == null) return;
        table.setRowHeight(30);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        if (table.getTableHeader() != null) {
            table.getTableHeader().setReorderingAllowed(false);
        }
    }

    private static void configureMacSystemProperties() {
        if (!isMac()) {
            return;
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.awt.application.name", "MAMS Scheduler");
    }

    private static void configureDefaults() {
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0.2f);
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Table.showVerticalLines", false);
    }

    private static boolean isMac() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase(Locale.ROOT).contains("mac");
    }
}
