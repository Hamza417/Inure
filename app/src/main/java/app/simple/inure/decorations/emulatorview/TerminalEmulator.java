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

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Renders text into a screen. Contains all the terminal-specific knowledge and
 * state. Emulates a subset of the X Window System xterm terminal, which in turn
 * is an emulator for a subset of the Digital Equipment Corporation vt100
 * terminal. Missing functionality: text attributes (bold, underline, reverse
 * video, color) alternate screen cursor key and keypad escape sequences.
 */
class TerminalEmulator {
    
    private final static int CHAR_SET_ALT_SPECIAL_GRAPHICS = 4;
    /**
     * Special graphics character set
     * <p>
     * Read more: <a href="https://en.wikipedia.org/wiki/DEC_Special_Graphics">https://en.wikipedia.org/wiki/DEC_Special_Graphics</a>
     */
    private static final char[] specialGraphicsCharMap = new char[128];
    static {
        for (char i = 0; i < 128; ++i) {
            specialGraphicsCharMap[i] = i;
        }
        
        specialGraphicsCharMap['_'] = ' ';       // Blank
        specialGraphicsCharMap['b'] = 0x2409;    // Tab
        specialGraphicsCharMap['c'] = 0x240C;    // Form feed
        specialGraphicsCharMap['d'] = 0x240D;    // Carriage return
        specialGraphicsCharMap['e'] = 0x240A;    // Line feed
        specialGraphicsCharMap['h'] = 0x2424;    // New line
        specialGraphicsCharMap['i'] = 0x240B;    // Vertical tab/"lantern"
        specialGraphicsCharMap['}'] = 0x00A3;    // Pound sterling symbol
        specialGraphicsCharMap['f'] = 0x00B0;    // Degree symbol
        specialGraphicsCharMap['`'] = 0x2B25;    // Diamond
        specialGraphicsCharMap['~'] = 0x2022;    // Bullet point
        specialGraphicsCharMap['y'] = 0x2264;    // Less-than-or-equals sign (<=)
        specialGraphicsCharMap['|'] = 0x2260;    // Not equals sign (!=)
        specialGraphicsCharMap['z'] = 0x2265;    // Greater-than-or-equals sign (>=)
        specialGraphicsCharMap['g'] = 0x00B1;    // Plus-or-minus sign (+/-)
        specialGraphicsCharMap['{'] = 0x03C0;    // Lowercase Greek letter pi
        specialGraphicsCharMap['.'] = 0x25BC;    // Down arrow
        specialGraphicsCharMap[','] = 0x25C0;    // Left arrow
        specialGraphicsCharMap['+'] = 0x25B6;    // Right arrow
        specialGraphicsCharMap['-'] = 0x25B2;    // Up arrow
        // specialGraphicsCharMap['h'] = '#';       // Board of squares // TODO this is not right
        specialGraphicsCharMap['a'] = 0x2592;    // Checkerboard
        specialGraphicsCharMap['0'] = 0x2588;    // Solid block
        specialGraphicsCharMap['q'] = 0x2500;    // Horizontal line (box drawing)
        specialGraphicsCharMap['x'] = 0x2502;    // Vertical line (box drawing)
        specialGraphicsCharMap['m'] = 0x2514;    // Lower left hand corner (box drawing)
        specialGraphicsCharMap['j'] = 0x2518;    // Lower right hand corner (box drawing)
        specialGraphicsCharMap['l'] = 0x250C;    // Upper left hand corner (box drawing)
        specialGraphicsCharMap['k'] = 0x2510;    // Upper right hand corner (box drawing)
        specialGraphicsCharMap['w'] = 0x252C;    // T pointing downwards (box drawing)
        specialGraphicsCharMap['u'] = 0x2524;    // T pointing leftwards (box drawing)
        specialGraphicsCharMap['t'] = 0x251C;    // T pointing rightwards (box drawing)
        specialGraphicsCharMap['v'] = 0x2534;    // T pointing upwards (box drawing)
        specialGraphicsCharMap['n'] = 0x253C;    // Large plus/lines crossing (box drawing)
        specialGraphicsCharMap['o'] = 0x23BA;    // Horizontal scanline 1
        specialGraphicsCharMap['p'] = 0x23BB;    // Horizontal scanline 3
        specialGraphicsCharMap['r'] = 0x23BC;    // Horizontal scanline 7
        specialGraphicsCharMap['s'] = 0x23BD;    // Horizontal scanline 9
    }
    /**
     * Stores the characters that appear on the screen of the emulated terminal.
     */
    private final TranscriptScreen mainBuffer;
    /**
     * The terminal session this emulator is bound to.
     */
    private final TermSession session;
    /**
     * Holds the arguments of the current escape sequence.
     */
    private final int[] args = new int[MAX_ESCAPE_PARAMETERS];
    /**
     * Holds OSC arguments, which can be strings.
     */
    private final byte[] OSCArg = new byte[MAX_OSC_STRING_LENGTH];
    private TranscriptScreen altBuffer;
    private TranscriptScreen screen;
    /**
     * What is the current graphics character set. [0] == G0, [1] == G1
     */
    private final int[] charSet = new int[2];
    private final ByteBuffer UTF8ByteBuffer;
    
    /**
     * The number of parameter arguments. This name comes from the ANSI standard
     * for terminal escape codes.
     */
    private static final int MAX_ESCAPE_PARAMETERS = 16;
    private final CharBuffer inputCharBuffer;
    private final CharsetDecoder UTF8Decoder;
    private TermKeyListener keyListener;
    /**
     * The cursor row. Numbered 0..mRows-1.
     */
    private int cursorRow;
    
    /**
     * Don't know what the actual limit is, this seems OK for now.
     */
    private static final int MAX_OSC_STRING_LENGTH = 512;
    
    // Escape processing states:
    
    /**
     * Escape processing state: Not currently in an escape sequence.
     */
    private static final int ESC_NONE = 0;
    
    /**
     * Escape processing state: Have seen an ESC character
     */
    private static final int ESC = 1;
    
    /**
     * Escape processing state: Have seen ESC POUND
     */
    private static final int ESC_POUND = 2;
    
    /**
     * Escape processing state: Have seen ESC and a character-set-select char
     */
    private static final int ESC_SELECT_LEFT_PAREN = 3;
    
    /**
     * Escape processing state: Have seen ESC and a character-set-select char
     */
    private static final int ESC_SELECT_RIGHT_PAREN = 4;
    
    /**
     * Escape processing state: ESC [
     */
    private static final int ESC_LEFT_SQUARE_BRACKET = 5;
    
    /**
     * Escape processing state: ESC [ ?
     */
    private static final int ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK = 6;
    
    /**
     * Escape processing state: ESC %
     */
    private static final int ESC_PERCENT = 7;
    
    /**
     * Escape processing state: ESC ] (AKA OSC - Operating System Controls)
     */
    private static final int ESC_RIGHT_SQUARE_BRACKET = 8;
    
    /**
     * Escape processing state: ESC ] (AKA OSC - Operating System Controls)
     */
    private static final int ESC_RIGHT_SQUARE_BRACKET_ESC = 9;
    /**
     * The cursor column. Numbered 0..mColumns-1.
     */
    private int cursorCol;
    /**
     * The number of character rows in the terminal screen.
     */
    private int rows;
    /**
     * The number of character columns in the terminal screen.
     */
    private int columns;
    /**
     * Keeps track of the current argument of the current escape sequence.
     * Ranges from 0 to MAX_ESCAPE_PARAMETERS-1. (Typically just 0 or 1.)
     */
    private int argIndex;
    private int OSCArgLength;
    private int OSCArgTokenizerIndex;
    
