package pl.com.ids;

import javax.swing.*;

public class SwingDebugger implements Debugger {
    @Override
    public void debug(String msg) {
        JOptionPane.showMessageDialog(null,
                msg,
                "Error/Blad",
                JOptionPane.ERROR_MESSAGE);
    }
}
