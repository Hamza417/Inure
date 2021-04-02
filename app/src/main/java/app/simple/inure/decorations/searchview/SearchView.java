package app.simple.inure.decorations.searchview;

import android.animation.LayoutTransition;
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

import androidx.annotation.Nullable;
import app.simple.inure.R;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.preferences.SearchPreferences;
import app.simple.inure.util.ViewUtils;

public class SearchView extends LinearLayout {
    
    private EditText editText;
    private ImageButton imageButton;
    private SearchViewEventListener searchViewEventListener;
    
    public SearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
        setProperties(attrs);
    }
    
    private void setProperties(AttributeSet attrs) {
        setElevation(getResources().getDimensionPixelSize(R.dimen.app_views_elevation));
        ViewUtils.INSTANCE.addShadow(this);
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutBackground.setBackground(getContext(), this, attrs);
        setLayoutTransition(new LayoutTransition());
    }
    
    @SuppressLint ("ClickableViewAccessibility")
    private void initViews() {
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.search_view, this, true);
        
        editText = view.findViewById(R.id.search_view_text_input_layout);
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
                    searchViewEventListener.onSearchTextChanged(s.toString(), count);
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
    }
    
    @Override
    public void saveHierarchyState(SparseArray <Parcelable> container) {
        super.saveHierarchyState(container);
    }
    
    public void setSearchViewEventListener(SearchViewEventListener searchViewEventListener) {
        this.searchViewEventListener = searchViewEventListener;
    }
}
