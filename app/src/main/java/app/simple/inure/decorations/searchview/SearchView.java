package app.simple.inure.decorations.searchview;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import app.simple.inure.R;
import app.simple.inure.decorations.typeface.TypeFaceEditText;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.decorations.views.CustomProgressBar;
import app.simple.inure.preferences.SearchPreferences;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TextViewUtils;
import kotlin.Unit;

public class SearchView extends LinearLayout implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private TypeFaceEditText editText;
    private TypeFaceTextView number;
    private ImageButton imageButton;
    private CustomProgressBar loader;
    private SearchViewEventListener searchViewEventListener;
    
    private ValueAnimator numberAnimator;
    private final DecimalFormat format = new DecimalFormat();
    
    private int oldNumber = 0;
    
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
        setProperties();
    }
    
    private void setProperties() {
        setElevation(5);
        setBackgroundColor(ThemeManager.INSTANCE.getTheme().getViewGroupTheme().getBackground());
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutTransition(new LayoutTransition());
    }
    
    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
    
        editText = view.findViewById(R.id.search_view_text_input_layout);
        number = view.findViewById(R.id.search_number);
        imageButton = view.findViewById(R.id.search_view_menu_button);
        loader = view.findViewById(R.id.loader);
    
        editText.setText(SearchPreferences.INSTANCE.getLastSearchKeyword());
        updateDeepSearchData();
        editText.setSaveEnabled(false); // ViewModel and SharedPreferences will handle the saved states
    
        TextViewUtils.INSTANCE.doOnTextChanged(editText, (s, start, before, count) -> {
            if (editText.isFocused()) {
                if (!s.toString().trim().equals(SearchPreferences.INSTANCE.getLastSearchKeyword())) {
                    loader.setVisibility(View.VISIBLE);
                    searchViewEventListener.onSearchTextChanged(s.toString().trim(), count);
                }
            }
            return Unit.INSTANCE;
        });
    
        imageButton.setOnClickListener(button -> searchViewEventListener.onSearchMenuPressed(button));
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.registerListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        editText.clearAnimation();
        imageButton.clearAnimation();
        if (numberAnimator != null) {
            numberAnimator.cancel();
        }
        app.simple.inure.preferences.SharedPreferences.INSTANCE.unregisterListener(this);
    }
    
    public void hideLoader() {
        loader.setVisibility(View.GONE);
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
    
    private void updateDeepSearchData() {
        if (SearchPreferences.INSTANCE.isDeepSearchEnabled()) {
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
            TextViewUtils.INSTANCE.setDrawableTint(editText, Color.parseColor("#d35400"));
            editText.setHint(R.string.deep_search);
        } else {
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
            TextViewUtils.INSTANCE.setDrawableTint(editText, ThemeManager.INSTANCE.getTheme().getIconTheme().getSecondaryIconColor());
            editText.setHint(R.string.search);
        }
    }
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SearchPreferences.deepSearch)) {
            loader.setVisibility(View.VISIBLE);
            updateDeepSearchData();
        }
    }
    
    public TypeFaceEditText getEditText() {
        return editText;
    }
}
