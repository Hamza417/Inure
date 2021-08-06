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

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListView;

import app.simple.inure.R;
import app.simple.inure.decorations.terminal.Terminal.CellRun;
import app.simple.inure.decorations.terminal.Terminal.TerminalClient;
import app.simple.inure.util.ColorUtils;

import static app.simple.inure.decorations.terminal.Terminal.TAG;

/**
 * Rendered contents of a {@link Terminal} session.
 */
public class TerminalView extends ListView {
    private static final boolean LOGD = true;
    
    private static final boolean SCROLL_ON_DAMAGE = false;
    private static final boolean SCROLL_ON_INPUT = true;
    private final TerminalMetrics mMetrics = new TerminalMetrics();
    private final TerminalKeys mTermKeys = new TerminalKeys();
    
    private final TerminalClient mClient = new TerminalClient() {
        @Override
        public void onDamage(final int startRow, final int endRow, int startCol, int endCol) {
            post(mDamageRunnable);
        }
        
        @Override
        public void onMoveRect(int destStartRow, int destEndRow, int destStartCol, int destEndCol,
                int srcStartRow, int srcEndRow, int srcStartCol, int srcEndCol) {
            post(mDamageRunnable);
        }
        
        @Override
        public void onMoveCursor(int posRow, int posCol, int oldPosRow, int oldPosCol, int visible) {
            post(mDamageRunnable);
        }
        
        @Override
        public void onBell() {
            Log.i(TAG, "DING!");
        }
    };
    
    private Terminal mTerm;
    private boolean mScrolled;
    private final Runnable mDamageRunnable = () -> {
        invalidateViews();
        if (SCROLL_ON_DAMAGE) {
            scrollToBottom(true);
        }
    };
    private int mRows;
    private int mCols;
    private int mScrollRows;
    private final BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TerminalLineView view;
            if (convertView != null) {
                view = (TerminalLineView) convertView;
            }
            else {
                view = new TerminalLineView(parent.getContext(), mTerm, mMetrics);
            }
            
            view.pos = position;
            view.row = posToRow(position);
            view.cols = mCols;
    
            return view;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public Object getItem(int position) {
            return null;
        }
        
        @Override
        public int getCount() {
            if (mTerm != null) {
                return mRows + mScrollRows;
            }
            else {
                return 0;
            }
        }
    };
    
    public TerminalView(Context context) {
        this(context, null);
    }
    
    public TerminalView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listViewStyle);
    }
    
    public TerminalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        setBackground(null);
        setDivider(null);
        
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        setAdapter(mAdapter);
        OnKeyListener mKeyListener = (v, keyCode, event) -> {
            final boolean res = mTermKeys.onKey(v, keyCode, event);
            if (res && SCROLL_ON_INPUT) {
                scrollToBottom(true);
            }
            return res;
        };
        setOnKeyListener(mKeyListener);
        
        OnItemClickListener mClickListener = (parent, v, pos, id) -> {
            if (parent.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) parent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(parent, InputMethodManager.SHOW_IMPLICIT);
            }
        };
        setOnItemClickListener(mClickListener);
    }
    
    private int rowToPos(int row) {
        return row + mScrollRows;
    }
    
    private int posToRow(int pos) {
        return pos - mScrollRows;
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        mScrolled = true;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mScrolled) {
            scrollToBottom(false);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        
        final int rows = h / mMetrics.charHeight;
        final int cols = w / mMetrics.charWidth;
        final int scrollRows = mScrollRows;
        
        final boolean sizeChanged = rows != mRows || cols != mCols;
        if (mTerm != null && sizeChanged) {
            mTerm.resize(rows, cols, scrollRows);
            
            mRows = rows;
            mCols = cols;
            mScrollRows = scrollRows;
            
            mAdapter.notifyDataSetChanged();
        }
    }
    
    public void scrollToBottom(boolean animate) {
        final int dur = animate ? 1024 : 0;
        smoothScrollToPositionFromTop(getCount(), 0, dur);
        mScrolled = true;
    }
    
    public Terminal getTerminal() {
        return mTerm;
    }
    
    public void setTerminal(Terminal term) {
        final Terminal orig = mTerm;
        if (orig != null) {
            orig.setClient(null);
        }
        mTerm = term;
        mScrolled = false;
        if (term != null) {
            term.setClient(mClient);
            mTermKeys.setTerminal(term);
            
            mMetrics.cursorPaint.setColor(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent));
            
            // Populate any current settings
            mRows = mTerm.getRows();
            mCols = mTerm.getCols();
            mScrollRows = mTerm.getScrollRows();
            mAdapter.notifyDataSetChanged();
        }
    }
    
    public void setTextSize(float textSize) {
        mMetrics.setTextSize(textSize);
        
        // Layout will kick off terminal resize when needed
        requestLayout();
    }
    
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions |=
                EditorInfo.IME_FLAG_NO_EXTRACT_UI |
                        EditorInfo.IME_FLAG_NO_ENTER_ACTION |
                        EditorInfo.IME_ACTION_NONE;
        outAttrs.inputType = EditorInfo.TYPE_NULL;
        return new BaseInputConnection(this, false) {
            @Override
            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                KeyEvent k;
                if (rightLength == 0 && leftLength == 0) {
                    k = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                    return this.sendKeyEvent(k);
                }
                for (int i = 0; i < leftLength; i++) {
                    k = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                    this.sendKeyEvent(k);
                }
                for (int i = 0; i < rightLength; i++) {
                    k = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL);
                    this.sendKeyEvent(k);
                }
                return true;
            }
        };
    }
    
    /**
     * Metrics shared between all {@link TerminalLineView} children. Locking
     * provided by main thread.
     */
    static class TerminalMetrics {
        private static final int MAX_RUN_LENGTH = 128;
        
        final Paint bgPaint = new Paint();
        final Paint textPaint = new Paint();
        final Paint cursorPaint = new Paint();
        
        /**
         * Run of cells used when drawing
         */
        final CellRun run;
        /**
         * Screen coordinates to draw chars into
         */
        final float[] pos;
        
        int charTop;
        int charWidth;
        int charHeight;
        
        public TerminalMetrics() {
            run = new CellRun();
            run.data = new char[MAX_RUN_LENGTH];
            run.blink = true;
            
            // Positions of each possible cell
            // TODO: make sure this works with surrogate pairs
            pos = new float[MAX_RUN_LENGTH * 2];
            setTextSize(30);
        }
        
        public void setTextSize(float textSize) {
            textPaint.setTypeface(Typeface.MONOSPACE);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(textSize);
            
            // Read metrics to get exact pixel dimensions
            final FontMetrics fm = textPaint.getFontMetrics();
            charTop = (int) Math.ceil(fm.top);
            
            final float[] widths = new float[1];
            textPaint.getTextWidths("X", widths);
            charWidth = (int) Math.ceil(widths[0]);
            charHeight = (int) Math.ceil(fm.descent - fm.top);
            
            // Update drawing positions
            for (int i = 0; i < MAX_RUN_LENGTH; i++) {
                pos[i * 2] = i * charWidth;
                pos[(i * 2) + 1] = -charTop;
            }
        }
        
        public void setTypeFace(Typeface typeFace) {
            textPaint.setTypeface(typeFace);
        }
    }
}
