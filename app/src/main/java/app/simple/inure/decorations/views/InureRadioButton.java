package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Accent;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TypeFace;

public class InureRadioButton extends AppCompatRadioButton implements ThemeChangedListener {
    
    public InureRadioButton(Context context) {
        super(context);
        init();
    }
    
    public InureRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public InureRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getPrimaryTextColor());
        if (!isInEditMode()) {
            setTypeface(TypeFace.INSTANCE.getBoldTypeFace(getContext()));
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.INSTANCE.removeListener(this);
    }
    
    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (checked) {
            setButtonTintList(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
        } else {
            setButtonTintList(ColorStateList.valueOf(ThemeManager.INSTANCE.getTheme().getSwitchViewTheme().getSwitchOffColor()));
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        setChecked(isChecked());
        init();
    }
    
    @Override
    public void onAccentChanged(@NonNull Accent accent) {
        setChecked(isChecked());
        init();
    }
}
