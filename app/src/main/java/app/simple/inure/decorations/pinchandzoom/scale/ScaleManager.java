package app.simple.inure.decorations.pinchandzoom.scale;

/**
 * Created by nemi on 30/05/16.
 */

public class ScaleManager {
    
    private final OnScaleChangedListener listener;
    private Scale scale;
    
    public interface OnScaleChangedListener {
        void onScaleChanged(Scale scale);
    }
    
    public ScaleManager(OnScaleChangedListener listener) {
        this.listener = listener;
    }
    
    public void reset() {
        scale = null;
    }
    
    public void onScale(float incrementalScale) {
        if (isScaling()) {
            scale.updateScale(incrementalScale);
            listener.onScaleChanged(scale);
        } else {
            this.scale = getScale(incrementalScale);
        }
    }
    
    private Scale getScale(float scale) {
        if (scale > 1.0f) {
            return new ScaleUp();
        } else if (scale < 1.0f) {
            return new ScaleDown();
        }
        
        return this.scale;
    }
    
    private boolean isScaling() {
        return scale != null;
    }
}
