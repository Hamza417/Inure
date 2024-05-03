package app.simple.inure.decorations.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import app.simple.inure.decorations.corners.DynamicCornerEditText;

public class HintsEditText extends DynamicCornerEditText {
    
    private Paint matchedPaint;
    private Paint hintPaint;
    
    private GestureDetector gestureDetectorCompat;
    
    private String matchedHint = "";
    private String remainingHint = "";
    private String finalVerificationHint = "";
    
    public HintsEditText(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public HintsEditText(@Nullable Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        matchedPaint = new Paint();
        matchedPaint.setColor(Color.TRANSPARENT);
        matchedPaint.setTextSize(getTextSize());
        matchedPaint.setAntiAlias(true);
        matchedPaint.setTypeface(getTypeface());
        
        hintPaint = new Paint();
        hintPaint.setColor(getCurrentHintTextColor());
        hintPaint.setTextSize(getTextSize());
        hintPaint.setAntiAlias(true);
        hintPaint.setTypeface(getTypeface());
        
        gestureDetectorCompat = new GestureDetector(getContext(), new HintGestureListener());
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        
        canvas.drawText(matchedHint, getPaddingLeft(), getBaseline(), matchedPaint);
        canvas.drawText(remainingHint, getPaddingLeft() + hintPaint.measureText(matchedHint), getBaseline(), hintPaint);
        
        super.onDraw(canvas);
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    public void drawHint(String matchedHint, String remainingHint) {
        setMatchedHint(matchedHint);
        setRemainingHint(remainingHint);
        invalidate();
    }
    
    public void setFinalVerificationHint(String finalVerificationHint) {
        this.finalVerificationHint = finalVerificationHint;
    }
    
    public String getFinalVerificationHint() {
        return finalVerificationHint;
    }
    
    public String getMatchedHint() {
        return matchedHint;
    }
    
    /**
     * @noinspection unused
     */
    public void setMatchedHint(String matchedHint) {
        this.matchedHint = matchedHint;
    }
    
    public String getRemainingHint() {
        return remainingHint;
    }
    
    /**
     * @noinspection unused
     */
    public void setRemainingHint(String remainingHint) {
        this.remainingHint = remainingHint;
    }
    
    public void clearHint() {
        this.matchedHint = "";
        this.remainingHint = "";
        invalidate();
    }
    
    @SuppressLint ("SetTextI18n")
    public void appendHintToText() {
        if (!getRemainingHint().isEmpty()) {
            if (getFinalVerificationHint().toLowerCase().equals(getMatchedHint().toLowerCase() + getRemainingHint().toLowerCase())) {
                setText(getFinalVerificationHint());
            } else {
                setText(getMatchedHint() + getRemainingHint());
            }
            
            setSelection(getText().length());
            clearHint();
        }
    }
    
    private class HintGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            
            if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                
                appendHintToText();
                
                return true;
            }
            
            return false;
        }
    }
}
