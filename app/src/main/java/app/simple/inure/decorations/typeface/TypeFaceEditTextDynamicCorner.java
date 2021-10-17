package app.simple.inure.decorations.typeface;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.util.ViewUtils;

public class TypeFaceEditTextDynamicCorner extends TypeFaceEditText {
    public TypeFaceEditTextDynamicCorner(@Nullable Context context) {
        super(context);
        setProps(null);
    }
    
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
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.viewerBackground)));
        LayoutBackground.setBackground(getContext(), this, attrs, 2F);
        ViewUtils.INSTANCE.addShadow(this);
    }
    
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            // animateElevation(25F);
        }
        else {
            // animateElevation(0F);
        }
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
                .hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    
    private void animateElevation(float elevation) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(this.getElevation(), elevation);
        valueAnimator.setInterpolator(new DecelerateInterpolator(1.5F));
        valueAnimator.setDuration(getResources().getInteger(R.integer.animation_duration));
        valueAnimator.addUpdateListener(animation -> {
            TypeFaceEditTextDynamicCorner.this.setElevation((float) animation.getAnimatedValue());
        });
        valueAnimator.start();
    }
}
