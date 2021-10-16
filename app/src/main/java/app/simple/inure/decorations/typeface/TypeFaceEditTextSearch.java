package app.simple.inure.decorations.typeface;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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
        setFocusableInTouchMode(true);
        setFocusable(true);
        setSaveEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
        }
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.viewerBackground)));
        LayoutBackground.setBackground(getContext(), this, attrs, 2F);
    }
    
    public void toggleInput() {
        switch (getVisibility()) {
            case View.VISIBLE: {
                requestFocus();
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
                break;
            }
            case View.INVISIBLE:
            case View.GONE: {
                clearFocus();
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                break;
            }
        }
    }
}
