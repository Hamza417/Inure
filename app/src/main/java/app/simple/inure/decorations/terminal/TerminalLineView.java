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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.decorations.terminal.TerminalView.TerminalMetrics;
import kotlin.Suppress;

/**
 * Rendered contents of a single line of a {@link Terminal} session.
 */
@SuppressLint ("ViewConstructor")
public class TerminalLineView extends View {
    private static final String TAG = "TAG";
    public int pos;
    public int row;
    public int cols;

    private final Terminal mTerm;
    private final TerminalMetrics mMetrics;

    public TerminalLineView(Context context, Terminal term, TerminalMetrics metrics) {
        super(context);
        mTerm = term;
        mMetrics = metrics;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
                getDefaultSize(mMetrics.charHeight, heightMeasureSpec));
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mTerm == null) {
            Log.w(TAG, "onDraw() without a terminal");
            canvas.drawColor(Color.MAGENTA);
            return;
        }

        final TerminalMetrics m = mMetrics;

        for (int col = 0; col < cols;) {
            mTerm.getCellRun(row, col, m.run);

            m.bgPaint.setColor(Color.TRANSPARENT);
            m.textPaint.setColor(ContextCompat.getColor(getContext(), R.color.textPrimary));

            final int x = col * m.charWidth;
            final int xEnd = x + (m.run.colSize * m.charWidth);

            canvas.save();
            canvas.translate(x, 0);
            canvas.clipRect(0, 0, m.run.colSize * m.charWidth, m.charHeight);

            canvas.drawPaint(m.bgPaint);
            
            canvas.drawPosText(m.run.data, 0, m.run.dataSize, m.pos, m.textPaint);

            canvas.restore();

            col += m.run.colSize;
        }

        if (mTerm.getCursorVisible() && mTerm.getCursorRow() == row) {
            canvas.save();
            canvas.translate(mTerm.getCursorCol() * m.charWidth, 0);
            canvas.drawRect(0, 0, m.charWidth, m.charHeight, m.cursorPaint);
            canvas.restore();
        }

    }
}
