package pl.com.ids.infrastructure;

import java.util.Arrays;

public class DebuggerFactory {
    public static Debugger getDebugger(String[] args) {
        Debugger debugger;
        if (Arrays.asList(args).contains("-q")) {
            debugger = new SwingDebugger();
        } else {
            debugger = new SimpleDebugger();
        }
        return debugger;
    }
}