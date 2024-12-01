package ems.input;

import ems.Constants;

import javax.swing.*;

public class NonEmptyVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
        JTextField tf = (JTextField) input;
        tf.setBackground(tf.getText().isBlank() ? Constants.ERROR_BACKGROUND : UIManager.getColor("TextField.background"));
        return !tf.getText().isBlank();
    }

    @Override
    public boolean shouldYieldFocus(JComponent source, JComponent target) {
        verify(source);
        return true;
    }
}
