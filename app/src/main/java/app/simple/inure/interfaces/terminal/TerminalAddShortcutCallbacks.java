package app.simple.inure.interfaces.terminal;

public interface TerminalAddShortcutCallbacks {
    default void onShortcutAdded(String path, String args, String label, boolean quoteForBash) {
    
    }
    
    default void onCreateShortcut(String path, String args, String label, String description) {
    
    }
}
