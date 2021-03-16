package app.simple.inure.decorations.searchview;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import app.simple.inure.R;

public class SearchView extends LinearLayout {
    
    private EditText editText;
    private ImageButton imageButton;
    private SearchViewEventListener searchViewEventListener;
    
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
        setProperties();
    }
    
    private void setProperties() {
        setElevation(getResources().getDimensionPixelSize(R.dimen.app_views_elevation));
        setOrientation(LinearLayout.HORIZONTAL);
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_popup));
        setLayoutTransition(new LayoutTransition());
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    private void initViews() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        
        editText = view.findViewById(R.id.search_view_text_input_layout);
        imageButton = view.findViewById(R.id.search_view_menu_button);
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* no-op */
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO - find a method to fix the text watcher getting called every time view's instance is restored
                //searchViewEventListener.onSearchTextChanged(s.toString(), count);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                /* no-op */
            }
        });
        
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                imageButton.animate().scaleX(0F).scaleY(0F).setInterpolator(new DecelerateInterpolator()).start();
                imageButton.setVisibility(View.GONE);
            }
            else {
                imageButton.animate().scaleX(1F).scaleY(1F).setInterpolator(new DecelerateInterpolator()).start();
                imageButton.setVisibility(View.VISIBLE);
            }
        });
        
        imageButton.setOnClickListener(button -> searchViewEventListener.onSearchMenuPressed(button));
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        editText.clearAnimation();
        imageButton.clearAnimation();
    }
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
}
