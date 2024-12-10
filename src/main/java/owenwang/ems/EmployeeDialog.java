package owenwang.ems;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class EmployeeDialog extends JDialog {

    private JPanel contentPane;
    private JButton submitButton;
    private JButton cancelButton;
    private JPanel details;
    private JTextField fNameField;
    private JTextField lNameField;
    private JRadioButton fullTimeRadioButton;
    private JRadioButton partTimeRadioButton;
    private JTextField employeeNumberField;
    private JPanel incomeDetails;
    private JSpinner hourlyWageField;
    private JSpinner hoursPerWeekField;
    private JSpinner weeksPerYearField;
    private JSpinner salaryField;
    private JLabel errorMessageLabel;
    private JComboBox<String> workLocationBox;
    private JRadioButton femaleGenderOption;
    private JRadioButton maleGenderOption;
    private JRadioButton otherGenderOption;
    private JSpinner deductionRateFTE;
    private JSpinner deductionRatePTE;
    private JRadioButton preferNotToSayGenderOption;

    private final int initialId; // The id of the employee to edit, or -1 to create new
    private int finalId; // -1 if the change was cancelled

    private final MyHashTable data;

    public static int newEmployeeDialog(JFrame parent, MyHashTable data) {
        var d = new EmployeeDialog(parent, data, -1);
        d.setVisible(true);
        return d.finalId;
    }

    // Returns -1 if no change, else returns the id of the employee after the change
    public static int modifyEmployeeDialog(JFrame parent, MyHashTable data, int employee) {
        var d = new EmployeeDialog(parent, data, employee);
        d.setVisible(true);
        return d.finalId;
    }

    private EmployeeDialog(JFrame parent, MyHashTable data, int employee) {
        super(parent, true);
        // this.suggestedId = suggestedId;
        this.data = data;
        this.initialId = employee;
        setTitle(this.initialId == -1 ? "New Employee" : "Edit Employee");
        $$$setupUI$$$();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(submitButton);
        setMinimumSize(getRootPane().getMinimumSize());
        setResizable(false);
        pack();
        setSize(400, getHeight());

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSubmit();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE

        workLocationBox.setModel(new DefaultComboBoxModel<>(Constants.WORK_LOCATIONS));

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        //otherGenderField.setEnabled(false);
        /*otherGenderOption.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) otherGenderField.setEnabled(true);
            if (e.getStateChange() == ItemEvent.DESELECTED) otherGenderField.setEnabled(false);
        });*/

        errorMessageLabel.setForeground(Color.red);
        errorMessageLabel.setText("");
        fullTimeRadioButton.addActionListener(e -> onFTESelected());
        partTimeRadioButton.addActionListener(e -> onPTESelected());

        salaryField.setModel(new SpinnerNumberModel(0d, 0d, Constants.MAX_INPUT_MONEY, 1d));
        salaryField.setEditor(new JSpinner.NumberEditor(salaryField, "#,##0.00"));
        hourlyWageField.setModel(new SpinnerNumberModel(0d, 0d, Constants.MAX_INPUT_MONEY, 1d));
        hourlyWageField.setEditor(new JSpinner.NumberEditor(hourlyWageField, "#,##0.00"));

        hoursPerWeekField.setModel(new SpinnerNumberModel(0d, 0d, Constants.HOURS_PER_WEEK, 1d));
        weeksPerYearField.setModel(new SpinnerNumberModel(0d, 0d, Constants.WEEKS_PER_YEAR, 1d));
        SpinnerModel percentModel = new SpinnerNumberModel(0d, 0d, 100d, 1d);
        deductionRateFTE.setModel(percentModel);
        deductionRatePTE.setModel(percentModel);
        initializeForm();
    }

    private void onSubmit() {
        // add your code here

        errorMessageLabel.setText("");

        EmployeeInfo emp = buildEmployee();
        if (emp != null) {
            if (initialId != -1) data.removeFromTable(initialId);
            data.addToTable(emp);
            finalId = emp.getEmpNum();
            dispose();
        }
    }

    private void initializeForm() {
        boolean emptyForm = initialId == -1;
        EmployeeInfo e = emptyForm ? null : data.getFromTable(initialId);
        employeeNumberField.setText(emptyForm ? "" : Integer.toString(e.getEmpNum()));
        fNameField.setText(emptyForm ? "" : e.getFirstName());
        lNameField.setText(emptyForm ? "" : e.getLastName());
        workLocationBox.setSelectedIndex(emptyForm ? 0 : e.getWorkLoc());
        if (!emptyForm) {
            switch (e.getGender()) {
                case EmployeeInfo.Gender.MALE:
                    maleGenderOption.setSelected(true);
                    break;
                case EmployeeInfo.Gender.FEMALE:
                    femaleGenderOption.setSelected(true);
                    break;
                case EmployeeInfo.Gender.OTHER:
                    otherGenderOption.setSelected(true);
                    break;
                case EmployeeInfo.Gender.PREFER_NOT_TO_SAY:
                    preferNotToSayGenderOption.setSelected(true);
                    break;
            }
        }
        if (e != null) {
            deductionRateFTE.setValue(e.getDeductRate() * 100d);
            deductionRatePTE.setValue(e.getDeductRate() * 100d);
        }
        if (e instanceof FTE) {
            fullTimeRadioButton.setSelected(true);
            onFTESelected();
            salaryField.setValue(((FTE) e).getYearlySalary());
        } else if (e instanceof PTE) {
            partTimeRadioButton.setSelected(true);
            onPTESelected();
            hourlyWageField.setValue(((PTE) e).getHourlyWage());
            hoursPerWeekField.setValue(((PTE) e).getHoursPerWeek());
            weeksPerYearField.setValue(((PTE) e).getWeeksPerYear());
        }
        submitButton.setText(initialId == -1 ? "Add" : "Save");
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private EmployeeInfo buildEmployee() {
        Optional<Integer> employeeNumber = Util.toInt(employeeNumberField.getText());
        if (employeeNumberField.getText().isBlank()) {
            errorMessageLabel.setText("Enter an employee number");
            return null;
        } else if (employeeNumber.isEmpty()) {
            errorMessageLabel.setText("Employee number must be an integer");
            return null;
        } else if (employeeNumber.get().compareTo(0) < 0) {
            errorMessageLabel.setText("Employee number must not be negative");
            return null;
        } else if (!employeeNumber.get().equals(initialId) && data.getFromTable(employeeNumber.get()) != null) {
                errorMessageLabel.setText("Employee ID already in use");
            return null;
        } else if (fNameField.getText().isBlank()) {
            errorMessageLabel.setText("Enter a first name");
            return null;
        } else if (lNameField.getText().isBlank()) {
            errorMessageLabel.setText("Enter a last name");
            return null;
        } else {
            // No errors
            EmployeeInfo emp;
            final int gender;
            if (maleGenderOption.isSelected()) gender = EmployeeInfo.Gender.MALE;
            else if (femaleGenderOption.isSelected()) gender = EmployeeInfo.Gender.FEMALE;
            else if (otherGenderOption.isSelected()) gender = EmployeeInfo.Gender.OTHER;
            else if (preferNotToSayGenderOption.isSelected()) gender = EmployeeInfo.Gender.PREFER_NOT_TO_SAY;
            else gender = 0;

            if (partTimeRadioButton.isSelected()) {
                emp = new PTE(employeeNumber.get(),
                        fNameField.getText().strip(),
                        lNameField.getText().strip(),
                        gender,
                        workLocationBox.getSelectedIndex(),
                        (Double) deductionRatePTE.getValue() / 100d,
                        (Double) hourlyWageField.getValue(),
                        (Double) hoursPerWeekField.getValue(),
                        (Double) weeksPerYearField.getValue()
                );
            } else {
                emp = new FTE(employeeNumber.get(),
                        fNameField.getText().strip(),
                        lNameField.getText().strip(),
                        gender,
                        workLocationBox.getSelectedIndex(),
                        (Double) deductionRateFTE.getValue() / 100d,
                        (Double) salaryField.getValue()
                );
            }
            return emp;
        }
    }

    private void onCancel() {
        // add your code here if necessary
        finalId = -1;
        dispose();
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
        contentPane.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(panel1, gbc);
        details = new JPanel();
        details.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel1.add(details, gbc);
        details.setBorder(BorderFactory.createTitledBorder(null, "Basic Details", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(10);
        label1.setHorizontalTextPosition(11);
        label1.setText("First Name:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        details.add(label1, gbc);
        fNameField = new JTextField();
        fNameField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(fNameField, gbc);
        final JLabel label2 = new JLabel();
        label2.setHorizontalAlignment(10);
        label2.setHorizontalTextPosition(11);
        label2.setText("Last Name:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        details.add(label2, gbc);
        lNameField = new JTextField();
        lNameField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(lNameField, gbc);
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment(10);
        label3.setHorizontalTextPosition(11);
        label3.setText("Type:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        details.add(label3, gbc);
        fullTimeRadioButton = new JRadioButton();
        fullTimeRadioButton.setSelected(true);
        fullTimeRadioButton.setText("Full-time");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(fullTimeRadioButton, gbc);
        partTimeRadioButton = new JRadioButton();
        partTimeRadioButton.setText("Part-time");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(partTimeRadioButton, gbc);
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment(10);
        label4.setHorizontalTextPosition(11);
        label4.setText("Employee Number:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        details.add(label4, gbc);
        employeeNumberField = new JTextField();
        employeeNumberField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(employeeNumberField, gbc);
        final JLabel label5 = new JLabel();
        label5.setHorizontalAlignment(10);
        label5.setHorizontalTextPosition(11);
        label5.setText("Gender:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        details.add(label5, gbc);
        femaleGenderOption = new JRadioButton();
        femaleGenderOption.setSelected(true);
        femaleGenderOption.setText("Female");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(femaleGenderOption, gbc);
        otherGenderOption = new JRadioButton();
        otherGenderOption.setText("Other");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(otherGenderOption, gbc);
        preferNotToSayGenderOption = new JRadioButton();
        preferNotToSayGenderOption.setText("Prefer not to say");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(preferNotToSayGenderOption, gbc);
        final JLabel label6 = new JLabel();
        label6.setHorizontalAlignment(10);
        label6.setHorizontalTextPosition(11);
        label6.setText("Work Location:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        details.add(label6, gbc);
        workLocationBox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        workLocationBox.setModel(defaultComboBoxModel1);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(workLocationBox, gbc);
        maleGenderOption = new JRadioButton();
        maleGenderOption.setText("Male");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        details.add(maleGenderOption, gbc);
        incomeDetails = new JPanel();
        incomeDetails.setLayout(new CardLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel1.add(incomeDetails, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        incomeDetails.add(panel2, "FTE");
        panel2.setBorder(BorderFactory.createTitledBorder(null, "Full-Time Employee", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label7 = new JLabel();
        label7.setHorizontalAlignment(10);
        label7.setHorizontalTextPosition(11);
        label7.setText("Salary:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        panel2.add(label7, gbc);
        final JLabel label8 = new JLabel();
        label8.setText("$");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label8, gbc);
        salaryField = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel2.add(salaryField, gbc);
        final JLabel label9 = new JLabel();
        label9.setHorizontalAlignment(10);
        label9.setHorizontalTextPosition(11);
        label9.setText("Deduction Rate:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        panel2.add(label9, gbc);
        deductionRateFTE = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel2.add(deductionRateFTE, gbc);
        final JLabel label10 = new JLabel();
        label10.setText("%");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label10, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        incomeDetails.add(panel3, "PTE");
        panel3.setBorder(BorderFactory.createTitledBorder(null, "Part-Time Employee", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label11 = new JLabel();
        label11.setHorizontalAlignment(10);
        label11.setHorizontalTextPosition(11);
        label11.setText("Hourly Wage:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        panel3.add(label11, gbc);
        final JLabel label12 = new JLabel();
        label12.setHorizontalAlignment(10);
        label12.setHorizontalTextPosition(11);
        label12.setText("Hours per Week:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        panel3.add(label12, gbc);
        final JLabel label13 = new JLabel();
        label13.setHorizontalAlignment(10);
        label13.setHorizontalTextPosition(11);
        label13.setText("Weeks Per Year:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        panel3.add(label13, gbc);
        final JLabel label14 = new JLabel();
        label14.setText("$");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label14, gbc);
        hourlyWageField = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel3.add(hourlyWageField, gbc);
        hoursPerWeekField = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel3.add(hoursPerWeekField, gbc);
        weeksPerYearField = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel3.add(weeksPerYearField, gbc);
        final JLabel label15 = new JLabel();
        label15.setHorizontalAlignment(10);
        label15.setHorizontalTextPosition(11);
        label15.setText("Deduction Rate:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 10, 0, 10);
        panel3.add(label15, gbc);
        deductionRatePTE = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel3.add(deductionRatePTE, gbc);
        final JLabel label16 = new JLabel();
        label16.setText("%");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label16, gbc);
        errorMessageLabel = new JLabel();
        errorMessageLabel.setEnabled(true);
        errorMessageLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 10);
        contentPane.add(errorMessageLabel, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        contentPane.add(panel4, gbc);
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        panel4.add(cancelButton);
        submitButton = new JButton();
        submitButton.setText("Add");
        panel4.add(submitButton);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(fullTimeRadioButton);
        buttonGroup.add(partTimeRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(femaleGenderOption);
        buttonGroup.add(femaleGenderOption);
        buttonGroup.add(maleGenderOption);
        buttonGroup.add(otherGenderOption);
        buttonGroup.add(preferNotToSayGenderOption);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private boolean isInputValid() {
        return !fNameField.getText().isBlank() && !lNameField.getText().isBlank() &&
                !Double.isNaN(Util.toDouble(employeeNumberField.getText()));
    }

    private void onFTESelected() {
        ((CardLayout) incomeDetails.getLayout()).show(incomeDetails, "FTE");
    }

    private void onPTESelected() {
        ((CardLayout) incomeDetails.getLayout()).show(incomeDetails, "PTE");
    }
}
