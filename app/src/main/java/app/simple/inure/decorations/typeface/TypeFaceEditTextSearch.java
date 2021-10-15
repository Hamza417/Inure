package app.simple.inure.decorations.typeface;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.LayoutBackground;

public class TypeFaceEditTextSearch extends TypeFaceEditText {
    public TypeFaceEditTextSearch(@Nullable Context context) {
        super(context);
        setProps(null);
    }
    
    public TypeFaceEditTextSearch(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setProps(attrs);
    }
    
    public TypeFaceEditTextSearch(@Nullable Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setProps(attrs);
    }
    
    private void setProps(AttributeSet attrs) {
        setSaveEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.viewerBackground)));
        LayoutBackground.setBackground(getContext(), this, attrs, 2F);
    }
}
