package app.simple.inure.decorations.searchview;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import app.simple.inure.R;
import app.simple.inure.decorations.ripple.DynamicRippleImageButton;
import app.simple.inure.decorations.theme.ThemeIcon;
import app.simple.inure.decorations.typeface.TypeFaceEditText;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.decorations.views.CustomProgressBar;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.preferences.SearchPreferences;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TextViewUtils;
import app.simple.inure.util.ViewUtils;
import kotlin.Unit;

public class SearchView extends LinearLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private ThemeIcon icon;
    private TypeFaceEditText editText;
    private TypeFaceTextView number;
    private DynamicRippleImageButton settings;
    private DynamicRippleImageButton clear;
    private DynamicRippleImageButton refresh;
    private DynamicRippleImageButton filter;
    private DynamicRippleImageButton more;
    private CustomProgressBar loader;
    private SearchViewEventListener searchViewEventListener;
    
    private ValueAnimator numberAnimator;
    private ValueAnimator iconAnimator;
    private final DecimalFormat format = new DecimalFormat();
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    private int oldNumber = 0;
    
    /**
     * @noinspection FieldCanBeLocal
     */
    private final int MORE_BUTTON_DELAY = 3000;
    
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
        setProperties();
    }
    
    private void setProperties() {
        // setElevation(5);
        setBackgroundColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground());
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutTransition(new LayoutTransition());
    }
    
    @SuppressLint ("SetTextI18n")
    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        
        icon = view.findViewById(R.id.icon);
        editText = view.findViewById(R.id.edit_text);
        number = view.findViewById(R.id.search_number);
        settings = view.findViewById(R.id.settings_button);
        clear = view.findViewById(R.id.clear_button);
        refresh = view.findViewById(R.id.refresh_button);
        filter = view.findViewById(R.id.filter_button);
        more = view.findViewById(R.id.more_button);
        loader = view.findViewById(R.id.loader);
        
        if (!isInEditMode()) {
            if (!SearchPreferences.INSTANCE.getLastSearchKeyword().isEmpty()) {
                ViewUtils.INSTANCE.visible(clear, false);
                editText.setText(SearchPreferences.INSTANCE.getLastSearchKeyword());
                
                if (SearchPreferences.INSTANCE.getLastSearchKeyword().startsWith("#")) {
                    editText.getText().setSpan(
                            new ForegroundColorSpan(AppearancePreferences.INSTANCE.getAccentColor()), 0, 1, 0);
                }
            } else {
                ViewUtils.INSTANCE.gone(clear, true);
            }
        }
        
        updateDeepSearchData();
        editText.setSaveEnabled(false); // ViewModel and SharedPreferences will handle the saved states
        
        TextViewUtils.INSTANCE.doOnTextChanged(editText, (s, start, before, count) -> {
            boolean isValidCount = !s.toString().trim().replace("#", "").isEmpty();
            
            if (editText.isFocused()) {
                if (!s.toString().trim().equals(SearchPreferences.INSTANCE.getLastSearchKeyword())) {
                    if (isValidCount) {
                        loader.setVisibility(View.VISIBLE);
                    } else {
                        loader.setVisibility(View.GONE);
                    }
                    
                    searchViewEventListener.onSearchTextChanged(s.toString().trim(), count);
                }
            }
            
            SearchPreferences.INSTANCE.setLastSearchKeyword(s.toString().trim());
            
            if (isValidCount) {
                ViewUtils.INSTANCE.visible(clear, true);
            } else {
                ViewUtils.INSTANCE.gone(clear, true);
            }
            
            updateSpans();
            
            return Unit.INSTANCE;
        });
        
        settings.setOnClickListener(button -> searchViewEventListener.onSearchMenuPressed(button));
        filter.setOnClickListener(button -> searchViewEventListener.onFilterPressed(button));
        more.setOnClickListener(v -> moreButtonState(true));
        
        refresh.setOnClickListener(button -> {
            loader.setVisibility(View.VISIBLE);
            searchViewEventListener.onSearchRefreshPressed(button);
        });
        
        clear.setOnClickListener(button -> {
            String text = editText.getText().toString().trim();
            if (text.startsWith("#")) {
                String[] parts = text.split(" ", 2);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    // Clear the keyword only
                    editText.setText(parts[0] + " ");
                    editText.setSelection(editText.getText().length());
                } else {
                    // Clear the tag
                    editText.getText().clear();
                }
            } else {
                // Clear the entire text
                editText.getText().clear();
            }
            
            setNewNumber(0);
            searchViewEventListener.onClear(button);
            SearchPreferences.INSTANCE.setLastSearchKeyword(editText.getText().toString().trim());
        });
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.registerSharedPreferencesListener(this);
    }
    
    public void hideLoader() {
        loader.setVisibility(View.GONE);
    }
    
    public void showLoader() {
        loader.setVisibility(View.VISIBLE);
    }
    
    @SuppressLint ("SetTextI18n")
    public void setKeyword(String keyword) {
        if (editText.getText().toString().startsWith("#")) {
            if (editText.getText().toString().split(" ").length > 1) {
                String split = editText.getText().toString().split(" ")[0];
                editText.setText(split + " " + keyword);
            } else {
                if (editText.getText().toString().endsWith(" ")) {
                    editText.append(keyword);
                } else {
                    editText.append(" " + keyword);
                }
            }
        } else {
            editText.setText(keyword);
        }
        
        editText.setSelection(editText.getText().length());
        updateSpans();
        showLoader();
        handler.postDelayed(this :: showInput, 500);
    }
    
    private void updateSpans() {
        if (editText.getText().toString().trim().startsWith("#")) {
            editText.getText().setSpan(
                    new ForegroundColorSpan(AppearancePreferences.INSTANCE.getAccentColor()), 0, 1, 0);
        } else {
            if (editText.getText().getSpans(0, 1, ForegroundColorSpan.class).length > 0) {
                // Remove the spans
                for (ForegroundColorSpan span : editText.getText().getSpans(0, 1, ForegroundColorSpan.class)) {
                    editText.getText().removeSpan(span);
                }
                
                // Move the cursor to the end of the text
                editText.setSelection(editText.getText().length());
                // Focus the edit text
                editText.requestFocus();
            }
        }
    }
    
    public String getKeyword() {
        return editText.getText().toString().trim();
    }
    
    public void setNewNumber(int number) {
        String pattern;
        
        if (number < 1000) {
            pattern = "000";
        } else if (number < 10000) {
            pattern = "0000";
        } else {
            this.number.setText("∞");
            return;
        }
        
        format.applyPattern(pattern);
        animateNumber(number);
    }
    
    private void animateNumber(int newNumber) {
        if (numberAnimator != null) {
            numberAnimator.cancel();
        }
        
        numberAnimator = ValueAnimator.ofInt(oldNumber, newNumber);
        numberAnimator.setInterpolator(new FastOutLinearInInterpolator());
        numberAnimator.setDuration(getResources().getInteger(R.integer.animation_duration));
        numberAnimator.addUpdateListener(animation -> {
            number.setText(format.format((int) animation.getAnimatedValue()));
            oldNumber = (int) animation.getAnimatedValue();
        });
        numberAnimator.start();
    }
    
    public void showInput() {
        editText.showInput();
    }
    
    public void hideInput() {
        editText.hideInput();
    }
    
    private void updateDeepSearchData() {
        if (SearchPreferences.INSTANCE.isDeepSearchEnabled()) {
            iconAnimator = ViewUtils.INSTANCE.animateTint(icon, AppearancePreferences.INSTANCE.getAccentColor());
            editText.setHint(R.string.deep_search);
            
            /*
             * Just so you know, this is a very bad idea!!!!
             * It will cause the app to work very slow
             * and will cause the app to crash in some cases.
             * I don't know why, but it does.
             */
            // editText.getText().clearSpans();
            
            /*
             * This is a better way to do it
             */
            for (ForegroundColorSpan span : editText.getText().getSpans(0, 1, ForegroundColorSpan.class)) {
                editText.getText().removeSpan(span);
            }
        } else {
            iconAnimator = ViewUtils.INSTANCE.animateTint(icon, ThemeManager.INSTANCE.getTheme().getIconTheme().getSecondaryIconColor());
            editText.setHint(R.string.search);
            if (editText.getText().toString().trim().startsWith("#")) {
                editText.getText().setSpan(
                        new ForegroundColorSpan(
                                AppearancePreferences.INSTANCE.getAccentColor()), 0, 1, 0);
            }
        }
    }
    
    private final Runnable moreButtonRunnable = () -> {
        moreButtonState(false);
    };
    
    private void moreButtonState(boolean state) {
        handler.removeCallbacks(moreButtonRunnable);
        
        if (state) {
            filter.setScaleX(0);
            filter.setScaleY(0);
            settings.setScaleX(0);
            settings.setScaleY(0);
            refresh.setScaleX(0);
            refresh.setScaleY(0);
            
            ViewUtils.INSTANCE.visible(filter, true);
            ViewUtils.INSTANCE.visible(settings, true);
            ViewUtils.INSTANCE.visible(refresh, true);
            ViewUtils.INSTANCE.gone(more, false);
            handler.postDelayed(moreButtonRunnable, MORE_BUTTON_DELAY);
        } else {
            ViewUtils.INSTANCE.gone(settings, true);
            ViewUtils.INSTANCE.gone(refresh, true);
            ViewUtils.INSTANCE.gone(filter, true);
            ViewUtils.INSTANCE.visible(more, true);
        }
    }
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SearchPreferences.DEEP_SEARCH)) {
            loader.setVisibility(View.VISIBLE);
            updateDeepSearchData();
        }
    }
    
    public TypeFaceEditText getEditText() {
        return editText;
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(moreButtonRunnable);
        handler.removeCallbacksAndMessages(null);
        
        editText.clearAnimation();
        settings.clearAnimation();
        clear.clearAnimation();
        refresh.clearAnimation();
        filter.clearAnimation();
        more.clearAnimation();
        loader.clearAnimation();
        
        if (numberAnimator != null) {
            numberAnimator.cancel();
        }
        
        if (iconAnimator != null) {
            iconAnimator.cancel();
        }
        
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterListener(this);
    }
}
