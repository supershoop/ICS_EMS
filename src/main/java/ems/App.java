package ems;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class App extends JFrame {
    private final MyHashTable data = new MyHashTable(32);
    private final List<Integer> ids = new ArrayList<>();
    private final EmployeeTableModel model = new EmployeeTableModel(data, ids);

    public App() {
        add(panel1);
        addEmployeeButton.addActionListener((e) -> this.addEmployeeButtonClick());
        table.setModel(model);
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        table.setDefaultRenderer(Double.class, new MoneyRenderer());
        table.getSelectionModel().addListSelectionListener(x -> onTableSelectionChanged());
        removeButton.addActionListener(e -> deleteEmployee());
        editButton.addActionListener(e -> editEmployee());
    }

    private void deleteEmployee() {
        int row = table.getSelectedRow();
        data.removeFromTable(model.employeeAtIndex(row).empNum);
        ids.remove(row);
        model.fireTableRowsDeleted(row, row);
    }

    private void onTableSelectionChanged() {
        if (table.getSelectionModel().isSelectionEmpty()) noneSelected();
        else employeeSelected(model.employeeAtIndex(table.getSelectedRow()));
    }

    private void employeeSelected(EmployeeInfo e) {
        ((CardLayout) detailsPane.getLayout()).show(detailsPane, "EmployeeDetailsFTE");
        FTEemployeeName.setText(String.format("%s %s", e.getFirstName(), e.getLastName()));
        FTEemployeeNumber.setText(String.format("#%d", e.getEmpNum()));
        fNameField.setText(e.getFirstName());
        lNameField.setText(e.getLastName());
        genderField.setText(Constants.GENDERS[e.getGender()]);
        workLocationField.setText(Constants.WORK_LOCATIONS[e.getWorkLocation()]);
        deductionRateField.setText(Constants.PERCENTAGE_FORMAT.format(e.getDeductRate() * 100d));
        grossIncomeField.setText(Constants.MONEY_FORMAT.format(e.calcGrossAnnualIncome()));
        netIncomeField.setText(Constants.MONEY_FORMAT.format(e.calcNetAnnualIncome()));
        showEmployeeDetails(e);
        if (e instanceof FTE) {
            var fte = (FTE) e;
            salaryField.setVisible(true);
            salaryField.setText(Constants.MONEY_FORMAT.format(fte.getYearlySalary()));
        } else if (e instanceof PTE) {
            var pte = (PTE) e;
            hourlyWageField.setText(Constants.MONEY_FORMAT.format(pte.getHourlyWage()));
            hoursPerWeekField.setText(Constants.DECIMAL_FORMAT.format(pte.getHoursPerWeek()));
            weeksPerYearField.setText(Constants.DECIMAL_FORMAT.format(pte.getWeeksPerYear()));
        }
    }

    private void showEmployeeDetails(EmployeeInfo e) {
        salaryLabel.setVisible(e instanceof FTE);
        salaryField.setVisible(e instanceof FTE);
        hourlyWageLabel.setVisible(e instanceof PTE);
        hourlyWageField.setVisible(e instanceof PTE);
        hoursPerWeekLabel.setVisible(e instanceof PTE);
        hoursPerWeekField.setVisible(e instanceof PTE);
        weeksPerYearLabel.setVisible(e instanceof PTE);
        weeksPerYearField.setVisible(e instanceof PTE);
        typeField.setText(e instanceof PTE ? "Part-time" : "Full-time");
    }

    public void noneSelected() {
        ((CardLayout) detailsPane.getLayout()).show(detailsPane, "Placeholder");
    }

    public void addEmployeeButtonClick() {
        int employee = EmployeeDialog.newEmployeeDialog(data);
        if (employee != -1) {
            ids.add(employee);
            Collections.sort(ids);
            int row = ids.indexOf(employee);
            model.fireTableRowsInserted(row, row);
        }
    }

    public void editEmployee() {
        var modification = EmployeeDialog.modifyEmployeeDialog(data, ids.get(table.getSelectedRow()));
        int oldRow = ids.indexOf(modification.oldNumber);
        if (modification.oldNumber != modification.newNumber) {
            ids.remove(Integer.valueOf(modification.newNumber));
            ids.add(modification.newNumber);
            Collections.sort(ids);
        }
        int newRow = ids.indexOf(modification.newNumber);
        if (oldRow == newRow) {
            model.fireTableRowsUpdated(newRow, newRow);
        } else {
            model.fireTableRowsDeleted(oldRow, oldRow);
            model.fireTableRowsInserted(newRow, newRow);
        }
        onTableSelectionChanged(); // refresh the details pane
    }

    private JPanel panel1;
    private JTable table;
    private JButton addEmployeeButton;
    private JLabel fNameField;
    private JLabel lNameField;
    private JLabel genderField;
    private JLabel workLocationField;
    private JLabel salaryField;
    private JLabel deductionRateField;
    private JPanel detailsPane;
    private JLabel FTEemployeeName;
    private JButton editButton;
    private JButton removeButton;
    private JLabel FTEemployeeNumber;
    private JLabel hourlyWageField;
    private JLabel hourlyWageLabel;
    private JLabel hoursPerWeekLabel;
    private JLabel weeksPerYearLabel;
    private JLabel grossIncomeField;
    private JLabel netIncomeField;
    private JLabel salaryLabel;
    private JLabel typeField;
    private JLabel hoursPerWeekField;
    private JLabel weeksPerYearField;

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
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        final JSplitPane splitPane1 = new JSplitPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(splitPane1, gbc);
        detailsPane = new JPanel();
        detailsPane.setLayout(new CardLayout(0, 0));
        splitPane1.setRightComponent(detailsPane);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(11);
        label1.setText("<html><p style=\"text-align: center\">Select an employee to view or edit details</p></html>");
        detailsPane.add(label1, "Placeholder");
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        detailsPane.add(panel2, "EmployeeDetailsFTE");
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(separator1, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel2.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$(null, Font.BOLD, -1, panel3.getFont()), null));
        final JLabel label2 = new JLabel();
        label2.setText("First Name");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label2, gbc);
        fNameField = new JLabel();
        fNameField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel3.add(fNameField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Last Name");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel3.add(label3, gbc);
        lNameField = new JLabel();
        lNameField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel3.add(lNameField, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label4, gbc);
        typeField = new JLabel();
        typeField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel3.add(typeField, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Gender");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel3.add(label5, gbc);
        genderField = new JLabel();
        genderField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel3.add(genderField, gbc);
        final JLabel label6 = new JLabel();
        label6.setText("Work Location");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel3.add(label6, gbc);
        workLocationField = new JLabel();
        workLocationField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel3.add(workLocationField, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel2.add(spacer1, gbc);
        editButton = new JButton();
        editButton.setText("Edit");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 5);
        panel2.add(editButton, gbc);
        removeButton = new JButton();
        removeButton.setText("Remove");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 10);
        panel2.add(removeButton, gbc);
        final JSeparator separator2 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(separator2, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel4, gbc);
        FTEemployeeName = new JLabel();
        Font FTEemployeeNameFont = this.$$$getFont$$$(null, Font.BOLD, 16, FTEemployeeName.getFont());
        if (FTEemployeeNameFont != null) FTEemployeeName.setFont(FTEemployeeNameFont);
        FTEemployeeName.setText("fName lName");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel4.add(FTEemployeeName, gbc);
        FTEemployeeNumber = new JLabel();
        Font FTEemployeeNumberFont = this.$$$getFont$$$(null, -1, 14, FTEemployeeNumber.getFont());
        if (FTEemployeeNumberFont != null) FTEemployeeNumber.setFont(FTEemployeeNumberFont);
        FTEemployeeNumber.setText("#123456");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel4.add(FTEemployeeNumber, gbc);
        final JSeparator separator3 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(separator3, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 0, 10);
        panel2.add(panel5, gbc);
        salaryLabel = new JLabel();
        salaryLabel.setText("Salary");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(salaryLabel, gbc);
        salaryField = new JLabel();
        salaryField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(salaryField, gbc);
        deductionRateField = new JLabel();
        deductionRateField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(deductionRateField, gbc);
        final JLabel label7 = new JLabel();
        label7.setText("Deduction Rate");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(label7, gbc);
        hourlyWageLabel = new JLabel();
        hourlyWageLabel.setText("Hourly Wage");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(hourlyWageLabel, gbc);
        hourlyWageField = new JLabel();
        hourlyWageField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(hourlyWageField, gbc);
        hoursPerWeekLabel = new JLabel();
        hoursPerWeekLabel.setText("Hours per Week");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(hoursPerWeekLabel, gbc);
        hoursPerWeekField = new JLabel();
        hoursPerWeekField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(hoursPerWeekField, gbc);
        weeksPerYearLabel = new JLabel();
        weeksPerYearLabel.setText("Weeks Per Year");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(weeksPerYearLabel, gbc);
        weeksPerYearField = new JLabel();
        weeksPerYearField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(weeksPerYearField, gbc);
        grossIncomeField = new JLabel();
        grossIncomeField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(grossIncomeField, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("Gross Income");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(label8, gbc);
        netIncomeField = new JLabel();
        netIncomeField.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(netIncomeField, gbc);
        final JLabel label9 = new JLabel();
        label9.setText("Net Income");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel5.add(label9, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        splitPane1.setLeftComponent(scrollPane1);
        table = new JTable();
        table.setFillsViewportHeight(true);
        scrollPane1.setViewportView(table);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(toolBar1, gbc);
        addEmployeeButton = new JButton();
        addEmployeeButton.setText("Add Employee");
        toolBar1.add(addEmployeeButton);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
