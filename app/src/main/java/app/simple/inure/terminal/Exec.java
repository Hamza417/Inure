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

import java.io.IOException;

/**
 * Utility methods for managing a pty file descriptor.
 */
public class Exec {
    // Warning: bump the library revision, when an incompatible change happens
    static {
        System.loadLibrary("inure_terminal_emulator");
    }
    
    static native void setPtyWindowSizeInternal(int fd, int row, int col, int xpixel, int ypixel) throws IOException;
    
    static native void setPtyUTF8ModeInternal(int fd, boolean utf8Mode) throws IOException;
}

