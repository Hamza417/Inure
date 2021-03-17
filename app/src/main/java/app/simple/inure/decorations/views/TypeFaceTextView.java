package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import app.simple.inure.R;
import app.simple.inure.preferences.MainPreferences;

public class TypeFaceTextView extends AppCompatTextView {
    
    private final TypedArray typedArray;
    
    public TypeFaceTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, 0, 0);
        init();
    }
    
    public TypeFaceTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TypeFaceTextView, defStyleAttr, 0);
        init();
    }
    
    private void init() {
        setTypeFace(MainPreferences.INSTANCE.getAppFont(), typedArray.getInt(R.styleable.TypeFaceTextView_appFontStyle, 0));
        setSelected(true);
    }
    
    private void setTypeFace(String appFont, int string) {
        switch (appFont) {
            case "lato": {
                switch (string) {
                    case 0: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_light));
                        break;
                    }
                    case 1: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_regular));
                        break;
                    }
                    case 2: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_bold));
                        break;
                    }
                }
            }
            case "roboto": {
                /*
                 * no op
                 */
            }
        }
    }
}
