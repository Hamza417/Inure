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
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Scroller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;

import androidx.annotation.NonNull;
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompat;
import app.simple.inure.decorations.emulatorview.compat.ClipboardManagerCompatFactory;
import app.simple.inure.decorations.emulatorview.compat.KeycodeConstants;
import app.simple.inure.decorations.emulatorview.compat.Patterns;
import app.simple.inure.preferences.TerminalPreferences;

/**
 * A view on a {@link TermSession}.  Displays the terminal emulator's screen,
 * provides access to its scrollback buffer, and passes input through to the
 * terminal emulator.
 * <p>
 * If this view is inflated from an XML layout, you need to call {@link
 * #attachSession attachSession} and {@link #setDensity setDensity} before using
 * the view.  If creating this view from code, use the {@link
 * #EmulatorView(Context, TermSession, DisplayMetrics)} constructor, which will
 * take care of this for you.
 */
public class EmulatorView extends View implements GestureDetector.OnGestureListener {
    
    private final static String TAG = "EmulatorView";
    private final static boolean LOG_KEY_EVENTS = true;
    private final static boolean LOG_IME = true;
    
    /**
     * A hash table of underlying URLs to implement clickable links.
     */
    private final Hashtable <Integer, URLSpan[]> linkLayer = new Hashtable <>();
    /**
     * We defer some initialization until we have been layed out in the view
     * hierarchy. The boolean tracks when we know what our size is.
     */
    private boolean knownSize;
    // Set if initialization was deferred because a TermSession wasn't attached
    private boolean deferInit = false;
    private int visibleWidth;
    private int visibleHeight;
    private TermSession termSession;
    /**
     * Total width of each character, in pixels
     */
    private float characterWidth;
    /**
     * Total height of each character, in pixels
     */
    private int characterHeight;
    /**
     * Top-of-screen margin
     */
    private int topOfScreenMargin;
    /**
     * Used to render text
     */
    private TextRenderer textRenderer;
    
    private boolean cursorBlink = false;
    /**
     * Text size. Zero means 4 x 8 font.
     */
    private int textSize = 10;
    /**
     * Color scheme (default foreground/background colors).
     */
    private ColorScheme colorScheme = BaseTextRenderer.defaultColorScheme;
    private Paint foregroundPaint;
    private Paint backgroundPaint;
    
    /**
     * Our terminal emulator.
     */
    private TerminalEmulator emulator;
    private boolean useCookedIme;
    /**
     * The number of rows of text to display.
     */
    private int rows;
    /**
     * The number of columns of text to display.
     */
    private int columns;
    /**
     * The number of columns that are visible on the display.
     */
    
    private int visibleColumns;
    /*
     * The number of rows that are visible on the view
     */
    private int visibleRows;
    /**
     * The top row of text to display. Ranges from -activeTranscriptRows to 0
     */
    private int topRow;
    
    private static final int CURSOR_BLINK_PERIOD = 1000;
    private int leftColumn;
    private boolean cursorVisible = true;
    private final MouseTrackingFlingRunner mouseTrackingFlingRunner = new MouseTrackingFlingRunner();
    
    private boolean backKeySendsCharacter = false;
    private int controlKeyCode;
    private int fnKeyCode;
    private boolean isSelectingText = false;
    private boolean isControlKeySent = false;
    /**
     * Our message handler class. Implements a periodic callback.
     */
    private final Handler handler = new Handler();
    private boolean isFnKeySent = false;
    private static final int SELECT_TEXT_OFFSET_Y = -40;
    private float scaledDensity;
    private int selXAnchor = -1;
    private int selYAnchor = -1;
    private int selX1 = -1;
    private int selY1 = -1;
    private int selX2 = -1;
    
    /**
     * Routing alt and meta keyCodes away from the IME allows Alt key processing to work on
     * the Asus Transformer TF101.
     * It doesn't seem to harm anything else, but it also doesn't seem to be
     * required on other platforms.
     * <p>
     * This test should be refined as we learn more.
     */
    private final static boolean sTrapAltAndMeta = Build.MODEL.contains("Transformer TF101");
    private int selY2 = -1;
    /**
     * Called by the TermSession when the contents of the view need updating
     */
    private final UpdateCallback updateNotify = new UpdateCallback() {
        public void onUpdate() {
            if (isSelectingText) {
                int rowShift = emulator.getScrollCounter();
                selY1 -= rowShift;
                selY2 -= rowShift;
                selYAnchor -= rowShift;
            }
            
            emulator.clearScrollCounter();
            ensureCursorVisible();
            invalidate();
        }
    };
    
    private GestureDetector gestureDetector;
    private GestureDetector.OnGestureListener extGestureListener;
    private Scroller scroller;
    
    private final Runnable flingRunner = new Runnable() {
        public void run() {
            if (scroller.isFinished()) {
                return;
            }
            // Check whether mouse tracking was turned on during fling.
            if (isMouseTrackingActive()) {
                return;
            }
            
            boolean more = scroller.computeScrollOffset();
            int newTopRow = scroller.getCurrY();
            if (newTopRow != topRow) {
                topRow = newTopRow;
                invalidate();
            }
            
            if (more) {
                post(this);
            }
            
        }
    };
    
    private String imeBuffer = "";
    
    private static final MatchFilter sHttpMatchFilter = new HttpMatchFilter();
    private boolean mouseTracking;
    private final Runnable blinkCursor = new Runnable() {
        public void run() {
            if (cursorBlink) {
                cursorVisible = !cursorVisible;
                handler.postDelayed(this, CURSOR_BLINK_PERIOD);
            } else {
                cursorVisible = true;
            }
            
            // Perhaps just invalidate the character with the cursor.
            invalidate();
        }
    };
    private float density;
    private float scrollRemainder;
    private TermKeyListener keyListener;
    
