package acceptance;

import pl.com.ids.infrastructure.Debugger;

public class NoOpDebugger implements Debugger {
    @Override
    public void debug(String msg) {

    }
}
