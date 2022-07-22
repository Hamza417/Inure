package app.simple.inure.decorations.fastscroll.backgrounds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.preferences.AppearancePreferences;

public class CircularBackground extends Drawable {
    
    @NonNull
    private final Paint paint;
    private final Path path = new Path();
    
    public CircularBackground(@NonNull Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(AppearancePreferences.INSTANCE.getAccentColor());
        paint.setStyle(Paint.Style.FILL);
    }
    
    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        path.addArc(0, 0, bounds.width(), bounds.height(), 0, 360);
        canvas.drawPath(path, paint);
    }
    
    @Override
    public void setAlpha(int alpha) {
    }
    
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }
    
    @Override
    public boolean isAutoMirrored() {
        return true;
    }
    
    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
    
    @Override
    public boolean getPadding(@NonNull Rect padding) {
        return true;
    }
    
    /**
     * For elevation shadows
     */
    @Override
    public void getOutline(@NonNull Outline outline) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outline.setPath(path);
            } else {
                outline.setConvexPath(path);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}