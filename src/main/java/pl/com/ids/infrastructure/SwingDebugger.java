package pl.com.ids.infrastructure;

import pl.com.ids.infrastructure.Debugger;

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
