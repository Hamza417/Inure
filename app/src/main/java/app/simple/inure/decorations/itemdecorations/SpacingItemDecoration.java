package app.simple.inure.decorations.itemdecorations;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int space;
    private boolean hasHeader = false;
    
    public SpacingItemDecoration(int space) {
        this.space = space;
    }
    
    public SpacingItemDecoration(int space, boolean hasHeader) {
        this.space = space;
        this.hasHeader = hasHeader;
    }
    
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NotNull View view, @NonNull RecyclerView parent, @NotNull RecyclerView.State state) {
        if (hasHeader) {
            if (parent.getChildAdapterPosition(view) == 0) {
                Log.d("SpacingItemDecoration", "Item is header: " + parent.getChildAdapterPosition(view));
            } else {
                if (parent.getChildAdapterPosition(view) % 2 == 1) {
                    Log.d("SpacingItemDecoration", "Item on left side: " + parent.getChildAdapterPosition(view));
                    outRect.left = space;
                    outRect.right = space / 2;
                } else {
                    Log.d("SpacingItemDecoration", "Item on right side: " + parent.getChildAdapterPosition(view));
                    outRect.right = space;
                    outRect.left = space / 2;
                }
                
                outRect.top = space / 2;
                outRect.bottom = space / 2;
            }
        } else {
            if (parent.getChildAdapterPosition(view) % 2 == 0) {
                outRect.left = space / 2;
                outRect.right = space;
            } else {
                outRect.right = space / 2;
                outRect.left = space;
            }
            
            if (parent.getChildAdapterPosition(view) > 3) {
                outRect.top = space / 2;
            } else {
                outRect.top = space;
            }
            
            outRect.bottom = space / 2;
        }
    }
}