    /**
     * Convert any URLs in the current row into a URLSpan,
     * and store that result in a hash table of URLSpan entries.
     *
     * @param row The number of the row to check for links
     * @return The number of lines in a multi-line-wrap set of links
     */
    private int createLinks(int row) {
        TranscriptScreen transcriptScreen = emulator.getScreen();
        char[] line = transcriptScreen.getScriptLine(row);
        int lineCount = 1;
        
        //Nothing to do if there's no text.
        if (line == null) {
            return lineCount;
        }
    
        /* If this is not a basic line, the array returned from getScriptLine()
         * could have arbitrary garbage at the end -- find the point at which
         * the line ends and only include that in the text to linkify.
         *
         * XXX: The fact that the array returned from getScriptLine() on a
         * basic line contains no garbage is an implementation detail -- the
         * documented behavior explicitly allows garbage at the end!
         */
        int lineLen;
        boolean textIsBasic = transcriptScreen.isBasicLine(row);
        if (textIsBasic) {
            lineLen = line.length;
        } else {
            // The end of the valid data is marked by a NUL character
            for (lineLen = 0; line[lineLen] != 0; ++lineLen) {
                Log.d(TAG, "lineLen: " + lineLen);
            }
        }
        
        SpannableStringBuilder textToLinkify = new SpannableStringBuilder(new String(line, 0, lineLen));
        
        boolean lineWrap = transcriptScreen.getScriptLineWrap(row);
        
        //While the current line has a wrap
        while (lineWrap) {
            //Get next line
            int nextRow = row + lineCount;
            line = transcriptScreen.getScriptLine(nextRow);
            
            //If next line is blank, don't try and append
            if (line == null) {
                break;
            }
            
            boolean lineIsBasic = transcriptScreen.isBasicLine(nextRow);
            if (textIsBasic && !lineIsBasic) {
                textIsBasic = lineIsBasic;
            }
            if (lineIsBasic) {
                lineLen = line.length;
            } else {
                // The end of the valid data is marked by a NUL character
                for (lineLen = 0; line[lineLen] != 0; ++lineLen) {
                    Log.d(TAG, "lineLen: " + lineLen);
                }
            }
            
            textToLinkify.append(new String(line, 0, lineLen));
            
            //Check if line after next is wrapped
            lineWrap = transcriptScreen.getScriptLineWrap(nextRow);
            ++lineCount;
        }
        
        Linkify.addLinks(textToLinkify, Patterns.WEB_URL,
                null, sHttpMatchFilter, null);
        URLSpan[] urls = textToLinkify.getSpans(0, textToLinkify.length(), URLSpan.class);
        if (urls.length > 0) {
            int columns = this.columns;
            
            //re-index row to 0 if it is negative
            int screenRow = row - topRow;
            
            //Create and initialize set of links
            URLSpan[][] linkRows = new URLSpan[lineCount][];
            for (int i = 0; i < lineCount; ++i) {
                linkRows[i] = new URLSpan[columns];
                Arrays.fill(linkRows[i], null);
            }
            
            //For each URL:
            for (URLSpan url : urls) {
                int spanStart = textToLinkify.getSpanStart(url);
                int spanEnd = textToLinkify.getSpanEnd(url);
        
                // Build accurate indices for links
                int startRow;
                int startCol;
                int endRow;
                int endCol;
                if (textIsBasic) {
                    /* endRow/endCol must be the last character of the link,
                     * not one after -- otherwise endRow might be too large */
                    int spanLastPos = spanEnd - 1;
                    // Basic line -- can assume one char per column
                    startRow = spanStart / this.columns;
                    startCol = spanStart % this.columns;
                    endRow = spanLastPos / this.columns;
                    endCol = spanLastPos % this.columns;
                } else {
                    /* Iterate over the line to get starting and ending columns
                     * for this span */
                    startRow = 0;
                    startCol = 0;
                    for (int i = 0; i < spanStart; ++i) {
                        char c = textToLinkify.charAt(i);
                        if (Character.isHighSurrogate(c)) {
                            ++i;
                            startCol += UnicodeTranscript.charWidth(c, textToLinkify.charAt(i));
                        } else {
                            startCol += UnicodeTranscript.charWidth(c);
                        }
                        if (startCol >= columns) {
                            ++startRow;
                            startCol %= columns;
                        }
                    }
            
                    endRow = startRow;
                    endCol = startCol;
                    for (int i = spanStart; i < spanEnd; ++i) {
                        char c = textToLinkify.charAt(i);
                        if (Character.isHighSurrogate(c)) {
                            ++i;
                            endCol += UnicodeTranscript.charWidth(c, textToLinkify.charAt(i));
                        } else {
                            endCol += UnicodeTranscript.charWidth(c);
                        }
                        if (endCol >= columns) {
                            ++endRow;
                            endCol %= columns;
                        }
                    }
                }
        
                //Fill linkRows with the URL where appropriate
                for (int i = startRow; i <= endRow; ++i) {
                    int runStart = (i == startRow) ? startCol : 0;
                    int runEnd = (i == endRow) ? endCol : this.columns - 1;
    
                    Arrays.fill(linkRows[i], runStart, runEnd + 1, url);
                }
            }
    
            //Add links into the link layer for later retrieval
            for (int i = 0; i < lineCount; ++i) {
                linkLayer.put(screenRow + i, linkRows[i]);
            }
        }
    
        return lineCount;
    }
    
    private void commonConstructor(Context context) {
        scroller = new Scroller(context);
        cursorBlink = TerminalPreferences.INSTANCE.getCursorBlinkState();
        mouseTrackingFlingRunner.scroller = new Scroller(context);
    }
    
