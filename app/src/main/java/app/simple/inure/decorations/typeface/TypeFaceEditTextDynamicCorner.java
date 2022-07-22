package app.simple.inure.decorations.typeface;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.util.ViewUtils;

public class TypeFaceEditTextDynamicCorner extends TypeFaceEditText {
    
    private ValueAnimator valueAnimator;
    
    public TypeFaceEditTextDynamicCorner(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setProps(attrs);
    }
    
    public TypeFaceEditTextDynamicCorner(@Nullable Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
    
        LayoutBackground.setBackground(getContext(), this, attrs, 2F);
        setBackground(false);
        ViewUtils.INSTANCE.addShadow(this);
    }
    
    public void toggleInput() {
        switch (getVisibility()) {
            case View.VISIBLE: {
                showInput();
                break;
            }
            case View.INVISIBLE:
            case View.GONE: {
                hideInput();
                break;
            }
        }
    }
    
    public void showInput() {
        requestFocus();
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }
    
    public void hideInput() {
        clearFocus();
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        super.onThemeChanged(theme, animate);
        setBackground(animate);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        hideInput();
        super.onDetachedFromWindow();
    }
}
