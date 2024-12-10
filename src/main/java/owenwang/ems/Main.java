package owenwang.ems;

import javax.swing.*;

/**
 * Employee Management System (EMS)
 * ICS4U0-4
 * 2024-12-10
 * Requires Java 11 or higher
 * @author Owen Wang <752381@pdsb.net>
 */
public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException,
            ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        App app = new App();
        app.setSize(App.PREFS.getInt("windowWidth", 800), App.PREFS.getInt("windowHeight", 680));
        app.setExtendedState(App.PREFS.getInt("windowExtendedState", JFrame.NORMAL));
        app.setVisible(true);
        app.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
}
