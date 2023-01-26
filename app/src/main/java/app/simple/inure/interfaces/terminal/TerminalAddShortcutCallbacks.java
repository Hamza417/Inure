package app.simple.inure.interfaces.terminal;

public interface TerminalAddShortcutCallbacks {
    void onShortcutAdded(String path, String args, String label, boolean quoteForBash);
}
