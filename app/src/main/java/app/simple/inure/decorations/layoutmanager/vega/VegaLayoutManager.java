package app.simple.inure.decorations.layoutmanager.vega;

import android.graphics.Rect;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import app.simple.inure.decorations.overscroll.VerticalListViewHolder;

public class VegaLayoutManager extends RecyclerView.LayoutManager {
    
    private final SparseArray <Rect> locationRects = new SparseArray <>();
    private final SparseBooleanArray attachedItems = new SparseBooleanArray();
    private final ArrayMap <Integer, Integer> viewTypeHeightMap = new ArrayMap <>();
    private int scroll = 0;
    private boolean needSnap = false;
    private int lastDy = 0;
    private int maxScroll = -1;
    private RecyclerView.Adapter <VerticalListViewHolder> adapter;
    private RecyclerView.Recycler recycler;
    
    @SuppressWarnings ("deprecation")
    public VegaLayoutManager() {
        setAutoMeasureEnabled(true);
    }
    
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    
    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        //noinspection unchecked
        this.adapter = newAdapter;
    }
    
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.recycler = recycler; // Apart from anything else, save the recycler first
        if (state.isPreLayout()) {
            return;
        }
        
        buildLocationRects();
        
        // Recycle and put it in the cache first, and then unify the layout again later
        detachAndScrapAttachedViews(recycler);
        layoutItemsOnCreate(recycler);
    }
    
    private void buildLocationRects() {
        locationRects.clear();
        attachedItems.clear();
        
        int tempPosition = getPaddingTop();
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            // 1. First calculate itemWidth and itemHeight
            int viewType = adapter.getItemViewType(i);
            int itemHeight;
            if (viewTypeHeightMap.containsKey(viewType)) {
                //noinspection ConstantConditions
                itemHeight = viewTypeHeightMap.get(viewType);
            }
            else {
                View itemView = recycler.getViewForPosition(i);
                addView(itemView);
                measureChildWithMargins(itemView, View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                itemHeight = getDecoratedMeasuredHeight(itemView);
                viewTypeHeightMap.put(viewType, itemHeight);
            }
            
            // 2. Assemble Rect and save
            Rect rect = new Rect();
            rect.left = getPaddingLeft();
            rect.top = tempPosition;
            rect.right = getWidth() - getPaddingRight();
            rect.bottom = rect.top + itemHeight;
            locationRects.put(i, rect);
            attachedItems.put(i, false);
            tempPosition = tempPosition + itemHeight;
        }
        
        if (itemCount == 0) {
            maxScroll = 0;
        }
        else {
            computeMaxScroll();
        }
    }
    
    /**
     * Provide an external interface, find the index of the first visual view
     */
    public int findFirstVisibleItemPosition() {
        int count = locationRects.size();
        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        for (int i = 0; i < count; i++) {
            if (Rect.intersects(displayRect, locationRects.get(i)) &&
                    attachedItems.get(i)) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Calculate the maximum sliding value
     */
    private void computeMaxScroll() {
        maxScroll = locationRects.get(locationRects.size() - 1).bottom - getHeight();
        if (maxScroll < 0) {
            maxScroll = 0;
            return;
        }
        
        int itemCount = getItemCount();
        int screenFilledHeight = 0;
        for (int i = itemCount - 1; i >= 0; i--) {
            Rect rect = locationRects.get(i);
            screenFilledHeight = screenFilledHeight + (rect.bottom - rect.top);
            if (screenFilledHeight > getHeight()) {
                int extraSnapHeight = getHeight() - (screenFilledHeight - (rect.bottom - rect.top));
                maxScroll = maxScroll + extraSnapHeight;
                break;
            }
        }
    }
    
    /**
     * When initializing, the layout child View
     */
    private void layoutItemsOnCreate(RecyclerView.Recycler recycler) {
        int itemCount = getItemCount();
        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        for (int i = 0; i < itemCount; i++) {
            Rect thisRect = locationRects.get(i);
            if (Rect.intersects(displayRect, thisRect)) {
                View childView = recycler.getViewForPosition(i);
                addView(childView);
                measureChildWithMargins(childView, View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                layoutItem(childView, locationRects.get(i));
                attachedItems.put(i, true);
                childView.setPivotY(0);
                childView.setPivotX(childView.getMeasuredWidth() / 2F);
                if (thisRect.top - scroll > getHeight()) {
                    break;
                }
            }
        }
    }
    
    
    /**
     * When initializing, the layout child View
     */
    private void layoutItemsOnScroll() {
        int childCount = getChildCount();
        // 1. Child already displayed on the screen
        int itemCount = getItemCount();
        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        int firstVisiblePosition = -1;
        int lastVisiblePosition = -1;
        for (int i = childCount - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            int position = getPosition(child);
            if (!Rect.intersects(displayRect, locationRects.get(position))) {
                // Recycle the View that slides off the screen
                removeAndRecycleView(child, recycler);
                attachedItems.put(position, false);
            }
            else {
                // Item is still in the display area, update the position of the item after sliding
                if (lastVisiblePosition < 0) {
                    lastVisiblePosition = position;
                }
                
                if (firstVisiblePosition < 0) {
                    firstVisiblePosition = position;
                }
                else {
                    firstVisiblePosition = Math.min(firstVisiblePosition, position);
                }
                
                layoutItem(child, locationRects.get(position)); //更新Item位置
            }
        }
        
        // 2. Reuse View processing
        if (firstVisiblePosition > 0) {
            // Search forward and reuse
            for (int i = firstVisiblePosition - 1; i >= 0; i--) {
                if (Rect.intersects(displayRect, locationRects.get(i)) &&
                        !attachedItems.get(i)) {
                    reuseItemOnScroll(i, true);
                }
                else {
                    break;
                }
            }
        }
        // Search and reuse in the future
        for (int i = lastVisiblePosition + 1; i < itemCount; i++) {
            if (Rect.intersects(displayRect, locationRects.get(i)) &&
                    !attachedItems.get(i)) {
                reuseItemOnScroll(i, false);
            }
            else {
                break;
            }
        }
    }
    
    /**
     * Reuse the View corresponding to position
     */
    private void reuseItemOnScroll(int position, boolean addViewFromTop) {
        View scrap = recycler.getViewForPosition(position);
        measureChildWithMargins(scrap, 0, 0);
        scrap.setPivotY(0);
        scrap.setPivotX(scrap.getMeasuredWidth() / 2F);
        
        if (addViewFromTop) {
            addView(scrap, 0);
        }
        else {
            addView(scrap);
        }
        // Layout this Item
        layoutItem(scrap, locationRects.get(position));
        attachedItems.put(position, true);
    }
    
    
    private void layoutItem(View child, Rect rect) {
        int topDistance = scroll - rect.top;
        int layoutTop, layoutBottom;
        int itemHeight = rect.bottom - rect.top;
        if (topDistance < itemHeight && topDistance > 0) {
            float rate1 = (float) topDistance / itemHeight;
            float rate2 = 1 - rate1 * rate1 / 3;
            float rate3 = 1 - rate1 * rate1;
            child.setScaleX(rate2);
            child.setScaleY(rate2);
            child.setAlpha(rate3);
            layoutTop = 0;
            layoutBottom = itemHeight;
        }
        else {
            child.setScaleX(1);
            child.setScaleY(1);
            child.setAlpha(1);
            
            layoutTop = rect.top - scroll;
            layoutBottom = rect.bottom - scroll;
        }
        layoutDecorated(child, rect.left, layoutTop, rect.right, layoutBottom);
    }
    
    @Override
    public boolean canScrollVertically() {
        return true;
    }
    
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0 || dy == 0) {
            return 0;
        }
        int travel = dy;
        if (dy + scroll < 0) {
            travel = -scroll;
        }
        else if (dy + scroll > maxScroll) {
            travel = maxScroll - scroll;
        }
        scroll += travel; //累计偏移量
        lastDy = dy;
        if (!state.isPreLayout() && getChildCount() > 0) {
            layoutItemsOnScroll();
        }
        
        return travel;
    }
    
    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        new StartSnapHelper().attachToRecyclerView(view);
    }
    
    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            needSnap = true;
        }
        super.onScrollStateChanged(state);
    }
    
    public int getSnapHeight() {
        if (!needSnap) {
            return 0;
        }
        needSnap = false;
        
        Rect displayRect = new Rect(0, scroll, getWidth(), getHeight() + scroll);
        int itemCount = getItemCount();
        for (int i = 0; i < itemCount; i++) {
            Rect itemRect = locationRects.get(i);
            if (displayRect.intersect(itemRect)) {
                
                if (lastDy > 0) {
                    // The scroll becomes larger, it belongs to the list and goes down, and the next one is snapView.
                    if (i < itemCount - 1) {
                        Rect nextRect = locationRects.get(i + 1);
                        return nextRect.top - displayRect.top;
                    }
                }
                return itemRect.top - displayRect.top;
            }
        }
        return 0;
    }
    
    public View findSnapView() {
        if (getChildCount() > 0) {
            return getChildAt(0);
        }
        return null;
    }
}
