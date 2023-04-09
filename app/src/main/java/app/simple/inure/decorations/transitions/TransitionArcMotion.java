package app.simple.inure.decorations.transitions;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.transition.PathMotion;
import app.simple.inure.R;

public class TransitionArcMotion extends PathMotion {
    private static final int DEFAULT_RADIUS = 500;
    private float curveRadius;
    
    public TransitionArcMotion() {
    
    }
    
    public TransitionArcMotion(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TransitionArcMotion);
        curveRadius = a.getInteger(R.styleable.TransitionArcMotion_arcRadius, DEFAULT_RADIUS);
        a.recycle();
    }
    
    @Override
    public Path getPath(float startX, float startY, float endX, float endY) {
        Path arcPath = new Path();
        
        float midX = startX + ((endX - startX) / 2);
        float midY = startY + ((endY - startY) / 2);
        float xDiff = midX - startX;
        float yDiff = midY - startY;
        
        double angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        
        float pointX = (float) (midX + curveRadius * Math.cos(angleRadians));
        float pointY = (float) (midY + curveRadius * Math.sin(angleRadians));
        
        arcPath.moveTo(startX, startY);
        arcPath.cubicTo(startX, startY, pointX, pointY, endX, endY);
        return arcPath;
    }
}