package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import app.simple.inure.decorations.typeface.TypeFaceEditText;
import app.simple.inure.preferences.FormattingPreferences;
import app.simple.inure.themes.manager.ThemeManager;

public class LineNumberEditText extends TypeFaceEditText implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private final String newline = System.getProperty("line.separator");
    private Rect rect;
    private Paint paint;
    
    private boolean showAllNumbers = false;
    private final int linePadding = 5;
    
    LineNumberEditText me;
    
    public LineNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public LineNumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        me = this;
        rect = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getQuaternaryTextColor());
        paint.setTextSize(getTextSize());
        paint.setTypeface(getTypeface());
        setPadding(getLineNumberPadding() + getPaddingLeft(), getPaddingTop(), getPaddingRight() + 20, getPaddingBottom());
        showAllNumbers = FormattingPreferences.INSTANCE.isCountingAllLines();
    
        // setHorizontallyScrolling(true);
        // setMovementMethod(new ScrollingMovementMethod());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int baseline = getBaseline();
        int lineNumber = 0;
    
        for (int i = 0; i < getLineCount(); i++) {
            if (showAllNumbers) {
                lineNumber++;
                canvas.drawText(String.format(getFormat() + ":", lineNumber).replaceAll("\\G0", " "), linePadding, baseline, paint);
            } else {
                try {
                    int start = getLayout().getLineStart(i - 1);
                    int end = getLayout().getLineEnd(i - 1);
    
                    if (getText().subSequence(start, end).toString().endsWith(newline)) {
                        lineNumber++;
                        canvas.drawText(String.format(getFormat() + ":", lineNumber).replaceAll("\\G0", " "), linePadding, baseline, paint);
                    } else {
                        canvas.drawText(" ", rect.left, baseline, paint);
                    }
                } catch (IndexOutOfBoundsException ignore) {
                    lineNumber++;
                    canvas.drawText(String.format(getFormat() + ":", lineNumber).replaceAll("\\G0", " "), linePadding, baseline, paint);
                }
            }
            
            baseline += getLineHeight();
        }
    }
    
    private String getFormat() {
        String format = "%d";
        
        if (getLineCount() < 1000) {
            format = "%03d";
        } else if (getLineCount() < 10000) {
            format = "%04d";
        } else if (getLineCount() < 100000) {
            format = "%05d";
        } else if (getLineCount() < 1000000) {
            format = "%06d";
        }
        
        return format;
    }
    
    private int getLineNumberPadding() {
        int padding = 60;
        
        if (getLineCount() < 10) {
            padding = linePadding + padding;
        } else if (getLineCount() < 100) {
            padding = linePadding + padding * 2;
        } else if (getLineCount() < 1000) {
            padding = linePadding + padding * 3;
        } else if (getLineCount() < 10000) {
            padding = linePadding + padding * 4;
        } else if (getLineCount() < 100000) {
            padding = linePadding + padding * 5;
        } else if (getLineCount() < 1000000) {
            padding = linePadding + padding * 6;
        }
    
        return padding;
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(FormattingPreferences.countAllLines)) {
            showAllNumbers = FormattingPreferences.INSTANCE.isCountingAllLines();
            invalidate();
        }
    }
}