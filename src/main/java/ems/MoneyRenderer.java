package ems;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MoneyRenderer extends DefaultTableCellRenderer {
    public MoneyRenderer() {
        super();
        setHorizontalAlignment(JLabel.RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : Constants.MONEY_FORMAT.format(value));
    }

}