    /**
     * Attach a {@link TermSession} to this view.
     *
     * @param session The {@link TermSession} this view will be displaying.
     */
    public void attachSession(TermSession session) {
        textRenderer = null;
        foregroundPaint = new Paint();
        backgroundPaint = new Paint();
        topRow = 0;
        leftColumn = 0;
        gestureDetector = new GestureDetector(getContext(), this);
        // gestureDetector.setIsLongpressEnabled(false);
        setVerticalScrollBarEnabled(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    
        termSession = session;
    
        keyListener = new TermKeyListener(session);
        session.setKeyListener(keyListener);
    
        // Do init now if it was deferred until a TermSession was attached
        if (deferInit) {
            deferInit = false;
            knownSize = true;
            initialize();
        }
    }
    
    /**
     * Create an <code>EmulatorView</code> for a {@link TermSession}.
     *
     * @param context The {@link Context} for the view.
     * @param session The {@link TermSession} this view will be displaying.
     * @param metrics The {@link DisplayMetrics} of the screen on which the view
     *                will be displayed.
     */
    public EmulatorView(Context context, TermSession session, DisplayMetrics metrics) {
        super(context);
        attachSession(session);
        setDensity(metrics);
        commonConstructor(context);
    }
    
    /**
     * Constructor called when inflating this view from XML.
     * <p>
     * You should call {@link #attachSession attachSession} and {@link
     * #setDensity setDensity} before using an <code>EmulatorView</code> created
     * using this constructor.
     */
    public EmulatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        commonConstructor(context);
    }
    
