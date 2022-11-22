package app.simple.inure.decorations.itemlistener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class CustomRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    
    int scrollDist = 0;
    boolean isVisible = true;
    static final float MINIMUM = 25;
    
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        
        if (isVisible && scrollDist > MINIMUM) {
            hide(dx, dy);
            scrollDist = 0;
            isVisible = false;
        } else if (!isVisible && scrollDist < -MINIMUM) {
            show(dx, dy);
            scrollDist = 0;
            isVisible = true;
        }
        
        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }
    }
    
    public abstract void show(int dx, int dy);
    
    public abstract void hide(int dx, int dy);
}
