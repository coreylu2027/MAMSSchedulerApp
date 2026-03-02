package edu.mams.app.forms;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class UiExceptionHandler {
    private static final AtomicBoolean INSTALLED = new AtomicBoolean(false);
    private static final AtomicBoolean SHOWING_DIALOG = new AtomicBoolean(false);

    private UiExceptionHandler() {
    }

    static void install() {
        if (!INSTALLED.compareAndSet(false, true)) {
            return;
        }

        Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            showUnexpectedError(throwable);
            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable);
            }
        });

        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            @Override
            protected void dispatchEvent(AWTEvent event) {
                try {
                    super.dispatchEvent(event);
                } catch (RuntimeException ex) {
                    showUnexpectedError(ex);
                }
            }
        });
    }

    static void showUnexpectedError(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        throwable.printStackTrace();

        if (!SHOWING_DIALOG.compareAndSet(false, true)) {
            return;
        }

        Runnable showDialog = () -> {
            try {
                Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
                JOptionPane.showMessageDialog(
                        owner,
                        buildMessage(throwable),
                        "Unexpected Error",
                        JOptionPane.ERROR_MESSAGE
                );
            } catch (RuntimeException dialogFailure) {
                dialogFailure.printStackTrace();
            } finally {
                SHOWING_DIALOG.set(false);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            showDialog.run();
        } else {
            SwingUtilities.invokeLater(showDialog);
        }
    }

    private static String buildMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null) {
            root = root.getCause();
        }

        String type = root.getClass().getSimpleName();
        String details = root.getMessage();
        if (details == null || details.isBlank()) {
            return "An unexpected error occurred (" + type + ").";
        }
        return "An unexpected error occurred:\n" + type + ": " + details;
    }
}
