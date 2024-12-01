package ems;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class EmployeeTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = { "Employee #", "First Name", "Last Name", "Type", "Gross Income", "Net Income" };
    private final MyHashTable data;
    private final List<Integer> ids;
    public EmployeeTableModel(MyHashTable data, List<Integer> ids) {
        this.data = data;
        this.ids = ids;
    }
    @Override
    public int getRowCount() {
        return ids.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    public EmployeeInfo employeeAtIndex(int i) {
        return data.getFromTable(ids.get(i));
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EmployeeInfo e = employeeAtIndex(rowIndex);
        switch (columnIndex) {
            case 0: return e.empNum;
            case 1: return e.firstName;
            case 2: return e.lastName;
            case 3: return e instanceof FTE ? "Full-time" : "Part-time";
            case 4: return e.calcGrossAnnualIncome();
            case 5: return e.calcNetAnnualIncome();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            case 4: case 5: return Double.class;
            default: return super.getColumnClass(columnIndex);
        }
    }



    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }
}
