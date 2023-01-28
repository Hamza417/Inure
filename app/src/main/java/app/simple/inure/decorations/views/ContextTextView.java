package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.preferences.AccessibilityPreferences;

public class ContextTextView extends TypeFaceTextView {
    
    public ContextTextView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public ContextTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ContextTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        if (AccessibilityPreferences.INSTANCE.isAppElementsContext()) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }
}
