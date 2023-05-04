package app.simple.inure.decorations.views;

import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class TopLeftGravityDrawable extends BitmapDrawable {
    private final Drawable mDrawable;
    
    public TopLeftGravityDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }
    
    @Override
    public void draw(Canvas canvas) {
        int halfCanvas = getBounds().height() / 2;
        int halfDrawable = mDrawable.getIntrinsicHeight() / 2;
        
        // align to top
        canvas.save();
        canvas.translate(0, -halfCanvas + halfDrawable);
        mDrawable.draw(canvas);
        canvas.restore();
    }
}