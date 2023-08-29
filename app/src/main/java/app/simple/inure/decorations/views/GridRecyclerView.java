package app.simple.inure.decorations.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.AnimRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An extension of RecyclerView, focused more on resembling a GridView.
 * Unlike {@link androidx.recyclerview.widget.RecyclerView}, this view can handle
 * {@code <gridLayoutAnimation>} as long as you provide it a
 * {@link androidx.recyclerview.widget.GridLayoutManager} in
 * {@code setLayoutManager(LayoutManager layout)}.
 * <p>
 * Created by Freddie (Musenkishi) Lust-Hed, Hamza417.
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
            // throw new IllegalStateException("GridRecyclerView must have a GridLayoutManager");
            Log.e("GridRecyclerView", "attachLayoutAnimationParameters: GridRecyclerView must have a GridLayoutManager");
        }
    }
    
    public void setLayoutControllerAnimation(@AnimRes int animationController) {
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), animationController);
        setLayoutAnimation(controller);
    }
    
    public void setLayoutControllerAnimation(@AnimRes int animationController, float delay) {
        LayoutAnimationController controller = new LayoutAnimationController(AnimationUtils.loadAnimation(getContext(), animationController), delay);
        setLayoutAnimation(controller);
    }
}