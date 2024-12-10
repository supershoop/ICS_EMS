package owenwang.ems;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class App extends JFrame {
    public static final Preferences PREFS = Preferences.userNodeForPackage(App.class);
    private final MyHashTable data = new MyHashTable(32);
    private final List<Integer> ids = new ArrayList<>();
    private final EmployeeTableModel model = new EmployeeTableModel(data, ids);
    private Path file = null;
    private boolean unsavedChanges = false;

    public App() {
        add(panel1);
        rootPane.registerKeyboardAction(e -> newFile(),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> save(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> saveAs(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> open(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> debug(),
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> checkIntegrity(),
                KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> searchField.requestFocus(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        addEmployeeButton.addActionListener((e) -> this.addEmployee());
        saveAsButton.addActionListener(e -> this.saveAs());
        newButton.addActionListener(e -> this.newFile());
        saveButton.addActionListener(e -> this.save());
        openButton.addActionListener(e -> this.open());
        employeeDetails.addEditListener(e -> this.editEmployee(employeeDetails.getEmployee()));
        employeeDetails.addRemovalListener(e -> this.removeSingleEmployee(employeeDetails.getEmployee()));
        removeAllButton.addActionListener(e -> this.removeSelectedEmployees());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    search();
                }
            }
        });
        searchButton.addActionListener(e -> this.search());
        table.setModel(model);
        table.setAutoCreateRowSorter(true);
        table.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        table.setDefaultRenderer(Double.class, new MoneyRenderer());
        table.getSelectionModel().addListSelectionListener(e -> onTableSelectionChanged());
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) removeSelectedEmployees();
            }
        });
        updateTitle();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClose();
            }
        });
        defaultOpen();
        updateTitle();
        updateToolbar();
        splitPane.setDividerLocation(PREFS.getInt("dividerLocation", splitPane.getDividerLocation()));
    }

    private void checkIntegrity() {
        // for debugging use, trigger with ctrl + alt + /
        int last = -1;
        for (var id : ids) {
            if (ids.size() == data.length() && id > last && data.getFromTable(id) != null) {
                last = id;
            } else {
                // error
                JOptionPane.showMessageDialog(this, "Id list is invalid.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No issues found.");
    }

    public void defaultOpen() {
        var lastOpenFile = PREFS.get("file", "");
        if (!lastOpenFile.isEmpty()) {
            openFrom(Path.of(lastOpenFile), true);
        }
    }

    private void updateTitle() {
        String fileName = file != null ? file.getFileName().toString() : "Untitled";
        setTitle(String.format("%s%s - EMS", fileName, unsavedChanges ? "*" : ""));
    }

    private EmployeeInfo currentlySelectedEmployee() {
        if (table.getSelectedRow() == -1) return null;
        else return model.employeeAtIndex(table.getRowSorter().convertRowIndexToModel(table.getSelectedRow()));
    }

    private boolean confirmRemoveEmployee(int count) {
        var e = currentlySelectedEmployee();
        if (e == null) return false;
        String title = count > 1 ? "Remove Employees" : "Remove Employee";
        String message = String.format("Are you sure you want to remove %s from the EMS? This action cannot be undone.",
                count > 1 ? String.format("%d employees", count) :
                        String.format("%s %s (#%d)", e.getFirstName(), e.getLastName(), e.getEmpNum())
        );
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    private void removeSelectedEmployees() {
        int count = table.getSelectedRowCount();
        if (count == 0) return;
        if (confirmRemoveEmployee(count)) {
            List<Integer> toRemove = new ArrayList<>();
            for (var i : table.getSelectedRows()) {
                i = table.getRowSorter().convertRowIndexToModel(i);
                data.removeFromTable(ids.get(i));
                toRemove.add(ids.get(i));
            }
            ids.removeAll(toRemove);
            model.fireTableDataChanged();
            changed();
        }
    }

    private void changed() {
        unsavedChanges = true;
        updateTitle();
    }

    private void removeSingleEmployee(EmployeeInfo e) {
        if (!confirmRemoveEmployee(1)) return;
        int row = ids.indexOf(e.getEmpNum());
        data.removeFromTable(e.getEmpNum());
        ids.remove(row);
        model.fireTableRowsDeleted(row, row);
        changed();
    }

    private void onTableSelectionChanged() {
        int count = table.getSelectionModel().getSelectedItemsCount();
        ((CardLayout) detailsPane.getLayout()).show(detailsPane, count < 2 ? "SingleSelect" : "MultiSelect");
        if (count < 2) {
            employeeDetails.setEmployee(currentlySelectedEmployee());
        } else {
            multiSelectLabel.setText(String.format("%d Employees", count));
        }
        if (splitPane.getDividerLocation() > splitPane.getMaximumDividerLocation()) {
            splitPane.setDividerLocation(splitPane.getMaximumDividerLocation());
        }
    }

    public void addEmployee() {
        int employee = EmployeeDialog.newEmployeeDialog(this, data);
        if (employee != -1) {
            int row = -Collections.binarySearch(ids, employee) - 1;
            ids.add(row, employee);
            model.fireTableRowsInserted(row, row);
            selectEmployee(employee);
            changed();
        }
    }

    public void editEmployee(EmployeeInfo e) {
        var initalId = e.getEmpNum();
        var finalId = EmployeeDialog.modifyEmployeeDialog(this, data, e.getEmpNum());
        if (finalId == -1) return;
        int initialIndex = Collections.binarySearch(ids, initalId);
        if (initalId != initialIndex) {
            ids.remove(initialIndex);
            ids.add(-Collections.binarySearch(ids, finalId) - 1, finalId);
        }
        if (finalId == initalId) {
            model.fireTableRowsUpdated(initialIndex, initialIndex);
        } else {
            model.fireTableDataChanged();
        }
        selectEmployee(finalId);
        onTableSelectionChanged(); // refresh the details pane
        changed();
    }

    private void updateToolbar() {
        saveButton.setEnabled(file != null);
    }

    private boolean saveTo(Path path) {
        try (var writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            TSV.serialize(data, writer);
            unsavedChanges = false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Writing file failed with error: " + e.getMessage(),
                    "Error writing file",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        file = path;
        updateTitle();
        return true;
    }

    private boolean selectEmployee(int num) {
        int index = Collections.binarySearch(ids, num);
        if (index < 0) return false;
        index = table.getRowSorter().convertRowIndexToView(index);
        table.getSelectionModel().setSelectionInterval(index, index);
        table.scrollRectToVisible(table.getCellRect(index, 1, true));
        return true;
    }

    private void search() {
        try {
            int num = Integer.parseInt(searchField.getText().strip());
            if (!selectEmployee(num)) {
                JOptionPane.showMessageDialog(this, String.format("No employee with number %d was found.", num),
                        "Employee Not Found", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ignored) {
            JOptionPane.showMessageDialog(this, "Please enter a valid employee number.",
                    "Invalid Employee Number", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void newFile() {
        if (!confirmUnsaved()) return;
        data.clear();
        ids.clear();
        model.fireTableDataChanged();
        file = null;
        unsavedChanges = false;
        updateTitle();
        updateToolbar();
    }

    private void debug() {
        // Reset everything, for debugging purposes
        // Activate with ctrl + alt + \
        newFile();
        try {
            PREFS.clear();
        } catch (BackingStoreException ignored) {
        }
    }

    private JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("EMS Files (*.ems)", "ems"));
        chooser.setCurrentDirectory(new File(PREFS.get("fileChooserDirectory",
                chooser.getCurrentDirectory().toString())));
        chooser.setAcceptAllFileFilterUsed(true);
        return chooser;
    }

    public boolean saveAs() {
        try {
            JFileChooser chooser = createFileChooser();
            chooser.setDialogTitle("Save As");
            var status = chooser.showSaveDialog(this);
            if (status == JFileChooser.APPROVE_OPTION) {
                Path path = chooser.getSelectedFile().toPath();
                if (!path.endsWith(".ems")
                        && chooser.getFileFilter() instanceof FileNameExtensionFilter) {
                    path = path.getParent().resolve(path.getFileName() + "." +
                            ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions()[0]);
                }
                PREFS.put("fileChooserDirectory", path.toAbsolutePath().toString());
                return saveTo(path);
            }
        } catch (RuntimeException ignored) {
            JOptionPane.showMessageDialog(this,
                    "Could not save, please ensure path is valid.", "Invalid Path", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return false;
    }

    public boolean save() {
        return file != null && saveTo(file);
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean openFrom(Path path) {
        return openFrom(path, false);
    }

    private boolean openFrom(Path path, boolean silent) {
        data.clear();
        ids.clear();
        try (var reader = Files.newBufferedReader(path)) {
            ids.addAll(TSV.deserialize(data, reader));
            unsavedChanges = false;
        } catch (IOException | RuntimeException e) {
            if (silent) return false;
            JOptionPane.showMessageDialog(this,
                    "Reading file failed with error: " + e.getMessage(),
                    "Error reading file",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        file = path;
        Collections.sort(ids);
        model.fireTableDataChanged();
        updateTitle();
        updateToolbar();
        return true;
    }

    public void open() {
        if (!confirmUnsaved()) return;
        try {
            JFileChooser chooser = createFileChooser();
            var status = chooser.showOpenDialog(this);
            if (status == JFileChooser.APPROVE_OPTION) {
                var path = chooser.getSelectedFile().toPath();
                openFrom(path);
                PREFS.put("fileChooserDirectory", path.toAbsolutePath().toString());
            }
        } catch (RuntimeException ignored) {
            JOptionPane.showMessageDialog(this,
                    "Could not open, please ensure path is valid.", "Invalid Path", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel panel1;
    private JTable table;
    private JButton addEmployeeButton;
    private JPanel detailsPane;
    private JButton removeAllButton;
    private JButton saveButton;
    private JButton openButton;
    private JButton saveAsButton;
    private JSplitPane splitPane;
    private JTextField searchField;
    private JButton searchButton;
    private EmployeeDetails employeeDetails;
    private JLabel multiSelectLabel;
    private JButton newButton;

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
        splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(splitPane, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        splitPane.setLeftComponent(panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane1, gbc);
        table = new JTable();
        table.setFillsViewportHeight(true);
        scrollPane1.setViewportView(table);
        final JLabel label1 = new JLabel();
        label1.setText("Search for Employee #:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel2.add(label1, gbc);
        searchField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(searchField, gbc);
        searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/edit-find.png")));
        searchButton.setText("Search");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel2.add(searchButton, gbc);
        detailsPane = new JPanel();
        detailsPane.setLayout(new CardLayout(0, 0));
        splitPane.setRightComponent(detailsPane);
        employeeDetails = new EmployeeDetails();
        detailsPane.add(employeeDetails.$$$getRootComponent$$$(), "SingleSelect");
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        detailsPane.add(panel3, "MultiSelect");
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(separator1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel3.add(spacer1, gbc);
        multiSelectLabel = new JLabel();
        Font multiSelectLabelFont = this.$$$getFont$$$(null, Font.BOLD, 16, multiSelectLabel.getFont());
        if (multiSelectLabelFont != null) multiSelectLabel.setFont(multiSelectLabelFont);
        multiSelectLabel.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/system-users.png")));
        multiSelectLabel.setText("X Employees");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel3.add(multiSelectLabel, gbc);
        final JSeparator separator2 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(separator2, gbc);
        removeAllButton = new JButton();
        removeAllButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/edit-delete.png")));
        removeAllButton.setText("Remove All");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        panel3.add(removeAllButton, gbc);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setBorderPainted(false);
        toolBar1.setFloatable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(toolBar1, gbc);
        addEmployeeButton = new JButton();
        addEmployeeButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/list-add-user.png")));
        addEmployeeButton.setText("Add Employee");
        toolBar1.add(addEmployeeButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        newButton = new JButton();
        newButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/document-new.png")));
        newButton.setText("New");
        toolBar1.add(newButton);
        saveButton = new JButton();
        saveButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/document-save.png")));
        saveButton.setText("Save");
        toolBar1.add(saveButton);
        saveAsButton = new JButton();
        saveAsButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/document-save-as.png")));
        saveAsButton.setText("Save As...");
        toolBar1.add(saveAsButton);
        openButton = new JButton();
        openButton.setIcon(new ImageIcon(getClass().getResource("/owenwang/ems/document-open.png")));
        openButton.setText("Open...");
        toolBar1.add(openButton);
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

    private boolean confirmUnsaved() {
        if (unsavedChanges) {
            int result = JOptionPane.showConfirmDialog(this,
                    "The EMS has been modified. Do you want to save changes?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if (file != null) {
                    return save();
                } else {
                    return saveAs();
                }
            } else return result != JOptionPane.CANCEL_OPTION;
        }
        return true;
    }

    private void onWindowClose() {
        if (confirmUnsaved()) {
            if (getExtendedState() != MAXIMIZED_BOTH) {
                PREFS.putInt("windowWidth", getWidth());
                PREFS.putInt("windowHeight", getHeight());
            }
            PREFS.putInt("windowExtendedState", getExtendedState());
            PREFS.putInt("dividerLocation", splitPane.getDividerLocation());
            dispose();
            if (file != null) PREFS.put("file", file.toAbsolutePath().toString());
        }
    }
}
