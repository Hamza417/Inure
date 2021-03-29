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
    
    public static final String PLUS_JAKARTA = "plus_jakarta";
    public static final String LATO = "lato";
    public static final String MULISH = "mulish";
    public static final String JOST = "jost";
    
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
    
    private void setTypeFace(String appFont, int style) {
        switch (appFont) {
            case "lato": {
                switch (style) {
                    case 0: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_light));
                        break;
                    }
                    case 1: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_regular));
                        break;
                    }
                    case 2: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_medium));
                        break;
                    }
                    case 3: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.lato_bold));
                        break;
                    }
                }
            }
            case "plus_jakarta": {
                switch (style) {
                    case 0: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_light));
                        break;
                    }
                    case 1: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_regular));
                        break;
                    }
                    case 2: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_medium));
                        break;
                    }
                    case 3: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.plus_jakarta_bold));
                        break;
                    }
                }
            }
            case "mulish": {
                switch (style) {
                    case 0: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.mulish_light));
                        break;
                    }
                    case 1: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.mulish_regular));
                        break;
                    }
                    case 2: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.mulish_medium));
                        break;
                    }
                    case 3: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.mulish_bold));
                        break;
                    }
                }
            }
            case "jost": {
                switch (style) {
                    case 0: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.jost_light));
                        break;
                    }
                    case 1: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.jost_regular));
                        break;
                    }
                    case 2: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.jost_medium));
                        break;
                    }
                    case 3: {
                        setTypeface(ResourcesCompat.getFont(getContext(), R.font.jost_bold));
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
