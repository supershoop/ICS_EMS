package owenwang.ems;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class MoneyRenderer extends DefaultTableCellRenderer {
    public MoneyRenderer() {
        super();
        setHorizontalAlignment(JLabel.RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : Constants.MONEY_FORMAT.format(value));
    }

}