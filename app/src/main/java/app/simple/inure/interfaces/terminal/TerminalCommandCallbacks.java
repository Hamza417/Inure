package app.simple.inure.interfaces.terminal;

import android.view.View;

import org.jetbrains.annotations.NotNull;

import app.simple.inure.models.TerminalCommand;

public interface TerminalCommandCallbacks {
    void onCommandClicked(TerminalCommand terminalCommand);
    
    void onCommandLongClicked(TerminalCommand terminalCommand, @NotNull View view, int minus);
}
