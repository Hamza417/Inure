package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import app.simple.inure.R;
import app.simple.inure.preferences.AccessibilityPreferences;

public class TagsRecyclerView extends RecyclerView {
    public TagsRecyclerView(@NonNull Context context) {
        super(context);
        init();
    }
    
    public TagsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public TagsRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        setNestedScrollingEnabled(false);
        
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        setLayoutManager(layoutManager);
        
        if (!AccessibilityPreferences.INSTANCE.isAnimationReduced()) {
            setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.list_pop_in_animation_controller));
        }
    }
    
    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        if (!AccessibilityPreferences.INSTANCE.isAnimationReduced()) {
            scheduleLayoutAnimation();
        }
    }
}