    // DecSet booleans
    
    /**
     * This mask indicates 132-column mode is set. (As opposed to 80-column
     * mode.)
     */
    private static final int K_132_COLUMN_MODE_MASK = 1 << 3;
    
    /**
     * DECSCNM - set means reverse video (light background.)
     */
    private static final int K_REVERSE_VIDEO_MASK = 1 << 5;
    
    /**
     * This mask indicates that origin mode is set. (Cursor addressing is
     * relative to the absolute screen size, rather than the currently set top
     * and bottom margins.)
     */
    private static final int K_ORIGIN_MODE_MASK = 1 << 6;
    
    /**
     * This mask indicates that wraparound mode is set. (As opposed to
     * stop-at-right-column mode.)
     */
    private static final int K_WRAPAROUND_MODE_MASK = 1 << 7;
    
    /**
     * This mask indicates that the cursor should be shown. DECTCEM
     */
    
    private static final int K_SHOW_CURSOR_MASK = 1 << 25;
    
    /**
     * This mask is the subset of DecSet bits that are saved / restored by
     * the DECSC / DECRC commands
     */
    private static final int K_DECSC_DECRC_MASK =
            K_ORIGIN_MODE_MASK | K_WRAPAROUND_MODE_MASK;
    /**
     * True if the current escape sequence should continue, false if the current
     * escape sequence should be terminated. Used when parsing a single
     * character.
     */
    private boolean continueSequence;
    /**
     * The current state of the escape sequence state machine.
     */
    private int escapeState;
    /**
     * Saved state of the cursor row, Used to implement the save/restore cursor
     * position escape sequences.
     */
    private int savedCursorRow;
    
    // Modes set with Set Mode / Reset Mode
    
    /**
     * True if insert mode (as opposed to replace mode) is active. In insert
     * mode new characters are inserted, pushing existing text to the right.
     */
    private boolean mInsertMode;
    /**
     * Saved state of the cursor column, Used to implement the save/restore
     * cursor position escape sequences.
     */
    private int savedCursorCol;
    
    // The margins allow portions of the screen to be locked.
    private int savedEffect;
    private int savedDecFlags_DECSC_DECRC;
    /**
     * Holds multiple DECSET flags. The data is stored this way, rather than in
     * separate booleans, to make it easier to implement the save-and-restore
     * semantics. The various k*ModeMask masks can be used to extract and modify
     * the individual flags current states.
     */
    private int decFlags;
    
    /**
     * The width of the last emitted spacing character.  Used to place
     * combining characters into the correct column.
     */
    private int mLastEmittedCharWidth = 0;
    
    /**
     * True if we just auto-wrapped and no character has been emitted on this
     * line yet.  Used to ensure combining characters following a character
     * at the edge of the screen are stored in the proper place.
     */
    private boolean justWrapped = false;
    /**
     * Saves away a snapshot of the DECSET flags. Used to implement save and
     * restore escape sequences.
     */
    private int savedDecFlags;
    /**
     * The current DECSET mouse tracking mode, zero for no mouse tracking.
     */
    private int mouseTrackingMode;
    /**
     * An array of tab stops. mTabStop[i] is true if there is a tab stop set for
     * column i.
     */
    private boolean[] tabStop;
    /**
     * The top margin of the screen, for scrolling purposes. Ranges from 0 to
     * mRows-2.
     */
    private int topMargin;
    /**
     * The bottom margin of the screen, for scrolling purposes. Ranges from
     * mTopMargin + 2 to mRows. (Defines the first row after the scrolling
     * region.
     */
    private int bottomMargin;
    /**
     * True if the next character to be emitted will be automatically wrapped to
     * the next line. Used to disambiguate the case where the cursor is
     * positioned on column mColumns-1.
     */
    private boolean aboutToAutoWrap;
    /**
     * Used for debugging, counts how many chars have been processed.
     */
    private int processedCharCount;
    /**
     * Foreground color, 0..255
     */
    private int foreColor;
    
    private final static int CHAR_SET_UK = 0;
    private final static int CHAR_SET_ASCII = 1;
    private final static int CHAR_SET_SPECIAL_GRAPHICS = 2;
    private final static int CHAR_SET_ALT_STANDARD = 3;
    private int defaultForeColor;
    /**
     * Background color, 0..255
     */
    private int backColor;
    private int defaultBackColor;
    /**
     * Current TextStyle effect
     */
    private int effect;
    private boolean keypadApplicationMode;
    /**
     * false == G0, true == G1
     */
    private boolean alternateCharSet;
    
    /**
     * UTF-8 support
     */
    private static final int UNICODE_REPLACEMENT_CHAR = 0xfffd;
    /**
     * Derived from mAlternateCharSet and mCharSet.
     * True if we're supposed to be drawing the special graphics.
     */
    private boolean useAlternateCharSet;
    /**
     * Used for moving selection up along with the scrolling text
     */
    private int scrollCounter = 0;
    private boolean defaultUTF8Mode = false;
    private boolean UTF8Mode = false;
    private boolean UTF8EscapeUsed = false;
    private int UTF8ToFollow = 0;
    private UpdateCallback UTF8ModeNotify;
    /**
     * Construct a terminal emulator that uses the supplied screen
     *
     * @param session the terminal session the emulator is attached to
     * @param screen  the screen to render characters into.
     * @param columns the number of columns to emulate
     * @param rows    the number of rows to emulate
     * @param scheme  the default color scheme of this emulator
     */
    public TerminalEmulator(TermSession session, TranscriptScreen screen, int columns, int rows, ColorScheme scheme) {
        this.session = session;
        mainBuffer = screen;
        this.screen = mainBuffer;
        altBuffer = new TranscriptScreen(columns, rows, rows, scheme);
        this.rows = rows;
        this.columns = columns;
        tabStop = new boolean[this.columns];
    
        setColorScheme(scheme);
    
        UTF8ByteBuffer = ByteBuffer.allocate(4);
        inputCharBuffer = CharBuffer.allocate(2);
        UTF8Decoder = StandardCharsets.UTF_8.newDecoder();
        UTF8Decoder.onMalformedInput(CodingErrorAction.REPLACE);
        UTF8Decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    
        reset();
    }
    
    /**
     * This is not accurate, but it makes the terminal more useful on
     * small screens.
     */
    private final static boolean DEFAULT_TO_AUTOWRAP_ENABLED = true;
    
    public void setKeyListener(TermKeyListener l) {
        keyListener = l;
    }
    
    public TranscriptScreen getScreen() {
        return screen;
    }
    
