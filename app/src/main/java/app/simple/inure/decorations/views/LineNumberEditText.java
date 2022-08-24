package app.simple.inure.decorations.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import app.simple.inure.decorations.typeface.TypeFaceEditText;
import app.simple.inure.themes.manager.ThemeManager;

public class LineNumberEditText extends TypeFaceEditText {
    
    private final String newline = System.getProperty("line.separator");
    private Rect rect;
    private Paint paint;
    
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
        setPadding(getLineNumberPadding() + getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        
        // setHorizontallyScrolling(true);
        // setMovementMethod(new ScrollingMovementMethod());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int baseline = getBaseline();
        int lineNumber = 0;
        for (int i = 0; i < getLineCount(); i++) {
            int start = getLayout().getLineStart(i);
            int end = getLayout().getLineEnd(i);
            
            if (getText().subSequence(start, end).toString().contains(newline)) {
                lineNumber++;
                canvas.drawText(String.format(getFormat() + ":", (lineNumber)).replaceAll("\\G0", " "), linePadding, baseline, paint);
            } else {
                canvas.drawText(" ", rect.left, baseline, paint);
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
}