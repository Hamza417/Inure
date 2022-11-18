package app.simple.inure.decorations.pinchandzoom.scale;

import androidx.annotation.NonNull;

/**
 * Created by nemi on 30/05/16.
 */

public class ScaleDown implements Scale {
    private float scale = 1.0f;
    
    @Override
    public void updateScale(float incrementalScale) {
        scale *= incrementalScale;
        
        if (scale > 1.0f) {
            scale = 1.0f;
        }
    }
    
    @Override
    public float getScale() {
        return 1.0f - scale;
    }
    
    @Override
    public int getType() {
        return TYPE_SCALE_DOWN;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "{ scale: down, scale: " + scale + " }";
    }
}