    public void updateSize(int columns, int rows) {
        if (this.rows == rows && this.columns == columns) {
            return;
        }
        if (columns <= 0) {
            throw new IllegalArgumentException("rows:" + columns);
        }
    
        if (rows <= 0) {
            throw new IllegalArgumentException("rows:" + rows);
        }
    
        TranscriptScreen screen = this.screen;
        TranscriptScreen altScreen;
        if (screen != mainBuffer) {
            altScreen = mainBuffer;
        } else {
            altScreen = altBuffer;
        }
    
        // Try to resize the screen without getting the transcript
        int[] cursor = {cursorCol, cursorRow};
        boolean fastResize = screen.fastResize(columns, rows, cursor);
    
        GrowableIntArray cursorColor = null;
        String charAtCursor = null;
        GrowableIntArray colors = null;
        String transcriptText = null;
        if (!fastResize) {
            /* Save the character at the cursor (if one exists) and store an
             * ASCII ESC character at the cursor's location
             * This is an epic hack that lets us restore the cursor later...
             */
            cursorColor = new GrowableIntArray(1);
            charAtCursor = screen.getSelectedText(cursorColor, cursorCol, cursorRow, cursorCol, cursorRow);
            screen.set(cursorCol, cursorRow, 27, 0);
    
            colors = new GrowableIntArray(1024);
            transcriptText = screen.getTranscriptText(colors);
            screen.resize(columns, rows, getStyle());
        }
        
        boolean altFastResize = true;
        GrowableIntArray altColors = null;
        String altTranscriptText = null;
        if (altScreen != null) {
            altFastResize = altScreen.fastResize(columns, rows, null);
        
            if (!altFastResize) {
                altColors = new GrowableIntArray(1024);
                altTranscriptText = altScreen.getTranscriptText(altColors);
                altScreen.resize(columns, rows, getStyle());
            }
        }
    
        if (this.rows != rows) {
            this.rows = rows;
            topMargin = 0;
            bottomMargin = this.rows;
        }
        if (this.columns != columns) {
            int oldColumns = this.columns;
            this.columns = columns;
            boolean[] oldTabStop = tabStop;
            tabStop = new boolean[this.columns];
            int toTransfer = Math.min(oldColumns, columns);
            System.arraycopy(oldTabStop, 0, tabStop, 0, toTransfer);
        }
    
        if (!altFastResize) {
            boolean wasAboutToAutoWrap = aboutToAutoWrap;
        
            // Restore the contents of the inactive screen's buffer
            this.screen = altScreen;
            cursorRow = 0;
            cursorCol = 0;
            aboutToAutoWrap = false;
        
            int end = altTranscriptText.length() - 1;
            /* Unlike for the main transcript below, don't trim off trailing
             * newlines -- the alternate transcript lacks a cursor marking, so
             * we might introduce an unwanted vertical shift in the screen
             * contents this way */
            char c, cLow;
            int colorOffset = 0;
            for (int i = 0; i <= end; i++) {
                c = altTranscriptText.charAt(i);
                int style = altColors.at(i - colorOffset);
                if (Character.isHighSurrogate(c)) {
                    cLow = altTranscriptText.charAt(++i);
                    emit(Character.toCodePoint(c, cLow), style);
                    ++colorOffset;
                } else if (c == '\n') {
                    setCursorCol(0);
                    doLinefeed();
                } else {
                    emit(c, style);
                }
            }
        
            this.screen = screen;
            aboutToAutoWrap = wasAboutToAutoWrap;
        }
        
        if (fastResize) {
            // Only need to make sure the cursor is in the right spot
            if (cursor[0] >= 0 && cursor[1] >= 0) {
                cursorCol = cursor[0];
                cursorRow = cursor[1];
            } else {
                // Cursor scrolled off screen, reset the cursor to top left
                cursorCol = 0;
                cursorRow = 0;
            }
    
            return;
        }
    
        cursorRow = 0;
        cursorCol = 0;
        aboutToAutoWrap = false;
    
        int newCursorRow = -1;
        int newCursorCol = -1;
        int newCursorTranscriptPos = -1;
        int end = transcriptText.length() - 1;
        while ((end >= 0) && transcriptText.charAt(end) == '\n') {
            end--;
        }
        char c, cLow;
        int colorOffset = 0;
        for (int i = 0; i <= end; i++) {
            c = transcriptText.charAt(i);
            int style = colors.at(i - colorOffset);
            if (Character.isHighSurrogate(c)) {
                cLow = transcriptText.charAt(++i);
                emit(Character.toCodePoint(c, cLow), style);
                ++colorOffset;
            } else if (c == '\n') {
                setCursorCol(0);
                doLinefeed();
            } else if (c == 27) {
                /* We marked the cursor location with ESC earlier, so this
                   is the place to restore the cursor to */
                newCursorRow = cursorRow;
                newCursorCol = cursorCol;
                newCursorTranscriptPos = screen.getActiveRows();
                if (charAtCursor != null && charAtCursor.length() > 0) {
                    // Emit the real character that was in this spot
                    int encodedCursorColor = cursorColor.at(0);
                    emit(charAtCursor.toCharArray(), 0, charAtCursor.length(), encodedCursorColor);
                }
            } else {
                emit(c, style);
            }
        }
        
        // If we marked a cursor location, move the cursor there now
        if (newCursorRow != -1 && newCursorCol != -1) {
            cursorRow = newCursorRow;
            cursorCol = newCursorCol;

            /* Adjust for any scrolling between the time we marked the cursor
               location and now */
            int scrollCount = screen.getActiveRows() - newCursorTranscriptPos;
            if (scrollCount > 0 && scrollCount <= newCursorRow) {
                cursorRow -= scrollCount;
            } else if (scrollCount > newCursorRow) {
                // Cursor scrolled off screen -- reset to top left corner
                cursorRow = 0;
                cursorCol = 0;
            }
        }
    }
    
    /**
     * Get the cursor's current row.
     *
     * @return the cursor's current row.
     */
    public final int getCursorRow() {
        return cursorRow;
    }
    
    private void setCursorRow(int row) {
        cursorRow = row;
        aboutToAutoWrap = false;
    }
    
    /**
     * Get the cursor's current column.
     *
     * @return the cursor's current column.
     */
    public final int getCursorCol() {
        return cursorCol;
    }
    
    private void setCursorCol(int col) {
        cursorCol = col;
        aboutToAutoWrap = false;
    }
    
    public final boolean getReverseVideo() {
        return (decFlags & K_REVERSE_VIDEO_MASK) != 0;
    }
    
    public final boolean getShowCursor() {
        return (decFlags & K_SHOW_CURSOR_MASK) != 0;
    }
    
    public final boolean getKeypadApplicationMode() {
        return keypadApplicationMode;
    }
    
    /**
     * Get the current DECSET mouse tracking mode, zero for no mouse tracking.
     *
     * @return the current DECSET mouse tracking mode.
     */
    public final int getMouseTrackingMode() {
        return mouseTrackingMode;
    }
    
    private void process(byte b) {
        process(b, true);
    }
    
    private void setDefaultTabStops() {
        for (int i = 0; i < columns; i++) {
            tabStop[i] = (i & 7) == 0 && i != 0;
        }
    }
    
    /**
     * Accept bytes (typically from the pseudo-teletype) and process them.
     *
     * @param buffer a byte array containing the bytes to be processed
     * @param base   the first index of the array to process
     * @param length the number of bytes in the array to process
     */
    public void append(byte[] buffer, int base, int length) {
        if (EmulatorDebug.LOG_CHARACTERS_FLAG) {
            Log.d(EmulatorDebug.LOG_TAG, "In: '" + EmulatorDebug.bytesToString(buffer, base, length) + "'");
        }
        for (int i = 0; i < length; i++) {
            byte b = buffer[base + i];
            try {
                process(b);
                processedCharCount++;
            } catch (Exception e) {
                Log.e(EmulatorDebug.LOG_TAG, "Exception while processing character "
                        + processedCharCount + " code "
                        + Integer.toString(b), e);
            }
        }
    }
    
