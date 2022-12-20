package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import app.simple.inure.R;
import app.simple.inure.adapters.menus.AdapterBottomMenu;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView;
import app.simple.inure.interfaces.menus.BottomMenuCallbacks;
import app.simple.inure.util.ViewUtils;
import kotlin.ranges.RangesKt;

public class BottomMenuRecyclerView extends CustomHorizontalRecyclerView {
    
    private int containerHeight;
    private int displayWidth;
    private boolean isScrollListenerAdded = false;
    
    public BottomMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    
    private void init(AttributeSet attributeSet) {
        if (isInEditMode()) {
            return;
        }
        displayWidth = new DisplayMetrics().widthPixels;
        int padding = getResources().getDimensionPixelOffset(R.dimen.popup_padding);
        setPadding(padding, padding, padding, padding);
        setElevation(getResources().getDimensionPixelOffset(R.dimen.app_views_elevation));
        LayoutBackground.setBackground(getContext(), this, attributeSet);
        ViewUtils.INSTANCE.addShadow(this);
        setClipToPadding(false);
        setClipChildren(true);
    }
    
    public void initBottomMenu(ArrayList <Integer> bottomMenuItems, BottomMenuCallbacks bottomMenuCallbacks) {
        AdapterBottomMenu adapterBottomMenu = new AdapterBottomMenu(bottomMenuItems);
        adapterBottomMenu.setBottomMenuCallbacks(bottomMenuCallbacks);
    
        if (getAdapter() == null) {
            setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.list_animation_controller));
        } else {
            setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getContext(), R.anim.grid_list_animation_controller));
        }
        setAdapter(adapterBottomMenu);
    
        post(() -> {
            ViewGroup.MarginLayoutParams layoutParams = (MarginLayoutParams) getLayoutParams();
        
            layoutParams.topMargin = getResources().getDimensionPixelOffset(R.dimen.bottom_menu_margin);
            layoutParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.bottom_menu_margin);
            layoutParams.leftMargin = getResources().getDimensionPixelOffset(R.dimen.bottom_menu_margin);
            layoutParams.rightMargin = getResources().getDimensionPixelOffset(R.dimen.bottom_menu_margin);
        
            containerHeight = getHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
        });
    }
    
    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        scheduleLayoutAnimation();
    }
    
    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = Math.min(widthSpec, displayWidth);
        super.onMeasure(width, heightSpec);
    }
    
    public AdapterBottomMenu getMenuAdapter() {
        return (AdapterBottomMenu) super.getAdapter();
    }
    
    public void initBottomMenuWithRecyclerView(ArrayList <Integer> bottomMenuItems, RecyclerView recyclerView, BottomMenuCallbacks bottomMenuCallbacks) {
        initBottomMenu(bottomMenuItems, bottomMenuCallbacks);
    
        /*
         * Rather than clearing all scroll listeners at once, which will break other
         * features of the app such as Fast Scroller, we will use a boolean to check
         * if the scroll listener has been added or not and then add it. This should
         * be valid till the lifecycle of the BottomMenuRecyclerView.
         */
        // recyclerView.clearOnScrollListeners();
    
        if (recyclerView != null) {
            if (recyclerView.getAdapter() != null) {
                if (recyclerView.getAdapter().getItemCount() > 10) {
                    if (!isScrollListenerAdded) {
                        recyclerView.addOnScrollListener(new OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                setTranslationY(dy);
                            }
                        });
                    
                        isScrollListenerAdded = true;
                    }
                }
            }
        }
    }
    
    public void updateBottomMenu(ArrayList <Integer> bottomMenuItems) {
        getMenuAdapter().updateMenu(bottomMenuItems);
        requestLayout();
    }
    
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            scheduleLayoutAnimation();
        }
    }
    
    public void setTranslationY(int dy) {
        if (dy > 0) {
            if (getTranslationY() < containerHeight) {
                setTranslationY(getTranslationY() + dy);
                setTranslationY(RangesKt.coerceAtMost(getTranslationY(), containerHeight));
            }
        } else {
            if (getTranslationY() > 0) {
                setTranslationY(getTranslationY() + dy);
                setTranslationY(RangesKt.coerceAtLeast(getTranslationY(), 0));
            }
        }
    }
}
