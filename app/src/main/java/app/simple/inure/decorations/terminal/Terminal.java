/*
 * Copyright (C) 2013 The Android Open Source Project
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

package app.simple.inure.decorations.terminal;

import android.graphics.Color;

/**
 * Single terminal session backed by a pseudo terminal on the local device.
 */
public class Terminal {
    public static final String TAG = "Terminal";
    private static int sNumber = 0;
    
    static {
        System.loadLibrary("_inure_terminal");
    }
    
    public final int key;
    private final long mNativePtr;
    private final Thread mThread;
    private final String mTitle;
    private TerminalClient mClient;
    private boolean mCursorVisible;
    private int mCursorRow;
    private int mCursorCol;
    
    public Terminal() {
        TerminalCallbacks mCallbacks = new TerminalCallbacks() {
            @Override
            public int damage(int startRow, int endRow, int startCol, int endCol) {
                if (mClient != null) {
                    mClient.onDamage(startRow, endRow, startCol, endCol);
                }
                return 1;
            }
            
            @Override
            public int moveRect(int destStartRow, int destEndRow, int destStartCol, int destEndCol,
                    int srcStartRow, int srcEndRow, int srcStartCol, int srcEndCol) {
                if (mClient != null) {
                    mClient.onMoveRect(destStartRow, destEndRow, destStartCol, destEndCol, srcStartRow,
                            srcEndRow, srcStartCol, srcEndCol);
                }
                return 1;
            }
            
            @Override
            public int moveCursor(int posRow, int posCol, int oldPosRow, int oldPosCol, int visible) {
                mCursorVisible = (visible != 0);
                mCursorRow = posRow;
                mCursorCol = posCol;
                if (mClient != null) {
                    mClient.onMoveCursor(posRow, posCol, oldPosRow, oldPosCol, visible);
                }
                return 1;
            }
            
            @Override
            public int bell() {
                if (mClient != null) {
                    mClient.onBell();
                }
                return 1;
            }
        };
    
        mNativePtr = nativeInit(mCallbacks);
        key = sNumber++;
        mTitle = TAG + " " + key;
    
        mThread = new Thread(mTitle) {
            @Override
            public void run() {
                nativeRun(mNativePtr);
            }
        };
    }

    private static native long nativeInit(TerminalCallbacks callbacks);
    
    private static native int nativeDestroy(long ptr);
    
    private static native int nativeRun(long ptr);
    
    private static native int nativeResize(long ptr, int rows, int cols, int scrollRows);
    
    private static native int nativeGetCellRun(long ptr, int row, int col, CellRun run);
    
    private static native int nativeGetRows(long ptr);
    
    private static native int nativeGetCols(long ptr);
    
    private static native int nativeGetScrollRows(long ptr);
    
    private static native boolean nativeDispatchKey(long ptr, int modifiers, int key);
    
    private static native boolean nativeDispatchCharacter(long ptr, int modifiers, int character);
    
    /**
     * Start thread which internally forks and manages the pseudo terminal.
     */
    public void start() {
        mThread.start();
    }
    
    public void destroy() {
        if (nativeDestroy(mNativePtr) != 0) {
            throw new IllegalStateException("destroy failed");
        }
    }
    
    public void setClient(TerminalClient client) {
        mClient = client;
    }
    
    public void resize(int rows, int cols, int scrollRows) {
        if (nativeResize(mNativePtr, rows, cols, scrollRows) != 0) {
            throw new IllegalStateException("resize failed");
        }
    }
    
    public int getRows() {
        return nativeGetRows(mNativePtr);
    }
    
    public int getCols() {
        return nativeGetCols(mNativePtr);
    }
    
    public int getScrollRows() {
        return nativeGetScrollRows(mNativePtr);
    }
    
    public void getCellRun(int row, int col, CellRun run) {
        if (nativeGetCellRun(mNativePtr, row, col, run) != 0) {
            throw new IllegalStateException("getCell failed");
        }
    }
    
    public boolean getCursorVisible() {
        return mCursorVisible;
    }
    
    public int getCursorRow() {
        return mCursorRow;
    }
    
    public int getCursorCol() {
        return mCursorCol;
    }
    
    public String getTitle() {
        // TODO: hook up to title passed through termprop
        return mTitle;
    }
    
    public boolean dispatchKey(int modifiers, int key) {
        return nativeDispatchKey(mNativePtr, modifiers, key);
    }
    
    public boolean dispatchCharacter(int modifiers, int character) {
        return nativeDispatchCharacter(mNativePtr, modifiers, character);
    }
    
    // NOTE: clients must not call back into terminal while handling a callback,
    // since native mutex isn't reentrant.
    public interface TerminalClient {
        void onDamage(int startRow, int endRow, int startCol, int endCol);
        
        void onMoveRect(int destStartRow, int destEndRow, int destStartCol, int destEndCol,
                int srcStartRow, int srcEndRow, int srcStartCol, int srcEndCol);
        
        void onMoveCursor(int posRow, int posCol, int oldPosRow, int oldPosCol, int visible);
        
        void onBell();
    }
    
    /**
     * Represents a run of one or more {@code VTermScreenCell} which all have
     * the same formatting.
     */
    public static class CellRun {
        char[] data;
        int dataSize;
        int colSize;
        
        boolean bold;
        int underline;
        boolean blink;
        boolean reverse;
        boolean strike;
        int font;
        
        int fg = Color.CYAN;
        int bg = Color.TRANSPARENT;
    }
}