    private void process(byte b, boolean doUTF8) {
        // Let the UTF-8 decoder try to handle it if we're in UTF-8 mode
        if (doUTF8 && UTF8Mode && handleUTF8Sequence(b)) {
            return;
        }
        
        // Handle C1 control characters
        if ((b & 0x80) == 0x80 && (b & 0x7f) <= 0x1f) {
            /* ESC ((code & 0x7f) + 0x40) is the two-byte escape sequence
               corresponding to a particular C1 code */
            process((byte) 27, false);
            process((byte) ((b & 0x7f) + 0x40), false);
            return;
        }
        
        switch (b) {
            case 0: // NUL
                // Do nothing
                break;
            
            case 7: // BEL
                /* If in an OSC sequence, BEL may terminate a string; otherwise do
                 * nothing */
                if (escapeState == ESC_RIGHT_SQUARE_BRACKET) {
                    doEscRightSquareBracket(b);
                }
                break;
            
            case 8: // BS
                setCursorCol(Math.max(0, cursorCol - 1));
                break;
            
            case 9: // HT
                // Move to next tab stop, but not past edge of screen
                setCursorCol(nextTabStop(cursorCol));
                break;
            
            case 13:
                setCursorCol(0);
                break;
            
            case 10: // CR
            case 11: // VT
            case 12: // LF
                doLinefeed();
                break;
    
            case 14: // SO:
                setAltCharSet(true);
                break;
    
            case 15: // SI:
                setAltCharSet(false);
                break;
    
            case 24: // CAN
            case 26: // SUB
                if (escapeState != ESC_NONE) {
                    escapeState = ESC_NONE;
                    emit((byte) 127);
                }
                break;
    
            case 27: // ESC
                // Starts an escape sequence unless we're parsing a string
                if (escapeState != ESC_RIGHT_SQUARE_BRACKET) {
                    startEscapeSequence(ESC);
                } else {
                    doEscRightSquareBracket(b);
                }
                break;
    
            default:
                continueSequence = false;
                switch (escapeState) {
                    case ESC_NONE -> {
                        if (b >= 32) {
                            emit(b);
                        }
                    }
                    case ESC ->
                            doEsc(b);
                    case ESC_POUND ->
                            doEscPound(b);
                    case ESC_SELECT_LEFT_PAREN ->
                            doEscSelectLeftParen(b);
                    case ESC_SELECT_RIGHT_PAREN ->
                            doEscSelectRightParen(b);
                    case ESC_LEFT_SQUARE_BRACKET ->
                            doEscLeftSquareBracket(b); // CSI
                    case ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK ->
                            doEscLSBQuest(b); // CSI ?
                    case ESC_PERCENT ->
                            doEscPercent(b);
                    case ESC_RIGHT_SQUARE_BRACKET ->
                            doEscRightSquareBracket(b);
                    case ESC_RIGHT_SQUARE_BRACKET_ESC ->
                            doEscRightSquareBracketEsc(b);
                    default ->
                            unknownSequence(b);
                }
                if (!continueSequence) {
                    escapeState = ESC_NONE;
                }
                break;
        }
    }
    
    private boolean handleUTF8Sequence(byte b) {
        if (UTF8ToFollow == 0 && (b & 0x80) == 0) {
            // ASCII character -- we don't need to handle this
            return false;
        }
    
        if (UTF8ToFollow > 0) {
            if ((b & 0xc0) != 0x80) {
                /* Not a UTF-8 continuation byte (doesn't begin with 0b10)
                   Replace the entire sequence with the replacement char */
                UTF8ToFollow = 0;
                UTF8ByteBuffer.clear();
                emit(UNICODE_REPLACEMENT_CHAR);
            
                /* The Unicode standard (section 3.9, definition D93) requires
                 * that we now attempt to process this byte as though it were
                 * the beginning of another possibly-valid sequence */
                return handleUTF8Sequence(b);
            }
        
            UTF8ByteBuffer.put(b);
            if (--UTF8ToFollow == 0) {
                // Sequence complete -- decode and emit it
                ByteBuffer byteBuf = UTF8ByteBuffer;
                CharBuffer charBuf = inputCharBuffer;
                CharsetDecoder decoder = UTF8Decoder;
            
                byteBuf.rewind();
                decoder.reset();
                decoder.decode(byteBuf, charBuf, true);
                decoder.flush(charBuf);
            
                char[] chars = charBuf.array();
                if (chars[0] >= 0x80 && chars[0] <= 0x9f) {
                    /* Sequence decoded to a C1 control character which needs
                       to be sent through process() again */
                    process((byte) chars[0], false);
                } else {
                    emit(chars);
                }
                
                byteBuf.clear();
                charBuf.clear();
            }
        } else {
            if ((b & 0xe0) == 0xc0) { // 0b110 -- two-byte sequence
                UTF8ToFollow = 1;
            } else if ((b & 0xf0) == 0xe0) { // 0b1110 -- three-byte sequence
                UTF8ToFollow = 2;
            } else if ((b & 0xf8) == 0xf0) { // 0b11110 -- four-byte sequence
                UTF8ToFollow = 3;
            } else {
                // Not a valid UTF-8 sequence start -- replace this char
                emit(UNICODE_REPLACEMENT_CHAR);
                return true;
            }
        
            UTF8ByteBuffer.put(b);
        }
        
        return true;
    }
    
    private void setAltCharSet(boolean alternateCharSet) {
        this.alternateCharSet = alternateCharSet;
        computeEffectiveCharSet();
    }
    
    private void computeEffectiveCharSet() {
        int charSet = this.charSet[alternateCharSet ? 1 : 0];
        useAlternateCharSet = charSet == CHAR_SET_SPECIAL_GRAPHICS;
    }
    
    private int nextTabStop(int cursorCol) {
        for (int i = cursorCol + 1; i < columns; i++) {
            if (tabStop[i]) {
                return i;
            }
        }
        return columns - 1;
    }
    
    private int prevTabStop(int cursorCol) {
        for (int i = cursorCol - 1; i >= 0; i--) {
            if (tabStop[i]) {
                return i;
            }
        }
        return 0;
    }
    
    private int getDecFlagsMask(int argument) {
        if (argument >= 1 && argument <= 32) {
            return (1 << argument);
        }
        
        return 0;
    }
    
    private void doEscPercent(byte b) {
        switch (b) {
            case '@' -> { // Esc % @ -- return to ISO 2022 mode
                setUTF8Mode(false);
                UTF8EscapeUsed = true;
            }
            case 'G' -> { // Esc % G -- UTF-8 mode
                setUTF8Mode(true);
                UTF8EscapeUsed = true;
            }
            default -> {
            } // unimplemented character set
        }
    }
    
    private void doEscLSBQuest(byte b) {
        int arg = getArg0(0);
        int mask = getDecFlagsMask(arg);
        int oldFlags = decFlags;
        switch (b) {
            case 'h' -> { // Esc [ ? Pn h - DECSET
                decFlags |= mask;
                switch (arg) {
                    case 1 ->
                            keyListener.setCursorKeysApplicationMode(true);
                    case 47, 1047, 1049 -> {
                        if (altBuffer != null) {
                            screen = altBuffer;
                        }
                    }
                }
                if (arg >= 1000 && arg <= 1003) {
                    mouseTrackingMode = arg;
                }
            }
            case 'l' -> { // Esc [ ? Pn l - DECRST
                decFlags &= ~mask;
                switch (arg) {
                    case 1 ->
                            keyListener.setCursorKeysApplicationMode(false);
                    case 47, 1047, 1049 ->
                            screen = mainBuffer;
                }
                if (arg >= 1000 && arg <= 1003) {
                    mouseTrackingMode = 0;
                }
            }
            case 'r' -> // Esc [ ? Pn r - restore
                    decFlags = (decFlags & ~mask) | (savedDecFlags & mask);
            case 's' -> // Esc [ ? Pn s - save
                    savedDecFlags = (savedDecFlags & ~mask) | (decFlags & mask);
            default ->
                    parseArg(b);
        }
    
        int newlySetFlags = (~oldFlags) & decFlags;
        int changedFlags = oldFlags ^ decFlags;
    
        // 132 column mode
        if ((changedFlags & K_132_COLUMN_MODE_MASK) != 0) {
            // We don't actually set/reset 132 cols, but we do want the
            // side effect of clearing the screen and homing the cursor.
            blockClear(0, 0, columns, rows);
            setCursorRowCol(0, 0);
        }
    
        // origin mode
        if ((newlySetFlags & K_ORIGIN_MODE_MASK) != 0) {
            // Home the cursor.
            setCursorPosition(0, 0);
        }
    }
    
