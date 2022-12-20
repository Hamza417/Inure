package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An extension of RecyclerView, focused more on resembling a GridView.
 * Unlike {@link androidx.recyclerview.widget.RecyclerView}, this view can handle
 * {@code <gridLayoutAnimation>} as long as you provide it a
 * {@link androidx.recyclerview.widget.GridLayoutManager} in
 * {@code setLayoutManager(LayoutManager layout)}.
 * <p>
 * Created by Freddie (Musenkishi) Lust-Hed.
 */
public class GridRecyclerView extends RecyclerView {
    
    public GridRecyclerView(Context context) {
        super(context);
    }
    
    public GridRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public GridRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (layout instanceof GridLayoutManager) {
            super.setLayoutManager(layout);
        } else {
            throw new ClassCastException("You should only use a GridLayoutManager with GridRecyclerView.");
        }
    }
    
    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        
        if (getAdapter() != null && getLayoutManager() instanceof GridLayoutManager) {
            
            GridLayoutAnimationController.AnimationParameters animationParams =
                    (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
            
            if (animationParams == null) {
                animationParams = new GridLayoutAnimationController.AnimationParameters();
                params.layoutAnimationParameters = animationParams;
            }
            
            int columns = ((GridLayoutManager) getLayoutManager()).getSpanCount();
            
            animationParams.count = count;
            animationParams.index = index;
            animationParams.columnsCount = columns;
            animationParams.rowsCount = count / columns;
            
            final int invertedIndex = count - 1 - index;
            animationParams.column = columns - 1 - (invertedIndex % columns);
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;
            
        } else {
            super.attachLayoutAnimationParameters(child, params, index, count);
        }
    }
}