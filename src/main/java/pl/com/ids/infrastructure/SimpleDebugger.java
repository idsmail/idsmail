package pl.com.ids.infrastructure;

import pl.com.ids.infrastructure.Debugger;

public class SimpleDebugger implements Debugger {
    @Override
    public void debug(String msg) {
        System.out.println(msg);
    }
}