    /**
     * @noinspection SameParameterValue
     */
    private void startEscapeSequence(int escapeState) {
        this.escapeState = escapeState;
        argIndex = 0;
        for (int j = 0; j < MAX_ESCAPE_PARAMETERS; j++) {
            args[j] = -1;
        }
    }
    
    private void doLinefeed() {
        int newCursorRow = cursorRow + 1;
        if (newCursorRow >= bottomMargin) {
            scroll();
            newCursorRow = bottomMargin - 1;
        }
        setCursorRow(newCursorRow);
    }
    
    private void doEscSelectLeftParen(byte b) {
        doSelectCharSet(0, b);
    }
    
    private void doEscSelectRightParen(byte b) {
        doSelectCharSet(1, b);
    }
    
    private void continueSequence() {
        continueSequence = true;
    }
    
    private void continueSequence(int state) {
        escapeState = state;
        continueSequence = true;
    }
    
    private void doSelectCharSet(int charSetIndex, byte b) {
        int charSet;
        switch (b) {
            case 'A' -> // United Kingdom character set
                    charSet = CHAR_SET_UK;
            case 'B' -> // ASCII set
                    charSet = CHAR_SET_ASCII;
            case '0' -> // Special Graphics
                    charSet = CHAR_SET_SPECIAL_GRAPHICS;
            case '1' -> // Alternate character set
                    charSet = CHAR_SET_ALT_STANDARD;
            case '2' ->
                    charSet = CHAR_SET_ALT_SPECIAL_GRAPHICS;
            default -> {
                unknownSequence(b);
                return;
            }
        }
        this.charSet[charSetIndex] = charSet;
        computeEffectiveCharSet();
    }
    
    private void doEscPound(byte b) {
        if (b == '8') { // Esc # 8 - DECALN alignment test
            screen.blockSet(0, 0, columns, rows, 'E',
                    getStyle());
        } else {
            unknownSequence(b);
        }
    }
    
    private void doEsc(byte b) {
        switch (b) {
            case '#' ->
                    continueSequence(ESC_POUND);
            case '(' ->
                    continueSequence(ESC_SELECT_LEFT_PAREN);
            case ')' ->
                    continueSequence(ESC_SELECT_RIGHT_PAREN);
            case '7' -> { // DECSC save cursor
                savedCursorRow = cursorRow;
                savedCursorCol = cursorCol;
                savedEffect = effect;
                savedDecFlags_DECSC_DECRC = decFlags & K_DECSC_DECRC_MASK;
            }
            case '8' -> { // DECRC restore cursor
                setCursorRowCol(savedCursorRow, savedCursorCol);
                effect = savedEffect;
                decFlags = (decFlags & ~K_DECSC_DECRC_MASK)
                        | savedDecFlags_DECSC_DECRC;
            }
            case 'D' -> // INDEX
                    doLinefeed();
            case 'E' -> { // NEL
                setCursorCol(0);
                doLinefeed();
            }
            case 'F' -> // Cursor to lower-left corner of screen
                    setCursorRowCol(0, bottomMargin - 1);
            case 'H' -> // Tab set
                    tabStop[cursorCol] = true;
            case 'M' -> { // Reverse index
                if (cursorRow <= topMargin) {
                    screen.blockCopy(0, topMargin, columns, bottomMargin
                            - (topMargin + 1), 0, topMargin + 1);
                    blockClear(0, topMargin, columns);
                } else {
                    cursorRow--;
                }
            }
            case 'N' -> // SS2
                    unimplementedSequence(b);
            case '0' -> // SS3
                    unimplementedSequence(b);
            case 'P' -> // Device control string
                    unimplementedSequence(b);
            case 'Z' -> // return terminal ID
                    sendDeviceAttributes();
            case '[' ->
                    continueSequence(ESC_LEFT_SQUARE_BRACKET);
            case '=' -> // DECKPAM
                    keypadApplicationMode = true;
            case ']' -> { // OSC
                startCollectingOSCArgs();
                continueSequence(ESC_RIGHT_SQUARE_BRACKET);
            }
            case '>' -> // DECKPNM
                    keypadApplicationMode = false;
            default ->
                    unknownSequence(b);
        }
    }
    
