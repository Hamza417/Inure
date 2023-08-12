package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class LegendRecyclerView extends RecyclerView {
    public LegendRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public LegendRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public LegendRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setNestedScrollingEnabled(false);
        setPadding(getPaddingLeft() + 100, getPaddingTop(), getPaddingRight() + 100, getPaddingBottom());
        
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        setLayoutManager(layoutManager);
    }
}
