package app.simple.inure.decorations.emulatorview.compat;

public interface ClipboardManagerCompat {
    CharSequence getText();
    
    boolean hasText();
    
    void setText(CharSequence text);
}
