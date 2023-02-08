package app.simple.inure.decorations.pathmotions;

import android.graphics.Path;
import android.graphics.PointF;

import com.google.android.material.transition.MaterialContainerTransform;

import androidx.annotation.NonNull;
import androidx.transition.PathMotion;

/**
 * A Material {@link PathMotion} that results in a more dramatic curve than {@link
 * androidx.transition.ArcMotion}.
 *
 * <p>Use InureArcMotion in conjunction with {@link MaterialContainerTransform} via {@link
 * MaterialContainerTransform#setPathMotion(PathMotion)} to have the container move along a curved
 * path from its start position to its end position.
 */
public final class InureArcMotion extends PathMotion {
    
    private static PointF getControlPoint(float startX, float startY, float endX, float endY) {
        if (startY > endY) {
            return new PointF(endX, startY);
        } else {
            return new PointF(startX, endY);
        }
    }
    
    @NonNull
    @Override
    public Path getPath(float startX, float startY, float endX, float endY) {
        Path path = new Path();
        path.moveTo(startX, startY);
        
        PointF controlPoint = getControlPoint(startX, startY, endX, endY);
        path.quadTo(controlPoint.x, controlPoint.y, endX, endY);
        path.offset(0, 0);
        
        // Create a more dramatic curve
        PointF controlPoint2 = getControlPoint(startX, startY, endX, endY);
        path.quadTo(controlPoint2.x, controlPoint2.y, endX, endY);
        path.offset(0, 0);
        
        return path;
    }
}
