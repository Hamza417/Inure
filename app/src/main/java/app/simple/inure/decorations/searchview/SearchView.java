package app.simple.inure.decorations.searchview;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import app.simple.inure.R;
import app.simple.inure.decorations.padding.PaddingAwareLinearLayout;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.preferences.SearchPreferences;
import app.simple.inure.themes.manager.ThemeManager;

public class SearchView extends PaddingAwareLinearLayout {
    
    private EditText editText;
    private TypeFaceTextView number;
    private ImageButton imageButton;
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
    
    @SuppressLint ("ClickableViewAccessibility")
    private void initViews() {
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
    
        editText = view.findViewById(R.id.search_view_text_input_layout);
        number = view.findViewById(R.id.search_number);
        imageButton = view.findViewById(R.id.search_view_menu_button);
        
        editText.setText(SearchPreferences.INSTANCE.getLastSearchKeyword());
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* no-op */
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.isFocused()) {
                    searchViewEventListener.onSearchTextChanged(s.toString().trim(), count);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                /* no-op */
            }
        });
        
        imageButton.setOnClickListener(button -> searchViewEventListener.onSearchMenuPressed(button));
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        editText.clearAnimation();
        imageButton.clearAnimation();
        if (numberAnimator != null) {
            numberAnimator.cancel();
        }
    }
    
    @Override
    public void saveHierarchyState(SparseArray <Parcelable> container) {
        super.saveHierarchyState(container);
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
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
}
