package pl.com.ids;

public class SimpleDebugger implements Debugger {
    @Override
    public void debug(String msg) {
        System.out.println(msg);
    }
}