    private boolean checkColor(int color) {
        boolean result = isValidColor(color);
        if (!result) {
            if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
                Log.w(EmulatorDebug.LOG_TAG,
                        String.format("Invalid color %d", color));
            }
        }
        return result;
    }
    
    private boolean isValidColor(int color) {
        return color >= 0 && color < TextStyle.ciColorLength;
    }
    
    private void doEscLeftSquareBracket(byte b) {
        // CSI
        switch (b) {
            case '@' -> // ESC [ Pn @ - ICH Insert Characters
            {
                int charsAfterCursor = columns - cursorCol;
                int charsToInsert = Math.min(getArg0(1), charsAfterCursor);
                int charsToMove = charsAfterCursor - charsToInsert;
                screen.blockCopy(cursorCol, cursorRow, charsToMove, 1,
                        cursorCol + charsToInsert, cursorRow);
                blockClear(cursorCol, cursorRow, charsToInsert);
            }
            case 'A' -> // ESC [ Pn A - Cursor Up
                    setCursorRow(Math.max(topMargin, cursorRow - getArg0(1)));
            case 'B' -> // ESC [ Pn B - Cursor Down
                    setCursorRow(Math.min(bottomMargin - 1, cursorRow + getArg0(1)));
            case 'C' -> // ESC [ Pn C - Cursor Right
                    setCursorCol(Math.min(columns - 1, cursorCol + getArg0(1)));
            case 'D' -> // ESC [ Pn D - Cursor Left
                    setCursorCol(Math.max(0, cursorCol - getArg0(1)));
            case 'G' -> // ESC [ Pn G - Cursor Horizontal Absolute
                    setCursorCol(Math.min(Math.max(1, getArg0(1)), columns) - 1);
            case 'H' -> // ESC [ Pn ; H - Cursor Position
                    setHorizontalVerticalPosition();
            case 'J' -> { // ESC [ Pn J - ED - Erase in Display
                // ED ignores the scrolling margins.
                switch (getArg0(0)) {
                    case 0 -> { // Clear below
                        blockClear(cursorCol, cursorRow, columns - cursorCol);
                        blockClear(0, cursorRow + 1, columns,
                                rows - (cursorRow + 1));
                    }
                    case 1 -> { // Erase from the start of the screen to the cursor.
                        blockClear(0, 0, columns, cursorRow);
                        blockClear(0, cursorRow, cursorCol + 1);
                    }
                    case 2 -> // Clear all
                            blockClear(0, 0, columns, rows);
                    default ->
                            unknownSequence(b);
                }
            }
            case 'K' -> { // ESC [ Pn K - Erase in Line
                switch (getArg0(0)) {
                    case 0 -> // Clear to right
                            blockClear(cursorCol, cursorRow, columns - cursorCol);
                    case 1 -> // Erase start of line to cursor (including cursor)
                            blockClear(0, cursorRow, cursorCol + 1);
                    case 2 -> // Clear whole line
                            blockClear(0, cursorRow, columns);
                    default ->
                            unknownSequence(b);
                }
            }
            case 'L' -> // Insert Lines
            {
                int linesAfterCursor = bottomMargin - cursorRow;
                int linesToInsert = Math.min(getArg0(1), linesAfterCursor);
                int linesToMove = linesAfterCursor - linesToInsert;
                screen.blockCopy(0, cursorRow, columns, linesToMove, 0,
                        cursorRow + linesToInsert);
                blockClear(0, cursorRow, columns, linesToInsert);
            }
            case 'M' -> // Delete Lines
            {
                int linesAfterCursor = bottomMargin - cursorRow;
                int linesToDelete = Math.min(getArg0(1), linesAfterCursor);
                int linesToMove = linesAfterCursor - linesToDelete;
                screen.blockCopy(0, cursorRow + linesToDelete, columns,
                        linesToMove, 0, cursorRow);
                blockClear(0, cursorRow + linesToMove, columns, linesToDelete);
            }
            case 'P' -> // Delete Characters
            {
                int charsAfterCursor = columns - cursorCol;
                int charsToDelete = Math.min(getArg0(1), charsAfterCursor);
                int charsToMove = charsAfterCursor - charsToDelete;
                screen.blockCopy(cursorCol + charsToDelete, cursorRow,
                        charsToMove, 1, cursorCol, cursorRow);
                blockClear(cursorCol + charsToMove, cursorRow, charsToDelete);
            }
            case 'T' -> // Mouse tracking
                    unimplementedSequence(b);
            case 'X' -> // Erase characters
                    blockClear(cursorCol, cursorRow, getArg0(0));
            case 'Z' -> // Back tab
                    setCursorCol(prevTabStop(cursorCol));
            case '?' -> // Esc [ ? -- start of a private mode set
                    continueSequence(ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK);
            case 'c' -> // Send device attributes
                    sendDeviceAttributes();
            case 'd' -> // ESC [ Pn d - Vert Position Absolute
                    setCursorRow(Math.min(Math.max(1, getArg0(1)), rows) - 1);
            case 'f' -> // Horizontal and Vertical Position
                    setHorizontalVerticalPosition();
            case 'g' -> { // Clear tab stop
                switch (getArg0(0)) {
                    case 0 ->
                            tabStop[cursorCol] = false;
                    case 3 -> {
                        for (int i = 0; i < columns; i++) {
                            tabStop[i] = false;
                        }
                    }
                    default -> {
                    }
                    // Specified to have no effect.
                }
            }
            case 'h' -> // Set Mode
                    doSetMode(true);
            case 'l' -> // Reset Mode
                    doSetMode(false);
            case 'm' -> // Esc [ Pn m - character attributes.
                // (can have up to 16 numerical arguments)
                    selectGraphicRendition();
            case 'n' -> { // Esc [ Pn n - ECMA-48 Status Report Commands
                //sendDeviceAttributes()
                switch (getArg0(0)) {
                    case 5 -> { // Device status report (DSR):
                        // Answer is ESC [ 0 n (Terminal OK).
                        byte[] dsr = {(byte) 27, (byte) '[', (byte) '0', (byte) 'n'};
                        session.write(dsr, 0, dsr.length);
                    }
                    case 6 -> { // Cursor position report (CPR):
                        // Answer is ESC [ y ; x R, where x,y is
                        // the cursor location.
                        byte[] cpr = String.format(Locale.US, "\033[%d;%dR",
                                cursorRow + 1, cursorCol + 1).getBytes();
                        session.write(cpr, 0, cpr.length);
                    }
                    default -> {
                    }
                }
            }
            case 'r' -> // Esc [ Pn ; Pn r - set top and bottom margins
            {
                // The top margin defaults to 1, the bottom margin
                // (unusually for arguments) defaults to mRows.
                //
                // The escape sequence numbers top 1..23, but we
                // number top 0..22.
                // The escape sequence numbers bottom 2..24, and
                // so do we (because we use a zero based numbering
                // scheme, but we store the first line below the
                // bottom-most scrolling line.
                // As a result, we adjust the top line by -1, but
                // we leave the bottom line alone.
                //
                // Also require that top + 2 <= bottom
        
                int top = Math.max(0, Math.min(getArg0(1) - 1, rows - 2));
                int bottom = Math.max(top + 2, Math.min(getArg1(rows), rows));
                topMargin = top;
                bottomMargin = bottom;
        
                // The cursor is placed in the home position
                setCursorRowCol(topMargin, 0);
            }
            default ->
                    parseArg(b);
        }
    }
    
    private void doEscRightSquareBracketEsc(byte b) {
        if (b == '\\') {
            doOSC();
        } else {// The ESC character was not followed by a \, so insert the ESC and
            // the current character in arg buffer.
            collectOSCArgs((byte) 0x1b);
            collectOSCArgs(b);
            continueSequence(ESC_RIGHT_SQUARE_BRACKET);
        }
    }
    
    private void selectGraphicRendition() {
        // SGR
        for (int i = 0; i <= argIndex; i++) {
            int code = args[i];
            if (code < 0) {
                if (argIndex > 0) {
                    continue;
                } else {
                    code = 0;
                }
            }
        
            // See http://en.wikipedia.org/wiki/ANSI_escape_code#graphics
            
            if (code == 0) { // reset
                foreColor = defaultForeColor;
                backColor = defaultBackColor;
                effect = TextStyle.fxNormal;
            } else if (code == 1) { // bold
                effect |= TextStyle.fxBold;
            } else if (code == 3) { // italics, but rarely used as such; "standout" (inverse colors) with TERM=screen
                effect |= TextStyle.fxItalic;
            } else if (code == 4) { // underscore
                effect |= TextStyle.fxUnderline;
            } else if (code == 5) { // blink
                effect |= TextStyle.fxBlink;
            } else if (code == 7) { // inverse
                effect |= TextStyle.fxInverse;
            } else if (code == 8) { // invisible
                effect |= TextStyle.fxInvisible;
            } else if (code == 10) { // exit alt charset (TERM=linux)
                setAltCharSet(false);
            } else if (code == 11) { // enter alt charset (TERM=linux)
                setAltCharSet(true);
            } else if (code == 22) { // Normal color or intensity, neither bright, bold nor faint
                //mEffect &= ~(TextStyle.fxBold | TextStyle.fxFaint);
                effect &= ~TextStyle.fxBold;
            } else if (code == 23) { // not italic, but rarely used as such; clears standout with TERM=screen
                effect &= ~TextStyle.fxItalic;
            } else if (code == 24) { // underline: none
                effect &= ~TextStyle.fxUnderline;
            } else if (code == 25) { // blink: none
                effect &= ~TextStyle.fxBlink;
            } else if (code == 27) { // image: positive
                effect &= ~TextStyle.fxInverse;
            } else if (code == 28) { // invisible
                effect &= ~TextStyle.fxInvisible;
            } else if (code >= 30 && code <= 37) { // foreground color
                foreColor = code - 30;
            } else if (code == 38 && i + 2 <= argIndex && args[i + 1] == 5) { // foreground 256 color
                int color = args[i + 2];
                if (checkColor(color)) {
                    foreColor = color;
                }
                i += 2;
            } else if (code == 39) { // set default text color
                foreColor = defaultForeColor;
            } else if (code >= 40 && code <= 47) { // background color
                backColor = code - 40;
            } else if (code == 48 && i + 2 <= argIndex && args[i + 1] == 5) { // background 256 color
                backColor = args[i + 2];
                int color = args[i + 2];
                if (checkColor(color)) {
                    backColor = color;
                }
                i += 2;
            } else if (code == 49) { // set default background color
                backColor = defaultBackColor;
            } else if (code >= 90 && code <= 97) { // bright foreground color
                foreColor = code - 90 + 8;
            } else if (code >= 100 && code <= 107) { // bright background color
                backColor = code - 100 + 8;
            } else {
                if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
                    Log.w(EmulatorDebug.LOG_TAG, String.format("SGR unknown code %d", code));
                }
            }
        }
    }
    
    private void doEscRightSquareBracket(byte b) {
        switch (b) {
            case 0x7 ->
                    doOSC();
            case 0x1b -> // Esc, probably start of Esc \ sequence
                    continueSequence(ESC_RIGHT_SQUARE_BRACKET_ESC);
            default ->
                    collectOSCArgs(b);
        }
    }
    
    private void blockClear(int sx, int sy, int w) {
        blockClear(sx, sy, w, 1);
    }
    
    private void blockClear(int sx, int sy, int w, int h) {
        screen.blockSet(sx, sy, w, h, ' ', getStyle());
    }
    
    private void doOSC() { // Operating System Controls
        startTokenizingOSC();
        int ps = nextOSCInt(';');
        switch (ps) { // Change icon name and window title to T
            // Change icon name to T
            case 0, 1, 2 -> // Change window title to T
                    changeTitle(ps, nextOSCString(-1));
            default ->
                    unknownParameter(ps);
        }
        finishSequence();
    }
    
    private void changeTitle(int parameter, String title) {
        if (parameter == 0 || parameter == 2) {
            session.setTitle(title);
        }
    }
    
    private int getForeColor() {
        return foreColor;
    }
    
    private int getStyle() {
        return TextStyle.encode(getForeColor(), getBackColor(), getEffect());
    }
    
    private void doSetMode(boolean newValue) {
        int modeBit = getArg0(0);
        if (modeBit == 4) {
            mInsertMode = newValue;
        } else {
            unknownParameter(modeBit);
        }
    }
    
    private void setHorizontalVerticalPosition() {
        
        // Parameters are Row ; Column
        
        setCursorPosition(getArg1(1) - 1, getArg0(1) - 1);
    }
    
    private int getBackColor() {
        return backColor;
    }
    
    private int getEffect() {
        return effect;
    }
    
    private void setCursorPosition(int x, int y) {
        int effectiveTopMargin = 0;
        int effectiveBottomMargin = rows;
        if ((decFlags & K_ORIGIN_MODE_MASK) != 0) {
            effectiveTopMargin = topMargin;
            effectiveBottomMargin = bottomMargin;
        }
        int newRow =
                Math.max(effectiveTopMargin, Math.min(effectiveTopMargin + y,
                        effectiveBottomMargin - 1));
        int newCol = Math.max(0, Math.min(x, columns - 1));
        setCursorRowCol(newRow, newCol);
    }
    
    private void sendDeviceAttributes() {
        // This identifies us as a DEC vt100 with advanced
        // video options. This is what the xterm terminal
        // emulator sends.
        byte[] attributes =
                {
                        /* VT100 */
                        (byte) 27, (byte) '[', (byte) '?', (byte) '1',
                        (byte) ';', (byte) '2', (byte) 'c'
                        
                        /* VT220
                        (byte) 27, (byte) '[', (byte) '?', (byte) '6',
                        (byte) '0',  (byte) ';',
                        (byte) '1',  (byte) ';',
                        (byte) '2',  (byte) ';',
                        (byte) '6',  (byte) ';',
                        (byte) '8',  (byte) ';',
                        (byte) '9',  (byte) ';',
                        (byte) '1',  (byte) '5', (byte) ';',
                        (byte) 'c'
                        */
                };
    
        session.write(attributes, 0, attributes.length);
    }
    
    private int getArg0(int defaultValue) {
        return getArg(0, defaultValue, true);
    }
    
    private int getArg1(int defaultValue) {
        return getArg(1, defaultValue, true);
    }
    
    private void scroll() {
        //System.out.println("Scroll(): mTopMargin " + mTopMargin + " mBottomMargin " + mBottomMargin);
        scrollCounter++;
        screen.scroll(topMargin, bottomMargin, getStyle());
    }
    
    /**
     * Process the next ASCII character of a parameter.
     *
     * @param b The next ASCII character of the parameter sequence.
     */
    private void parseArg(byte b) {
        if (b >= '0' && b <= '9') {
            if (argIndex < args.length) {
                int oldValue = args[argIndex];
                int thisDigit = b - '0';
                int value;
                if (oldValue >= 0) {
                    value = oldValue * 10 + thisDigit;
                } else {
                    value = thisDigit;
                }
                args[argIndex] = value;
            }
            continueSequence();
        } else if (b == ';') {
            if (argIndex < args.length) {
                argIndex++;
            }
            continueSequence();
        } else {
            unknownSequence(b);
        }
    }
    
    /**
     * @noinspection SameParameterValue
     */
    private int getArg(int index, int defaultValue, boolean treatZeroAsDefault) {
        int result = args[index];
        if (result < 0 || (result == 0 && treatZeroAsDefault)) {
            result = defaultValue;
        }
        return result;
    }
    
    private void startCollectingOSCArgs() {
        OSCArgLength = 0;
    }
    
    private void collectOSCArgs(byte b) {
        if (OSCArgLength < MAX_OSC_STRING_LENGTH) {
            OSCArg[OSCArgLength++] = b;
            continueSequence();
        } else {
            unknownSequence(b);
        }
    }
    
    private void startTokenizingOSC() {
        OSCArgTokenizerIndex = 0;
    }
    
    private void unimplementedSequence(byte b) {
        if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            logError("unimplemented", b);
        }
        finishSequence();
    }
    
    private void unknownSequence(byte b) {
        if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            logError("unknown", b);
        }
        finishSequence();
    }
    
    private void unknownParameter(int parameter) {
        if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            String buf = "Unknown parameter" +
                    parameter;
            logError(buf);
        }
    }
    
    /** @noinspection SameParameterValue*/
    private String nextOSCString(int delimiter) {
        int start = OSCArgTokenizerIndex;
        int end = start;
        while (OSCArgTokenizerIndex < OSCArgLength) {
            byte b = OSCArg[OSCArgTokenizerIndex++];
            if ((int) b == delimiter) {
                break;
            }
            end++;
        }
        if (start == end) {
            return "";
        }
        return new String(OSCArg, start, end - start, StandardCharsets.UTF_8);
    }
    
    private void logError(String error) {
        if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            Log.e(EmulatorDebug.LOG_TAG, error);
        }
        finishSequence();
    }
    
    /**
     * @noinspection SameParameterValue
     */
    private int nextOSCInt(int delimiter) {
        int value = -1;
        while (OSCArgTokenizerIndex < OSCArgLength) {
            byte b = OSCArg[OSCArgTokenizerIndex++];
            if ((int) b == delimiter) {
                break;
            } else if (b >= '0' && b <= '9') {
                if (value < 0) {
                    value = 0;
                }
                value = value * 10 + b - '0';
            } else {
                unknownSequence(b);
            }
        }
        return value;
    }
    
    private void logError(String errorType, byte b) {
        if (EmulatorDebug.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            StringBuilder buf = new StringBuilder();
            buf.append(errorType);
            buf.append(" sequence ");
            buf.append(" EscapeState: ");
            buf.append(escapeState);
            buf.append(" char: '");
            buf.append((char) b);
            buf.append("' (");
            buf.append(b);
            buf.append(")");
            boolean firstArg = true;
            for (int i = 0; i <= argIndex; i++) {
                int value = args[i];
                if (value >= 0) {
                    if (firstArg) {
                        firstArg = false;
                        buf.append("args = ");
                    }
                    buf.append(String.format(Locale.getDefault(), "%d; ", value));
                }
            }
            logError(buf.toString());
        }
    }
    
    private void finishSequence() {
        escapeState = ESC_NONE;
    }
    
    private void emit(int c) {
        emit(c, getStyle());
    }
    
    private boolean autoWrapEnabled() {
        return (decFlags & K_WRAPAROUND_MODE_MASK) != 0;
    }
    
    /**
     * Send a UTF-16 char or surrogate pair to the screen.
     *
     * @param c A char[2] containing either a single UTF-16 char or a surrogate pair to be sent to the screen.
     */
    private void emit(char[] c) {
        if (Character.isHighSurrogate(c[0])) {
            emit(Character.toCodePoint(c[0], c[1]));
        } else {
            emit(c[0]);
        }
    }
    
    /**
     * Send a Unicode code point to the screen.
     *
     * @param c         The code point of the character to display
     * @param foreColor The foreground color of the character
     * @param backColor The background color of the character
     * @noinspection JavadocReference
     */
    private void emit(int c, int style) {
        boolean autoWrap = autoWrapEnabled();
        int width = UnicodeTranscript.charWidth(c);
        
        if (autoWrap) {
            if (cursorCol == columns - 1 && (aboutToAutoWrap || width == 2)) {
                screen.setLineWrap(cursorRow);
                cursorCol = 0;
                justWrapped = true;
                if (cursorRow + 1 < bottomMargin) {
                    cursorRow++;
                } else {
                    scroll();
                }
            }
        }
        
        if (mInsertMode & width != 0) { // Move character to right one space
            int destCol = cursorCol + width;
            if (destCol < columns) {
                screen.blockCopy(cursorCol, cursorRow, columns - destCol,
                        1, destCol, cursorRow);
            }
        }
        
        if (width == 0) {
            // Combining character -- store along with character it modifies
            if (justWrapped) {
                screen.set(columns - mLastEmittedCharWidth, cursorRow - 1, c, style);
            } else {
                screen.set(cursorCol - mLastEmittedCharWidth, cursorRow, c, style);
            }
        } else {
            screen.set(cursorCol, cursorRow, c, style);
            justWrapped = false;
        }
        
        if (autoWrap) {
            aboutToAutoWrap = (cursorCol == columns - 1);
    
            //Force line-wrap flag to trigger even for lines being typed
            if (aboutToAutoWrap) {
                screen.setLineWrap(cursorRow);
            }
        }
    
        cursorCol = Math.min(cursorCol + width, columns - 1);
        if (width > 0) {
            mLastEmittedCharWidth = width;
        }
    }
    
    private void emit(byte b) {
        if (useAlternateCharSet && b < 128) {
            emit(specialGraphicsCharMap[b]);
        } else {
            emit((int) b);
        }
    }
    
    /**
     * Send an array of UTF-16 chars to the screen.
     *
     * @param c A char[] array whose contents are to be sent to the screen.
     * @noinspection SameParameterValue
     */
    private void emit(char[] c, int offset, int length, int style) {
        for (int i = offset; i < length; ++i) {
            if (c[i] == 0) {
                break;
            }
            if (Character.isHighSurrogate(c[i])) {
                emit(Character.toCodePoint(c[i], c[i + 1]), style);
                ++i;
            } else {
                emit(c[i], style);
            }
        }
    }
    
    private void setCursorRowCol(int row, int col) {
        cursorRow = Math.min(row, rows - 1);
        cursorCol = Math.min(col, columns - 1);
        aboutToAutoWrap = false;
    }
    
    public int getScrollCounter() {
        return scrollCounter;
    }
    
    public void clearScrollCounter() {
        scrollCounter = 0;
    }
    
    /**
     * Reset the terminal emulator to its initial state.
     */
    public void reset() {
        cursorRow = 0;
        cursorCol = 0;
        argIndex = 0;
        continueSequence = false;
        escapeState = ESC_NONE;
        savedCursorRow = 0;
        savedCursorCol = 0;
        savedEffect = 0;
        savedDecFlags_DECSC_DECRC = 0;
        decFlags = 0;
        if (DEFAULT_TO_AUTOWRAP_ENABLED) {
            decFlags |= K_WRAPAROUND_MODE_MASK;
        }
        decFlags |= K_SHOW_CURSOR_MASK;
        savedDecFlags = 0;
        mInsertMode = false;
        topMargin = 0;
        bottomMargin = rows;
        aboutToAutoWrap = false;
        foreColor = defaultForeColor;
        backColor = defaultBackColor;
        keypadApplicationMode = false;
        alternateCharSet = false;
        charSet[0] = CHAR_SET_ASCII;
        charSet[1] = CHAR_SET_SPECIAL_GRAPHICS;
        computeEffectiveCharSet();
        // mProcessedCharCount is preserved unchanged.
        setDefaultTabStops();
        blockClear(0, 0, columns, rows);
    
        setUTF8Mode(defaultUTF8Mode);
        UTF8EscapeUsed = false;
        UTF8ToFollow = 0;
        UTF8ByteBuffer.clear();
        inputCharBuffer.clear();
    }
    
    public void setDefaultUTF8Mode(boolean defaultToUTF8Mode) {
        defaultUTF8Mode = defaultToUTF8Mode;
        if (!UTF8EscapeUsed) {
            setUTF8Mode(defaultToUTF8Mode);
        }
    }
    
    public boolean getUTF8Mode() {
        return UTF8Mode;
    }
    
    public void setUTF8Mode(boolean utf8Mode) {
        if (utf8Mode && !UTF8Mode) {
            UTF8ToFollow = 0;
            UTF8ByteBuffer.clear();
            inputCharBuffer.clear();
        }
        UTF8Mode = utf8Mode;
        if (UTF8ModeNotify != null) {
            UTF8ModeNotify.onUpdate();
        }
    }
    
    public void setUTF8ModeUpdateCallback(UpdateCallback utf8ModeNotify) {
        UTF8ModeNotify = utf8ModeNotify;
    }
    
    public void setColorScheme(ColorScheme scheme) {
        defaultForeColor = TextStyle.ciForeground;
        defaultBackColor = TextStyle.ciBackground;
        mainBuffer.setColorScheme(scheme);
        if (altBuffer != null) {
            altBuffer.setColorScheme(scheme);
        }
    }
    
    public String getSelectedText(int x1, int y1, int x2, int y2) {
        return screen.getSelectedText(x1, y1, x2, y2);
    }
    
    public void finish() {
        if (altBuffer != null) {
            altBuffer.finish();
            altBuffer = null;
        }
    }
}
