package owenwang.ems.input;

import owenwang.ems.Constants;

import javax.swing.*;

public class NumericVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        try {
            Double.parseDouble(tf.getText().strip());
            tf.setBackground(UIManager.getColor("TextField.background"));
            return true;
        }
        catch (NumberFormatException e) {
            tf.setBackground(Constants.ERROR_BACKGROUND);
            return false;
        }
    }

    @Override
    public boolean shouldYieldFocus(JComponent source, JComponent target) {
        verify(source);
        return true;
    }
}
