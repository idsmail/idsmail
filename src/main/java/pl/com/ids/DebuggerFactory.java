package pl.com.ids;

import java.util.Arrays;

public class DebuggerFactory {
    static Debugger getDebugger(String[] args) {
        Debugger debugger;
        if (Arrays.asList(args).contains("-q")) {
            debugger = new SwingDebugger();
        } else {
            debugger = new SimpleDebugger();
        }
        return debugger;
    }
}