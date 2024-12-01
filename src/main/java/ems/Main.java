package ems;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException,
            ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        App app = new App();
        app.setSize(800, 680);
        app.setVisible(true);
        app.setTitle("Employee Management System");
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
