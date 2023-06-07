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

package app.simple.inure.decorations.emulatorview;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * A terminal session, consisting of a VT100 terminal emulator and its
 * input and output streams.
 * <p>
 * You need to supply an {@link InputStream} and {@link OutputStream} to
 * provide input and output to the terminal.  For a locally running
 * program, these would typically point to a tty; for a telnet program
 * they might point to a network socket.  Reader and writer threads will be
 * spawned to do I/O to these streams.  All other operations, including
 * processing of input and output in {@link #processInput processInput} and
 * {@link #write(byte[], int, int) write}, will be performed on the main thread.
 * <p>
 * Call {@link #setTermIn} and {@link #setTermOut} to connect the input and
 * output streams to the emulator.  When all of your initialization is
 * complete, your initial screen size is known, and you're ready to
 * start VT100 emulation, call {@link #initializeEmulator} or {@link
 * #updateSize} with the number of rows and columns the terminal should
 * initially have.  (If you attach the session to an {@link EmulatorView},
 * the view will take care of setting the screen size and initializing the
 * emulator for you.)
 * <p>
 * When you're done with the session, you should call {@link #finish} on it.
 * This frees emulator data from memory, stops the reader and writer threads,
 * and closes the attached I/O streams.
 */
public class TermSession {
    private final Thread readerThread;
    private final ByteQueue byteQueue;
    private final byte[] receiverBuffer;
    private final Thread writerThread;
    private final ByteQueue writeQueue;
    private final CharBuffer writeCharBuffer;
    private final ByteBuffer writeByteBuffer;
    private final CharsetEncoder utf8Encoder;
    private TermKeyListener keyListener;
    private ColorScheme colorScheme = BaseTextRenderer.defaultColorScheme;
    private UpdateCallback notify;
    private OutputStream termOut;
    private InputStream termIn;
    private String title;
    private TranscriptScreen transcriptScreen;
    private TerminalEmulator terminalEmulator;
    private boolean defaultUTF8Mode;
    private Handler writerHandler;
    private FinishCallback finishCallback;
    
    // Number of rows in the transcript
    private static final int TRANSCRIPT_ROWS = 10000;
    
    private static final int NEW_INPUT = 1;
    private static final int NEW_OUTPUT = 2;
    private static final int FINISH = 3;
    private static final int EOF = 4;
    
    /**
     * Callback to be invoked when a {@link TermSession} finishes.
     *
     * @see TermSession#setUpdateCallback
     */
    public interface FinishCallback {
        /**
         * Callback function to be invoked when a {@link TermSession} finishes.
         *
         * @param session The <code>TermSession</code> which has finished.
         */
        void onSessionFinish(TermSession session);
    }
    
    private boolean isRunning = false;
    @SuppressLint ("HandlerLeak")
    private final Handler messageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (!isRunning) {
                return;
            }
            if (msg.what == NEW_INPUT) {
                readFromProcess();
            } else if (msg.what == EOF) {
                new Handler(Looper.getMainLooper()).post(() -> onProcessExit());
            }
        }
    };
    
    public TermSession(final boolean exitOnEOF) {
        writeCharBuffer = CharBuffer.allocate(2);
        writeByteBuffer = ByteBuffer.allocate(4);
        utf8Encoder = StandardCharsets.UTF_8.newEncoder();
        utf8Encoder.onMalformedInput(CodingErrorAction.REPLACE);
        utf8Encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    
        receiverBuffer = new byte[4 * 1024];
        byteQueue = new ByteQueue(4 * 1024);
    
        readerThread = new Thread() {
            private final byte[] buffer = new byte[4096];
        
            @Override
            public void run() {
                try {
                    while (true) {
                        int read = termIn.read(buffer);
                        if (read == -1) {
                            // EOF -- process exited
                            break;
                        }
                        int offset = 0;
                        while (read > 0) {
                            int written = byteQueue.write(buffer,
                                    offset, read);
                            offset += written;
                            read -= written;
                            messageHandler.sendMessage(
                                    messageHandler.obtainMessage(NEW_INPUT));
                        }
                    }
                } catch (IOException | InterruptedException ignored) {
                }
            
                if (exitOnEOF) {
                    messageHandler.sendMessage(messageHandler.obtainMessage(EOF));
                }
            }
        };
        readerThread.setName("TermSession input reader");
    
        writeQueue = new ByteQueue(4096);
        writerThread = new Thread() {
            private final byte[] mBuffer = new byte[4096];
        
            @Override
            public void run() {
                Looper.prepare();
            
                writerHandler = new Handler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == NEW_OUTPUT) {
                            writeToOutput();
                        } else if (msg.what == FINISH) {
                            Looper.myLooper().quit();
                        }
                    }
                };
                
                // Drain anything in the queue from before we started
                writeToOutput();
                
                Looper.loop();
            }
            
            private void writeToOutput() {
                ByteQueue writeQueue = TermSession.this.writeQueue;
                byte[] buffer = mBuffer;
                OutputStream termOut = TermSession.this.termOut;
                
                int bytesAvailable = writeQueue.getBytesAvailable();
                int bytesToWrite = Math.min(bytesAvailable, buffer.length);
                
                if (bytesToWrite == 0) {
                    return;
                }
    
                try {
                    writeQueue.read(buffer, 0, bytesToWrite);
                    termOut.write(buffer, 0, bytesToWrite);
                    termOut.flush();
                } catch (IOException | InterruptedException e) {
                    // Ignore exception
                    // We don't really care if the receiver isn't listening.
                    // We just make a best effort to answer the query.
                    e.printStackTrace();
                }
            }
        };
        writerThread.setName("TermSession output writer");
    }
    
    private UpdateCallback mTitleChangedListener;
    
    public TermSession() {
        this(false);
    }
    
    public void setKeyListener(TermKeyListener l) {
        keyListener = l;
    }
    
    protected void onProcessExit() {
        finish();
    }
    
    /**
     * Set the terminal emulator's window size and start terminal emulation.
     *
     * @param columns The number of columns in the terminal window.
     * @param rows    The number of rows in the terminal window.
     */
    public void initializeEmulator(int columns, int rows) {
        transcriptScreen = new TranscriptScreen(columns, TRANSCRIPT_ROWS, rows, colorScheme);
        terminalEmulator = new TerminalEmulator(this, transcriptScreen, columns, rows, colorScheme);
        terminalEmulator.setDefaultUTF8Mode(defaultUTF8Mode);
        terminalEmulator.setKeyListener(keyListener);
    
        isRunning = true;
        readerThread.start();
        writerThread.start();
    }
    
    /**
     * Write data to the terminal output.  The written data will be consumed by
     * the emulation client as input.
     * <p>
     * <code>write</code> itself runs on the main thread.  The default
     * implementation writes the data into a circular buffer and signals the
     * writer thread to copy it from there to the {@link OutputStream}.
     * <p>
     * Subclasses may override this method to modify the output before writing
     * it to the stream, but implementations in derived classes should call
     * through to this method to do the actual writing.
     *
     * @param data   An array of bytes to write to the terminal.
     * @param offset The offset into the array at which the data starts.
     * @param count  The number of bytes to be written.
     */
    public void write(byte[] data, int offset, int count) {
        try {
            while (count > 0) {
                int written = writeQueue.write(data, offset, count);
                offset += written;
                count -= written;
                notifyNewOutput();
            }
        } catch (InterruptedException ignored) {
        }
    }
    
    /**
     * Write the UTF-8 representation of a String to the terminal output.  The
     * written data will be consumed by the emulation client as input.
     * <p>
     * This implementation encodes the String and then calls
     * {@link #write(byte[], int, int)} to do the actual writing.  It should
     * therefore usually be unnecessary to override this method; override
     * {@link #write(byte[], int, int)} instead.
     *
     * @param data The String to write to the terminal.
     */
    public void write(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        write(bytes, 0, bytes.length);
    }
    
    /**
     * Write the UTF-8 representation of a single Unicode code point to the
     * terminal output.  The written data will be consumed by the emulation
     * client as input.
     * <p>
     * This implementation encodes the code point and then calls
     * {@link #write(byte[], int, int)} to do the actual writing.  It should
     * therefore usually be unnecessary to override this method; override
     * {@link #write(byte[], int, int)} instead.
     *
     * @param codePoint The Unicode code point to write to the terminal.
     */
    public void write(int codePoint) {
        ByteBuffer byteBuf = writeByteBuffer;
        if (codePoint < 128) {
            // Fast path for ASCII characters
            byte[] buf = byteBuf.array();
            buf[0] = (byte) codePoint;
            write(buf, 0, 1);
            return;
        }
    
        CharBuffer charBuf = writeCharBuffer;
        CharsetEncoder encoder = utf8Encoder;
    
        charBuf.clear();
        byteBuf.clear();
        //noinspection ResultOfMethodCallIgnored
        Character.toChars(codePoint, charBuf.array(), 0);
        encoder.reset();
        encoder.encode(charBuf, byteBuf, true);
        encoder.flush(byteBuf);
        write(byteBuf.array(), 0, byteBuf.position() - 1);
    }
    
    /* Notify the writer thread that there's new output waiting */
    private void notifyNewOutput() {
        Handler writerHandler = this.writerHandler;
        if (writerHandler == null) {
            /* Writer thread isn't started -- will pick up data once it does */
            return;
        }
        writerHandler.sendEmptyMessage(NEW_OUTPUT);
    }
    
    /**
     * Get the {@link OutputStream} associated with this session.
     *
     * @return This session's {@link OutputStream}.
     */
    @SuppressWarnings ("unused")
    public OutputStream getTermOut() {
        return termOut;
    }
    
    /**
     * Set the {@link OutputStream} associated with this session.
     *
     * @param termOut This session's {@link OutputStream}.
     */
    public void setTermOut(OutputStream termOut) {
        this.termOut = termOut;
    }
    
    /**
     * Get the {@link InputStream} associated with this session.
     *
     * @return This session's {@link InputStream}.
     */
    @SuppressWarnings ("unused")
    public InputStream getTermIn() {
        return termIn;
    }
    
    /**
     * Set the {@link InputStream} associated with this session.
     *
     * @param termIn This session's {@link InputStream}.
     */
    public void setTermIn(InputStream termIn) {
        this.termIn = termIn;
    }
    
    /**
     * @return Whether the terminal emulation is currently running.
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    @SuppressWarnings ("unused")
    TranscriptScreen getTranscriptScreen() {
        return transcriptScreen;
    }
    
    TerminalEmulator getTerminalEmulator() {
        return terminalEmulator;
    }
    
    /**
     * Set an {@link UpdateCallback} to be invoked when the terminal emulator's
     * screen is changed.
     *
     * @param notify The {@link UpdateCallback} to be invoked on changes.
     */
    public void setUpdateCallback(UpdateCallback notify) {
        this.notify = notify;
    }
    
    /**
     * Notify the {@link UpdateCallback} registered by {@link
     * #setUpdateCallback setUpdateCallback} that the screen has changed.
     */
    protected void notifyUpdate() {
        if (notify != null) {
            notify.onUpdate();
        }
    }
    
    /**
     * Get the terminal session's title (may be null).
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Change the terminal session's title.
     */
    public void setTitle(String title) {
        this.title = title;
        notifyTitleChanged();
    }
    
    /**
     * Set an {@link UpdateCallback} to be invoked when the terminal emulator's
     * title is changed.
     *
     * @param listener The {@link UpdateCallback} to be invoked on changes.
     */
    public void setTitleChangedListener(UpdateCallback listener) {
        mTitleChangedListener = listener;
    }
    
    /**
     * Notify the UpdateCallback registered for title changes, if any, that the
     * terminal session's title has changed.
     */
    protected void notifyTitleChanged() {
        UpdateCallback listener = mTitleChangedListener;
        if (listener != null) {
            listener.onUpdate();
        }
    }
    
    /**
     * Change the terminal's window size.  Will call {@link #initializeEmulator}
     * if the emulator is not yet running.
     * <p>
     * You should override this method if your application needs to be notified
     * when the screen size changes (for example, if you need to issue
     * <code>TIOCSWINSZ</code> to a tty to adjust the window size).  <em>If you
     * do override this method, you must call through to the superclass
     * implementation.</em>
     *
     * @param columns The number of columns in the terminal window.
     * @param rows    The number of rows in the terminal window.
     */
    public void updateSize(int columns, int rows) {
        if (terminalEmulator == null) {
            initializeEmulator(columns, rows);
        } else {
            terminalEmulator.updateSize(columns, rows);
        }
    }
    
    /**
     * Retrieve the terminal's screen and scrollback buffer.
     *
     * @return A {@link String} containing the contents of the screen and
     * scrollback buffer.
     */
    public String getTranscriptText() {
        return transcriptScreen.getTranscriptText();
    }
    
    /**
     * Look for new input from the ptty, send it to the terminal emulator.
     */
    private void readFromProcess() {
        int bytesAvailable = byteQueue.getBytesAvailable();
        int bytesToRead = Math.min(bytesAvailable, receiverBuffer.length);
        int bytesRead;
        try {
            bytesRead = byteQueue.read(receiverBuffer, 0, bytesToRead);
        } catch (InterruptedException e) {
            return;
        }
    
        // Give subclasses a chance to process the read data
        processInput(receiverBuffer, 0, bytesRead);
        notifyUpdate();
    }
    
    /**
     * Process input and send it to the terminal emulator.  This method is
     * invoked on the main thread whenever new data is read from the
     * InputStream.
     * <p>
     * The default implementation sends the data straight to the terminal
     * emulator without modifying it in any way.  Subclasses can override it to
     * modify the data before giving it to the terminal.
     *
     * @param data   A byte array containing the data read.
     * @param offset The offset into the buffer where the read data begins.
     * @param count  The number of bytes read.
     */
    protected void processInput(byte[] data, int offset, int count) {
        terminalEmulator.append(data, offset, count);
    }
    
    /**
     * Write something directly to the terminal emulator input, bypassing the
     * emulation client, the session's {@link InputStream}, and any processing
     * being done by {@link #processInput processInput}.
     *
     * @param data   The data to be written to the terminal.
     * @param offset The starting offset into the buffer of the data.
     * @param count  The length of the data to be written.
     */
    protected final void appendToEmulator(byte[] data, @SuppressWarnings ("SameParameterValue") int offset, int count) {
        terminalEmulator.append(data, offset, count);
    }
    
    /**
     * Set the terminal emulator's color scheme (default colors).
     *
     * @param scheme The {@link ColorScheme} to be used (use null for the
     *               default scheme).
     */
    public void setColorScheme(ColorScheme scheme) {
        if (scheme == null) {
            scheme = BaseTextRenderer.defaultColorScheme;
        }
        colorScheme = scheme;
        if (terminalEmulator == null) {
            return;
        }
        terminalEmulator.setColorScheme(scheme);
    }
    
    /**
     * Set whether the terminal emulator should be in UTF-8 mode by default.
     * <p>
     * In UTF-8 mode, the terminal will handle UTF-8 sequences, allowing the
     * display of text in most of the world's languages, but applications must
     * encode C1 control characters and graphics drawing characters as the
     * corresponding UTF-8 sequences.
     *
     * @param utf8ByDefault Whether the terminal emulator should be in UTF-8
     *                      mode by default.
     */
    public void setDefaultUTF8Mode(boolean utf8ByDefault) {
        defaultUTF8Mode = utf8ByDefault;
        if (terminalEmulator == null) {
            return;
        }
        terminalEmulator.setDefaultUTF8Mode(utf8ByDefault);
    }
    
    /**
     * Get whether the terminal emulator is currently in UTF-8 mode.
     *
     * @return Whether the emulator is currently in UTF-8 mode.
     */
    public boolean getUTF8Mode() {
        if (terminalEmulator == null) {
            return defaultUTF8Mode;
        } else {
            return terminalEmulator.getUTF8Mode();
        }
    }
    
    /**
     * Set an {@link UpdateCallback} to be invoked when the terminal emulator
     * goes into or out of UTF-8 mode.
     *
     * @param utf8ModeNotify The {@link UpdateCallback} to be invoked.
     */
    public void setUTF8ModeUpdateCallback(UpdateCallback utf8ModeNotify) {
        if (terminalEmulator != null) {
            terminalEmulator.setUTF8ModeUpdateCallback(utf8ModeNotify);
        }
    }
    
    /**
     * Reset the terminal emulator's state.
     */
    public void reset() {
        terminalEmulator.reset();
        notifyUpdate();
    }
    
    /**
     * Set a {@link FinishCallback} to be invoked once this terminal session is
     * finished.
     *
     * @param callback The {@link FinishCallback} to be invoked on finish.
     */
    public void setFinishCallback(FinishCallback callback) {
        finishCallback = callback;
    }
    
    public void removeFinishCallback() {
        finishCallback = null;
    }
    
    /**
     * Finish this terminal session.  Frees resources used by the terminal
     * emulator and closes the attached <code>InputStream</code> and
     * <code>OutputStream</code>.
     */
    public void finish() {
        try {
            isRunning = false;
            try {
                terminalEmulator.finish();
            } catch (Exception e) {
                // throw new RuntimeException(e);
                // Ignore any exceptions that occur during finish
            }
            if (transcriptScreen != null) {
                transcriptScreen.finish();
            }
        
            // Stop the reader and writer threads, and close the I/O streams
            if (writerHandler != null) {
                writerHandler.sendEmptyMessage(FINISH);
            }
        
            try {
                termIn.close();
                termOut.close();
            } catch (IOException e) {
                // We don't care if this fails
            } catch (NullPointerException ignored) {
            }
        
            if (finishCallback != null) {
                finishCallback.onSessionFinish(this);
            }
        } catch (Exception ignored) {
            // Ignore any exceptions that occur during finish
        }
    }
}
