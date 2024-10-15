/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.simple.inure.terminal;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;
import app.simple.inure.decorations.emulatorview.ColorScheme;
import app.simple.inure.decorations.emulatorview.TermSession;
import app.simple.inure.decorations.emulatorview.UpdateCallback;
import app.simple.inure.preferences.ShellPreferences;
import app.simple.inure.preferences.TerminalPreferences;
import app.simple.inure.terminal.util.TermSettings;

/**
 * A terminal session, consisting of a TerminalEmulator, a TranscriptScreen,
 * and the I/O streams used to talk to the process.
 */
public class GenericTermSession extends TermSession {
    //** Set to true to force into 80 x 24 for testing with vttest. */
    private static final boolean VTTEST_MODE = false;
    
    private static Field descriptorField;
    
    private final long createdAt;
    
    @SuppressWarnings ("unused")
    public static final int PROCESS_EXIT_FINISHES_SESSION = 0;
    @SuppressWarnings ("unused")
    public static final int PROCESS_EXIT_DISPLAYS_MESSAGE = 1;
    final ParcelFileDescriptor termParcelFileDescriptor;
    TermSettings termSettings;
    // A cookie which uniquely identifies this session.
    private String handle;
    
    private String processExitMessage;
    
    private final UpdateCallback UTF8ModeNotify = () -> setPtyUTF8Mode(getUTF8Mode());
    
    GenericTermSession(ParcelFileDescriptor termParcelFileDescriptor, TermSettings settings, boolean exitOnEOF) {
        super(exitOnEOF);
        
        this.termParcelFileDescriptor = termParcelFileDescriptor;
        
        this.createdAt = System.currentTimeMillis();
        
        updatePrefs(settings);
    }
    
    @SuppressWarnings ("JavaReflectionMemberAccess")
    @SuppressLint ("DiscouragedPrivateApi")
    private static void cacheDescField() throws NoSuchFieldException {
        if (descriptorField != null) {
            return;
        }
        
        descriptorField = FileDescriptor.class.getDeclaredField("descriptor");
        descriptorField.setAccessible(true);
    }
    
    @Override
    public void initializeEmulator(int columns, int rows) {
        if (VTTEST_MODE) {
            columns = 80;
            rows = 24;
        }
        super.initializeEmulator(columns, rows);
        
        setPtyUTF8Mode(getUTF8Mode());
        setUTF8ModeUpdateCallback(UTF8ModeNotify);
    }
    
    @Override
    public void updateSize(int columns, int rows) {
        if (VTTEST_MODE) {
            columns = 80;
            rows = 24;
        }
        // Inform the attached pty of our new size:
        setPtyWindowSize(rows, columns, 0, 0);
        super.updateSize(columns, rows);
    }
    
    /* XXX We should really get this ourselves from the resource bundle, but
       we cannot hold a context */
    public void setProcessExitMessage(String message) {
        processExitMessage = message;
    }
    
    @Override
    protected void onProcessExit() {
        if (ShellPreferences.INSTANCE.getCloseWindowOnExitState()) {
            finish();
        } else if (processExitMessage != null) {
            byte[] msg = ("\r\n[" + processExitMessage + "]").getBytes(StandardCharsets.UTF_8);
            appendToEmulator(msg, 0, msg.length);
            notifyUpdate();
        }
    }
    
    @SuppressLint ("ObsoleteSdkInt")
    private static int getIntFd(ParcelFileDescriptor parcelFd) throws IOException {
        if (Build.VERSION.SDK_INT >= 12) {
            return FdHelperHoneycomb.getFd(parcelFd);
        } else {
            try {
                cacheDescField();
                
                return descriptorField.getInt(parcelFd.getFileDescriptor());
            } catch (Exception e) {
                throw new IOException("Unable to obtain file descriptor on this OS version: " + e.getMessage());
            }
        }
    }
    
    public void updatePrefs(TermSettings settings) {
        termSettings = settings;
        setColorScheme(new ColorScheme(settings.getColorScheme()));
        setDefaultUTF8Mode(TerminalPreferences.INSTANCE.getUTF8State());
    }
    
    @Override
    public void finish() {
        try {
            termParcelFileDescriptor.close();
        } catch (IOException e) {
            // Okay, we tried.
        }
        
        super.finish();
    }
    
    /**
     * Gets the terminal session's title.  Unlike the superclass' getTitle(),
     * if the title is null or an empty string, the provided default title will
     * be returned instead.
     *
     * @param defaultTitle The default title to use if this session's title is
     *                     unset or an empty string.
     */
    public String getTitle(String defaultTitle) {
        String title = getTitle();
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            return defaultTitle;
        }
    }
    
    public String getHandle() {
        return handle;
    }
    
    public void setHandle(String handle) {
        if (this.handle != null) {
            throw new IllegalStateException("Cannot change handle once set");
        }
        this.handle = handle;
    }
    
    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + createdAt + ',' + handle + ')';
    }
    
    /**
     * @return true, if failing to operate on file descriptor deserves an exception (never the case for ATE own shell)
     */
    boolean isFailFast() {
        return false;
    }
    
    /**
     * Set the widow size for a given pty. Allows programs
     * connected to the pty learn how large their screen is.
     */
    @SuppressWarnings ("SameParameterValue")
    void setPtyWindowSize(int row, int col, int xPixel, int yPixel) {
        // If the tty goes away too quickly, this may get called after it's descriptor is closed
        if (!termParcelFileDescriptor.getFileDescriptor().valid()) {
            return;
        }
        
        try {
            Exec.setPtyWindowSizeInternal(getIntFd(termParcelFileDescriptor), row, col, xPixel, yPixel);
        } catch (IOException e) {
            Log.e("exec", "Failed to set window size: " + e.getMessage());
            
            if (isFailFast()) {
                throw new IllegalStateException(e);
            }
        }
    }
    
    /**
     * Set or clear UTF-8 mode for a given pty.  Used by the terminal driver
     * to implement correct erase behavior in cooked mode (Linux >= 2.6.4).
     */
    void setPtyUTF8Mode(boolean utf8Mode) {
        // If the tty goes away too quickly, this may get called after it's descriptor is closed
        if (!termParcelFileDescriptor.getFileDescriptor().valid()) {
            return;
        }
        
        try {
            Exec.setPtyUTF8ModeInternal(getIntFd(termParcelFileDescriptor), utf8Mode);
        } catch (IOException e) {
            Log.e("exec", "Failed to set UTF mode: " + e.getMessage());
    
            if (isFailFast()) {
                throw new IllegalStateException(e);
            }
        }
    }
}
