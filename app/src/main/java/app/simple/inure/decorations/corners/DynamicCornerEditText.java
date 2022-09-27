package app.simple.inure.decorations.corners;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.decorations.typeface.TypeFaceEditText;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.ViewUtils;

public class DynamicCornerEditText extends TypeFaceEditText {
    
    public DynamicCornerEditText(@Nullable Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setProps(attrs);
    }
    
    public DynamicCornerEditText(@Nullable Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setProps(attrs);
    }
    
    private void setProps(AttributeSet attrs) {
        if (!isInEditMode()) {
            setFocusableInTouchMode(true);
            setFocusable(true);
            setSaveEnabled(true);
        
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_YES);
            }
        
            LayoutBackground.setBackground(getContext(), this, attrs, 2F);
            setBackground(false, ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getViewerBackground());
            ViewUtils.INSTANCE.addShadow(this);
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        super.onThemeChanged(theme, animate);
        setBackground(animate, theme.getViewGroupTheme().getViewerBackground());
    }
    
    @Override
    protected void onDetachedFromWindow() {
        hideInput();
        super.onDetachedFromWindow();
    }
}