    /**
     * Constructor called when inflating this view from XML with a
     * default style set.
     * <p>
     * You should call {@link #attachSession attachSession} and {@link
     * #setDensity setDensity} before using an <code>EmulatorView</code> created
     * using this constructor.
     */
    public EmulatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        commonConstructor(context);
    }
    
    /**
     * Update the screen density for the screen on which the view is displayed.
     *
     * @param metrics The {@link DisplayMetrics} of the screen.
     */
    public void setDensity(DisplayMetrics metrics) {
        if (density == 0) {
            // First time we've known the screen density, so update font size
            textSize = (int) (textSize * metrics.density);
        }
        density = metrics.density;
        scaledDensity = metrics.scaledDensity;
    }
    
    /**
     * Inform the view that it is now visible on screen.
     */
    public void onResume() {
        updateSize(false);
        if (cursorBlink) {
            handler.postDelayed(blinkCursor, CURSOR_BLINK_PERIOD);
        }
        if (keyListener != null) {
            keyListener.onResume();
        }
    }
    
    /**
     * Inform the view that it is no longer visible on the screen.
     */
    public void onPause() {
        if (cursorBlink) {
            handler.removeCallbacks(blinkCursor);
        }
        if (keyListener != null) {
            keyListener.onPause();
        }
    }
    
    /**
     * Set this <code>EmulatorView</code>'s color scheme.
     *
     * @param scheme The {@link ColorScheme} to use (use null for the default
     *               scheme).
     * @see TermSession#setColorScheme
     * @see ColorScheme
     */
    public void setColorScheme(ColorScheme scheme) {
        //noinspection ReplaceNullCheck, it's more readable
        if (scheme == null) {
            colorScheme = BaseTextRenderer.defaultColorScheme;
        } else {
            colorScheme = scheme;
        }
    
        updateText();
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = useCookedIme ?
                EditorInfo.TYPE_CLASS_TEXT :
                EditorInfo.TYPE_NULL;
        return new BaseInputConnection(this, true) {
            /**
             * Used to handle composing text requests
             */
            private int mCursor;
            private int composingTextStart;
            private int composingTextEnd;
            private int mSelectedTextStart;
            private int mSelectedTextEnd;
    
            private void sendText(CharSequence text) {
                int n = text.length();
                char c;
                try {
                    for (int i = 0; i < n; i++) {
                        c = text.charAt(i);
                        if (Character.isHighSurrogate(c)) {
                            int codePoint;
                            if (++i < n) {
                                codePoint = Character.toCodePoint(c, text.charAt(i));
                            } else {
                                // Unicode Replacement Glyph, aka white question mark in black diamond.
                                codePoint = '\ufffd';
                            }
                            mapAndSend(codePoint);
                        } else {
                            mapAndSend(c);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "error writing ", e);
                }
            }
            
            private void mapAndSend(int c) throws IOException {
                int result = keyListener.mapControlChar(c);
                if (result < TermKeyListener.KEYCODE_OFFSET) {
                    termSession.write(result);
                } else {
                    keyListener.handleKeyCode(result - TermKeyListener.KEYCODE_OFFSET, null, getKeypadApplicationMode());
                }
                clearSpecialKeyStatus();
            }
            
            public boolean beginBatchEdit() {
                if (LOG_IME) {
                    Log.w(TAG, "beginBatchEdit");
                }
                setImeBuffer("");
                mCursor = 0;
                composingTextStart = 0;
                composingTextEnd = 0;
                return true;
            }
            
            public boolean clearMetaKeyStates(int arg0) {
                if (LOG_IME) {
                    Log.w(TAG, "clearMetaKeyStates " + arg0);
                }
                return false;
            }
            
            public boolean commitCompletion(CompletionInfo arg0) {
                if (LOG_IME) {
                    Log.w(TAG, "commitCompletion " + arg0);
                }
                return false;
            }
            
            public boolean endBatchEdit() {
                if (LOG_IME) {
                    Log.w(TAG, "endBatchEdit");
                }
                return true;
            }
            
            public boolean finishComposingText() {
                if (LOG_IME) {
                    Log.w(TAG, "finishComposingText");
                }
                sendText(imeBuffer);
                setImeBuffer("");
                composingTextStart = 0;
                composingTextEnd = 0;
                mCursor = 0;
                return true;
            }
            
            public int getCursorCapsMode(int reqModes) {
                if (LOG_IME) {
                    Log.w(TAG, "getCursorCapsMode(" + reqModes + ")");
                }
                int mode = 0;
                if ((reqModes & TextUtils.CAP_MODE_CHARACTERS) != 0) {
                    mode |= TextUtils.CAP_MODE_CHARACTERS;
                }
                return mode;
            }
            
            public ExtractedText getExtractedText(ExtractedTextRequest arg0,
                    int arg1) {
                if (LOG_IME) {
                    Log.w(TAG, "getExtractedText" + arg0 + "," + arg1);
                }
                return null;
            }
            
            public CharSequence getTextAfterCursor(int n, int flags) {
                if (LOG_IME) {
                    Log.w(TAG, "getTextAfterCursor(" + n + "," + flags + ")");
                }
                int len = Math.min(n, imeBuffer.length() - mCursor);
                if (len <= 0 || mCursor < 0 || mCursor >= imeBuffer.length()) {
                    return "";
                }
                return imeBuffer.substring(mCursor, mCursor + len);
            }
            
            public CharSequence getTextBeforeCursor(int n, int flags) {
                if (LOG_IME) {
                    Log.w(TAG, "getTextBeforeCursor(" + n + "," + flags + ")");
                }
                int len = Math.min(n, mCursor);
                if (len <= 0 || mCursor < 0 || mCursor >= imeBuffer.length()) {
                    return "";
                }
                return imeBuffer.substring(mCursor - len, mCursor);
            }
            
            public boolean performContextMenuAction(int arg0) {
                if (LOG_IME) {
                    Log.w(TAG, "performContextMenuAction" + arg0);
                }
                return true;
            }
            
            public boolean performPrivateCommand(String arg0, Bundle arg1) {
                if (LOG_IME) {
                    Log.w(TAG, "performPrivateCommand" + arg0 + "," + arg1);
                }
                return true;
            }
            
            public boolean reportFullscreenMode(boolean arg0) {
                if (LOG_IME) {
                    Log.w(TAG, "reportFullscreenMode" + arg0);
                }
                return true;
            }
            
            public boolean commitCorrection(CorrectionInfo correctionInfo) {
                if (LOG_IME) {
                    Log.w(TAG, "commitCorrection");
                }
                return true;
            }
            
            public boolean commitText(CharSequence text, int newCursorPosition) {
                if (LOG_IME) {
                    Log.w(TAG, "commitText(\"" + text + "\", " + newCursorPosition + ")");
                }
                clearComposingText();
                sendText(text);
                setImeBuffer("");
                mCursor = 0;
                return true;
            }
            
            private void clearComposingText() {
                int len = imeBuffer.length();
                if (composingTextStart > len || composingTextEnd > len) {
                    composingTextEnd = composingTextStart = 0;
                    return;
                }
                setImeBuffer(imeBuffer.substring(0, composingTextStart) +
                        imeBuffer.substring(composingTextEnd));
                if (mCursor < composingTextStart) {
                    // do nothing
                    Log.d(TAG, "mCursor < mComposingTextStart");
                } else if (mCursor < composingTextEnd) {
                    mCursor = composingTextStart;
                } else {
                    mCursor -= composingTextEnd - composingTextStart;
                }
                composingTextEnd = composingTextStart = 0;
            }
            
            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                if (LOG_IME) {
                    Log.w(TAG, "deleteSurroundingText(" + leftLength +
                            "," + rightLength + ")");
                }
                if (leftLength > 0) {
                    for (int i = 0; i < leftLength; i++) {
                        sendKeyEvent(
                                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    }
                } else if ((leftLength == 0) && (rightLength == 0)) {
                    // Delete key held down / repeating
                    sendKeyEvent(
                            new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                }
                // TODO: handle forward deletes.
                return true;
            }
            
            public boolean performEditorAction(int actionCode) {
                if (LOG_IME) {
                    Log.w(TAG, "performEditorAction(" + actionCode + ")");
                }
                if (actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // The "return" key has been pressed on the IME.
                    sendText("\r");
                }
                return true;
            }
            
            public boolean sendKeyEvent(KeyEvent event) {
                if (LOG_IME) {
                    Log.w(TAG, "sendKeyEvent(" + event + ")");
                }
                // Some keys are sent here rather than to commitText.
                // In particular, del and the digit keys are sent here.
                // (And I have reports that the HTC Magic also sends Return here.)
                // As a bit of defensive programming, handle every key.
                dispatchKeyEvent(event);
                return true;
            }
            
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                if (LOG_IME) {
                    Log.w(TAG, "setComposingText(\"" + text + "\", " + newCursorPosition + ")");
                }
                int len = imeBuffer.length();
                if (composingTextStart > len || composingTextEnd > len) {
                    return false;
                }
                setImeBuffer(imeBuffer.substring(0, composingTextStart) +
                        text + imeBuffer.substring(composingTextEnd));
                composingTextEnd = composingTextStart + text.length();
                mCursor = newCursorPosition > 0 ? composingTextEnd + newCursorPosition - 1
                        : composingTextStart - newCursorPosition;
                return true;
            }
            
            public boolean setSelection(int start, int end) {
                if (LOG_IME) {
                    Log.w(TAG, "setSelection" + start + "," + end);
                }
                int length = imeBuffer.length();
                if (start == end && start > 0 && start < length) {
                    mSelectedTextStart = mSelectedTextEnd = 0;
                    mCursor = start;
                } else if (start < end && start > 0 && end < length) {
                    mSelectedTextStart = start;
                    mSelectedTextEnd = end;
                    mCursor = start;
                }
                return true;
            }
            
            public boolean setComposingRegion(int start, int end) {
                if (LOG_IME) {
                    Log.w(TAG, "setComposingRegion " + start + "," + end);
                }
                if (start < end && start > 0 && end < imeBuffer.length()) {
                    clearComposingText();
                    composingTextStart = start;
                    composingTextEnd = end;
                }
                return true;
            }
            
            public CharSequence getSelectedText(int flags) {
                if (LOG_IME) {
                    Log.w(TAG, "getSelectedText " + flags);
                }
                int len = imeBuffer.length();
                if (mSelectedTextEnd >= len || mSelectedTextStart > mSelectedTextEnd) {
                    return "";
                }
                return imeBuffer.substring(mSelectedTextStart, mSelectedTextEnd + 1);
            }
            
        };
    }
    
    private void setImeBuffer(String buffer) {
        if (!buffer.equals(imeBuffer)) {
            invalidate();
        }
        imeBuffer = buffer;
    }
    
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
    
    /**
     * Set a {@link GestureDetector.OnGestureListener
     * GestureDetector.OnGestureListener} to receive gestures performed on this
     * view.  Can be used to implement additional
     * functionality via touch gestures or override built-in gestures.
     *
     * @param listener The {@link
     *                 GestureDetector.OnGestureListener
     *                 GestureDetector.OnGestureListener} which will receive
     *                 gestures.
     */
    public void setExtGestureListener(GestureDetector.OnGestureListener listener) {
        extGestureListener = listener;
    }
    
    /**
     * Compute the vertical extent of the horizontal scrollbar's thumb within
     * the vertical range. This value is used to compute the length of the thumb
     * within the scrollbar's track.
     */
    @Override
    protected int computeVerticalScrollExtent() {
        return rows;
    }
    
    /**
     * Get the terminal emulator's keypad application mode.
     */
    public boolean getKeypadApplicationMode() {
        return emulator.getKeypadApplicationMode();
    }
    
    /**
     * Compute the vertical offset of the vertical scrollbar's thumb within the
     * horizontal range. This value is used to compute the position of the thumb
     * within the scrollbar's track.
     */
    @Override
    protected int computeVerticalScrollOffset() {
        return emulator.getScreen().getActiveRows() + topRow - rows;
    }
    
    /**
     * Compute the vertical range that the vertical scrollbar represents.
     */
    @Override
    protected int computeVerticalScrollRange() {
        return emulator.getScreen().getActiveRows();
    }
    
    /**
     * Call this to initialize the view.
     */
    private void initialize() {
        TermSession session = termSession;
    
        updateText();
        
        emulator = session.getTerminalEmulator();
        session.setUpdateCallback(updateNotify);
        
        requestFocus();
    }
    
    /**
     * Get the {@link TermSession} corresponding to this view.
     *
     * @return The {@link TermSession} object for this view.
     */
    public TermSession getTermSession() {
        return termSession;
    }
    
    /**
     * Get the width of the visible portion of this view.
     *
     * @return The width of the visible portion of this view, in pixels.
     */
    public int getVisibleWidth() {
        return visibleWidth;
    }
    
    /**
     * Get the height of the visible portion of this view.
     *
     * @return The height of the visible portion of this view, in pixels.
     */
    public int getVisibleHeight() {
        return visibleHeight;
    }
    
    /**
     * Gets the visible number of rows for the view, useful when updating Ptysize with the correct number of rows/columns
     *
     * @return The rows for the visible number of rows, this is calculate in updateSize(int w, int h), please call
     * updateSize(true) if the view changed, to get the correct calculation before calling this.
     */
    public int getVisibleRows() {
        return visibleRows;
    }
    
    /**
     * Gets the visible number of columns for the view, again useful to get when updating PTYsize
     *
     * @return the columns for the visisble view, please call updateSize(true) to re-calculate this if the view has changed
     */
    public int getVisibleColumns() {
        return visibleColumns;
    }
    
    /**
     * Page the terminal view (scroll it up or down by <code>delta</code>
     * screenfuls).
     *
     * @param delta The number of screens to scroll. Positive means scroll down,
     *              negative means scroll up.
     */
    public void page(int delta) {
        topRow =
                Math.min(0, Math.max(-(emulator.getScreen()
                        .getActiveTranscriptRows()), topRow + rows * delta));
        invalidate();
    }
    
    /**
     * Page the terminal view horizontally.
     *
     * @param deltaColumns the number of columns to scroll. Positive scrolls to
     *                     the right.
     */
    public void pageHorizontal(int deltaColumns) {
        leftColumn =
                Math.max(0, Math.min(leftColumn + deltaColumns, columns
                        - visibleColumns));
        invalidate();
    }
    
    /**
     * Sets the text size, which in turn sets the number of rows and columns.
     *
     * @param fontSize the new font size, in density-independent pixels.
     */
    public void setTextSize(int fontSize) {
        textSize = (int) (fontSize * density);
        updateText();
    }
    
    /**
     * Sets the IME mode ("cooked" or "raw").
     *
     * @param useCookedIME Whether the IME should be used in cooked mode.
     */
    public void setUseCookedIME(boolean useCookedIME) {
        useCookedIme = useCookedIME;
    }
    
    /**
     * Send a single mouse event code to the terminal.
     */
    private void sendMouseEventCode(MotionEvent e, int button_code) {
        int x = (int) (e.getX() / characterWidth) + 1;
        int y = (int) ((e.getY() - topOfScreenMargin) / characterHeight) + 1;
        // Clip to screen, and clip to the limits of 8-bit data.
        boolean out_of_bounds =
                x < 1 || y < 1 ||
                        x > columns || y > rows ||
                        x > 255 - 32 || y > 255 - 32;
        //Log.d(TAG, "mouse button "+x+","+y+","+button_code+",oob="+out_of_bounds);
        if (button_code < 0 || button_code > 255 - 32) {
            Log.e(TAG, "mouse button_code out of range: " + button_code);
            return;
        }
        if (!out_of_bounds) {
            byte[] data = {
                    '\033', '[', 'M',
                    (byte) (32 + button_code),
                    (byte) (32 + x),
                    (byte) (32 + y)};
            termSession.write(data, 0, data.length);
        }
    }
    
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        if (extGestureListener != null && extGestureListener.onSingleTapUp(e)) {
            return true;
        }
        
        if (isMouseTrackingActive()) {
            sendMouseEventCode(e, 0); // BTN1 press
            sendMouseEventCode(e, 3); // release
        }
        
        requestFocus();
        return true;
    }
    
    /**
     * Returns true if mouse events are being sent as escape sequences to the terminal.
     */
    public boolean isMouseTrackingActive() {
        return emulator.getMouseTrackingMode() != 0 && mouseTracking;
    }
    
    public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        if (extGestureListener != null && extGestureListener.onScroll(e1, e2, distanceX, distanceY)) {
            return true;
        }
        
        distanceY += scrollRemainder;
        int deltaRows = (int) (distanceY / characterHeight);
        scrollRemainder = distanceY - deltaRows * characterHeight;
        
        if (isMouseTrackingActive()) {
            // Send mouse wheel events to terminal.
            for (; deltaRows > 0; deltaRows--) {
                sendMouseEventCode(e1, 65);
            }
            for (; deltaRows < 0; deltaRows++) {
                sendMouseEventCode(e1, 64);
            }
            return true;
        }
        
        topRow =
                Math.min(0, Math.max(-(emulator.getScreen()
                        .getActiveTranscriptRows()), topRow + deltaRows));
        invalidate();
        
        return true;
    }
    
    // Begin GestureDetector.OnGestureListener methods
    
    public boolean onJumpTapDown(MotionEvent e1, MotionEvent e2) {
        // Scroll to bottom
        topRow = 0;
        invalidate();
        return true;
    }
    
    public void onLongPress(@NonNull MotionEvent e) {
        // XXX hook into external gesture listener
        showContextMenu();
    }
    
    public boolean onJumpTapUp(MotionEvent e1, MotionEvent e2) {
        // Scroll to top
        topRow = -emulator.getScreen().getActiveTranscriptRows();
        invalidate();
        return true;
    }
    
    public void onSingleTapConfirmed(MotionEvent e) {
    }
    
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX,
            float velocityY) {
        if (extGestureListener != null && extGestureListener.onFling(e1, e2, velocityX, velocityY)) {
            return true;
        }
        
        scrollRemainder = 0.0f;
        
        if (isMouseTrackingActive()) {
            mouseTrackingFlingRunner.fling(e1, velocityX, velocityY);
        } else {
            float SCALE = 0.25f;
            scroller.fling(0, topRow,
                    -(int) (velocityX * SCALE), -(int) (velocityY * SCALE),
                    0, 0,
                    -emulator.getScreen().getActiveTranscriptRows(), 0);
            // onScroll(e1, e2, 0.1f * velocityX, -0.1f * velocityY);
            post(flingRunner);
        }
        return true;
    }
    
    public void onShowPress(@NonNull MotionEvent e) {
        if (extGestureListener != null) {
            extGestureListener.onShowPress(e);
        }
    }
    
    public boolean onDown(@NonNull MotionEvent e) {
        if (extGestureListener != null && extGestureListener.onDown(e)) {
            return true;
        }
        scrollRemainder = 0.0f;
        return true;
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSelectingText) {
            return onTouchEventWhileSelectingText(ev);
        } else {
            return gestureDetector.onTouchEvent(ev);
        }
    }
    
    private boolean onTouchEventWhileSelectingText(MotionEvent ev) {
        int action = ev.getAction();
        int cx = (int) (ev.getX() / characterWidth);
        int cy = Math.max(0,
                (int) ((ev.getY() + SELECT_TEXT_OFFSET_Y * scaledDensity)
                        / characterHeight) + topRow);
        switch (action) {
            case MotionEvent.ACTION_DOWN -> {
                selXAnchor = cx;
                selYAnchor = cy;
                selX1 = cx;
                selY1 = cy;
                selX2 = selX1;
                selY2 = selY1;
            }
            case MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                int minX = Math.min(selXAnchor, cx);
                int maxX = Math.max(selXAnchor, cx);
                int minY = Math.min(selYAnchor, cy);
                int maxY = Math.max(selYAnchor, cy);
                selX1 = minX;
                selY1 = minY;
                selX2 = maxX;
                selY2 = maxY;
            
                if (action == MotionEvent.ACTION_UP) {
                    ClipboardManagerCompat clip = ClipboardManagerCompatFactory
                            .getManager(getContext().getApplicationContext());
                    clip.setText(getSelectedText().trim());
                    toggleSelectingText();
                }
            
                invalidate();
            }
            default -> {
                toggleSelectingText();
                invalidate();
            }
        }
        return true;
    }
    
    // End GestureDetector.OnGestureListener methods
    
    private void clearSpecialKeyStatus() {
        if (isControlKeySent) {
            isControlKeySent = false;
            keyListener.handleControlKey(false);
            invalidate();
        }
        if (isFnKeySent) {
            isFnKeySent = false;
            keyListener.handleFnKey(false);
            invalidate();
        }
    }
    
    private void updateText() {
        ColorScheme scheme = colorScheme;
        if (textSize > 0) {
            textRenderer = new PaintRenderer(textSize, scheme);
        } else {
            textRenderer = new Bitmap4x8FontRenderer(getResources(), scheme);
        }
        
        foregroundPaint.setColor(scheme.getForeColor());
        backgroundPaint.setColor(scheme.getBackColor());
        characterWidth = textRenderer.getCharacterWidth();
        characterHeight = textRenderer.getCharacterHeight();
        
        updateSize(true);
    }
    
    /**
     * Called when a key is pressed in the view.
     *
     * @param keyCode The keycode of the key which was pressed.
     * @param event   A {@link KeyEvent} describing the event.
     * @return Whether the event was handled.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS) {
            Log.w(TAG, "onKeyDown " + keyCode);
        }
        if (handleControlKey(keyCode, true)) {
            return true;
        } else if (handleFnKey(keyCode, true)) {
            return true;
        } else if (isSystemKey(keyCode, event)) {
            if (isInterceptedSystemKey(keyCode)) {
                // Don't intercept the system keys
                return super.onKeyDown(keyCode, event);
            }
        }
        
        // Translate the keyCode into an ASCII character.
        try {
            int oldCombiningAccent = keyListener.getCombiningAccent();
            int oldCursorMode = keyListener.getCursorMode();
            keyListener.keyDown(keyCode, event, getKeypadApplicationMode(),
                    TermKeyListener.isEventFromToggleDevice(event));
            if (keyListener.getCombiningAccent() != oldCombiningAccent
                    || keyListener.getCursorMode() != oldCursorMode) {
                invalidate();
            }
        } catch (IOException e) {
            // Ignore I/O exceptions
        }
        
        return true;
    }
    
    /**
     * Do we want to intercept this system key?
     */
    private boolean isInterceptedSystemKey(int keyCode) {
        return keyCode != KeyEvent.KEYCODE_BACK || !backKeySendsCharacter;
    }
    
    /**
     * Called when a key is released in the view.
     *
     * @param keyCode The keycode of the key which was released.
     * @param event   A {@link KeyEvent} describing the event.
     * @return Whether the event was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (LOG_KEY_EVENTS) {
            Log.w(TAG, "onKeyUp " + keyCode);
        }
        if (handleControlKey(keyCode, false)) {
            return true;
        } else if (handleFnKey(keyCode, false)) {
            return true;
        } else if (isSystemKey(keyCode, event)) {
            // Don't intercept the system keys
            if (isInterceptedSystemKey(keyCode)) {
                return super.onKeyUp(keyCode, event);
            }
        }
        
        keyListener.keyUp(keyCode, event);
        clearSpecialKeyStatus();
        return true;
    }
    
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (sTrapAltAndMeta) {
            boolean altEsc = keyListener.getAltSendsEsc();
            boolean altOn = (event.getMetaState() & KeyEvent.META_ALT_ON) != 0;
            boolean metaOn = (event.getMetaState() & KeyEvent.META_META_ON) != 0;
            boolean altPressed = (keyCode == KeyEvent.KEYCODE_ALT_LEFT)
                    || (keyCode == KeyEvent.KEYCODE_ALT_RIGHT);
            boolean altActive = keyListener.isAltActive();
            if (altEsc && (altOn || altPressed || altActive || metaOn)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    return onKeyDown(keyCode, event);
                } else {
                    return onKeyUp(keyCode, event);
                }
            }
        }
        
        if (handleHardwareControlKey(keyCode, event)) {
            return true;
        }
        
        if (keyListener.isCtrlActive()) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                return onKeyDown(keyCode, event);
            } else {
                return onKeyUp(keyCode, event);
            }
        }
        
        return super.onKeyPreIme(keyCode, event);
    }
    
    private boolean handleControlKey(int keyCode, boolean down) {
        if (keyCode == controlKeyCode) {
            if (LOG_KEY_EVENTS) {
                Log.w(TAG, "handleControlKey " + keyCode);
            }
            keyListener.handleControlKey(down);
            invalidate();
            return true;
        }
        return false;
    }
    
    private boolean handleHardwareControlKey(int keyCode, KeyEvent event) {
        if (keyCode == KeycodeConstants.KEYCODE_CTRL_LEFT ||
                keyCode == KeycodeConstants.KEYCODE_CTRL_RIGHT) {
            if (LOG_KEY_EVENTS) {
                Log.w(TAG, "handleHardwareControlKey " + keyCode);
            }
            boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
            keyListener.handleHardwareControlKey(down);
            invalidate();
            return true;
        }
        return false;
    }
    
    private boolean handleFnKey(int keyCode, boolean down) {
        if (keyCode == fnKeyCode) {
            if (LOG_KEY_EVENTS) {
                Log.w(TAG, "handleFnKey " + keyCode);
            }
            keyListener.handleFnKey(down);
            invalidate();
            return true;
        }
        return false;
    }
    
    private boolean isSystemKey(int ignoredKeyCode, KeyEvent event) {
        return event.isSystem();
    }
    
    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        if (termSession == null) {
            // Not ready, defer until TermSession is attached
            deferInit = true;
            return;
        }
        
        if (!knownSize) {
            knownSize = true;
            initialize();
        } else {
            updateSize(false);
        }
    }
    
    private void updateSize(int w, int h) {
        columns = Math.max(1, (int) (((float) w) / characterWidth));
        visibleColumns = Math.max(1, (int) (((float) visibleWidth) / characterWidth));
        
        topOfScreenMargin = textRenderer.getTopMargin();
        rows = Math.max(1, (h - topOfScreenMargin) / characterHeight);
        visibleRows = Math.max(1, (visibleHeight - topOfScreenMargin) / characterHeight);
        termSession.updateSize(columns, rows);
        
        // Reset our paging:
        topRow = 0;
        leftColumn = 0;
        
        invalidate();
    }
    
    /**
     * Update the view's idea of its size.
     *
     * @param force Whether a size adjustment should be performed even if the
     *              view's size has not changed.
     */
    public void updateSize(boolean force) {
        //Need to clear saved links on each display refresh
        linkLayer.clear();
        if (knownSize) {
            int w = getWidth();
            int h = getHeight();
            // Log.w("Term", "(" + w + ", " + h + ")");
            if (force || w != visibleWidth || h != visibleHeight) {
                visibleWidth = w;
                visibleHeight = h;
                updateSize(visibleWidth, visibleHeight);
            }
        }
    }
    
    /**
     * Draw the view to the provided {@link Canvas}.
     *
     * @param canvas The {@link Canvas} to draw the view to.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        updateSize(false);
        
        if (emulator == null) {
            // Not ready yet
            return;
        }
        
        int w = getWidth();
        int h = getHeight();
        
        boolean reverseVideo = emulator.getReverseVideo();
        textRenderer.setReverseVideo(reverseVideo);
        
        Paint backgroundPaint = reverseVideo ? foregroundPaint : this.backgroundPaint;
        canvas.drawRect(0, 0, w, h, backgroundPaint);
        float x = -leftColumn * characterWidth;
        float y = characterHeight + topOfScreenMargin;
        int endLine = topRow + rows;
        int cx = emulator.getCursorCol();
        int cy = emulator.getCursorRow();
        boolean cursorVisible = this.cursorVisible && emulator.getShowCursor();
        String effectiveImeBuffer = imeBuffer;
        int combiningAccent = keyListener.getCombiningAccent();
        if (combiningAccent != 0) {
            effectiveImeBuffer += String.valueOf((char) combiningAccent);
        }
        int cursorStyle = keyListener.getCursorMode();
        
        int linkLinesToSkip = 0; //for multi-line links
        
        for (int i = topRow; i < endLine; i++) {
            int cursorX = -1;
            if (i == cy && cursorVisible) {
                cursorX = cx;
            }
            int selx1 = -1;
            int selx2 = -1;
            if (i >= selY1 && i <= selY2) {
                if (i == selY1) {
                    selx1 = selX1;
                }
                if (i == selY2) {
                    selx2 = selX2;
                } else {
                    selx2 = columns;
                }
            }
            emulator.getScreen().drawText(i, canvas, x, y, textRenderer, cursorX, selx1, selx2, effectiveImeBuffer, cursorStyle);
            y += characterHeight;
            //if no lines to skip, create links for the line being drawn
            if (linkLinesToSkip == 0) {
                linkLinesToSkip = createLinks(i);
            }
            
            //createLinks always returns at least 1
            --linkLinesToSkip;
        }
    }
    
    private void ensureCursorVisible() {
        topRow = 0;
        if (visibleColumns > 0) {
            int cx = emulator.getCursorCol();
            int visibleCursorX = emulator.getCursorCol() - leftColumn;
            if (visibleCursorX < 0) {
                leftColumn = cx;
            } else if (visibleCursorX >= visibleColumns) {
                leftColumn = (cx - visibleColumns) + 1;
            }
        }
    }
    
    /**
     * Toggle text selection mode in the view.
     */
    public void toggleSelectingText() {
        isSelectingText = !isSelectingText;
        setVerticalScrollBarEnabled(!isSelectingText);
        if (!isSelectingText) {
            selX1 = -1;
            selY1 = -1;
            selX2 = -1;
            selY2 = -1;
        }
    }
    
    /**
     * Whether the view is currently in text selection mode.
     */
    public boolean getSelectingText() {
        return isSelectingText;
    }
    
    /**
     * Get selected text.
     *
     * @return A {@link String} with the selected text.
     */
    public String getSelectedText() {
        return emulator.getSelectedText(selX1, selY1, selX2, selY2);
    }
    
    /**
     * Send an Fn key event to the terminal.  The Fn modifier key can be used to
     * generate various special characters and escape codes.
     */
    public void sendFnKey() {
        isFnKeySent = true;
        keyListener.handleFnKey(true);
        invalidate();
    }
    
    /**
     * Get the URL for the link displayed at the specified screen coordinates.
     *
     * @param x The x coordinate being queried (from 0 to screen width)
     * @param y The y coordinate being queried (from 0 to screen height)
     * @return The URL for the link at the specified screen coordinates, or
     * null if no link exists there.
     */
    public String getURLat(float x, float y) {
        float width = getWidth();
        float height = getHeight();
        
        //Check for division by zero
        //If width or height is zero, there are probably no links around, so return null.
        if (width == 0 || height == 0) {
            return null;
        }
        
        //Get fraction of total screen
        float x_pos = x / width;
        float y_pos = y / height;
        
        //Convert to integer row/column index
        int row = (int) Math.floor(y_pos * rows);
        int col = (int) Math.floor(x_pos * columns);
        
        //Grab row from link layer
        URLSpan[] linkRow = linkLayer.get(row);
        URLSpan link;
        
        //If row exists, and link exists at column, return it
        if (linkRow != null && (link = linkRow[col]) != null) {
            return link.getURL();
        } else {
            return null;
        }
    }
    
    /**
     * Send a Ctrl key event to the terminal.
     */
    public void sendControlKey() {
        isControlKeySent = true;
        keyListener.handleControlKey(true);
        invalidate();
    }
    
    /**
     * Accept links that start with http[s]:
     */
    private static class HttpMatchFilter implements MatchFilter {
        public boolean acceptMatch(CharSequence s, int start, int end) {
            return startsWith(s, start, end, "http:") ||
                    startsWith(s, start, end, "https:");
        }
        
        private boolean startsWith(CharSequence s, int start, int end, String prefix) {
            int prefixLen = prefix.length();
            int fragmentLen = end - start;
            if (prefixLen > fragmentLen) {
                return false;
            }
    
            for (int i = 0; i < prefixLen; i++) {
                if (s.charAt(start + i) != prefix.charAt(i)) {
                    return false;
                }
            }
    
            return true;
        }
    }
    
    /**
     * Set the key code to be sent when the Back key is pressed.
     */
    public void setBackKeyCharacter(int keyCode) {
        keyListener.setBackKeyCharacter(keyCode);
        backKeySendsCharacter = (keyCode != 0);
    }
    
    /**
     * Set whether to prepend the ESC keycode to the character when when pressing
     * the ALT Key.
     *
     * @param flag Whether ALT should send ESC+keycode.
     */
    public void setAltSendsEsc(boolean flag) {
        keyListener.setAltSendsEsc(flag);
    }
    
    /**
     * Set the keycode corresponding to the Ctrl key.
     */
    public void setControlKeyCode(int keyCode) {
        controlKeyCode = keyCode;
    }
    
    /**
     * Set the keycode corresponding to the Fn key.
     */
    public void setFnKeyCode(int keyCode) {
        fnKeyCode = keyCode;
    }
    
    public void setTermType(String termType) {
        keyListener.setTermType(termType);
    }
    
    /**
     * Set whether mouse events should be sent to the terminal as escape codes.
     */
    public void setMouseTracking(boolean flag) {
        mouseTracking = flag;
    }
    
    /**
     * Sends mouse wheel codes to terminal in response to fling.
     */
    private class MouseTrackingFlingRunner implements Runnable {
        private Scroller scroller;
        private int lastY;
        private MotionEvent motionEvent;
        
        public void fling(MotionEvent e, float velocityX, float velocityY) {
            float SCALE = 0.15f;
            scroller.fling(0, 0,
                    -(int) (velocityX * SCALE), -(int) (velocityY * SCALE),
                    0, 0, -100, 100);
            lastY = 0;
            motionEvent = e;
            post(this);
        }
        
        public void run() {
            if (scroller.isFinished()) {
                return;
            }
            // Check whether mouse tracking was turned off during fling.
            if (!isMouseTrackingActive()) {
                return;
            }
    
            boolean more = scroller.computeScrollOffset();
            int newY = scroller.getCurrY();
            for (; lastY < newY; lastY++) {
                sendMouseEventCode(motionEvent, 65);
            }
            
            for (; lastY > newY; lastY--) {
                sendMouseEventCode(motionEvent, 64);
            }
    
            if (more) {
                post(this);
            }
        }
    }
}
